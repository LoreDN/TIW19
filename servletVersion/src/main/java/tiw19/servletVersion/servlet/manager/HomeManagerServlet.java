package tiw19.servletVersion.servlet.manager;

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
import tiw19.servletVersion.dao.TaskDAO;
import tiw19.servletVersion.model.Project;
import tiw19.servletVersion.model.Task;
import tiw19.servletVersion.model.User;

import java.io.IOException;
import java.sql.SQLException;

import java.util.List;

/**
 * Servlet for the Manager Home Page.
 * <br>This Servlet handles the Home Page for the Manager users.
 * <br>This Servlet operates on the "/homeManager.html" Web Page.
 *
 * @author LoreDN - Lorenzo Di Napoli
 * @version 1.0
 * @since 1.0
 */
@WebServlet("/homeManager")
public class HomeManagerServlet extends HttpServlet {
    private static final String[] variables = {
            "hoursErrorMessage", "hoursSuccessMessage", "hoursTaskTitles", "plannedMonth", "plannedHours",
            "collaboratorErrorMessage", "collaboratorSuccessMessage", "collaboratorTaskTitles", "collaborator", "collaboratorMonth",
            "assignmentErrorMessage", "assignmentSuccessMessage", "projectTitle"
    };

    /**
     * HTTP GET request handler.
     * <br>This method handles an HTTP GET request, redirecting the User to the "/homeManager.html" Web Page if he is a MANAGER, otherwise to "/login.html".
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
        if (session == null || session.getAttribute("user") == null || !((User) session.getAttribute("user")).isManager()) {
            // fallback to "/logout"
            response.sendRedirect(request.getContextPath() + "/logout");
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

        // load Tasks
        try {
            List<Task> tasks = TaskDAO.getAllToBeAssigned(((User) session.getAttribute("user")).getUsername());
            context.setVariable("tasks", tasks);
        } catch (SQLException exception) {
            String errorMessage = "[DB Error] : Error while getting Tasks from DB!!!";
            context.setVariable("hoursErrorMessage", errorMessage);
            context.setVariable("collaboratorErrorMessage", errorMessage);
        }

        // load Projects to Assign
        try {
            List<Project> toAssignProjects = ProjectDAO.getCreatedProjects(((User) session.getAttribute("user")).getUsername());
            context.setVariable("toAssignProjects", toAssignProjects);
        } catch (SQLException exception) {
            String errorMessage = "[DB Error] : Error while getting Projects from DB!!!";
            context.setVariable("assignmentErrorMessage", errorMessage);
        }

        // load Web Page "/homeManager"
        engine.process("homeManager", context, response.getWriter());
    }

    /**
     * @see HttpServlet Default implementation.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        super.doPost(request, response);
    }
}
