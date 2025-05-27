import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.Iterator;
import java.io.OutputStream;

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

        String jiraUrl = jiraBaseUrl + "/rest/api/3/issue/";

        String json = "{\n" +
                "  \"fields\": {\n" +
                "    \"project\": {\n" +
                "      \"key\": \"" + parentIssueKey.split("-")[0] + "\"\n" +
                "    },\n" +
                "    \"parent\": {\n" +
                "      \"key\": \"" + parentIssueKey + "\"\n" +
                "    },\n" +
                "    \"summary\": \"" + summary + "\",\n" +
                "    \"description\": \"" + description + "\",\n" +
                "    \"issuetype\": {\n" +
                "      \"name\": \"Sub-task\"\n" +
                "    }\n" +
                "  }\n" +
                "}";

        URL url = new URL(jiraUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");

        String auth = email + ":" + apiToken;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        connection.setRequestProperty("Authorization", "Basic " + encodedAuth);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = json.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        int responseCode = connection.getResponseCode();
        System.out.println("HTTP Response Code: " + responseCode);
    }
}
