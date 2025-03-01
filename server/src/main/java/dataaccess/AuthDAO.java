package dataaccess;

import model.AuthData;

public interface AuthDAO {
    // A method for clearing all data from the database.
    // DataAccessException is thrown if the data can't be accessed (for any reason)
    void clear() throws DataAccessException;

    // Retrieve an authorization given an authToken.
    // DataAccessException is thrown if the data can't be accessed (for any reason)
    AuthData getAuth(String authToken) throws DataAccessException;

    // Create a new authorization.
    // DataAccessException is thrown if the data can't be accessed (for any reason)
    void createAuth(AuthData authData) throws DataAccessException;

    // Delete an authorization so that it is no longer valid.
    // DataAccessException is thrown if the data can't be accessed (for any reason)
    void deleteAuth(String authToken) throws DataAccessException;
}
