import harreader.model.HarEntry;
import harreader.model.HarLog;
import harreader.model.HarRequest;
import harreader.model.HarResponse;
import harwriter.HarFileWriter;
import org.codehaus.jackson.JsonParseException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
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

    public static void getPerfEntryLogs(WebDriver driver, String fileName) throws IOException {
        List<LogEntry> logEntries = driver.manage().logs().get(LogType.PERFORMANCE).getAll();
        int[] count = new int[]{0};
        HarLog log = new HarLog();
        HarFileWriter harWriter = new HarFileWriter();
        List<HarEntry> entries = new ArrayList<HarEntry>();
        String currentURL = driver.getCurrentUrl();
        try
        {
            for (LogEntry le : logEntries) {
                JSONObject json = new JSONObject(le.getMessage());
                JSONObject message = json.getJSONObject("message");
                String method = message.getString("method");
                JSONObject params = message.getJSONObject("params");
                if (params != null)
                {
                    if(method.equals("Network.requestWillBeSent")) {
                        JSONObject request = params.getJSONObject("request");
                        if (request != null) {
                            String docUrl = params.getString("documentURL");
                            if (currentURL.equals(docUrl)) {
                               // System.out.println(request.toString());
                               // System.out.println("---------- headers: "+ request.get("headers"));
                               // System.out.println("---------- priority: "+ request.get("initialPriority"));
                                if(request.get("initialPriority") != null) {
                                    if (request.get("initialPriority").equals("VeryHigh")
                                    || request.get("initialPriority").equals("High")) {
                                        HarEntry entry = new HarEntry();
                                        HarRequest harRequest = new HarRequest();
                                        entry.set_priority(request.get("initialPriority").toString());
                                        harRequest.setUrl(request.get("url").toString());
                                        entry.setRequest(harRequest);
                                        entry.set_resourceType(params.get("type").toString());
                                        for(LogEntry logEntriesResp : logEntries){
                                            JSONObject jsonRes = new JSONObject(logEntriesResp.getMessage());
                                            JSONObject messageRes = jsonRes.getJSONObject("message");
                                            String methodRes = messageRes.getString("method");
                                            JSONObject paramsRes = messageRes.getJSONObject("params");
                                            if(methodRes.equals("Network.responseReceived" )){
                                                JSONObject response = paramsRes.getJSONObject("response");
                                                if(response.getString("url").equals(request.get("url"))){
                                                    //System.out.println(request.toString());
                                                    JSONObject times = response.getJSONObject("timing");
                                                    Double dnsTime = times.getDouble("dnsEnd") - times.getDouble("dnsStart");
                                                    Double sslTime = times.getDouble("sslEnd") - times.getDouble("sslStart");
                                                    Double sendTime = times.getDouble("sendEnd") - times.getDouble("sendStart");
                                                    Double connectTime = times.getDouble("connectEnd") - times.getDouble("connectStart");
                                                    Double receiveHeadersEndTime = times.getDouble("receiveHeadersEnd");
                                                    entry.setTime((int)Math.round(dnsTime + sslTime + sendTime + connectTime + receiveHeadersEndTime));
                                                    entries.add(entry);
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            log.setEntries(entries);
            File file = new File("D:/Programas/XAMPP/htdocs/webbench/src/main/java/files/" + fileName + ".har");
            while (file.exists()){
                count[0]++;
                file = new File("D:/Programas/XAMPP/htdocs/webbench/src/main/java/files/" + fileName + "_" + count[0] +".har");
            }
            file.getParentFile().mkdirs();
            // FileWriter harFile = new FileWriter(file);
            // harFile.write();
            harWriter.writeHarFile(log, file);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        catch (JsonParseException e)
        {
            e.printStackTrace();
        }
    }
}
