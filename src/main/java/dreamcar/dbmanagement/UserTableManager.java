package dreamcar.dbmanagement;

import dreamcar.dbmanagement.tables.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class UserTableManager extends DatabaseManager {

    public UserTableManager(Connection connection) {
        super(connection, "user");
    }

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

    public void deleteUser(User user) {
        new FavCarTableManager(connection).deleteUsersFavCars(user);

        String sqlQuery = String.format(DELETE_QUERY, TABLE, "username");
        try {
            PreparedStatement pst = super.connection.prepareStatement(sqlQuery);
            pst.setString(1, user.username());
            pst.execute();
        } catch (SQLException e) {
            System.out.println("deleteUser:\n"+e);
        }
    }

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
    public ArrayList<User> getAdmins() {
        return getUsers().stream()
                .filter(user -> user.isAdmin() && user.isActive())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public User getUserByUsername(String username) {
        return getUsers().stream()
                .filter(u -> u.username().equals(username))
                .findFirst().get();
    }

}
