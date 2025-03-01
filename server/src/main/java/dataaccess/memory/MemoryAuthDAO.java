package dataaccess.memory;

import model.AuthData;
import java.util.Map;
import java.util.HashMap;

public class MemoryAuthDAO {
    // implementation of data access interface that stores server's data in main memory RAM
    private final Map<String, AuthData> authMap = new HashMap<>();
}
