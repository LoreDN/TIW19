package tiw19.servletVersion.dao;

import tiw19.servletVersion.db.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
}
