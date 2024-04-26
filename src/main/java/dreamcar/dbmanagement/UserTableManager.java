package dreamcar.dbmanagement;

import dreamcar.dbmanagement.tables.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * A user táblát kezelő osztály.
 */
public class UserTableManager extends DatabaseManager {

    /**
     * Csatlakozást biztosít a MySql szerverhez.
     *
     * @param connection MySql kapcsoalt
     */
    public UserTableManager(Connection connection) {
        super(connection, "user");
    }

    /**
     * Új User rekordot vesz fel a user táblába.
     *
     * @param user táblához tartozó rekord példány
     * @return visszajelzi, hogy sikerült-e a művelet
     */
    public boolean addUser(User user) {
        if (getUsernames().contains(user.username())) {
            return false;
        }
        String sqlQuery = getInsertIntoTableQuery(TABLE, 5);
        try {
            PreparedStatement pst = super.connection.prepareStatement(sqlQuery);
            pst.setString(1, user.username());
            pst.setString(2, user.password());
            pst.setBoolean(3, user.isAdmin());
            pst.setString(4, user.name());
            pst.setBoolean(5, user.isActive());
            pst.execute();
        } catch (SQLException e) {
            System.out.println("addUser:\n"+e);
            return false;
        }
        return true;
    }

    /**
     * Negálja az adott User rekord is_admin értékét.
     *
     * @param user módosítandó User példány
     */
    public void changeAdminStatus(User user) {
        String sqlQuery = String.format(UPDATE_QUERY, TABLE, "is_admin", "username");
        try {
            PreparedStatement pst = super.connection.prepareStatement(sqlQuery);
            pst.setBoolean(1, !user.isAdmin());
            pst.setString(2, user.username());
            pst.execute();
        } catch (SQLException e) {
            System.out.println("changeAdminStatus:\n"+e);
        }
    }

    /**
     * Negálja az adott User rekord is_active értékét.
     *
     * @param user módosítandó User példány
     */
    public void changeActivityStatus(User user) {
        String sqlQuery = String.format(UPDATE_QUERY, TABLE, "is_active", "username");
        try {
            PreparedStatement pst = super.connection.prepareStatement(sqlQuery);
            pst.setBoolean(1, !user.isActive());
            pst.setString(2, user.username());
            pst.execute();
        } catch (SQLException e) {
            System.out.println("changeActivityStatus:\n"+e);
        }
    }

    /**
     * Módosítja az adott User rekord password mezőjét.
     *
     * @param user módosítandó User példány
     * @param newPassword érték amire módosúl
     */
    public void changePassword(User user, String newPassword) {
        String sqlQuery = String.format(UPDATE_QUERY, TABLE, "password", "username");
        try {
            PreparedStatement pst = super.connection.prepareStatement(sqlQuery);
            pst.setString(1, newPassword);
            pst.setString(2, user.username());
            pst.execute();
        } catch (SQLException e) {
            System.out.println("changePassword:\n"+e);
        }
    }

    /**
     * Törli az adott User rekordot a user táblából és a hozzá tartozó rekordokat
     * a car_pic táblából.
     *
     * @param user törlendő User rekord példány
     */
    public void deleteUser(User user) {
        new FavCarTableManager(connection).deleteUsersFavCars(user);
        //TODO: TÖRÖLNI A HOZZÁ TARTOZÓ REKORDOKAT A CAR_PIC TÁBLÁBÓL
        String sqlQuery = String.format(DELETE_QUERY, TABLE, "username");
        try {
            PreparedStatement pst = super.connection.prepareStatement(sqlQuery);
            pst.setString(1, user.username());
            pst.execute();
        } catch (SQLException e) {
            System.out.println("deleteUser:\n"+e);
        }
    }

    /**
     * Visszaadja az összes User objektumot ami a táblában szerepel.
     *
     * @return user táblából származó User lista
     */
    public ArrayList<User> getUsers() {
        ArrayList<User> users = null;
        try {
            ResultSet rs = getColumnsFromTable(TABLE, "*");
            users = new ArrayList<>();
            while (rs.next()) {
                users.add(new User(
                        rs.getString("username")
                        , rs.getString("password")
                        , rs.getBoolean("is_admin")
                        , rs.getString("name")
                        , rs.getBoolean("is_active")
                ));
            }
        } catch (SQLException e) {
            System.out.println("getUsers:\n"+e);
        }
        return users;
    }

    /**
     * Visszadja a User táblából a username oszlopot.
     *
     * @return user tábla username oszlopából készült lista
     */
    private ArrayList<String> getUsernames() {
        ArrayList<String> username = null;
        try {
            ResultSet rs = getColumnsFromTable(TABLE, "username");
            username = new ArrayList<>();
            while (rs.next()) {
                username.add(rs.getString("username"));
            }
        } catch (SQLException e) {
            System.out.println("getUsernames:\n"+e);
        }
        return username;
    }

    /**
     * Vissza adja a is_admin = true értékkel rendelkező User rekordokat a user táblából.
     *
     * @return admin jogosultságú User rekordok listája
     */
    public ArrayList<User> getAdmins() {
        return getUsers().stream()
                .filter(user -> user.isAdmin() && user.isActive())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Vissza adja a user táblóból, az adott usernamehez tartozó teljes User rekordot.
     *
     * @param username lekérni kívánt rekord username értéke
     * @return usernamehez tartozó User rekord
     */
    public User getUserByUsername(String username) {
        return getUsers().stream()
                .filter(u -> u.username().equals(username))
                .findFirst().get();
    }

}
