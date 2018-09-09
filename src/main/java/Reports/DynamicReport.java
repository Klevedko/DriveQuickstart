package Reports;

import api.authorize.Apiv1;
import api.authorize.Apiv3;
import com.google.api.services.appsactivity.Appsactivity;
import com.google.api.services.appsactivity.model.*;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.PermissionList;
import map.AuditMap;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONObject;
import org.quartz.*;

import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class DynamicReport implements Job {

    public static String history = "";
    public static String historyAdd = "";
    public static String historyDel = "";
    public static String historyRem = "";
    public static JSONArray geodata;
    public static String resultfiletemplate = "Dynamic_audit_result_";
    public static String resultfile = "";
    public static String owners_string = "";
    public static ArrayList<AuditMap> resultMap = new ArrayList<AuditMap>();
    public static DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss:ms");
    // Переменные для запуска CRON и логики
    public static Boolean allEmailFromINovus;
    public static boolean needReMap = false;
    public static boolean running = false;
    public static boolean firstRun = true;
    public static boolean needmail = false;

    public static String attentionString = "\n";
    public static String evlist_string = "";
    public static Drive driveservice;
    public static Appsactivity service;

    static {
        try {
            service = Apiv1.getAppsactivityService();
            driveservice = Apiv3.Drive();
        } catch (Exception e) {
        }
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
        try {
            System.out.println("---------------- DYNAMIC REPORT RUNS---------------- ");
            // папка для мониторинга
            ListActivitiesResponse result = service.activities().list()
                    .setSource("drive.google.com")
                    .setDriveAncestorId("1LR-gffElJbKDmUeZUDOZpTwjbD19odNq")//.setPageSize(11)
                    .execute();

            List<Activity> activities = result.getActivities();
            if (activities == null || activities.size() == 0) {
                System.out.println("No activity.");
            } else {
                read_activities(activities);
                // Если это не первый запуск, и есть новые строки БЕЗ @I-NOVUS!!
                if (!firstRun && needmail) {
                    System.out.println("SEND EMAIL");
                    //SendMail.main(attentionString);
                    attentionString = "";
                } else
                    System.out.println("we do not need to send email");
                System.out.println("22222222222222222222222222222222222222222222222222222222222222222222222222222222");
            }
        } catch (Exception exec) {
        }
        // Первый step Cron пройден
        firstRun = false;
        //running = false;
    }

    public static void read_activities(List<Activity> activities) {
        needReMap = false;
        System.out.println("Recent activity:");
        System.out.println("STARTsize=" + resultMap.size());
        for (Activity activity : activities) {
            // для каждой activity получаем события EVENTS
            List<Event> eventList = activity.getSingleEvents();
            for (Event e : eventList) {
                User user = e.getUser();
                Target target = e.getTarget();
                String date = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(new Date(e.getEventTimeMillis().longValue()));

                if (user == null || target == null) {
                    continue;
                }
                System.out.printf("%s: %s. FILE: %s,  ACTION: %s. GETPERMISSIONCHANGES_JSON %s\n", date, user.getName(), target.getName(), e.getPrimaryEventType(), e.getPermissionChanges());
                List<PermissionChange> evlist = e.getPermissionChanges();
                evlist_string = "";
                // если у строки activity есть список изменений JSON --->
                if (!(evlist == null)) {
                    // если json не пуст, получаем подмассивы в одну строку.
                    for (PermissionChange permissionChange : evlist) {
                        evlist_string = evlist_string + permissionChange;
                    }
                    // парсим json и получаем список кто-что-добавил в activity
                    addedDeletedRemovedPermissions(evlist_string);
                } else history = "";

                // получаем очередную строку activity, и если она НЕ В МАПЕ , добавляем, дополнив getEditors'ом
                AuditMap candy = new AuditMap(date, user.getName(), target.getName(), e.getPrimaryEventType(), history, "", false);
                // Если записи в мапе нет - добавим. перед добавлением посмотрим - проблемная ли запись.
                owners_string = read_editors(target.getId());
                if (!(resultMap.contains(candy)) && !allEmailFromINovus) {
                    System.out.println("adding bad event !!");
                    candy.setV3_owners(owners_string);
                    candy.setAllEmailFromINovus(allEmailFromINovus);
                    resultMap.add(candy);
                    needReMap = true;
                    if (needmail && !firstRun)
                        attentionString += candy.getBody() + "\n";
                    //System.out.println("added line = " + candy.getAll());

                } else
                    System.out.println("already exists!");
            }
            history = historyDel = historyAdd = historyRem = "";
        }
        Collections.sort(resultMap);
        System.out.println("ENDsize=" + resultMap.size());
        if (needReMap)
            write_to_file(resultMap);
    }

    public static void addedDeletedRemovedPermissions(String evlist_string) {
        JSONObject obj = new JSONObject(evlist_string);
        try {
            geodata = obj.getJSONArray("addedPermissions");
            historyAdd = "addedPermissions:\n" + getHistory(geodata);
        } catch (Exception e) {
        }
        try {
            geodata = obj.getJSONArray("deletedPermissions");
            historyDel = "deletedPermissions:\n" + getHistory(geodata);
        } catch (Exception e) {
        }
        try {
            geodata = obj.getJSONArray("removedPermissions");
            historyRem = "removedPermissions:\n" + getHistory(geodata);
        } catch (Exception e) {
        }
        history = historyAdd.concat(historyDel.concat(historyRem));
    }

    public static String read_editors(String fileid) {
        String s = "";
        allEmailFromINovus = true;
        try {
            PermissionList permissionList = driveservice.permissions().list(fileid).setPageSize(100).setFields("permissions(id, displayName, emailAddress, role)")
                    .execute();
            List<com.google.api.services.drive.model.Permission> p = permissionList.getPermissions();
            for (com.google.api.services.drive.model.Permission pe : p) {
                s += pe.getDisplayName() + " ( " + pe.getEmailAddress() + " ) : " + pe.getRole() + "\n";
                if (pe.getEmailAddress() != null) {
                    if (!(pe.getEmailAddress().toLowerCase().contains("@i-novus"))) {
                        allEmailFromINovus = false;
                        needmail = true;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("=====" + e.getMessage() + e.getLocalizedMessage());
        }
        return s;
    }

    public static String getHistory(JSONArray geodata) {
        String his = "";
        for (int i = 0; i < geodata.length(); ++i) {
            JSONObject person = geodata.getJSONObject(i);
            his += "   " + (person.has("name") ? person.getString("name") : person.getString("permissionId")) + ": " + person.getString("role") + "\n";
        }
        return his;
    }

    public static void write_to_file(ArrayList<AuditMap> resultMap) {
        try {
            System.out.println("writing to the file....");
            String audit_date = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
            resultfile = resultfiletemplate.concat(audit_date.concat(".xlsx"));
            XSSFWorkbook wb = new XSSFWorkbook();
            int row = 0;

            Cell cell;

            Sheet list = wb.createSheet("Go");
            String output = resultfile;

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
                cell.setCellValue(product.getDate());
                cell = dataRow.createCell(1);
                cell.setCellValue(product.getName());
                cell = dataRow.createCell(2);
                cell.setCellValue(product.getTarget_name());
                cell = dataRow.createCell(3);
                cell.setCellValue(product.getEventAction());
                cell = dataRow.createCell(4);
                cell.setCellValue(product.getHistory());
                cell = dataRow.createCell(5);
                cell.setCellValue(product.getV3_owners());
                cell = dataRow.createCell(6);
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