package api;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.PermissionList;
import map.AuditMap;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Apiv1v3crontest implements Job {

    public static ArrayList<AuditMap> resultMap = new ArrayList<AuditMap>();
    public static DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss:ms");
    public static String resultfile="audit_result_";
    // Переменные для запуска CRON и логики
    public static boolean running = false;
    public static Boolean allEmailFromINovus;
    public static boolean needReMap = false;
    public static boolean needmail = false;
    public static FileList fileList;

    public void execute(JobExecutionContext context) throws JobExecutionException {
        System.out.println(dateFormat.format(new Date()));
        // блок для CRON - не запускаем, пока не выполнился предыдущий шаг
        if (running) {
            return;
        }
        // запустили
        running = true;
        needmail = false;
        System.out.println("11111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111");
        try {
            String FileId = "1wlyj65snRXW5QXhp5wJD54eUxtZm7CNZ";
            String query = "'" + FileId + "'  in parents and trashed=false";
            FileList fileList = drive_v3(query);
            List<File> activities = fileList.getFiles();
            deeper_in_folders(activities);

            SendMail.main(write_to_file(resultMap));
            // Первый step Cron пройден
            running = false;
        } catch (Exception exec) {
        }
    }

    public static FileList drive_v3(String query) {
        try {
            Drive driveservice = Apiv3.Drive();
            return driveservice.files().list().setQ(query).execute();
        } catch (Exception x) {
        }
        return fileList;
    }

    public static void deeper_in_folders(List<File> file) {
        needReMap = false;
        for (File f : file) {
            try {
                String querry_deeper = "'" + f.getId() + "'  in parents and trashed=false";
                deeper_in_folders(drive_v3(querry_deeper).getFiles());
            } catch (Exception ss) {
            }
            System.out.println(f.getName());
            AuditMap candy = new AuditMap(f.getName() , getOwners(f.getId()));
            resultMap.add(candy);
        }
    }

    public static String getOwners(String fileid) {
        String s = "";
        allEmailFromINovus = true;
        try {
            Drive driveservice = Apiv3.Drive();
            PermissionList permissionList = driveservice.permissions().list(fileid).setFields("permissions(id, displayName, emailAddress, role)")
                    .execute();
            List<com.google.api.services.drive.model.Permission> p = permissionList.getPermissions();
            for (com.google.api.services.drive.model.Permission pe : p) {
                s += pe.getDisplayName() + " ( " + pe.getEmailAddress() + " ) : " + pe.getRole() + "\n";
                if (pe.getEmailAddress() != null) {
                    if (!(pe.getEmailAddress().toLowerCase().contains("@i-novus"))) {
                        allEmailFromINovus=false;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("=====" + e.getMessage() + e.getLocalizedMessage());
        }
        return s;
    }

    public static String write_to_file(ArrayList<AuditMap> resultMap) {
        try {
            String audit_date= new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            resultfile=resultfile.concat(audit_date.concat(".xlsx"));
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
            cell.setCellValue("Current rights on file");
            cell = dataRow.createCell(2);
            cell.setCellValue("all from i-novus");
            row++;

            for (AuditMap product : resultMap) {
                dataRow = list.createRow(row);
                cell = dataRow.createCell(0);
                cell.setCellValue(product.getName());
                cell = dataRow.createCell(1);
                cell.setCellValue(product.getV3_getOwners());
                row++;
            }
            wb.write(fileout);
            fileout.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.exit(0);
        }
        return resultfile;
    }
}