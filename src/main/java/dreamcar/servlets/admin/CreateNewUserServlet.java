package dreamcar.servlets.admin;

import dreamcar.dbmanagement.UserTableManager;
import dreamcar.dbmanagement.tables.User;
import dreamcar.servlets.ResponseComponents;
import dreamcar.startup.connection.MySqlConnection;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Optional;

/*
    # # # # # # # # # # # # # # # # # # # # # # # # # # # # #
    # _____________________MEGJEGYZÉS:_____________________ #
    # Utólag vettem észre, hogy az adminnak is tudnia kell  #
    # felhasználót létrehoznia, így ez egy gyors másolata   #
    # a regisztrációs servletnek kisebb módosításokkal.     #
    # # # # # # # # # # # # # # # # # # # # # # # # # # # # #
 */

/**
 * Admin felületről történő új felhasználó felvételét biztosító servlet osztály.
 */
@WebServlet("/newuser")
public class CreateNewUserServlet extends HttpServlet {

    private static final String REG_FORM = """
                    <div class="w-100 bg-dark p-2 text-light d-flex justify-content-start">
                            <button class="btn btn-secondary justify-content-center"  type="button" style="font-size: 1.5vh;"  onclick="location.href = 'admin'">
                                Vissza
                            </button>
                    </div>
                    <div class="d-flex justify-content-center">
                        <div class="col-sm-5 col-xs-7 m-3" style="min-width: 40vh;">
                            <div class="card h-100 m-3  pt-3 d-flex flex-column flex-box justify-content-between rounded-4 shadow bg-light text-dark">
                                <h2 class="card-header">Felhasználó felvétel</h2>
                                <div class="card-body h-100 d-flex flex-column justify-content-between">
                                    <form id="regisztralas" method="post">
                                        <p class="card-text">Minden mező kitöltése kötelező!</p><br>
                                        <label for="name">Teljes neve:</label><br>
                                        <input class="rounded-3 border border-primary" type="text" name="name" placeholder="Charlie Firpo"> <br><br>
                
                                        <label for="username">Felhasználónév:</label><br>
                                        <input class="rounded-3 border border-primary" type="text" name="username" placeholder="pl: kicsi_pisztacia72"><br>
                                        <p class="text-danger" %s >A felhasználónév már foglalt...</p>
                                        <p class="text-danger" %s >A felhasználónév tartalma az abc kis és nagy betűi, illetve számok lehetnek az alábbi karakterekkel kombinálva: ['_', '-'], hossza pedig legalább 3, max 15 karakter.</p>
                                        <br>
                
                                        <label for="password2">Jelszó:</label><br>
                                        <input class="rounded-3 border border-primary" type="password" name="password1" placeholder="********"> <br>
                                        <p class="%s">Min 8 karakter legyen és tartalmazzon, kis és nagybetűt, számot és speciális karaktert!</p>
                                        <br>
                
                                        <label for="password2">Jelszó még egyszer:</label><br>
                                        <input class="rounded-3 border border-primary" type="password" name="password2" placeholder="********"> <br>
                                        <p class="text-danger" %s >Hm... Ez a két jelszó nem stimmel...</p><br><br>
                                        <label for="password2">Admin felhasználó:</label><br>
                                        <input type="checkbox" name="admin" style="height: 1.7vh; width: 1.7vh;"><br>
                                        <button type="submit" class="btn btn-info rounded-4" style="font-size: 2vh"
                                            form="regisztralas">Hozzáadás!</button>
                                        <p class="text-danger" %s >Tölts ki minden mezőt, hogy folytatni tudjuk!</p>
                                    </form>
                                </div>
                            </div>
                        </div>
                    </div>
                """;

    /**
     * Megvizsgálja a jelszót, hogy megfelel-e az alábbi követelményeknek:
     *  - minimum 8 karakter
     *  - tartalmazzon legalább egy kis és egy nagy betűt
     *  - tartalmazzon legalább egy számot
     *  - tartalmazzon legalább egy speciális karaktert
     *
     * @param password vizsgálandó jelszó
     * @return megfelelés eredménye
     */
    private boolean checkPasswordIsValid(String password) {
        return password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()\\-_=+{};:,.<>?])(?=.{8,})\\S+$");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        doPost(req, resp);
    }

    /**
     * Megvizsgálja a felhasználónevet, hogy megfelel-e a követelményeknek.
     *
     * @param username vizsgálandó felhasználónév
     * @return megfelelés eredménye
     */
    private boolean checkUsernameIsValid(String username) {
        return username.matches("^[a-zA-Z0-9_-]{3,15}$");
    }

    /**
     * Ellenőrzi, hogy az adott felhasználónév szerepel-e már a user táblában.
     *
     * @param username elenőrizendő felhasználónév
     * @return visszaadja, hogy van-e már ilyen felhasználónév a user táblában
     */
    private boolean checkUsernameIsExist(String username) {
        String hashedUsername = DigestUtils.sha256Hex(username);
        UserTableManager utm = new UserTableManager(MySqlConnection.getConnection());
        return utm.getUsers().stream()
                .map(User::username)
                .anyMatch(hashedUsername::equals);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        ResponseComponents.setResponseHeader(response);

        UserTableManager utm = new UserTableManager(MySqlConnection.getConnection());
        String adminname = Optional.ofNullable((String) request.getSession().getAttribute("admin")).orElse("");


        try {
            if (adminname.isEmpty()) {
                response.sendRedirect("adminlogin");
            } else if (utm.getAdmins().stream().map(User::username).noneMatch(adminname::equals)) {
                response.sendRedirect("adminlogin");
            }

            String reservedUsername = "hidden";
            String badUsername = "hidden";
            String badPassword = "text-info";
            String differentPasswords = "hidden";
            String emptyFields = "hidden";
            boolean everythingOk = true;

            String username = Optional.ofNullable(request.getParameter("username")).orElse("");
            String fullname = Optional.ofNullable(request.getParameter("name")).orElse("");
            String password1 = Optional.ofNullable(request.getParameter("password1")).orElse("");
            String password2 = Optional.ofNullable(request.getParameter("password2")).orElse("");

            PrintWriter writer = response.getWriter();
            writer.println(ResponseComponents.getHeader("Registration"));

            if (username.isEmpty() && fullname.isEmpty() && password1.isEmpty() && password2.isEmpty()) {
                everythingOk = false;
            } else if (username.isEmpty() || fullname.isEmpty() || password1.isEmpty() || password2.isEmpty()) {
                emptyFields = "";
                everythingOk = false;
            } else if (!checkUsernameIsValid(username)) {
                badUsername = "";
                everythingOk = false;
            } else if (checkUsernameIsExist(username)) {
                reservedUsername = "";
                everythingOk = false;
            } else if (!checkPasswordIsValid(password1)) {
                badPassword = "text-danger";
                everythingOk = false;
            } else if (!password1.equals(password2)) {
                differentPasswords = "";
                everythingOk = false;
            }

            if (everythingOk) {
                String hashedUsername = DigestUtils.sha256Hex(username);
                String hashedPassword = DigestUtils.sha256Hex(password1);
                boolean isAdmin = request.getParameter("admin") != null;
                utm.addUser(new User(hashedUsername, hashedPassword, isAdmin, fullname, true));
                response.sendRedirect("admin");
            }

            writer.println(String.format(REG_FORM, reservedUsername, badUsername, badPassword, differentPasswords, emptyFields));
            writer.println(ResponseComponents.getFooter());

            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

