package tiw19.servletVersion.servlet.admin;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import tiw19.servletVersion.dao.WorkPackageDAO;
import tiw19.servletVersion.dao.TaskDAO;
import tiw19.servletVersion.model.User;
import tiw19.servletVersion.model.WorkPackage;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Servlet for the Task Creation.
 * <br>This Servlet handles the creation of a Task by an Admin user, checking if all the fields are valid.
 * <br>This Servlet operates on the form of the "/homeAdmin.html" Web Page.
 *
 * @author LoreDN - Lorenzo Di Napoli
 * @version 1.0
 * @since 1.0
 */
@WebServlet("/homeAdmin/createTask")
public class CreateTaskServlet extends HttpServlet {

    /**
     * @see HttpServlet Redirect to "/homeAdmin".
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // never access it directly
        response.sendRedirect(request.getContextPath() + "/homeAdmin");
    }

    /**
     * HTTP POST request Handler.
     * <br>This method handles an HTTP POST request, checking if the Task is valid to be created.
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
        String taskWPValues = request.getParameter("workPackageTitles");
        String taskTitle = request.getParameter("title");
        String taskStartingMonth  = request.getParameter("startingMonth");
        String taskEndingMonth  = request.getParameter("endingMonth");
        String projectTitle, workPackageTitle;

        // check user
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null || !((User) session.getAttribute("user")).isAdmin()) {
            // fallback to "/login"
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        // parse parameters
        try {
            String[] parts = taskWPValues.split("###");
            projectTitle = parts[0];
            workPackageTitle = parts[1];
        } catch (Exception exception) {
            reloadWebPage("Work-Package selection is required!!!",
                    taskWPValues, taskTitle, taskStartingMonth, taskEndingMonth,
                    request, response
            );
            return;
        }

        // Server-side validation
        String taskErrorMessage = null;
        if (taskTitle == null || taskTitle.isBlank() ||
                taskStartingMonth == null || taskStartingMonth.isBlank() ||
                taskEndingMonth == null || taskEndingMonth.isBlank()
        ) {
            taskErrorMessage = "All fields are required!!!";
        } else {
            try {
                WorkPackage workPackage = WorkPackageDAO.getFromTitle(((User) session.getAttribute("user")).getUsername(), projectTitle, workPackageTitle);
                // Work-Package
                if (workPackage == null) taskErrorMessage = "[Task Error] : Work-Package not found!!!";
                // starting month
                else if (Integer.parseInt(taskStartingMonth) < 1)
                    taskErrorMessage = "[Task Error] : Task starting month must be greater than 1!!!";
                else if (Integer.parseInt(taskStartingMonth) < workPackage.getStartingMonth())
                    taskErrorMessage = "[Task Error] : Task starting month cannot be smaller than the Work-Package one!!!";
                else if (Integer.parseInt(taskStartingMonth)  > workPackage.getEndingMonth())
                    taskErrorMessage = "[Task Error] : Task starting month cannot be greater than the Work-Package ending one!!!";
                // ending month
                else if (Integer.parseInt(taskEndingMonth) < 1)
                    taskErrorMessage = "[Task Error] : Task ending month must be greater than 1!!!";
                else if (Integer.parseInt(taskEndingMonth) < Integer.parseInt(taskStartingMonth))
                    taskErrorMessage = "[Task Error] : Task ending month cannot be smaller than the starting one!!!";
                else if (Integer.parseInt(taskEndingMonth) > workPackage.getEndingMonth())
                    taskErrorMessage = "[Task Error] : Task ending month cannot be greater than the Work-Package one!!!";
            } catch (NumberFormatException exception) {
                taskErrorMessage = "Task starting/ending months must be a number!!!";
            } catch (SQLException exception) {
                taskErrorMessage ="[DB Error] : " + exception.getMessage();
            }
        }

        // form error
        if (taskErrorMessage != null) {
            reloadWebPage(taskErrorMessage,
                    taskWPValues, taskTitle, taskStartingMonth, taskEndingMonth,
                    request, response
            );
            return;
        }

        // DB-side validation
        try {
            // check if Task already exists
            if (TaskDAO.existsTask(((User) session.getAttribute("user")).getUsername(), projectTitle, workPackageTitle, taskTitle)) {
                reloadWebPage("[Task Error] : Task with title \"" + taskTitle + "\" already exists for Work-Package \"" + workPackageTitle + "\" in the Project \"" + projectTitle + "\"!!!",
                        taskWPValues, taskTitle, taskStartingMonth, taskEndingMonth,
                        request, response
                );
            } else {
                // create the new Task
                if (TaskDAO.addNew(
                        ((User) session.getAttribute("user")).getUsername(), projectTitle, workPackageTitle, taskTitle,
                        Integer.parseInt(taskStartingMonth), Integer.parseInt(taskEndingMonth)
                )) {
                    response.sendRedirect(request.getContextPath() + "/homeAdmin");
                } else {
                    reloadWebPage("[DB Error] : Creation of Task \"" + taskTitle + "\" for the Work-Package \"" + workPackageTitle + "\" for the Project \"" + projectTitle + "\" was not successfully!!! " +
                                    "Check for all the constraints to be valid!!!",
                            taskWPValues, taskTitle, taskStartingMonth, taskEndingMonth,
                            request, response
                    );
                }
            }
        } catch (SQLException exception) {
            reloadWebPage("[DB Error] : " + exception.getMessage(),
                    taskWPValues, taskTitle, taskStartingMonth, taskEndingMonth,
                    request, response
            );
        }
    }

    // reload the Web Page showing the Error
    private void reloadWebPage(String taskErrorMessage, String taskWPValues, String taskTitle, String taskStartingMonth, String taskEndingMonth,
                               HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        session.setAttribute("taskErrorMessage", taskErrorMessage);
        session.setAttribute("taskWorkPackageTitles", taskWPValues);
        session.setAttribute("taskTitle", taskTitle);
        session.setAttribute("taskStartingMonth", taskStartingMonth);
        session.setAttribute("taskEndingMonth", taskEndingMonth);
        response.sendRedirect(request.getContextPath() + "/homeAdmin");
    }
}
