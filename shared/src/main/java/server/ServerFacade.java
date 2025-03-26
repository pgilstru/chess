package server;

import com.google.gson.Gson;
import com.google.gson.JsonSerializer;
import model.AuthData;
import model.GameData;
import model.JoinRequest;
import model.UserData;
import model.ResponseException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class ServerFacade {

    private final String serverUrl;
    private String authToken;

    public ServerFacade(String url) {
        this.serverUrl = url;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public void clearDB() throws ResponseException {
        var path = "/db";
        this.makeRequest("DELETE", path, null, null, null);
    }

    // createGame
    public GameData createGame(GameData gameData) {

        if (gameData == null) {
            throw new ResponseException(400, "Must provide auth data");
        }
        var path = "/game";
//        return this.makeRequest("POST", path, gameData, GameData.class, authToken);
        GameData res = null;
        try {
            res = this.makeRequest("POST", path, gameData, GameData.class, authToken);
        } catch (ResponseException e) {
            // Handle exception if needed
            System.out.println("Error during createGame: " + e.getMessage());
        }
        return res;
    }

    // joinGame
    public void joinGame(JoinRequest joinRequest) {
        if (authToken == null || authToken.isEmpty()) {
            throw new ResponseException(401, "user not logged in");
        }
        var path = "/game";
//        this.makeRequest("PUT", path, joinRequest, null, null);
        try {
            this.makeRequest("PUT", path, joinRequest, null, authToken);
        } catch (ResponseException e) {
            // Handle exception if needed
            System.out.println("Error during logout: " + e.getMessage());
        }
    }

    // listGames
    public List<GameData> listGames() {
        var path = "/game";
//        Map<String, List<GameData>> res = this.makeRequest("GET", path, null, new TypeToken);
        record listGameResponse(List<GameData> games) {
        }
        var res = this.makeRequest("GET", path, null, listGameResponse.class, null);
        return res.games;
//        return res.get("games");
    }

    // login
    public AuthData login(UserData userData) {
        var path = "/session";
        return makeRequest("POST", path, userData, AuthData.class, null);
    }

    // logout
    public void logout(String authToken) {
        if (authToken == null || authToken.isEmpty()) {
            throw new ResponseException(401, "user not logged in");
        }
        var path = "/session";
        try {
            this.makeRequest("DELETE", path, null, null, authToken);
        } catch (ResponseException e) {
            // Handle exception if needed
            System.out.println("Error during logout: " + e.getMessage());
        }
    }

    // register
    public AuthData register(UserData userData) {
        var path = "/user";
        return makeRequest("POST", path, userData, AuthData.class, null);
    }

    // makeRequest
    // request handler for http requests
    private <T> T makeRequest(String method, String path, Object req, Class<T> responseClass, String authToken) throws ResponseException {
        try {
            URL url = (new URI(serverUrl + path)).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);
            http.setDoOutput(true);


            if (authToken != null && !authToken.isEmpty()) {
                http.setRequestProperty("authorization", "Bearer " + authToken);
            }

            writeBody(req, http);
            http.connect();
            throwIfUnsuccessful(http);
            return readBody(http, responseClass);
        } catch (ResponseException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseException(500, "Server communication error: " + ex.getMessage());
        }
    }

    private static void writeBody(Object request, HttpURLConnection http) throws IOException {
        if (request != null) {
            http.addRequestProperty("Content-Type", "application/json");
            String reqData = new Gson().toJson(request);
            try (OutputStream reqBody = http.getOutputStream()) {
                reqBody.write(reqData.getBytes());
            }
        }
    }

    private void throwIfUnsuccessful(HttpURLConnection http) throws IOException, ResponseException {
        var status = http.getResponseCode();
        if (!isSuccessful(status)) {
            try (InputStream respErr = http.getErrorStream()) {
                if (respErr != null) {
                    throw ResponseException.fromJson(respErr);
                }
            }

            throw new ResponseException(status, "Server error: " + status);
        }
    }

    private static <T> T readBody(HttpURLConnection http, Class<T> responseClass) throws IOException {
        T response = null;
        if (http.getContentLength() < 0) {
            try (InputStream respBody = http.getInputStream()) {
                InputStreamReader reader = new InputStreamReader(respBody);
                if (responseClass != null) {
                    response = new Gson().fromJson(reader, responseClass);
                }
            }
        }
        return response;
    }


    private boolean isSuccessful(int status) {
//        return status / 100 == 2;
        return status >= 200 && status < 300;
    }
}
