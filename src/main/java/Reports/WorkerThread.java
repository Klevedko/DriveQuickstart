package Reports;

import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.PermissionList;
import com.sun.org.apache.xpath.internal.operations.Bool;
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
            //System.out.println("starting new thread WITH NAME= " + elemet.getName());
            String ownersList = "";
            String goodOwnersList = "";
            String badOwnersList = "";
            Boolean allEmailFromINovus = true;
            String realOwner = "";
            try {
                PermissionList permissionList = driveservice.permissions().list(elemet.getId()).setFields("permissions(displayName, emailAddress, role)")
                        .execute();
                List<com.google.api.services.drive.model.Permission> p = permissionList.getPermissions();
                for (com.google.api.services.drive.model.Permission pe : p) {
                    ownersList += pe.getDisplayName() + " ( " + pe.getEmailAddress() + " ) : " + pe.getRole() + "\n";
                    if (pe.getEmailAddress() != null) {
                        if (!(pe.getEmailAddress().toLowerCase().contains("@i-novus"))) {
                            badOwnersList += pe.getEmailAddress().toString()+ "\n";
                            allEmailFromINovus = false;
                        }
                        else{
                            goodOwnersList+=pe.getEmailAddress().toString()+ "\n";
                        }
                        if ((pe.getRole().equals("owner"))) {
                            realOwner = pe.getDisplayName() + " ( " + pe.getEmailAddress() + " )";
                        }
                    }
                }
                //if (!allEmailFromINovus) {
                //System.out.println("name- " + elemet.getId());System.out.println("realOwner= "+ realOwner);
                    elemet.setIdreal_owner(realOwner);
                    elemet.setIdowners(ownersList);
                    elemet.setGoodOwnersList(goodOwnersList);
                    elemet.setBadOwnersList(badOwnersList);
                    elemet.setIdInovus(allEmailFromINovus);
                //} else
                  //  fileIdMap.remove(elemet);
            } catch (Exception e) {
                System.out.println("REST ERROR = " + e.getMessage());
                fileIdMap.remove(elemet);
                System.out.println("removed Object with ID = " + elemet.getId());
            }
            System.out.println("DONE thread WITH NAME= " + elemet.getName());

        }
    }
}