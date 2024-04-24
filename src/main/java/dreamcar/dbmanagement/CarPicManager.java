package dreamcar.dbmanagement;

import dreamcar.dbmanagement.tables.CarPic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class CarPicManager extends DatabaseManager {

    public CarPicManager(Connection connection) {
        super(connection, "car_pic");
    }

    public void addCarPic(CarPic carPic) {
        if (getCarPicsIds().contains(carPic.id())) {
            changeImg(carPic, carPic.imgName());
        }
        String sqlQuery = getInsertIntoTableQuery(TABLE, 3);
        try {
            PreparedStatement pst = super.connection.prepareStatement(sqlQuery);
            pst.setString(1, carPic.id());
            pst.setString(2, carPic.favCarId());
            pst.setString(3, carPic.imgName());
            pst.execute();
        } catch (SQLException e) {
            System.out.println(""+e);
        }
    }

    public void changeImg(CarPic carPic, String newImgName) {
        String sqlQuery = String.format(UPDATE_QUERY, TABLE, "img", "id");
        try {
            PreparedStatement pst = super.connection.prepareStatement(sqlQuery);
            pst.setString(1, newImgName);
            pst.setString(2, carPic.id());
            pst.execute();
        } catch (SQLException e) {
            System.out.println(""+e);
        }
    }

    public ArrayList<String> getCarPicsIds() {
        return getCarPics().stream()
                .map(CarPic::id)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public ArrayList<CarPic> getCarPics() {
        ArrayList<CarPic> carPics = null;
        try {
            ResultSet rs = getColumnsFromTable(TABLE, "*");
            carPics = new ArrayList<>();
            while (rs.next()) {
                carPics.add(new CarPic(
                        rs.getString("id")
                        , rs.getString("fav_car_id")
                        , rs.getString("img")
                ));
            }
        } catch (SQLException e) {
            System.out.println(""+e);
        }
        return carPics;
    }

    public void deleteCarPicById(String id) {
        String sqlQuery = String.format(DELETE_QUERY, TABLE, "id");
        try {
            PreparedStatement pst = super.connection.prepareStatement(sqlQuery);
            pst.setString(1, id);
            pst.execute();
        } catch (SQLException e) {
            System.out.println(""+e);
        }
    }

    public void deleteCarPic(CarPic carPic) {
        deleteCarPicById(carPic.id());
    }

}
