package service;

import dataaccess.memory.MemoryAuthDAO;
import dataaccess.memory.MemoryGameDAO;
import dataaccess.memory.MemoryUserDAO;
import model.AuthData;
import model.ResponseException;
import model.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UserServiceTest {

    private static MemoryUserDAO userDAO;
    private static MemoryAuthDAO authDAO;
    private static MemoryGameDAO gameDAO;
    private static UserService userService;

    @BeforeAll
    public static void beforeAll() {
        userDAO = new MemoryUserDAO();
        authDAO = new MemoryAuthDAO();
        gameDAO = new MemoryGameDAO();
        userService = new UserService(userDAO, authDAO);
    }

    @BeforeEach
    public void clear() throws ResponseException, DatabaseUnavailableException {
        // clear everything before running each test
        new ClearService(userDAO, authDAO, gameDAO).clear();
    }

    @Test
    public void successfulRegister() {
        // tests successfully registering
        UserData userData = new UserData("testUser", "password", "kimkim@kimkim.kim");

        // try to register, should receive authData back if successful
        AuthData authData = userService.register(userData);

        // verify you got authData back (aka were successful)
        Assertions.assertNotNull(authData);

        // verify it registered the correct info and such
        Assertions.assertEquals("testUser", authData.username());
    }

    @Test
    public void failedRegisterNameTaken() {
        // tests registering with an unavailable username (should fail)

        // first, need to register with an available username (this will be the existing user)
        UserData userData = new UserData("testUser", "password", "kimkim@kimkim.kim");
        userService.register(userData);

        // attempt to register with the same username again
        UserData userData2 = new UserData("testUser", "diffpassword", "jamjam@jamjam.jam");

        // verify it threw an exception (it should have)
        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class,
                () -> userService.register(userData2));
        Assertions.assertEquals("username is already taken", thrown.getMessage());
    }

    @Test
    public void successfulLogin() {
        // tests successfully logging in with correct credentials

        // first, need to register
        UserData userData = new UserData("testUser", "password", "kimkim@kimkim.kim");
        userService.register(userData);

        // attempt to login with registered credentials
        AuthData loginAuthData = userService.login(userData);

        // verify you got authData back (aka were successful in logging in)
        Assertions.assertNotNull(loginAuthData);

        // verify it logged in and isn't just grabbing other data or returning wrong
        Assertions.assertEquals("testUser", loginAuthData.username());
    }

    @Test
    public void failedLoginIncorrectPW() {
        // tests logging in with an incorrect password (should fail)
        // first, need to register
        UserData userData = new UserData("testUser", "password", "kimkim@kimkim.kim");
        userService.register(userData);

        // attempt to login with wrong password
        UserData incorrectUserData = new UserData("testUser", "wrongPassword", "kimkim@kimkim.kim");
        AuthData loginAuthData = userService.login(incorrectUserData);

        // verify it returned null (aka password was incorrect)
        Assertions.assertNull(loginAuthData);
    }

    @Test
    public void successfulLogout() {
        // tests successfully logging out
        // first, need to register
        UserData userData = new UserData("testUser", "password", "kimkim@kimkim.kim");
        userService.register(userData);

        // then, login
        AuthData authData = userService.login(userData);

        // verify no exception is thrown when attempting to logout with valid authToken
        Assertions.assertDoesNotThrow(() -> userService.logout(authData.authToken()));
    }

    @Test
    public void failedLogoutUnAuth() {
        // attempt to logout while not logged in
        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class,
                () -> userService.logout(null));
        Assertions.assertEquals("Must be authenticated to logout", thrown.getMessage());
    }
}
