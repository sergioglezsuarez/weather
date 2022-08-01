public class Weather {
    public String weather;
    public float temp;
    public float wind;
    public int windDirection;
    public int humidity;
    public int pressure;
    public String ts;
    public Location location;

    public Weather(String weather, float temp, float wind, int windDirection, int humidity, int pressure, String ts, Location location){
        this.ts = ts;
        this.location = location;
        this.weather = weather;
        this.temp = temp;
        this.wind = wind;
        this.windDirection = windDirection;
        this.humidity = humidity;
        this.pressure = pressure;
    }
}
