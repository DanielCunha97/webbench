import edu.umass.cs.benchlab.har.tools.HarFileWriter;
import harreader.HarReader;
import harreader.model.Har;

import java.io.File;
import java.lang.reflect.Array;
import java.util.*;


public class HarFileModel {
    private CsvWriter csvWriter = new CsvWriter();
    private JsonWriter jsonWriter = new JsonWriter();
    private String filename;
    // with LinkedHashMap we preserve insertion order in Hashmap
    private LinkedHashMap<String, ResourceInfo> timeMap = new LinkedHashMap<String, ResourceInfo>();
    private LinkedHashMap<String, ResourceInfo> timeMapSecondRun = new LinkedHashMap<String, ResourceInfo>();
    private LinkedHashMap<String, ArrayList<ResourceInfo>> timeHarMap = new LinkedHashMap<String, ArrayList<ResourceInfo>>();
    private  ArrayList<String> combinationsArray = new ArrayList<>();

    public HarFileModel(String fileName) {
        this.filename = fileName;
        FillResourcesMap(fileName);
    }

    public LinkedHashMap<String, ResourceInfo> GetTime() {
        return timeMap;
    }

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

        csvWriter.SaveDiffResourcesTimes(diffRsrcTimes, filename);
        Set<ResourceNodeModel> nodesWithoutDuplicates = new LinkedHashSet<ResourceNodeModel>(nodeList);
        nodeList.clear();
        nodeList.addAll(nodesWithoutDuplicates);

