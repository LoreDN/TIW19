package tiw19.servletVersion.servlet.manager;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import tiw19.servletVersion.dao.ProjectDAO;
import tiw19.servletVersion.model.Project;
import tiw19.servletVersion.model.User;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Servlet for Project Assignment.
 * <br>This Servlet handles the assignment of a Project by a Manager user, checking if all the fields are valid.
 * <br>This Servlet operates on the form of the "/homeManager.html" Web Page.
 *
 * @author LoreDN - Lorenzo Di Napoli
 * @version 1.0
 * @since 1.0
 */
@WebServlet("/homeManager/projectAssignment")
public class ProjectAssignmentServlet extends HttpServlet {

    /**
     * @see HttpServlet Redirect to "/homeManager".
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // never access it directly
        response.sendRedirect(request.getContextPath() + "/homeManager");
    }

    /**
     * HTTP POST request Handler.
     * <br>This method handles an HTTP POST request, checking if a Project is valid to be put in the status ASSIGNED.
     * <br>It manages both the case of invalid form (like blank strings) and the case of not correct parameters (through the DB).
     *
     * @param request An {@link HttpServletRequest} object that contains the request the client has made of the servlet
     * @param response An {@link HttpServletResponse} object that contains the response the servlet sends to the client
     *
     * @throws ServletException Servlet error
     * @throws IOException Connection error
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String projectTitle = request.getParameter("projectTitle");

        // check user
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null || !((User) session.getAttribute("user")).isManager()) {
            // fallback to "/login"
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        // Server-side validation
        String assignmentErrorMessage = null;
        if (projectTitle == null || projectTitle.isBlank()) {
            assignmentErrorMessage = "Project Title is required!!!";
        } else {
            try {
                Project project = ProjectDAO.getFromTitle(((User) session.getAttribute("user")).getUsername(), projectTitle);
                if (project == null) assignmentErrorMessage = "[Assignment Error] : Project not found!!!";
                else if (project.isAssigned() || project.isConcluded())
                    assignmentErrorMessage = "[Assignment Error] : Project must have status \"CREATED\"!!!";
            } catch (SQLException exception) {
                assignmentErrorMessage = "[DB Error] : " + exception.getMessage();
            }
        }

        // form error
        if (assignmentErrorMessage != null) {
            reloadWebPage(assignmentErrorMessage, projectTitle,
                    request, response
            );
            return;
        }

        // DB-side validation
        try {
            // check if Project can be assigned
            if (!ProjectDAO.canBeAssigned(((User) session.getAttribute("user")).getUsername(), projectTitle)) {
                reloadWebPage("[Assignment Error] : Not all the Task for the Project \"" + projectTitle + "\" are assigned!!! " +
                                "Maybe there is a Work-Package with no sub Tasks!!!",
                        projectTitle,
                        request, response
                );
            } else {
                // assign the Project
                if (ProjectDAO.setToAssigned(((User) session.getAttribute("user")).getUsername(), projectTitle)) {
                    response.sendRedirect(request.getContextPath() + "/homeManager");
                } else {
                    reloadWebPage("[DB Error Error] : Assignment of Project \"" + projectTitle + "\" was not successfully!!! " +
                                    "Check for all the constraints to be valid!!!",
                            projectTitle,
                            request, response
                    );
                }
            }
        } catch (SQLException exception) {
            reloadWebPage("[DB Error] : " + exception.getMessage(),
                    projectTitle,
                    request, response
            );
        }
    }

    // reload the Web Page showing the Error
    private void reloadWebPage(String assignmentErrorMessage, String projectTitle,
                               HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        session.setAttribute("assignmentErrorMessage", assignmentErrorMessage);
        session.setAttribute("projectTitle", projectTitle);
        response.sendRedirect(request.getContextPath() + "/homeManager");
    }
}
