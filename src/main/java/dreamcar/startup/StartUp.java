package dreamcar.startup;

import dreamcar.dbmanagement.UserTableManager;
import dreamcar.startup.connection.ConnectionFailedException;
import dreamcar.startup.connection.MySqlConnection;
import dreamcar.dbmanagement.tables.User;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import org.apache.commons.codec.digest.DigestUtils;

public class StartUp implements ServletContextListener {

    private boolean connectionFailed = false;

    private void connectionCheck() {
        try {
            MySqlConnection.connectToServer();
        } catch (ConnectionFailedException e) {
            connectionFailed = true;
            throw new RuntimeException(e);
            //TODO: leállítani a futást
        }
    }

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

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (!connectionFailed) {
            MySqlConnection.terminateConnection();
        }
    }

}
