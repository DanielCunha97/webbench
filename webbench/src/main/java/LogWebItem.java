import java.util.HashMap;

public class LogWebItem {
    Integer coordX = null;
    Integer coordY = null;
    Double size = null;
    Double predictedTime;
    String KLMInput;

    public LogWebItem(Integer coordX, Integer coordY, Double size, Double predictedTime, String KLMInput) {
        this.coordX = coordX;
        this.coordY = coordY;
        this.size = size;
        this.predictedTime = predictedTime;
        this.KLMInput =KLMInput;
    }

    public LogWebItem(Double predictedTime,String KLMInput) {
        this.predictedTime = predictedTime;
        this.KLMInput = KLMInput;
    }

    public String getKLMInput() {
        return KLMInput;
    }

    public Integer getCoordX() {
        return coordX;
    }

    public Integer getCoordY() {
        return coordY;
    }

    public Double getSize() {
        return size;
    }

    public Double getPredictedTime() {
        return predictedTime;
    }
}
