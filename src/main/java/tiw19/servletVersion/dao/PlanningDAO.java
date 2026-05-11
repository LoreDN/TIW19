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
     * Get the Planning for a couple (Task, Month).
     *
     * @param taskID ID of the Task in the couple (Task, Month).
     * @param month Month in the couple (Task, Month).
     *
     * @return Planning of the couple (Task, Month).
     * @throws SQLException DB error
     */
    public static synchronized Planning getFromCouple(int taskID, int month) throws SQLException {
        String query = "SELECT * " +
                "FROM PLANNING " +
                "WHERE taskID = ? AND month = ?";

        // connect to the DB
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            // set the parametric query
            statement.setInt(1, taskID);
            statement.setInt(2, month);

            // get the Plannings
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                     return new Planning(
                            resultSet.getInt("TASKID"),
                            resultSet.getInt("MONTH"),
                            resultSet.getInt("PLANNEDHOURS"),
                            resultSet.getInt("WORKEDHOURS")
                    );
                } else return null;
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

    /**
     * Get the total Planned Hours for a Task.
     *
     * @param task Task to query.
     *
     * @return Total Planned Hours for the Task.
     * @throws SQLException DB error
     */
    public static synchronized int getPlannedHours(Task task) throws SQLException {
        if (task == null) return 0;
        String query= "SELECT sum(plannedHours) " +
                "FROM PLANNING " +
                "WHERE taskID = ?";

        // connect to the DB
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            // set the parametric query
            statement.setInt(1, task.getID());

            // get the Planned Hours
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) return resultSet.getInt("SUM(PLANNEDHOURS)");
                else return 0;
            }
        }
    }

    /**
     * Get the total Worked Hours for a Task.
     *
     * @param task Task to query.
     *
     * @return Total Worked Hours for the Task.
     * @throws SQLException DB error
     */
    public static synchronized int getWorkedHours(Task task) throws SQLException {
        if (task == null) return 0;
        String query= "SELECT sum(workedHours) " +
                "FROM PLANNING " +
                "WHERE taskID = ?";

        // connect to the DB
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            // set the parametric query
            statement.setInt(1, task.getID());

            // get the Planned Hours
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) return resultSet.getInt("SUM(WORKEDHOURS)");
                else return 0;
            }
        }
    }

    /**
     * Check if a Task is completed.
     *
     * @param taskID ID of the Task.
     *
     * @return True if Task is completed, false otherwise.
     * @throws SQLException DB error
     */
    public static synchronized boolean isCompleted(int taskID) throws SQLException {
        String query = "SELECT count(*) " +
                "FROM PLANNING " +
                "WHERE taskID = ? AND plannedHours < workedHours";

        // connect to the DB
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            // set the parametric query
            statement.setInt(1, taskID);

            // check if Task is concluded
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) return resultSet.getInt("COUNT(*)") == 0;
                else return false;
            }
        }
    }
}
