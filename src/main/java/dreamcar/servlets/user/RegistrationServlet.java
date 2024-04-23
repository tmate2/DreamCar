package dreamcar.servlets.user;

import dreamcar.dbmanagement.UserTableManager;
import dreamcar.dbmanagement.tables.User;
import dreamcar.servlets.ResponseComponents;
import dreamcar.startup.connection.MySqlConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Optional;

@WebServlet("/registration")
public class RegistrationServlet extends HttpServlet {

    private static final String LOGIN_FORM = """
                    <div class="d-flex justify-content-center">
                        <div class="col-sm-5 col-xs-7 m-3" style="min-width: 40vh;">
                            <div class="card h-100 m-3  pt-3 d-flex flex-column flex-box justify-content-between rounded-4 shadow bg-light text-dark">
                                <h2 class="card-header">Regisztráció</h2>
                                <div class="card-body h-100 d-flex flex-column justify-content-between">
                                    <form id="regisztralas" method="post">
                                        <p class="card-text">Szia!<br>Ahhoz, hogy csatlakozni tudj hozzánk, kérlek tölts ki minden mezőt.</p><br>
                                        <label for="name">Mi a teljes neved?</label><br>
                                        <input class="rounded-3 border border-primary" type="text" name="name" placeholder="Charlie Firpo"> <br><br>
                
                                        <label for="username">Felhasználónév:</label><br>
                                        <input class="rounded-3 border border-primary" type="text" name="username" placeholder="pl: kicsi_pisztacia72"><br>
                                        <p class="text-danger" %s >Sajnos ez a felhasználónév már foglalt...</p>
                                        <p class="text-danger" %s >A felhasználónév tartalma az abc kis és nagy betűi, illetve számok lehetnek az alábbi karakterekkel kombinálva: ['_', '-'], hossza pedig legalább 3, max 15 karakter.</p>
                                        <br>
                
                                        <label for="password2">Jelszó:</label><br>
                                        <input class="rounded-3 border border-primary" type="password" name="password1" placeholder="********"> <br><br>
                
                                        <label for="password2">Jelszó még egyszer:</label><br>
                                        <input class="rounded-3 border border-primary" type="password" name="password2" placeholder="********"> <br>
                                        <p class="text-danger" %s >Hm... Ez a két jelszó nem stimmel...</p><br><br>
                                        <button type="submit" class="btn btn-info rounded-4" style="font-size: 2vh"
                                            form="regisztralas">Csatlakozom!</button>
                                        <p class="text-danger" %s >Tölts ki minden mezőt, hogy folytatni tudjuk!</p>
                                    </form>
                                </div>
                            </div>
                        </div>
                    </div>
                """;

    @Override
    public void init() throws ServletException {
        //TODO
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        doPost(req, resp);
    }

    private boolean checkUsernameIsValid(String username) {
        return username.matches("^[a-zA-Z0-9_-]{3,15}$");
    }

    private boolean checkUsernameIsExist(String username) {
        String hashedUsername = DigestUtils.sha256Hex(username);
        UserTableManager utm = new UserTableManager(MySqlConnection.getConnection());
        return utm.getUsers().stream()
                .map(User::username)
                .anyMatch(hashedUsername::equalsIgnoreCase);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        try {
            if (ResponseComponents.checkUserInHeader(request)) {
                response.sendRedirect(request.getRequestURI().replace("/registration", "/home"));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String reservedUsername = "hidden";
        String badUsername = "hidden";
        String differentPasswords = "hidden";
        String emptyFields = "hidden";
        boolean everythingOk = true;

        String username = Optional.ofNullable(request.getParameter("username")).orElse("");
        String fullname = Optional.ofNullable(request.getParameter("name")).orElse("");
        String password1 = Optional.ofNullable(request.getParameter("password1")).orElse("");
        String password2 = Optional.ofNullable(request.getParameter("password2")).orElse("");

        try {
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
            } else if (!password1.equals(password2)) {
                differentPasswords = "";
                everythingOk = false;
            }
            if (everythingOk) {
                UserTableManager utm = new UserTableManager(MySqlConnection.getConnection());
                String hashedUsername = DigestUtils.sha256Hex(username);
                String hashedPassword = DigestUtils.sha256Hex(password1);
                utm.addUser(new User(hashedUsername, hashedPassword, false, fullname, true));
                response.sendRedirect(request.getRequestURI().replace("/registration", "/login"));
            }
            writer.println(String.format(LOGIN_FORM, reservedUsername, badUsername, differentPasswords, emptyFields));
            writer.println(ResponseComponents.getFooter());

            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void destroy() {
        super.destroy();
    }

}
