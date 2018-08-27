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
import com.google.api.services.appsactivity.Appsactivity;
import com.google.api.services.appsactivity.AppsactivityScopes;
import com.google.api.services.appsactivity.model.*;
import com.google.api.services.appsactivity.model.User;
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
import java.util.*;

public class DriveQuickstart {
    /**
     * Application name.
     */
    private static final String APPLICATION_NAME =
            "G Suite Activity API Java Quickstart";

    /**
     * Directory to store authorization tokens for this application.
     */
    private static final java.io.File DATA_STORE_DIR = new java.io.File("tokens");

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
    public static String his = "";
    public static JSONArray geodata;

    public static Credential authorize() throws IOException {
        // Load client secrets.
        InputStream in =
                DriveQuickstart.class.getResourceAsStream("/credentials.json");
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
                flow, new LocalServerReceiver()).authorize("user");
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
        Credential credential = authorize();
        return new Appsactivity.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public static void main(String[] args) throws IOException {
        Appsactivity service = getAppsactivityService();

        ListActivitiesResponse result = service.activities().list()
                .setSource("drive.google.com")
                .setDriveAncestorId("root")
                .setPageSize(111)
                .execute();

        List<Activity> activities = result.getActivities();
        if (activities == null || activities.size() == 0) {
            System.out.println("No activity.");
        } else {
            ArrayList<Employee> al = new ArrayList<Employee>();

            for (Activity activity : activities) {

                Event event = activity.getCombinedEvent();
                String date = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                        .format(new java.util.Date(event.getEventTimeMillis().longValue()));

                User user = event.getUser();
                Target target = event.getTarget();
                if (user == null || target == null  /*|| !(event.getPrimaryEventType().equals("permissionChange"))*/) {
                    continue;
                }
                String evlist_string = "";
                System.out.printf("%s: %s. FILE: %s,  ACTION: %s. GETPERMISSIONCHANGES_JSON %s\n",
                        date,
                        user.getName(),
                        target.getName(),
                        event.getPrimaryEventType(),
                        event.getPermissionChanges()
                );

                List<PermissionChange> evlist = event.getPermissionChanges();
                if (!(evlist == null)) {
                    for (PermissionChange permissionChange : evlist) {
                        evlist_string = evlist_string + permissionChange;
                    }
                    if (event.getPrimaryEventType().equals("permissionChange") || event.getPrimaryEventType().equals("create")) {
                        System.out.println(event.getPrimaryEventType());
                        JSONObject obj = new JSONObject(evlist_string);
                        try {
                            geodata = obj.getJSONArray("addedPermissions");
                            System.out.println("add" + geodata);
                            historyAdd = "addedPermissions:\n" + getHistory(geodata);

                        } catch (Exception e) {
                            System.out.println(e.getLocalizedMessage());
                        }

                        try {
                            geodata = obj.getJSONArray("deletedPermissions");
                            System.out.println("del" + geodata);
                            historyDel = "deletedPermissions:\n" + getHistory(geodata);
                        } catch (Exception e) {
                            System.out.println(e.getLocalizedMessage());
                        }

                        try {
                            geodata = obj.getJSONArray("removedPermissions");
                            historyRem = "removedPermissions:\n" + getHistory(geodata);
                            System.out.println("rem" + geodata);
                        } catch (Exception e) {
                            System.out.println(e.getLocalizedMessage());
                        }
                    }
                    history = historyAdd.concat(historyDel.concat(historyRem));
                    al.add(new Employee(date, user.getName(), target.getName(), event.getPrimaryEventType(), history));
                    clearAll();
                }
            }
            Collections.sort(al);
            write_to_file(al);
        }
    }

    public static String getHistory(JSONArray geodata) {
        final int n = geodata.length();
        for (int i = 0; i < n; ++i) {
            JSONObject person = geodata.getJSONObject(i);
            his = person.getString("name") + " : " + person.getString("role") + "\n";
        }
        return his;
    }

    public static void clearAll() {
        history = "";
        historyDel = "";
        historyAdd = "";
        historyRem = "";
    }

    public static void write_to_file(ArrayList<Employee> al) {
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
            System.out.println("Recent activity:");
            row++;

            for (Employee product : al) {
                dataRow = list.createRow(row);
                cell = dataRow.createCell(0);
                cell.setCellValue(product.getDate());
                cell = dataRow.createCell(1);
                cell.setCellValue(product.getName());
                cell = dataRow.createCell(2);
                cell.setCellValue(product.getTarget_name());
                cell = dataRow.createCell(3);
                cell.setCellValue(product.getGet1());
                cell = dataRow.createCell(4);
                cell.setCellValue(product.getGet2());
                row++;
            }

            wb.write(fileout);
            fileout.close();
        } catch (Exception e) {
        }

    }
}