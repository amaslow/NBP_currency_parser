package safetyinstructions;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Safetyinstructions {

    public static void main(String[] args) throws IOException {
        File source = new File("G:\\Product Content\\PRODUCTS\\0001030\\Safetyinstruction_0001030_20150115.pdf");
        File dir = new File("G:\\Product Content\\PRODUCTS\\");
        FileWriter fw = new FileWriter("H:/Logs/Safetyinstructions.log", true);
        BufferedWriter bw = new BufferedWriter(fw);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        File[] subDirs = dir.listFiles(new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        });

        for (File subDir : subDirs) {
            File[] subFiles = subDir.listFiles(new FileFilter() {

                @Override
                public boolean accept(File pathname) {
                    return pathname.isFile() && pathname.getName().startsWith("Safetyinstruction_");
                }
            });

            if (subFiles.length == 0) {
                String sap = subDir.getName();
                if (sap.length() == 7) {
                    File dest = new File(subDir.getAbsolutePath() + "\\Safetyinstruction_" + sap + "_20150115.pdf");
                    System.out.println(dest + " - " + sap);
                    bw.newLine();
                    bw.write(sdf.format(new Date()) + " - " + sap);
                    copyFile(source, dest);
                }
            }
        }
        bw.newLine();
        bw.write("----------------------------------------------");
        bw.flush();
        bw.close();
    }

    private static void copyFile(File src, File dstFile) throws IOException {
        InputStream in = null;
        try {
            in = new FileInputStream(src);
            OutputStream out = new FileOutputStream(dstFile);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Safetyinstructions.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            in.close();
        }

    }
}
