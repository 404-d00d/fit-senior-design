package org.javafx.Controllers;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

import org.javafx.Main.Main;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class InventoryDashboardController {

   @FXML
   private Button addIngredientButton;

   @FXML
   private Button spacesButton;

   @FXML
   private Button placesButton;

   @FXML
   private Button menuButton;

   @FXML
   private VBox menuPane;

   @FXML
   private Button inventoryButton;

   @FXML
   private Button recipesButton;

   @FXML
   private Button inboxButton;

   @FXML
   private Button browseRecipesButton;

   @FXML
   private Button profileButton;

   @FXML
   private Button settingsButton;

   @FXML
   private Button shoppingListsButton;

   @FXML
   private Button neededIngredientsButton;

   @FXML
   private Button cancelButton;

   @FXML
   private Button manualButton;

   @FXML
   private Pane uploadPane;

   @FXML
   private Pane spacesPane;

   @FXML
   private Pane placesPane;

   private boolean spaces;
   private boolean places;

   @FXML
   private void initialize() {
   
      menuButton.setOnAction(event -> {
         try {
            if(menuPane.isVisible()) {
               menuPane.setVisible(false); // hide menu pane
            }
            else {
               menuPane.setVisible(true); // show menu pane
            }
         } catch (Exception e) {
            e.printStackTrace();
         }
      });

      addIngredientButton.setOnAction(event -> {
         try {

            if(spacesPane.isVisible()) {
               spacesPane.setVisible(false);
               spaces = true;
            }
            else if(placesPane.isVisible()) {
               placesPane.setVisible(false);
               places = true;
            }

            uploadPane.setVisible(true);


         } catch (Exception e) {
            e.printStackTrace();
         }
      });

      cancelButton.setOnAction(event -> {
         try {
            uploadPane.setVisible(false);

            if (spaces) {
               spacesPane.setVisible(true);
            }
            else if (places) {
               placesPane.setVisible(true);
            }

         } catch (Exception e) {
            e.printStackTrace();
         }
      });

      // Switch to Inventroy Screen
      inventoryButton.setOnAction(event -> {
         try {
            Main.showInventoryScreen(); // Switch to Inventory Screen
         } catch (Exception e) {
            e.printStackTrace();
         }
      });

      setHoverEffect(inventoryButton);

      // Switch to MyRecipes Screen
      recipesButton.setOnAction(event -> {
         try {
            Main.showMyRecipesScreen();  // Switch to MyRecipes
         } catch (Exception e) {
            e.printStackTrace();
         }
      });

      setHoverEffect(recipesButton);

      // Switch to Inbox Screen
      inboxButton.setOnAction(event -> {
         try {
            //Main.  // Switch to ...
         } catch (Exception e) {
            e.printStackTrace();
         }
      });

      setHoverEffect(inboxButton);

      // Switch to Browse Recipes Screen
      browseRecipesButton.setOnAction(event -> {
         try {
            //Main.  // Switch to ...
         } catch (Exception e) {
            e.printStackTrace();
         }
      });

      setHoverEffect(browseRecipesButton);

      // Switch to Profile Screen
      profileButton.setOnAction(event -> {
         try {
            //Main.  // Switch to ...
         } catch (Exception e) {
            e.printStackTrace();
         }
      });

      setHoverEffect(profileButton);
      
      // Switch to Browse Settings Screen
      settingsButton.setOnAction(event -> {
         try {
            //Main.  // Switch to ...
         } catch (Exception e) {
            e.printStackTrace();
         }
      });

      setHoverEffect(settingsButton);

      // Switch to Shopping Lists Screen
      shoppingListsButton.setOnAction(event -> {
         try {
            //Main.  // Switch to ...
         } catch (Exception e) {
            e.printStackTrace();
         }
      });

      setHoverEffect(shoppingListsButton);

      // Switch to NeededIngredients Screen
      neededIngredientsButton.setOnAction(event -> {
         try {
            //Main.  // Switch to ...
         } catch (Exception e) {
            e.printStackTrace();
         }
      });

      setHoverEffect(neededIngredientsButton);
   }

   private void setHoverEffect(Button button) {
      button.setOnMouseEntered(this::handleMouseEntered);
      button.setOnMouseExited(this::handleMouseExited);
  }

   private void handleMouseEntered(MouseEvent event) {
      Button button = (Button) event.getSource();
      // Change style when mouse enters
      button.setStyle("-fx-background-color: orange; -fx-text-fill: white; -fx-wrap-text: true; -fx-font-size: 40px;");
   }

   private void handleMouseExited(MouseEvent event) {
      Button button = (Button) event.getSource();
      // Reset style when mouse exits
      button.setStyle("-fx-background-color: transparent; -fx-text-fill: black; -fx-wrap-text: true; -fx-font-size: 40px;");
   }

    @FXML
   private void onBarcodeUpload() {
       // Open the file chooser for the user to select an image
       FileChooser fileChooser = new FileChooser();
       fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
       fileChooser.setTitle("Select Barcode Image(s)");
       
       // Get the current stage (window)
       Stage stage = (Stage) manualButton.getScene().getWindow();
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
          // Use environment to find Python if it's in the PATH
            ProcessBuilder pb = new ProcessBuilder(
                "python",                      // Will use the 'python' command from PATH
                "src/Barcode Module/BarcodeModule.py",         // Relative path to the script
                imagePath                       // Image path passed as argument
            );

            // Set the working directory to the project's base folder
            pb.directory(new File(System.getProperty("user.dir"))); 

            pb.redirectErrorStream(true);  // Redirect errors to the output stream
            Process process = pb.start();

            // Capture the output from the Python script
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String result = in.lines().collect(Collectors.joining("\n"));
            process.waitFor();  // Wait for the process to finish
            System.out.println(result);  // Print the result to the terminal
        } 
        
        catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error: " + e.getMessage());
        }
   }

}
