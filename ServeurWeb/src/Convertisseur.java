import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

public class Convertisseur {
    public String convertir(String name) throws IOException {
        byte[] fileContent = Files.readAllBytes(Paths.get(name));
        String encodedString = Base64.getEncoder().encodeToString(fileContent);
        return encodedString;
    }
}
