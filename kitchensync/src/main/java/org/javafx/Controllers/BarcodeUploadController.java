package org.javafx.Controllers;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class BarcodeUploadController {

   @FXML
   private Button BarcodeUpload;  // This maps to the button in the FXML file

   // This method will be triggered when the user clicks the "Upload Barcode" button
   @FXML
   private void onBarcodeUpload() {
       // Open the file chooser for the user to select an image
       FileChooser fileChooser = new FileChooser();
       fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
       fileChooser.setTitle("Select Barcode Image(s)");
       
       // Get the current stage (window)
       Stage stage = (Stage) BarcodeUpload.getScene().getWindow();
       List<File> files = fileChooser.showOpenMultipleDialog(stage);
       
       if (files != null && !files.isEmpty()) {
         // For each selected file, call the Python script
         for (File file : files) {
             runPythonScript(file.getAbsolutePath());
         }
      }
   }

   // Method to run the Python script and print the result to the terminal
   private void runPythonScript(String imagePath) {
        try {
            String pythonScriptPath = "C:\\Users\\raddi\\Documents\\GitHub\\fit-senior-design\\kitchensync\\src\\main\\python\\PriceFinder.py";
            String productName = "orange juice";
            String pageNumber = "1";

            String[] command = {
                "python", pythonScriptPath, productName, pageNumber
            };

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream())
            );

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(",")) {
                    String[] parts = line.split(",");
                    double avgPrice = Double.parseDouble(parts[0]);
                    String unit = parts[1];

                    System.out.println("🛒 Product: " + productName);
                    System.out.println("💰 Avg Price: " + avgPrice + " per " + unit);
                    System.out.println("📦 Per Pound: " + (avgPrice * 16));
                    System.out.println("📏 Per Gram: " + (avgPrice * 28.35));
                    System.out.println("💧 Per mL: " + (avgPrice * 29.57));
                } else {
                    System.out.println("🔍 " + line); // for debug or error messages
                }
            }

            int exitCode = process.waitFor();
            System.out.println("✅ Python process exited with code: " + exitCode);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}