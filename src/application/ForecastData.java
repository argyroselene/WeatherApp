package application;

public class ForecastData {
    private String date;
    private double maxTemp;
    private double minTemp;
    private String condition;

    public ForecastData(String date, double maxTemp, double minTemp, String condition) {
        this.date = date;
        this.maxTemp = maxTemp;
        this.minTemp = minTemp;
        this.condition = condition;
    }

    public String getDate() { return date; }
    public double getMaxTemp() { return maxTemp; }
    public double getMinTemp() { return minTemp; }
    public String getCondition() { return condition; }
}
