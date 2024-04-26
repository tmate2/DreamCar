package dreamcar.dbmanagement.tables;

/**
 * A user tábla rekordjait reprezentálja
 *
 * @param username SHA256 hashelt érték a felhasználó által megadott username-ből
 * @param password SHA256 hashelt érték a felhasználó által megadott jelszóból
 * @param isAdmin admin jogosultságok elérése
 * @param name felhasználó által megadott név
 * @param isActive fiók állapota
 */
public record User(String username, String password, boolean isAdmin, String name, boolean isActive) {}
