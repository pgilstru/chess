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
                throw new IllegalArgumentException("input must not be null");
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

            // create authData
            AuthData authData = AuthData.generateNewAuthData(username);


        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

//    public LoginResult login(LoginRequest loginRequest) {}
//    public void logout(LogoutRequest logoutRequest) {}
}
