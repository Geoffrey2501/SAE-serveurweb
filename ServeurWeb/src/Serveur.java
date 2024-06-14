import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Serveur {
    int port;
    static Socket s;
    byte[] page;

    public static void main(String[] args) throws IOException {
        ServeurHTTP serveur = null;
        try {
            serveur = new ServeurHTTP();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        while (true) {
                try {
                    s = serveur.serverSocket.accept();
                    System.out.println(s.getInetAddress());
                    //if(serveur.validerConnexion(s.getInetAddress().getHostAddress()))s.close();
                    BufferedReader read = new BufferedReader(new InputStreamReader(s.getInputStream()));
                    serveur.envoyer(read.readLine(), s);
                }
                catch (Exception e ) {
                    serveur.addError(e.toString());
                }
        }

    }

}
