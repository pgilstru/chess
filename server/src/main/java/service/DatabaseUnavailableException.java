package service;

import dataaccess.DataAccessException;

public class DatabaseUnavailableException extends DataAccessException {
    public DatabaseUnavailableException(String message) {
        super(message);
    }
}
