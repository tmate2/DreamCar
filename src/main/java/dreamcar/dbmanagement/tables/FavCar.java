package dreamcar.dbmanagement.tables;

/**
 *  A car_brand tábla rekordjait reprezentálja
 *
 * @param id carTypeId + year konkatenáció SHA256-os hashelt értéke
 * @param carTypeId típust azonosítja a car_type táblában
 * @param userId username, tulajdonos felhasználót azonosítja a user táblában
 * @param year felhasználó által megadott, típushoz tartozó évszám
 * @param color felhasználó által megadott, típushoz tartozó szín
 * @param fuel felhasználó által megadott, típushoz tartozó üzemanyagtípus
 */
public record FavCar(String id, String carTypeId, String userId, short year, String color, String fuel) {}
