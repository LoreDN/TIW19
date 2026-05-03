package tiw19.servletVersion.dao;

import tiw19.servletVersion.model.User;
import tiw19.servletVersion.db.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * User Data-Access-Object.
 * <br>This Class works as the DAO for the USERS Table of the DB.
 *
 * @author LoreDN - Lorenzo Di Napoli
 * @version 1.0
 * @since 1.0
 */
public class UserDAO {
    private static final String queryCredentials =
            "SELECT name, surname, username, photo, role " +
            "FROM USERS " +
            "WHERE username = ? AND password = ?";
    private static final String queryUsername =
            "SELECT name, surname, username, photo, role " +
            "FROM USERS " +
            "WHERE username = ?";
    private static final String queryID =
            "SELECT ID " +
            "FROM USERS " +
            "WHERE username = ?";

    /**
     * Constructor of the User DAO.
     */
    public UserDAO() {}

    /**
     * Get a logged User.
     * <br>This static method checks the credentials of a Client, and if valid returns the corresponding User.
     *
     * @param username Username of the Client.
     * @param password Password of the Client.
     *
     * @return User corresponding to the couple (username,password)
     * @throws SQLException DB error
     */
    public static User checkCredentials(String username, String password) throws SQLException {
        User user = null;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(queryCredentials)) {

            // set the parametric query
            statement.setString(1, username);
            statement.setString(2, password);

            // get the logged User
            try (ResultSet queryResult = statement.executeQuery()) {
                if (queryResult.next()) {
                    user = new User(
                            queryResult.getString("NAME"),
                            queryResult.getString("SURNAME"),
                            queryResult.getString("USERNAME"),
                            queryResult.getString("PHOTO"),
                            queryResult.getString("ROLE")
                    );
                }
            }
        }
        return user;
    }

    /**
     * Get a logged User by its username.
     * <br>This static method gets a User only from a Client username.
     *
     * @param username Username of the Client.
     *
     * @return User corresponding to the username.
     * @throws SQLException DB error
     */
    public static User getFromUsername(String username) throws SQLException {
        User user = null;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(queryUsername)) {

            // set the parametric query
            statement.setString(1, username);

            // get the User
            try (ResultSet queryResult = statement.executeQuery()) {
                if (queryResult.next()) {
                    user = new User(
                            queryResult.getString("NAME"),
                            queryResult.getString("SURNAME"),
                            queryResult.getString("USERNAME"),
                            queryResult.getString("PHOTO"),
                            queryResult.getString("ROLE")
                    );
                }
            }
        }
        return user;
    }

    /**
     * Get the ID of a User.
     *
     * @param username Username of the User.
     *
     * @return User.ID
     * @throws SQLException DB error
     */
    public static int getID(String username) throws SQLException {
        int userID;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(queryID)) {

            // set the parametric query
            statement.setString(1, username);

            // get the userID
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                userID = resultSet.getInt("ID");
            }
        }
        return userID;
    }
}