package dreamcar.dbmanagement;

import dreamcar.dbmanagement.tables.CarPic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * A car_brand táblát kezeli
 */
public class CarPicManager extends DatabaseManager {

    /**
     * Csatlakozik a MySQL szerverhez
     *
     * @param connection MySQL kapcsolat
     */
    public CarPicManager(Connection connection) {
        super(connection, "car_pic");
    }

    /**
     * Új rekordot vesz fel a car_pic táblába, ha már van ilyen rekord akkor felülírja a kép elérésiútját
     *
     * @param carPic táblához tartozó rekord osztály
     */
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

    /**
     * Mődosítja az adott példány img rekordját a car_pic táblában
     *
     * @param carPic táblához tartozó rekord osztály
     * @param newImgName az új kép elérésiútja
     */
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

    /**
     * car_pic id oszlopát adja vissza
     *
     * @return car_pic id-k listája
     */
    public ArrayList<String> getCarPicsIds() {
        return getCarPics().stream()
                .map(CarPic::id)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Visszaadja az össze CarPic objektumot ami a táblában szerepel
     *
     * @return car_pic táblából származó CarPic lista
     */
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

    /**
     * Törli a car_pic táblából a megadott fav_car_id-t tartalmazó rekordot
     *
     * @param id CarPic rekord fav_car_id azonosítója
     */
    public void deleteCarPicByFavCarId(String id) {
        String sqlQuery = String.format(DELETE_QUERY, TABLE, "fav_car_id");
        try {
            PreparedStatement pst = super.connection.prepareStatement(sqlQuery);
            pst.setString(1, id);
            pst.execute();
        } catch (SQLException e) {
            System.out.println(""+e);
        }
    }

    /**
     * Törli a car_pic táblából a a megadott példányt
     *
     * @param carPic törlendő CarPic példány
     */
    public void deleteCarPic(CarPic carPic) {
        deleteCarPicByFavCarId(carPic.favCarId());
    }

}
