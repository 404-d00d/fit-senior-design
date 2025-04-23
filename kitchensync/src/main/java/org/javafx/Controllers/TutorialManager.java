package org.javafx.Controllers;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

public class TutorialManager {
    private static final Map<String, Boolean> tutorialCompletedMap = new HashMap<>();
    private static final String FILE_PATH = "UserInformation.json";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static Map<String, Object> userData = new HashMap<>();

    static {
        tutorialCompletedMap.put("MealPlanner", false);
        tutorialCompletedMap.put("MyRecipes", false);
        tutorialCompletedMap.put("Inventory", false);
        tutorialCompletedMap.put("FindRecipes", false);
        tutorialCompletedMap.put("MyLists", false);
        tutorialCompletedMap.put("UserDashboard", false);

        loadUserData(); // Load overrides from file
    }

    public static boolean isCompleted(String section) {
        return tutorialCompletedMap.getOrDefault(section, true);
    }

    public static void markCompleted(String section) {
        tutorialCompletedMap.put(section, true);
        saveUserData();
    }

    private static void loadUserData() {
        try (Reader reader = new FileReader(FILE_PATH)) {
            Type type = new TypeToken<Map<String, Object>>(){}.getType();
            userData = gson.fromJson(reader, type);

            // Safely extract tutorialsCompleted section
            Object tutorialsObj = userData.get("tutorialsCompleted");
            if (tutorialsObj instanceof Map) {
               Map<?, ?> tutorialsRaw = (Map<?, ?>) tutorialsObj;
               for (Map.Entry<?, ?> entry : tutorialsRaw.entrySet()) {
                   String key = String.valueOf(entry.getKey());
                   Boolean value = Boolean.parseBoolean(String.valueOf(entry.getValue()));
                   tutorialCompletedMap.put(key, value);
               }
           }
           
        } catch (IOException e) {
            System.out.println("No tutorial file found, creating default.");
            userData = new HashMap<>();
            userData.put("tutorialsCompleted", new HashMap<String, Boolean>());
            saveUserData(); // Save default structure
        }
    }

    private static void saveUserData() {
        // Update the "tutorialsCompleted" section of userData
        userData.put("tutorialsCompleted", tutorialCompletedMap);

        try (Writer writer = new FileWriter(FILE_PATH)) {
            gson.toJson(userData, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
