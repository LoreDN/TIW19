package tiw19.servletVersion.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import tiw19.servletVersion.dao.UserDAO;
import tiw19.servletVersion.model.User;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Servlet for the Login Page.
 * <br>This Servlet handles the Login of a User, if it is successfully, it redirects to the right Home Page, otherwise it comes back to the Login one.
 * <br>This Servlet operates on the "/login.html" Web Page.
 *
 * @author LoreDN - Lorenzo Di Napoli
 * @version 1.0
 * @since 1.0
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    /**
     * HTTP GET request handler.
     * <br>This method handles an HTTP GET request, redirecting the User to the "/login.html" Web Page.
     *
     * @param request An {@link HttpServletRequest} object that contains the request the client has made of the servlet
     * @param response An {@link HttpServletResponse} object that contains the response the servlet sends to the client
     *
     * @throws ServletException Servlet error
     * @throws IOException Connection error
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // load Thymeleaf
        TemplateEngine engine = (TemplateEngine) getServletContext().getAttribute("templateEngine");
        JakartaServletWebApplication app = JakartaServletWebApplication.buildApplication(getServletContext());
        WebContext context = new WebContext(app.buildExchange(request, response));
        response.setContentType("text/html;charset=UTF-8");

        // load Web Page "/login"
        engine.process("login", context, response.getWriter());
    }

    /**
     * HTTP POST request Handler.
     * <br>This method handles an HTTP POST request, checking if the Login credentials are valid and redirecting the User to the right home Web Page (if correct).
     * <br>It manages both the case of invalid form (like blank strings) and the case of not correct credentials (through the DB).
     *
     * @param request An {@link HttpServletRequest} object that contains the request the client has made of the servlet
     * @param response An {@link HttpServletResponse} object that contains the response the servlet sends to the client
     *
     * @throws ServletException Servlet error
     * @throws IOException Connection error
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        // load Thymeleaf
        TemplateEngine engine = (TemplateEngine) getServletContext().getAttribute("templateEngine");
        JakartaServletWebApplication app = JakartaServletWebApplication.buildApplication(getServletContext());
        WebContext context = new WebContext(app.buildExchange(request, response));
        response.setContentType("text/html;charset=UTF-8");

        // Server-side validation
        String errorMessage = null;
        if (username == null || username.isBlank()) {
            if (password == null || password.isBlank()) errorMessage = "Username and password are required!!!";
            else errorMessage = "Username is required!!!";
        } else if (password == null || password.isBlank()) errorMessage = "Password is required!!!";

        // form error
        if (errorMessage != null) {
            reloadWebPage(errorMessage, username, response, engine, context);
            return;
        }

        // DB-side credentials check
        try {
            UserDAO userDAO = new UserDAO();
            User user = userDAO.checkCredentials(username, password);

            if (user == null) {
                reloadWebPage(
                        "[Login Error] : Invalid username or password",
                        username, response, engine, context
                );
            } else {
                // save session
                HttpSession session = request.getSession();
                session.setAttribute("user", user);

                // role redirect
                if (user.isAdmin()) response.sendRedirect(request.getContextPath() + "/homeAdmin");
                else if (user.isManager()) response.sendRedirect(request.getContextPath() + "/homeManager");
                else if (user.isCollaborator()) response.sendRedirect(request.getContextPath() + "/homeCollaborator");
                else response.sendRedirect(request.getContextPath() + "/chooseTechnicalRole");
            }
        } catch (SQLException exception) {
            reloadWebPage(
                    "[Database Error] : " + exception.getMessage(),
                    username, response, engine, context
            );
        }
    }

    // reload the Web Page showing the Error
    private void reloadWebPage(String errorMessage, String username,
                               HttpServletResponse response, TemplateEngine engine, WebContext context) throws IOException {
        context.setVariable("errorMessage", errorMessage);
        context.setVariable("username", username);
        engine.process("login", context, response.getWriter());
    }
}