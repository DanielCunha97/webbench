public class LogWebItem {
    Integer coordX = null;
    Integer coordY = null;
    Double size = null;
    Double predictedTime;

    public LogWebItem(Integer coordX, Integer coordY, Double size, Double predictedTime) {
        this.coordX = coordX;
        this.coordY = coordY;
        this.size = size;
        this.predictedTime = predictedTime;
    }

    public LogWebItem(Double predictedTime) {
        this.predictedTime = predictedTime;
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
