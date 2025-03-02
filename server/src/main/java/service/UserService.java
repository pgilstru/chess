package service;

import dataaccess.UserDAO;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import model.UserData;
import model.AuthData;

public class UserService {
    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public UserService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

//    public RegisterResult register(RegisterRequest registerRequest) {}
    public AuthData register(UserData userData) {
        try {
            // verify userData isn't empty
            if (userData == null) {
                throw new IllegalArgumentException("register input must not be null");
            }

            // verify username doesn't already exist
            String username = userData.username();
            if (userDAO.getUser(username) != null) {
                throw new IllegalArgumentException("username is already taken");
            }

            // username is available, create new userData
            String password = userData.password();
            String email = userData.email();
            UserData user = new UserData(username, password, email);

            // actually create the user
            userDAO.createUser(user);

            // create new authData
            AuthData authData = AuthData.generateNewAuthData(username);
            authDAO.createAuth(authData);

            return authData;
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

//    public LoginResult login(LoginRequest loginRequest) {}
    public AuthData login(UserData userData) {
        try {
            // verify userData isn't empty
            if (userData == null) {
                throw new IllegalArgumentException("login input must not be null");
            }

            String username = userData.username();
            UserData currUser = userDAO.getUser(username);

            // verify user exists and compare entered password to password stored
            if (currUser == null || !userData.password().equals(currUser.password())) {
                // say username or password to be more secure
                throw new IllegalArgumentException("username or password is incorrect");
            }

            // create new authData
            AuthData authData = AuthData.generateNewAuthData(username);
            authDAO.createAuth(authData);

            return authData;
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

//    public void logout(LogoutRequest logoutRequest) {}
    public void logout(String authToken) {
        try {
            // verify user is authenticated before logging out
            if (authDAO.getAuth(authToken) == null) {
                throw new IllegalArgumentException("Must be authenticated to logout");
            }

            // delete authToken
            authDAO.deleteAuth(authToken);

        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
