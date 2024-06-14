import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Serveur {
    int port;
    static Socket s;
    byte[] page;

    public static void main(String[] args) {
        ServeurHTTP serveur = null;
        try {
            serveur = new ServeurHTTP();
        } catch (IOException e) {
            throw new RuntimeException(e);
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
