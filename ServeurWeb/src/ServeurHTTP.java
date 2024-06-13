import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ServeurHTTP {
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
    String[][]supp= {
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
     * @throws IOException
     */
    public ServeurHTTP() throws IOException {
        this.lireConfig("etc/myweb/config.xml");
        this.c = new Convertisseur();
    }

    /**
     * Envoie les données au serveur
     * @param s le string envoyé par le serveur
     * @param soc la socket
     * @throws IOException
     */
    public void envoyer(String s, Socket soc) throws IOException {
        String res = s.replace("GET /", "").replace(" HTTP/1.1", "").trim();
        FileInputStream r = null;
        this.addLogin(soc.toString(), res);
        try {
            String name;
            if (res.isEmpty()) name = "etc/index.html";
            else name = "etc/" + res;
            System.out.println(soc.isClosed());


            if (name.equals("etc/status"))LireDisque.ecrireInfo(name);

            if(name.contains("mp3") || name.contains("mp4") || name.contains("img") || name.contains("jpg") || name.contains("jpeg") || name.contains("ico")){
                String fichierEncoder =this.c.convertir(name);
                System.out.println(fichierEncoder);
                soc.getOutputStream().write(fichierEncoder.getBytes());
                soc.getOutputStream().flush();
                soc.getOutputStream().close();
            }
            else {
                r = new FileInputStream(name);
                DataOutputStream out = new DataOutputStream(soc.getOutputStream());
                int byteRead;
                while ((byteRead = r.read()) != -1) {
                    out.write(byteRead);
                    out.flush();
                }
                out.close();
                r.close();
            }


        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Permet de lire le fichie config.xml et de récupérer les infos
     * @throws IOException
     */
    public void  lireConfig(String path) throws IOException{
        BufferedReader br = new BufferedReader(new FileReader(path));
        String line;
        String[] info = new String[6];
        br.readLine();
        int i=0;
        while(br.ready() && i<6){
            line = br.readLine();
            info[i] = line.replace(supp[i][0], "").replace(supp[i][1], "").trim();
            i++;
        }
        this.port = Integer.parseInt(info[0]);
        this.ipOK = this.toBinaire(info[2].split("/")[0]);;
        this.ipBan =this.toBinaire(info[3].split("/")[0]);
        this.serverSocket = new ServerSocket(this.port);
        this.acces = new Log(info[4]);
        this.erreur = new Log(info[5]);
        int m = Integer.parseInt(info[2].split("/")[1]);
        for (int j= 0; j<masque.length;j++){
            if (j<m)masque[j] = 1;
            else masque[j] = 0;
        }
    }

    /**
     * Permet de savoir si une ip est accepté ou non
     * @param ip l'adresse ip String
     * @return
     */
    public boolean validerConnexion(String ip){
        byte[] s = toBinaire(ip);
        byte[] res = new byte[s.length];
        for (int i=0; i<s.length; i++){
            res[i] = (byte) (this.masque[i] & s[i]);
        }

        for (int i=0; i<res.length; i++){
            if(res[i] != this.ipOK[i]){
                return false;
            }
        }
        return true;
    }

    public void addLogin(String ip, String page) throws IOException {
        this.acces.ajout(ip +" "+ page);
    }
    public void addError(String error) throws IOException {
        this.erreur.ajout(error);
    }


    /**
     * Transforme un String en byte
     * @param ip adresse en String
     * @return tableau de byte
     */
    public byte[] toBinaire(String ip){
        String[] parts = ip.split("\\.");
        byte[] ipBytes = new byte[32];

        for (int i=0; i<parts.length; i++){
            int x = Integer.parseInt(parts[i]);

            for (int j = 0; j < 8; j++) {
                if (x % 2 == 1) ipBytes[(7-j)+i*8] = 1;
                else ipBytes[(7-j)+i*8] = 0;
                x /= 2;
            }
        }
        return ipBytes;
    }



    /**
     * Utiliser pour le toString afin de lire un tableau de byte
     * @param ip une addresse ip en byte
     * @return
     */
    public String voirIP(byte[] ip){
        String res = "";
        for (int i=0; i<ip.length; i++){
            if (i%8==0 && i!=0) res+=".";
            res+= ip[i];
        }
        return res;
    }

    /**
     *
     * @return les informations utiles du serveur
     */
    @Override
    public String toString() {
        String s;
        s="Port : "+this.port+'\n';
        s+="IpOk : "+voirIP(this.ipOK)+'\n';
        s+="IpBan : "+voirIP(this.ipBan)+'\n';
        s+="Masque : "+voirIP(this.masque)+'\n';
        return s;
    }
}
