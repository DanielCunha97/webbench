import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import harreader.HarReader;
import harreader.HarReaderException;
import harreader.model.Har;
import harreader.model.HarEntry;


import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


public class HarFileModel {
    private CsvWriter csvWriter = new CsvWriter();
    private JsonWriter jsonWriter = new JsonWriter();
    private String filename;
    // with LinkedHashMap we preserve insertion order in Hashmap
    private LinkedHashMap<String, ArrayList<ResourceInfo>> timeHarMap = new LinkedHashMap<String, ArrayList<ResourceInfo>>();
    LinkedList<ProcessCombinationModel> combinationStatistics = new LinkedList<>();

    public HarFileModel(String fileName) throws HarReaderException {
        this.filename = fileName;
        FillResourcesMap(fileName);
    }

    /*public LinkedHashMap<String, ResourceInfo> GetTime() {
        return timeMap;
    }*/


    private static Float percentile(List<Float> latencies, double percentile) {
        Collections.sort(latencies);
        int index = (int) Math.ceil(percentile / 100.0 * latencies.size());
        return latencies.get(index-1);
    }

    private void calculateResourcesFromTests(int fileCount){
        List<ResourceNodeModel> nodeList = new ArrayList<ResourceNodeModel>();
        LinkedHashMap<String, ArrayList<ResourceInfo>> resourcesTtimes = new LinkedHashMap<String, ArrayList<ResourceInfo>>(timeHarMap);
        ArrayList<ResourcesTimeModel> diffRsrcTimes = new ArrayList<>();
        int counter = 0;
        for (Map.Entry<String, ArrayList<ResourceInfo>> hashMap : timeHarMap.entrySet()) {
            // gravar apenas os nodes para o grafo (firstResources)
            ResourceNodeModel resourceNode = new ResourceNodeModel();
            resourceNode.firstRsrc = (counter++) + "-" + hashMap.getKey().toString();
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

    /**
     *
     * @param resources
     * @param len
     * @param fileCount
     */
    private void combinations(ArrayList<String> resources, int len, int fileCount){
        System.out.println("Número resources " + resources.size());
        Set<Set<String>> combinations = Sets.combinations(ImmutableSet.copyOf(resources), len);
        Set r;
        StringBuffer line = new StringBuffer();
        Iterator combIterator = combinations.iterator();
        System.out.println("Número combinações " + combinations.size());
        int i = 0;
        while (combIterator.hasNext()){
            System.out.println("Comb " + i++);
            r = (Set) combIterator.next();
            //System.out.println("R size: "+ r.size());

            line.setLength(0);
            Iterator lineIterator = r.iterator();
            while(lineIterator.hasNext()){
                if(line.length()>0) line.append(",");
                line.append(lineIterator.next().toString());
            }

            ProcessCombinationModel combinationInfo= new ProcessCombinationModel();
            combinationInfo.combination = line.toString();
            calculateStatistics(combinationInfo,fileCount);
        }
    }

    /**
     * @param combinationInfo single combination of resources
     * @param fileCount number of runs
     * @return boolean indication whether the combination was found
     */
    private void calculateStatistics(ProcessCombinationModel combinationInfo, int fileCount){
        boolean resourceFound=false;

        for (int i =0; i < fileCount; i++) { //controlo por run
            String[] resources = combinationInfo.combination.split(","); // resources of each combination
            for(String combinationResource : resources){
                resourceFound=false;
                for(ResourceInfo comb : timeHarMap.get(combinationResource))
                    if(comb.harRun == i) {
                        resourceFound = true;
                        combinationInfo.resourceLength += comb.resourceLength;
                        break;
                    }
                if(! resourceFound){ break;}
                combinationInfo.numberOfRuns ++;
            }
        }
        combinationInfo.percentage = combinationInfo.numberOfRuns/fileCount;
        if(combinationInfo.percentage > 0.5)
            this.combinationStatistics.add(combinationInfo);
    }


  /*  private void calculateCombinationPercentage(int fileCount){
        long sumResourceLength = 0;
        // <combination, numberOfCounts>
        LinkedHashMap<String, ProcessCombinationModel> countCombinations = new LinkedHashMap<>();
        ArrayList<ResourceCombination> resourcesCombinationList = new ArrayList<>();
        int[] count = new int[]{0};
        int[] countRun = new int[]{0};
        // foreach run/file
        for (int i =0; i < fileCount; i++) {
            // foreach combination
            for(String combination : combinationsArray){
                //String combinationFormat = combination.replace("[","").replace("]", "").replace(" ", "");
                String[] resource = combination.split(","); // resources of each combination
                for(String combinationResource : resource){
                    for(Map.Entry<String, ArrayList<ResourceInfo>> hashMap : timeHarMap.entrySet()){
                        //verificar se em cada key do hashmap existe aquele resource com o run i
                        if(combinationResource.equals(hashMap.getKey())){ // combination resource there is on the resources list
                            //iterating over Resource values only
                            Iterator<ResourceInfo> values = hashMap.getValue().iterator();
                            while(values.hasNext()){
                                ResourceInfo infoValue = values.next();
                                if(infoValue.harRun == i){ // this resource there is on run i
                                    count[0]++;
                                    sumResourceLength = sumResourceLength + infoValue.resourceLength;
                                    break;  //encontrou, então sair do loop
                                }
                            }

                        }
                    }
                }
                if (count[0] == resource.length) { //combinação existe no run i
                    countRun[0]++;
                    if(countCombinations.containsKey(Arrays.toString(resource))){
                        ProcessCombinationModel combinationInfo = new ProcessCombinationModel();
                        combinationInfo.numberOfRuns = countCombinations.get(Arrays.toString(resource)).numberOfRuns + countRun[0];
                        combinationInfo.resourceLength = sumResourceLength;
                        countCombinations.put(Arrays.toString(resource), combinationInfo); //"update" info about combination
                    }
                    else { //create new one
                        ProcessCombinationModel combinationInfo = new ProcessCombinationModel();
                        combinationInfo.numberOfRuns = countRun[0];
                        combinationInfo.resourceLength = sumResourceLength;
                        countCombinations.put(Arrays.toString(resource), combinationInfo);
                    }
                    sumResourceLength = 0;
                    count[0] = 0;
                    countRun[0] = 0;
                }
                else{ // esta combinação n existe no run i
                    if(countCombinations.containsKey(Arrays.toString(resource))){
                        ProcessCombinationModel combinationInfo = new ProcessCombinationModel();
                        combinationInfo.numberOfRuns = countCombinations.get(Arrays.toString(resource)).numberOfRuns + countRun[0];
                        combinationInfo.resourceLength = sumResourceLength;
                        countCombinations.put(Arrays.toString(resource), combinationInfo);
                    }
                    else {
                        ProcessCombinationModel combinationInfo = new ProcessCombinationModel();
                        combinationInfo.numberOfRuns = countRun[0];
                        combinationInfo.resourceLength = sumResourceLength;
                        countCombinations.put(Arrays.toString(resource), combinationInfo); // insert combination with count 0
                    }
                    sumResourceLength = 0;
                    count[0] = 0;
                    countRun[0] = 0;
                }
            }
        }
        // calcular a probabilidade de cada recurso e dentro do outro ciclo verificar qd há vários casos com precentagens superiores
        // a 50%
        for(Map.Entry<String, ProcessCombinationModel> hashMap : countCombinations.entrySet()){
           // if((double) Math.round(hashMap.getValue().numberOfRuns*100)/fileCount >=50) {
                ResourceCombination resourceCombination = new ResourceCombination();
                resourceCombination.combination = hashMap.getKey();
                resourceCombination.percentage = (double) Math.round(hashMap.getValue().numberOfRuns * 100) / fileCount;
                resourceCombination.resourceLength = hashMap.getValue().resourceLength;
                resourcesCombinationList.add(resourceCombination);
           // }
        }

       *//* for(ResourceCombination combinationInfo : resourcesCombinationList){
            if()
        }*//*
        csvWriter.SaveResourcesCombinationsProbabilities(resourcesCombinationList, filename);
    }*/

    public void FillResourcesMap(String fileName) throws HarReaderException {
        int[] count = new int[]{0};
        int fileCount = 0;
        LinkedHashMap<String, ArrayList<ResourceInfo>> newTimeHarMap = new LinkedHashMap<String, ArrayList<ResourceInfo>>();
        try {
            HarReader harReader = new HarReader();
            /*
            File file = new File("/Users/cunha/Downloads/webbench/src/main/java/files/" + fileName + ".har");
            Har harTest = harReader.readFromFile(file);
            harTest.getLog().getEntries().forEach(entry -> {
                if(!entry.getResponse().getHeaders().get(0).getValue().contains("no-cache")){
                    ResourceInfo resourceInfo = new ResourceInfo();
                    resourceInfo.resourceTime = (float) entry.getTime();
                    resourceInfo.resourceType = entry.get_resourceType();
                    resourceInfo.cachedResource = entry.getResponse().getHeaders().get(0).getValue();
                    resourceInfo.harRun = fileCount;
                    resourceInfo.resourceLength = entry.getResponse().getBodySize();
                    ArrayList<ResourceInfo> resourcesList = new ArrayList<>();
                    resourcesList.add(resourceInfo);
                    timeHarMap.put(entry.getRequest().getUrl(), resourcesList);
                }
            });*/
            File file = new File("/Users/cunha/Downloads/webbench/src/main/java/files/" + fileName + ".har");
            while (file.exists()){
                Har otherHar = harReader.readFromFile(file);
                for (HarEntry otherEntry : otherHar.getLog().getEntries()) {//Set<String> keys = timeHarMap.keySet();
                    //for(String key : keys){
                    //se o url já existir no dicionário
                    //if(otherEntry.getRequest().getUrl().equals(key)){
                    if (!otherEntry.getResponse().getHeaders().get(0).getValue().contains("no-cache")) {
                        ResourceInfo resourceInfo = new ResourceInfo();
                        resourceInfo.resourceTime = (float) otherEntry.getTime();
                        resourceInfo.resourceType = otherEntry.get_resourceType();
                        resourceInfo.cachedResource = otherEntry.getResponse().getHeaders().get(0).getValue();
                        resourceInfo.resourceLength = otherEntry.getResponse().getBodySize();
                        resourceInfo.harRun = fileCount;

                        if (timeHarMap.containsKey(otherEntry.getRequest().getUrl())) {
                            ArrayList<ResourceInfo> list = timeHarMap.get(otherEntry.getRequest().getUrl());
                            AtomicBoolean repeatedCall = new AtomicBoolean(false);
                            list.forEach(value -> {
                                if (value.resourceTime.equals(resourceInfo.resourceTime)) {
                                    repeatedCall.set(true);
                                    return;
                                }
                            });
                            if (!repeatedCall.get())
                                timeHarMap.get(otherEntry.getRequest().getUrl()).add(resourceInfo);
                        } else {
                            ArrayList<ResourceInfo> l = new ArrayList<>();
                            l.add(resourceInfo);
                            timeHarMap.put(otherEntry.getRequest().getUrl(), l);
                        }

                    }
                }
                file = new File("/Users/cunha/Downloads/webbench/src/main/java/files/" + fileName + "_" + ++fileCount +".har");
            }

            // add resources IDs to hashMap
            /*Iterator<Map.Entry<String, ArrayList<ResourceInfo>>> entries = timeHarMap.entrySet().iterator();
            while(entries.hasNext()){
                Map.Entry<String, ArrayList<ResourceInfo>> entry = entries.next();
                ArrayList<ResourceInfo> resourcesList = new ArrayList<>();
                resourcesList.addAll(entry.getValue());
                newTimeHarMap.put(count[0] + "-" + entry.getKey(), resourcesList);
                count[0]++;
            }*/
            // calculate time difference between all resources. FileCount = number of files
            calculateResourcesFromTests(fileCount);
            ArrayList<String> allResources = new ArrayList<>(timeHarMap.keySet());
            combinations(allResources, 41, fileCount);
            //calculateCombinationPercentage(fileCount);
        } catch (Exception ex) {
            // e.printStackTrace();
            System.out.println(ex.getMessage());
        }
    }
}
