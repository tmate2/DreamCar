package dreamcar.dbmanagement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

/**
 * Adatbázis tábláit kezelő abasztrakt osztály
 */
public abstract class DatabaseManager {

    protected Connection connection;
    protected final String TABLE;
    protected final static String DELETE_QUERY = "DELETE FROM %s WHERE %s = ?";
    protected final static String UPDATE_QUERY = "UPDATE %s SET %s = ? WHERE %s = ?";

    /**
     * Csatlakozik a MySQL adatbázishoz és meghatározza a kezelendő táblát
     *
     * @param connection MySQL adatbázishoz való csatlakozás
     * @param table alosztály által kezelt tábla neve
     */
    protected DatabaseManager(Connection connection, String table) {
        this.connection = connection;
        this.TABLE = table;
    }

    /**
     * Vissza ad egy INSERT INTO mintát
     *
     * @param table a tábla neve, ahol a parancsot végrehajtjuk
     * @param valuesNumber beszúrandó értékek száma
     * @return INSERT INTO [table] VALUES ([? ,] * valuesNumber)
     */
    protected String getInsertIntoTableQuery(String table, int valuesNumber) {
        String[] marks = new String[valuesNumber];
        Arrays.fill(marks, "?");
        return String.format("INSERT INTO %s VALUES(%s)", table, String.join(",",marks));
    }

    /**
     * Egy "SELECT [columNames...] FROM [table]" lekérés mintája
     *
     * @param table a tábla neve, ahol a lekérést végrehajtjuk
     * @param columnName lekérést tartalmazó oszlop(ok) neve(i)
     * @return a parancs végrehajtásának eredményhalmaza
     */
    protected ResultSet getColumnsFromTable(String table, String... columnName) {
        String sqlQuery = String.format("SELECT %s FROM %s", String.join(",", columnName), table);
        ResultSet rs = null;
        try {
            PreparedStatement preparedStatement = this.connection.prepareStatement(sqlQuery);
            rs = preparedStatement.executeQuery();
        } catch (SQLException e) {
            System.out.println("getColumnFromTable:\n" + e);
        }
        return rs;
    }

}
