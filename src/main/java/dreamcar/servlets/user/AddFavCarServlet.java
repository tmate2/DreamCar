package dreamcar.servlets.user;

import dreamcar.dbmanagement.*;
import dreamcar.dbmanagement.tables.CarPic;
import dreamcar.dbmanagement.tables.CarType;
import dreamcar.dbmanagement.tables.FavCar;
import dreamcar.dbmanagement.tables.User;
import dreamcar.servlets.ResponseComponents;
import dreamcar.startup.connection.MySqlConnection;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.*;
import java.util.*;

/**
 * A fav_car táblához tartozó műveleteket biztosító servlet osztály.
 */
@WebServlet("/addcar")
public class AddFavCarServlet extends HttpServlet {

    private final String BODY = """
                    <div class="d-flex justify-content-center m-0 p-0" style="background-color: silver;">
                        <div class="col-sm-5 col-xs-7 m-3" style="min-width: 40vh;">
                            <div class="card h-100 m-3  pt-3 d-flex flex-column flex-box justify-content-between rounded-4 shadow bg-light text-dark">
                                <h2 class="card-header">Hozzáadása</h2>
                                <div class="card-body h-100 d-flex flex-column justify-content-between">
                                    <form id="cartypehozzaadas" method="post" onchange="changeName()">
                                        <label>Típus:</label><br>
                                        <div class="dropdown-center">
                                            <button id="radio-btn-btn" class="btn btn-light dropdown-toggle justify-content-center"  type="button" data-bs-toggle="dropdown" aria-expanded="false" style="font-size: 1.5vh;">
                                                Típus választás
                                            </button>
                                            <ul class="dropdown-menu" style="font-size: 1.5vh;">
                                                %s
                                            </ul>
                                        </div>
                                        <br>
                                        <label for="year">Évjárat:</label><br>
                                        <input  type="number" min="1900" max="2035" step="1" value="2024" class="rounded-2" name="year"> <br><br>
                                        <label for="color">Szín:</label><br>
                                        <input  type="text" class="rounded-2" name="color" placeholder="pl: Ezüst"> <br><br>
                                        <label for="fuel">Üzemanyag:</label><br>
                                        <input  type="text" class="rounded-2" name="fuel" placeholder="pl: Benzin"> <br><br>
                                        <label for="pic">Kép feltöltés (opcionális):</label><br>
                                        <input disable type="file" class="rounded-2" name="pic"> <br><br>
                                        <p class="text-danger" %s>Ez már szerepel a listádon.</p>
                                        <p class="text-danger" %s>Adj meg minden szükséges adatot!</p>
                                        <button type="submit" class="btn btn-info m-2 rounded-2" style="font-size: 2vh" form="cartypehozzaadas" name="adding">Hozzáadása</button><br>
                                        <button type="button" class="btn btn-outline-info mb-2 rounded-2" style="font-size: 2vh" onclick="location.href = 'request'">Hiányzó autó kérvényezése</button><br>
                                        <button type="button" class="btn btn-danger rounded-2" style="font-size: 2vh" onclick="location.href = 'home'">Vissza</button>
                                    </form>
                                </div>
                            </div>
                        </div>
                    </div>
                    <script>
                        function changeName() {
                            const radioBtns = document.getElementsByName("type");
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
     * Legenerálja egy dropdown menűbe a car_type táblában lévő rekordok alapján, hogy miket lehet
     * választani és hozzáadni a fav_car táblához.
     *
     * @return car_type rekordjait tartalmazó dropdown menű törzse
     */
    private String createCarList() {
        CarTypeManager ctm = new CarTypeManager(MySqlConnection.getConnection());
        ArrayList<CarType> carTypes = ctm.getCarTypes();
        if (carTypes.isEmpty()) {
            return """
                                                                <li>Jelenleg nem tudsz kocsit választani, nézz vissza később!</li>
                """;
        }
        CarBrandManager cbm = new CarBrandManager(MySqlConnection.getConnection());
        Map<String, String> carNames = new HashMap<>();

        for (CarType carType : carTypes) {
            String actualCarBrandName = cbm.getCarBrands().stream()
                    .filter(carBrand -> carBrand.id().equals(carType.carBrandId()))
                    .findFirst()
                    .get().name();

            carNames.put(
                    actualCarBrandName + " " + carType.name()
                    , carType.id()
            );
        }

        String rowSample = """
                                                                <li><input class="m-1" type="radio" id="%s" name="type" value="%s" style="height: 1.6vh; width: 1.6vh;"><label for="%s" style="font-size: 1.7vh;">%s</label><br></li>
                """;
        ArrayList<String> rows = new ArrayList<>();
        for (String car : carNames.keySet().stream().sorted(String::compareTo).toList()) {
            rows.add(String.format(rowSample
                    , car
                    , carNames.get(car)
                    , car
                    , car
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
        String username = Optional.ofNullable((String) request.getSession().getAttribute("user")).orElse("");
        UserTableManager utm = new UserTableManager(MySqlConnection.getConnection());

        try {
            if (username.isEmpty()) {
                response.sendRedirect("login");
            } else if (utm.getUsers().stream().filter(User::isActive).map(User::username).noneMatch(username::equals)) {
                response.sendRedirect("login");
            }

            String emptyFields = "hidden";
            String isEnable = "hidden";

            if (request.getParameter("adding") != null) {
                String type = Optional.ofNullable(request.getParameter("type")).orElse("");
                String year = Optional.ofNullable(request.getParameter("year")).orElse("");
                String color = Optional.ofNullable(request.getParameter("color")).orElse("");
                String fuel = Optional.ofNullable(request.getParameter("fuel")).orElse("");

                if (!type.isEmpty() && !year.isEmpty() && !color.isEmpty() && !fuel.isEmpty()) {
                    String newFavCarId = DigestUtils.sha256Hex(username + type + year);
                    FavCarTableManager fctm = new FavCarTableManager(MySqlConnection.getConnection());
                    if (!fctm.getUserFavCars(username).stream()
                            .map(FavCar::id)
                            .toList()
                            .contains(newFavCarId)) {
                        fctm.addFavCar(new FavCar(
                                newFavCarId
                                , type
                                , username
                                , Short.parseShort(year)
                                , color
                                , fuel
                        ));
                        CarPicManager cpm = new CarPicManager(MySqlConnection.getConnection());
                        cpm.addCarPic(new CarPic(type, newFavCarId,"cars/default.jpg"));
                        response.sendRedirect("home");
                    }
                    isEnable = "";
                }
                emptyFields = "";
            }

            PrintWriter writer = response.getWriter();
            writer.println(ResponseComponents.getHeader("Add Favorite Car"));
            writer.println(String.format(BODY, createCarList(), isEnable, emptyFields));
            writer.println(ResponseComponents.getFooter());
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
