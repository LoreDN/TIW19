package tiw19.servletVersion.dao;

import tiw19.servletVersion.db.DBConnection;
import tiw19.servletVersion.model.Task;
import tiw19.servletVersion.model.Planning;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;

/**
 * Planning Data-Access-Object.
 * <br>This Class works as the DAO for the PLANNING Table of the DB.
 *
 * @author LoreDN - Lorenzo Di Napoli
 * @version 1.0
 * @since 1.0
 */
public class PlanningDAO {

    /**
     * Check if a Planning already exists.
     * <br>This static method checks if a Planning already exists, by query for the couple (Task.ID, month).
     *
     * @param taskID    ID of the Task in the couple (Task, Month).
     * @param month     Month in the couple (Task, Month).
     *
     * @return True if the Planning already exists, false otherwise.
     *
     * @throws SQLException DB error
     */
    public static synchronized boolean existsPlanning(int taskID, int month) throws SQLException {
        String query = "SELECT count(*) " +
                "FROM PLANNING " +
                "WHERE taskID = ? AND month = ?";

        // connect to the DB
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            // set the parametric query
            statement.setInt(1, taskID);
            statement.setInt(2, month);

            // get the Planning
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) return resultSet.getInt("COUNT(*)") > 0;
                else return true;
            }
        }
    }

    /**
     * Add a new Planning.
     * <br>This static method adds a new Planning into the DB.
     *
     * @param taskID    ID of the Task in the couple (Task, Month).
     * @param month     Month in the couple (Task, Month).
     * @param hours     Number of planned hours for couple (Task, Month).
     *
     * @throws SQLException DB error
     */
    public static synchronized void addNew(int taskID, int month, int hours) throws SQLException {
        String query = "INSERT INTO PLANNING " +
                "(taskID, month, plannedHours)" +
                "VALUES (?, ?, ?)";

        // connect to the DB
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            // set the parametric query
            statement.setInt(1, taskID);
            statement.setInt(2, month);
            statement.setInt(3, hours);

            // update DB
            statement.executeUpdate();
        }
    }

    /**
     * Check if all Months of a Task are planned.
     *
     * @param taskID ID of the Task to check.
     *
     * @return True if all Months are planned, false otherwise.
     * @throws SQLException DB error
     */
    public static synchronized boolean allMonthsPlanned(int taskID) throws SQLException {
        String query = "SELECT count(*) " +
                "FROM PLANNING " +
                "WHERE taskID = ?";

        // connect to the DB
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            // get the Task
            Task task = TaskDAO.getFromID(taskID);
            if (task == null) return false;

            // set the parametric query
            statement.setInt(1, taskID);

            // check if all months are planned
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) return resultSet.getInt("COUNT(*)") == task.getEndingMonth() - task.getStartingMonth() + 1;
                else return false;
            }
        }
    }

    /**
     * Get the Planning for a Task.
     *
     * @param taskID ID of the Task.
     *
     * @return Planning of the Task.
     * @throws SQLException DB error
     */
    public static synchronized List<Planning> getFromTask(int taskID) throws SQLException {
        String query = "SELECT * " +
                "FROM PLANNING " +
                "WHERE taskID = ?";

        // connect to the DB
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            // set the parametric query
            statement.setInt(1, taskID);

            // get the Plannings
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Planning> plannings = new ArrayList<>();
                while (resultSet.next()) {
                    plannings.add(new Planning(
                            resultSet.getInt("TASKID"),
                            resultSet.getInt("MONTH"),
                            resultSet.getInt("PLANNEDHOURS"),
                            resultSet.getInt("WORKEDHOURS")
                    ));
                }
                return plannings;
            }
        }
    }

    /**
     * Update the Total Worked Hours for the couple (Task, Month).
     *
     * @param taskID    ID of the Task in the couple (Task, Month).
     * @param month     Month in the couple (Task, Month).
     *
     * @throws SQLException DB error
     */
    public static synchronized void updatePlanning(int taskID, int month) throws SQLException {
        String query = "UPDATE PLANNING as P " +
                "SET P.workedHours = (" +
                    "SELECT sum(workedHours) " +
                    "FROM COLLABORATORS as C " +
                    "WHERE C.taskID = P.taskID AND C.month = P.month " +
                ") " +
                "WHERE P.taskID = ? AND P.month = ?";

        // connect to the DB
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            // set the parametric query
            statement.setInt(1, taskID);
            statement.setInt(2, month);

            // update the DB
            statement.executeUpdate();
        }
    }
}
