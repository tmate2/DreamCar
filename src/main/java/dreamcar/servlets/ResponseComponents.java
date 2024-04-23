package dreamcar.servlets;

import dreamcar.dbmanagement.UserTableManager;
import dreamcar.dbmanagement.tables.User;
import dreamcar.startup.connection.MySqlConnection;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ResponseComponents {

    private static final String HEADER = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>%s</title>
                <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
                <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
                <link rel="stylesheet" href="style/style.css">
            </head>
            <body>
                <div id="container" class="container d-flex flex-column m-auto vh-100 p-0 text-center justify-content-between">
            """;

    private static final String LOGIN = """
                    <div class="d-flex justify-content-center">
                        <div class="col-sm-5 col-xs-7 m-3" style="min-width: 40vh;">
                            <div class="card h-100 m-3  pt-3 d-flex flex-column flex-box justify-content-between rounded-4 shadow bg-light text-dark">
                                <h2 class="card-header">Bejelentkezés</h2>
                                <div class="card-body h-100 d-flex flex-column justify-content-between">
                                    <form id="bejelentkezes" method="post" action="%s">
                                        <p class="card-text">Üdv újra!</p>
                                        <label for="username">Felhasználónév:</label><br>
                                        <input type="text" name="username" placeholder="pl: charlie_firpo"> <br><br>
                                        <label for="password">Jelszó:</label><br>
                                        <input type="password" name="password" placeholder="********"> <br><br>
                                        <p class="text-danger" %s>Helytelen felhasználónév vagy jelszó</p><br>
                                        <button type="submit" class="btn btn-info rounded-4" style="font-size: 2vh" form="bejelentkezes">Bejelentkezek</button>
                                    </form>
                                </div>
                            </div>
                        </div>
                    </div>
            """;

    private static final String FOOTER = """
                    <div id="footer-text" class="d-flex justify-content-between p-3 bg-dark text-light">
                        <div class="col-md-3">Semmilyen jog sincs fenntartva.</div>
                        <div class="col-md-2"><a href="admin" style="text-decoration: none; color: white">Admin</a></div>
                    </div>
                </div>
            </body>
            </html>
            """;


    public static String getHeader(String title) {
        return String.format(HEADER, title);
    }

    public static String getLogin(String servletName, boolean warning) {
        String hidde = warning ? "" : "hidden";
        return String.format(LOGIN, servletName, hidde);
    }

    public static String getFooter() {
        return FOOTER;
    }

    public static boolean checkUserInHeader(HttpServletRequest request) {
        String user = Optional.ofNullable(request.getParameter("user")).orElse("");
        UserTableManager utm = new UserTableManager(MySqlConnection.getConnection());
        Map<String, String> credentials = utm.getUsers().stream()
                .filter(User::isActive)
                .collect(Collectors.toMap(User::username, User::password));
        return credentials.containsKey(user);
    }


    public static void setResponseHeader(HttpServletResponse response) {
        // MINDENHOVA KELL AHOL BEJELENTKEZVE LEHET CSAK A USER
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1
        response.setHeader("Pragma", "no-cache"); // HTTP 1.0
        response.setHeader("Expires", "0"); // Proxy
    }

}
