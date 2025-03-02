package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.GameData;

public class GameService {
    private GameDAO gameDAO;
    private AuthDAO authDAO;

    public GameService(GameDAO gameDAO, AuthDAO authDAO) {
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

    // public CreateResult create(CreateRequest createRequest) {}
    public GameData create(GameData gameName, String authToken) {
        try {
            // verify user is authenticated first
            if (authDAO.getAuth(authToken) == null) {
                throw new IllegalArgumentException("Must be authenticated to logout");
            }

            // create a new game
            gameDAO.createGame(gameName);


        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    // public JoinResult join(JoinRequest joinRequest) {}
    // public ListResult list(ListRequest listRequest) {}
}
