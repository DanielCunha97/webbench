import harreader.HarReader;
import harreader.model.Har;

import java.io.File;
import java.lang.reflect.Array;
import java.util.*;


public class HarFileModel {
    private CsvWriter csvWriter = new CsvWriter();
    private JsonWriter jsonWriter = new JsonWriter();

    // with LinkedHashMap we preserve insertion order in Hashmap
    private LinkedHashMap<String, ResourceInfo> timeMap = new LinkedHashMap<String, ResourceInfo>();
    private LinkedHashMap<String, ResourceInfo> timeMapSecondRun = new LinkedHashMap<String, ResourceInfo>();

    public HarFileModel() {
        FillResourcesMap();
    }

    public LinkedHashMap<String, ResourceInfo> GetTime() {
        return timeMap;
    }

 /*   public void calculateDiffResourcesTimes() {
        LinkedHashMap<String, LinkedHashMap<Float, String>> resourcesTtimes = new LinkedHashMap<String, LinkedHashMap<Float, String>>(timeMap);
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
    }*/

    public void calculateResourcesTimes() {
        List<Float> numArray = new ArrayList<>();
        LinkedHashMap<String, ResourceInfo> resourcesTtimes = new LinkedHashMap<String, ResourceInfo>(timeMap);
        LinkedHashMap<String, ResourceInfo> secondResourcesTtimes = new LinkedHashMap<String, ResourceInfo>(timeMapSecondRun);
        ArrayList<ResourcesTimeModel> diffRsrcTimes = new ArrayList<>();
        List<ResourceNodeModel> nodeList = new ArrayList<ResourceNodeModel>();
        for (Map.Entry<String, ResourceInfo> hashMap : resourcesTtimes.entrySet()) {
            // gravar apenas os nodes para o grafo (firstResources)
            ResourceNodeModel resourceNode = new ResourceNodeModel();
            resourceNode.firstRsrc = hashMap.getKey().toString();
            resourceNode.type = hashMap.getValue().resourceType;
            nodeList.add(resourceNode);
            for(Map.Entry<String, ResourceInfo> secondHashMap : secondResourcesTtimes.entrySet()){
                if(hashMap.getKey().toString().equals(secondHashMap.getKey().toString())){
                    //----------------------------------
                    timeMap.remove(hashMap.getKey());
                    for (Map.Entry<String, ResourceInfo> pair : timeMap.entrySet()) {
                        ResourcesTimeModel resourcesTimeModel = new ResourcesTimeModel();
                        resourcesTimeModel.firstRsrc = hashMap.getKey().toString();
                        resourcesTimeModel.secondRsrc = pair.getKey().toString();
                        resourcesTimeModel.diffResourceTime = Math.abs((float) hashMap.getValue().resourceTime - (float) pair.getValue().resourceTime);
                        for(Map.Entry<String, ResourceInfo> secondPair : timeMapSecondRun.entrySet()){
                            if(secondPair.getKey().equals(pair.getKey())){
                                resourcesTimeModel.resourceType = secondPair.getValue().resourceType;
                                resourcesTimeModel.diffResourceTime2 = Math.abs(secondHashMap.getValue().resourceTime - secondPair.getValue().resourceTime);
                                numArray.add(Math.abs(resourcesTimeModel.diffResourceTime));
                                numArray.add(Math.abs(resourcesTimeModel.diffResourceTime2));
                                Collections.sort(numArray);
                                if (numArray.size() % 2 == 0)
                                    resourcesTimeModel.median = ((double)numArray.get(numArray.size()/2) + (double)numArray.get(numArray.size()/2 - 1))/2;
                                else
                                    resourcesTimeModel.median = (double) numArray.get(numArray.size()/2);
                                resourcesTimeModel.percentil_cinco = percentile(numArray,5);
                                resourcesTimeModel.percentil_noventaCinco = percentile(numArray,95);
                                numArray.clear();
                            }
                        }
                        diffRsrcTimes.add(resourcesTimeModel);
                    }
                }
            }
        }

        csvWriter.SaveDiffResourcesTimes(diffRsrcTimes);
        Set<ResourceNodeModel> nodesWithoutDuplicates = new LinkedHashSet<ResourceNodeModel>(nodeList);
        nodeList.clear();
        nodeList.addAll(nodesWithoutDuplicates);

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
                    String x = entry.get_priority();
                    String y = entry.get_resourceType();
                    if(entry.getRequest().getUrl().equals(secondEntry.getRequest().getUrl()) && !checkDuplicates.contains(secondEntry.getRequest().getUrl())){
                        if(entry.get_priority().equals("VeryHigh") || entry.get_priority().equals("High")){
                            ResourceInfo resourceInfo = new ResourceInfo();
                            resourceInfo.resourceTime = (float) entry.getTime();
                            resourceInfo.resourceType = entry.get_resourceType();
                            checkDuplicates.add(entry.getRequest().getUrl());
                            // to add different keys, because there are resources with the same url
                            timeMap.put(count[0] + "-" + entry.getRequest().getUrl(), resourceInfo);
                            ResourceInfo secondResourceInfo = new ResourceInfo();
                            secondResourceInfo.resourceTime = (float) secondEntry.getTime();
                            secondResourceInfo.resourceType = secondEntry.get_resourceType();
                            timeMapSecondRun.put(count[0] + "-" + secondEntry.getRequest().getUrl(), secondResourceInfo);
                            count[0]++;
                        }
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
