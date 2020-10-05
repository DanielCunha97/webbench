import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class Util {
    public static void analyzeLog(WebDriver driver) {

        LogEntries logEntries = driver.manage().logs().get(LogType.BROWSER);

        for (LogEntry entry : logEntries) {
            if(entry.getMessage().contains("[LOGREQUEST]"))
                System.out.println(new Date(entry.getTimestamp()) + " " + entry.getLevel() + " " + entry.getMessage());
        }
    }

    // This method compares two strings
    // lexicographically without using
    // library functions
    public static Boolean stringCompare(String str1, String str2)
    {
        int l1 = str1.length();
        int l2 = str2.length();
        int lmin = Math.min(l1, l2);

        for (int i = 0; i < lmin; i++) {
            int str1_ch = (int)str1.charAt(i);
            int str2_ch = (int)str2.charAt(i);

            if (str1_ch != str2_ch) {
                return false;
            }
        }

        // Edge case for strings like
        // String 1="Geeks" and String 2="Geeksforgeeks"
        if (l1 != l2) {
            return false;
        }

        // If none of the above conditions is true,
        // it implies both the strings are equal
        else {
            return true;
        }
    }

    private static JSONArray getPerfEntryLogs(WebDriver driver) {
        LogEntries logEntries = driver.manage().logs().get(LogType.PERFORMANCE);
        JSONArray perfJsonArray = new JSONArray();
        logEntries.forEach(entry -> {
                JSONObject messageJSON = new JSONObject(entry.getMessage()).getJSONObject("message");
            // System.out.println("Entry JSON: " + messageJSON);
            // System.out.println("Entry: " + messageJSON.getJSONObject("log").getJSONObject("entries").getJSONObject("request").get("url"));
                perfJsonArray.put(messageJSON);
        });
        return perfJsonArray;
    }

    public static void getHAR(WebDriver driver, String fileName) throws IOException {
        String destinationFile = "D:/Programas/XAMPP/htdocs/webbench/src/main/java/files/" + fileName + ".har";
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
        ((JavascriptExecutor) driver).executeScript(
                "!function(e,o){e.src=\"D:/Programas/XAMPP/htdocs/webbench/src/main/javascript/assets/chromePerfLogsHAR.js\"," +
                        "e.onload=function(){jQuery.noConflict(),console.log(\"jQuery injected\")},document.head.appendChild(e)}(document.createElement(\"script\"));");
        File file = new File(destinationFile);
        if(file.exists()){
            file = new File("D:/Programas/XAMPP/htdocs/webbench/src/main/java/files/WebSiteHarFileSecondRun.har");
        }
        file.getParentFile().mkdirs();
        FileWriter harFile = new FileWriter(file);
        harFile.write((String) ((JavascriptExecutor) driver).executeAsyncScript(
                "return module.getHarFromMessages(arguments[0])", getPerfEntryLogs(driver).toString()));
        harFile.close();
    }
}
