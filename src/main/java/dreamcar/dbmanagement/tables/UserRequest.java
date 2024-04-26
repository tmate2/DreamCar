package dreamcar.dbmanagement.tables;

/**
 * A user tábla rekordjait reprezentálja
 *
 * @param request egy felhasználó által megfogalmazott kérés
 * @param username fehasználót azonosító username
 */
public record UserRequest(String request, String username) {}
