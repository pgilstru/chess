package dataaccess;

import dataaccess.sql.SQLUserDAO;
import model.UserData;
import org.junit.jupiter.api.*;

public class UserDAOTest {

    private static SQLUserDAO userDAO;

    @BeforeAll
    public static void beforeAll() throws DataAccessException {
        userDAO = new SQLUserDAO();
    }

    @BeforeEach
    public void clear() throws DataAccessException {
        userDAO.clear();
    }

    @Test
    @DisplayName("Should successfully create a new user")
    void successfulCreateUser() throws DataAccessException {
        // create some user data
        UserData userData = new UserData("testUser", "password", "kimkim@kimkim.kim");

        // verify no exception gets thrown and user is created successfully
        Assertions.assertDoesNotThrow(() -> userDAO.createUser(userData));

        // verify userData is correct
        UserData retrievedUser = userDAO.getUser("testUser");

        Assertions.assertAll(
                () -> Assertions.assertNotNull(retrievedUser),
                () -> Assertions.assertEquals("testUser", retrievedUser.username()),
                () -> Assertions.assertEquals("kimkim@kimkim.kim", retrievedUser.email())
        );
    }

    @Test
    @DisplayName("Should fail to create a new user if username already exists")
    void failedCreateUserNameTaken() throws DataAccessException {
        // create some user data
        UserData userData1 = new UserData("testUser", "password", "kimkim@kimkim.kim");
        Assertions.assertDoesNotThrow(() -> userDAO.createUser(userData1));

        // attempt to create a user with the same username (should fail)
        UserData userData2 = new UserData("testUser", "diffpassword", "jamjam@jamjam.jam");

        // verify an exception gets thrown and the user is not created successfully
        Assertions.assertThrows(DataAccessException.class, () -> userDAO.createUser(userData2));
    }
}
