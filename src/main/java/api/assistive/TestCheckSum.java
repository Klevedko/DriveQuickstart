package api.assistive;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;

public class TestCheckSum {

    public static String main(String args[]) throws Exception {

        File f = new File("C:/IdeaProjects/DriveQuickstart/audit_results.xls");
        if (f.exists()) {
            String datafile = "C:/IdeaProjects/DriveQuickstart/audit_results.xls";
            MessageDigest md = MessageDigest.getInstance("SHA1");
            FileInputStream fis = new FileInputStream(datafile);
            byte[] dataBytes = new byte[1024];

            int nread = 0;

            while ((nread = fis.read(dataBytes)) != -1) {
                md.update(dataBytes, 0, nread);
            }

            byte[] mdbytes = md.digest();

            //convert the byte to hex format
            StringBuffer sb = new StringBuffer("");
            for (int i = 0; i < mdbytes.length; i++) {
                sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            //System.out.println("Digest(in hex format):: " + sb.toString());
            return sb.toString();
        } else {
            System.out.println("new file");
            return "ERROR HASH";
        }
    }
}