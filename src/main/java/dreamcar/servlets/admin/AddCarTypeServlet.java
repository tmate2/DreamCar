package dreamcar.servlets.admin;

import dreamcar.servlets.ResponseComponents;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

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
                                <div class="d-flex justify-content-around">
                                    <button type="submit" class="btn btn-danger rounded-2" name="deletetypes" form="typeform" style="font-size: 2vh;">Kijelöltek törlése</button>
                                </div>
                            </div>
                        </div>
                    </form>
            """;

    private final String ADDING_FORM = """
                    <div class="d-flex justify-content-center m-0 p-0" style="background-color: silver;">
                        <div class="col-sm-5 col-xs-7 m-3" style="min-width: 40vh;">
                            <div class="card h-100 m-3  pt-3 d-flex flex-column flex-box justify-content-between rounded-4 shadow bg-light text-dark">
                                <h2 class="card-header">Hozzáadása</h2>
                                <div class="card-body h-100 d-flex flex-column justify-content-between">
                                    <form id="cartypehozzaadas" method="post">
                                        <label>Márka:</label><br>
                                        <div class="dropdown-center">
                                            <button class="btn btn-light dropdown-toggle justify-content-center"  type="button" data-bs-toggle="dropdown" aria-expanded="false" style="font-size: 1.5vh;">
                                                Márka választás
                                            </button>
                                            <ul class="dropdown-menu" style="font-size: 1.5vh;">
                                                %s
                                            </ul>
                                        </div>
                                        <label for="carbrandname">Típus:</label><br>
                                        <input type="text" class="rounded-2" name="carbrandname" placeholder="pl: Mustang"> <br>
                                        <p class="text-danger" %s>Ez a típus már szerepel az adatbázisban</p>
                                        <button type="submit" class="btn btn-info m-2 rounded-2" style="font-size: 2vh" form="cartypehozzaadas" name="adding">Hozzáadása</button><br>
                                        <button type="button" class="btn btn-danger rounded-2" style="font-size: 2vh" onclick="location.href = 'admin'">Mégse</button>
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

        try {
            PrintWriter writer = response.getWriter();
            writer.println(ResponseComponents.getHeader("Car Types"));

            writer.println(ResponseComponents.getFooter());
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
