package tiw19.servletVersion.servlet.collaborator;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import tiw19.servletVersion.dao.CollaboratorDAO;
import tiw19.servletVersion.dao.PlanningDAO;
import tiw19.servletVersion.dao.ProjectDAO;
import tiw19.servletVersion.dao.TaskDAO;
import tiw19.servletVersion.model.*;

import java.io.IOException;
import java.sql.SQLException;

import java.util.List;
import java.util.Map;

/**
 * Servlet for the Collaborator Home Page.
 * <br>This Servlet handles the Home Page for the Collaborator users.
 * <br>This Servlet operates on the "/homeCollaborator.html" Web Page.
 *
 * @author LoreDN - Lorenzo Di Napoli
 * @version 1.0
 * @since 1.0
 */
@WebServlet("/homeCollaborator")
public class HomeCollaboratorServlet extends HttpServlet {
    private static final String[] variables = {
            "hoursErrorMessage", "hoursSuccessMessage", "selectedMonth", "workedHours"
    };

    /**
     * HTTP GET request handler.
     * <br>This method handles an HTTP GET request, redirecting the User to the "/homeCollaborator.html" Web Page if he is a COLLABORATOR, otherwise to "/login.html".
     *
     * @param request An {@link HttpServletRequest} object that contains the request the client has made of the servlet
     * @param response An {@link HttpServletResponse} object that contains the response the servlet sends to the client
     *
     * @throws ServletException Servlet error
     * @throws IOException Connection error
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String projectID = request.getParameter("projectID");
        String taskID = request.getParameter("taskID");

        // check User
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null || !((User) session.getAttribute("user")).isCollaborator()) {
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

        // load Projects
        try {
            List<Project> projects = ProjectDAO.getAssignedProjects(((User) session.getAttribute("user")).getUsername());
            context.setVariable("projects", projects);
        } catch (SQLException exception) {
            context.setVariable("projectsErrorMessage", "[DB Error] : Error while getting Projects from DB!!!");
        }

        // load Work-Packages & Tasks
        if (projectID != null && !projectID.isBlank()) {
            try {
                int ID = Integer.parseInt(projectID);
                Project selectedProject = ProjectDAO.getFromID(ID);
                if (selectedProject == null) throw new SQLException();
                Map<WorkPackage, List<Task>> assignments = CollaboratorDAO.getAssignments(((User) session.getAttribute("user")).getUsername(), ID);
                context.setVariable("assignments", assignments);
                context.setVariable("selectedProject", selectedProject);
            } catch (SQLException exception) {
                context.setVariable("assignmentsErrorMessage", "[DB Error] : Error while getting Work-Packages & Tasks from DB!!!");
            }
        }

        // load Planning
        if (taskID != null && !taskID.isBlank()) {
            try {
                int ID = Integer.parseInt(taskID);
                Task selectedTask = TaskDAO.getFromID(ID);
                if (selectedTask == null) throw new SQLException();
                context.setVariable("plannings", PlanningDAO.getFromTask(ID));
                context.setVariable("taskMonths", CollaboratorDAO.getAssignedMonths(((User) session.getAttribute("user")).getID(), ID));
                context.setVariable("selectedTask", selectedTask);
            } catch (SQLException exception) {
                context.setVariable("hoursErrorMessage", "[DB Error] : Error while getting Task Planning from DB!!!");
            }
        }

        // load Web Page "/homeCollaborator"
        engine.process("homeCollaborator", context, response.getWriter());
    }

    /**
     * @see HttpServlet Default implementation.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        super.doPost(request, response);
    }
}
