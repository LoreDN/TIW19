package tiw19.servletVersion.dao;

import tiw19.servletVersion.db.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Work-Package Data-Access-Object.
 * <br>This Class works as the DAO for the WORK_PACKAGES Table of the DB.
 *
 * @author LoreDN - Lorenzo Di Napoli
 * @version 1.0
 * @since 1.0
 */
public class WorkPackageDAO {
    private static final String queryExists =
            "SELECT count(*) " +
            "FROM WORK_PACKAGES JOIN PROJECTS ON WORK_PACKAGES.projectID = PROJECTS.ID " +
            "WHERE PROJECTS.title = ? AND WORK_PACKAGES.title = ?";
    private static final String queryProjectID =
            "SELECT id " +
            "FROM PROJECTS " +
            "WHERE PROJECTS.title = ?";
    private static final String queryWorkPackagesNumber =
            "SELECT count(*) " +
            "FROM WORK_PACKAGES " +
            "WHERE projectID = ?";
    private static final String insertNew =
            "INSERT INTO WORK_PACKAGES " +
            "(orderNumber, title, startingMonth, endingMonth, projectID) " +
            "VALUES (?, ?, ?, ?, ?)";

    /**
     * Constructor of the Work-Package DAO.
     */
    public WorkPackageDAO() {}

    /**
     * Check if a Work-Package already exists.
     * <br>This method checks if a Work-Package already exists, by query for the title.
     *
     * @param projectTitle Title of the Project referring to the Work-Package.
     * @param workPackageTitle Title of the Work-Package to check.
     *
     * @return True if the Work-Package already exists, false otherwise.
     *
     * @throws SQLException DB error
     */
    public boolean existsWorkPackage(String projectTitle, String workPackageTitle) throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(queryExists)) {

            // set the parametric Query
            statement.setString(1, projectTitle);
            statement.setString(2, workPackageTitle);

            // get the Project
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt("COUNT(*)") > 0;
            }
        }
    }

    /**
     * Add a new Work-Package.
     * <br>This method adds a new Work-Package into the DB.
     *
     * @param projectTitle              Title of the Project referring to the Work-Package.
     * @param workPackageTitle          Title of the Work-Package.
     * @param workPackageStartingMonth  Starting month of the Work-Package.
     * @param workPackageEndingMonth    Ending month of the Work-Package.
     *
     * @throws SQLException DB error
     */
    public void addNew(String projectTitle, String workPackageTitle, int workPackageStartingMonth, int workPackageEndingMonth) throws SQLException {
        // get the projectID
        int projectID;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(queryProjectID)) {
            statement.setString(1, projectTitle);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                projectID = resultSet.getInt("ID");
            }
        }

        // get the number of Work-Packages
        int workPackagesNumber;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(queryWorkPackagesNumber)) {
            statement.setInt(1, projectID);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                workPackagesNumber = resultSet.getInt("COUNT(*)");
            }
        }

        // add new Work-Package
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(insertNew)) {

            // set the parametric query
            statement.setInt(1, workPackagesNumber + 1);
            statement.setString(2, workPackageTitle);
            statement.setInt(3, workPackageStartingMonth);
            statement.setInt(4, workPackageEndingMonth);
            statement.setInt(5, projectID);

            // update DB
            statement.executeUpdate();
        }
    }
}
