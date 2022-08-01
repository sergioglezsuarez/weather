import javax.jms.JMSException;
import javax.jms.TextMessage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Instant;

public class LinearRegression {
    public static void trainAndPredict(String dir, TextMessage textMessage) throws IOException, JMSException {
        String[] commands = {"python", "\"C:/Users/sergi/OneDrive - Universidad de Las Palmas de Gran Canaria/DACD/practicas/linearRegression/main.py\"",
                "train", dir};
        Process process = new ProcessBuilder(commands).start();
        InputStream is = process.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);

        String line;
        while ((line = br.readLine()) != null) {
            System.out.println(line);
        }

        try {
            int exitValue = process.waitFor();
            if (exitValue != 0) {
                System.out.println("\nCódigo de salida: " + exitValue);
            }
        } catch (InterruptedException e) {
            e.printStackTrace(System.err);
        }

        Weather weather = Parser.parseTextMessage(textMessage);
        commands = new String[]{"python",
                "\"C:/Users/sergi/OneDrive - Universidad de Las Palmas de Gran Canaria/DACD/practicas/linearRegression/main.py\"",
                "predict", Instant.parse(weather.ts).toEpochMilli() + "", weather.location.lat + "", weather.location.lon + "",
                weather.pressure + "", weather.humidity + ""};
        process = new ProcessBuilder(commands).start();
        is = process.getInputStream();
        isr = new InputStreamReader(is);
        br = new BufferedReader(isr);

        while ((line = br.readLine()) != null) {
            System.out.println(line);
        }

        System.out.println("actual temperature: " + weather.temp);

        try {
            int exitValue = process.waitFor();
            if (exitValue != 0) {
                System.out.println("\nCódigo de salida: " + exitValue);
            }
        } catch (InterruptedException e) {
            e.printStackTrace(System.err);
        }
    }
}
