package org.javafx.Controllers;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import java.util.ArrayList;
import org.javafx.Item.Item;

import org.javafx.Main.Main;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;



public class InventoryDashboardController {

   @FXML
   private Button addIngredientButton, spacesButton, placesButton, menuButton, inventoryButton, myRecipesButton, inboxButton,
                  browseRecipesButton, profileButton, settingsButton, myListsButton, cancelButton, manualButton, closeIngredient, 
                  addTagButton, imageSelect, removeButton, saveButton, productIDSearchButton,
                  userDashboardButton, mealPlannerButton;

   @FXML
   private VBox menuPane;

   @FXML
   private TextField productID; // Field to enter UPC
   
   @FXML
   private TextField productName;   
   
   @FXML
   private TextField productQuantity;   
   
   @FXML
   private TextField productTag;      

   @FXML
   private ComboBox<String> productUnit;

   @FXML
   private ComboBox<String> productLoc;

   @FXML
   private DatePicker productEXPDate;

   @FXML
   private Pane uploadPane;

   @FXML
   private Pane spacesPane;

   @FXML
   private Pane placesPane, inventoryPane;

   @FXML
   private Pane addIngredientMenuPane;

   private boolean spaces = true;
   private boolean places = false;

   private File selectedImageFile;

   @FXML
   private ListView<String> tagsListView; // To display added tags
   private ObservableList<String> tags = FXCollections.observableArrayList();

   private ArrayList<Item> ingredientInventory = new ArrayList<Item>();

