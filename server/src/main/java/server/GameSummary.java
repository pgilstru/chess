package server;

import service.GameService;

public class GameSummary {
    private int gameID;
    private String gameName;
    private String whiteUsername;
    private String blackUsername;

    public GameSummary (int gameID, String whiteUsername, String blackUsername, String gameName) {
        this.gameID = gameID;
        this.gameName = gameName;
        this.whiteUsername = whiteUsername;
        this.blackUsername = blackUsername;
    }

    public int getGameID() {
        return gameID;
    }

    public String getWhiteUsername() {
        return whiteUsername;
    }

    public String getBlackUsername() {
        return blackUsername;
    }

    public String getGameName() {
        return gameName;
    }
}
