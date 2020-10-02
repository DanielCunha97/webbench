import java.io.File;
import java.io.FileWriter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.List;

public class JsonWriter {

    public JsonWriter(){
    }

    public void SaveDiffResourcesTimes (List<ResourceNodeModel> nodeList, ArrayList<ResourcesTimeModel> resourcesTimeModels) {
        try {
            FileWriter writer = new FileWriter(new File("D:/Programas/XAMPP/htdocs/webbench/src/main/javascript/files/ResourcesTimes.json"));
            JSONArray node = new JSONArray();
            JSONArray link = new JSONArray();
            for (ResourceNodeModel nodeModel:nodeList) {
                JSONObject arrayObj = new JSONObject();
                arrayObj.put("name",nodeModel.firstRsrc);
                arrayObj.put("id",Integer.parseInt(nodeModel.firstRsrc.split("-")[0]));
                arrayObj.put("group", 1);
                node.add(arrayObj);
            }
            for (ResourcesTimeModel resourceModel:resourcesTimeModels) {
                JSONObject arrayObj = new JSONObject();
                if(resourceModel.diffResourceTime > 0 && resourceModel.diffResourceTime2 > 0){
                    arrayObj.put("source",Integer.parseInt(resourceModel.firstRsrc.split("-")[0]));
                    arrayObj.put("target",Integer.parseInt(resourceModel.secondRsrc.split("-")[0]));
                    String dt = "2020-01-01";  // Start date
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    Calendar c = Calendar.getInstance();
                    c.setTime(sdf.parse(dt));
                    c.add(Calendar.DATE, 10);  // number of days to add
                    dt = sdf.format(c.getTime());  // dt is now the new date
                    arrayObj.put("event_date",dt);
                    arrayObj.put("value",resourceModel.diffResourceTime);
                    arrayObj.put("value2",resourceModel.diffResourceTime2);
                    arrayObj.put("median",resourceModel.median);
                    arrayObj.put("percentile",resourceModel.percentil);
                    link.add(arrayObj);
                }
            }

            JSONObject obj = new JSONObject();
            obj.put("nodes", node);
            obj.put("links",link);

            writer.write(obj.toJSONString());
            writer.close();
        } catch (Exception e) {
            // e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }

}
