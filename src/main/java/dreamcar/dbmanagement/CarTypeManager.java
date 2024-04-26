package dreamcar.dbmanagement;

import dreamcar.dbmanagement.tables.CarBrand;
import dreamcar.dbmanagement.tables.CarType;
import dreamcar.dbmanagement.tables.FavCar;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * A car_type táblát kezelő osztály.
 */
public class CarTypeManager extends DatabaseManager {

    /**
     * Csatlakozást biztosít a MySQL szerverhez.
     *
     * @param connection MySQL kapcsolat
     */
    public CarTypeManager(Connection connection) {
        super(connection, "car_type");
    }

    /**
     * Új CarType rekordot vesz fel a car_type táblába.
     *
     * @param carType táblához tartozó rekord példány
     * @return visszajelzi, hogy sikerült-e a művelet
     */
    public boolean addCarType(CarType carType) {
        if (getCarTypesIds().contains(carType.id())) {
            return false;
        }
        String sqlQuery = getInsertIntoTableQuery(TABLE, 3);
        try {
            PreparedStatement pst = super.connection.prepareStatement(sqlQuery);
            pst.setString(1, carType.id());
            pst.setString(2, carType.carBrandId());
            pst.setString(3, carType.name());
            pst.execute();
        } catch (SQLException e) {
            System.out.println("addCarType"+e);
            return false;
        }
        return true;
    }

    /**
     * A car_type tábla id oszlopát adja vissza.
     *
     * @return car_type id-k listája
     */
    public ArrayList<String> getCarTypesIds() {
        return getCarTypes().stream()
                .map(CarType::id)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Visszaadja az össze CarType objektumot ami a táblában szerepel.
     *
     * @return car_type táblából származó CarType lista
     */
    public ArrayList<CarType> getCarTypes() {
        ArrayList<CarType> carTypes = null;
        try {
            ResultSet rs = getColumnsFromTable(TABLE, "*");
            carTypes = new ArrayList<>();
            while (rs.next()) {
                carTypes.add(new CarType(rs.getString("id")
                        , rs.getString("car_brand")
                        , rs.getString("name")
                        ));
            }
        } catch (SQLException e) {
            System.out.println("getCarTypes"+e);
        }
        return carTypes;
    }

    private void delete(String where, String what) {
        String sqlQuery = String.format(DELETE_QUERY, TABLE, where);
        try {
            PreparedStatement pst = super.connection.prepareStatement(sqlQuery);
            pst.setString(1, what);
            pst.execute();
        } catch (SQLException e) {
            System.out.println("delete"+e);
        }
    }

    /**
     * Törli az adott CarType példányt a car_type táblából és a fav_car táblából is
     * töröl minden olyan rekordot, ahol szerepel.
     *
     * @param carType törlendő CarType rekord
     */
    public void deleteCarType(CarType carType) {
        delete("id", carType.id());
        deleteCarTypesFromFavCar(carType);
    }

    /**
     * Törli az adott CarBrand-hez tartozó car_type rekordokat.
     *
     * @param carBrand törlendő típusok márkái
     */
    public void deleteCarBrandsTypes(CarBrand carBrand) {
        this.getCarTypes().stream()
                .filter(carType -> carType.carBrandId().equals(carBrand.id()))
                .toList()
                .forEach(this::deleteCarType);
        delete("car_brand", carBrand.id());
    }

    /**
     * Törli a megadott CarType példányokat a fav_car táblából.
     *
     * @param carType törlendő típus
     */
    public void deleteCarTypesFromFavCar(CarType carType) {
        FavCarTableManager ftm = new FavCarTableManager(connection);
        for (FavCar favCar : ftm.getFavCars()) {
            if (favCar.carTypeId().equals(carType.id())) {
                ftm.deleteFavCar(favCar);
            }
        }
    }

}
