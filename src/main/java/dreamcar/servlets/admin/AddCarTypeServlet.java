package dreamcar.servlets.admin;

import dreamcar.dbmanagement.CarBrandManager;
import dreamcar.dbmanagement.CarTypeManager;
import dreamcar.dbmanagement.UserTableManager;
import dreamcar.dbmanagement.tables.CarBrand;
import dreamcar.dbmanagement.tables.CarType;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A car_type tábla kezelését segítő servlet osztály
 */
@WebServlet("/addtype")
public class AddCarTypeServlet extends HttpServlet {

    private final String TYPE_TABEL = """
                    <form id="typeform" method="post" class="m-3">
                        <div class="d-flex justify-content-center h-100 w-100 p-3">
                            <div class="col-sm-5 col-xs-7 w-100 p-3 table-responsive" style="min-width: 40vh;">
                                <h1>Típusok</h1>
                                <table class="table table-striped table-bordered">
                                    <thead>
                                        <tr>
                                            <th scope="col">Típus</th>
                                            <th scope="col">Márka</th>
                                            <th scope="col">Megjelölés</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        %s
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </form>
                    <div class="d-flex justify-content-around">
                        <button type="submit" class="btn btn-danger rounded-2" name="deletetypes" form="typeform" style="font-size: 2vh;">Kijelöltek törlése</button>
                    </div>
            """;

    private final String ADDING_FORM = """
                    <div class="d-flex justify-content-center m-0 p-0" style="background-color: silver;">
                        <div class="col-sm-5 col-xs-7 m-3" style="min-width: 40vh;">
                            <div class="card h-100 m-3  pt-3 d-flex flex-column flex-box justify-content-between rounded-4 shadow bg-light text-dark">
                                <h2 class="card-header">Hozzáadása</h2>
                                <div class="card-body h-100 d-flex flex-column justify-content-between">
                                    <form id="cartypehozzaadas" method="post" onchange="changeName()">
                                        <label>Márka:</label><br>
                                        <div class="dropdown-center">
                                            <button id="radio-btn-btn" class="btn btn-light dropdown-toggle justify-content-center"  type="button" data-bs-toggle="dropdown" aria-expanded="false" style="font-size: 1.5vh;">
                                                Márka választás
                                            </button>
                                            <ul class="dropdown-menu" style="font-size: 1.5vh;">
                                                %s
                                            </ul>
                                        </div>
                                        <label for="cartypename">Típus:</label><br>
                                        <input type="text" class="rounded-2" name="cartypename" placeholder="pl: Mustang"> <br>
                                        <p class="text-danger" %s>Ez a típus már szerepel az adatbázisban</p>
                                        <button type="submit" class="btn btn-info m-2 rounded-2" style="font-size: 2vh" form="cartypehozzaadas" name="addcartype">Hozzáadása</button><br>
                                        <button type="button" class="btn btn-danger rounded-2" style="font-size: 2vh" onclick="location.href = 'admin'">Vissza</button>
                                    </form>
                                </div>
                            </div>
                        </div>
                    </div>
                    <script>
                    function changeName() {
                        const radioBtns = document.getElementsByName("brandname");
                        let newName = "";
                        for (var radioBtn of radioBtns) {
                            if (radioBtn.checked) {
                                newName = radioBtn.id;
                            }
                        }
                        document.getElementById("radio-btn-btn").innerHTML = newName;
                    }
                    </script>
            """;

    /**
     * A car_type tábla tartalma alapján legenerálja a megjelenítendő táblázat törzsét.
     *
     * @return egy táblázat törzse a car_type tábla rekordjaival
     */
    private String createTypeTable() {
        CarTypeManager ctm = new CarTypeManager(MySqlConnection.getConnection());
        ArrayList<CarType> carTypes = ctm.getCarTypes();
        if (carTypes.isEmpty()) {
            return """
                                                <tr>
                                                    <td colspan="3">Jelenleg nincs még felvéve egy típus sem</td>
                                                </tr>
                    """;
        }
        String rowSample = """
                                            <tr>
                                                <td>%s</td>
                                                <td>%s</td>
                                                <td><input type="checkbox" name="ctchck-%s" style="height: 1.7vh; width: 1.7vh;"></td>
                                            </tr>
                """;
        ArrayList<String> rows = new ArrayList<>();
        CarBrandManager cbm = new CarBrandManager(MySqlConnection.getConnection());
        for (CarType carType : carTypes) {
            String carBrandName = cbm.getCarBrands().stream()
                    .filter(carBrand -> carBrand.id().equals(carType.carBrandId()))
                    .map(CarBrand::name)
                    .findFirst().get();
            rows.add(String.format(rowSample
                    , carType.name()
                    , carBrandName
                    , carType.id()
                    ));
        }
        return String.join("\n", rows);
    }

    /**
     * A car_brand tábla tartalma alapján egy dropdown menű választható részét generálja le.
     *
     * @return dropdown menűbe való választható elemek HTML blokkja
     */
    private String createDropdown() {
        CarBrandManager cbm = new CarBrandManager(MySqlConnection.getConnection());
        if (cbm.getCarBrands().isEmpty()) {
            return """
                                                        <li>Nincs egy márka se felvéve</li>
                    """;
        }

        String rowSimple = """
                                               <li><input class="m-1" type="radio" id="%s" name="brandname"  value="%s" style="height: 1.6vh; width: 1.6vh;"><label for="%s" style="font-size: 1.7vh;">%s</label><br></li>
            """;
        ArrayList<String> rows = new ArrayList<>();
        for (CarBrand carBrand : cbm.getCarBrands().stream().sorted(Comparator.comparing(CarBrand::name)).toList()) {
            rows.add(String.format(rowSimple
                    , carBrand.name()
                    , carBrand.id()
                    , carBrand.name()
                    , carBrand.name()
            ));
        }
        return String.join("\n", rows);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
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

            String hidden = "hidden";

            if (request.getParameter("deletetypes") != null) {
                ArrayList<String> typeIds = request.getParameterMap().keySet().stream()
                        .filter(r -> r.startsWith("ctchck-"))
                        .map(r -> r.substring(7))
                        .collect(Collectors.toCollection(ArrayList::new));

                CarTypeManager ctm = new CarTypeManager(MySqlConnection.getConnection());
                typeIds.forEach(id -> ctm.deleteCarType(
                        ctm.getCarTypes().stream()
                                .filter(brand -> brand.id().equals(id))
                                .findFirst().get()));
            } else if (request.getParameter("addcartype") != null
                    && request.getParameter("brandname") != null
                    && request.getParameter("cartypename") != null) {
                String newCarTypeName = request.getParameter("cartypename").toUpperCase();
                String carBrandId = request.getParameter("brandname");

                CarTypeManager ctm = new CarTypeManager(MySqlConnection.getConnection());
                String newCarTypeId = DigestUtils.sha256Hex(newCarTypeName + carBrandId);
                if (!ctm.getCarTypesIds().contains(newCarTypeId)) {
                    ctm.addCarType(new CarType(
                            newCarTypeId
                            , carBrandId
                            , newCarTypeName
                    ));
                    response.sendRedirect("addtype");
                }
                hidden = "";
            }

            PrintWriter writer = response.getWriter();
            writer.println(ResponseComponents.getHeader("Car Types"));
            writer.println(String.format(TYPE_TABEL, createTypeTable()));
            writer.println(String.format(ADDING_FORM, createDropdown(), hidden));
            writer.println(ResponseComponents.getFooter());
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
