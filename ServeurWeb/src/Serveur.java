import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Serveur {

    public static void main(String[] args) {
        int port = 80;
        Socket s;
        byte[] page;

        if (args.length == 1) {
            port = Integer.parseInt(args[0]);
        }

        try {
            ServerSocket serveur = new ServerSocket(port);
            while (true) {
                s = serveur.accept();
                BufferedReader read = new BufferedReader(new InputStreamReader(s.getInputStream()));
                envoyer(read.readLine(), s);
                read.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void envoyer(String s, Socket soc) throws IOException {
        String res = s.replace("GET /", "").replace(" HTTP/1.1", "").trim();
        FileInputStream r = null;
        try {
            if (res.length() == 0) r = new FileInputStream("src/site/index.html");
            else r = new FileInputStream("src/site/" + res);
            DataOutputStream out = new DataOutputStream(soc.getOutputStream());
            int byteRead;
            while ((byteRead = r.read()) != -1) {
                out.write(byteRead);
                out.flush();
            }
            out.close();
            r.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
