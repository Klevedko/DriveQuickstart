package Reports;

import api.assistive.SimpleEmail;
import api.authorize.Apiv3;
import api.Google.CreateGoogleFile;
import api.assistive.SendMail;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.PermissionList;
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
import java.util.concurrent.*;

public class StaticReport implements Job {
    public static ExecutorService executor = Executors.newFixedThreadPool(6);//creating a pool of 5 threads

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
    public static List<Future<Object>> futures = new ArrayList<>();

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
            System.out.println("start " + new Date());
            String startFolderId = "'" + "1tP-IDq3DksMYA1HPMuubADEllTxCQ04j" + "'  in parents and trashed=false";

            // ------------------  threads start! ----------------------
            futures.add(executor.submit(new Callable<Object>() {
                public Object call() throws Exception {
                    return null;
                }
            }));
            Runnable worker = new WorkerThread(startFolderId);
                executor.execute(worker);//calling execute method of ExecutorService
            while (executor.isTerminated()) {
                System.out.println("Finished all threads");
                write_to_file(resultMap);
                String WebViewLink = CreateGoogleFile.main(resultfile);
                SendMail.main(resultfile, WebViewLink);
                System.out.println("end " + new Date());
            }
            // Первый step Cron пройден
            // running = false;
        } catch (Exception exec) {
            try {
                SimpleEmail.generateAndSendEmail();
            } catch (Exception global) {
            }
        }
    }


    // Метод возвращает список владельцев. Переопределяет  allEmailFromINovus  и  realOwner
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
            System.out.println("getOwners = " + e.getMessage() + e.getLocalizedMessage());
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
                row++;
            }
            wb.write(fileout);
            fileout.close();
        } catch (Exception e) {
            System.out.println("write_to_file = " + e);
            System.exit(0);
        }
    }
}