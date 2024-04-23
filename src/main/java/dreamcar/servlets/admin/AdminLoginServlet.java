package dreamcar.servlets.admin;

import dreamcar.dbmanagement.UserTableManager;
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
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@WebServlet("/adminlogin")
public class AdminLoginServlet extends HttpServlet {

    private boolean checkAdminCredentials(String username, String password) {
        UserTableManager utm = new UserTableManager(MySqlConnection.getConnection());
        Map<String, String> credentials = utm.getUsers().stream()
                .filter(User::isActive)
                .filter(User::isAdmin)
                .collect(Collectors.toMap(User::username, User::password));
        return credentials.containsKey(username) && credentials.get(username).equals(password);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String username = Optional.ofNullable(request.getParameter("username")).orElse("");
        String password = Optional.ofNullable(request.getParameter("password")).orElse("");

        PrintWriter writer = response.getWriter();
        writer.println(ResponseComponents.getHeader("Admin Login"));
        boolean warning = false;

        if (!username.isEmpty() && !password.isEmpty()) {
            String hashedUsername = DigestUtils.sha256Hex(username);
            String hashedPassword = DigestUtils.sha256Hex(password);
            if (checkAdminCredentials(hashedUsername, hashedPassword)) {
                request.getSession().setAttribute("admin", hashedUsername);
                response.sendRedirect(request.getRequestURI().replace("/admin/login", "/admin"));
            }
            warning = true;
        }
        writer.println(ResponseComponents.getLogin("", warning));
        //TODO: Kitalálni valamit arra, hogy ne dobjon tovább az admin/admin/login-ra, ha az Admin-ra kattintok megint
        writer.println(ResponseComponents.getFooter());
        writer.close();
    }

}
