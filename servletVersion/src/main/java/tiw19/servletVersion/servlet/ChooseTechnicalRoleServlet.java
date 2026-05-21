package tiw19.servletVersion.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import jakarta.servlet.http.HttpSession;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;
import tiw19.servletVersion.model.User;

import java.io.IOException;

/**
 * Servlet for the Login Page (Choose Technical Role).
 * <br>This Servlet handles the Login of a User, in particular the choice of the Technical Role (Manager vs Collaborator).
 * <br>This Servlet operates on the "/login/chooseTechnicalRole.html" Web Page.
 *
 * @author LoreDN - Lorenzo Di Napoli
 * @version 1.0
 * @since 1.0
 */
@WebServlet("/login/chooseTechnicalRole")
public class ChooseTechnicalRoleServlet extends HttpServlet {

    /**
     * HTTP GET request handler.
     * <br>This method handles an HTTP GET request, redirecting the User to the "/login/chooseTechnicalRole.html" Web Page.
     *
     * @param request An {@link HttpServletRequest} object that contains the request the client has made of the servlet
     * @param response An {@link HttpServletResponse} object that contains the response the servlet sends to the client
     *
     * @throws ServletException Servlet error
     * @throws IOException Connection error
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // check User
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null ||
                (!((User) session.getAttribute("user")).isManager() && !((User) session.getAttribute("user")).isCollaborator())
        ) {
            // fallback to "/logout"
            response.sendRedirect(request.getContextPath() + "/logout");
            return;
        }

        // load Thymeleaf
        TemplateEngine engine = (TemplateEngine) getServletContext().getAttribute("templateEngine");
        JakartaServletWebApplication app = JakartaServletWebApplication.buildApplication(getServletContext());
        WebContext context = new WebContext(app.buildExchange(request, response));
        response.setContentType("text/html;charset=UTF-8");

        // load Web Page "/login/chooseTechnicalRole"
        engine.process("chooseTechnicalRole", context, response.getWriter());
    }

    /**
     * @see HttpServlet Default implementation.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        super.doPost(request, response);
    }
}
