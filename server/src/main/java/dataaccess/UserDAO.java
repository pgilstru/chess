package dataaccess;

import model.UserData;

public interface UserDAO {
    // A method for clearing all data from the database.
    // DataAccessException is thrown if the data can't be accessed (for any reason)
    void clear() throws DataAccessException;

    // Retrieve a user with the given username.
    // DataAccessException is thrown if the data can't be accessed (for any reason)
    UserData getUser(String username) throws DataAccessException;

    // Create a new user.
    // DataAccessException is thrown if the data can't be accessed (for any reason)
    void createUser(UserData userData) throws DataAccessException;
}
