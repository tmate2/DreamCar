package dreamcar.dbmanagement;

import dreamcar.dbmanagement.tables.CarBrand;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class CarBrandManager extends DatabaseManager {

    public CarBrandManager(Connection connection) {
        super(connection, "car_brand");
    }

    public boolean addCarBrand(CarBrand carBrand) {
        if (getCarBrandIds().contains(carBrand.id())){}
        String sqlQuery = getInsertIntoTableQuery(TABLE, 2);
        try {
            PreparedStatement pst = super.connection.prepareStatement(sqlQuery);
            pst.setString(1, carBrand.id());
            pst.setString(2, carBrand.name());
            pst.execute();
        } catch (SQLException e) {
            System.out.println("addCarBrand"+e);
            return false;
        }
        return true;
    }

    public ArrayList<String> getCarBrandIds() {
        return getCarBrands().stream()
                .map(CarBrand::id)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public ArrayList<String> getCarBrandNames() {
        return getCarBrands().stream()
                .map(CarBrand::name)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public ArrayList<CarBrand> getCarBrands() {
        ArrayList<CarBrand> carBrands = null;
        try {
            ResultSet rs = getColumnsFromTable(TABLE, "*");
            carBrands = new ArrayList<>();
            while (rs.next()) {
                carBrands.add(new CarBrand(rs.getString("id"), rs.getString("name")));
            }
        } catch (SQLException e) {
            System.out.println("getCarBrands"+e);
        }
        return carBrands;
    }

    public void deleteCarBrand(CarBrand carBrand) {
        new CarTypeManager(connection).deleteCarBrandsTypes(carBrand);

        String sqlQuery = String.format(DELETE_QUERY, TABLE, "id");
        try {
            PreparedStatement pst = super.connection.prepareStatement(sqlQuery);
            pst.setString(1, carBrand.id());
            pst.execute();
        } catch (SQLException e) {
            System.out.println(""+e);
        }
    }

}
