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


    /**
     *
     * Fonction qui revoie un string d'un fichier avec les codes exécutés
     * @param fichier le nom d'un fichier a faire exécuté
     * @return
     * @throws Exception
     */
    public String obtenirPageExecuter(String fichier) throws Exception {
        String s = readFile(fichier);
        return extractCodeSnippets(s);
    }



    private String executeCode(String interpreter, String code) {

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


    private String readFile(String filePath) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }

    private String extractCodeSnippets(String htmlContent) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new InputSource(new StringReader(htmlContent)));

        NodeList codeList = document.getElementsByTagName("code");
        for (int i = 0; i < codeList.getLength(); i++) {
            Element codeElement = (Element) codeList.item(i);
            String interpreter = codeElement.getAttribute("interpreteur");
            String codeContent = codeElement.getTextContent().trim();

            // Execute the code and get the result
            String result = executeCode(interpreter, codeContent);

            // Replace the code content with the result
            codeElement.setTextContent(result);
        }

        // Convert the document back to a string
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(document), new StreamResult(writer));
        return writer.getBuffer().toString();
    }
}