   @FXML
   private void initialize() {

      //Main.setScale(inventoryPane);

      productUnit.getItems().addAll("kg", "g", "l", "ml", "oz", "lbs");
      productLoc.getItems().addAll("Fridge", "Freezer", "Pantry", "Cabinet");
      
      // Link the list of tags to the ListView for display
      //tagsListView.setItems(tags);

      //<ListView fx:id="tagsListView" /> this can be added to see them in the ui
      //Later, we can replace the ListView with a more visually appealing UI (e.g., chips or tags).
      addTagButton.setOnAction(event -> {
         try {
            String tag = productTag.getText().trim();
            if (!tag.isEmpty()) {
               tags.add(tag);  // Add the tag to the list
               productTag.clear();  // Clear the input field after adding the tag
            }
         } catch (Exception e) {
            e.printStackTrace();
         }
      });

      imageSelect.setOnAction(event -> {
         try {
            SelectImage();
         } catch (Exception e) {
            e.printStackTrace();
         }
      });

      saveButton.setOnAction(event -> {
         try {
            saveIngredient();
         } catch (Exception e) {
            e.printStackTrace();
         }
      });

      productIDSearchButton.setOnAction(event -> {
         try {
            fetchProductDetails();
         } catch (Exception e) {
            e.printStackTrace();
         }
      });

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

      spacesButton.setOnAction(event -> {
         try {
         placesPane.setVisible(false);
         places = false;

         spacesPane.setVisible(true);
         spaces = true;
         } catch (Exception e) {
            e.printStackTrace();
         }
      });

      setClickEffect(spacesButton);

      placesButton.setOnAction(event -> {
         try {
         spacesPane.setVisible(false);
         spaces = false;

         placesPane.setVisible(true);
         places = true;
         } catch (Exception e) {
            e.printStackTrace();
         }
      });

      setClickEffect(placesButton);

      manualButton.setOnAction(event -> {
         try {
            uploadPane.setVisible(false);
            addIngredientMenuPane.setVisible(true);

         } catch (Exception e) {
            e.printStackTrace();
         }
      });

      closeIngredient.setOnAction(event -> {
         try {
            addIngredientMenuPane.setVisible(false);

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
      myRecipesButton.setOnAction(event -> {
         try {
            Main.showMyRecipesScreen();  // Switch to MyRecipes
         } catch (Exception e) {
            e.printStackTrace();
         }
      });

      setHoverEffect(myRecipesButton);

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

      // Switch to My Lists Screen
      myListsButton.setOnAction(event -> {
         try {
            Main.showMyListsScreen();
         } catch (Exception e) {
            e.printStackTrace();
         }
      });

      setHoverEffect(myListsButton);

      // Switch to userDashboard Screen
      userDashboardButton.setOnAction(event -> {
         try {
            Main.showUserDashboardScreen();
         } catch (Exception e) {
            e.printStackTrace();
         }
      });

      setHoverEffect(userDashboardButton);

      // Switch to mealPlanner Screen
      mealPlannerButton.setOnAction(event -> {
         try {

         } catch (Exception e) {
            e.printStackTrace();
         }
      });

      setHoverEffect(mealPlannerButton);
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

   private void setClickEffect(Button button) {
      button.setOnMouseClicked(this::handleMouseClicked);
   }

   private void handleMouseClicked(MouseEvent event) {

      if(spaces) {

         spacesButton.setStyle("-fx-border-color: orange; -fx-background-color: darkgrey; -fx-background-radius: 50; -fx-border-radius: 50; -fx-border-width: 4; -fx-font-size: 30;");
         placesButton.setStyle("-fx-border-color: transparent; -fx-background-color: darkgrey; -fx-background-radius: 50; -fx-border-radius: 50; -fx-border-width: 4; -fx-font-size: 30;");

      } else {

         spacesButton.setStyle("-fx-border-color: transparent; -fx-background-color: darkgrey; -fx-background-radius: 50; -fx-border-radius: 50; -fx-border-width: 4; -fx-font-size: 30;");
         placesButton.setStyle("-fx-border-color: orange; -fx-background-color: darkgrey; -fx-background-radius: 50; -fx-border-radius: 50; -fx-border-width: 4; -fx-font-size: 30;");
      }

      
   }

   //create the py script to get the product details similar to the barcode scanner script
   private void fetchProductDetails() {
      String upc = productID.getText().trim();
      if (!upc.isEmpty()) {
         try {
            //ProcessBuilder pb = new ProcessBuilder("python", "src/main/python/get_product_details.py", upc);
            //Process process = pb.start();
            
            //BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            //String line;
            //while ((line = in.readLine()) != null) {
                  //System.out.println("Python Output: " + line);  // Simulating fetching details
                  // Parse and fill in the product details here
            //}
         } catch (Exception e) {
            e.printStackTrace();
         }
      }
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

   private void saveIngredient() {

      if (isFormValid()) {
         // Get values from input fields
         String ingredientName = productName.getText();
         String quantity = productQuantity.getText();
         String unit = productUnit.getValue();
         String location = productLoc.getValue();
         LocalDate expirationDate = productEXPDate.getValue();
         String convertedDate = expirationDate.toString();

         DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd"); // Define your desired date pattern
         String expirationDateString = expirationDate.format(formatter); // Convert LocalDate to String

         // Print the values to the terminal for testing
         System.out.println("Ingredient Name: " + ingredientName);
         System.out.println("Quantity: " + quantity);
         System.out.println("Unit: " + unit);
         System.out.println("Location: " + location);
         System.out.println("Expiration Date: " + expirationDateString);
         //System.out.println("Selected Image: " + selectedImageFile.getAbsolutePath());

         // In the future, we will use these values to add to the database
         // Example: db.insertIngredient(ingredientName, quantity, unit, location, expirationDate);
         
         Item newIngredient = new Item(ingredientName, "0", Integer.parseInt(quantity), unit, location, convertedDate);

         ingredientInventory.add(newIngredient);

         productName.clear();
         productQuantity.clear();
         productUnit.setValue(null);
         productLoc.setValue(null);
         productEXPDate.setValue(null);
         selectedImageFile = null;

         for (int j = 0; j < ingredientInventory.size(); j++) {

            System.out.println(ingredientInventory.get(j).getName()+", "+ingredientInventory.get(j).getQuantity()+" of "+ingredientInventory.get(j).getUnit());
         }

         addIngredientMenuPane.setVisible(false);

         if (spaces) {
            spacesPane.setVisible(true);
         }
         else if (places) {
            placesPane.setVisible(true);
         }
      }

      else {
         // Show alert if the form is not valid
         Alert alert = new Alert(AlertType.ERROR);
         alert.setTitle("Form Error");
         alert.setHeaderText(null);
         alert.setContentText("Please fill in all required fields.");
         alert.showAndWait();
     }


   }

   private boolean isFormValid() {
      // Check if required fields are filled
      return !productName.getText().trim().isEmpty() &&
             !productQuantity.getText().trim().isEmpty();
   }

   private void SelectImage() {
      FileChooser fileChooser = new FileChooser();
      fileChooser.setTitle("Select Ingredient Image");

      // Restrict to image file types
      fileChooser.getExtensionFilters().add(
         new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
      );

      Stage stage = (Stage) imageSelect.getScene().getWindow();  // Get the current stage
      selectedImageFile = fileChooser.showOpenDialog(stage);

      if (selectedImageFile != null) {
         // process or store the selected image
      }
   }

   // Method to run the Python script and print the result to the terminal
   private void runPythonScript(String imagePath) {
        try {
          // Use ProcessBuilder to run the Python script with the image path
          // Use environment to find Python if it's in the PATH
            ProcessBuilder pb = new ProcessBuilder(
                "python",                      // Will use the 'python' command from PATH
                "src/main/python/Barcode Module/BarcodeModule.py",         // Relative path to the script
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
