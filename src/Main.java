import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.Iterator;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        String excelPath = "src/TestSet_Test.xlsx"; // uprav cestu podle potřeby
        String jiraBaseUrl = "https://tvuj-team.atlassian.net"; // tvůj JIRA base URL
        String authEmail = "tvuj@email.com";
        String apiToken = "tvuj-api-token";
        String parentIssueKey = "TEST-45"; // nahraď skutečným klíčem

        try (FileInputStream fis = new FileInputStream(new File(excelPath));
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> iterator = sheet.iterator();
            iterator.next(); // přeskoč hlavičku

            while (iterator.hasNext()) {
                Row row = iterator.next();
                String summary = row.getCell(0).getStringCellValue();
                String description = row.getCell(1).getStringCellValue();

                try {
                    createSubtask(jiraBaseUrl, authEmail, apiToken, summary, description, parentIssueKey);
                } catch (Exception e) {
                    System.out.println("Chyba při vytváření subtasku: " + summary);
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void createSubtask(String jiraBaseUrl, String email, String apiToken,
                                     String summary, String description, String parentIssueKey) throws Exception {

        String jiraUrl = jiraBaseUrl + "/rest/api/3/issue";

        String json = "{\n" +
                "  \"fields\": {\n" +
                "    \"project\": {\n" +
                "      \"key\": \"" + parentIssueKey.split("-")[0] + "\"\n" +
                "    },\n" +
                "    \"parent\": {\"key\": \"" + parentIssueKey + "\"},\n" +
                "    \"summary\": \"" + summary + "\",\n" +
                "    \"description\": \"" + description + "\",\n" +
                "    \"issuetype\": {\"name\": \"Sub-task\"}\n" +
                "  }\n" +
                "}";

        URL url = new URL(jiraUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");

        String auth = email + ":" + apiToken;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        conn.setRequestProperty("Authorization", "Basic " + encodedAuth);

        conn.getOutputStream().write(json.getBytes());

        int responseCode = conn.getResponseCode();
        if (responseCode >= 200 && responseCode < 300) {
            System.out.println("✅ Subtask vytvořen: " + summary);
        } else {
            System.out.println("❌ Chyba: HTTP " + responseCode);
        }

        conn.disconnect();
    }
    }
