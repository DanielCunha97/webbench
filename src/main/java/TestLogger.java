import java.util.ArrayList;
import java.util.HashMap;

public class TestLogger {
    StringBuilder sb = new StringBuilder();
    public String cleanKlmString= new String("");
    private ArrayList<LogWebItem> log = new ArrayList<LogWebItem>();
    private ArrayList<OperatorsStatistics> operatorsCount = new ArrayList<OperatorsStatistics>();

    public TestLogger(){}

    public void addItem(LogWebItem logItem){
        log.add(logItem);
    }

    public LogWebItem getWebItem(int index){
        return log.get(index);
    }

    public LogWebItem getLastWebItem(){
        if(log.size()==0) return null;

        return log.get(log.size()-1);
    }

    public float calculateTotalTime(){
        float time = 0.0f;
        for(LogWebItem item : log){
            time+= item.getPredictedTime();
        }
        return time;
    }

    public ArrayList<OperatorsStatistics> getOperatorsCount() {
        return operatorsCount;
    }

    private void calculateOperatorsPercentage(String klmInput){
        for(int i =0; i < operatorsCount.size(); i++){
            OperatorsStatistics operatorsStatistics = new OperatorsStatistics();
            operatorsStatistics.count = operatorsCount.get(i).count;
            operatorsStatistics.operator = operatorsCount.get(i).operator;
            operatorsStatistics.percentage = ((float)operatorsCount.get(i).count/klmInput.length())*100;
            operatorsCount.set(i, operatorsStatistics);
        }
    }

    public String getCompleteKLMInput(){
        String klmResult = new String("");
        for(LogWebItem item : log){
            klmResult += item.getKLMInput(); //Total of operations from KLM input
        }
         return cleanKLMString(klmResult);
    }

    public void calculateTotalOperators(String klmInput, HashMap<Character,Float> timeMap){
        for(char c : klmInput.toCharArray()){
            if(c=='K' || c=='M' || c=='B' || c=='P' || c=='H') {
                if (operatorsCount.isEmpty() || !operatorsCount.stream().anyMatch(p -> p.operator.equals(Character.toString(c)))) {
                    OperatorsStatistics operatorsStatistics = new OperatorsStatistics();
                    operatorsStatistics.count = 0;
                    operatorsStatistics.operator = Character.toString(c);
                    operatorsCount.add(operatorsStatistics);
                }
            }
            for(int i=0; i <operatorsCount.size(); i++){
                if (operatorsCount.get(i).operator.equals(Character.toString(c))) {
                    OperatorsStatistics operatorsStatistics = new OperatorsStatistics();
                    operatorsStatistics.count = operatorsCount.get(i).count + 1;
                    operatorsStatistics.operator = Character.toString(c);
                    operatorsCount.set(i, operatorsStatistics);
                }
            }
        }
        calculateOperatorsPercentage(klmInput);
        calculateTimePerOperator(timeMap);
    }

    private void calculateTimePerOperator(HashMap<Character,Float> timeMap){
        for(int i =0; i < operatorsCount.size(); i++){
            OperatorsStatistics operatorsStatistics = new OperatorsStatistics();
            operatorsStatistics.count = operatorsCount.get(i).count;
            operatorsStatistics.operator = operatorsCount.get(i).operator;
            operatorsStatistics.percentage = operatorsCount.get(i).percentage;
            if (timeMap.containsKey(operatorsCount.get(i).operator.charAt(0))){
                    operatorsStatistics.timePerOperator = timeMap.get(operatorsCount.get(i).operator.charAt(0)) * operatorsStatistics.count;
            }
            operatorsCount.set(i, operatorsStatistics);
        }
    }

    //clean KLM string (ex: PB....PB, remove operator inside this sequence(H operator))
    private String cleanKLMString(String klmInput){
        if(klmInput != null){
            while(!klmInput.replaceFirst("PBHPB", "PBPB").equals(klmInput)){
                klmInput = klmInput.replaceFirst("PBHPB","PBPB");
            }
            while(!klmInput.replaceFirst("HH", "H").equals(klmInput)){
                klmInput = klmInput.replaceFirst("HH","H");
            }
            cleanKlmString = handleMOperator(klmInput);;
            return cleanKlmString;
        }
        return new String();
    }

    // Add M operator before K's sequence
    private String handleMOperator (String klmInput){
        int[] iterator = new int[]{0};
        sb = new StringBuilder(klmInput);
        for (int i= 0; i < klmInput.length(); i++){
            if(klmInput.charAt(i) == 'K'){
                for(int j = i + 1; j > i; j++ ){
                    if(klmInput.charAt(i) != klmInput.charAt(j)){
                        //increment 'i' position in new String when we add a new M on klmString
                        sb.insert(i + iterator[0], 'M');
                        iterator[0]++;
                        i = j + 1;
                    }
                }
            }
        }
        addMOperatorBeforeP(sb.toString());
        removeMOperator();
        return sb.toString();
    }

    private void removeMOperator() {
        String klmSequence = sb.toString();
        while(!klmSequence.replaceFirst("PMB", "PB").equals(klmSequence)){
            klmSequence = klmSequence.replaceFirst("PMB","PB");
        }
        while(!klmSequence.replaceFirst("PMK", "PK").equals(klmSequence)){
            klmSequence = klmSequence.replaceFirst("PMK","PK");
        }
        while(!klmSequence.replaceFirst("HMB", "HB").equals(klmSequence)){
            klmSequence = klmSequence.replaceFirst("HMB","HB");
        }

        // Removing M's that are command terminators
       /* for(){

        }*/

        sb = new StringBuilder(klmSequence);
    }

    private void addMOperatorBeforeP(String klmString) {
        int[] iterator = new int[]{0};
        sb = new StringBuilder(klmString);
        for (int i = 0; i< klmString.length(); i++){
            if(klmString.charAt(i) == 'P'){
                sb.insert(i +  iterator[0], 'M');
                iterator[0]++;
            }
        }
    }
}
