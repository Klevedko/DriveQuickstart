package Reports;

import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.PermissionList;
import map.FileIdMap;

import java.util.List;

import static Reports.StaticReport.*;

class finderThread implements Runnable {
    private String q;

    public finderThread(String income_element) {
        this.q = income_element;
    }

    public void run() {
        try {
            System.out.println();
            Thread.sleep(800);
            final FileList fileList = get_driveservice_v3_files(q);
            final List<File> listFile = fileList.getFiles();
            for (final File f : listFile) {
                System.out.println("ssssstarting new thread WITH NAME= " + f.getName());
                fileIdMap.add(new FileIdMap(f.getId(), f.getName(), f.getWebViewLink()));
                querry_deeper = "'" + f.getId() + "'  in parents and trashed=false";
                System.out.println(querry_deeper);
                Runnable finderworker = new finderThread(querry_deeper);
                findfutures.add(findexecutor.submit(finderworker));
            }
        } catch (Exception ss) {
        }

    }
}