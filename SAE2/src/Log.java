import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class Log {
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
