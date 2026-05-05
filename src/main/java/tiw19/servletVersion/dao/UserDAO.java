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
    public static synchronized User checkCredentials(String username, String password) throws SQLException {
        String query = "SELECT name, surname, username, photo, role " +
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
     * Get a logged User by its username.
     * <br>This static method gets a User only from a Client username.
     *
     * @param username Username of the Client.
     *
     * @return User corresponding to the username.
     * @throws SQLException DB error
     */
    public static User getFromUsername(String username) throws SQLException {
        String query = "SELECT name, surname, username, photo, role " +
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
}