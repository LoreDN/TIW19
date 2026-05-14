package tiw19.servletVersion.servlet.admin;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import tiw19.servletVersion.dao.ProjectDAO;
import tiw19.servletVersion.model.*;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Servlet for the Project Verification Page.
 * <br>This Servlet handles the Project Verification Page for the Admin users.
 * <br>This Servlet operates on the "/homeAdmin/projectVerification.html" Web Page.
 *
 * @author LoreDN - Lorenzo Di Napoli
 * @version 1.0
 * @since 1.0
 */
@WebServlet("/homeAdmin/projectVerification")
public class ProjectVerificationServlet extends HttpServlet {

    /**
     * HTTP GET request handler.
     * <br>This method handles an HTTP GET request, redirecting the User to the "/homeAdmin/projectVerification.html" Web Page if he is an Admin, otherwise to "/login.html".
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
        if (session == null || session.getAttribute("user") == null || !((User) session.getAttribute("user")).isAdmin()) {
            // fallback to "/logout"
            response.sendRedirect(request.getContextPath() + "/logout");
            return;
        }

        // load Thymeleaf
        TemplateEngine engine = (TemplateEngine) getServletContext().getAttribute("templateEngine");
        JakartaServletWebApplication app = JakartaServletWebApplication.buildApplication(getServletContext());
        WebContext context = new WebContext(app.buildExchange(request, response));
        response.setContentType("text/html;charset=UTF-8");

        // load Projects
        try {
            String admin = ((User) session.getAttribute("user")).getUsername();
            context.setVariable("createdProjects", ProjectDAO.getAll(admin, "CREATED"));
            context.setVariable("assignedProjects", ProjectDAO.getAll(admin, "ASSIGNED"));
            context.setVariable("concludedProjects", ProjectDAO.getAll(admin, "CONCLUDED"));
        } catch (SQLException exception) {
            String errorMessage = "[DB Error] : Error while getting Projects from DB!!!";
            context.setVariable("loadingErrorMessage", errorMessage);
        }

        // load Web Page "/homeAdmin/projectVerification"
        engine.process("projectVerification", context, response.getWriter());
    }

    /**
     * @see HttpServlet Default implementation.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        super.doPost(request, response);
    }

}
