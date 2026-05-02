package tiw19.servletVersion.dao;

import tiw19.servletVersion.db.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Task Data-Access-Object.
 * <br>This Class works as the DAO for the TASKS Table of the DB.
 *
 * @author LoreDN - Lorenzo Di Napoli
 * @version 1.0
 * @since 1.0
 */
public class TaskDAO {
    private static final String queryExists =
            "SELECT count(*) " +
            "FROM TASKS AS T JOIN (" +
                    "SELECT WP.ID " +
                    "FROM WORK_PACKAGES AS WP JOIN PROJECTS AS P ON WP.projectID = P.ID " +
                    "WHERE P.title = ? AND WP.title = ?" +
            ") AS WP ON T.workPackageID = WP.ID " +
            "WHERE T.title = ?";
    private static final String queryWorkPackageID =
            "SELECT WP.ID, WP.orderNumber " +
            "FROM WORK_PACKAGES AS WP JOIN PROJECTS AS P ON WP.projectID = P.ID " +
            "WHERE P.title = ? AND WP.title = ?";
    private static final String queryTasksNumber =
            "SELECT count(*) " +
            "FROM TASKS " +
            "WHERE workPackageID = ?";
    private static final String insertNew =
            "INSERT INTO TASKS " +
            "(orderNumber, title, startingMonth, endingMonth, workPackageID) " +
            "VALUES (?, ?, ?, ?, ?)";

    /**
     * Constructor of the Task DAO.
     */
    public TaskDAO() {}

    /**
     * Check if a Task already exists.
     * <br>This method checks if a Task already exists, by query for the title.
     *
     * @param projectTitle      Title of the Project referring to the Task.
     * @param workPackageTitle  Title of the Work-Package referring to the Task.
     * @param taskTitle         Title of the Task to check.
     *
     * @return True if the Task already exists, false otherwise.
     *
     * @throws SQLException DB error
     */
    public boolean existsTask(String projectTitle, String workPackageTitle, String taskTitle) throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(queryExists)) {

            // set the parametric Query
            statement.setString(1, projectTitle);
            statement.setString(2, workPackageTitle);
            statement.setString(3, taskTitle);

            // get the Project
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt("COUNT(*)") > 0;
            }
        }
    }

    /**
     * Add a new Task.
     * <br>This method adds a new Task into the DB.
     *
     * @param projectTitle              Title of the Project referring to the Task.
     * @param workPackageTitle          Title of the Work-Package referring to the Task.
     * @param taskTitle                 Title of the Task.
     * @param taskStartingMonth         Starting month of the Task.
     * @param taskEndingMonth           Ending month of the Task.
     *
     * @throws SQLException DB error
     */
    public void addNew(String projectTitle, String workPackageTitle, String taskTitle, int taskStartingMonth, int taskEndingMonth) throws SQLException {
        // get the workPackageID and workPackageOrderNumber
        int workPackageID, workPackageOrderNumber;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(queryWorkPackageID)) {
            statement.setString(1, projectTitle);
            statement.setString(2, workPackageTitle);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                workPackageID = resultSet.getInt("ID");
                workPackageOrderNumber = resultSet.getInt("ORDERNUMBER");
            }
        }

        // get the number of Tasks
        int tasksNumber;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(queryTasksNumber)) {
            statement.setInt(1, workPackageID);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                tasksNumber = resultSet.getInt("COUNT(*)");
            }
        }

        // add new Task
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(insertNew)) {

            // set the parametric query
            statement.setBigDecimal(1, new BigDecimal(String.format("%d.%02d",workPackageOrderNumber, tasksNumber + 1)));
            statement.setString(2, taskTitle);
            statement.setInt(3, taskStartingMonth);
            statement.setInt(4, taskEndingMonth);
            statement.setInt(5, workPackageID);

            // update DB
            statement.executeUpdate();
        }
    }
}
