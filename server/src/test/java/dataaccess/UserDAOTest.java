package dataaccess;

import dataaccess.memory.MemoryUserDAO;
import dataaccess.sql.SQLUserDAO;
import model.UserData;
import org.eclipse.jetty.server.Authentication;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class UserDAOTest {

    private SQLUserDAO getUserDAO(Class<? extends SQLUserDAO> DAOclass) throws DataAccessException {
        SQLUserDAO userDAO;
//        if (DAOclass.equals(SQLUserDAO.class)) {
            userDAO = new SQLUserDAO();
//        } else {
//            userDAO = new MemoryUserDAO();
//        }
        userDAO.clear();
        return userDAO;
    }

    @ParameterizedTest
    @ValueSource(classes = {SQLUserDAO.class})
    void successfulCreateUser(Class<? extends SQLUserDAO> DAOclass) throws DataAccessException {
        SQLUserDAO userDAO = getUserDAO(DAOclass);

        // create some user data
        UserData userData = new UserData("testUser", "password", "kimkim@kimkim.kim");

        // verify no exception gets thrown and the user is created successfully
        Assertions.assertDoesNotThrow(() -> userDAO.createUser(userData));
    }

    @ParameterizedTest
    @ValueSource(classes = {SQLUserDAO.class})
    void failedCreateUserNameTaken(Class<? extends SQLUserDAO> DAOclass) throws DataAccessException {
        SQLUserDAO userDAO = getUserDAO(DAOclass);

        // create some user data
        UserData userData1 = new UserData("testUser", "password", "kimkim@kimkim.kim");
        Assertions.assertDoesNotThrow(() -> userDAO.createUser(userData1));

        // attempt to create a user with the same username (should fail)
        UserData userData2 = new UserData("testUser", "diffpassword", "jamjam@jamjam.jam");

        // verify an exception gets thrown and the user is not created successfully
        Assertions.assertThrows(DataAccessException.class, () -> userDAO.createUser(userData2));
    }
}
