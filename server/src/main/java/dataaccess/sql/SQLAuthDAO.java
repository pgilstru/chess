package dataaccess.sql;

import com.google.gson.Gson;
import dataaccess.*;
import model.AuthData;
import service.ResponseException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import static java.sql.Types.NULL;

public class SQLAuthDAO implements AuthDAO {

    // constructor to initialize dao and configure DB
    public SQLAuthDAO() throws DataAccessException {
        configureDatabase();
    }

    @Override
    public void clear() throws DataAccessException {
        // clear all authData in the authData table, but not the table itself
        var statement = "DELETE FROM authTokens";
        executeUpdate(statement);
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        // retrieve authData given an authToken from the DB
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT authToken, username FROM authTokens WHERE authToken=?";
            try (var prepStatement = conn.prepareStatement(statement)) {
                // bind the authToken parameter
                prepStatement.setString(1, authToken);

                try (var rs = prepStatement.executeQuery()) {
                    if (rs.next()) {
                        // return authData object from the result set
                        return readAuth(rs);
                    }
                }
            }
        } catch (Exception ex) {
            throw new DataAccessException("Error: unable to read data " + ex.getMessage());
        }
        // no matching auth token found, return null
        return null;
    }

    @Override
    public void createAuth(AuthData authData) throws DataAccessException {
        // Create a new authorization in the DB
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "INSERT INTO authTokens (authToken, username) VALUES (?, ?)";
            try (var prepStatement = conn.prepareStatement(statement)) {
                // bind authToken
                prepStatement.setString(1, authData.authToken());

                // bind username
                prepStatement.setString(2, authData.username());

                prepStatement.executeUpdate();
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Error: unable to create authToken " + ex.getMessage());
        }
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        // Delete an authorization from DB so that it is no longer valid.
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "DELETE FROM authTokens WHERE authToken=?";
            try (var prepStatement = conn.prepareStatement(statement)) {
                // bind authToken
                prepStatement.setString(1, authToken);

                prepStatement.executeUpdate();
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Error: unable to create authToken " + ex.getMessage());
        }
    }

    private AuthData readAuth(ResultSet rs) throws SQLException {
        // connects given result set to an AuthData object
        String authToken = rs.getString("authToken");
        String username = rs.getString("username");
        return new AuthData(authToken, username);
//        var authData = new Gson().fromJson(username, AuthData.class);
//        return authData.getAuth(authToken);
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

//    private int executeUpdate(String statement, Object... params) throws DataAccessException {
//        try (var conn = DatabaseManager.getConnection()) {
//            try (var prepStatement = conn.prepareStatement(statement, Statement.RETURN_GENERATED_KEYS)) {
//                for (int i = 0; i < params.length; i++) {
//                    var param = params[i];
//                    switch (param) {
//                        case String p -> prepStatement.setString(i + 1, p);
//                        case Integer p -> prepStatement.setInt(i + 1, p);
////                        case AuthData p -> prepStatement.setString(i + 1, p.toString());
//                        case null -> prepStatement.setNull(i + 1, NULL);
//                        default -> {
//                        }
//                    }
//                }
//
//                prepStatement.executeUpdate();
//
//                var rs = prepStatement.getGeneratedKeys();
//
//                if (rs.next()) {
//                    return rs.getInt(1);
//                }
//
//                return 0;
//            }
//        } catch (SQLException ex) {
//            throw new DataAccessException("Error: updating database " + ex.getMessage());
//        }
//    }

    // array of sql statements responsible for setting up the DB and its tables
    private final String[] createStatements = {
            // handle creating authTokens table if it doesn't already exist
            """
            CREATE TABLE IF NOT EXISTS authTokens (
            `authToken` varchar(255) NOT NULL,
            `username` varchar(255) NOT NULL,
            PRIMARY KEY (authToken),
            INDEX(username)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """
    };


    private void configureDatabase() throws DataAccessException {
        // ensure DB exists by attempting to create it
        DatabaseManager.createDatabase();
        try (var conn = DatabaseManager.getConnection()) {
            // execute createStatements CREATE TABLE statement
            for (var statement : createStatements) {
                try (var preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Error: creating tables " + ex.getMessage());
        }
    }
}
