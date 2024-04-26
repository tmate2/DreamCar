package dreamcar.dbmanagement.tables;

/**
 * A car_brand tábla rekordjait reprezentálja
 *
 * @param id márkát azanosítja, name paraméter SHA256 hashelt értéke
 * @param name a márka neve
 */
public record CarBrand(String id, String name) {}
