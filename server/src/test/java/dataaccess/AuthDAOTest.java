package dataaccess;

import dataaccess.sql.SQLAuthDAO;
import model.*;
import org.junit.jupiter.api.*;

public class AuthDAOTest {

    private static SQLAuthDAO authDAO;

    @BeforeAll
    public static void beforeAll() throws DataAccessException {
        authDAO = new SQLAuthDAO();
    }

    @BeforeEach
    public void clear() throws DataAccessException {
        authDAO.clear();
    }

    @Test
    @DisplayName("Should successfully create a new authToken")
    void successCreateAuth() throws DataAccessException {
        // create new authData object
        AuthData authData = new AuthData("Token", "testuser");

        // verify no exception gets thrown and auth is successfully created
        Assertions.assertDoesNotThrow(() -> authDAO.createAuth(authData));

        // retrieve auth data
        AuthData retrievedData = authDAO.getAuth("Token");

        // verify authData is correct
        Assertions.assertAll(
                () -> Assertions.assertNotNull(retrievedData),
                () -> Assertions.assertEquals("Token", retrievedData.authToken()),
                () -> Assertions.assertEquals("testuser", retrievedData.username())
        );
    }

    @Test
    @DisplayName("Should fail to create a new authToken if authToken already exists")
    void failedCreateAuthAlreadyAuth() throws DataAccessException {
        // add two authdata with same token
        AuthData authData1 = new AuthData("token", "user1");
        authDAO.createAuth(authData1);

        AuthData authData2 = new AuthData("token", "user2");

        // verify exception is thrown
        Assertions.assertThrows(DataAccessException.class, () -> authDAO.createAuth(authData2));
    }
}
