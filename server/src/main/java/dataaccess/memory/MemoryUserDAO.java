package dataaccess.memory;

import dataaccess.UserDAO;
import dataaccess.DataAccessException;
import model.UserData;

import java.util.HashMap;
import java.util.Map;

public class MemoryUserDAO implements UserDAO {
    private final Map<String, UserData> users = new HashMap<>();

    @Override
    public void clear() {
        // A method for clearing all user data from the database.
        users.clear();
    }

    @Override
    public UserData getUser(String username) throws DataAccessException{
        // verify username exists
        if (!users.containsKey(username)) {
            throw new DataAccessException("user doesn't exists");
        }

        // Retrieve a user with the given username.
        return users.get(username);
    }

    @Override
    public void createUser(UserData userData) throws DataAccessException{
        // verify user doesn't already exist (does this in userservice now...?)
//        if (users.containsKey(userData.username())) {
//            throw new DataAccessException("user already exists");
//        }

        // Create a new user.
        users.put(userData.username(), userData);
    }
}
