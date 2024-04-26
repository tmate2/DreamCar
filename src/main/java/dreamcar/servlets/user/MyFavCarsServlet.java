package dreamcar.servlets.user;

import dreamcar.dbmanagement.*;
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

/**
 * Adott felhasználóhoz tartozó fav_car és car_pic rekordjait képpel megjelenítő servlet osztály.
 */
@WebServlet("/mycars")
public class MyFavCarsServlet extends HttpServlet {

    private final String BODY = """
                    <div class="w-100 bg-dark p-2 text-light d-flex justify-content-start">
                        <button class="btn btn-secondary justify-content-center" type="button" style="font-size: 1.5vh;"
                            onclick="location.href = 'home'">
                            Vissza
                        </button>
                    </div>
                    <div class="d-flex flex-column justify-content-start" style="background-color:silver;">
                        %s
                    </div>
            """;

    private final String CARD_SAMPLE = """
                        <div class="d-flex justify-content-center">
                            <div class="col-sm-5 col-xs-7 m-3  w-100" style="max-width: 70vh;">
                                <div
                                    class="card h-100 m-3  pt-1 d-flex flex-column flex-box justify-content-between rounded-4 shadow bg-light text-dark">
                                    <p class="card-header">%s</p>
                                    <div class="card-body h-100 d-flex flex-column justify-content-between">
                                        <img src="%s">
                                    </div>
                                </div>
                            </div>
                        </div>
            """;

    /**
     * Megjelenítésül szolgáló kártya legenerálása.
     *
     * @param username felhasználót azonosító felhasználónév
     * @return car_pic és car_type rekord alapján generált kártyák
     */
    private String createCards(String username) {
        FavCarTableManager fctm = new FavCarTableManager(MySqlConnection.getConnection());
        ArrayList<FavCar> favCars = fctm.getUserFavCars(username);
        if (favCars.isEmpty()) {
            return """
                    <h1>Még nincs felvéve egy kedvenc autó sem...<h1>
                    """;
        }
        final String TITLE = "%s %s %s (%s)";
        ArrayList<String> cards = new ArrayList<>();
        for (FavCar favCar : favCars) {
            String year = String.valueOf(favCar.year());
            String carTypeId = favCar.carTypeId();
            String fuel = favCar.fuel();

            CarPicManager cpm = new CarPicManager(MySqlConnection.getConnection());
            String picPath = cpm.getCarPics().stream()
                    .filter(cp -> cp.favCarId().equals(favCar.id()))
                    .findFirst().get()
                    .imgName();

            CarTypeManager ctm = new CarTypeManager(MySqlConnection.getConnection());
            CarType carType = ctm.getCarTypes().stream()
                    .filter(ct -> ct.id().equals(carTypeId))
                    .findFirst().get();

            CarBrandManager cbm = new CarBrandManager(MySqlConnection.getConnection());
            String carBrandName = cbm.getCarBrands().stream()
                    .filter(cb -> cb.id().equals(carType.carBrandId()))
                    .findFirst().get().name();

            cards.add(
                    String.format(CARD_SAMPLE
                            , String.format(TITLE, year, carBrandName, carType.name(), fuel)
                            , picPath)
            );
        }
        return String.join("\n", cards);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        ResponseComponents.setResponseHeader(response);
        UserTableManager utm = new UserTableManager(MySqlConnection.getConnection());
        String username = (String) request.getSession().getAttribute("user");

        try {
            if (username.isEmpty()) {
                response.sendRedirect("login");
            } else if (utm.getAdmins().stream().map(User::username).noneMatch(username::equals)) {
                response.sendRedirect("login");
            }

            PrintWriter writer = response.getWriter();
            writer.println(ResponseComponents.getHeader("My Favorite Cars"));
            writer.println(String.format(BODY, createCards(username)));
            writer.println(ResponseComponents.getFooter());
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
