package org.javafx.Controllers;

import java.io.*;
import java.util.*;
import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class MacroCalculator {
    public static List<Map<String, Object>> calculateNutrition(List<Map<String, Object>> ingredients) {
        try {
            // Convert ingredient list to JSON string
            Gson gson = new Gson();
            String jsonInput = gson.toJson(ingredients);

            // 🔹 Call Python script
            ProcessBuilder pb = new ProcessBuilder("python", "src/main/python/MacroCalculatorModule.py");
            pb.redirectErrorStream(true);
            Process process = pb.start();

            // 🔹 Write JSON data to Python script
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
            writer.write(jsonInput);
            writer.flush();
            writer.close();

            // 🔹 Read output from Python script
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }
            reader.close();
            process.waitFor();

            // 🔹 Parse JSON response using Gson
            Type listType = new TypeToken<List<Map<String, Object>>>() {}.getType();
            return gson.fromJson(output.toString(), listType);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}


