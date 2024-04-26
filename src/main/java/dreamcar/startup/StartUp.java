package dreamcar.startup;

import dreamcar.dbmanagement.UserTableManager;
import dreamcar.startup.connection.MySqlConnection;
import dreamcar.dbmanagement.tables.User;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * A webalkalmazás elindítását segító osztály.
 */
public class StartUp implements ServletContextListener {

    private boolean connectionFailed = false;

    /**
     * Létrehozza és ellenőrzi, hogy sikeresen létrejött-e a kapcsolat az MySQL szerverrel
     */
    private void connectionCheck() {
        MySqlConnection.connectToServer();

        connectionFailed = (MySqlConnection.getConnection() == null);
        //TODO: leállítani a futást
    }

    /**
     * A webalkalmazás indulásakor ellenőrzi, hogy létezik-e admin felhasználó. Ha nem
     * létrehoz egy alap admin felhasználót admin jelszóval.
     *
     * @param servletContextEvent
     */
    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        connectionCheck();
        UserTableManager utm = new UserTableManager(MySqlConnection.getConnection());
        if (utm.getAdmins().isEmpty()) {
            utm.addUser(new User(
                    DigestUtils.sha256Hex("admin")
                    , DigestUtils.sha256Hex("admin")
                    , true
                    , "Administrator"
                    , true));
        }
    }

    /**
     * A webalkalmazás leállításánál biztonságosan megszünteti a kapcsolatot a MySql szerverrel.
     *
     * @param sce
     */
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (!connectionFailed) {
            MySqlConnection.terminateConnection();
        }
    }

}
