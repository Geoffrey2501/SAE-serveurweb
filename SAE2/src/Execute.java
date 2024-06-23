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

public class Execute {

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