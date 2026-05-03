package tiw19.servletVersion.servlet.admin;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import tiw19.servletVersion.dao.ProjectDAO;
import tiw19.servletVersion.dao.UserDAO;
import tiw19.servletVersion.model.User;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Servlet for the Project Creation.
 * <br>This Servlet handles the creation of a Project by an Admin user, checking if all the fields are valid.
 * <br>This Servlet operates on the form of the "/homeAdmin.html" Web Page.
 *
 * @author LoreDN - Lorenzo Di Napoli
 * @version 1.0
 * @since 1.0
 */
@WebServlet("/homeAdmin/createProject")
public class CreateProjectServlet extends HttpServlet {

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
     * <br>This method handles an HTTP POST request, checking if the Project is valid to be created.
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
        String projectTitle = request.getParameter("title");
        String projectDuration = request.getParameter("duration");
        String projectManager = request.getParameter("manager");

        // check user
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null || !((User) session.getAttribute("user")).isAdmin()) {
            // fallback to "/login"
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        // Server-side validation
        String projectErrorMessage = null;
        if (projectTitle == null || projectTitle.isBlank() ||
                projectDuration == null || projectDuration.isBlank() ||
                projectManager == null || projectManager.isBlank()
        ) {
            projectErrorMessage = "All fields are required!!!";
        } else {
            try {
                User user = UserDAO.getFromUsername(projectManager);
                // manager
                if (user == null) projectErrorMessage = "[Project Error] : User not found!!!";
                else if (!user.isManager()) projectErrorMessage = "[Project Error] : User is not manager!!!";
                // duration
                else if (Integer.parseInt(projectDuration) < 1) projectErrorMessage = "[Project Error] : Project duration (in months) must be greater than 1!!!";
            } catch (NumberFormatException exception) {
                projectErrorMessage = "Project duration must be a number!!!";
            } catch (SQLException exception) {
                projectErrorMessage = "[DB Error] : " + exception.getMessage();
            }
        }

        // form error
        if (projectErrorMessage != null) {
            reloadWebPage(projectErrorMessage,
                    projectTitle, projectDuration, projectManager,
                    request, response
            );
            return;
        }

        // DB-side validation
        try {
            if (ProjectDAO.existProject(((User) session.getAttribute("user")).getUsername(), projectTitle)) {
                reloadWebPage("[Project Error] : Project with title \"" + projectTitle + "\" already exists!!!",
                        projectTitle, projectDuration, projectManager,
                        request, response
                );
            } else {
                ProjectDAO.addNew(
                        projectTitle, Integer.parseInt(projectDuration),
                        ((User) session.getAttribute("user")).getUsername(), projectManager
                );
                response.sendRedirect(request.getContextPath() + "/homeAdmin");
            }
        } catch (SQLException exception) {
            reloadWebPage("[DB Error] : " + exception.getMessage(),
                    projectTitle, projectDuration, projectManager,
                    request, response
            );
        }
    }

    // reload the Web Page showing the Error
    private void reloadWebPage(String projectErrorMessage, String projectTitle, String projectDuration, String projectManager,
                               HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        session.setAttribute("projectErrorMessage", projectErrorMessage);
        session.setAttribute("projectTitle", projectTitle);
        session.setAttribute("projectDuration", projectDuration);
        session.setAttribute("projectManager", projectManager);
        response.sendRedirect(request.getContextPath() + "/homeAdmin");
    }
}
