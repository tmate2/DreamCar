package dreamcar.dbmanagement.tables;

public record User(String username, String password, boolean isAdmin, String name, boolean isActive) {}
