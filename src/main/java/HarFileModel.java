import de.sstoehr.harreader.HarReader;
import de.sstoehr.harreader.model.Har;
import de.sstoehr.harreader.model.HarEntry;

import java.io.File;
import java.lang.reflect.Array;
import java.util.*;


public class HarFileModel {
    private CsvWriter csvWriter = new CsvWriter();
    private JsonWriter jsonWriter = new JsonWriter();

    // with LinkedHashMap we preserve insertion order in Hashmap
    private LinkedHashMap<String, Float> timeMap = new LinkedHashMap<String, Float>();
    private LinkedHashMap<String, Float> timeMapSecondRun = new LinkedHashMap<String, Float>();

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


    public void calculateResourcesTimes() {
        List<Float> numArray = new ArrayList<>();
        LinkedHashMap<String, Float> resourcesTtimes = new LinkedHashMap<String, Float>(timeMap);
        LinkedHashMap<String, Float> secondResourcesTtimes = new LinkedHashMap<String, Float>(timeMapSecondRun);
        ArrayList<ResourcesTimeModel> diffRsrcTimes = new ArrayList<>();
        List<ResourceNodeModel> nodeList = new ArrayList<ResourceNodeModel>();
        for (Map.Entry hashMap : resourcesTtimes.entrySet()) {
            // gravar apenas os nodes para o grafo (firstResources)
            ResourceNodeModel resourceNode = new ResourceNodeModel();
            resourceNode.firstRsrc = hashMap.getKey().toString();
            nodeList.add(resourceNode);
            for(Map.Entry secondHashMap : secondResourcesTtimes.entrySet()){
                if(hashMap.getKey().toString().equals(secondHashMap.getKey().toString())){
                    //----------------------------------
                    timeMap.remove(hashMap.getKey());
                    for (Map.Entry<String, Float> pair : timeMap.entrySet()) {
                        ResourcesTimeModel resourcesTimeModel = new ResourcesTimeModel();
                        resourcesTimeModel.firstRsrc = hashMap.getKey().toString();
                        System.out.println(pair.getKey() + " = " + pair.getValue());
                        resourcesTimeModel.secondRsrc = pair.getKey().toString();
                        resourcesTimeModel.diffResourceTime = Math.abs((float) hashMap.getValue() - (float) pair.getValue());
                        for(Map.Entry<String, Float> secondPair : timeMapSecondRun.entrySet()){
                            if(secondPair.getKey().equals(pair.getKey())){
                                resourcesTimeModel.diffResourceTime2 = Math.abs((float) secondHashMap.getValue() - (float) secondPair.getValue());
                                numArray.add(Math.abs(resourcesTimeModel.diffResourceTime));
                                numArray.add(Math.abs(resourcesTimeModel.diffResourceTime2));
                                Collections.sort(numArray);
                                if (numArray.size() % 2 == 0)
                                    resourcesTimeModel.median = ((double)numArray.get(numArray.size()/2) + (double)numArray.get(numArray.size()/2 - 1))/2;
                                else
                                    resourcesTimeModel.median = (double) numArray.get(numArray.size()/2);
                                resourcesTimeModel.percentil = percentile(numArray,25);
                                numArray.clear();
                            }
                        }
                        diffRsrcTimes.add(resourcesTimeModel);
                    }
                }
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

    private static Float percentile(List<Float> latencies, double percentile) {
        Collections.sort(latencies);
        int index = (int) Math.ceil(percentile / 100.0 * latencies.size());
        return latencies.get(index-1);
    }

    public void FillResourcesMap() {
        int[] count = new int[]{0};
        int[] secondCount = new int[]{0};
        ArrayList<String> checkDuplicates = new ArrayList<String>();
        try {
            HarReader harReader = new HarReader();
            Har har = harReader.readFromFile(new File("D:/Programas/XAMPP/htdocs/webbench/src/main/java/files/twitter.com.user1.har"));
            Har secondHar = harReader.readFromFile(new File("D:/Programas/XAMPP/htdocs/webbench/src/main/java/files/twitter.com.user1_secondRUN.har"));

            har.getLog().getEntries().forEach(entry -> {
                secondHar.getLog().getEntries().forEach(secondEntry -> {
                    if(entry.getRequest().getUrl().equals(secondEntry.getRequest().getUrl()) && !checkDuplicates.contains(secondEntry.getRequest().getUrl())){
                        checkDuplicates.add(entry.getRequest().getUrl());
                        // to add different keys, because there are resources with the same url
                        timeMap.put(count[0] + "-" + entry.getRequest().getUrl(), (float) entry.getTime());
                        timeMapSecondRun.put(count[0] + "-" + secondEntry.getRequest().getUrl(), (float) secondEntry.getTime());
                        count[0]++;
                    }
                });
            });


            // calculate time difference between resource 1 and resource 2
           //-- calculateDiffResourcesTimes();
            calculateResourcesTimes();
        } catch (Exception ex) {
            // e.printStackTrace();
            System.out.println(ex.getMessage());
        }
    }

}
