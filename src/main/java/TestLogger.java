import java.util.ArrayList;

public class TestLogger {
    private ArrayList<LogWebItem> log = new ArrayList<LogWebItem>();

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
}
