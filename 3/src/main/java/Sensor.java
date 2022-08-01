import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Sensor {
    private static HttpURLConnection connection;

    public static String sendAPIRequest(URL url){
        BufferedReader reader;
        String line;
        StringBuffer responseContent = new StringBuffer();
        try {
            connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            int status = connection.getResponseCode();

            if (status > 299) {
                reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            } else {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            }
            while ((line = reader.readLine()) != null) {
                responseContent.append(line);
            }
            reader.close();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            connection.disconnect();
        }
        return responseContent.toString();
    }

    public static void sendEvent(String weather) throws JMSException {
        TopicConnectionFactory connectionFactory = new ActiveMQConnectionFactory(ActiveMQConnection.DEFAULT_BROKER_URL);
        TopicConnection connection = connectionFactory.createTopicConnection();
        connection.start();

        TopicSession session = connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);

        Topic topic = session.createTopic("sensor.Weather");
        TopicPublisher publisher = session.createPublisher(topic);
        TextMessage message = session.createTextMessage(weather);

        publisher.send(message);

        System.out.println("Sending '" + message.getText() + "'");
    }
}