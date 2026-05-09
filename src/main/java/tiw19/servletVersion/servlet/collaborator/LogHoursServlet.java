package tiw19.servletVersion.servlet.collaborator;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import tiw19.servletVersion.dao.CollaboratorDAO;
import tiw19.servletVersion.dao.PlanningDAO;
import tiw19.servletVersion.dao.ProjectDAO;
import tiw19.servletVersion.dao.TaskDAO;
import tiw19.servletVersion.model.Project;
import tiw19.servletVersion.model.Task;
import tiw19.servletVersion.model.User;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Servlet for the Log of Worked Hours.
 * <br>This Servlet handles the log of the Worked Hours by a Collaborator user for a triplet (Task, Month, Collaborator), checking if all the fields are valid.
 * <br>This Servlet operates on the form of the "/homeCollaborator.html" Web Page.
 *
 * @author LoreDN - Lorenzo Di Napoli
 * @version 1.0
 * @since 1.0
 */
@WebServlet("/homeCollaborator/logHours")
public class LogHoursServlet extends HttpServlet {

    /**
     * @see HttpServlet Redirect to "/homeCollaborator".
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // never access it directly
        response.sendRedirect(request.getContextPath() + "/homeCollaborator");
    }

    /**
     * HTTP POST request Handler.
     * <br>This method handles an HTTP POST request, checking if the triplet (Task, Month, Collaborator) is valid to be logged.
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
        String projectID = request.getParameter("projectID");
        String taskID = request.getParameter("taskID");
        String selectedMonth = request.getParameter("month");
        String workedHours = request.getParameter("hours");

        // check user
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null || !((User) session.getAttribute("user")).isCollaborator()) {
            // fallback to "/login"
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        // Server-side validation
        String hoursErrorMessage = null;
        if (projectID == null || projectID.isBlank() ||
                taskID == null || taskID.isBlank() ||
                selectedMonth == null || selectedMonth.isBlank() ||
                workedHours == null || workedHours.isBlank()
        ) {
            hoursErrorMessage = "All fields are required!!!";
        } else {
            try {
                Project project = ProjectDAO.getFromID(Integer.parseInt(projectID));
                Task task = TaskDAO.getFromID(Integer.parseInt(taskID));
                // hidden parameters
                if (project == null) hoursErrorMessage = "[Hours Error] : Project not found!!!";
                else if (task == null) hoursErrorMessage = "[Hours Error] : Task not found!!!";
                    // month
                else if (Integer.parseInt(selectedMonth) < task.getStartingMonth())
                    hoursErrorMessage = "[Hours Error] : Month cannot be smaller than the Task starting one!!!";
                else if (Integer.parseInt(selectedMonth) > task.getEndingMonth())
                    hoursErrorMessage = "[Hours Error] : Month cannot be greater than the Task ending one!!!";
                    // worked hours
                else if (Integer.parseInt(workedHours) < 0)
                    hoursErrorMessage = "[Hours Error] : Worked hours must be at least 0!!!";
            } catch (NumberFormatException exception) {
                hoursErrorMessage = "Month/Hours must be a number!!!";
            } catch (SQLException exception) {
                hoursErrorMessage ="[DB Error] : " + exception.getMessage();
            }
        }

        // form error
        if (hoursErrorMessage != null) {
            reloadWebPage(hoursErrorMessage,
                    selectedMonth, workedHours,
                    request, response
            );
            return;
        }

        // DB-side validation
        try {
            int ID = Integer.parseInt(taskID);
            int month = Integer.parseInt(selectedMonth);
            // log the Worked Hours
            CollaboratorDAO.logWorkedHours(
                    ((User) session.getAttribute("user")).getID(),
                    ID, month, Integer.parseInt(workedHours)
            );
            PlanningDAO.updatePlanning(ID, month);
            session.setAttribute("hoursSuccessMessage", "[Hours Success] : Worked Hours have ben logged successfully for the month M" + selectedMonth + "!!!");
            response.sendRedirect(request.getContextPath() + "/homeCollaborator" +
                    "?projectID=" + request.getParameter("projectID") +
                    "&taskID=" + request.getParameter("taskID")
            );
        } catch (SQLException exception) {
            reloadWebPage("[DB Error] : " + exception.getMessage(),
                    selectedMonth, workedHours,
                    request, response
            );
        }
    }

    // reload the Web Page showing the Error
    private void reloadWebPage(String hoursErrorMessage, String selectedMonth, String workedHours,
                               HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        session.setAttribute("hoursErrorMessage", hoursErrorMessage);
        session.setAttribute("selectedMonth", selectedMonth);
        session.setAttribute("workedHours", workedHours);
        response.sendRedirect(request.getContextPath() + "/homeCollaborator" +
                "?projectID=" + request.getParameter("projectID") +
                "&taskID=" + request.getParameter("taskID")
        );
    }
}
