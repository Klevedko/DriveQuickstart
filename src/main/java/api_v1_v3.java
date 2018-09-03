import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.admin.reports.Reports;
import com.google.api.services.appsactivity.Appsactivity;
import com.google.api.services.appsactivity.AppsactivityScopes;
import com.google.api.services.appsactivity.model.*;
import com.google.api.services.appsactivity.model.User;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.PermissionList;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.*;

public class api_v1_v3 {
    /**
     * Application name.
     */
    private static final String APPLICATION_NAME =
            "G Suite Activity API Java Quickstart";

    /**
     * Directory to store authorization tokens for this application.
     */
    private static final java.io.File DATA_STORE_DIR = new java.io.File("token_v1");

    /**
     * Global instance of the {@link FileDataStoreFactory}.
     */
    private static FileDataStoreFactory DATA_STORE_FACTORY;

    /**
     * Global instance of the JSON factory.
     */
    private static final JsonFactory JSON_FACTORY =
            JacksonFactory.getDefaultInstance();

    /**
     * Global instance of the HTTP transport.
     */
    private static HttpTransport HTTP_TRANSPORT;

    /**
     * Global instance of the scopes required by this quickstart.
     * <p>
     * If modifying these scopes, delete your previously saved credentials
     * at ~/.credentials/appsactivity-java-quickstart
     */
    private static final List<String> SCOPES = Arrays.asList(AppsactivityScopes.ACTIVITY);

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Creates an authorized Credential object.
     *
     * @return an authorized Credential object.
     * @throws IOException
     */
    public static String history = "";
    public static String historyAdd = "";
    public static String historyDel = "";
    public static String historyRem = "";
    public static JSONArray geodata;
    public static ArrayList<audit_map> al = new ArrayList<audit_map>();

    public static Credential authorize() throws IOException {
        // Load client secrets.
        InputStream in =
                api_v1_v3.class.getResourceAsStream("/credentials.json");
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow =
                new GoogleAuthorizationCodeFlow.Builder(
                        HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                        .setDataStoreFactory(DATA_STORE_FACTORY)
                        .setAccessType("offline")
                        .build();
        Credential credential = new AuthorizationCodeInstalledApp(
                flow, new LocalServerReceiver()).authorize("akrasilnikov@i-novus.ru");
        System.out.println(
                "Credentials saved to " + DATA_STORE_DIR.getAbsolutePath());
        return credential;
    }

    /**
     * Build and return an authorized Apps Activity client service.
     *
     * @return an authorized Appsactivity client service
     * @throws IOException
     */
    public static Appsactivity getAppsactivityService() throws IOException {
        Credential active_credential = authorize();
        return new Appsactivity.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, active_credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public static void main(String[] args) throws IOException, GeneralSecurityException {
        Appsactivity service = getAppsactivityService();
// папка для мониторинга
        ListActivitiesResponse result = service.activities().list()
                .setSource("drive.google.com")
                .setDriveAncestorId("root")
                //.setPageSize(111)
                .execute();
        List<Activity> activities = result.getActivities();
        if (activities == null || activities.size() == 0) {
            System.out.println("No activity.");
        } else
            read_activities(activities);
    }


    public static void read_activities(List<Activity> activities) {
        {
            System.out.println("Recent activity:");

            for (Activity activity : activities) {
                Event event = activity.getCombinedEvent();
                User user = event.getUser();
                Target target = event.getTarget();
                String date = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(new java.util.Date(event.getEventTimeMillis().longValue()));

                if (user == null || target == null) {
                    continue;
                }
                String evlist_string = "";
                System.out.printf("%s: %s. FILE: %s,  ACTION: %s. GETPERMISSIONCHANGES_JSON %s\n",
                        date, user.getName(), target.getName(), event.getPrimaryEventType(), event.getPermissionChanges());

                List<PermissionChange> evlist = event.getPermissionChanges();
                if (!(evlist == null)) {
                    for (PermissionChange permissionChange : evlist) {
                        evlist_string = evlist_string + permissionChange;
                    }
                   /* if (event.getPrimaryEventType().equals("permissionChange")
                            || event.getPrimaryEventType().equals("create")
                            || event.getPrimaryEventType().equals("upload")
                            || event.getPrimaryEventType().equals("move")
                            )
                    {*/
                        //if(event.getPrimaryEventType().equals("upload")){
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
                   // }
                    history = historyAdd.concat(historyDel.concat(historyRem));
                    System.out.println(history);
                    String history_str = read_editors(target.getId());

                    al.add(new audit_map(date, user.getName(), target.getName(), event.getPrimaryEventType(), history, read_editors(target.getId())));
                    clearAll();
                }
            }
            Collections.sort(al);
            write_to_file(al);
        }
    }

    public static String read_editors(String fileid) {
        String s = "";
        try {
            // используем 3 версию rest api чтобы получить пользаков и их роли ( к файлу )
            Drive driveservice = api_v3.Drive();
            PermissionList permissionList = driveservice.permissions().list(fileid).setPageSize(100).setFields("permissions(id, displayName, emailAddress, role)")
                    .execute();
            List<com.google.api.services.drive.model.Permission> p = permissionList.getPermissions();
            for (com.google.api.services.drive.model.Permission pe : p) {
                s+= pe.getDisplayName() + " ( " + pe.getEmailAddress() + " ) : " + pe.getRole() + "\n";
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

    public static void clearAll() {
        history = "";
        historyDel = "";
        historyAdd = "";
        historyRem = "";
    }

    public static void write_to_file(ArrayList<audit_map> al) {
        try {
            HSSFWorkbook wb = new HSSFWorkbook();
            Cell cell;
            Sheet list = wb.createSheet("SheetName");
            byte row = 0;
            String output = "audit_results.xls";

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
            row++;
            for (audit_map product : al) {
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
                cell.setCellValue(product.getV1_getEditors());
                row++;
            }
            wb.write(fileout);
            fileout.close();
        } catch (Exception e) {
        }
    }
}