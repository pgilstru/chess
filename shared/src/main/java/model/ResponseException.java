package model;

import com.google.gson.Gson;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class ResponseException extends RuntimeException {
    final private int statusCode;

    public ResponseException(int statusCode, String msg) {
        super(msg);
        this.statusCode = statusCode;
    }

    public String toJson() {
        return new Gson().toJson(Map.of("Message: ", getMessage(), "\nStatus: ", statusCode));
    }

    public static ResponseException fromJson(InputStream stream) {
        HashMap map = new Gson().fromJson(new InputStreamReader(stream), HashMap.class);
        int status = ((Double) map.get("status")).intValue();
        String msg = map.get("message").toString();
        return new ResponseException(status, msg);
    }

    public int statusCode() {
        return statusCode;
    }
}