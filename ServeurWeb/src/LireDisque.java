import java.io.*;
import java.text.NumberFormat;

public class LireDisque {
    public static NumberFormat nf = NumberFormat.getInstance();

    /**
     * Fonction qui récupère les informations sur la machines est les écrite dans une page donnée
     */
    public static void ecrireInfo(String f) throws IOException {

        BufferedWriter b = new BufferedWriter(new FileWriter(f));


        double nbProc = Runtime.getRuntime().availableProcessors();
        String memory = formatSize(Runtime.getRuntime().freeMemory());
        b.write(String.valueOf(nbProc));
        b.write(memory);
        File[] roots = File.listRoots();
        for (File root : roots) {
            String name = root.getAbsolutePath();
            String space = formatSize(root.getFreeSpace());
            b.write(name+" : "+ space);
        }

        b.close();
    }

    public static String formatSize(Long size)
    {
        long sizeMO = (long) size /1000000;
        return nf.format(sizeMO)+" Mo";
    }



}