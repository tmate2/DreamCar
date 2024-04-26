package dreamcar.servlets.user;

import dreamcar.dbmanagement.RequestTableManager;
import dreamcar.dbmanagement.UserTableManager;
import dreamcar.dbmanagement.tables.User;
import dreamcar.dbmanagement.tables.UserRequest;
import dreamcar.servlets.ResponseComponents;
import dreamcar.startup.connection.MySqlConnection;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Optional;

/**
 * Felhasználók kérvényt leadó felület servlet osztálya.
 */
@WebServlet("/request")
public class SendRequestServlet extends HttpServlet {

    private final String REQUEST_FROM = """
                    <div class="w-100 bg-dark p-2 text-light d-flex justify-content-start">
                        <button class="btn btn-secondary justify-content-center"  type="button" style="font-size: 1.5vh;"  onclick="location.href = 'addcar'">
                            Vissza
                        </button>
                    </div>
                    <div class="d-flex justify-content-center vh-100 h-100">
                        <div class="col-sm-5 col-xs-7 m-3" style="min-width: 40vh;">
                            <div class="card m-3  pt-3 d-flex flex-column flex-box justify-content-between rounded-4 shadow bg-light text-dark">
                                <h2 class="card-header">Kérelem</h2>
                                <div class="card-body h-100 d-flex flex-column justify-content-between">
                                    <form id="requestform" method="post">
                                        <p class="card-text">Írja le, milyen autót hiányol az adatbázisunkból.</p>
                                        <label for="request">Autó neve:</label><br>
                                        <input type="text" name="request" placeholder="pl: Bugatti Chiron"> <br><br>
                                        <button type="submit" class="btn btn-info rounded-4" style="font-size: 2vh" form="requestform">Küldés</button>
                                    </form>
                                </div>
                            </div>
                        </div>
                    </div>
            """;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        ResponseComponents.setResponseHeader(response);
        String username = Optional.ofNullable((String) request.getSession().getAttribute("user")).orElse("");
        UserTableManager utm = new UserTableManager(MySqlConnection.getConnection());

        try {
            if (username.isEmpty()) {
                response.sendRedirect("login");
            } else if (utm.getUsers().stream().filter(User::isActive).map(User::username).noneMatch(username::equals)) {
                response.sendRedirect("login");
            }

            String userRequest = Optional.ofNullable(request.getParameter("request")).orElse("");

            if (!userRequest.isEmpty()) {
                RequestTableManager rtm = new RequestTableManager(MySqlConnection.getConnection());
                rtm.addRequest(new UserRequest(userRequest, username));
                response.sendRedirect("addcar");
            }

            PrintWriter writer = response.getWriter();
            writer.println(ResponseComponents.getHeader("Kérelem"));
            writer.println(REQUEST_FROM);
            writer.println(ResponseComponents.getFooter());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
