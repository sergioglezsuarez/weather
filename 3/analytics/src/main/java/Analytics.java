import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.sql.Connection;
import java.time.Instant;

public class Analytics {
    private static String dir;

    public static void setDir(String path){
        dir = path;
    }

    public static void setupSubscriber() throws JMSException {
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(ActiveMQConnection.DEFAULT_BROKER_URL);
        javax.jms.Connection connection = connectionFactory.createConnection();
        connection.setClientID("analytics");
        connection.start();
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        Topic topic = session.createTopic("sensor.Weather");

        TopicSubscriber subscriber = session.createDurableSubscriber(topic, "analytics");
        MessageListener listener = new MessageListener() {
            public void onMessage(Message message) {
                TextMessage textMessage = (TextMessage) message;
                try {
                    System.out.println(textMessage.getText());
                    addToDatabase(dir, textMessage);
                    writeToCsv(dir);
                    LinearRegression.trainAndPredict(dir, textMessage);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        subscriber.setMessageListener(listener);
    }

    private static Connection connect(String dir) throws SQLException {
        dir = "jdbc:sqlite:" + dir;
        return DriverManager.getConnection(dir + "weather.db");
    }

    public static void addToDatabase(String dir, TextMessage textMessage) throws JMSException, SQLException {
        Weather weather = Parser.parseTextMessage(textMessage);
        long ts = Instant.parse(weather.ts).toEpochMilli();
        Connection connection = connect(dir);
        String sql = "CREATE TABLE IF NOT EXISTS weather (ts integer, lat real, lon real, temperature real," +
                "pressure real, humidity real);";
        Statement statement = connection.createStatement();
        statement.execute(sql);
        sql = "INSERT INTO weather(ts,lat,lon,temperature,pressure,humidity) VALUES(?,?,?,?,?,?)";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setLong(1, ts);
        preparedStatement.setFloat(2, weather.location.lat);
        preparedStatement.setFloat(3, weather.location.lon);
        preparedStatement.setFloat(4, weather.temp);
        preparedStatement.setFloat(5, weather.pressure);
        preparedStatement.setFloat(6, weather.humidity);
        preparedStatement.executeUpdate();
    }

    public static ResultSet readFromDatabase(String dir) throws SQLException {
        Connection connection = connect(dir);
        String sql = "SELECT * FROM weather";
        Statement statement = connection.createStatement();
        return statement.executeQuery(sql);
    }

    public static void writeToCsv(String dir) throws SQLException, IOException {
        ResultSet resultSet = readFromDatabase(dir);
        File f = new File(dir + "weather.csv");
        if (!f.exists()){
            f.createNewFile();
        }
        FileWriter fileWriter = new FileWriter(dir + "weather.csv");
        while (resultSet.next()){
            fileWriter.write(resultSet.getLong("ts") + ";" +
                    resultSet.getFloat("lat") + ";" +
                    resultSet.getFloat("lon") + ";" +
                    resultSet.getFloat("temperature") + ";" +
                    resultSet.getFloat("pressure") + ";" +
                    resultSet.getFloat("humidity") + "\n");
        }
        fileWriter.close();
    }
}