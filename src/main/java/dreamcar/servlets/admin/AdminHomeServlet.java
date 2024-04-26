package dreamcar.servlets.admin;

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
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Az admin felhaszálók kezelőfelületét biztosító servlet osztály.
 */
@WebServlet("/admin")
public class AdminHomeServlet extends HttpServlet {

    private final String PAGE_HEADER = """
                    <div class="w-100 bg-dark p-2 text-light d-flex justify-content-center">
                        <div class="dropdown-center">
                            <button class="btn btn-secondary dropdown-toggle justify-content-center"  type="button" data-bs-toggle="dropdown" aria-expanded="false" style="font-size: 1.5vh;">
                            %s
                            </button>
                            <ul class="dropdown-menu" style="font-size: 1.5vh;">
                                <li><input type="button" class="dropdown-item" value="Kijelentkezés" onclick="location.href = 'logout'"></li>
                            </ul>
                        </div>
                    </div>
            """;

    private final String QUERY_TABLE = """
                 <form id="queryform" class="d-flex flex-column" method="post">
                    <div class="d-flex justify-content-center h-100 w-100 p-3">
                        <div class="col-sm-5 col-xs-7 w-100 p-3 table-responsive" style="min-width: 40vh;">
                            <h1>Kérések</h1>
                            <table class="table table-striped table-bordered">
                                <thead>
                                    <tr>
                                      <th scope="col">Kérés tartalma</th>
                                      <th scope="col">Felhasználónév</th>
                                      <th scope="col">Megjelölés</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    %s
                                </tbody>
                            </table>
                        </div>
                    </div>
                    <div class="d-flex justify-content-around m-3">
                        <button type="submit" class="btn btn-danger m-2 rounded-2" name="deleterequest" form="queryform" style="font-size: 2vh">Kijelöltek törlése</button>
                        <button type="submit" class="btn btn-light m-2 rounded-2" name="addnewbrand" form="queryform" style="font-size: 2vh">Márkák kezelése</button>
                        <button type="submit" class="btn btn-light m-2 rounded-2" name="addnewtype" form="queryform" style="font-size: 2vh">Típusok kezelése</button>
                    </div>
                    <div class="d-flex justify-content-around m-3">
                        <button type="submit" class="btn btn-danger m-2 rounded-2" name="deleteallrequest" form="queryform" style="font-size: 2vh">Összes kérés törlése</button>
                    </div>
                 </form>
            """;

    private final String USER_TABLE = """
                 <form id="userform" class="d-flex flex-column" style="background-color: silver;" method="post">
                    <div class="d-flex justify-content-center h-100 w-100 p-3">
                        <div class="col-sm-5 col-xs-7 w-100 p-3 table-responsive" style="min-width: 40vh;">
                            <h1>Felhasználók</h1>
                            <table class="table table-striped table-bordered">
                                <thead>
                                    <tr>
                                      <th scope="col">Felhasználónév</th>
                                      <th scope="col">Típus</th>
                                      <th scope="col">Név</th>
                                      <th scope="col">Státusz</th>
                                      <th scope="col">Megjelölés</th>
                                    </tr>
                                  </thead>
                                  <tbody>
                                    %s
                                </tbody>
                            </table>
                        </div>
                    </div>
                    <div class="d-flex justify-content-around m-3">
                        <button type="submit" class="btn btn-danger m-2 rounded-2" name="deleteselectedusers" form="userform" style="font-size: 2vh">Kijelöltek törlése</button>
                        <button type="submit" class="btn btn-light m-2 rounded-2" name="changeusertypes" form="userform" style="font-size: 2vh">Kijelöltek típusának változtatása</button>
                        <button type="submit" class="btn btn-light m-2 rounded-2" name="changeuserstatus" form="userform" style="font-size: 2vh">Kijelöltek státuszának változtatása</button>
                    </div>
                    <div class="d-flex justify-content-around m-3">
                        <button type="submit" class="btn btn-danger m-2 rounded-2" name="deletelockedusers" form="userform" style="font-size: 2vh">Összes zárolt felhasználó törlése</button>
                        <button type="button" class="btn btn-light m-2 rounded-2" style="font-size: 2vh" onclick="location.href = 'newuser'">Felhasználó hozzáadása</button>
                    </div>
                 </form>
            """;

    /**
     * A query tábla tartalma alapján legenerálja a megjelenítendő táblázat törzsét.
     *
     * @return egy táblázat törzse a query tábla rekordjaival
     */
    private String createQueryTable() {
        RequestTableManager rtm = new RequestTableManager(MySqlConnection.getConnection());
        ArrayList<UserRequest> userRequests = rtm.getRequests();
        if (userRequests.isEmpty()) {
            return """
                                            <tr>
                                                <td colspan="3">Nincs beérkező kérés</td>
                                            </tr>
                    """;
        }

        String rowSample = """
                                         <tr>
                                            <td>%s</td>
                                            <td>%s</td>
                                            <td><input type="checkbox" name="rchck-%s" style="height: 1.7vh; width: 1.7vh;"></td>
                                        </tr>
                """;
        ArrayList<String> rows = new ArrayList<>();
        for (UserRequest request : userRequests) {
            rows.add(String.format(rowSample, request.request(), request.username(), request.request()));
        }
        return String.join("\n", rows);
    }

