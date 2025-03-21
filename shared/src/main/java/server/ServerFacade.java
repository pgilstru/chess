package server;

import model.GameData;
import model.JoinRequest;

public class ServerFacade {

    private final String serverUrl;

    public ServerFacade(String url) {
        this.serverUrl = url;
    }

    // createGame
    public GameData createGame(GameData gameData) {
        var path = "/create";
        return this.makeRequest("POST", path, gameData, GameData.class);
    }

    // joinGame
    public void joinGame(JoinRequest joinRequest) {
        var path = "/join";
        this.makeRequest("PUT", path, joinRequest, null);
    }

    // listGames
    public List<GameData> listGames() {
        var path = "/games";
        record listPetResponse(List<GameData> gameDataList) {
        }
        return this.makeRequest("GET", path, null, listPetResponse.class);
    }

}
