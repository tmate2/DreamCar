package dreamcar.servlets.user;

import dreamcar.dbmanagement.CarBrandManager;
import dreamcar.dbmanagement.CarTypeManager;
import dreamcar.dbmanagement.FavCarTableManager;
import dreamcar.dbmanagement.UserTableManager;
import dreamcar.dbmanagement.tables.CarType;
import dreamcar.dbmanagement.tables.FavCar;
import dreamcar.dbmanagement.tables.User;
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

/**
 * Felhasználói főoldalt biztosító servlet osztály.
 */
@WebServlet("/home")
public class HomeServlet extends HttpServlet {

    private final String ROW_TEMP = """
                                <tr>
                                    <td>%s</td> <!-- MÁRKA -->
                                    <td>%s</td> <!-- TÍPUS -->
                                    <td>%s</td> <!-- ÉVJÁRAT -->
                                    <td>%s</td> <!-- SZÍN -->
                                </tr>
            """;

    private final String HOME_BODY = """
                <div class="w-100 bg-dark p-2 text-light d-flex justify-content-between">
                        <div><a href="addcar" class="text-light">Új kocsi hozzáadása</a></div>
                        <div><a href="mycars" class="text-light">Autóim</a></div>
                        <div class="dropdown-center">
                            <button class="btn btn-secondary dropdown-toggle justify-content-center"  type="button" data-bs-toggle="dropdown" aria-expanded="false" style="font-size: 1.5vh;">
                                %s
                            </button>
                            <ul class="dropdown-menu" style="font-size: 1.5vh;">
                                <li><input type="button" class="dropdown-item" value="Jelszó változtatás" onclick="location.href = 'changepassword'"></li>
                                <li><input type="button" class="dropdown-item text-danger" value="Fiók törlése" onclick="deleteRequest()"></li>
                                <li><hr class="dropdown-divider"></li>
                                <li><input type="button" class="dropdown-item" value="Kijelentkezés" onclick="location.href = 'logout'"></li>
                            </ul>
                        </div>
                    </div>
            
                    <div class="d-flex justify-content-center vh-100 w-100">
                        <div class="col-sm-5 col-xs-7 m-3 w-75" style="min-width: 40vh;">
                            <table table class="table table-striped table-bordered">
                                <thead>
                                    <tr>
                                      <th scope="col">Márka</th>
                                      <th scope="col">Típus</th>
                                      <th scope="col">Évjárat</th>
                                      <th scope="col">Szín</th>
                                    </tr>
                                  </thead>
                                  <tbody>
                                    %s
                                  </tbody>
                            </table>
                        </div>
                    </div>
                    <script src="js/userControl.js"></script>
            """;

    /**
     * Az oldal törzést generálja le a felhasználó adatai alapján.
     *
     * @param name felhasználó teljes neve, ami a felső sávban jelenik meg
     * @param tableContent felhasználóhoz tartozó fav_car rekordokat tartalmazó táblázat törzse
     * @return felület HTML törzse
     */
    private String getHomeBody(String name, String tableContent) {
        return String.format(HOME_BODY, name, tableContent);
    }

    /**
     * Felhasználóhoz tartozó fav_car rekordokból állít össze egy sort
     *
     * @param favCar sorban szerepelni fogó FavCar rekord
     * @return HTML tábláhatba tartozó sor
     */
    private String createRow(FavCar favCar) {
        CarTypeManager ctm = new CarTypeManager(MySqlConnection.getConnection());
        CarType carType = ctm.getCarTypes().stream()
                .filter(ct -> ct.id().equals(favCar.carTypeId()))
                .findFirst().orElse(null);

        CarBrandManager cbm = new CarBrandManager(MySqlConnection.getConnection());
        String brandName = cbm.getCarBrands().stream()
                .filter(carBrand -> carBrand.id().equals(carType.carBrandId()))
                .findFirst()
                .get().name();

        String type = carType.name();
        return String.format(ROW_TEMP, brandName, type, favCar.year(), favCar.color());
    }

    /**
     * Felhasználóhoz tartozó fav_car rekordokból csinálja meg a megjelenítő táblázat törzsét.
     *
     * @param username felhasználót azonosító felhasználónév
     * @return HTML táblázat törzse
     */
    private String createTableContent(String username) {
        FavCarTableManager ftm = new FavCarTableManager(MySqlConnection.getConnection());
        if (ftm.getUserFavCars(String.valueOf(username)).isEmpty()) {
            return """
                                    <tr>
                                        <td colspan="4">Jelenleg még nincs egy kedvenc autód sem felvéve...</td>
                                    </tr>
                """;
        }

        ArrayList<FavCar> userFavCars = ftm.getUserFavCars(username);
        ArrayList<String> tableRows = new ArrayList<>();
        for (FavCar favCar : userFavCars) {
            tableRows.add(createRow(favCar));
        }
        return String.join("\n", tableRows);
    }

    /**
     * Visszaadja a felhasználónévhez tartozó nevet.
     *
     * @param username felhasználót azonosító felhasználónév
     * @return felhasználónévhez tartozó név
     */
    private String getUserFullName(String username) {
        UserTableManager utm = new UserTableManager(MySqlConnection.getConnection());
        return utm.getUsers().stream()
                .filter(User::isActive)
                .filter(user -> user.username().equals(username))
                .findFirst().orElse(null)
                .name();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        ResponseComponents.setResponseHeader(response);

        try {
            String username = Optional.ofNullable((String) request.getSession().getAttribute("user")).orElse("");
            UserTableManager utm = new UserTableManager(MySqlConnection.getConnection());

            if (username.isEmpty()) {
                response.sendRedirect("login");
            } else if (utm.getUsers().stream().filter(User::isActive).map(User::username).noneMatch(username::equals)) {
                response.sendRedirect("login");
            }

            PrintWriter writer = response.getWriter();
            writer.println(ResponseComponents.getHeader("Home"));
            writer.println(getHomeBody(getUserFullName(username), createTableContent(username)));
            writer.println(ResponseComponents.getFooter());
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
