public class OperatorsStatistics {
    String operator;
    int count;
    float percentage;
    float timePerOperator;

    public OperatorsStatistics(String operator, int count, float percentage, float timePerOperator) {
        this.operator = operator;
        this.count = count;
        this.percentage = percentage;
        this.timePerOperator = timePerOperator;
    }

    public OperatorsStatistics() {

    }
}
