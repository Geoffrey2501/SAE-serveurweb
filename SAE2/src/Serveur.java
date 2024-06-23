import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.lang.management.ManagementFactory;


public class Serveur {
    int port;
    static Socket s;
    byte[] page;

    public static void main(String[] args) {
        // Obtenir le PID du processus actuel
        String pid = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];

        // Le chemin vers le fichier myweb.pid
        String pidFilePath = "var/run/myweb.pid";

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
}
