package tiw19.servletVersion.servlet.manager;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import tiw19.servletVersion.dao.PlanningDAO;
import tiw19.servletVersion.dao.TaskDAO;
import tiw19.servletVersion.model.Task;
import tiw19.servletVersion.model.User;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Servlet for the couple (Task, Month) Assignment.
 * <br>This Servlet handles the assignment of a couple (Task, Month) by a Manager user, checking if all the fields are valid.
 * <br>This Servlet operates on the form of the "/homeManager.html" Web Page.
 *
 * @author LoreDN - Lorenzo Di Napoli
 * @version 1.0
 * @since 1.0
 */
@WebServlet("/homeManager/hoursAssignment")
public class HoursAssignmentServlet extends HttpServlet {

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
     * <br>This method handles an HTTP POST request, checking if the couple (Task , Month) is valid to be assigned.
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
        String plannedMonth = request.getParameter("month");
        String plannedHours = request.getParameter("hours");
        String projectTitle, workPackageTitle, taskTitle;

        // check user
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null || !((User) session.getAttribute("user")).isManager()) {
            // fallback to "/login"
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        // parse parameters
        try {
            String[] parts = taskValues.split("###");
            projectTitle = parts[1];
            workPackageTitle = parts[2];
            taskTitle = parts[3];
        } catch (Exception exception) {
            reloadWebPage("Task selection is required!!!",
                    taskValues, plannedMonth, plannedHours,
                    request, response
            );
            return;
        }

        // Server-side validation
        String hoursErrorMessage = null;
        if (plannedMonth == null || plannedMonth.isBlank() ||
                plannedHours == null || plannedHours.isBlank()
        ) {
            hoursErrorMessage = "All fields are required!!!";
        } else {
            try {
                Task task = TaskDAO.getFromTitle(((User) session.getAttribute("user")).getUsername(), projectTitle, workPackageTitle, taskTitle);
                // Task
                if (task == null) hoursErrorMessage = "[Hours Error] : Task not found!!!";
                // month
                else if (Integer.parseInt(plannedMonth) < 1)
                    hoursErrorMessage = "[Hours Error] : Month must be greater than 1!!!";
                else if (Integer.parseInt(plannedMonth) < task.getStartingMonth())
                    hoursErrorMessage = "[Hours Error] : Month cannot be smaller than the Task starting one!!!";
                else if (Integer.parseInt(plannedMonth) > task.getEndingMonth())
                    hoursErrorMessage = "[Hours Error] : Month cannot be greater than the Task ending one!!!";
                // hours
                else if (Integer.parseInt(plannedHours) < 0)
                    hoursErrorMessage = "[Hours Error] : Planned hours must be greater than 0!!!";
            } catch (NumberFormatException exception) {
                hoursErrorMessage = "Month/planned hours must be a number!!!";
            } catch (SQLException exception) {
                hoursErrorMessage = "[DB Error] : " + exception.getMessage();
            }
        }

        // form error
        if (hoursErrorMessage != null) {
            reloadWebPage(hoursErrorMessage,
                    taskValues, plannedMonth, plannedHours,
                    request, response
            );
            return;
        }

        // DB-side validation
        try {
            // check if Planning already exists
            int taskID = TaskDAO.getID(((User) session.getAttribute("user")).getUsername(), projectTitle, workPackageTitle, taskTitle);
            if (PlanningDAO.existsPlanning(taskID, Integer.parseInt(plannedMonth))) {
                reloadWebPage("[Hours Error] : Planning for couple (\"" + taskTitle + "\", " + plannedMonth + ") already exists!!!",
                        taskValues, plannedMonth, plannedHours,
                        request, response
                );
            } else {
                // create the new Planning
                PlanningDAO.addNew(taskID, Integer.parseInt(plannedMonth), Integer.parseInt(plannedHours));
                response.sendRedirect(request.getContextPath() + "/homeManager");
            }
        } catch (SQLException exception) {
            reloadWebPage("[DB Error] : " + exception.getMessage(),
                    taskValues, plannedMonth, plannedHours,
                    request, response
            );
        }
    }

    // reload the Web Page showing the Error
    private void reloadWebPage(String hoursErrorMessage, String taskValues, String plannedMonth, String plannedHours,
                               HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        session.setAttribute("hoursErrorMessage", hoursErrorMessage);
        session.setAttribute("hoursTaskTitles", taskValues);
        session.setAttribute("plannedMonth", plannedMonth);
        session.setAttribute("plannedHours", plannedHours);
        response.sendRedirect(request.getContextPath() + "/homeManager");
    }
}
