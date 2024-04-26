package dreamcar.dbmanagement;

import dreamcar.dbmanagement.tables.CarBrand;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * A car_brand táblát kezelő osztály
 */
public class CarBrandManager extends DatabaseManager {

    /**
     * Csatlakozik a MySQL szerverhez
     *
     * @param connection MySQL kapcsolat
     */
    public CarBrandManager(Connection connection) {
        super(connection, "car_brand");
    }

    /**
     * Új rekordot vesz fel a car_brand táblába
     *
     * @param carBrand táblához tartozó rekord osztály
     * @return visszajelzi, hogy sikerült-e a művelet
     */
    public boolean addCarBrand(CarBrand carBrand) {
        if (getCarBrandIds().contains(carBrand.id())){
            return false;
        }
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

    /**
     * A car_brand tábla id oszlopát adja vissza
     *
     * @return car_brand id-k listája
     */
    public ArrayList<String> getCarBrandIds() {
        return getCarBrands().stream()
                .map(CarBrand::id)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * car_brand name oszlopát adja vissza
     *
     * @return car_brand-ben szereplő márkák listája
     */
    public ArrayList<String> getCarBrandNames() {
        return getCarBrands().stream()
                .map(CarBrand::name)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Visszaadja az össze CarBrand objektumot ami a táblában szerepel
     *
     * @return car_brand táblából származó CarBrand lista
     */
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

    /**
     * Törli a car_brand táblából az adott CarBrand példányt és a car_type táblából
     * minden olyan típust, aminél szerepel.
     *
     * @param carBrand törlendő CarBrand objektum
     */
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
