package Controllers;

import java.util.HashMap;
import java.util.Map;

public class NavigationControllerS {
    private static NavigationControllerS instance;
    private Map<String, Object> sharedData;

    private NavigationControllerS() {
        sharedData = new HashMap<>();
    }

    public static NavigationControllerS getInstance() {
        if (instance == null) {
            instance = new NavigationControllerS();
        }
        return instance;
    }

    public void addData(String key, Object value) {
        sharedData.put(key, value);
    }

    public Object getSharedData(String key) {
        return sharedData.get(key);
    }

    public void clearData(String key) {
        sharedData.remove(key);
    }

    public void clearAllData() {
        sharedData.clear();
    }
}