import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Test {

    public static void main(String[] args){
        System.out.println(executeCode("C:\\\\Program Files\\\\Git\\\\bin\\\\bash.exe", "date"));
    }



    private static String executeCode(String interpreteur, String code) {
        String interpreteurPath = interpreteur;
        if (interpreteurPath == null) {
            return "Unknown interpreter: " + interpreteur;
        }

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(interpreteurPath, "-c", code);
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
}
