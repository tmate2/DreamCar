package dreamcar.servlets.admin;

import dreamcar.dbmanagement.CarBrandManager;
import dreamcar.dbmanagement.RequestTableManager;
import dreamcar.dbmanagement.UserTableManager;
import dreamcar.dbmanagement.tables.CarBrand;
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
import java.util.Optional;
import java.util.stream.Collectors;

@WebServlet("/addbrand")
public class AddCarBrandServlet extends HttpServlet {

    private final String BRAND_TABLE = """
                    <form id="brandform" method="post">
                        <div class="d-flex justify-content-center h-100 w-100 p-3">
                            <div class="col-sm-5 col-xs-7 w-100 p-3 table-responsive" style="min-width: 40vh;">
                                <h1>Márkák</h1>
                                <table class="table table-striped table-bordered">
                                    <thead>
                                        <tr>
                                            <th scope="col">Név</th>
                                            <th scope="col">Megjelölés</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        %s
                                    </tbody>
                                </table>
                            </div>
                        </div>
                        <div class="d-flex justify-content-around">
                            <button type="submit" class="btn btn-danger m-2 rounded-2" name="deletebrands" form="brandform" style="font-size: 2vh;">Kijelöltek törlése</button>
                        </div>
                    </form>
            """;

    private final String ADDING_FORM = """
                    <div class="d-flex justify-content-center">
                        <div class="col-sm-5 col-xs-7 m-3" style="min-width: 40vh;">
                            <div class="card h-100 m-3  pt-3 d-flex flex-column flex-box justify-content-between rounded-4 shadow bg-light text-dark">
                                <h2 class="card-header">Hozzáadása</h2>
                                <div class="card-body h-100 d-flex flex-column justify-content-between">
                                    <form id="carbrandhozzaadas" method="post">
                                        <label for="carbrandname">Márka:</label><br>
                                        <input type="text" class="rounded-2" name="carbrandname" placeholder="pl: Ford"> <br>
                                        <p class="text-danger" %s>Ez a márka már szerepel az adatbázisban</p>
                                        <button type="submit" class="btn btn-info m-2 rounded-2" style="font-size: 2vh" form="carbrandhozzaadas" name="adding">Hozzáadása</button><br>
                                        <button type="button" class="btn btn-danger rounded-2" style="font-size: 2vh" onclick="location.href = 'admin'">Mégse</button>
                                    </form>
                                </div>
                            </div>
                        </div>
                    </div>
            """;

    private String createTable() {
        CarBrandManager cbm = new CarBrandManager(MySqlConnection.getConnection());
        ArrayList<CarBrand> carBrands = cbm.getCarBrands();
        if (carBrands.isEmpty()) {
            return """
                                                <tr>
                                                    <td colspan="2">Nincs még márka felvéve</td>
                                                </tr>
                    """;
        }
        String rowSample = """
                                            <tr>
                                                <td>%s</td>
                                                <td><input type="checkbox" name="cbchck-%s" style="height: 1.7vh; width: 1.7vh;"></td>
                                            </tr>
                """;
        ArrayList<String> rows = new ArrayList<>();
        for (CarBrand cb : carBrands) {
            rows.add(String.format(rowSample, cb.name(), cb.id()));
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

            if (request.getParameter("deletebrands") != null) {
                ArrayList<String> brandIds = request.getParameterMap().keySet().stream()
                        .filter(r -> r.startsWith("cbchck-"))
                        .map(r -> r.substring(7))
                        .collect(Collectors.toCollection(ArrayList::new));
                CarBrandManager cbm = new CarBrandManager(MySqlConnection.getConnection());
                brandIds.forEach(id -> cbm.deleteCarBrand(
                        cbm.getCarBrands().stream()
                                .filter(brand -> brand.id().equals(id))
                                .findFirst().get()));
            } else if (request.getParameter("adding") != null && request.getParameter("carbrandname") != null) {
                String newCarBrandName = request.getParameter("carbrandname").toUpperCase();
                CarBrandManager cbm = new CarBrandManager(MySqlConnection.getConnection());
                if (!cbm.getCarBrandNames().contains(newCarBrandName)) {
                    cbm.addCarBrand(new CarBrand(
                            DigestUtils.sha256Hex(newCarBrandName)
                            , newCarBrandName
                    ));
                    response.sendRedirect("addbrand");
                }
                hidden = "";
            }

            PrintWriter writer = response.getWriter();
            writer.println(ResponseComponents.getHeader("Add Car Brand"));
            writer.println(String.format(BRAND_TABLE, createTable()));
            writer.println(String.format(ADDING_FORM, hidden));
            writer.println(ResponseComponents.getFooter());
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
