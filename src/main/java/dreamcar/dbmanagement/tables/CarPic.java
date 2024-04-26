package dreamcar.dbmanagement.tables;

/**
 *  A car_pic tábla rekordjait reprezentálja
 *
 * @param id typCarId, ami azonosítja az autó típust
 * @param favCarId a fav_car táblához tartozó azonosító
 * @param imgName a kép elérésiútja
 */
public record CarPic(String id, String favCarId, String imgName) {}
