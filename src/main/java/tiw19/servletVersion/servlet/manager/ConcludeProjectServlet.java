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
 * Servlet for the Project Conclusion.
 * <br>This Servlet handles the conclusion of a Project by a Manager user, checking if all the fields are valid.
 * <br>This Servlet operates on the button of the "/homeManager/projetMonitoring.html" Web Page.
 *
 * @author LoreDN - Lorenzo Di Napoli
 * @version 1.0
 * @since 1.0
 */
@WebServlet("/homeManager/projectMonitoring/concludeProject")
public class ConcludeProjectServlet extends HttpServlet {

    /**
     * @see HttpServlet Redirect to "/homeManager/projectMonitoring".
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // never access it directly
        response.sendRedirect(request.getContextPath() + "/homeManager/projectMonitoring");
    }

    /**
     * HTTP POST request Handler.
     * <br>This method handles an HTTP POST request, checking if the Project is valid to be concluded.
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
        String projectId = request.getParameter("projectID");

        // check user
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null || !((User) session.getAttribute("user")).isManager()) {
            // fallback to "/login"
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        // Server-side validation
        String conclusionErrorMessage = null;
        int ID = Integer.parseInt(projectId);
        Project selectedProject = null;
        try {
            selectedProject = ProjectDAO.getFromID(ID);
            if (selectedProject == null) conclusionErrorMessage = "[Conclusion Error] : Project not found!!!";
        } catch (SQLException exception) {
            conclusionErrorMessage = "[DB Error] : " + exception.getMessage();
        }

        // form error
        if (conclusionErrorMessage != null) {
            reloadWebPage(conclusionErrorMessage, request, response);
            return;
        }

        // DB-side validation
        try {
            // check if Project can be concluded
            if (ProjectDAO.canBeConcluded(ID)) {
                if (ProjectDAO.setToConcluded(ID)) {
                    session.setAttribute("conclusionSuccessMessage", "[Conclusion Success] : Project \"" + selectedProject.getTitle() + "\" has been marked as CONCLUDED successfully!!!");
                    response.sendRedirect(request.getContextPath() + "/homeManager/projectMonitoring");
                } else {
                    reloadWebPage("[DB Error] : Conclusion of the Project \"" + selectedProject.getTitle() + "\" was not successful!!! " +
                                    "Check for all the constraints to be valid!!!",
                            request, response
                    );
                }
            } else {
                reloadWebPage("[Conclusion Error] : Project cannot be concluded, check if all Task have been completed!!!",
                        request, response
                );
            }
        } catch (SQLException exception) {
            reloadWebPage("[DB Error] : " + exception.getMessage(),
                    request, response
            );
        }
    }

    // reload the Web Page showing the Error
    private void reloadWebPage(String conclusionErrorMessage, HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        session.setAttribute("conclusionErrorMessage", conclusionErrorMessage);
        response.sendRedirect(request.getContextPath() + "/homeManager/projectMonitoring" +
                "?projectID=" + request.getParameter("projectID")
        );
    }
}
