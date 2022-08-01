import com.google.gson.Gson;

import javax.jms.*;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Timer;
import java.util.TimerTask;

public class Main {
    public static void main(String[] args) throws IOException, JMSException {
        URL url = new URL("http://api.openweathermap.org/data/2.5/weather?q=London&units=metric&appid=0d4b62701294de6884f6283a36539315");

        Timer timer = new Timer();

        String dir = "C:/Users/sergi/OneDrive - Universidad de Las Palmas de Gran Canaria/DACD/practicas/";

        DatalakeBuilder.setDir(dir);
        DatalakeBuilder.setupSubscriber();
        Analytics.setDir(dir);
        Analytics.setupSubscriber();

        TimerTask task = new TimerTask() {
            @Override
            public void run()
            {
                String responseBody = Sensor.sendAPIRequest(url);

                Weather weather = Parser.parseHttpRequest(responseBody);
                Gson gson = new Gson();

                try {
                    Sensor.sendEvent(gson.toJson(weather));
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }
        };
        timer.schedule(task, 10, 9000);
    }
}
