package dreamcar.startup.connection;

public class ConnectionFailedException extends Exception {
    @Override
    public String getMessage() {
        return "Sikertelen csatlakozás az SQL szerverhez!";
    }
}
