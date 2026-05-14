package tiw19.servletVersion.dao;

import tiw19.servletVersion.db.DBConnection;
import tiw19.servletVersion.model.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.*;

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
     * Get the Planning for a triplet (Task, Month, Collaborator).
     *
     * @param userID    ID of the Collaborator in the triplet (Task, Month, Collaborator).
     * @param taskID    ID of the Task in the triplet (Task, Month, Collaborator).
     * @param month     Month in the triplet (Task, Month, Collaborator).
     *
     * @return Planning of the triplet (Task, Month, Collaborator).
     * @throws SQLException DB error
     */
    public static synchronized Collaborator getFromTriplet(int userID, int taskID, int month) throws SQLException {
        String query = "SELECT * " +
                "FROM COLLABORATORS " +
                "WHERE userID = ? AND taskID = ? AND month = ?";

        // connect to the DB
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            // set the parametric query
            statement.setInt(1, userID);
            statement.setInt(2, taskID);
            statement.setInt(3, month);

            // get the Plannings
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new Collaborator(
                            resultSet.getInt("USERID"),
                            resultSet.getInt("TASKID"),
                            resultSet.getInt("MONTH"),
                            resultSet.getInt("WORKEDHOURS")
                    );
                } else return null;
            }
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

    /**
     * Get all the assignments to a Collaborator in a Project.
     *
     * @param username  Username of the Collaborator.
     * @param projectID ID of the Project.
     *
     * @return Map of Tasks for each Work-Package.
     * @throws SQLException DB error
     */
    public static synchronized Map<WorkPackage, List<Task>> getAssignments(String username, int projectID) throws SQLException {
        Map<WorkPackage, List<Task>> assignments = new LinkedHashMap<>();
        User collaborator = UserDAO.getFromUsername(username);
        if (collaborator == null || !collaborator.isCollaborator()) return assignments;
        String query = "SELECT  DISTINCT T.ID, T.orderNumber, T.title, T.startingMonth, T.endingMonth, WP.projectID, T.workPackageID " +
                "FROM COLLABORATORS AS C " +
                "JOIN TASKS AS T ON C.taskID = T.ID " +
                "JOIN WORK_PACKAGES AS WP ON T.workPackageID = WP.ID " +
                "JOIN PROJECTS AS P ON WP.projectID = P.ID " +
                "WHERE C.userID = ? AND P.ID = ? " +
                "ORDER BY WP.orderNumber, T.orderNumber";

        // connect to the DB
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            // set the parametric query
            statement.setInt(1, collaborator.getID());
            statement.setInt(2, projectID);

            // get the Work-Packages & Tasks
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    WorkPackage workPackage = WorkPackageDAO.getFromID(resultSet.getInt("WORKPACKAGEID"));
                    if (workPackage == null) return new HashMap<>();
                    if (!assignments.containsKey(workPackage)) assignments.put(workPackage, new ArrayList<>());
                    assignments.get(workPackage).add(new Task(
                            resultSet.getInt("ID"),
                            resultSet.getFloat("ORDERNUMBER"),
                            resultSet.getString("TITLE"),
                            resultSet.getInt("STARTINGMONTH"),
                            resultSet.getInt("ENDINGMONTH"),
                            workPackage.getProject(),
                            workPackage
                    ));
                }
            }

        }
        return assignments;
    }

    /**
     * Get all the Months assigned to a Collaborator for a Task.
     *
     * @param userID ID of the Collaborator in the triplet (Task, Month, Collaborator).
     * @param taskID ID of the Task in the triplet (Task, Month, Collaborator).
     *
     * @return All months assigned to the Collaborator for the Task.
     * @throws SQLException DB error
     */
    public static synchronized List<Integer> getAssignedMonths(int userID, int taskID) throws SQLException {
        String query = "SELECT month " +
                "FROM COLLABORATORS " +
                "WHERE userID = ? AND taskID = ?";

        // connect to the DB
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            // set the parametric query
            statement.setInt(1, userID);
            statement.setInt(2, taskID);

            // get the Months
            List<Integer> months = new ArrayList<>();
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    months.add(resultSet.getInt("MONTH"));
                }
            }
            return months;
        }
    }

    /**
     * Log the Worked Hours for the triplet (Task, Month, Collaborator).
     *
     * @param userID        ID of the Collaborator in the triplet (Task, Month, Collaborator).
     * @param taskID        ID of the Task in the triplet (Task, Month, Collaborator).
     * @param month         Month in the triplet (Task, Month, Collaborator).
     * @param workedHours   Worked Hours to log.
     *
     * @throws SQLException DB error
     */
    public static synchronized void logWorkedHours(int userID, int taskID, int month, int workedHours) throws SQLException {
        String query = "UPDATE COLLABORATORS " +
                "SET workedHours = ? " +
                "WHERE userID = ? AND taskID = ? AND month = ?";

        // connect to the DB
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            // set the parametric query
            statement.setInt(1, workedHours);
            statement.setInt(2, userID);
            statement.setInt(3, taskID);
            statement.setInt(4, month);

            // update the DB
            statement.executeUpdate();
        }
    }
}
