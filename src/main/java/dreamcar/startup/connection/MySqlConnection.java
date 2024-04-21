package dreamcar.startup.connection;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class MySqlConnection {

    private static String username;
    private static String password;
    private static String mysqlIp;
    private static String mysqlPort;
    private static String database;

    private static Connection connection = null;

    public static void connectToServer() throws ConnectionFailedException {
        try {
            File config = new File("../webapps/DreamCar-1.0/WEB-INF/properties");

            if (config.exists() && config.canRead()) {
                Properties properties = new Properties();
                properties.load(new FileInputStream(config));
                mysqlIp = properties.getProperty("mysql_ip");
                mysqlPort = properties.getProperty("mysql_port");
                username = properties.getProperty("username");
                password = properties.getProperty("password");
                database = properties.getProperty("database");
            }

            Class.forName("com.mysql.jdbc.Driver");

            System.out.println("######################################################"+database);

            connection = DriverManager.getConnection(
                    String.format("jdbc:mysql://%s:%s/%s", mysqlIp, mysqlPort, database )
                    , username
                    , password
            );

        } catch (RuntimeException | IOException | SQLException | ClassNotFoundException e) {
            throw new ConnectionFailedException();
        }

        if (connection == null){
            throw new ConnectionFailedException();
        }
    }

    public static Connection getConnection() {
        return connection;
    }

    public static void terminateConnection() {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
