import java.io.File;
import java.text.NumberFormat;

import javax.swing.filechooser.FileSystemView;

public class LireDisque {


    public static void main(String[] args)
    {
        File[] roots = File.listRoots();
        for (File root : roots) printInfos(root);
    }


    public static void printInfos(File root)
    {
        System.out.println("path: "+root.getAbsolutePath());
        System.out.println("drive: "+driveLetter(root));
        System.out.println("name: "+name(root));
        System.out.println("totalSpace: "+formatSize(root.getTotalSpace()));
        System.out.println("freeSpace: "+formatSize(root.getFreeSpace()));
        System.out.println();
    }



    public static String formatSize(Long size)
    {
        long sizeMO = (long) size /1000000;
        return nf.format(sizeMO)+" Mo";
    }

    public static String driveLetter(File root)
    {
        String path = root.getAbsolutePath();
        if(path.contains(":")) return path.split(":")[0];
        return "";
    }

    public static String name(File root)
    {
        String n = f.getSystemDisplayName(root);
        if(n.isEmpty() || n.equals("/")) return "";
        String p = root.getAbsolutePath().substring(0,2);
        if(n.endsWith("("+p+")")) n = n.substring(0,n.length()-p.length());
        return n;
    }

    public static FileSystemView f = FileSystemView.getFileSystemView();
    public static NumberFormat nf = NumberFormat.getInstance();

}