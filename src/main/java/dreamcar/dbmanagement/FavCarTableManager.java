package dreamcar.dbmanagement;

import dreamcar.dbmanagement.tables.FavCar;
import dreamcar.dbmanagement.tables.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class FavCarTableManager extends DatabaseManager {

    public FavCarTableManager(Connection connection) {
        super(connection, "fav_car");
    }

    public boolean addFavCar(FavCar favCar) {
        if (getFavCarIds().contains(favCar.id())) {
            return false;
        }

        String sqlQuery = getInsertIntoTableQuery(TABLE, 6);

        try {
            PreparedStatement pts = super.connection.prepareStatement(sqlQuery);
            pts.setString(1, favCar.id());
            pts.setString(2, favCar.carTypeId());
            pts.setString(3, favCar.userId());
            pts.setShort(4, favCar.year());
            pts.setString(5, favCar.color());
            pts.setString(6, favCar.fuel());
            pts.execute();
        } catch (SQLException e) {
            System.out.println("addFavCar:\n"+e);
            return false;
        }
        return true;
    }

    public void deleteFavCar(FavCar favCar) {
        new CarPicManager(connection).deleteCarPicById(favCar.id());

        String sqlQuery = String.format(DELETE_QUERY, TABLE, "id");
        try {
            PreparedStatement pst = super.connection.prepareStatement(sqlQuery);
            pst.setString(1, favCar.id());
            pst.execute();
        } catch (SQLException e) {
            System.out.println("deleteFavCar:\n"+e);
        }
    }

    public void deleteUsersFavCars(User user) {
        //Megjegyzés: ez a car_pic táblából is törli ezeket
        getUserFavCars(user.username())
                .forEach(this::deleteFavCar);

        try {
            String sqlQuery = String.format(DELETE_QUERY, TABLE, "user_id");
            PreparedStatement pst = connection.prepareStatement(sqlQuery);
            pst.setString(1, user.username());
            pst.execute();
        } catch (SQLException e) {
            System.out.println(""+e);
        }
    }

    public ArrayList<FavCar> getFavCars() {
        ArrayList<FavCar> favCars = null;
        try {
            ResultSet rs = getColumnsFromTable(TABLE, "*");
            favCars = new ArrayList<>();
            while (rs.next()) {
                favCars.add(new FavCar(
                        rs.getString("id")
                        , rs.getString("car_type_id")
                        , rs.getString("user_id")
                        , rs.getShort("year")
                        , rs.getString("color")
                        , rs.getString("fuel")
                ));
            }
        } catch (SQLException e) {
            System.out.println("getFavCars:\n"+e);
        }
        return favCars;
    }

    public ArrayList<FavCar> getUserFavCars(String userId) {
        ArrayList<FavCar> favCars = null;
        String sqlQuery = String.format("SELECT * FROM %s WHERE user_id = ?", TABLE);
        try {
            PreparedStatement pst = super.connection.prepareStatement(sqlQuery);
            pst.setString(1, userId);
            ResultSet rs = pst.executeQuery();
            favCars = new ArrayList<>();
            while (rs.next()) {
                favCars.add(new FavCar(
                        rs.getString("id")
                        , rs.getString("car_type_id")
                        , rs.getString("user_id")
                        , rs.getShort("year")
                        , rs.getString("color")
                        , rs.getString("fuel")
                ));
            }
        } catch (SQLException e) {
            System.out.println("getFavCars:\n"+e);
        }
        return favCars;
    }

    private ArrayList<String> getFavCarIds() {
        ArrayList<String> favCarIds = null;
        try {
            ResultSet rs = getColumnsFromTable(TABLE, "id");
            favCarIds = new ArrayList<>();
            while (rs.next()) {
                favCarIds.add(rs.getString("id"));
            }
        } catch (SQLException e) {
            System.out.println("getFavCarIds:\n"+e);
        }
        return favCarIds;
    }

}