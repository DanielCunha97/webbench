import de.sstoehr.harreader.HarReader;
import de.sstoehr.harreader.model.Har;
import de.sstoehr.harreader.model.HarEntry;

import java.io.File;
import java.util.*;


public class HarFileModel {
    private CsvWriter csvWriter = new CsvWriter();
    private JsonWriter jsonWriter = new JsonWriter();

    // with LinkedHashMap we preserve insertion order in Hashmap
    private LinkedHashMap<String, Float> timeMap = new LinkedHashMap<String, Float>();

    public HarFileModel() {
        FillResourcesMap();
    }

    public LinkedHashMap<String, Float> GetTime() {
        return timeMap;
    }

    public void calculateDiffResourcesTimes() {
        LinkedHashMap<String, Float> resourcesTtimes = new LinkedHashMap<String, Float>(timeMap);
        ArrayList<ResourcesTimeModel> diffRsrcTimes = new ArrayList<>();
        List<ResourceNodeModel> nodeList = new ArrayList<ResourceNodeModel>();
        for (Map.Entry hashMap : resourcesTtimes.entrySet()) {
            // gravar apenas os nodes para o grafo (firstResources)
            ResourceNodeModel resourceNode = new ResourceNodeModel();
            resourceNode.firstRsrc = hashMap.getKey().toString();
            nodeList.add(resourceNode);
            //----------------------------------
            timeMap.remove(hashMap.getKey());
            for (Map.Entry pair : timeMap.entrySet()) {
                    ResourcesTimeModel resourcesTimeModel = new ResourcesTimeModel();
                    // first resource
                    resourcesTimeModel.firstRsrc = hashMap.getKey().toString();
                    System.out.println(pair.getKey() + " = " + pair.getValue());
                    resourcesTimeModel.secondRsrc = pair.getKey().toString();
                    resourcesTimeModel.diffResourceTime = Math.abs((float) hashMap.getValue() - (float) pair.getValue());
                    diffRsrcTimes.add(resourcesTimeModel);
                }
        }
        csvWriter.SaveDiffResourcesTimes(diffRsrcTimes);
        System.out.println("list of nodes : " + nodeList);
        Set<ResourceNodeModel> nodesWithoutDuplicates = new LinkedHashSet<ResourceNodeModel>(nodeList);
        nodeList.clear();
        nodeList.addAll(nodesWithoutDuplicates);

        System.out.println("list of nodes without duplicates : " + nodeList);
        jsonWriter.SaveDiffResourcesTimes(nodeList, diffRsrcTimes);
    }

    public void FillResourcesMap() {
        int[] count = new int[]{0};
        try {
            HarReader harReader = new HarReader();
            Har har = harReader.readFromFile(new File("D:/Programas/XAMPP/htdocs/webbench/src/main/java/files/KatalonHarFile.har"));
            har.getLog().getEntries().forEach(entry -> {
                // to add different keys, because exists resources with the same url
                timeMap.put(count[0] + "-" + entry.getRequest().getUrl(), (float) entry.getTime());
                count[0]++;
            });
            // calculate time difference between resource 1 and resource 2
            calculateDiffResourcesTimes();
        } catch (Exception ex) {
            // e.printStackTrace();
            System.out.println(ex.getMessage());
        }
    }

}
