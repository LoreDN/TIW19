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
 * Work-Package Data-Access-Object.
 * <br>This Class works as the DAO for the WORK_PACKAGES Table of the DB.
 *
 * @author LoreDN - Lorenzo Di Napoli
 * @version 1.0
 * @since 1.0
 */
public class WorkPackageDAO {

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
    public static synchronized boolean existsWorkPackage(String admin, String projectTitle, String workPackageTitle) throws SQLException {
        User user = UserDAO.getFromUsername(admin);
        if (user == null || !user.isAdmin()) return true;
        String query = "SELECT count(*) " +
                "FROM WORK_PACKAGES AS WP JOIN PROJECTS AS P ON WP.projectID = P.ID " +
                "WHERE P.title = ? AND P.admin = ? AND WP.title = ?";

        // connect to the DB
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            // set the parametric Query
            statement.setString(1, projectTitle);
            statement.setString(2, admin);
            statement.setString(3, workPackageTitle);

            // get the Project
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) return resultSet.getInt("COUNT(*)") > 0;
                else return true;
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
     * @return True if the Work-Package addition is successfully, false otherwise.
     * @throws SQLException DB error
     */
    public static synchronized boolean addNew(String admin, String projectTitle, String workPackageTitle, int workPackageStartingMonth, int workPackageEndingMonth) throws SQLException {
        User user = UserDAO.getFromUsername(admin);
        if (user == null || !user.isAdmin()) return false;
        String query = "INSERT INTO WORK_PACKAGES " +
                "(orderNumber, title, startingMonth, endingMonth, projectID) " +
                "VALUES (?, ?, ?, ?, ?)";

        // connect to the DB
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            // get Project ID
            Project project = ProjectDAO.getFromTitle(admin, projectTitle);
            if (project == null) return false;
            int projectID = project.getID();

            // set the parametric query
            statement.setInt(1, getNumber(projectID) + 1);
            statement.setString(2, workPackageTitle);
            statement.setInt(3, workPackageStartingMonth);
            statement.setInt(4, workPackageEndingMonth);
            statement.setInt(5, projectID);

            // update DB
            statement.executeUpdate();
        }
        return true;
    }

    /**
     * Get the number of Work-Packages for a reference Project.
     *
     * @param projectID ID of the Project of reference.
     *
     * @return Number of Work-Packages for the reference Project.
     * @throws SQLException DB error
     */
    public static synchronized int getNumber(int projectID) throws SQLException {
        String query = "SELECT count(*) " +
                "FROM WORK_PACKAGES " +
                "WHERE projectID = ?";

        // connect to the DB
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            // set the parametric query
            statement.setInt(1, projectID);

            // get the number of Work-Packages
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) return resultSet.getInt("COUNT(*)");
                else return 0;
            }
        }
    }

    /**
     * Get a Work-Packages ID from its title.
     *
     * @param username          Admin/Manager of the Project (and the Work-Packages).
     * @param projectTitle      Title of the Project of reference.
     * @param workPackageTitle  Title of the Work-Package.
     *
     * @return WorkPackage.ID, -1 if it does not exist.
     * @throws SQLException DB error
     */
    public static synchronized int getID(String username, String projectTitle, String workPackageTitle) throws SQLException {
        String query;
        User user = UserDAO.getFromUsername(username);
        if (user == null) return -1;
        if (user.isAdmin()) {
            query = "SELECT WP.ID " +
                    "FROM WORK_PACKAGES AS WP JOIN PROJECTS AS P ON WP.projectID = P.ID " +
                    "WHERE P.admin = ? AND P.title = ? AND WP.title = ?";
        } else if (user.isManager()) {
            query = "SELECT WP.ID " +
                    "FROM WORK_PACKAGES AS WP JOIN PROJECTS AS P ON WP.projectID = P.ID " +
                    "WHERE P.manager = ? AND P.title = ? AND WP.title = ?";
        } else return -1;

        // connect to the DB
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            // set the parametric query
            statement.setString(1, username);
            statement.setString(2, projectTitle);
            statement.setString(3, workPackageTitle);

            // get the Tasks ID
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) return resultSet.getInt("ID");
                else return -1;
            }
        }
    }

    /**
     * Get all Work-Packages ID for a Project.
     *
     * @param username      Admin/Manager of the Project (and the Work-Packages).
     * @param projectTitle  Title of the Project of reference.
     *
     * @return WorkPackage.ID for a Project.
     * @throws SQLException DB error
     */
    public static synchronized List<Integer> getIDs(String username, String projectTitle) throws SQLException {
        List<Integer> workPackagesID = new ArrayList<>();
        String query;
        User user = UserDAO.getFromUsername(username);
        if (user == null) return workPackagesID;
        if (user.isAdmin()) {
            query = "SELECT WP.ID " +
                    "FROM WORK_PACKAGES AS WP JOIN PROJECTS AS P ON WP.projectID = P.ID " +
                    "WHERE P.title = ? AND P.admin = ?";
        } else if (user.isManager()) {
            query = "SELECT WP.ID " +
                    "FROM WORK_PACKAGES AS WP JOIN PROJECTS AS P ON WP.projectID = P.ID " +
                    "WHERE P.title = ? AND P.manager = ?";
        } else return workPackagesID;

        // connect to the DB
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            // set the parametric query
            statement.setString(1, projectTitle);
            statement.setString(2, username);

            // get the Tasks ID
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    workPackagesID.add(resultSet.getInt("ID"));
                }
            }
        }
        return workPackagesID;
    }

    /**
     * Get a Work-Packages orderNumber from its ID.
     *
     * @param workPackageID Title of the Work-Package.
     *
     * @return WorkPackage.orderNumber, 0 if it does not exist.
     * @throws SQLException DB error
     */
    public static synchronized int getOrderNumber(int workPackageID) throws SQLException {
        String query = "SELECT orderNumber " +
                "FROM WORK_PACKAGES " +
                "WHERE ID = ?";

        // connect to the DB
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            // set the parametric query
            statement.setInt(1, workPackageID);

            // get the number of Work-Packages
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) return resultSet.getInt("ORDERNUMBER");
                else return 0;
            }
        }
    }

    /**
     * Get all the Work-Packages.
     *
     * @param username Admin/Manager of the Projects (and Work-Packages).
     *
     * @return All Work-Packages.
     * @throws SQLException DB error
     */
    public static synchronized List<WorkPackage> getAll(String username) throws SQLException {
        List<WorkPackage> workPackages = new ArrayList<>();
        String query;
        User user = UserDAO.getFromUsername(username);
        if (user == null) return workPackages;
        if (user.isAdmin()) {
            query = "SELECT WP.ID, WP.orderNumber, WP.title, WP.startingMonth, WP.endingMonth, WP.projectID " +
                    "FROM WORK_PACKAGES AS WP JOIN PROJECTS AS P ON WP.projectID = P.ID " +
                    "WHERE P.admin = ?";
        } else if (user.isManager()) {
            query = "SELECT WP.ID, WP.orderNumber, WP.title, WP.startingMonth, WP.endingMonth, WP.projectID " +
                    "FROM WORK_PACKAGES AS WP JOIN PROJECTS AS P ON WP.projectID = P.ID " +
                    "WHERE P.manager = ?";
        } else return workPackages;

        // connect to the DB
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            // set the parametric query
            statement.setString(1, username);

            // get the Work-Packages
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    workPackages.add(new WorkPackage(
                            resultSet.getInt("ID"),
                            resultSet.getInt("ORDERNUMBER"),
                            resultSet.getString("TITLE"),
                            resultSet.getInt("STARTINGMONTH"),
                            resultSet.getInt("ENDINGMONTH"),
                            ProjectDAO.getFromID(resultSet.getInt("PROJECTID"))
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
     * @param username          Admin/Manager of the Project.
     * @param projectTitle      Title of the Project.
     * @param workPackageTitle  Title of the Work-Package.
     *
     * @return Work-Packages corresponding to the title.
     * @throws SQLException DB error
     */
    public static synchronized WorkPackage getFromTitle(String username, String projectTitle, String workPackageTitle) throws SQLException {
        String query;
        User user = UserDAO.getFromUsername(username);
        if (user == null) return null;
        if (user.isAdmin()) {
            query = "SELECT WP.ID, WP.orderNumber, WP.title, WP.startingMonth, WP.endingMonth, WP.projectID " +
                    "FROM WORK_PACKAGES AS WP JOIN PROJECTS AS P ON WP.projectID = P.ID " +
                    "WHERE P.title = ? AND P.admin = ? AND WP.title = ?";
        } else if (user.isManager()) {
            query = "SELECT WP.ID, WP.orderNumber, WP.title, WP.startingMonth, WP.endingMonth, WP.projectID " +
                    "FROM WORK_PACKAGES AS WP JOIN PROJECTS AS P ON WP.projectID = P.ID " +
                    "WHERE P.title = ? AND P.manager = ? AND WP.title = ?";
        } else return null;

        // connect to the DB
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            // set the parametric query
            statement.setString(1, projectTitle);
            statement.setString(2, username);
            statement.setString(3, workPackageTitle);

            // get the Work-Package
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new WorkPackage(
                            resultSet.getInt("ID"),
                            resultSet.getInt("ORDERNUMBER"),
                            resultSet.getString("TITLE"),
                            resultSet.getInt("STARTINGMONTH"),
                            resultSet.getInt("ENDINGMONTH"),
                            ProjectDAO.getFromID(resultSet.getInt("PROJECTID"))
                    );
                } else return null;
            }
        }
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
    public static synchronized WorkPackage getFromID(int ID) throws SQLException {
        String query = "SELECT ID, orderNumber, title, startingMonth, endingMonth, projectID " +
                "FROM WORK_PACKAGES " +
                "WHERE ID = ?";

        // connect to the DB
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            // set the parametric query
            statement.setInt(1, ID);

            // get the User
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new WorkPackage(
                            resultSet.getInt("ID"),
                            resultSet.getInt("ORDERNUMBER"),
                            resultSet.getString("TITLE"),
                            resultSet.getInt("STARTINGMONTH"),
                            resultSet.getInt("ENDINGMONTH"),
                            ProjectDAO.getFromID(resultSet.getInt("PROJECTID"))
                    );
                } else return null;
            }
        }
    }

    /**
     * Check if at least one sub Task is planned for the Month.
     *
     * @param workPackageID ID of the Work-Package to check.
     * @param month         Month to check.
     *
     * @return True if at least one sub Task is planned, false otherwise.
     * @throws SQLException DB error
     */
    public static synchronized boolean existsSubTasks(int workPackageID, int month) throws SQLException {
        String query = "SELECT count(*) " +
                "FROM TASKS AS T JOIN PLANNING AS P ON P.taskID = T.ID " +
                "WHERE T.workPackageID = ? AND P.month = ?";

        // connect to the DB
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            // set the parametric query
            statement.setInt(1, workPackageID);
            statement.setInt(2, month);

            // check the Tasks
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) return resultSet.getInt("COUNT(*)") > 0;
                return false;
            }
        }
    }
}
