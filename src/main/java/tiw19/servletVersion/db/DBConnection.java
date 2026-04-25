package tiw19.servletVersion.db;

import java.io.IOException;
import java.io.InputStream;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import java.util.Properties;

/**
 * DB Connection Handler.
 * <br>This class handles the connection to the DB, it can be called from anywhere in the architecture.
 *
 * @author LoreDN - Lorenzo Di Napoli
 * @version 1.0
 * @since 1.0
 */
public class DBConnection {
    private static final String URL;
    private static final String USER;
    private static final String PASSWORD;

    // static loader for db properties
    static {
        try (InputStream input = DBConnection.class
                .getClassLoader()
                .getResourceAsStream("db.properties")) {
            Properties props = new Properties();
            props.load(input);
            URL      = props.getProperty("db.url");
            USER     = props.getProperty("db.user");
            PASSWORD = props.getProperty("db.password");
        } catch (IOException exception) {
            throw new RuntimeException("Cannot load db.properties! ", exception);
        }
    }

    /**
     * Get the DB Connection.
     * <br>This is the only method of the class; it is a static method which connects to the DB.
     *
     * @return DB Connection
     * @throws SQLException DB Connection error
     */
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.mariadb.jdbc.Driver");
        } catch (ClassNotFoundException exception) {
            throw new SQLException("MariaDB driver not found! ", exception);
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}