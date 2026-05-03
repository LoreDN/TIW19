package tiw19.servletVersion.servlet.manager;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import tiw19.servletVersion.dao.CollaboratorDAO;
import tiw19.servletVersion.dao.TaskDAO;
import tiw19.servletVersion.dao.UserDAO;
import tiw19.servletVersion.model.Task;
import tiw19.servletVersion.model.User;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Servlet for the triplet (Task, Month, Collaborator) Assignment.
 * <br>This Servlet handles the assignment of a triplet (Task, Month, Collaborator) by a Manager user, checking if all the fields are valid.
 * <br>This Servlet operates on the form of the "/homeManager.html" Web Page.
 *
 * @author LoreDN - Lorenzo Di Napoli
 * @version 1.0
 * @since 1.0
 */
@WebServlet("/homeManager/collaboratorAssignment")
public class CollaboratorAssignmentServlet extends HttpServlet {

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
     * <br>This method handles an HTTP POST request, checking if the triplet (Task , Month, Collaborator) is valid to be assigned.
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
        String taskValues = request.getParameter("taskTitles");
        String collaboratorMonth = request.getParameter("month");
        String collaborator = request.getParameter("collaborator");

        // check user
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null || !((User) session.getAttribute("user")).isManager()) {
            // fallback to "/login"
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        // parse parameters
        String projectTitle, workPackageTitle, taskTitle;
        try {
            String[] parts = taskValues.split("###");
            projectTitle = parts[1];
            workPackageTitle = parts[2];
            taskTitle = parts[3];
        } catch (Exception exception) {
            reloadWebPage("Task selection is required!!!",
                    taskValues, collaborator, collaboratorMonth,
                    request, response
            );
            return;
        }

        // Server-side validation
        String collaboratorErrorMessage = null;
        if (collaborator == null || collaborator.isBlank() ||
                collaboratorMonth == null || collaboratorMonth.isBlank()
        ) {
            collaboratorErrorMessage = "All fields are required!!!";
        } else {
            try {
                Task task = TaskDAO.getFromTitle(((User) session.getAttribute("user")).getUsername(), projectTitle, workPackageTitle, taskTitle);
                User collaboratorUser = UserDAO.getFromUsername(collaborator);
                // Task
                if (task == null) collaboratorErrorMessage = "[Hours Error] : Task not found!!!";
                // collaborator
                else if (collaboratorUser == null)
                    collaboratorErrorMessage = "[Collaborator Error] : Collaborator not found!!!";
                else if (!collaboratorUser.isCollaborator())
                    collaboratorErrorMessage = "[Collaborator Error] : Collaborator must have the role COLLABORATOR!!!";
                // month
                else if (Integer.parseInt(collaboratorMonth) < 1)
                    collaboratorErrorMessage = "[Collaborator Error] : Month must be greater than 1!!!";
                else if (Integer.parseInt(collaboratorMonth) < task.getStartingMonth())
                    collaboratorErrorMessage = "[Collaborator Error] : Month cannot be smaller than the Task starting one!!!";
                else if (Integer.parseInt(collaboratorMonth) > task.getEndingMonth())
                    collaboratorErrorMessage = "[Collaborator Error] : Month cannot be greater than the Task ending one!!!";
            } catch (NumberFormatException exception) {
                collaboratorErrorMessage = "Month must be a number!!!";
            } catch (SQLException exception) {
                collaboratorErrorMessage = "[DB Error] : " + exception.getMessage();
            }
        }

        // form error
        if (collaboratorErrorMessage != null) {
            reloadWebPage(collaboratorErrorMessage,
                    taskValues, collaborator, collaboratorMonth,
                    request, response
            );
            return;
        }

        // DB-side validation
        try {
            // check if Collaborator is already assigned to (Task, Month)
            int taskID = TaskDAO.getID(((User) session.getAttribute("user")).getUsername(), projectTitle, workPackageTitle, taskTitle);
            if (CollaboratorDAO.existsAssignment(taskID, Integer.parseInt(collaboratorMonth), collaborator)) {
                reloadWebPage("[Collaborator Error] : Collaborator is already assigned to couple (\"" + taskTitle + "\", " + collaboratorMonth + ")!!!",
                        taskValues, collaborator, collaboratorMonth,
                        request, response
                );
            } else {
                // assign Collaborator to (Task, Month)
                CollaboratorDAO.newAssignment(taskID, Integer.parseInt(collaboratorMonth), collaborator);
                response.sendRedirect(request.getContextPath() + "/homeManager");
            }
        } catch (SQLException exception) {
            reloadWebPage("[DB Error] : " + exception.getMessage(),
                    taskValues, collaborator, collaboratorMonth,
                    request, response
            );
        }
    }

    // reload the Web Page showing the Error
    private void reloadWebPage(String collaboratorErrorMessage, String taskValues, String collaborator, String collaboratorMonth,
                               HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        session.setAttribute("collaboratorErrorMessage", collaboratorErrorMessage);
        session.setAttribute("collaboratorTaskTitles", taskValues);
        session.setAttribute("collaborator", collaborator);
        session.setAttribute("collaboratorMonth", collaboratorMonth);
        response.sendRedirect(request.getContextPath() + "/homeManager");
    }
}
