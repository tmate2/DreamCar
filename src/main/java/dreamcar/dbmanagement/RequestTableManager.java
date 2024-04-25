package dreamcar.dbmanagement;

import dreamcar.dbmanagement.tables.UserRequest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class RequestTableManager extends DatabaseManager {

    public RequestTableManager(Connection connection) {
        super(connection, "request");
    }

    public void addRequest(UserRequest request) {
        String sqlQuery = getInsertIntoTableQuery(TABLE, 2);
        try {
            PreparedStatement pst = connection.prepareStatement(sqlQuery);
            pst.setString(1, request.request());
            pst.setString(2, request.username());
            pst.execute();
        } catch (SQLException e) {
            System.out.println(""+e);
        }
    }

    public ArrayList<UserRequest> getRequests() {
        ArrayList<UserRequest> requests = null;
        try {
            ResultSet rs = getColumnsFromTable(TABLE, "*");
            requests = new ArrayList<>();
            while (rs.next()) {
                requests.add(new UserRequest(rs.getString("request"), rs.getString("username")));
            }
        } catch (SQLException e) {
            System.out.println(""+e);
        }
        return requests;
    }

    public void deleteRequest(String request) {
        String sqlQuery = String.format(DELETE_QUERY, TABLE, "request");
        try {
            PreparedStatement pst = connection.prepareStatement(sqlQuery);
            pst.setString(1, request);
            pst.execute();
        } catch (SQLException e) {
            System.out.println(""+e);
        }
    }

}
