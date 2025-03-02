package model;

import java.util.UUID;

public record AuthData(String authToken, String username) {
    public static AuthData generateNewAuthData(String username) {
        String uuidString = UUID.randomUUID().toString(); // referenced uuidgenerator.net for this
        return new AuthData(uuidString, username);
    }
}
