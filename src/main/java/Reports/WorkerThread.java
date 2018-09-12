package Reports;

import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.PermissionList;
import map.FileIdMap;

import java.util.List;

import static Reports.StaticReport.*;

class WorkerThread implements Runnable {
    private FileIdMap elemet;

    public WorkerThread(FileIdMap income_element) {
        this.elemet = income_element;
    }

    public void run() {
        {
            System.out.println("starting new thread WITH NAME= " + elemet.getName());
            ownersList = "";
            allEmailFromINovus = true;
            try {
                Thread.sleep(400);
                PermissionList permissionList = driveservice.permissions().list(elemet.getId()).setFields("permissions(displayName, emailAddress, role)")
                        .execute();
                List<com.google.api.services.drive.model.Permission> p = permissionList.getPermissions();
                for (com.google.api.services.drive.model.Permission pe : p) {
                    ownersList += pe.getDisplayName() + " ( " + pe.getEmailAddress() + " ) : " + pe.getRole() + "\n";
                    if (pe.getEmailAddress() != null) {
                        if (!(pe.getEmailAddress().toLowerCase().contains("@i-novus"))) {
                            allEmailFromINovus = false;
                        }
                        if ((pe.getRole().equals("owner"))) {
                            realOwner = pe.getDisplayName() + " ( " + pe.getEmailAddress() + " )";
                        }
                    }
                }
                elemet.setIdreal_owner(realOwner);
                elemet.setIdowners(ownersList);
                elemet.setIdInovus(allEmailFromINovus);
            } catch (Exception e) {
                System.out.println("getOwners = " + e.getMessage() + e.getLocalizedMessage());
            }
            System.out.println("DONE thread WITH NAME= " + elemet.getName());

        }
    }
}