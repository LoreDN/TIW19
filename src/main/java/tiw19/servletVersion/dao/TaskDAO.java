package tiw19.servletVersion.dao;

import tiw19.servletVersion.db.DBConnection;
import tiw19.servletVersion.model.Task;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.List;

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
                    "WHERE P.title = ? AND P.admin = ? AND WP.title = ?" +
            ") AS WP ON T.workPackageID = WP.ID " +
            "WHERE T.title = ?";
    private static final String queryWorkPackageID =
            "SELECT WP.ID, WP.orderNumber " +
            "FROM WORK_PACKAGES AS WP JOIN PROJECTS AS P ON WP.projectID = P.ID " +
            "WHERE P.title = ? AND P.admin = ? AND WP.title = ?";
    private static final String queryTasksNumber =
            "SELECT count(*) " +
            "FROM TASKS " +
            "WHERE workPackageID = ?";
    private static final String insertNew =
            "INSERT INTO TASKS " +
            "(orderNumber, title, startingMonth, endingMonth, workPackageID) " +
            "VALUES (?, ?, ?, ?, ?)";
    private static final String queryAll =
            "SELECT T.title, T.startingMonth, T.endingMonth, WP.projectID, T.workPackageID " +
            "FROM TASKS AS T JOIN WORK_PACKAGES AS WP ON T.workPackageID = WP.ID JOIN PROJECTS AS P ON WP.projectID = P.ID " +
            "WHERE P.manager = ?";
    private static final String queryTitle =
            "SELECT T.title, T.startingMonth, T.endingMonth, WP.projectID, T.workPackageID " +
            "FROM TASKS AS T JOIN WORK_PACKAGES AS WP ON T.workPackageID = WP.ID JOIN PROJECTS AS P ON WP.projectID = P.ID " +
            "WHERE P.title = ? AND P.manager = ? AND WP.title = ? AND T.title = ?";
    private static final String queryID =
            "SELECT T.ID " +
            "FROM TASKS AS T JOIN WORK_PACKAGES AS WP ON T.workPackageID = WP.ID JOIN PROJECTS AS P ON WP.projectID = P.ID " +
            "WHERE P.title = ? AND P.manager = ? AND WP.title = ? AND T.title = ?";
    private static final String queryManager =
            "SELECT T.ID " +
            "FROM TASKS AS T JOIN WORK_PACKAGES AS WP ON T.workPackageID = WP.ID JOIN PROJECTS AS P ON WP.projectID = P.ID " +
            "WHERE P.title = ? AND P.manager = ?";
    private static final String queryFromID =
            "SELECT T.title, T.startingMonth, T.endingMonth, WP.projectID, T.workPackageID " +
            "FROM TASKS AS T JOIN WORK_PACKAGES AS WP ON T.workPackageID = WP.ID JOIN PROJECTS AS P ON WP.projectID = P.ID " +
            "WHERE T.ID = ?";
    private static final String queryHoursAssigned =
            "SELECT count(*) " +
            "FROM PLANNING " +
            "WHERE taskID = ?";
    private static final String queryCollaboratorAssigned =
            "SELECT  count(*) " +
            "FROM COLLABORATORS " +
            "WHERE taskID = ? AND month = ?";

    /**
     * Check if a Task already exists.
     * <br>This static method checks if a Task already exists, by query for the title.
     *
     * @param admin             Admin of the Project referring to the Task.
     * @param projectTitle      Title of the Project referring to the Task.
     * @param workPackageTitle  Title of the Work-Package referring to the Task.
     * @param taskTitle         Title of the Task to check.
     *
     * @return True if the Task already exists, false otherwise.
     *
     * @throws SQLException DB error
     */
    public static boolean existsTask(String admin, String projectTitle, String workPackageTitle, String taskTitle) throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(queryExists)) {

            // set the parametric Query
            statement.setString(1, projectTitle);
            statement.setString(2, admin);
            statement.setString(3, workPackageTitle);
            statement.setString(4, taskTitle);

            // get the Project
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt("COUNT(*)") > 0;
            }
        }
    }

    /**
     * Add a new Task.
     * <br>This static method adds a new Task into the DB.
     *
     * @param projectTitle              Title of the Project referring to the Task.
     * @param workPackageTitle          Title of the Work-Package referring to the Task.
     * @param taskTitle                 Title of the Task.
     * @param taskStartingMonth         Starting month of the Task.
     * @param taskEndingMonth           Ending month of the Task.
     *
     * @throws SQLException DB error
     */
    public static void addNew(String admin, String projectTitle, String workPackageTitle, String taskTitle, int taskStartingMonth, int taskEndingMonth) throws SQLException {
        // get the workPackageID and workPackageOrderNumber
        int workPackageID, workPackageOrderNumber;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(queryWorkPackageID)) {
            statement.setString(1, projectTitle);
            statement.setString(2, admin);
            statement.setString(3, workPackageTitle);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) return;
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

    /**
     * Get all the Tasks.
     *
     * @param manager Manager of the Projects (and Tasks).
     *
     * @return All Tasks.
     * @throws SQLException DB error
     */
    public static List<Task> getAll(String manager) throws SQLException {
        List<Task> tasks = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(queryAll)) {

            // set the parametric query
            statement.setString(1, manager);

            // get the Work-Packages
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    tasks.add(new Task(
                            resultSet.getString("TITLE"),
                            Integer.parseInt(resultSet.getString("STARTINGMONTH")),
                            Integer.parseInt(resultSet.getString("ENDINGMONTH")),
                            ProjectDAO.getFromID(Integer.parseInt(resultSet.getString("PROJECTID"))),
                            WorkPackageDAO.getFromID(Integer.parseInt(resultSet.getString("WORKPACKAGEID")))
                    ));
                }
            }
        }
        return tasks;
    }

    /**
     * Get the Task by its title.
     * <br>This static method gets a Task only from its title (and the Project's one).
     *
     * @param manager           Manager of the Project.
     * @param projectTitle      Title of the Project.
     * @param workPackageTitle  Title of the Work-Package.
     * @param taskTitle         Title of the Task.
     *
     * @return Task corresponding to the title.
     * @throws SQLException DB error
     */
    public static Task getFromTitle(String manager, String projectTitle, String workPackageTitle, String taskTitle) throws SQLException {
        Task task;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(queryTitle)) {

            // set the parametric query
            statement.setString(1, projectTitle);
            statement.setString(2, manager);
            statement.setString(3, workPackageTitle);
            statement.setString(4, taskTitle);

            // get the Task
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                task = new Task(
                        resultSet.getString("TITLE"),
                        Integer.parseInt(resultSet.getString("STARTINGMONTH")),
                        Integer.parseInt(resultSet.getString("ENDINGMONTH")),
                        ProjectDAO.getFromID(Integer.parseInt(resultSet.getString("PROJECTID"))),
                        WorkPackageDAO.getFromID(Integer.parseInt(resultSet.getString("WORKPACKAGEID")))
                );
            }
        }
        return task;
    }

    /**
     * Get the ID of a Task.
     *
     * @param manager           Manager of the Project.
     * @param projectTitle      Title of the Project.
     * @param workPackageTitle  Title of the Work-Package.
     * @param taskTitle         Title of the Task.
     *
     * @return Task.ID
     * @throws SQLException DB error
     */
    public static int getID(String manager, String projectTitle, String workPackageTitle, String taskTitle) throws SQLException {
        int taskID;
        try (Connection connection = DBConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(queryID)) {

            // set the parametric query
            statement.setString(1, projectTitle);
            statement.setString(2, manager);
            statement.setString(3, workPackageTitle);
            statement.setString(4, taskTitle);

            // get the taskID
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                taskID = resultSet.getInt("ID");
            }
        }
        return taskID;
    }

    /**
     * Get all Tasks ID for a manager.
     *
     * @param manager       Manager of the Project (and the Tasks).
     * @param projectTitle  Title of the Project of reference.
     *
     * @return Task.ID for manager's Tasks.
     * @throws SQLException DB error
     */
    public static List<Integer> getIDFromManager(String manager, String projectTitle) throws SQLException {
        List<Integer> tasksID = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(queryManager)) {

            // set the parametric query
            statement.setString(1, projectTitle);
            statement.setString(2, manager);

            // get the Tasks ID
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    tasksID.add(Integer.parseInt(resultSet.getString("ID")));
                }
            }
        }
        return tasksID;
    }

    /**
     * Get a Task form its ID.
     *
     * @param taskID ID of the Task.
     *
     * @return Task corresponding to the ID.
     * @throws SQLException DB error
     */
    public static Task getFromID(int taskID) throws SQLException {
        Task task;
        try (Connection connection = DBConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(queryFromID)) {

            // set the parametric query
            statement.setInt(1, taskID);

            // get the Task
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                task = new Task(
                        resultSet.getString("TITLE"),
                        Integer.parseInt(resultSet.getString("STARTINGMONTH")),
                        Integer.parseInt(resultSet.getString("ENDINGMONTH")),
                        ProjectDAO.getFromID(Integer.parseInt(resultSet.getString("PROJECTID"))),
                        WorkPackageDAO.getFromID(Integer.parseInt(resultSet.getString("WORKPACKAGEID")))
                );
            }
        }
        return task;
    }

    /**
     * Check if a Task is fully assigned.
     *
     * @param taskID ID of the Task.
     *
     * @return true if Task is fully assigned, false otherwise.
     * @throws SQLException Db error
     */
    public static boolean isFullyAssigned(int taskID) throws SQLException {
        // check the plannedHours
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(queryHoursAssigned)) {
            statement.setInt(1, taskID);
            try (ResultSet resultSet = statement.executeQuery()) {
                Task task = getFromID(taskID);
                resultSet.next();
                if (resultSet.getInt("COUNT(*)") != task.getEndingMonth() - task.getStartingMonth() + 1) return false;
            }
        }

        // check the collaborators
        Task task = getFromID(taskID);
        for (int month = task.getStartingMonth(); month <= task.getEndingMonth(); month++) {
            try (Connection connection = DBConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(queryCollaboratorAssigned)) {
                statement.setInt(1, taskID);
                statement.setInt(2, month);
                try (ResultSet resultSet = statement.executeQuery()) {
                    resultSet.next();
                    if (resultSet.getInt("COUNT(*)") == 0) return false;
                }
            }
        }
        return true;
    }
}
