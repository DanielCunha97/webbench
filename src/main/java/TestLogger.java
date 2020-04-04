import java.util.ArrayList;
import java.util.HashMap;

public class TestLogger {
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

    public void calculateOperatorsPercentage(String klmInput){
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
        return klmResult;
    }

    public void calculateTotalOperators(String klmInput){
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
    }
}
