package dreamcar.servlets.user;

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

/**
 * Jelszóváltoztatást biztosító servlet osztály.
 */
@WebServlet("/changepassword")
public class ChangePasswordServlet extends HttpServlet {

    private final String BODY = """
                    <div class="d-flex justify-content-center">
                        <div class="col-sm-5 col-xs-7 m-3" style="min-width: 40vh;">
                            <div class="card h-100 m-3  pt-3 d-flex flex-column flex-box justify-content-between rounded-4 shadow bg-light text-dark">
                                <h2 class="card-header">Jelszó változtatás</h2>
                                <div class="card-body h-100 d-flex flex-column justify-content-between">
                                    <form id="jelszocsere" method="post" action="changepassword">
                                        <h6 class="card-text">A jelszavad megváltoztatásához először add meg a régit, utána pedig az új jelszót.</h6>
                                        <br>
                                        <label for="password">Jelszó:</label><br>
                                        <input type="password" class="rounded-2" name="password" placeholder="********"> <br>
                                        <p class="text-danger" %s>Hibás jelszó</p><br>
                                        <label for="newpassword1">Új jelszó:</label><br>
                                        <input type="password" class="rounded-2" name="newpassword1" placeholder="********"> <br><br>
                                        <label for="password">Új jelszó megint:</label><br>
                                        <input type="password" class="rounded-2" name="newpassword2" placeholder="********"> <br>
                                        <p class="%s">Min 8 karakter legyen és tartalmazzon, kis és nagybetűt, számot és speciális karaktert!</p>
                                        <br>
                                        <p class="text-danger" %s>A két új jelszó nem egyezik</p><br>
                                        <button type="submit" class="btn btn-info m-3 rounded-4" style="font-size: 2vh" form="jelszocsere">Csere</button><br>
                                        <button type="button" class="btn btn-danger rounded-4" style="font-size: 2vh" onclick="location.href = 'home'">Mégse</button>
                                    </form>
                                </div>
                            </div>
                        </div>
                    </div>
            """;


    /**
     * Ellenőrzi a fehasználónévhez tartozó (régi) jelszót
     * @param username felhasználót azonosító felasználónév
     * @param password felhasználóhoz tartozó (régi) jelszó
     * @return Ellenőrzés eredménye
     */
    private boolean checkPassword(String username, String password) {
        UserTableManager utm = new UserTableManager(MySqlConnection.getConnection());
        return utm.getUserByUsername(username).password().equals(password);
    }

    /**
     * Megvizsgálja az új jelszót, hogy megfelel-e az alábbi követelményeknek:
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

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        ResponseComponents.setResponseHeader(response);

        String password = Optional.ofNullable(request.getParameter("password")).orElse("");
        String newPassword1 = Optional.ofNullable(request.getParameter("newpassword1")).orElse("");
        String newPassword2 = Optional.ofNullable(request.getParameter("newpassword2")).orElse("");

        String validPassword = "text-info";
        String badPassword = "hidden";
        String differentPassword = "hidden";

        UserTableManager utm = new UserTableManager(MySqlConnection.getConnection());
        String username = Optional.ofNullable((String) request.getSession().getAttribute("user")).orElse("");

        try {
            if (username.isEmpty()) {
                response.sendRedirect("login");
            } else if (utm.getUsers().stream().filter(User::isActive).map(User::username).noneMatch(username::equals)) {
                response.sendRedirect("login");
            }

            if (!password.isEmpty() && !newPassword1.isEmpty() && !newPassword2.isEmpty()) {
                String hashedPassword = DigestUtils.sha256Hex(password);
                if (checkPassword(username, hashedPassword)) {
                    if (!checkPasswordIsValid(newPassword1)) {
                        validPassword = "text-danger";
                    } else if (newPassword1.equals(newPassword2)) {
                        utm.changePassword(
                                utm.getUserByUsername(username)
                                , DigestUtils.sha256Hex(newPassword1)
                        );
                        response.sendRedirect("home");
                    } else {
                        differentPassword = "";
                    }
                } else {
                    badPassword = "";
                }
            }

            PrintWriter writer = response.getWriter();
            writer.println(ResponseComponents.getHeader("Change Password"));
            writer.println(String.format(BODY, badPassword, validPassword, differentPassword));
            writer.println(ResponseComponents.getFooter());
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
