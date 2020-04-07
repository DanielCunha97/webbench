import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class CsvWriter {

    public void SaveStatistics (ArrayList<OperatorsStatistics> operatorsStatistics){
        try {
            PrintWriter writer = new PrintWriter(new File("D:/Programas/XAMPP/htdocs/ChartsKLM/DataToCharts.csv"));
            StringBuilder sb = new StringBuilder();
            sb.append("Operator");
            sb.append(',');
            sb.append("Number of Interactions");
            sb.append(',');
            sb.append("Percentage");
            sb.append(',');
            sb.append("TimePerOperator");
            sb.append('\n');
            for(int i=0; i <operatorsStatistics.size(); i++) {
                sb.append(operatorsStatistics.get(i).operator + ",");
                sb.append(operatorsStatistics.get(i).count + ",");
                sb.append(operatorsStatistics.get(i).percentage + ",");
                sb.append(operatorsStatistics.get(i).timePerOperator);
                sb.append('\n');
            }
            writer.write(sb.toString());
            writer.close();
        } catch (Exception e) {
            // e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }

}
