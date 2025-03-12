package dataaccess.sql;

import dataaccess.*;
import service.ResponseException;

public class SQLAuthDAO implements AuthDAO {

    public SQLAuthDAO() throws DataAccessException {
        configureDatabase();
    }

    private void configureDatabase() throws DataAccessException {
        DatabaseManager.createDatabase();
        try (var conn = DatabaseManager.getConnection()) {
            for (var preparedStatement = createStatements) {
                try (var preparedStatement = conn.prepareStatement(preparedStatement)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException ex) {
            // change this!
            throw new ResponseException(500, String.format("something"));
        }
    }
}
