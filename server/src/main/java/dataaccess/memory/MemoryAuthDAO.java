package dataaccess.memory;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import model.AuthData;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.HashMap;

public class MemoryAuthDAO implements AuthDAO{
    // implementation of data access interface that stores server's data in main memory RAM

    // use a hashmap to store authTokens in RAM memory
    private final Map<String, AuthData> authTokens = new HashMap<>();

    @Override
    public void clear() {
        // A method for clearing all auth data from the database.
        authTokens.clear();
    }

    @Override
    public AuthData getAuth(String authToken) {
        // Retrieve an authorization given an authToken.
        return authTokens.get(authToken);
    }

    @Override
    public void createAuth(AuthData authData) throws DataAccessException{
        // check if a token already exists
        if (authTokens.containsKey(authData.authToken())) {
            throw new DataAccessException("An authToken already exists for user");
        }

        // Create a new authorization.
        authTokens.put(authData.authToken(), authData);
        System.out.println(authTokens);
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException{
        // verify there is a token to delete
        if (!authTokens.containsKey(authToken)) {
            throw new DataAccessException("The authToken cannot be found for user");
        }

        // Delete an authorization so that it is no longer valid.
        authTokens.remove(authToken);
    }
}
