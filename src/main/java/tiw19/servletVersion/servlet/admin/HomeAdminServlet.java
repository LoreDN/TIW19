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
import tiw19.servletVersion.dao.WorkPackageDAO;
import tiw19.servletVersion.model.Project;
import tiw19.servletVersion.model.User;
import tiw19.servletVersion.model.WorkPackage;

import java.io.IOException;
import java.sql.SQLException;

import java.util.List;

/**
 * Servlet for the Admin Home Page.
 * <br>This Servlet handles the Home Page for the Admin users.
 * <br>This Servlet operates on the "/homeAdmin.html" Web Page.
 *
 * @author LoreDN - Lorenzo Di Napoli
 * @version 1.0
 * @since 1.0
 */
@WebServlet("/homeAdmin")
public class HomeAdminServlet extends HttpServlet {
    private static final String[] variables = {
            "projectErrorMessage", "projectSuccessMessage", "projectTitle", "projectDuration", "projectManager",
            "workPackageErrorMessage", "workPackageSuccessMessage", "workPackageProjectTitle", "workPackageTitle", "workPackageStartingMonth", "workPackageEndingMonth",
            "taskErrorMessage", "taskSuccessMessage", "taskWorkPackageTitles", "taskTitle", "taskStartingMonth", "taskEndingMonth"
    };

    /**
     * HTTP GET request handler.
     * <br>This method handles an HTTP GET request, redirecting the User to the "/homeAdmin.html" Web Page if he is an Admin, otherwise to "/login.html".
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
            // fallback to "/login"
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        // load Thymeleaf
        TemplateEngine engine = (TemplateEngine) getServletContext().getAttribute("templateEngine");
        JakartaServletWebApplication app = JakartaServletWebApplication.buildApplication(getServletContext());
        WebContext context = new WebContext(app.buildExchange(request, response));
        response.setContentType("text/html;charset=UTF-8");

        // get Session variables
        for (String variable : variables) {
            if (session.getAttribute(variable) != null) {
                context.setVariable(variable, session.getAttribute(variable));
                session.removeAttribute(variable);
            }
        }

        // load Projects with status CREATED
        try {
            List<Project> createdProjects = ProjectDAO.getCreatedProjects(((User) session.getAttribute("user")).getUsername());
            context.setVariable("createdProjects", createdProjects);
        } catch (SQLException exception) {
            String errorMessage = "[DB Error] : Error while getting Projects from DB!!!";
            context.setVariable("workPackageErrorMessage", errorMessage);
            context.setVariable("taskErrorMessage", errorMessage);
        }

        // load Work-Packages
        try {
            List<WorkPackage> workPackages = WorkPackageDAO.getCreated(((User) session.getAttribute("user")).getUsername());
            context.setVariable("workPackages", workPackages);
        } catch (SQLException exception) {
            String errorMessage = "[DB Error] : Error while getting Work-Packages from DB!!!";
            context.setVariable("taskErrorMessage", errorMessage);
        }

        // load Web Page "/homeAdmin"
        engine.process("homeAdmin", context, response.getWriter());
    }

    /**
     * @see HttpServlet Default implementation.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        super.doPost(request, response);
    }
}
