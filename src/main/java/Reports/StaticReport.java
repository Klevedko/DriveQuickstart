package Reports;

import api.Apiv3;
import api.CreateGoogleFile;
import api.SendMail;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.PermissionList;
import com.google.api.services.drive.model.User;
import map.AuditMap;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StaticReport implements Job {

    public static ArrayList<AuditMap> resultMap = new ArrayList<AuditMap>();
    public static String resultfiletemplate = "Static_audit_result_";
    public static String resultfile = "";
    // Переменные для запуска CRON и логики
    public static boolean running = false;
    public static Boolean allEmailFromINovus;
    public static FileList fileList;
    public static String owners = "";
    public static String querry_deeper = "";
    public static Drive driveservice;
    public static String ownersList;
    public static String realOwner;

    static {
        try {
            driveservice = Apiv3.Drive();
        } catch (Exception e) {
        }
    }

    public void execute(JobExecutionContext context) throws JobExecutionException {
        // блок для CRON - не запускаем, пока не выполнился предыдущий шаг
        if (running) {
            return;
        }
        // запустили
        running = true;
        System.out.println("---------------- STATIC REPORT RUNS ---------------- ");
        try {
            System.out.println(new Date());
            String FileId = "0B3jemUSF0v3dVFN6Wk8taXdLcms";
            String query = "'" + FileId + "'  in parents and trashed=false";
            FileList fileList = get_driveservice_v3_files(query);
            List<File> activities = fileList.getFiles();
            deeper_in_folders(activities);
            write_to_file(resultMap);
            String WebViewLink = CreateGoogleFile.main(resultfile);
            SendMail.main(resultfile, WebViewLink);
            System.out.println(new Date());

            // Первый step Cron пройден
            // running = false;
        } catch (Exception exec) {
        }
    }

    public static FileList get_driveservice_v3_files(String query) {
        try {
            return driveservice.files().list().setQ(query).setFields("nextPageToken, " +
                    "files(id, name, owners, parents, webViewLink, owners)").execute();
            //, sharingUser(emailAddress, permissionId)
        } catch (Exception x) {
            System.out.println(x);
            //System.exit(1);
        }
        return fileList;
    }

    public static void deeper_in_folders(List<File> file) {
        for (File f : file) {
            try {
                System.out.println(f.getName());
                owners = getOwners(f.getId());
                if (!allEmailFromINovus) {
                //AuditMap candy = new AuditMap(f.getName(), realOwner, f.getWebViewLink(), owners, allEmailFromINovus);
                resultMap.add(new AuditMap(f.getName(), realOwner, f.getWebViewLink(), owners, allEmailFromINovus));
                }
                querry_deeper = "'" + f.getId() + "'  in parents and trashed=false";
                deeper_in_folders(get_driveservice_v3_files(querry_deeper).getFiles());
            } catch (Exception ss) {
                System.out.println(ss);
            }
        }
    }

    public static String getOwners(String fileid) {
        ownersList = "";
        allEmailFromINovus = true;
        try {
            PermissionList permissionList = driveservice.permissions().list(fileid).setFields("permissions(displayName, emailAddress, role)")
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
        } catch (Exception e) {
            System.out.println("=====" + e.getMessage() + e.getLocalizedMessage());
        }
        return ownersList;
    }

    public static void write_to_file(ArrayList<AuditMap> resultMap) {
        try {
            String audit_date = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
            resultfile = resultfiletemplate.concat(audit_date.concat(".xlsx"));
            System.out.println("writing to the file....");
            XSSFWorkbook wb = new XSSFWorkbook();
            int row = 0;
            Cell cell;
            Sheet list = wb.createSheet("Go");
            String output = resultfile;
            FileOutputStream fileout;
            fileout = new FileOutputStream(output);
            Row dataRow = list.createRow(row);
            cell = dataRow.createCell(0);
            cell.setCellValue("File");
            cell = dataRow.createCell(1);
            cell.setCellValue("realOwner");
            cell = dataRow.createCell(2);
            cell.setCellValue("WebViewLink");
            cell = dataRow.createCell(3);
            cell.setCellValue("Current rights on file");
            cell = dataRow.createCell(4);
            cell.setCellValue("all from i-novus");
            row++;

            for (AuditMap product : resultMap) {
                dataRow = list.createRow(row);
                cell = dataRow.createCell(0);
                cell.setCellValue(product.getName());
                cell = dataRow.createCell(1);
                cell.setCellValue(product.getRealOnwer());
                cell = dataRow.createCell(2);
                cell.setCellValue(product.getWebViewLink());
                cell = dataRow.createCell(3);
                cell.setCellValue(product.getV3_owners());
                cell = dataRow.createCell(4);
                cell.setCellValue(product.getAllEmailFromINovus().toString());
                row++;
            }
            wb.write(fileout);
            fileout.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.exit(0);
        }
    }
}