package tiw19.servletVersion.dao;

import tiw19.servletVersion.model.User;
import tiw19.servletVersion.db.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;

/**
 * User Data-Access-Object.
 * <br>This Class works as the DAO for the USERS Table of the DB.
 *
 * @author LoreDN - Lorenzo Di Napoli
 * @version 1.0
 * @since 1.0
 */
public class UserDAO {

    /**
     * Get a logged User by its username and password.
     *
     * @param username Username of the Client.
     * @param password Password of the Client.
     *
     * @return User corresponding to the couple (username,password)
     * @throws SQLException DB error
     */
    public static synchronized User checkCredentials(String username, String password) throws SQLException {
        String query = "SELECT ID, name, surname, username, photo, role " +
                "FROM USERS " +
                "WHERE username = ? AND password = ?";

        // connect to the DB
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            // set the parametric query
            statement.setString(1, username);
            statement.setString(2, password);

            // get the logged User
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new User(
                            resultSet.getInt("ID"),
                            resultSet.getString("NAME"),
                            resultSet.getString("SURNAME"),
                            resultSet.getString("USERNAME"),
                            resultSet.getString("PHOTO"),
                            resultSet.getString("ROLE")
                    );
                } else return null;
            }
        }
    }

    /**
     * Get the ID of a User.
     *
     * @param username Username of the User.
     *
     * @return User.ID
     * @throws SQLException DB error
     */
    public static synchronized int getID(String username) throws SQLException {
        String query = "SELECT ID " +
                "FROM USERS " +
                "WHERE username = ?";

        // connect to the DB
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            // set the parametric query
            statement.setString(1, username);

            // get the userID
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) return resultSet.getInt("ID");
                else return -1;
            }
        }
    }

    /**
     * Get a logged User by its ID.
     *
     * @param userID ID of the Client.
     *
     * @return User corresponding to the username.
     * @throws SQLException DB error
     */
    public static User getFromID(int userID) throws SQLException {
        String query = "SELECT ID, name, surname, username, photo, role " +
                "FROM USERS " +
                "WHERE ID = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            // set the parametric query
            statement.setInt(1, userID);

            // get the User
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new User(
                            resultSet.getInt("ID"),
                            resultSet.getString("NAME"),
                            resultSet.getString("SURNAME"),
                            resultSet.getString("USERNAME"),
                            resultSet.getString("PHOTO"),
                            resultSet.getString("ROLE")
                    );
                } else return null;
            }
        }
    }

    /**
     * Get a logged User by its username.
     *
     * @param username Username of the Client.
     *
     * @return User corresponding to the username.
     * @throws SQLException DB error
     */
    public static User getFromUsername(String username) throws SQLException {
        String query = "SELECT ID, name, surname, username, photo, role " +
                "FROM USERS " +
                "WHERE username = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            // set the parametric query
            statement.setString(1, username);

            // get the User
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new User(
                            resultSet.getInt("ID"),
                            resultSet.getString("NAME"),
                            resultSet.getString("SURNAME"),
                            resultSet.getString("USERNAME"),
                            resultSet.getString("PHOTO"),
                            resultSet.getString("ROLE")
                    );
                } else return null;
            }
        }
    }

    /**
     * Get the Collaborators assigned to at least one Project for a Manager.
     *
     * @param manager Manager of the Projects assigned to the Collaborators.
     *
     * @return All Collaborators to at least one Project for the Manager.
     * @throws SQLException DB error
     */
    public static synchronized List<User> getCollaborators(String manager) throws SQLException {
        List<User> collaborators = new ArrayList<>();
        User user = getFromUsername(manager);
        if (user == null || !user.isManager()) return collaborators;
        String query = "SELECT DISTINCT U.ID, U.name, U.surname, U.username, U.photo, U.role " +
                "FROM USERS AS U " +
                "JOIN COLLABORATORS AS C ON U.ID = C.userID " +
                "JOIN TASKS AS T ON C.taskID = T.ID " +
                "JOIN WORK_PACKAGES AS WP ON T.workPackageID = WP.ID " +
                "JOIN PROJECTS AS P ON WP.projectID = P.ID " +
                "WHERE P.manager = ? AND P.status != 'CONCLUDED'";

        // connect to the DB
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            // set the parametric query
            statement.setString(1, manager);

            // get the Users
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    collaborators.add(new User(
                            resultSet.getInt("ID"),
                            resultSet.getString("NAME"),
                            resultSet.getString("SURNAME"),
                            resultSet.getString("USERNAME"),
                            resultSet.getString("PHOTO"),
                            resultSet.getString("ROLE")
                    ));
                }
            }
        }
        return collaborators;
    }
}