        jsonWriter.SaveDiffResourcesTimes(nodeList, diffRsrcTimes, filename);
    }

    private static Float percentile(List<Float> latencies, double percentile) {
        Collections.sort(latencies);
        int index = (int) Math.ceil(percentile / 100.0 * latencies.size());
        return latencies.get(index-1);
    }

    private void calculateResourcesFromTests(LinkedHashMap<String, ArrayList<ResourceInfo>> resourcesMap, int fileCount){
        List<ResourceNodeModel> nodeList = new ArrayList<ResourceNodeModel>();
        LinkedHashMap<String, ArrayList<ResourceInfo>> resourcesTtimes = new LinkedHashMap<String, ArrayList<ResourceInfo>>(resourcesMap);
        ArrayList<ResourcesTimeModel> diffRsrcTimes = new ArrayList<>();
        for (Map.Entry<String, ArrayList<ResourceInfo>> hashMap : resourcesMap.entrySet()) {
            // gravar apenas os nodes para o grafo (firstResources)
            ResourceNodeModel resourceNode = new ResourceNodeModel();
            resourceNode.firstRsrc = hashMap.getKey().toString();
            resourceNode.type = hashMap.getValue().iterator().next().resourceType;
            resourceNode.probability = hashMap.getValue().size() >= fileCount ? 100 : ((float)hashMap.getValue().size()/fileCount)*100;
            resourceNode.cachedResource = hashMap.getValue().iterator().next().cachedResource.contains("no-cache") ? "false" : "true";
            nodeList.add(resourceNode);
            //---------------------------------------------------
            //looking for next pair on hashmap
            resourcesTtimes.remove(hashMap.getKey());
            for (Map.Entry<String, ArrayList<ResourceInfo>> pair : resourcesTtimes.entrySet()) { //Next Resource of hashMap
                ResourcesTimeModel resourcesTimeModel = new ResourcesTimeModel();
                resourcesTimeModel.firstRsrc = hashMap.getKey().toString();
                resourcesTimeModel.secondRsrc = pair.getKey().toString();
                resourcesTimeModel.resourceType = pair.getValue().iterator().next().resourceType;
                //iterating over Resource values only
                Iterator<ResourceInfo> values = hashMap.getValue().iterator();
                //iterating over next Resource values only
                Iterator<ResourceInfo> nextValues = pair.getValue().iterator();
                //add the first ones, before iteration
                resourcesTimeModel.diffResourceTimes.add(Math.abs(values.next().resourceTime - nextValues.next().resourceTime));
                while(values.hasNext() && nextValues.hasNext()){
                    ResourceInfo infoValue = values.next();
                    ResourceInfo infoNextValue = nextValues.next();
                    if(infoNextValue.repeatedCall == false || infoValue.repeatedCall == false){
                        resourcesTimeModel.diffResourceTimes.add(infoValue.resourceTime - infoNextValue.resourceTime);
                    }
                }
                Collections.sort(resourcesTimeModel.diffResourceTimes);
                if (resourcesTimeModel.diffResourceTimes.size() % 2 == 0)
                    resourcesTimeModel.median = ((double)resourcesTimeModel.diffResourceTimes.get(resourcesTimeModel.diffResourceTimes.size()/2) + (double)resourcesTimeModel.diffResourceTimes.get(resourcesTimeModel.diffResourceTimes.size()/2 - 1))/2;
                else
                    resourcesTimeModel.median = (double) resourcesTimeModel.diffResourceTimes.get(resourcesTimeModel.diffResourceTimes.size()/2);
                resourcesTimeModel.percentil_cinco = percentile(resourcesTimeModel.diffResourceTimes,5);
                resourcesTimeModel.percentil_noventaCinco = percentile(resourcesTimeModel.diffResourceTimes,95);
                resourcesTimeModel.probability = resourceNode.probability;
                // percentagem
                diffRsrcTimes.add(resourcesTimeModel);
            }
        }
        csvWriter.SaveDiffResourcesTimes(diffRsrcTimes, filename);
        Set<ResourceNodeModel> nodesWithoutDuplicates = new LinkedHashSet<ResourceNodeModel>(nodeList);
        nodeList.clear();
        nodeList.addAll(nodesWithoutDuplicates);

        jsonWriter.SaveDiffResourcesTimes(nodeList, diffRsrcTimes, filename);
    }

    private void combinations(ArrayList<String> resources, int len, int startPosition, String[] result){
        if (len == 0){
            System.out.println(Arrays.toString(result));
            combinationsArray.add(Arrays.toString(result));
            return;
        }
        for (int i = startPosition; i <= resources.size()-len; i++){
            result[result.length - len] = resources.get(i);
            combinations(resources, len-1, i+1, result);
        }
    }

    private void calculateCombinationPercentage(LinkedHashMap<String, ArrayList<ResourceInfo>> resourcesPerRun, int fileCount){
        // <combination, numberOfCounts>
        LinkedHashMap<String, Integer> countCombinations = new LinkedHashMap<>();
        int[] count = new int[]{0};
        int[] countRun = new int[]{0};
        // foreach run/file
        for (int i =0; i < fileCount; i++) {
            // foreach combination
            for(String combination : combinationsArray){
                String combinationFormat = combination.replace("[","").replace("]", "").replace(" ", "");
                String[] resource = combinationFormat.split(","); // resources of each combination
                for(String combinationResource : resource){
                    for(Map.Entry<String, ArrayList<ResourceInfo>> hashMap : resourcesPerRun.entrySet()){
                        //verificar se em cada key do hashmap existe aquele resource com o run i
                        if(combinationResource.equals(hashMap.getKey())){ // combination resource there is on the resources list
                            //iterating over Resource values only
                            Iterator<ResourceInfo> values = hashMap.getValue().iterator();
                            while(values.hasNext()){
                                ResourceInfo infoValue = values.next();
                                if(infoValue.harRun == i){ // this resource there is on run i
                                    count[0]++;
                                    break;  //encontrou, então sair do loop
                                }
                            }

                        }
                    }
                }
                if (count[0] == resource.length) { //combinação existe no run i
                    countRun[0]++;
                    if(countCombinations.containsKey(Arrays.toString(resource))){
                        int numberRuns = countCombinations.get(Arrays.toString(resource));
                        countCombinations.put(Arrays.toString(resource), numberRuns + countRun[0]); //"update" do count da combinação
                    }
                    else {
                        countCombinations.put(Arrays.toString(resource), countRun[0]);
                    }
                    count[0] = 0;
                    countRun[0] = 0;
                }
                else{ // esta combinação n existe no run i
                    if(countCombinations.containsKey(Arrays.toString(resource))){
                        int numberRuns = countCombinations.get(Arrays.toString(resource));
                        countCombinations.put(Arrays.toString(resource), numberRuns + countRun[0]);
                    }
                    else {
                        countCombinations.put(Arrays.toString(resource), countRun[0]); // insert combination with count 0
                    }
                    count[0] = 0;
                    countRun[0] = 0;
                }
            }
        }
        csvWriter.SaveResourcesCombinationsProbabilities(countCombinations, filename, fileCount);
    }

    public void FillResourcesMap(String fileName) {
        int[] count = new int[]{0};
        int[] fileCount = new int[]{0};
        LinkedHashMap<String, ArrayList<ResourceInfo>> newTimeHarMap = new LinkedHashMap<String, ArrayList<ResourceInfo>>();
        ArrayList<String> checkDuplicates = new ArrayList<String>();
        try {
            HarReader harReader = new HarReader();
            File file = new File("D:/Programas/XAMPP/htdocs/webbench/src/main/java/files/" + fileName + ".har");
            Har harTest = harReader.readFromFile(file);
            harTest.getLog().getEntries().forEach(entry -> {
                if(!entry.getResponse().getHeaders().get(0).getValue().contains("no-cache")){
                    ResourceInfo resourceInfo = new ResourceInfo();
                    resourceInfo.resourceTime = (float) entry.getTime();
                    resourceInfo.resourceType = entry.get_resourceType();
                    resourceInfo.cachedResource = entry.getResponse().getHeaders().get(0).getValue();
                    resourceInfo.harRun = fileCount[0];
                    ArrayList<ResourceInfo> resourcesList = new ArrayList<>();
                    resourcesList.add(resourceInfo);
                    timeHarMap.put(entry.getRequest().getUrl(), resourcesList);
                }
            });
            while (file.exists()){
                fileCount[0]++;
                file = new File("D:/Programas/XAMPP/htdocs/webbench/src/main/java/files/" + fileName + "_" + fileCount[0] +".har");
                if(!file.exists())
                    break;
                Har otherHar = harReader.readFromFile(file);
                otherHar.getLog().getEntries().forEach(otherEntry -> {
                    Set<String> keys = timeHarMap.keySet();
                    for(String key : keys){
                        //se o url já existir no dicionário
                        if(otherEntry.getRequest().getUrl().equals(key)){
                            if(!otherEntry.getResponse().getHeaders().get(0).getValue().contains("no-cache")) {
                                ResourceInfo resourceInfo = new ResourceInfo();
                                resourceInfo.resourceTime = (float) otherEntry.getTime();
                                resourceInfo.resourceType = otherEntry.get_resourceType();
                                resourceInfo.cachedResource = otherEntry.getResponse().getHeaders().get(0).getValue();
                                // Value update
                                ArrayList<ResourceInfo> values = new ArrayList<>();
                                values.addAll(timeHarMap.get(key));
                                values.forEach(value -> {
                                    if (value.resourceTime.equals(resourceInfo.resourceTime)) {
                                        resourceInfo.repeatedCall = true;
                                    }
                                });
                                resourceInfo.harRun = fileCount[0];
                                values.add(resourceInfo);
                                timeHarMap.put(key, values);
                            }
                        }
                    }
                    if(!timeHarMap.containsKey(otherEntry.getRequest().getUrl())){
                        if(!otherEntry.getResponse().getHeaders().get(0).getValue().contains("no-cache")) {
                            ResourceInfo resourceInfo = new ResourceInfo();
                            resourceInfo.resourceTime = (float) otherEntry.getTime();
                            resourceInfo.resourceType = otherEntry.get_resourceType();
                            resourceInfo.cachedResource = otherEntry.getResponse().getHeaders().get(0).getValue();
                            resourceInfo.harRun = fileCount[0];
                            ArrayList<ResourceInfo> resourcesList = new ArrayList<>();
                            resourcesList.add(resourceInfo);
                            timeHarMap.put(otherEntry.getRequest().getUrl(), resourcesList);
                        }
                    }
                });
            }

            // add resources IDs to hashMap
            Iterator<Map.Entry<String, ArrayList<ResourceInfo>>> entries = timeHarMap.entrySet().iterator();
            while(entries.hasNext()){
                Map.Entry<String, ArrayList<ResourceInfo>> entry = entries.next();
                ArrayList<ResourceInfo> resourcesList = new ArrayList<>();
                resourcesList.addAll(entry.getValue());
                newTimeHarMap.put(count[0] + "-" + entry.getKey(), resourcesList);
                count[0]++;
            }
            // calculate time difference between all resources. FileCount = number of files
            calculateResourcesFromTests(newTimeHarMap, fileCount[0]);
            ArrayList<String> allResources = new ArrayList<>(newTimeHarMap.keySet());
           // for(int i=1; i<= 5; i++) { //starts with combination [R1,R2]. Next iteration [R1,R2,R3]...
                combinations(allResources, 5, 0, new String [5]);
            //}

            calculateCombinationPercentage(newTimeHarMap, fileCount[0]);
        } catch (Exception ex) {
            // e.printStackTrace();
            System.out.println(ex.getMessage());
        }
    }
}
