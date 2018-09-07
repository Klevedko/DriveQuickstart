package api;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
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

    public static String history = "";
    public static String historyAdd = "";
    public static String historyDel = "";
    public static String historyRem = "";
    public static JSONArray geodata;
    public static ArrayList<AuditMap> resultMap = new ArrayList<AuditMap>();
    public final String[] arguments = new String[]{"123"};
    public static DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss:ms");
    // Переменные для запуска CRON и логики
    public static boolean running = false;
    public static Boolean allEmailFromINovus;
    public static boolean needReMap = false;
    public static boolean firstRun = true;
    public static boolean needmail = false;
    public static String attentionString = "\n";
    public static FileList fileList;

    public static FileList drive_v2(String query) {
        try {
            Drive driveservice = Apiv3.Drive();
            return driveservice.files().list().setQ(query).execute();
        } catch (Exception x) {
        }
        return fileList;
    }

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
            String FileId = "1tP-IDq3DksMYA1HPMuubADEllTxCQ04j";
            String query = "'" + FileId + "'  in parents and trashed=false";
            FileList fileList = drive_v2(query);
            List<File> activities = fileList.getFiles();
            deeper_in_folders(activities);
            write_to_file(resultMap);
        } catch (Exception exec) {
        }
        // Первый step Cron пройден
        firstRun = false;
        running = false;
    }

    public static void deeper_in_folders(List<File> file) {
        needReMap = false;
        for (File f : file) {
            try {
                String query2 = "'" + f.getId() + "'  in parents and trashed=false";
                deeper_in_folders(drive_v2(query2).getFiles());
            } catch (Exception ss) {
            }
            System.out.println(f.getName());
            AuditMap candy = new AuditMap(f.getName());
            resultMap.add(candy);
        }
    }

    public static void write_to_file(ArrayList<AuditMap> resultMap) {
        try {
            System.out.println("writing to the file....");
            XSSFWorkbook wb = new XSSFWorkbook();
            int row = 0;
            Cell cell;
            Sheet list = wb.createSheet("Go");
            String output = "audit_results.xlsx";
            FileOutputStream fileout;
            fileout = new FileOutputStream(output);
            Row dataRow = list.createRow(row);
            cell = dataRow.createCell(0);
            cell.setCellValue("Date");
            cell = dataRow.createCell(1);
            cell.setCellValue("Who");
            cell = dataRow.createCell(2);
            cell.setCellValue("File");
            cell = dataRow.createCell(3);
            cell.setCellValue("Action");
            cell = dataRow.createCell(4);
            cell.setCellValue("Activities");
            cell = dataRow.createCell(5);
            cell.setCellValue("Current rights on file");
            cell = dataRow.createCell(6);
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
            System.out.println(e.getMessage());
            System.exit(0);
        }
    }

/*
    public static void read_from_excel_when_start(ArrayList<AuditMap> resultMap) {
        try {
            Workbook workbook = WorkbookFactory.create(new File("C:/IdeaProjects/DriveQuickstart/audit_results.xlsx"));

            Sheet sheet = workbook.getSheetAt(0);

            // Create a DataFormatter to format and get each cell's value as String
            DataFormatter dataFormatter = new DataFormatter();

            // 1. You can obtain a rowIterator and columnIterator and iterate over them
            System.out.println("\n\nIterating over Rows and Columns using Iterator\n");
            Iterator<Row> rowIterator = sheet.rowIterator();
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                AuditMap news = new AuditMap();
                // Now let's iterate over the columns of the current row
                Iterator<Cell> cellIterator = row.cellIterator();

                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    String cellValue = dataFormatter.formatCellValue(cell);
                    System.out.print(cellValue + "\t");
                }
                System.out.println();
            }
        } catch (Exception e) {

            System.out.println(e.getMessage());
            System.exit(0);
        }
    }
*/
}