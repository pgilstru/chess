package dataaccess;

import model.GameData;

import java.util.Collection;

public interface GameDAO {
    // A method for clearing all data from the database.
    // DataAccessException is thrown if the data can't be accessed (for any reason)
    void clear() throws DataAccessException;

    // Retrieve a specified game with the given game ID.
    // DataAccessException is thrown if the data can't be accessed (for any reason)
    GameData getGame(int gameID) throws DataAccessException;

    // Create a new game.
    // DataAccessException is thrown if the data can't be accessed (for any reason)
    GameData createGame(GameData gameData) throws DataAccessException;

    // Retrieve all games.
    // DataAccessException is thrown if the data can't be accessed (for any reason)
// possibly change this to a list instead of collection??
    Collection<GameData> listGames() throws DataAccessException;

    // Updates a chess game.
    // DataAccessException is thrown if the data can't be accessed (for any reason)
    void updateGame(GameData gameData) throws DataAccessException;
}
