package Reports;

import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import map.AuditMap;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import static Reports.StaticReport.*;

class GetIdsThread implements Runnable {
    private String message;

    public GetIdsThread(String s) {
        this.message = s;
    }

    public void run() {
        try {
            List<Future<Object>> futures = new ArrayList<>();
            List<File> listFile = get_driveservice_v3_files(this.message).getFiles();
            for (final File f : listFile) {
                futures.add(executor.submit(new Callable<Object>() {
                    public Object call() throws Exception {
                        return null;
                    }
                }));
                if (f.getMimeType().equals("application/vnd.google-apps.folder") || f.getMimeType().equals("folder")) {
                    System.out.println("folder= " + f.getName());
                    Thread.sleep(350);
                    String newId = "'" + f.getId() + "'  in parents and trashed=false";
                    Runnable worker = new GetIdsThread(newId);
                    executor.execute(worker);
                } else {
                    System.out.println("  file= " + f.getName() + resultMap.size());
                    resultMap.add(new AuditMap(f.getName(), realOwner, f.getWebViewLink(), owners, allEmailFromINovus));
//                    resultMap.add(new AuditMap(f.getName()));
                }
            }
        } catch (Exception tt) {
            System.out.println(tt);
        }
    }
public static void x(){

}
    private void processmessage() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static FileList get_driveservice_v3_files(String query) {
        try {
            return driveservice.files().list().setQ(query).setFields("nextPageToken, " +
                    "files(id, name, owners, parents, webViewLink, owners, mimeType, thumbnailLink)").execute();
            //, sharingUser(emailAddress, permissionId)
        } catch (Exception x) {
            System.out.println("get_driveservice_v3_files = " + x);
        }
        return fileList;
    }
}