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
import org.apache.poi.ss.usermodel.*;
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

    public static ArrayList<FileIdMap> fileIdMap = new ArrayList<FileIdMap>();
    public static String resultfiletemplate = "Static_audit_result_";
    public static String resultfile = "";
    public static FileOutputStream fileout;

    // Переменные для запуска CRON и логики
    public static boolean running = false;
    public static FileList fileList;
    public static String querry_deeper = "";
    public static Drive driveservice;
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
            String startFolderId = "1KQqgo-qELtFFK6hH_bHpzfaFbZg4WnFk";
            //String startFolderId = "1tP-IDq3DksMYA1HPMuubADEllTxCQ04j";
            String query = "'" + startFolderId + "'  in parents and trashed=false";
            System.out.println("---------------- STATIC RUN ---------------- ");
            FileList fileList = get_driveservice_v3_files(query);
            List<File> listFile = fileList.getFiles();
            deeper_in_folders(listFile);
            System.out.println("end recursion " + new Date());
            System.out.println(fileIdMap.size());
          /*  System.out.println("------show me fileidmap");
            for (int i = 0; i < fileIdMap.size(); i++) {
                System.out.println(fileIdMap.get(i).getId());
            }*/
            //System.out.println("----- start THREADS " + new Date());
            /*for (int i = 0; i < fileIdMap.size(); i++) {
                Thread.sleep(400);
                Runnable worker = new WorkerThread(fileIdMap.get(i));
                futures.add(executor.submit(worker));
            }*/
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
                    "files(id, name, webViewLink, mimeType)").execute();
            //, sharingUser(emailAddress, permissionId)
        } catch (Exception x) {
            System.out.println("get_driveservice_v3_files = " + x);
        }
        return fileList;
    }

    public static void deeper_in_folders(List<File> file) {
        for (File f : file) {
            try {
                // Исключаем
                // Проекты внедрения
                if (!(f.getId().equals("0B3jemUSF0v3dMnJKUUx1c2NlcTQ") || f.getId().equals("0B3jemUSF0v3dRTlZdGlUVE1KQUk")
                        || f.getId().equals("0B3jemUSF0v3dYTZEdkNKSmkzXzg") || f.getId().equals("0B3jemUSF0v3dSGNsNjJwc0JwMUU")
                        // Проекты разработки
                        || f.getId().equals("0B3jemUSF0v3dU1FTdVRpdWNId00") || f.getId().equals("0B3jemUSF0v3dR3VjOWU2SzFUWE0") || f.getId().equals("0B3jemUSF0v3dUDdJaU1YN2RBdmc")
                )) {

                    if (f.getMimeType().equals("application/vnd.google-apps.folder") || f.getMimeType().equals("folder")) {
                        //System.out.println(f.getName());
                        querry_deeper = "'" + f.getId() + "'  in parents and trashed=false";
                        deeper_in_folders(get_driveservice_v3_files(querry_deeper).getFiles());
                    } else {
                        //System.out.println(f.getName());
                        fileIdMap.add(new FileIdMap(f.getId(), f.getName(), f.getWebViewLink()));
                        Runnable worker = new WorkerThread(fileIdMap.get(fileIdMap.size() - 1));
                        Thread.sleep(160);
                        futures.add(executor.submit(worker));
                    }
                }
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
            Cell cell;
            String output = resultfile;
            fileout = new FileOutputStream(output);
            Sheet goodList = wb.createSheet("Good");
            Sheet badlist = wb.createSheet("Bad");
            System.out.println("create_columns1");
            create_columns(wb, goodList);
            System.out.println("create_columns2");
            create_columns(wb, badlist);
            System.out.println("write!");

            int goodRow = 1;
            int badRow = 1;
            boolean isbad;
            for (FileIdMap product : fileIdMap) {
                Row dataRow;
                if (product.getBadOwnersList().isEmpty()) {
                    dataRow = goodList.createRow(goodRow);
                    isbad = false;
                } else {
                    dataRow = badlist.createRow(badRow);
                    isbad = true;
                }
                cell = dataRow.createCell(0);
                CreationHelper createHelper = wb.getCreationHelper();
                Hyperlink link = createHelper.createHyperlink(Hyperlink.LINK_FILE);
                CellStyle hlink_style = wb.createCellStyle();
                link.setAddress(product.getWebViewLink());
                cell.setHyperlink(link);
                cell.setCellStyle(hlink_style);
                cell.setCellValue(product.getName());
                cell = dataRow.createCell(1);
                cell.setCellValue(product.getIdreal_owner());
                cell = dataRow.createCell(2);
                cell.setCellValue(product.getWebViewLink());
                cell = dataRow.createCell(3);
                cell.setCellValue(product.getIdowners());
                cell = dataRow.createCell(4);
                cell.setCellValue(product.getGoodOwnersList());
                cell = dataRow.createCell(5);
                cell.setCellValue(product.getBadOwnersList());
                cell = dataRow.createCell(6);
                cell.setCellValue(product.getIdInovus().toString());
                if (isbad) badRow++;
                else
                    goodRow++;
            }
            wb.write(fileout);
            fileout.close();
        } catch (Exception e) {
            System.out.println("write_to_file = " + e);
            System.exit(0);
        }
    }

    public static void create_columns(Workbook wb, Sheet x) {
        try {
            int row = 0;
            Cell cell;
            Row dataRow = x.createRow(row);
            cell = dataRow.createCell(0);
            cell.setCellValue("File");
            cell = dataRow.createCell(1);
            cell.setCellValue("realOwner");
            cell = dataRow.createCell(2);
            cell.setCellValue("WebViewLink");
            cell = dataRow.createCell(3);
            cell.setCellValue("owners");
            cell = dataRow.createCell(4);
            cell.setCellValue("Good owners");
            cell = dataRow.createCell(5);
            cell.setCellValue("Bad owners");
            cell = dataRow.createCell(6);
            cell.setCellValue("all from i-novus");

        } catch (Exception create_columns) {
            System.out.println("create_columns" + create_columns);
        }
    }
}