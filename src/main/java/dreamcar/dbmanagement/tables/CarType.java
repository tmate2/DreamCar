package dreamcar.dbmanagement.tables;

/**
 *  A car_type tábla rekordjait reprezentálja
 *
 * @param id a name paraméter SHA256 hashelt értéke
 * @param carBrandId a márkát azonosítja a car_brand táblában
 * @param name a típus neve
 */
public record CarType(String id, String carBrandId, String name) {}
