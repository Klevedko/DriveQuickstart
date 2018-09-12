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
import map.FileIdMap;
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

public class StaticReport {

    public static ArrayList<AuditMap> resultMap = new ArrayList<AuditMap>();
    public static ArrayList<FileIdMap> fileIdMap = new ArrayList<FileIdMap>();
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
    public static ExecutorService executor = Executors.newFixedThreadPool(6);
    public static List<Future<?>> futures = new ArrayList<>();

    static {
        try {
            driveservice = Apiv3.Drive();
        } catch (Exception e) {
        }
    }

    public static void main(String[] args) {
        // блок для CRON - не запускаем, пока не выполнился предыдущий шаг
        /*if (running) {
            return;
        }*/
        // запустили
        running = true;
        try {
            System.out.println("start " + new Date());
            String startFolderId = "0B3jemUSF0v3dTmtnektTSDNlelk";
            //String startFolderId = "0B3jemUSF0v3dVFN6Wk8taXdLcms";
            String query = "'" + startFolderId + "'  in parents and trashed=false";
            System.out.println("---------------- STATIC RUN ---------------- ");
            FileList fileList = get_driveservice_v3_files(query);
            List<File> listFile = fileList.getFiles();
            deeper_in_folders(listFile);
            System.out.println("------show me fileidmap");
          /*  for (int i = 0; i < fileIdMap.size(); i++) {
                System.out.println(fileIdMap.get(i).getId());
            }*/

            System.out.println("----- start THREADS");
            for (int i = 0; i < fileIdMap.size(); i++) {
                Runnable worker = new WorkerThread(fileIdMap.get(i));
                futures.add(executor.submit(worker));
            }
            for (Future<?> feature : futures) {
                while (!feature.isDone())
                    TimeUnit.SECONDS.sleep(1);
                feature.get();
            }

            write_to_file(fileIdMap);
            String WebViewLink = CreateGoogleFile.main(resultfile);
            SendMail.main(resultfile, WebViewLink);
            System.out.println("end " + new Date());

            // Первый step Cron пройден
            // running = false;
        } catch (Exception exec) {
            try {
                SimpleEmail.generateAndSendEmail();
            } catch (Exception global) {
            }
        } finally {
            executor.shutdown();
        }
    }

    public static FileList get_driveservice_v3_files(String query) {
        try {
            return driveservice.files().list().setQ(query).setFields("nextPageToken, " +
                    "files(id, name, owners, webViewLink, owners, mimeType)").execute();
            //, sharingUser(emailAddress, permissionId)
        } catch (Exception x) {
            System.out.println("get_driveservice_v3_files = " + x);
        }
        return fileList;
    }

    public static void deeper_in_folders(List<File> file) {
        for (File f : file) {
            try {
                if (f.getMimeType().equals("application/vnd.google-apps.folder") || f.getMimeType().equals("folder")) {
                    System.out.println(f.getName());
                    querry_deeper = "'" + f.getId() + "'  in parents and trashed=false";
                    deeper_in_folders(get_driveservice_v3_files(querry_deeper).getFiles());
                }
                else
                    fileIdMap.add(new FileIdMap(f.getId(), f.getName(), f.getWebViewLink(),
                            "", "", false));
            } catch (Exception ss) {
                System.out.println("deeper_in_folders = " + f.getName() + ss);
            }
        }
    }

    // Метод возвращает список владельцев. Переопределяет  allEmailFromINovus  и  realOwner

    public static void write_to_file(ArrayList<FileIdMap> fileIdMap) {
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

            for (FileIdMap product : fileIdMap) {
                dataRow = list.createRow(row);
                cell = dataRow.createCell(0);
                cell.setCellValue(product.getName());
                cell = dataRow.createCell(1);
                cell.setCellValue(product.getIdreal_owner());
                cell = dataRow.createCell(2);
                cell.setCellValue(product.getWebViewLink());
                cell = dataRow.createCell(3);
                cell.setCellValue(product.getIdowners());
                cell = dataRow.createCell(4);
                cell.setCellValue(product.getIdInovus().toString());
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