    /**
     * A user tábla tartalma alapján legenerálja a megjelenítendő táblázat törzsét.
     *
     * @return egy táblázat törzse a user tábla rekordjaival
     */
    private String createUserTable(String currentUser) {
        UserTableManager utm = new UserTableManager(MySqlConnection.getConnection());
        String rowSample = """
                                        <tr>
                                            <td>%s</td>
                                            <td>%s</td>
                                            <td>%s</td>
                                            <td>%s</td>
                                            <td><input %s type="checkbox" name="uchck-%s" style="height: 1.7vh; width: 1.7vh;"></td>
                                        </tr>
                """;
        ArrayList<String> rows = new ArrayList<>();
        for (User user : utm.getUsers()) {
            rows.add(String.format(rowSample
                    , user.username()
                    , user.isAdmin() ? "Admin" : "User"
                    , user.name()
                    , user.isActive() ? "Aktív" : "Zárolt"
                    , user.username().equals(currentUser) ? "disabled" : ""
                    , user.username()
            ));
        }
        return String.join("\n", rows);
    }

    /**
     * Az oldalon megtalálható gombok funkcióit biztosítja.
     *
     * @param request
     * @param response
     * @throws IOException
     */
    private void handelButtons(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (request.getParameter("deleterequest") != null) {
            ArrayList<String> userRequests = request.getParameterMap().keySet().stream()
                    .filter(r -> r.startsWith("rchck-"))
                    .map(r -> r.substring(6))
                    .collect(Collectors.toCollection(ArrayList::new));

            RequestTableManager rtm = new RequestTableManager(MySqlConnection.getConnection());
            rtm.getRequests().stream()
                    .filter(r -> userRequests.contains(r.request()))
                    .forEach(r -> rtm.deleteRequest(r.request()));
        } else if (request.getParameter("addnewbrand") != null) {
            response.sendRedirect(request.getRequestURI().replace("/admin", "/addbrand"));
        } else if (request.getParameter("addnewtype") != null) {
            response.sendRedirect(request.getRequestURI().replace("/admin", "/addtype"));
        } else if (request.getParameter("deleteallrequest") != null) {
            RequestTableManager rtm = new RequestTableManager(MySqlConnection.getConnection());
            rtm.getRequests().forEach(r -> rtm.deleteRequest(r.request()));
        } else if (request.getParameter("deleteselectedusers") != null) {
            ArrayList<String> users = request.getParameterMap().keySet().stream()
                    .filter(r -> r.startsWith("uchck-"))
                    .map(r -> r.substring(6))
                    .collect(Collectors.toCollection(ArrayList::new));

            UserTableManager utm = new UserTableManager(MySqlConnection.getConnection());
            users.forEach(user -> utm.deleteUser(utm.getUserByUsername(user)));
        } else if (request.getParameter("changeusertypes") != null) {
            ArrayList<String> users = request.getParameterMap().keySet().stream()
                    .filter(r -> r.startsWith("uchck-"))
                    .map(r -> r.substring(6))
                    .collect(Collectors.toCollection(ArrayList::new));

            UserTableManager utm = new UserTableManager(MySqlConnection.getConnection());
            users.forEach(user -> utm.changeAdminStatus(utm.getUserByUsername(user)));
        } else if (request.getParameter("changeuserstatus") != null) {
            ArrayList<String> users = request.getParameterMap().keySet().stream()
                    .filter(r -> r.startsWith("uchck-"))
                    .map(r -> r.substring(6))
                    .collect(Collectors.toCollection(ArrayList::new));

            UserTableManager utm = new UserTableManager(MySqlConnection.getConnection());
            users.forEach(user -> utm.changeActivityStatus(utm.getUserByUsername(user)));
        } else if (request.getParameter("deletelockedusers") != null) {
            UserTableManager utm = new UserTableManager(MySqlConnection.getConnection());
            utm.getUsers().stream()
                    .filter(user -> !user.isActive())
                    .forEach(utm::deleteUser);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp){
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        ResponseComponents.setResponseHeader(response);

        UserTableManager utm = new UserTableManager(MySqlConnection.getConnection());
        String username = Optional.ofNullable((String) request.getSession().getAttribute("admin")).orElse("");

        try {
            if (username.isEmpty()) {
                response.sendRedirect("adminlogin");
            } else if (utm.getAdmins().stream().map(User::username).noneMatch(username::equals)) {
                response.sendRedirect("adminlogin");
            }
            User actualAdmin = utm.getUserByUsername(username);

            handelButtons(request, response);

            PrintWriter writer = response.getWriter();
            writer.println(ResponseComponents.getHeader("DreamCar Admin"));
            writer.println(String.format(PAGE_HEADER, actualAdmin.name()));
            writer.println(String.format(QUERY_TABLE, createQueryTable()));
            writer.println(String.format(USER_TABLE, createUserTable(actualAdmin.username())));
            writer.println(ResponseComponents.getFooter());
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
