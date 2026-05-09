package tiw19.servletVersion.dao;

import tiw19.servletVersion.db.DBConnection;
import tiw19.servletVersion.model.Project;
import tiw19.servletVersion.model.User;
import tiw19.servletVersion.model.WorkPackage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.List;
import java.util.ArrayList;

/**
 * Project Data-Access-Object.
 * <br>This Class works as the DAO for the PROJECTS Table of the DB.
 *
 * @author LoreDN - Lorenzo Di Napoli
 * @version 1.0
 * @since 1.0
 */
public class ProjectDAO {

    /**
     * Check if a Project already exists.
     * <br>This static method checks if a Project already exists, by query for the title.
     *
     * @param username      Admin/Manager of the Project to check.
     * @param projectTitle  Title of the Project to check.
     *
     * @return True if the Project already exists, false otherwise.
     * @throws SQLException DB error
     */
    public static synchronized boolean existProject(String username, String projectTitle) throws SQLException {
        String query;
        User user = UserDAO.getFromUsername(username);
        if (user == null) return true;
        else if (user.isAdmin()) {
            query = "SELECT count(*) " +
                    "FROM PROJECTS " +
                    "WHERE title = ? AND admin = ?";
        } else if (user.isManager()) {
            query = "SELECT count(*) " +
                    "FROM PROJECTS " +
                    "WHERE title = ? AND manager = ?";
        } else return true;

        // connect to the DB
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            // set the parametric Query
            statement.setString(1, projectTitle);
            statement.setString(2, username);

            // get the Project
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) return resultSet.getInt("COUNT(*)") > 0;
                else return true;
            }
        }
    }

    /**
     * Add a new Project.
     * <br>This static method adds a new Project into the DB.
     *
     * @param projectTitle      Title of the Project.
     * @param projectDuration   Duration (in months) of the Project.
     * @param projectAdmin      Admin of the Project.
     * @param projectManager    Manager of the Project.
     *
     * @throws SQLException     DB error
     */
    public static synchronized void addNew(String projectTitle, int projectDuration, String projectAdmin, String projectManager) throws SQLException {
        String query = "INSERT INTO PROJECTS " +
                "(title, monthsNumber, admin, manager) " +
                "VALUES (?, ?, ?, ?)";

        // connect to the DB
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            // set the parametric query
            statement.setString(1, projectTitle);
            statement.setInt(2, projectDuration);
            statement.setString(3, projectAdmin);
            statement.setString(4, projectManager);

            // update DB
            statement.executeUpdate();
        }
    }

    /**
     * Get a Project by its title.
     * <br>This static method gets a Project only from its title.
     *
     * @param username  Admin/Manager of the Project.
     * @param title     Title of the Project.
     *
     * @return Project corresponding to the title.
     * @throws SQLException DB error
     */
    public static synchronized Project getFromTitle(String username, String title) throws SQLException {
        String query;
        User user = UserDAO.getFromUsername(username);
        if (user == null) return null;
        else if (user.isAdmin()) {
            query = "SELECT ID, title, monthsNumber, status, admin " +
                    "FROM PROJECTS " +
                    "WHERE title = ? AND admin = ?";
        } else if (user.isManager()) {
            query = "SELECT title, monthsNumber, status, admin " +
                    "FROM PROJECTS " +
                    "WHERE title = ? AND manager = ?";
        } else return null;

        // connect to the DB
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            // set the parametric query
            statement.setString(1, title);
            statement.setString(2, username);

            // get the User
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new Project(
                            resultSet.getInt("ID"),
                            resultSet.getString("TITLE"),
                            resultSet.getInt("MONTHSNUMBER"),
                            resultSet.getString("ADMIN"),
                            resultSet.getString("STATUS")
                    );
                } else return null;
            }
        }
    }

    /**
     * Get a Project by its ID.
     * <br>This static method gets a Project only from its ID.
     *
     * @param ID ID of the Project.
     *
     * @return Project corresponding to the ID.
     * @throws SQLException DB error
     */
    public static synchronized Project getFromID(int ID) throws SQLException {
        String query = "SELECT title, monthsNumber, status, admin " +
                "FROM PROJECTS " +
                "WHERE ID = ?";

        // connect to the DB
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            // set the parametric query
            statement.setInt(1, ID);

            // get the User
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new Project(
                            ID,
                            resultSet.getString("TITLE"),
                            resultSet.getInt("MONTHSNUMBER"),
                            resultSet.getString("ADMIN"),
                            resultSet.getString("STATUS")
                    );
                } else return null;
            }
        }
    }

    /**
     * Get all Project with status CREATED.
     * <br>This static method gets all the Projects with the status CREATED.
     *
     * @param admin Admin of the Projects.
     *
     * @return Projects with Project.status == CREATED.
     * @throws SQLException DB error
     */
    public static synchronized List<Project> getCreatedProjects(String admin) throws SQLException {
        List<Project> projects = new ArrayList<>();
        User user = UserDAO.getFromUsername(admin);
        if (user == null) return projects;
        if (!user.isAdmin()) return projects;
        String query = "SELECT ID, title, monthsNumber, status, admin " +
                "FROM PROJECTS " +
                "WHERE status = 'CREATED' AND admin = ?";

        // connect to the DB
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            // set the parametric query
            statement.setString(1, admin);

            // get the Projects
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    projects.add(new Project(
                            resultSet.getInt("ID"),
                            resultSet.getString("TITLE"),
                            resultSet.getInt("MONTHSNUMBER"),
                            resultSet.getString("ADMIN"),
                            resultSet.getString("STATUS")
                    ));
                }
            }
        }
        return projects;
    }

    /**
     * Get all Project to be Assigned.
     * <br>This static method gets all the Projects that has to yet be Assigned.
     *
     * @param manager Manager of the Projects.
     *
     * @return Projects with Project.status == CREATED.
     * @throws SQLException DB error
     */
    public static synchronized List<Project> getToBeAssignedProjects(String manager) throws SQLException {
        List<Project> projects = new ArrayList<>();
        User user = UserDAO.getFromUsername(manager);
        if (user == null) return projects;
        if (!user.isManager()) return projects;
        String query = "SELECT ID, title, monthsNumber, status, admin " +
                "FROM PROJECTS " +
                "WHERE status = 'CREATED' AND manager = ?";

        // connect to the DB
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            // set the parametric query
            statement.setString(1, manager);

            // get the Projects
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    projects.add(new Project(
                            resultSet.getInt("ID"),
                            resultSet.getString("TITLE"),
                            resultSet.getInt("MONTHSNUMBER"),
                            resultSet.getString("ADMIN"),
                            resultSet.getString("STATUS")
                    ));
                }
            }
        }
        return projects;
    }

    /**
     * Check if a Project can be assigned.
     * <br>This static method check if Project is valid to be assigned.
     *
     * @param manager       Manager of the project.
     * @param projectTitle  Title of the Project.
     *
     * @return true if a Project can be assigned, false otherwise.
     * @throws SQLException DB error
     */
    public static synchronized boolean canBeAssigned(String manager, String projectTitle) throws SQLException {
        User user = UserDAO.getFromUsername(manager);
        if (user == null || !user.isManager()) return false;

        // check Project
        Project project = getFromTitle(manager, projectTitle);
        if (project == null) return false;
        int projectID = project.getID();
        for (int month = 1; month <= project.getMonthsNumber(); month++) {
            if (!existsSubWorkPackages(projectID, month)) return false;
        }

        // check Work-Packages
        List<Integer> workPackagesID = WorkPackageDAO.getIDs(manager, projectTitle);
        for (Integer workPackageID : workPackagesID) {
            WorkPackage workPackage = WorkPackageDAO.getFromID(workPackageID);
            if (workPackage == null) return false;
            for (int month = workPackage.getStartingMonth(); month <= workPackage.getEndingMonth(); month++) {
                if (!WorkPackageDAO.existsSubTasks(workPackageID, month)) return false;
            }
        }

        // check Tasks
        List<Integer> tasksID = TaskDAO.getIDs(manager, projectTitle);
        for (Integer taskID : tasksID) {
            if (!TaskDAO.isFullyAssigned(taskID)) return false;
        }
        return true;
    }

    /**
     * Check if at least one sub Work-Package exists for the Month.
     *
     * @param projectID ID of the Project to check.
     * @param month     Month to check.
     *
     * @return True if at least one Work-Package exists, false otherwise.
     * @throws SQLException DB error
     */
    public static synchronized boolean existsSubWorkPackages(int projectID, int month) throws SQLException {
        String query = "SELECT count(*) " +
                "FROM PROJECTS AS P " +
                "JOIN WORK_PACKAGES AS WP ON P.ID = WP.projectID " +
                "WHERE P.ID = ? AND ? BETWEEN WP.startingMonth AND WP.endingMonth";

        // connect to the DB
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            // set the parametric query
            statement.setInt(1, projectID);
            statement.setInt(2, month);

            // check the Work-Package
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) return resultSet.getInt("COUNT(*)") > 0;
                return false;
            }
        }
    }

    /**
     * Set Project status to ASSIGNED.
     *
     * @param manager       Manager of the Project.
     * @param projectTitle  Title of the Project.
     *
     * @return True if assignment was successfully, false otherwise.
     * @throws SQLException DB error
     */
    public static synchronized boolean setToAssigned(String manager, String projectTitle) throws SQLException {
        User user = UserDAO.getFromUsername(manager);
        if (user == null || !user.isManager()) return false;
        String updateAssign = "UPDATE PROJECTS " +
                "SET status = 'ASSIGNED' " +
                "WHERE title = ? AND manager = ?";

        // connect to the DB
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(updateAssign)) {

            // set the parametric query
            statement.setString(1, projectTitle);
            statement.setString(2, manager);

            // update the DB
            statement.executeUpdate();
        }
        return true;
    }

    /**
     * Get all Project with status ASSIGNED.
     * <br>This static method gets all the Projects with the status CREATED.
     *
     * @param collaborator Collaborator to the Tasks (and the Projects).
     *
     * @return Projects with Project.status == ASSIGNED.
     * @throws SQLException DB error
     */
    public static synchronized List<Project> getAssignedProjects(String collaborator) throws SQLException {
        List<Project> projects = new ArrayList<>();
        User user = UserDAO.getFromUsername(collaborator);
        if (user == null || !user.isCollaborator()) return projects;
        String query = "SELECT P.ID, P.title, P.monthsNumber, P.admin, P.status " +
                "FROM PROJECTS AS P " +
                "WHERE P.status = 'ASSIGNED' AND EXISTS (" +
                    "SELECT 1 " +
                    "FROM WORK_PACKAGES AS WP " +
                    "JOIN TASKS AS T ON T.workPackageID = WP.ID " +
                    "JOIN COLLABORATORS AS C ON C.taskID = T.ID " +
                    "WHERE WP.projectID = P.ID AND C.userID = ?" +
                ")";

        // connect to the DB
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            // set parametric query
            statement.setInt(1, UserDAO.getID(collaborator));

            // get the Projects
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    projects.add(new Project(
                            resultSet.getInt("ID"),
                            resultSet.getString("TITLE"),
                            Integer.parseInt(resultSet.getString("MONTHSNUMBER")),
                            resultSet.getString("ADMIN"),
                            resultSet.getString("STATUS")
                    ));
                }
            }
        }
        return projects;
    }
}
