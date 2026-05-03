package tiw19.servletVersion.dao;

import tiw19.servletVersion.db.DBConnection;
import tiw19.servletVersion.model.Project;

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
    private static final String queryExists =
            "SELECT count(*) " +
            "FROM PROJECTS " +
            "WHERE title = ? AND admin = ?";
    private static final String insertNew =
            "INSERT INTO PROJECTS " +
            "(title, monthsNumber, admin, manager) " +
            "VALUES (?, ?, ?, ?)";
    private static final String queryTitle =
            "SELECT title, monthsNumber, status " +
            "FROM PROJECTS " +
            "WHERE title = ? AND admin = ?";
    private static final String queryID =
            "SELECT title, monthsNumber, status " +
            "FROM PROJECTS " +
            "WHERE ID = ?";
    private static final String queryCreated =
            "SELECT title, monthsNumber, status " +
            "FROM PROJECTS " +
            "WHERE status = 'CREATED' AND admin = ?";

    /**
     * Check if a Project already exists.
     * <br>This static method checks if a Project already exists, by query for the title.
     *
     * @param admin         Admin of the Project to check.
     * @param projectTitle  Title of the Project to check.
     *
     * @return True if the Project already exists, false otherwise.
     * @throws SQLException DB error
     */
    public static boolean existProject(String admin, String projectTitle) throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(queryExists)) {

            // set the parametric Query
            statement.setString(1, projectTitle);
            statement.setString(2, admin);

            // get the Project
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt("COUNT(*)") > 0;
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
    public static void addNew(String projectTitle, int projectDuration, String projectAdmin, String projectManager) throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(insertNew)) {

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
     * @param admin Admin of the Project.
     * @param title Title of the Project
     *
     * @return Project corresponding to the title.
     * @throws SQLException DB error
     */
    public static Project getFromTitle(String admin, String title) throws SQLException {
        Project project = null;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(queryTitle)) {

            // set the parametric query
            statement.setString(1, title);
            statement.setString(2, admin);

            // get the User
            try (ResultSet queryResult = statement.executeQuery()) {
                if (queryResult.next()) {
                    project = new Project(
                            queryResult.getString("TITLE"),
                            Integer.parseInt(queryResult.getString("MONTHSNUMBER")),
                            queryResult.getString("STATUS")
                    );
                }
            }
        }
        return project;
    }

    /**
     * Get a Project by its ID.
     * <br>This static method gets a Project only from its ID.
     *
     * @param ID ID of the Project
     *
     * @return Project corresponding to the title.
     * @throws SQLException DB error
     */
    public static Project getFromID(int ID) throws SQLException {
        Project project = null;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(queryID)) {

            // set the parametric query
            statement.setInt(1, ID);

            // get the User
            try (ResultSet queryResult = statement.executeQuery()) {
                if (queryResult.next()) {
                    project = new Project(
                            queryResult.getString("TITLE"),
                            Integer.parseInt(queryResult.getString("MONTHSNUMBER")),
                            queryResult.getString("STATUS")
                    );
                }
            }
        }
        return project;
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
    public static List<Project> getCreatedProjects(String admin) throws SQLException {
        List<Project> projects = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(queryCreated)) {

            // set the parametric query
            statement.setString(1, admin);

            // get the Projects
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    projects.add(new Project(
                            resultSet.getString("TITLE"),
                            resultSet.getInt("MONTHSNUMBER"),
                            resultSet.getString("STATUS")
                    ));
                }
            }
        }
        return projects;
    }
}
