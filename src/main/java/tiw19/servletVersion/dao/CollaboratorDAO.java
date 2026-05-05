package tiw19.servletVersion.dao;

import tiw19.servletVersion.db.DBConnection;
import tiw19.servletVersion.model.Task;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Collaborator Data-Access-Object.
 * <br>This Class works as the DAO for the COLLABORATORS Table of the DB.
 *
 * @author LoreDN - Lorenzo Di Napoli
 * @version 1.0
 * @since 1.0
 */
public class CollaboratorDAO {

    /**
     * Check if a Collaborator is already assigned to (Task, Month).
     * <br>This static method checks if a Collaborator is already assigned to (Task, Month), by query for the triplet (Task, Month, Collaborator).
     *
     * @param taskID        ID of the Task in the couple (Task, Month).
     * @param month         Month in the couple (Task, Month).
     * @param collaborator  Collaborator to check.
     *
     * @return True if the assignment already exists, false otherwise.
     *
     * @throws SQLException DB error
     */
    public static synchronized boolean existsAssignment(int taskID, int month, String collaborator) throws SQLException {
        String query = "SELECT count(*) " +
                "FROM COLLABORATORS " +
                "WHERE userID = ? AND taskID = ? AND month = ?";

        // connect to the DB
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            // set the parametric query
            statement.setInt(1, UserDAO.getID(collaborator));
            statement.setInt(2, taskID);
            statement.setInt(3, month);

            // get the assignment
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) return resultSet.getInt("COUNT(*)") > 0;
                else return true;
            }
        }
    }

    /**
     * Assign a Collaborator to a couple (Task, Month).
     * <br>This static method adds a new triplet (Task, Month, Collaborator) into the DB.
     *
     * @param taskID        ID of the Task in the triplet (Task, Month, Collaborator).
     * @param month         Month in the triplet (Task, Month, Collaborator).
     * @param collaborator  Collaborator in the triplet (Task, Month, Collaborator).
     *
     * @throws SQLException DB error
     */
    public static synchronized void newAssignment(int taskID, int month, String collaborator) throws SQLException {
        String query = "INSERT INTO COLLABORATORS " +
                "(userID, taskID, month)" +
                "VALUES (?, ?, ?)";

        // connect to the DB
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            // set the parametric query
            statement.setInt(1, UserDAO.getID(collaborator));
            statement.setInt(2, taskID);
            statement.setInt(3, month);

            // update DB
            statement.executeUpdate();
        }
    }

    /**
     * Check if all Months of a Task are assigned.
     *
     * @param taskID ID of the Task to check.
     *
     * @return True if all Months are assigned, false otherwise.
     * @throws SQLException DB error
     */
    public static synchronized boolean allMonthsAssigned(int taskID) throws SQLException {
        String query = "SELECT count(*) " +
                "FROM COLLABORATORS " +
                "WHERE taskID = ? AND month = ?";

        // get the Task
        Task task = TaskDAO.getFromID(taskID);
        if (task == null) return false;

        // connect to the DB
        for (int month = task.getStartingMonth(); month <= task.getEndingMonth(); month++) {
            try (Connection connection = DBConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(query)) {

                // set the parametric query
                statement.setInt(1, taskID);
                statement.setInt(2, month);

                // check if all months are planned
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        if (resultSet.getInt("COUNT(*)") == 0) return false;
                    } else return false;
                }
            }
        }
        return true;
    }
}
