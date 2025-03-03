package dataaccess.memory;

import dataaccess.GameDAO;
import dataaccess.DataAccessException;
import model.GameData;
import java.util.List;
import java.util.ArrayList;

import java.lang.reflect.Array;
import java.util.*;

public class MemoryGameDAO implements GameDAO {
    private final Map<Integer, GameData> games = new HashMap<>();

    @Override
    public void clear() {
        // A method for clearing all game data from the database.
        games.clear();
    }

    @Override
    public GameData getGame(int gameID) {
        // Retrieve a specified game with the given game ID.
        return games.get(gameID);
    }

    @Override
    public GameData createGame(GameData gameData) throws DataAccessException {
        // verify there is gameData to add
        if (gameData.game() == null) {
            throw new DataAccessException("Cannot create an empty game");
        }
        int gameIdCount = 1;
        while (getGame(gameIdCount) != null) {
            // increment the gameIdCount until there is an available one
            gameIdCount++;
        }

        String whiteUser = gameData.whiteUsername();
        String blackUser = gameData.blackUsername();
        String gameName = gameData.gameName();

        // Create a new game.
        gameData = new GameData(gameIdCount, whiteUser, blackUser, gameName, gameData.game());
        return games.put(gameIdCount, gameData);
    }

    @Override
    public List<GameData> listGames() {
        // Retrieve all games.
        return new ArrayList<>(games.values());
    }

    @Override
    public void updateGame(GameData gameData) throws DataAccessException{
        // verify the game to update exists
        if (!games.containsKey(gameData.gameID())) {
            throw new DataAccessException("Game to update doesn't exist");
        }

        // verify there is gameData to add
        if (gameData.game() == null) {
            throw new DataAccessException("Cannot create an empty game");
        }

        // Updates a chess game.
        games.put(gameData.gameID(), gameData);
    }
}
