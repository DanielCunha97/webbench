import de.sstoehr.harreader.HarReader;
import de.sstoehr.harreader.model.Har;
import de.sstoehr.harreader.model.HarEntry;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;


public class HarFileModel {
    private CsvWriter csvWriter = new CsvWriter();

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
        for (Map.Entry hashMap : resourcesTtimes.entrySet()) {
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
    }

    public void FillResourcesMap() {
        int[] count = new int[]{0};
        try {
            HarReader harReader = new HarReader();
            Har har = harReader.readFromFile(new File("C:/Users/Daniel Cunha/Desktop/webbench/src/KatalonHarFile.har"));
            har.getLog().getEntries().forEach(entry -> {
                count[0]++;
                timeMap.put(count[0] + "-" + entry.getResponse().getContent().getMimeType(), (float) entry.getTime());
            });
            // calculate time difference between resource 1 and resource 2
            calculateDiffResourcesTimes();
        } catch (Exception ex) {
            // e.printStackTrace();
            System.out.println(ex.getMessage());
        }
    }

}
