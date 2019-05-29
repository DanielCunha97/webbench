import java.util.HashMap;

public class KLMModel {
    private HashMap<Character,Float> timeMap = new HashMap<Character,Float>();
    private static KLMModel model = null;

    private KLMModel() {
        fillTimeMap();
    }

    private void fillTimeMap() {
        this.timeMap.put('K', 0.2f);
        this.timeMap.put('P', 1.1f);
        this.timeMap.put('H', 0.4f);
        this.timeMap.put('M', 1.35f);
    }


    public synchronized static KLMModel instance(){
        if(model == null) model = new KLMModel();

        return model;
    }

    public double getPredictedTime(String action, double distance, double size){

        String klmInput = null;
        switch(action){
            case "click":
                klmInput = "MHPK";
                break;
            case "submit":
                klmInput = "MHPK";
                break;
            case "close":
                klmInput = "MHPK";
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

        double predictedTime = calculateTotalTime(distance, size, klmInput);

        System.out.println("size:" + size);
        System.out.println("distance:" + distance);
        System.out.println("predicted time:" + predictedTime);

        return predictedTime;
    }

    public double getPredictedTime(String action, String typedValue, double distance, double size){

        String klmInput = null;
        switch(action){
            case "open":
                klmInput = "MH" + typedValue.replaceAll(".","K");
                break;
            case "type":
                klmInput = "MHP" + typedValue.replaceAll(".","K");
                break;
            default:
                throw new AssertionError(action + "is and invalid action");
        }

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