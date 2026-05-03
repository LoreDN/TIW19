package tiw19.servletVersion.dao;

import tiw19.servletVersion.db.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Planning Data-Access-Object.
 * <br>This Class works as the DAO for the PLANNING Table of the DB.
 *
 * @author LoreDN - Lorenzo Di Napoli
 * @version 1.0
 * @since 1.0
 */
public class PlanningDAO {
    private static final String queryExists =
            "SELECT count(*) " +
            "FROM PLANNING " +
            "WHERE taskID = ? AND month = ?";
    private static final String insertNew =
            "INSERT INTO PLANNING " +
            "(taskID, month, plannedHours)" +
            "VALUES (?, ?, ?)";

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
    public static boolean existsPlanning(int taskID, int month) throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(queryExists)) {

            // set the parametric Query
            statement.setInt(1, taskID);
            statement.setInt(2, month);

            // get the Planning
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt("COUNT(*)") > 0;
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
    public static void addNew(int taskID, int month, int hours) throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(insertNew)) {

            // set the parametric query
            statement.setInt(1, taskID);
            statement.setInt(2, month);
            statement.setInt(3, hours);

            // update DB
            statement.executeUpdate();
        }
    }
}
