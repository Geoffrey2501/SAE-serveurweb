import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Serveur {
    int port;
    static Socket s;
    byte[] page;

    public static void main(String[] args) throws IOException {
        try {
            ServeurHTTP serveur = new ServeurHTTP();
            while (true) {
                s = serveur.serverSocket.accept();
                BufferedReader read = new BufferedReader(new InputStreamReader(s.getInputStream()));
                serveur.envoyer(read.readLine(), s);
                read.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
