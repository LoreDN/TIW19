package tiw19.servletVersion.dao;

import tiw19.servletVersion.db.DBConnection;
import tiw19.servletVersion.model.Task;
import tiw19.servletVersion.model.User;
import tiw19.servletVersion.model.WorkPackage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.math.BigDecimal;

import java.util.List;
import java.util.ArrayList;

/**
 * Task Data-Access-Object.
 * <br>This Class works as the DAO for the TASKS Table of the DB.
 *
 * @author LoreDN - Lorenzo Di Napoli
 * @version 1.0
 * @since 1.0
 */
public class TaskDAO {

    /**
     * Check if a Task already exists.
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
    public static synchronized boolean existsTask(String admin, String projectTitle, String workPackageTitle, String taskTitle) throws SQLException {
        User user = UserDAO.getFromUsername(admin);
        if (user == null || !user.isAdmin()) return true;
        String query = "SELECT count(*) " +
                "FROM TASKS AS T JOIN (" +
                    "SELECT WP.ID " +
                    "FROM WORK_PACKAGES AS WP JOIN PROJECTS AS P ON WP.projectID = P.ID " +
                    "WHERE P.title = ? AND P.admin = ? AND WP.title = ?" +
                ") AS WP ON T.workPackageID = WP.ID " +
                "WHERE T.title = ?";

        // connect to the DB
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            // set the parametric Query
            statement.setString(1, projectTitle);
            statement.setString(2, admin);
            statement.setString(3, workPackageTitle);
            statement.setString(4, taskTitle);

            // get the Project
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) return resultSet.getInt("COUNT(*)") > 0;
                else return true;
            }
        }
    }

    /**
     * Add a new Task.
     *
     * @param projectTitle              Title of the Project referring to the Task.
     * @param workPackageTitle          Title of the Work-Package referring to the Task.
     * @param taskTitle                 Title of the Task.
     * @param taskStartingMonth         Starting month of the Task.
     * @param taskEndingMonth           Ending month of the Task.
     *
     * @return True is Task addition was successfully, false otherwise.
     * @throws SQLException DB error
     */
    public static synchronized boolean addNew(String admin, String projectTitle, String workPackageTitle, String taskTitle, String description, int taskStartingMonth, int taskEndingMonth) throws SQLException {
        User user = UserDAO.getFromUsername(admin);
        if (user == null || !user.isAdmin()) return false;
        String query = "INSERT INTO TASKS " +
                "(orderNumber, title, description, startingMonth, endingMonth, workPackageID) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        // add new Task
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            // get Work-Package ID
            int workPackageID = WorkPackageDAO.getID(admin, projectTitle, workPackageTitle);
            if (workPackageID == -1) return false;

            // set the parametric query
            statement.setBigDecimal(1, new BigDecimal(String.format(
                    "%d.%02d",
                    WorkPackageDAO.getOrderNumber(workPackageID),
                    getNumber(workPackageID) + 1)
            ));
            statement.setString(2, taskTitle);
            statement.setString(3, description);
            statement.setInt(4, taskStartingMonth);
            statement.setInt(5, taskEndingMonth);
            statement.setInt(6, workPackageID);

            // update DB
            statement.executeUpdate();
        }
        return true;
    }

    /**
     * Get the number of Tasks for a reference Work-Package.
     *
     * @param workPackageID ID of the Work-Package of reference.
     *
     * @return Number of Tasks for the reference Work-Package.
     * @throws SQLException DB error
     */
    public static synchronized int getNumber(int workPackageID) throws SQLException {
        String query = "SELECT count(*) " +
                "FROM TASKS " +
                "WHERE workPackageID = ?";

        // connect to the DB
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            // set the parametric query
            statement.setInt(1, workPackageID);

            // get the number of Work-Packages
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) return resultSet.getInt("COUNT(*)");
                else return 0;
            }
        }
    }

    /**
     * Get the ID of a Task.
     *
     * @param username          Admin/Manager of the Project.
     * @param projectTitle      Title of the Project.
     * @param workPackageTitle  Title of the Work-Package.
     * @param taskTitle         Title of the Task.
     *
     * @return Task.ID
     * @throws SQLException DB error
     */
    public static synchronized int getID(String username, String projectTitle, String workPackageTitle, String taskTitle) throws SQLException {
        String query;
        User user = UserDAO.getFromUsername(username);
        if (user == null) return -1;
        if (user.isAdmin()) {
            query = "SELECT T.ID " +
                    "FROM TASKS AS T JOIN WORK_PACKAGES AS WP ON T.workPackageID = WP.ID JOIN PROJECTS AS P ON WP.projectID = P.ID " +
                    "WHERE P.title = ? AND P.admin = ? AND WP.title = ? AND T.title = ?";
        } else if (user.isManager()) {
            query = "SELECT T.ID " +
                    "FROM TASKS AS T JOIN WORK_PACKAGES AS WP ON T.workPackageID = WP.ID JOIN PROJECTS AS P ON WP.projectID = P.ID " +
                    "WHERE P.title = ? AND P.manager = ? AND WP.title = ? AND T.title = ?";
        } else return -1;

        // connect to the DB
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            // set the parametric query
            statement.setString(1, projectTitle);
            statement.setString(2, username);
            statement.setString(3, workPackageTitle);
            statement.setString(4, taskTitle);

            // get the taskID
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) return resultSet.getInt("ID");
                else return -1;
            }
        }
    }

    /**
     * Get all Tasks ID for a Project.
     *
     * @param username      Admin/Manager of the Project (and the Tasks).
     * @param projectTitle  Title of the Project of reference.
     *
     * @return Task.ID for a Project.
     * @throws SQLException DB error
     */
    public static synchronized List<Integer> getIDs(String username, String projectTitle) throws SQLException {
        List<Integer> tasksID = new ArrayList<>();
        String query;
        User user = UserDAO.getFromUsername(username);
        if (user == null) return tasksID;
        if (user.isAdmin()) {
            query = "SELECT T.ID " +
                    "FROM TASKS AS T JOIN WORK_PACKAGES AS WP ON T.workPackageID = WP.ID JOIN PROJECTS AS P ON WP.projectID = P.ID " +
                    "WHERE P.title = ? AND P.admin = ?";
        } else if (user.isManager()) {
            query = "SELECT T.ID " +
                    "FROM TASKS AS T JOIN WORK_PACKAGES AS WP ON T.workPackageID = WP.ID JOIN PROJECTS AS P ON WP.projectID = P.ID " +
                    "WHERE P.title = ? AND P.manager = ?";
        } else return tasksID;

        // connect to the DB
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            // set the parametric query
            statement.setString(1, projectTitle);
            statement.setString(2, username);

            // get the Tasks ID
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    tasksID.add(resultSet.getInt("ID"));
                }
            }
        }
        return tasksID;
    }

    /**
     * Get all the Tasks.
     *
     * @param username Admin/Manager of the Projects (and Tasks).
     *
     * @return All Tasks.
     * @throws SQLException DB error
     */
    public static synchronized List<Task> getAllToBeAssigned(String username) throws SQLException {
        List<Task> tasks = new ArrayList<>();
        String query;
        User user = UserDAO.getFromUsername(username);
        if (user == null) return tasks;
        if (user.isAdmin()) {
            query = "SELECT T.ID, T.orderNumber, T.title, T.description, T.startingMonth, T.endingMonth, WP.projectID, T.workPackageID " +
                    "FROM TASKS AS T " +
                    "JOIN WORK_PACKAGES AS WP ON T.workPackageID = WP.ID " +
                    "JOIN PROJECTS AS P ON WP.projectID = P.ID " +
                    "WHERE P.admin = ? AND P.status = 'CREATED'";
        } else if (user.isManager()) {
            query = "SELECT T.ID, T.orderNumber, T.title, T.description, T.startingMonth, T.endingMonth, WP.projectID, T.workPackageID " +
                    "FROM TASKS AS T " +
                    "JOIN WORK_PACKAGES AS WP ON T.workPackageID = WP.ID " +
                    "JOIN PROJECTS AS P ON WP.projectID = P.ID " +
                    "WHERE P.manager = ? AND P.status = 'CREATED'";
        } else return tasks;

        // connect to the DB
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            // set the parametric query
            statement.setString(1, username);

            // get the Work-Packages
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    tasks.add(new Task(
                            resultSet.getInt("ID"),
                            resultSet.getFloat("ORDERNUMBER"),
                            resultSet.getString("TITLE"),
                            resultSet.getString("DESCRIPTION"),
                            resultSet.getInt("STARTINGMONTH"),
                            resultSet.getInt("ENDINGMONTH"),
                            ProjectDAO.getFromID(resultSet.getInt("PROJECTID")),
                            WorkPackageDAO.getFromID(resultSet.getInt("WORKPACKAGEID"))
                    ));
                }
            }
        }
        return tasks;
    }

    /**
     * Get the Task by its title.
     *
     * @param username          Admin/Manager of the Project.
     * @param projectTitle      Title of the Project.
     * @param workPackageTitle  Title of the Work-Package.
     * @param taskTitle         Title of the Task.
     *
     * @return Task corresponding to the title.
     * @throws SQLException DB error
     */
    public static synchronized Task getFromTitle(String username, String projectTitle, String workPackageTitle, String taskTitle) throws SQLException {
        String query;
        User user = UserDAO.getFromUsername(username);
        if (user == null) return null;
        if (user.isAdmin()) {
            query = "SELECT T.ID, T.orderNumber, T.title, T.description, T.startingMonth, T.endingMonth, WP.projectID, T.workPackageID " +
                    "FROM TASKS AS T JOIN WORK_PACKAGES AS WP ON T.workPackageID = WP.ID JOIN PROJECTS AS P ON WP.projectID = P.ID " +
                    "WHERE P.title = ? AND P.admin = ? AND WP.title = ? AND T.title = ?";
        } else if (user.isManager()) {
            query = "SELECT T.ID, T.orderNumber, T.title, T.description, T.startingMonth, T.endingMonth, WP.projectID, T.workPackageID " +
                    "FROM TASKS AS T JOIN WORK_PACKAGES AS WP ON T.workPackageID = WP.ID JOIN PROJECTS AS P ON WP.projectID = P.ID " +
                    "WHERE P.title = ? AND P.manager = ? AND WP.title = ? AND T.title = ?";
        } else return null;

        // connect to the DB
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            // set the parametric query
            statement.setString(1, projectTitle);
            statement.setString(2, username);
            statement.setString(3, workPackageTitle);
            statement.setString(4, taskTitle);

            // get the Task
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new Task(
                            resultSet.getInt("ID"),
                            resultSet.getFloat("ORDERNUMBER"),
                            resultSet.getString("TITLE"),
                            resultSet.getString("DESCRIPTION"),
                            resultSet.getInt("STARTINGMONTH"),
                            resultSet.getInt("ENDINGMONTH"),
                            ProjectDAO.getFromID(resultSet.getInt("PROJECTID")),
                            WorkPackageDAO.getFromID(resultSet.getInt("WORKPACKAGEID"))
                    );
                } else return null;
            }
        }
    }

    /**
     * Get a Task from its ID.
     *
     * @param taskID ID of the Task.
     *
     * @return Task corresponding to the ID.
     * @throws SQLException DB error
     */
    public static synchronized Task getFromID(int taskID) throws SQLException {
        String query = "SELECT T.ID, T.orderNumber, T.title, T.description, T.startingMonth, T.endingMonth, WP.projectID, T.workPackageID " +
                "FROM TASKS AS T " +
                "JOIN WORK_PACKAGES AS WP ON T.workPackageID = WP.ID " +
                "JOIN PROJECTS AS P ON WP.projectID = P.ID " +
                "WHERE T.ID = ?";

        // connect to the DB
        try (Connection connection = DBConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(query)) {

            // set the parametric query
            statement.setInt(1, taskID);

            // get the Task
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new Task(
                            resultSet.getInt("ID"),
                            resultSet.getFloat("ORDERNUMBER"),
                            resultSet.getString("TITLE"),
                            resultSet.getString("DESCRIPTION"),
                            resultSet.getInt("STARTINGMONTH"),
                            resultSet.getInt("ENDINGMONTH"),
                            ProjectDAO.getFromID(resultSet.getInt("PROJECTID")),
                            WorkPackageDAO.getFromID(resultSet.getInt("WORKPACKAGEID"))
                    );
                } else return null;
            }
        }
    }

    /**
     * Get all the Tasks from a Work-Package.
     *
     * @param workPackage Work-package of reference.
     *
     * @return All Tasks for the Work-Package.
     * @throws SQLException DB error
     */
    public static synchronized List<Task> getFromWorkPackage(WorkPackage workPackage) throws SQLException {
        List<Task> tasks = new ArrayList<>();
        if (workPackage == null) return tasks;
        String query = "SELECT * " +
                "FROM TASKS " +
                "WHERE workPackageID = ?";

        // connect to the DB
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            // set the parametric query
            statement.setInt(1,workPackage.getID());

            // get the Tasks
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    tasks.add(new Task(
                            resultSet.getInt("ID"),
                            resultSet.getFloat("ORDERNUMBER"),
                            resultSet.getString("TITLE"),
                            resultSet.getString("DESCRIPTION"),
                            resultSet.getInt("STARTINGMONTH"),
                            resultSet.getInt("ENDINGMONTH"),
                            workPackage.getProject(),
                            workPackage
                    ));
                }
            }
        }
        return tasks;
    }

    /**
     * Check if a Task is fully assigned.
     *
     * @param taskID ID of the Task.
     *
     * @return True if Task is fully assigned, false otherwise.
     * @throws SQLException DB error
     */
    public static synchronized boolean isFullyAssigned(int taskID) throws SQLException {
        return PlanningDAO.allMonthsPlanned(taskID) && CollaboratorDAO.allMonthsAssigned(taskID);
    }
}
