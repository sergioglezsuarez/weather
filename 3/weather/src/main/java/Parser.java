import org.json.JSONArray;
import org.json.JSONObject;

import javax.jms.JMSException;
import javax.jms.TextMessage;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

public class Parser {
    public static Weather parseHttpRequest(String responseBody){
        JSONObject album = new JSONObject(responseBody);
        float lon = album.getJSONObject("coord").getFloat("lon");
        float lat = album.getJSONObject("coord").getFloat("lat");

        JSONArray weatherArray = album.getJSONArray("weather");
        JSONObject weatherObject = weatherArray.getJSONObject(0);
        String weatherMain = weatherObject.getString("main");

        JSONObject main = album.getJSONObject("main");
        float mainTemp = main.getFloat("temp");
        int mainPressure = main.getInt("pressure");
        int mainHumidity = main.getInt("humidity");

        JSONObject wind = album.getJSONObject("wind");
        float windSpeed = wind.getFloat("speed");
        int windDeg = wind.getInt("deg");

        Location location = new Location(lon, lat);

        String ts = DateTimeFormatter.ISO_INSTANT.format(Instant.now());

        return new Weather(weatherMain, mainTemp, windSpeed, windDeg, mainHumidity, mainPressure, ts, location);
    }

    public static Weather parseTextMessage(TextMessage textMessage) throws JMSException {
        JSONObject album = new JSONObject(textMessage.getText());
        float lon = album.getJSONObject("location").getFloat("lon");
        float lat = album.getJSONObject("location").getFloat("lat");
        Location location = new Location(lon, lat);
        String weather = album.getString("weather");
        float temp = album.getFloat("temp");
        float wind = album.getFloat("wind");
        int windDirection = album.getInt("windDirection");
        int humidity = album.getInt("humidity");
        int pressure = album.getInt("pressure");
        String ts = album.getString("ts");
        return new Weather(weather, temp, wind, windDirection, humidity, pressure, ts, location);
    }
}
