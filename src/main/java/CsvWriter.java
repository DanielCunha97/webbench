import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class CsvWriter {
    private String klmResult;

    public CsvWriter(){

    }

    public void SaveKLMString(String klmResult){
        this.klmResult = klmResult;
    }

    public void SaveStatistics (ArrayList<OperatorsStatistics> operatorsStatistics){
        try {
            PrintWriter writer = new PrintWriter(new File("D:/Programas/XAMPP/htdocs/ChartsKLM/DataToCharts.csv"));
            StringBuilder sb = new StringBuilder();
            sb.append("Operator");
            sb.append(';');
            sb.append("Number of Interactions");
            sb.append(';');
            sb.append("Percentage");
            sb.append(';');
            sb.append("TimePerOperator");
            sb.append('\n');
            for(int i=0; i <operatorsStatistics.size(); i++) {
                sb.append(operatorsStatistics.get(i).operator + ";");
                sb.append(operatorsStatistics.get(i).count + ";");
                sb.append(operatorsStatistics.get(i).percentage + ";");
                sb.append(operatorsStatistics.get(i).timePerOperator);
                sb.append('\n');
            }
            // Change this code later!!
            sb.append("W" + ";");
            sb.append(0 + ";");
            sb.append(0.0 + ";");
            sb.append(0.0);
            sb.append('\n');
            //----------------------------------
            sb.append(klmResult);
            writer.write(sb.toString());
            writer.close();
        } catch (Exception e) {
            // e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }

    public void SaveDiffResourcesTimes (ArrayList<ResourcesTimeModel> resourcesTimeModels){
        try {
            PrintWriter writer = new PrintWriter(new File("C:/Users/Daniel Cunha/Desktop/webbench/src/ResourcesTimes.csv"));
            StringBuilder sb = new StringBuilder();
            sb.append("First Resource");
            sb.append(';');
            sb.append("Second Resource");
            sb.append(';');
            sb.append("Time difference between them");
            sb.append('\n');
            resourcesTimeModels.forEach(resource -> {
                sb.append(resource.firstRsrc + ";");
                sb.append(resource.secondRsrc + ";");
                sb.append(resource.diffResourceTime);
                sb.append('\n');
            });
            writer.write(sb.toString());
            writer.close();
        } catch (Exception e) {
            // e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }
}
