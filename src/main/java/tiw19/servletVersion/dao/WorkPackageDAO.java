package tiw19.servletVersion.dao;

import tiw19.servletVersion.db.DBConnection;
import tiw19.servletVersion.model.WorkPackage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.List;
import java.util.ArrayList;

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
            "FROM WORK_PACKAGES AS WP JOIN PROJECTS AS P ON WP.projectID = P.ID " +
            "WHERE P.title = ? AND P.admin = ? AND WP.title = ?";
    private static final String queryProjectID =
            "SELECT ID " +
            "FROM PROJECTS " +
            "WHERE title = ? and admin = ?";
    private static final String queryWorkPackagesNumber =
            "SELECT count(*) " +
            "FROM WORK_PACKAGES " +
            "WHERE projectID = ?";
    private static final String insertNew =
            "INSERT INTO WORK_PACKAGES " +
            "(orderNumber, title, startingMonth, endingMonth, projectID) " +
            "VALUES (?, ?, ?, ?, ?)";
    private static final String queryAll =
            "SELECT WP.title, WP.startingMonth, WP.endingMonth, WP.projectID " +
            "FROM WORK_PACKAGES AS WP JOIN PROJECTS AS P ON WP.projectID = P.ID " +
            "WHERE P.admin = ?";
    private static final String queryTitle =
            "SELECT WP.title, WP.startingMonth, WP.endingMonth, WP.projectID " +
            "FROM WORK_PACKAGES AS WP JOIN PROJECTS AS P ON WP.projectID = P.ID " +
            "WHERE P.title = ? AND P.admin = ? AND WP.title = ?";
    private static final String queryID =
            "SELECT title, startingMonth, endingMonth, projectID " +
            "FROM WORK_PACKAGES " +
            "WHERE ID = ?";

    /**
     * Check if a Work-Package already exists.
     * <br>This static method checks if a Work-Package already exists, by query for the title.
     *
     * @param admin             Admin of the Project referring to the Work-Package.
     * @param projectTitle      Title of the Project referring to the Work-Package.
     * @param workPackageTitle  Title of the Work-Package to check.
     *
     * @return True if the Work-Package already exists, false otherwise.
     * @throws SQLException DB error
     */
    public static boolean existsWorkPackage(String admin, String projectTitle, String workPackageTitle) throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(queryExists)) {

            // set the parametric Query
            statement.setString(1, projectTitle);
            statement.setString(2, admin);
            statement.setString(3, workPackageTitle);

            // get the Project
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt("COUNT(*)") > 0;
            }
        }
    }

    /**
     * Add a new Work-Package.
     * <br>This static method adds a new Work-Package into the DB.
     *
     * @param admin                     Admin of the Project referring to the Work-Package.
     * @param projectTitle              Title of the Project referring to the Work-Package.
     * @param workPackageTitle          Title of the Work-Package.
     * @param workPackageStartingMonth  Starting month of the Work-Package.
     * @param workPackageEndingMonth    Ending month of the Work-Package.
     *
     * @throws SQLException DB error
     */
    public static void addNew(String admin, String projectTitle, String workPackageTitle, int workPackageStartingMonth, int workPackageEndingMonth) throws SQLException {
        // get the projectID
        int projectID;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(queryProjectID)) {
            statement.setString(1, projectTitle);
            statement.setString(2, admin);
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

    /**
     * Get all the Work-Packages.
     *
     * @param admin Admin of the Projects (and Work-Packages).
     *
     * @return All Work-Packages.
     * @throws SQLException DB error
     */
    public static List<WorkPackage> getAll(String admin) throws SQLException {
        List<WorkPackage> workPackages = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(queryAll)) {

            // set the parametric query
            statement.setString(1, admin);

            // get the Work-Packages
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    workPackages.add(new WorkPackage(
                            resultSet.getString("TITLE"),
                            Integer.parseInt(resultSet.getString("STARTINGMONTH")),
                            Integer.parseInt(resultSet.getString("ENDINGMONTH")),
                            ProjectDAO.getFromID(Integer.parseInt(resultSet.getString("PROJECTID")))
                    ));
                }
            }
        }
        return workPackages;
    }

    /**
     * Get the Work-Package by its title.
     * <br>This static method gets a Work-Package only from its title (and the Project's one).
     *
     * @param admin             Admin of the Project.
     * @param projectTitle      Title of the Project.
     * @param workPackageTitle  Title of the Work-Package.
     *
     * @return Work-Packages corresponding to the title.
     * @throws SQLException DB error
     */
    public static WorkPackage getFromTitle(String admin, String projectTitle, String workPackageTitle) throws SQLException {
        WorkPackage workPackage;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(queryTitle)) {

            // set the parametric query
            statement.setString(1, projectTitle);
            statement.setString(2, admin);
            statement.setString(3, workPackageTitle);

            // get the Work-Package
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                workPackage = new WorkPackage(
                        resultSet.getString("TITLE"),
                        Integer.parseInt(resultSet.getString("STARTINGMONTH")),
                        Integer.parseInt(resultSet.getString("ENDINGMONTH")),
                        ProjectDAO.getFromID(Integer.parseInt(resultSet.getString("PROJECTID")))
                );
            }
        }
        return workPackage;
    }

    /**
     * Get a Work-Package by its ID.
     * <br>This static method gets a Work-Package only from its ID.
     *
     * @param ID ID of the Work-Package.
     *
     * @return Work-Package corresponding to the ID.
     * @throws SQLException DB error
     */
    public static WorkPackage getFromID(int ID) throws SQLException {
        WorkPackage workPackage = null;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(queryID)) {

            // set the parametric query
            statement.setInt(1, ID);

            // get the User
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    workPackage = new WorkPackage(
                            resultSet.getString("TITLE"),
                            Integer.parseInt(resultSet.getString("STARTINGMONTH")),
                            Integer.parseInt(resultSet.getString("ENDINGMONTH")),
                            ProjectDAO.getFromID(Integer.parseInt(resultSet.getString("PROJECTID")))
                    );
                }
            }
        }
        return workPackage;
    }
}
