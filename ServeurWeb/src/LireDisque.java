import java.io.File;
import java.text.NumberFormat;

public class LireDisque {
    public static NumberFormat nf = NumberFormat.getInstance();

    public static void main(String[] args) {
        System.out.println("Available processors (cores): " +
                Runtime.getRuntime().availableProcessors());

        System.out.println("Free memory (bytes): " +
                formatSize(Runtime.getRuntime().freeMemory()));

        File[] roots = File.listRoots();

        for (File root : roots) {
            System.out.println("File system root: " + root.getAbsolutePath());
            System.out.println("Free space (bytes): " + formatSize(root.getFreeSpace()));;
        }
    }



    public static String formatSize(Long size)
    {
        long sizeMO = (long) size /1000000;
        return nf.format(sizeMO)+" Mo";
    }

}