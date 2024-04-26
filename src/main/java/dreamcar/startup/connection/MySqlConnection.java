package dreamcar.startup.connection;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * A MySql kapcsolatért felelős osztály
 */
public class MySqlConnection {

    private static String username;
    private static String password;
    private static String mysqlIp;
    private static String mysqlPort;
    private static String database;

    private static Connection connection;

    /**
     * Beolvassa a WEB-INF-en belül található konfigurációs állományt ami alapján
     * csatlakozik a MySql szerverhez
     */
    public static void connectToServer() {
        try {
            /*
             * A ../webapps/ és /WEB-INF/... közöti résznek meg kell egyeznie a .war fájl nevével.
             * (azaz, verzióváltásnál át kell írni...)
             *
             */
            File config = new File("../webapps/DreamCar-1.0/WEB-INF/properties.conf");

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
            connection = DriverManager.getConnection(
                    String.format("jdbc:mysql://%s:%s/%s", mysqlIp, mysqlPort, database )
                    , username
                    , password
            );

        } catch (RuntimeException | IOException | SQLException | ClassNotFoundException e) {
            System.out.println("connectToServer:\n"+e);
        }

    }

    /**
     * Elérhetővé teszi a létrejött kapcsolatot.
     *
     * @return MySql adatbázis kapcsolat
     */
    public static Connection getConnection() {
        return connection;
    }

    /**
     * Lezárja a kapcsolatot.
     */
    public static void terminateConnection() {
        try {
            connection.close();
        } catch (SQLException e) {
            System.out.println("terminateConnection:\n"+e);
        }
    }

}
