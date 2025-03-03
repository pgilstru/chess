package handler;

import org.eclipse.jetty.server.Authentication;
import service.ClearService;
import dataaccess.*;
import service.GameService;

public class ClearHandler {
    private AuthDAO authDAO;
    private GameDAO gameDAO;
    private UserDAO userDAO;

    public ClearHandler(AuthDAO authDAO, GameDAO gameDAO, UserDAO userDAO){
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
        this.userDAO = userDAO;
    }

    // convert HTTP clear() request into Java usable objects and data

    // call the appropriate service

    // when the service responds, convert the response object back to JSON

    // send the HTTP response with the new JSON
}
