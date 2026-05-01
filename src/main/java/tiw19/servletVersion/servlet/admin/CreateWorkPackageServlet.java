package tiw19.servletVersion.servlet.admin;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import tiw19.servletVersion.dao.ProjectDAO;
import tiw19.servletVersion.dao.WorkPackageDAO;
import tiw19.servletVersion.model.Project;
import tiw19.servletVersion.model.User;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Servlet for the Work-Package Creation.
 * <br>This Servlet handles the creation of a Work-Package by an Admin user, checking if all the fields are valid.
 * <br>This Servlet operates on the form of the "/homeAdmin.html" Web Page.
 *
 * @author LoreDN - Lorenzo Di Napoli
 * @version 1.0
 * @since 1.0
 */
@WebServlet("/homeAdmin/createWorkPackage")
public class CreateWorkPackageServlet extends HttpServlet {

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
     * <br>This method handles an HTTP POST request, checking if the Work-Package is valid to be created.
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
        String workPackageTitle = request.getParameter("title");
        String workPackageStartingMonth  = request.getParameter("startingMonth");
        String workPackageEndingMonth  = request.getParameter("endingMonth");

        // check user
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null || !((User) session.getAttribute("user")).isAdmin()) {
            // fallback to "/login"
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        // Server-side validation
        String workPackageErrorMessage = null;
        if (workPackageTitle == null || workPackageTitle.isBlank() ||
                workPackageStartingMonth == null || workPackageStartingMonth.isBlank() ||
                workPackageEndingMonth == null || workPackageEndingMonth.isBlank()
        ) {
            workPackageErrorMessage = "All fields are required!!!";
        } else {
            try {
                ProjectDAO projectDAO = new ProjectDAO();
                Project project = projectDAO.getFromTitle(projectTitle);
                // Project
                if (project == null) workPackageErrorMessage = "[Work-Package Error] : Project not found!!!";
                // starting month
                else if (Integer.parseInt(workPackageStartingMonth) < 1)
                    workPackageErrorMessage = "[Work-Package Error] : Work-Package starting month must be greater than 0!!!";
                else if (Integer.parseInt(workPackageStartingMonth) > project.getMonthsNumber())
                    workPackageErrorMessage = "[Work-Package Error] : Work-Package starting month cannot be greater than the Project duration!!!";
                // ending month
                else if (Integer.parseInt(workPackageEndingMonth) < 1)
                    workPackageErrorMessage = "[Work-Package Error] : Work-Package ending month must be greater than 0!!!";
                else if (Integer.parseInt(workPackageEndingMonth) < Integer.parseInt(workPackageStartingMonth))
                    workPackageErrorMessage = "[Work-Package Error] : Work-Package ending month cannot be smaller than the starting one!!!";
                else if (Integer.parseInt(workPackageEndingMonth) > project.getMonthsNumber())
                    workPackageErrorMessage = "[Work-Package Error] : Work-Package ending month cannot be greater than the Project duration!!!";
            } catch (NumberFormatException exception) {
                workPackageErrorMessage = "Work-Package starting/ending months must be a number!!!";
            } catch (SQLException exception) {
                workPackageErrorMessage ="[DB Error] : " + exception.getMessage();
            }
        }

        // form error
        if (workPackageErrorMessage != null) {
            reloadWebPage(workPackageErrorMessage,
                    projectTitle, workPackageTitle, workPackageStartingMonth, workPackageEndingMonth,
                    request, response
            );
            return;
        }

        // DB-side validation
        try {
            // check if Work-Package already exists
            WorkPackageDAO workPackageDAO = new WorkPackageDAO();
            if (workPackageDAO.existsWorkPackage(projectTitle, workPackageTitle)) {
                reloadWebPage("[Work-Package Error] : Work-Package with title \"" + workPackageTitle + "\" already exists for the Project \"" + projectTitle + "\"!!!",
                        projectTitle, workPackageTitle, workPackageStartingMonth, workPackageEndingMonth,
                        request, response
                );
            } else {
                // create the new Work-Package
                workPackageDAO.addNew(
                        projectTitle, workPackageTitle,
                        Integer.parseInt(workPackageStartingMonth), Integer.parseInt(workPackageEndingMonth)
                );
                response.sendRedirect(request.getContextPath() + "/homeAdmin");
            }
        } catch (SQLException exception) {
            reloadWebPage("[DB Error] : " + exception.getMessage(),
                    projectTitle, workPackageTitle, workPackageStartingMonth, workPackageEndingMonth,
                    request, response
            );
        }
    }

    // reload the Web Page showing the Error
    private void reloadWebPage(String workPackageErrorMessage, String projectTitle, String workPackageTitle, String workPackageStartingMonth, String workPackageEndingMonth,
                               HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        session.setAttribute("workPackageErrorMessage", workPackageErrorMessage);
        session.setAttribute("workPackageProjectTitle", projectTitle);
        session.setAttribute("workPackageTitle", workPackageTitle);
        session.setAttribute("workPackageStartingMonth", workPackageStartingMonth);
        session.setAttribute("workPackageEndingMonth", workPackageEndingMonth);
        response.sendRedirect(request.getContextPath() + "/homeAdmin");
    }
}
