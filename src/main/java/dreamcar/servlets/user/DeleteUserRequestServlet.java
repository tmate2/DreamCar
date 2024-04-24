package dreamcar.servlets.user;

import dreamcar.dbmanagement.UserTableManager;
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

@WebServlet("/deleteuser")
public class DeleteUserRequestServlet extends HttpServlet {

    private final String DELETE = """
                    <div class="d-flex justify-content-center">
                        <div class="col-sm-5 col-xs-7 m-3" style="min-width: 40vh;">
                            <div class="card h-100 m-3  pt-3 d-flex flex-column flex-box justify-content-between rounded-4 shadow bg-light text-dark">
                                <h2 class="card-header">Fiók törlése</h2>
                                <div class="card-body h-100 d-flex flex-column justify-content-between">
                                    <form id="delete" method="post" >
                                        <p class="card-text">A törléshez írja be a jelszavát</p>
                                        <label for="password">Jelszó:</label><br>
                                        <input type="password" name="password" placeholder="********"> <br>
                                        <p class="text-danger" %s>Hibás jelszó</p>
                                        <br>
                                        <button type="submit" class="btn btn-danger rounded-4 w-100" style="font-size: 2vh" form="delete">Törlés</button>
                                        <br>
                                        <br>
                                        <button type="button" class="btn btn-info rounded-4 w-100" style="font-size: 2vh" onclick="location.href = 'home'">Mégse</button>
                                    </form>
                                </div>
                            </div>
                        </div>
                    </div>
            """;

    private boolean checkPassword(String username, String password) {
        UserTableManager utm = new UserTableManager(MySqlConnection.getConnection());
        return utm.getUserByUsername(username).password().equals(password);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        ResponseComponents.setResponseHeader(response);
        String password = Optional.ofNullable(request.getParameter("password")).orElse("");
        try {
            String username = (String) request.getSession().getAttribute("user");
            if (username.isEmpty()) {
                response.sendRedirect("login");
            } else if (!ResponseComponents.checkUserInHeader(request)) {
                response.sendRedirect("login");
            }
            String hidden = "hidden";
            if (!password.isEmpty()) {
                hidden = "";
                String hashedPassword = DigestUtils.sha256Hex(password);
                if (checkPassword(username, hashedPassword)) {
                    UserTableManager utm = new UserTableManager(MySqlConnection.getConnection());
                    utm.changeActivityStatus(utm.getUserByUsername(username));
                    request.getSession().invalidate();
                    response.sendRedirect(request.getRequestURI().replace("/deleteuser", "/"));
                }
            }
            PrintWriter writer = response.getWriter();
            writer.println(ResponseComponents.getHeader("Delete User"));
            writer.println(String.format(DELETE, hidden));
            writer.println(ResponseComponents.getFooter());
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
