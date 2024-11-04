package org.javafx.Controllers;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import java.util.ArrayList;
import java.util.HashMap;

import org.javafx.Item.Item;

import org.javafx.Main.Main;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.scene.control.TextInputDialog;
import java.util.Optional;



public class InventoryDashboardController {

   @FXML
   private Button addIngredientButton, spacesButton, placesButton, menuButton, inventoryButton, myRecipesButton, inboxButton,
                  browseRecipesButton, profileButton, settingsButton, myListsButton, cancelButton, manualButton, closeIngredient, 
                  addTagButton, imageSelect, removeButton, saveButton, productIDSearchButton, addSpace, addCategory,
                  userDashboardButton, mealPlannerButton, fridgeButton, freezerButton, pantryButton;

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

   private Map<String, Map<String, FlowPane>> spacesCategories = new HashMap<>();
   private Map<String, List<String>> defaultCategories; // Map to hold categories for each space

   @FXML
   private FlowPane expiringSoonCategory, readyToEatCategory, dairyCategory, meatCategory;

   private Image selectedImage;

   @FXML
   private ScrollPane categoryScrollPane; // The scroll pane holding all categories

   @FXML
   private VBox categoryContainer, spacesButtons;

   private String currentSpace; // Track the currently selected space

   @FXML
   private void initialize() {

      setupDefaultCategories();

      // Set current space to default (e.g., "Fridge")
      currentSpace = "Fridge";

      // Set up the default view for Fridge when the pane loads
      loadCategoriesForSpace("Fridge");
      styleActiveButton(fridgeButton);

      // Set up button event handlers to load categories
      fridgeButton.setOnAction(event -> {
         loadCategoriesForSpace("Fridge");
         styleActiveButton(fridgeButton);
      });
      freezerButton.setOnAction(event -> {
            loadCategoriesForSpace("Freezer");
            styleActiveButton(freezerButton);
      });
      pantryButton.setOnAction(event -> {
            loadCategoriesForSpace("Pantry");
            styleActiveButton(pantryButton);
      });

      productUnit.getItems().addAll("kg", "g", "l", "ml", "oz", "lbs");
      productLoc.getItems().addAll("Fridge", "Freezer", "Pantry");

      // Set up event listeners for add buttons
      addSpace.setOnAction(event -> addSpace());
      addCategory.setOnAction(event -> addCategory());
      
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

   private void addSpace() {
      TextInputDialog dialog = new TextInputDialog();
      dialog.setTitle("Add New Space");
      dialog.setHeaderText("Enter the name for the new space:");
      dialog.setContentText("Space Name:");
      
      Optional<String> result = dialog.showAndWait();
      result.ifPresent(name -> {
          String spaceName = name.trim(); // Define final spaceName here
          if (!spaceName.isEmpty()) {
              
              // 1. Add the space to the ComboBox
              productLoc.getItems().add(spaceName);
              
              // 2. Add the space to defaultCategories with initial categories
              defaultCategories.put(spaceName, List.of("All Items", "Expiring Soon"));
              spacesCategories.put(spaceName, new HashMap<>());
  
              // 3. Create and style the new button
              Button newSpaceButton = new Button(spaceName);
              newSpaceButton.setStyle("-fx-background-color: darkgrey; -fx-background-radius: 50; -fx-border-radius: 50; -fx-border-width: 4; -fx-font-size: 30; -fx-text-fill: black;");
              newSpaceButton.setPrefHeight(60);
              newSpaceButton.setPrefWidth(346);
  
              // Set button action
              newSpaceButton.setOnAction(event -> {
                  loadCategoriesForSpace(spaceName); // Now spaceName is effectively final
                  styleActiveButton(newSpaceButton);
              });
  
              // 4. Add the button to the VBox
              spacesButtons.getChildren().add(newSpaceButton);
          }
      });
  }

   // Method to add a new category to the current space
   private void addCategory() {
      if (currentSpace == null) {
         showAlert("No Space Selected", "Please select a space before adding a category.");
         return;
      }

      TextInputDialog dialog = new TextInputDialog();
      dialog.setTitle("Add New Category");
      dialog.setHeaderText("Enter the name of the new category:");

      Optional<String> result = dialog.showAndWait();
      result.ifPresent(categoryName -> {
         Map<String, FlowPane> categories = spacesCategories.get(currentSpace);
         if (!categories.containsKey(categoryName)) {
            // Create a new FlowPane for the category
            FlowPane newCategoryPane = new FlowPane();
            newCategoryPane.setPrefHeight(200);
            newCategoryPane.setPrefWidth(1400);
            newCategoryPane.setStyle("-fx-background-color: lightgray; -fx-hgap: 10; -fx-vgap: 10;");

            // Add the new category to the current space in spacesCategories
            categories.put(categoryName, newCategoryPane);

            // Update UI with the new category
            Text categoryHeader = new Text(categoryName);
            categoryHeader.setFont(new Font("System Bold", 36));
            categoryHeader.setStyle("-fx-fill: #333;");
            
            // Add the header and the FlowPane to the VBox container
            categoryContainer.getChildren().addAll(categoryHeader, newCategoryPane);
         }
      });
   }


   // Utility method to show an alert
   private void showAlert(String title, String content) {
      Alert alert = new Alert(Alert.AlertType.WARNING);
      alert.setTitle(title);
      alert.setContentText(content);
      alert.showAndWait();
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

   private void setupDefaultCategories() {
      defaultCategories = new HashMap<>();
  
      // Define default categories for each space
      defaultCategories.put("Fridge", List.of("All Items", "Expiring Soon", "Ready to Eat", "Dairy", "Meat & Seafood"));
      defaultCategories.put("Freezer", List.of("All Items","Expiring Soon", "Frozen Foods", "Meat & Seafood", "Vegetables"));
      defaultCategories.put("Pantry", List.of("All Items", "Expiring Soon", "Canned Goods", "Dry Foods", "Condiments"));
   }

   // Load and display categories based on the selected space
   private void loadCategoriesForSpace(String space) {
      // Clear existing categories in the VBox
      categoryContainer.getChildren().clear();

      List<String> categories = defaultCategories.getOrDefault(space, List.of("Miscellaneous"));

      for (String category : categories) {
         // Create a header for each category
         Text categoryHeader = new Text(category);
         categoryHeader.setFont(new Font("System Bold", 36));
         categoryHeader.setStyle("-fx-fill: #333;");

         // Create a FlowPane for items in this category
         FlowPane categoryPane = new FlowPane();
         categoryPane.setPrefHeight(200); // Adjust height as needed
         categoryPane.setPrefWidth(1400); // Adjust width as needed
         categoryPane.setStyle("-fx-background-color: lightgray; -fx-hgap: 10; -fx-vgap: 10;");

         // Add the header and the FlowPane to the VBox container
         categoryContainer.getChildren().addAll(categoryHeader, categoryPane);

         // Optionally we can store the FlowPane in a map if we need to access it dynamically
         spacesCategories.computeIfAbsent(space, k -> new HashMap<>()).put(category, categoryPane);
      }
   }

   // Helper method to style the active space button
   private void styleActiveButton(Button activeButton) {
      // Reset styles for all space buttons within the VBox
      for (Node node : spacesButtons.getChildren()) {
          if (node instanceof Button) {
              ((Button) node).setStyle("-fx-background-color: darkgrey; -fx-background-radius: 50; -fx-border-radius: 50; -fx-border-width: 4; -fx-font-size: 30;");
          }
      }
  
      // Highlight the active button
      activeButton.setStyle("-fx-background-color: orange; -fx-background-radius: 50; -fx-border-color: black; -fx-border-radius: 50; -fx-border-width: 4; -fx-font-size: 30;");
  }

   //System.out.println(ingredientInventory.get(j).getName()+", "+ingredientInventory.get(j).getQuantity()+" of "+ingredientInventory.get(j).getUnit());

   private void saveIngredient() {

      if (isFormValid()) {
         // Get values from input fields
         String ingredientName = productName.getText();
         String quantity = productQuantity.getText();
         String unit = productUnit.getValue();
         String location = productLoc.getValue();
         LocalDate expirationDate = productEXPDate.getValue();
         String convertedDate = expirationDate.toString();

         // Format date for display
         //DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
         //String expirationDateString = expirationDate.format(formatter);

         // Create new ingredient item and add to inventory
         Item newIngredient = new Item(ingredientName, "0", Integer.parseInt(quantity), unit, location, convertedDate);
         ingredientInventory.add(newIngredient);

         try {
            // Load the FXML for the ingredient card
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/javafx/Resources/ingredientCard.fxml"));
            VBox ingredientCard = loader.load();

            // Set ingredient card details
            IngredientCardController controller = loader.getController();
            controller.setIngredientData(Integer.parseInt(newIngredient.getID()), ingredientName + " - " + quantity + " " + unit, selectedImage, this);

            // Store the controller in the properties map for later access
            ingredientCard.getProperties().put("controller", controller);

            Map<String, FlowPane> locationCategories = spacesCategories.get(location);

           // 1. Add ingredient to "All Items" by default
           FlowPane allItemsPane = locationCategories.get("All Items");
           if (allItemsPane != null) {
               allItemsPane.getChildren().add(createDuplicateNode(ingredientCard));
           }

           // 2. Determine additional categories based on tags and expiration date
           List<String> categories = tags.stream().map(String::toLowerCase).collect(Collectors.toList());
           if (expirationDate != null && expirationDate.isBefore(LocalDate.now().plusDays(3))) {
               categories.add("expiring soon");
           }

            // Add ingredient to each relevant category, matching case-insensitively
            for (Map.Entry<String, FlowPane> entry : locationCategories.entrySet()) {
               String category = entry.getKey().toLowerCase(); // Convert category name to lowercase
               FlowPane categoryPane = entry.getValue();
               
               if (categories.contains(category)) {
                  categoryPane.getChildren().add(createDuplicateNode(ingredientCard));
               }
            }

            // Clear the input fields after saving
            productName.clear();
            productQuantity.clear();
            productUnit.setValue(null);
            productLoc.setValue(null);
            productEXPDate.setValue(null);
            tags.clear();

            // Hide add ingredient pane and show main inventory view
            addIngredientMenuPane.setVisible(false);
            if (spaces) {
               spacesPane.setVisible(true);
            } else if (places) {
               placesPane.setVisible(true);
            }

         } catch (IOException e) {
             e.printStackTrace();
         }
      } else {
         // Show alert if the form is not valid
         Alert alert = new Alert(AlertType.ERROR);
         alert.setTitle("Form Error");
         alert.setHeaderText(null);
         alert.setContentText("Please fill in all required fields.");
         alert.showAndWait();
     }


   }

   // Helper method to duplicate the ingredient card
   private Node createDuplicateNode(VBox originalCard) {
      try {
         // Load the FXML again to create a fresh duplicate
         FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/javafx/Resources/ingredientCard.fxml"));
         VBox duplicateCard = loader.load();

         // Transfer data from the original card to the duplicate
         IngredientCardController originalController = (IngredientCardController) originalCard.getProperties().get("controller");
         IngredientCardController duplicateController = loader.getController();
         
         // Set the same data on the duplicate
         duplicateController.setIngredientData(originalController.getIngredientId(), 
                                                originalController.getIngredientName(), 
                                                originalController.getImage(), 
                                                this);
         return duplicateCard;
      } catch (IOException e) {
         e.printStackTrace();
         return null;
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
         // Convert file to Image
        selectedImage = new Image(selectedImageFile.toURI().toString());
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

   // Method to open the edit form with the recipe's current details
   public void openEditIngriedent(int ingredientId) {
      // Logic to open edit pane and load recipe details by ID
      System.out.println("Edit recipe with ID: " + ingredientId);
   }

   // Method to delete the recipe by ID
   public void deleteIngriendent(int ingredientId) {
      // Logic to remove the recipe from data source and refresh UI
      System.out.println("Delete recipe with ID: " + ingredientId);

      // Remove from database, then remove from frontend

      // Access and remove the recipe card widget

      //work on having this remove the card from all categories it might be apart of

      //VBox ingredientCard = recipeWidgets.get(ingredientId);
      //if (ingredientCard != null) {
          //recipeFlowPane.getChildren().remove(ingredientCard);
          //recipeWidgets.remove(ingredientId);  // Clean up from the map
      //}
   }

}
