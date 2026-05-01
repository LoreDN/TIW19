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
            "WHERE title = ?";
    private static final String insertNew =
            "INSERT INTO PROJECTS " +
            "(title, monthsNumber, admin, manager) " +
            "VALUES (?, ?, ?, ?)";
    private static final String queryTitle =
            "SELECT title, monthsNumber, status " +
            "FROM PROJECTS " +
            "WHERE title = ?";
    private static final String queryCreated =
            "SELECT title, monthsNumber, status " +
            "FROM PROJECTS " +
            "WHERE status = 'CREATED'";

    /**
     * Constructor of the Project DAO.
     */
    public ProjectDAO() {}

    /**
     * Check if a Project already exists.
     * <br>This method checks if a Project already exists, by query for the title.
     *
     * @param projectTitle Title of the Project to check.
     *
     * @return True if the Project already exists, false otherwise.
     *
     * @throws SQLException DB error
     */
    public boolean existProject(String projectTitle) throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(queryExists)) {

            // set the parametric Query
            statement.setString(1, projectTitle);

            // get the Project
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt("COUNT(*)") > 0;
            }
        }
    }

    /**
     * Add a new Project.
     * <br>This method adds a new Project into the DB.
     *
     * @param projectTitle      Title of the Project.
     * @param projectDuration   Duration (in months) of the Project.
     * @param projectAdmin      Admin of the Project.
     * @param projectManager    Manager of the Project.
     *
     * @throws SQLException     DB error
     */
    public void addNew(String projectTitle, int projectDuration, String projectAdmin, String projectManager) throws SQLException {
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
     * <br>This method gets a Project only from its title.
     *
     * @param title Title of the Project
     *
     * @return Project corresponding to the title.
     * @throws SQLException DB error
     */
    public Project getFromTitle(String title) throws SQLException {
        Project project = null;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(queryTitle)) {

            // set the parametric query
            statement.setString(1, title);

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
     * <br>This method gets all the Projects with the status CREATED.
     *
     * @return Projects with Project.status == CREATED.
     * @throws SQLException DB error
     */
    public List<Project> getCreatedProjects() throws SQLException {
        List<Project> projects = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             ResultSet resultSet = connection.prepareStatement(queryCreated).executeQuery()) {
            while (resultSet.next()) {
                projects.add(new Project(
                        resultSet.getString("TITLE"),
                        resultSet.getInt("MONTHSNUMBER"),
                        resultSet.getString("STATUS")
                ));
            }
        }
        return projects;
    }
}
