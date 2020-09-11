import java.io.File;
import java.io.FileWriter;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
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
                node.add(nodeModel.firstRsrc);
            }

            for (ResourcesTimeModel resourceModel:resourcesTimeModels) {
                JSONObject arrayObj = new JSONObject();
                arrayObj.put("firstResource",resourceModel.firstRsrc);
                arrayObj.put("secondResource",resourceModel.secondRsrc);
                arrayObj.put("time",resourceModel.diffResourceTime);
                link.add(arrayObj);
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
