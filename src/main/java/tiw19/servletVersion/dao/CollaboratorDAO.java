package tiw19.servletVersion.dao;

import tiw19.servletVersion.db.DBConnection;

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
    private static final String queryExists =
            "SELECT count(*) " +
            "FROM COLLABORATORS " +
            "WHERE userID = ? AND taskID = ? AND month = ?";
    private static final String insertNew =
            "INSERT INTO COLLABORATORS " +
            "(userID, taskID, month)" +
            "VALUES (?, ?, ?)";

    /**
     * Check if a Collaborator is already assigned to (Task, Month).
     * <br>This static method checks if a Collaborator is already assigned to (Task, Month), by query for the triplet (Task, Month, Collaborator).
     *
     * @param taskID        ID of the Task in the couple (Task, Month).
     * @param month         Month in the couple (Task, Month).
     * @param collaborator  Collaborator to check.
     *
     * @return True if the Planning already exists, false otherwise.
     *
     * @throws SQLException DB error
     */
    public static boolean existsAssignment(int taskID, int month, String collaborator) throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(queryExists)) {

            // set the parametric Query
            statement.setInt(1, UserDAO.getID(collaborator));
            statement.setInt(2, taskID);
            statement.setInt(3, month);

            // get the Planning
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt("COUNT(*)") > 0;
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
    public static void newAssignment(int taskID, int month, String collaborator) throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(insertNew)) {

            // set the parametric query
            statement.setInt(1, UserDAO.getID(collaborator));
            statement.setInt(2, taskID);
            statement.setInt(3, month);

            // update DB
            statement.executeUpdate();
        }
    }
}
