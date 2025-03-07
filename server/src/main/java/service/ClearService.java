package service;

import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import dataaccess.UserDAO;
import dataaccess.AuthDAO;

public class ClearService {
    private final UserDAO userDAO;
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;

    public ClearService(UserDAO userDAO, AuthDAO authDAO, GameDAO gameDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
    }

    // public void clear() {}
    public void clear() {
        try {
            // simulate a failure here (for example, a failure to connect to the database)
            if (userDAO == null || authDAO == null || gameDAO == null) {
                throw new DatabaseUnavailableException("database unavailable");
            } // this def needs to change, but will be a placeholder for now

            // clear user data, auth data, and game data
            userDAO.clear();
            authDAO.clear();
            gameDAO.clear();
        } catch (DatabaseUnavailableException e) {
            throw new RuntimeException("Database error", e);
        } catch (DataAccessException e) {
            // change this from runtime exception to something better?
            throw new RuntimeException("Data access error", e);
        }
    }
}
