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

public class CarTypeManager extends DatabaseManager {

    public CarTypeManager(Connection connection) {
        super(connection, "car_type");
    }

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

    public ArrayList<String> getCarTypesIds() {
        return getCarTypes().stream()
                .map(CarType::id)
                .collect(Collectors.toCollection(ArrayList::new));
    }

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
            System.out.println(""+e);
        }
    }

    public void deleteCarType(CarType carType) {
        deleteCarTypesFromFavCar(carType);
        delete("id", carType.id());
    }

    public void deleteCarBrandsTypes(CarBrand carBrand) {
        delete("car_brand", carBrand.id());
    }

    public void deleteCarTypesFromFavCar(CarType carType) {
        FavCarTableManager ftm = new FavCarTableManager(connection);
        for (FavCar favCar : ftm.getFavCars()) {
            if (favCar.carTypeId().equals(carType.id())) {
                // Alkotói megjegyzés: ez törli majd a car_pic táblából is
                ftm.deleteFavCar(favCar);
            }
        }
    }

}
