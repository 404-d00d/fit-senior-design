package Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

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
          // Use ProcessBuilder to run the Python script with the image path
          ProcessBuilder pb = new ProcessBuilder("C:\\Users\\cnede\\AppData\\Local\\Programs\\Python\\Python312\\python.exe", "src/BarcodeModule.py", imagePath);
          pb.redirectErrorStream(true);  // Redirect errors to the output stream
          Process process = pb.start();

          // Capture the output from the Python script
          BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
          String result = in.lines().collect(Collectors.joining("\n"));
          process.waitFor();  // Wait for the process to finish
          System.out.println(result);  // Print the result to the terminal
      } catch (Exception e) {
          e.printStackTrace();
          System.out.println("Error: " + e.getMessage());
      }
  }
}