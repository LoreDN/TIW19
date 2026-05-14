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

import tiw19.servletVersion.dao.*;
import tiw19.servletVersion.model.*;

import java.io.IOException;
import java.sql.SQLException;

import java.util.*;

/**
 * Servlet for the Collaborators Monitoring Page.
 * <br>This Servlet handles the Collaborators Monitoring Page for the Manager users.
 * <br>This Servlet operates on the "/homeManager/collaboratorsMonitoring.html" Web Page.
 *
 * @author LoreDN - Lorenzo Di Napoli
 * @version 1.0
 * @since 1.0
 */
@WebServlet("/homeManager/collaboratorsMonitoring")
public class CollaboratorsMonitoringServlet extends HttpServlet {

    /**
     * HTTP GET request handler.
     * <br>This method handles an HTTP GET request, redirecting the User to the "/homeManager/collaboratorsMonitoring.html" Web Page if he is a Manager, otherwise to "/login.html".
     *
     * @param request An {@link HttpServletRequest} object that contains the request the client has made of the servlet
     * @param response An {@link HttpServletResponse} object that contains the response the servlet sends to the client
     *
     * @throws ServletException Servlet error
     * @throws IOException Connection error
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String collaboratorID = request.getParameter("collaboratorID");

        // check User
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null || !((User) session.getAttribute("user")).isManager()) {
            // fallback to "/login"
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        // load Thymeleaf
        TemplateEngine engine = (TemplateEngine) getServletContext().getAttribute("templateEngine");
        JakartaServletWebApplication app = JakartaServletWebApplication.buildApplication(getServletContext());
        WebContext context = new WebContext(app.buildExchange(request, response));
        response.setContentType("text/html;charset=UTF-8");

        // load Collaborators
        try {
            List<User> collaborators = UserDAO.getCollaborators(((User) session.getAttribute("user")).getUsername());
            context.setVariable("collaborators", collaborators);
        } catch (SQLException e) {
            String errorMessage = "[DB Error] : Error while getting Collaborators from DB!!!";
            context.setVariable("collaboratorsErrorMessage", errorMessage);
        }

        // load Project Stats
        if (collaboratorID != null && !collaboratorID.isBlank()) {
            Map<Project, Map<WorkPackage, Map<Task, Map<Integer, Collaborator>>>> tableData = new LinkedHashMap<>();
            try {
                int ID = Integer.parseInt(collaboratorID);
                User selectedCollaborator = UserDAO.getFromID(ID);
                if (selectedCollaborator == null) throw new SQLException();
                List<Project> projects = ProjectDAO.getCollaboratorProjects(
                        ((User) session.getAttribute("user")).getUsername(),
                        selectedCollaborator.getUsername()
                );
                Map<Project, List<Integer>> months = new LinkedHashMap<>();
                for (Project project : projects) {
                    months.put(project, new ArrayList<>());
                    for (int month = 1; month <= project.getMonthsNumber(); month++) months.get(project).add(month);
                }

                // populate Project Stats table
                for (Project project : projects) {
                    tableData.put(project, new LinkedHashMap<>());
                    Map<WorkPackage, List<Task>> workPackages = WorkPackageDAO.getFromProject(project);
                    for (WorkPackage workPackage : workPackages.keySet()) {
                        Map<Task, Map<Integer, Collaborator>> tasks = new LinkedHashMap<>();
                        tableData.get(project).put(workPackage, tasks);
                        for (Task task : workPackages.get(workPackage)) {
                            int taskID = task.getID();
                            Map<Integer, Collaborator> plannings = new LinkedHashMap<>();
                            tasks.put(task, plannings);
                            for (int month : months.get(project)) {
                                Collaborator planning = CollaboratorDAO.getFromTriplet(ID, taskID, month);
                                if (planning != null) plannings.put(month, planning);
                            }
                        }
                    }
                }

                // save context variables
                context.setVariable("months", months);
                context.setVariable("tableData", tableData);
                context.setVariable("selectedCollaborator", selectedCollaborator);
            } catch (SQLException exception) {
                context.setVariable("monitoringErrorMessage", "[DB Error] : Error while getting Collaborator Stats from DB!!!");
            }
        }

        // load Web Page "/homeManager/collaboratorsMonitoring"
        engine.process("collaboratorsMonitoring", context, response.getWriter());
    }

    /**
     * @see HttpServlet Default implementation.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        super.doPost(request, response);
    }

}