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

import tiw19.servletVersion.dao.PlanningDAO;
import tiw19.servletVersion.dao.ProjectDAO;
import tiw19.servletVersion.dao.WorkPackageDAO;
import tiw19.servletVersion.model.*;

import java.io.IOException;
import java.sql.SQLException;

import java.util.*;

/**
 * Servlet for the Project Monitoring Page.
 * <br>This Servlet handles the Project Monitoring Page for the Manager users.
 * <br>This Servlet operates on the "/homeManager/projectMonitoring.html" Web Page.
 *
 * @author LoreDN - Lorenzo Di Napoli
 * @version 1.0
 * @since 1.0
 */
@WebServlet("/homeManager/projectMonitoring")
public class ProjectMonitoringServlet extends HttpServlet {
    private static final String[] variables = {
            "conclusionErrorMessage", "conclusionSuccessMessage",
    };

    /**
     * HTTP GET request handler.
     * <br>This method handles an HTTP GET request, redirecting the User to the "/homeManager/projectMonitoring.html" Web Page if he is a Manager, otherwise to "/login.html".
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

        // get Session variables
        for (String variable : variables) {
            if (session.getAttribute(variable) != null) {
                context.setVariable(variable, session.getAttribute(variable));
                session.removeAttribute(variable);
            }
        }

        // load Projects
        try {
            List<Project> projects = ProjectDAO.getManagedProjects(((User) session.getAttribute("user")).getUsername());
            context.setVariable("projects", projects);
        } catch (SQLException e) {
            String errorMessage = "[DB Error] : Error while getting Projects from DB!!!";
            context.setVariable("projectsErrorMessage", errorMessage);
        }

        // load Project Stats
        if (projectID != null && !projectID.isBlank()) {
            Map<WorkPackage, Map<Task, Map<Integer, Planning>>> tableData = new LinkedHashMap<>();
            try {
                int ID = Integer.parseInt(projectID);
                Project selectedProject = ProjectDAO.getFromID(ID);
                if (selectedProject == null) throw new SQLException();
                List<Integer> months = new ArrayList<>();
                for (int month = 1; month <= selectedProject.getMonthsNumber(); month++) months.add(month);

                // populate Task Stats table
                Map<WorkPackage, List<Task>> workPackages = WorkPackageDAO.getFromProject(selectedProject);
                for (WorkPackage workPackage : workPackages.keySet()) {
                    Map<Task, Map<Integer, Planning>> tasks = new LinkedHashMap<>();
                    tableData.put(workPackage, tasks);
                    for (Task task : workPackages.get(workPackage)) {
                        int taskID = task.getID();
                        Map<Integer, Planning> plannings = new LinkedHashMap<>();
                        tasks.put(task, plannings);
                        for (int month : months) {
                            Planning planning = PlanningDAO.getFromCouple(taskID, month);
                            if (planning != null) plannings.put(month, planning);
                        }
                    }
                }

                // save context variables
                context.setVariable("months", months);
                context.setVariable("tableData", tableData);
                context.setVariable("selectedProject", selectedProject);
            } catch (SQLException exception) {
                context.setVariable("monitoringErrorMessage", "[DB Error] : Error while getting Project Stats from DB!!!");
            }

            // check if Project can be Concluded
            boolean canBeConcluded = true;
            for (Map<Task, Map<Integer, Planning>> tasks : tableData.values()) {
                for (Map<Integer, Planning> plannings : tasks.values()) {
                    for (Planning p : plannings.values()) {
                        if (p.getWorkedHours() < p.getPlannedHours()) {
                            canBeConcluded = false;
                            break;
                        }
                    }
                }
                if (!canBeConcluded) break;
            }
            context.setVariable("canBeConcluded", canBeConcluded);
        }

        // load Web Page "/homeManager/projectMonitoring"
        engine.process("projectMonitoring", context, response.getWriter());
    }

    /**
     * @see HttpServlet Default implementation.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        super.doPost(request, response);
    }

}