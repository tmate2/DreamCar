package dreamcar.servlets;

import dreamcar.dbmanagement.UserTableManager;
import dreamcar.dbmanagement.tables.User;
import dreamcar.startup.connection.MySqlConnection;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Optional;

/**
 * A válaszokban gyakran használt eszközöket tartalmazó osztály.
 */
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
                    <div class="w-100 bg-dark p-2 text-light d-flex justify-content-start">
                                <button class="btn btn-secondary justify-content-center"  type="button" style="font-size: 1.5vh;"  onclick="location.href = 'index.html'">
                                    Vissza
                                </button>
                    </div>
                    <div class="d-flex justify-content-center vh-100 h-100">
                        <div class="col-sm-5 col-xs-7 m-3" style="min-width: 40vh;">
                            <div class="card m-3  pt-3 d-flex flex-column flex-box justify-content-between rounded-4 shadow bg-light text-dark">
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
                        <div class="col-md-2"><a onclick="location.href = 'adminlogin'" style="text-decoration: none; color: white; cursor: pointer;">Admin</a></div>
                    </div>
                </div>
            </body>
            </html>
            """;


    /**
     * Egy HTML minta <head>...</head> és <body><div class="container...">... részét adja vissza.
     *
     * @param title Az oldal címe
     * @return Oldal címével ellátott HTML minta részlet
     */
    public static String getHeader(String title) {
        return String.format(HEADER, title);
    }

    /**
     * Egy egyszerű bejelnetkezési HTML minta törzse.
     *
     * @param servletName Feldolgozó szervlet URI paramétere
     * @param warning Hibás bejelnetkezési figyelmeztetés megjelenítése
     * @return Formázott bejelentkezési HTML törzs
     */
    public static String getLogin(String servletName, boolean warning) {
        String hidde = warning ? "" : "hidden";
        return String.format(LOGIN, servletName, hidde);
    }

    /**
     * Egy HTML minta lezáró részét adja vissza.
     * @return HTML minta lezáró része
     */
    public static String getFooter() {
        return FOOTER;
    }

    public static boolean checkUserInHeader(HttpServletRequest request) {
        String user = Optional.ofNullable((String) request.getSession().getAttribute("user")).orElse("");
        UserTableManager utm = new UserTableManager(MySqlConnection.getConnection());
        return utm.getUsers().stream()
                .filter(User::isActive)
                .map(User::username)
                .anyMatch(user::equals);
    }

    /**
     * Letiltja, hogy a böngésző cache-lje az adott választ.
     *
     * @param response
     */
    public static void setResponseHeader(HttpServletResponse response) {
        // MINDENHOVA KELL AHOL BEJELENTKEZVE LEHET CSAK A USER
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1
        response.setHeader("Pragma", "no-cache"); // HTTP 1.0
        response.setHeader("Expires", "0"); // Proxy
    }

}
