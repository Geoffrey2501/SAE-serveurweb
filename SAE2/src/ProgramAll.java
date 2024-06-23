import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;



public class ProgramAll {
    int port;
    static Socket s;
    byte[] page;

    public static void main(String[] args) {
        // Obtenir le PID du processus actuel
        String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];

        // Le chemin vers le fichier myweb.pid
        String pidFilePath = "/var/run/myweb.pid";

        // Écrire le PID dans le fichier
        try (FileWriter writer = new FileWriter(new File(pidFilePath))) {
            writer.write(pid);
        } catch (IOException e) {
            System.err.println("Erreur lors de l'écriture du fichier PID : " + e.getMessage());
            e.printStackTrace();
        }


        ServeurHTTP serveur = null;
        try {
            serveur = new ServeurHTTP();
        } catch (IOException e1) {
            throw new RuntimeException(e1);
        }
        while (true) {
            try {
                s = serveur.serverSocket.accept();
                System.out.println(s.getInetAddress().toString());
                //if(serveur.validerConnexion(s.getInetAddress().getHostAddress()))s.close();
                BufferedReader read = new BufferedReader(new InputStreamReader(s.getInputStream()));
                serveur.envoyer(read.readLine(), s);
            }catch (NumberFormatException e1){
                e1.printStackTrace();
            }
            catch (IOException | InterruptedException e2) {
                e2.printStackTrace();
            }
        }
    }


    public static class ServeurHTTP {
        int port;
        Socket s;
        byte[] page;
        ServerSocket serverSocket;
        Log acces;
        Log erreur;
        Convertisseur c;
        /**
         * Le masque des différents ip accepté ou refusé
         */
        byte[] masque = new byte[32];
        /**
         * Les strings à supprimer dans le fichier xml
         */
        String[][] supp = {
                {"<port>", "</port>"},
                {"<root>", "</root>"},
                {"<accept>", "</accept>"},
                {"<reject>", "</reject>"},
                {"<accesslog>", "</accesslog>"},
                {"<errorlog>", "</errorlog>"},
        };

        //Les ip acceptés et refusés
        byte[] ipOK;
        byte[] ipBan;

        /**
         * constructeur de la classe
         *
         * @throws IOException
         */
        public ServeurHTTP() throws IOException {
            this.lireConfig("/etc/myweb/myweb.conf");
            this.c = new Convertisseur();
        }

        /**
         * Envoie les données au serveur
         *
         * @param s   le string envoyé par le serveur
         * @param soc la socket
         * @throws IOException
         */
        public void envoyer(String s, Socket soc) throws IOException, InterruptedException {
            String res = s.replace("GET /", "").replace(" HTTP/1.1", "").trim();
            this.addLogin(soc.toString(), res);
            try {
                String name;
                if (res.isEmpty()) name = "etc/index.html";
                else name = "etc/" + res;
                System.out.println(soc.isClosed());


                if (name.equals("etc/status")) LireDisque.ecrireInfo(name);

                BufferedReader reader = new BufferedReader(new FileReader(name));
                DataOutputStream out = new DataOutputStream(soc.getOutputStream());
                String line;
                while ((line = reader.readLine()) != null) {
                    if(line.contains("<img")){
                        line = line.replace("src=\"", "");
                        line = line.replace("<img", "");
                        line = line.replace("\">", "").trim();
                        System.out.println(line);
                        String o = this.c.convertir(line);
                        out.write(("<img src=\"data:image/png;base64,"+o+"\" >" ).getBytes("UTF-8"));
                        out.write("\n".getBytes("UTF-8"));
                        out.flush();
                    }
                    else if(line.contains("<source")){
                        line = line.replace("<source", "").trim();
                        line = line.replace("src=\"", "");
                        line = line.replace("\">", "").trim();
                        System.out.println(line);
                        String o = this.c.convertir(line);
                        out.write(("<source src=\"data:video/mp4;base64,"+o+"\" >" ).getBytes("UTF-8"));
                        out.write("\n".getBytes("UTF-8"));
                        out.flush();
                    }
                    else if(line.contains("<code")) {
                        line = Execute.extractCodeSnippets(line);
                        out.write(line.getBytes("UTF-8"));
                        out.write("\n".getBytes("UTF-8"));
                        out.flush();
                    }
                    else {
                        out.write(line.getBytes("UTF-8"));
                        out.write("\n".getBytes("UTF-8"));
                        out.flush();
                    }


                }
                out.close();
                reader.close();


            } catch (Exception e1) {
                throw new RuntimeException(e1);
            }
        }

        /**
         * Permet de lire le fichie config.xml et de récupérer les infos
         *
         * @throws IOException
         */
        public void lireConfig(String path) throws IOException {
            BufferedReader br = new BufferedReader(new FileReader(path));
            String line;
            String[] info = new String[6];
            br.readLine();
            int i = 0;
            while (br.ready() && i < 6) {
                line = br.readLine();

                info[i] = line.replace(supp[i][0], "").replace(supp[i][1], "").trim();
                System.out.println(info[i]);
                i++;
            }
            this.port = Integer.parseInt(info[0]);
            this.ipOK = this.toBinaire(info[2].split("/")[0]);
            ;
            this.ipBan = this.toBinaire(info[3].split("/")[0]);
            this.serverSocket = new ServerSocket(this.port);

            this.acces = new Log(info[4]);
            this.erreur = new Log(info[5]);
            int m = Integer.parseInt(info[2].split("/")[1]);
            for (int j = 0; j < masque.length; j++) {
                if (j < m) masque[j] = 1;
                else masque[j] = 0;
            }
        }


        /**
         * Permet de savoir si une ip est accepté ou non
         *
         * @param ip l'adresse ip String
         * @return
         */
        public boolean validerConnexion(String ip) {
            byte[] s = toBinaire(ip);
            byte[] res = new byte[s.length];
            for (int i = 0; i < s.length; i++) {
                res[i] = (byte) (this.masque[i] & s[i]);
            }

            for (int i = 0; i < res.length; i++) {
                if (res[i] != this.ipOK[i]) {
                    return false;
                }
            }
            return true;
        }

        public void addLogin(String ip, String page) throws IOException {
            this.acces.ajout(ip + " " + page);
        }

        public void addError(String error) throws IOException {
            this.erreur.ajout(error);
        }


        /**
         * Transforme un String en byte
         *
         * @param ip adresse en String
         * @return tableau de byte
         */
        public byte[] toBinaire(String ip) {
            String[] parts = ip.split("\\.");
            byte[] ipBytes = new byte[32];

            for (int i = 0; i < parts.length; i++) {
                int x = Integer.parseInt(parts[i]);

                for (int j = 0; j < 8; j++) {
                    if (x % 2 == 1) ipBytes[(7 - j) + i * 8] = 1;
                    else ipBytes[(7 - j) + i * 8] = 0;
                    x /= 2;
                }
            }
            return ipBytes;
        }


        /**
         * Utiliser pour le toString afin de lire un tableau de byte
         *
         * @param ip une addresse ip en byte
         * @return
         */
        public String voirIP(byte[] ip) {
            String res = "";
            for (int i = 0; i < ip.length; i++) {
                if (i % 8 == 0 && i != 0) res += ".";
                res += ip[i];
            }
            return res;
        }

        /**
         * @return les informations utiles du serveur
         */
        @Override
        public String toString() {
            String s;
            s = "Port : " + this.port + '\n';
            s += "IpOk : " + voirIP(this.ipOK) + '\n';
            s += "IpBan : " + voirIP(this.ipBan) + '\n';
            s += "Masque : " + voirIP(this.masque) + '\n';
            return s;
        }


        public static List<String> splitString(String input, int length) {
            List<String> parts = new ArrayList<>();

            int start = 0;
            while (start < input.length()) {
                int end = Math.min(start + length, input.length());
                parts.add(input.substring(start, end));
                start += length;
            }

            return parts;
        }
    }

    public static class Log {
        String name;
        BufferedWriter bf;
        public Log(String name) {
            this.name = name;
            try {
                this.bf = new BufferedWriter(new FileWriter(this.name));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public void ajout(String msg) throws IOException {
            bf.newLine();
            bf.write(msg);
            bf.flush();
        }


        public void end() throws IOException {
            this.bf.close();
        }
    }

    public static class LireDisque {
        public static NumberFormat nf = NumberFormat.getInstance();

        /**
         * Fonction qui récupère les informations sur la machines est les écrite dans une page donnée
         */
        public static void ecrireInfo(String f) throws IOException {

            BufferedWriter b = new BufferedWriter(new FileWriter(f));


            double nbProc = Runtime.getRuntime().availableProcessors();
            String memory = formatSize(Runtime.getRuntime().freeMemory());
            b.write("Nombre de processus : "+String.valueOf(nbProc));
            b.newLine();
            b.write("Mémoire disponible : "+memory);
            File[] roots = File.listRoots();
            for (File root : roots) {
                b.newLine();
                String name = root.getAbsolutePath();
                String space = formatSize(root.getFreeSpace());
                b.write("Nom disque : "+name+" / espace disponible :"+ space);
            }



            b.close();
        }

        public static String formatSize(Long size)
        {
            long sizeMO = (long) size /1000000;
            return nf.format(sizeMO)+" Mo";
        }



    }


    public static class Convertisseur {
        public String convertir(String name) throws IOException {
            byte[] fileContent = Files.readAllBytes(Paths.get("etc/"+name));
            String encodedString = Base64.getEncoder().encodeToString(fileContent);
            return encodedString;
        }
    }



    public static class Execute {

        private static String executeCode(String interpreter, String code) {
            try {
                ProcessBuilder processBuilder = new ProcessBuilder(interpreter, "-c", code);
                processBuilder.redirectErrorStream(true);
                Process process = processBuilder.start();

                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line).append("\n");
                }

                process.waitFor();
                return result.toString().trim();
            } catch (Exception e) {
                e.printStackTrace();
                return "Error executing code: " + e.getMessage();
            }
        }


        /**
         *
         * Fonction qui revoie un string d'un fichier avec les codes exécutés
         * @param input le string d'un fichier
         * @return
         * @throws Exception
         */
        private static String extractCodeSnippets(String input) throws Exception {
            String interpreter = input.substring(input.indexOf("interpreteur=«")+14, input.indexOf("»>"));
            String code = input.substring(input.indexOf(">")+1, input.indexOf("</code>"));

            input = input.replace(input.substring(input.indexOf(">"), input.indexOf("</code>")+7),  executeCode(interpreter, code));
            input = input.replace(input.substring(input.indexOf("<code interpreteur=«"), input.indexOf("»")), "");

            return input;
        }
    }
}



