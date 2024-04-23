package dreamcar.servlets.user;

import dreamcar.dbmanagement.FavCarTableManager;
import dreamcar.servlets.ResponseComponents;
import dreamcar.startup.connection.MySqlConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/home")
public class HomeServlet extends HttpServlet {

    @Override
    public void init() throws ServletException {
        super.init();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        ResponseComponents.setResponseHeader(response);

        try {
            PrintWriter writer = response.getWriter();
            writer.println(ResponseComponents.getHeader("DreamCar Home"));
            FavCarTableManager ftm = new FavCarTableManager(MySqlConnection.getConnection());
            if (ftm.getUserFavCars(String.valueOf(request.getSession().getAttribute("username"))).isEmpty()) {
                //TODO: letiltani, hogy megnézhesse a kocsijait külön oldalon
            }
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
