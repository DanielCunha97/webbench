import de.sstoehr.harreader.HarReaderException;

import java.util.ArrayList;
import java.util.HashMap;

public class KLMModel {
    private HashMap<Character,Float> timeMap = new HashMap<Character,Float>();
    private static KLMModel model = null;
    HarFileModel harFileModel = new HarFileModel();

    private KLMModel()  {
        fillTimeMap();
    }
    float wOperatorTime = /*harFileModel.GetTime()*/0.0f ;
    private void fillTimeMap() {
        this.timeMap.put('K', 0.3f);
        this.timeMap.put('P', 1.1f);
        this.timeMap.put('B', 0.1f);
        this.timeMap.put('H', 0.4f);
        this.timeMap.put('M', 1.35f);
        this.timeMap.put('W', wOperatorTime);  // the value is dynamic because depends from performance of network..
    }

    public HashMap<Character,Float> getOperatorsTimes() {
        return timeMap;
    }

    public synchronized static KLMModel instance(){
        if(model == null) model = new KLMModel();

        return model;
    }

    public String getKLMInput(String action, String typedValue){
        String klmInput = null;
        switch(action){
            case "open": //terá de ser introduzido o valor do operador W
                klmInput = "MH" + typedValue.replaceAll(".","K");
                break;
            case "type": //Key
                klmInput = "MHP" + typedValue.replaceAll(".","K");
                break;
            case "click":
                klmInput = "HMPB";
                break;
            case "submit":
                klmInput = "MHPB";
                break;
            case "close":
                klmInput = "MHPB";
                break;
            case "mouseOver":
                klmInput = "MHPK";
                break;
            case "select":
                klmInput = "MHPK";
                break;
            default:
                throw new AssertionError(action + "is and invalid action");
        }

        return klmInput;
    }

    public double getPredictedTime(String action, double distance, double size){

        String klmInput = null;
        klmInput = getKLMInput(action,null);

        double predictedTime = calculateTotalTime(distance, size, klmInput);

        System.out.println("size:" + size);
        System.out.println("distance:" + distance);
        System.out.println("predicted time:" + predictedTime);

        return predictedTime;
    }

    public double getPredictedTime(String action, String typedValue, double distance, double size){

        String klmInput = null;
        klmInput = getKLMInput(action,typedValue);

        double predictedTime = calculateTotalTime(distance, size, klmInput);

        System.out.println("size:" + size);
        System.out.println("distance:" + distance);
        System.out.println("predicted time:" + predictedTime);

        return predictedTime;
    }

    private double calculateTotalTime(double distance, double size, String klmInput) {
        double totalTime = 0.0f;
        for(char c : klmInput.toCharArray()){
            totalTime += this.timeMap.get(c)+ ((c=='P' && distance>0)?fittsLawIndexDifficulty(distance,size):0);
        }

        return totalTime;
    }

    public double fittsLawIndexDifficulty(double distance, double size){
        return Math.max(0.0, Math.log(1+ distance/size)/Math.log(2.0));
    }
}