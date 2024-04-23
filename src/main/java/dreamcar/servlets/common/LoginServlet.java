package dreamcar.servlets.common;

import dreamcar.dbmanagement.UserTableManager;
import dreamcar.dbmanagement.tables.User;
import dreamcar.servlets.ResponseComponents;
import dreamcar.startup.connection.MySqlConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private boolean checkCredentials(String username, String password) {
        UserTableManager utm = new UserTableManager(MySqlConnection.getConnection());
        Map<String, String> credentials = utm.getUsers().stream()
                .filter(User::isActive)
                .collect(Collectors.toMap(User::username, User::password));
        return credentials.containsKey(username) && credentials.get(username).equals(password);
    }

    @Override
    public void init() throws ServletException {
        //TODO
    }

    //TODO: Átírni az index.html átírányítását, hogy ne használjon GET-et...
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String username = Optional.ofNullable(request.getParameter("username")).orElse("");
        String password = Optional.ofNullable(request.getParameter("password")).orElse("");

        PrintWriter writer = response.getWriter();
        writer.println(ResponseComponents.getHeader("Login"));
        boolean warning = false;

        if (!username.isEmpty() && !password.isEmpty()) {
            String hashedUsername = DigestUtils.sha256Hex(username);
            String hashedPassword = DigestUtils.sha256Hex(password);
            if (checkCredentials(hashedUsername, hashedPassword)) {
                request.getSession().setAttribute("user", hashedUsername);
                response.sendRedirect(request.getRequestURI().replace("/login", "/home"));
            }
            warning = true;
        }
        writer.println(ResponseComponents.getLogin("", warning));
        writer.println(ResponseComponents.getFooter());
        writer.close();
    }

    @Override
    public void destroy() {
        //TODO
    }
}
