import com.google.gson.Gson;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class DatalakeBuilder {
    private static String dir;

    public static void setDir(String path){
        dir = path;
    }

    public static void writeToFile(String dir, TextMessage textMessage) throws IOException, JMSException {
        Weather weather = Parser.parseTextMessage(textMessage);
        Gson gson = new Gson();
        String info = gson.toJson(weather);
        String date = weather.ts.substring(0, 10);
        String hour = weather.ts.substring(11, 13);
        dir = dir + "datalake/events/sensor.Weather/";
        File directory = new File(dir);
        if (!directory.exists()){
            System.out.println(directory.mkdirs());
        }
        String filepath = dir + date + "-" + hour + ".json";
        File f = new File(filepath);
        if (!f.exists()) {
            f.createNewFile();
        }
        FileWriter file = new FileWriter(dir+date+"-"+hour+".json", true);
        file.write(info+"\n");
        file.close();
    }

    public static void setupSubscriber() throws JMSException {
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(ActiveMQConnection.DEFAULT_BROKER_URL);
        Connection connection = connectionFactory.createConnection();
        connection.setClientID("datalake-builder");
        connection.start();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        Topic topic = session.createTopic("sensor.Weather");

        TopicSubscriber subscriber = session.createDurableSubscriber(topic, "datalake-builder");
        MessageListener listener = new MessageListener() {
            public void onMessage(Message message) {
                TextMessage textMessage = (TextMessage) message;
                try {
                    System.out.println(textMessage.getText());
                    writeToFile(dir, textMessage);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        subscriber.setMessageListener(listener);
    }
}
