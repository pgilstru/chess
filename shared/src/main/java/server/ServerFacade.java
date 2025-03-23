package server;

import com.google.gson.Gson;
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

public class ServerFacade {

    private final String serverUrl;

    public ServerFacade(String url) {
        this.serverUrl = url;
    }

    // createGame
    public GameData createGame(GameData gameData) {
        var path = "/game";
        return this.makeRequest("POST", path, gameData, GameData.class);
    }

    // joinGame
    public void joinGame(JoinRequest joinRequest) {
        var path = "/game";
        this.makeRequest("PUT", path, joinRequest, null);
    }

    // listGames
    public List<GameData> listGames() {
        var path = "/game";
        record listGameResponse(List<GameData> games) {
        }
        var res = this.makeRequest("GET", path, null, listGameResponse.class);
        return res.games;
    }

    // login
    public AuthData login(UserData userData) {
        var path = "/session";
        return makeRequest("POST", path, userData, AuthData.class);
    }

    // logout
    public void logout(UserData userData) {
        var path = "/session";
        makeRequest("DELETE", path, null, null);
    }

    // register
    public AuthData register(UserData userData) {
        var path = "/user";
        return makeRequest("POST", path, userData, AuthData.class);
    }

    // makeRequest
    // request handler for http requests
    private <T> T makeRequest(String method, String path, Object req, Class<T> responseClass) throws ResponseException {
        try {
            URL url = (new URI(serverUrl + path)).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);
            http.setDoOutput(true);

            writeBody(req, http);
            http.connect();
            throwIfUnsuccessful(http);
            return readBody(http, responseClass);
        } catch (ResponseException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseException(500, ex.getMessage());
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

            throw new ResponseException(status, "other failure: " + status);
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
        return status / 100 == 2;
    }
}
