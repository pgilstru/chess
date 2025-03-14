package dataaccess.sql;

import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import model.UserData;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SQLUserDAO implements UserDAO {
    // constructor to initialize dao and configure DB
    public SQLUserDAO() throws DataAccessException {
        // array of sql statements responsible for setting up the DB and its tables
        String[] createStatements = {
                // handle creating userData table if it doesn't already exist
                """
            CREATE TABLE IF NOT EXISTS users (
            `username` varchar(255) NOT NULL,
            `password` varchar(255) NOT NULL,
            `email` varchar(255) NOT NULL,
            PRIMARY KEY (username),
            INDEX(password),
            INDEX(email)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """
        };
        DatabaseManager.configureDatabase(createStatements);
    }

    @Override
    public void clear() throws DataAccessException {
        // clear all userData in the userData table, but not the table itself
        var statement = "DELETE FROM users";
        executeUpdate(statement);
    }

    @Override
    public UserData getUser(String username) throws DataAccessException{
        // retrieve userData given a username from the DB
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT username, password, email FROM users WHERE username=?";
            try (var prepStatement = conn.prepareStatement(statement)) {
                // bind the username parameter
                prepStatement.setString(1, username);

                try (var rs = prepStatement.executeQuery()) {
                    if (rs.next()) {
                        // return userData object from the result set
                        return readUser(rs);
                    }
                }
            }
        } catch (Exception ex) {
            throw new DataAccessException("Error: unable to get userData " + ex.getMessage());
        }
        // no matching auth token found, return null
        return null;
    }

    @Override
    public void createUser(UserData userData) throws DataAccessException{
        // verify user doesn't already exist
        if (getUser(userData.username()) != null) {
            throw new DataAccessException("Error: user already exists.");
        }

        // Create a new user in the DB
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";
            try (var prepStatement = conn.prepareStatement(statement)) {
                // bind authToken
                prepStatement.setString(1, userData.username());

                // bind username
                prepStatement.setString(2, userData.password());

                // bind email
                prepStatement.setString(3, userData.email());

                prepStatement.executeUpdate();
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Error: unable to create user " + ex.getMessage());
        }
    }

    private UserData readUser(ResultSet rs) throws SQLException {
        // connects given result set to an UserData object
        String username = rs.getString("username");
        String password = rs.getString("password");
        String email = rs.getString("email");
        return new UserData(username, password, email);
    }

    private void executeUpdate(String statement) throws DataAccessException {
        // helps with executing sql update statements
        try (var conn = DatabaseManager.getConnection()) {
            try (var prepStatement = conn.prepareStatement(statement)) {
                prepStatement.executeUpdate();
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Error: updating database " + ex.getMessage());
        }
    }
}
