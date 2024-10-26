package org.javafx.Controllers;

import java.io.File;
import java.time.LocalDate;

import org.javafx.Main.Main;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class MyRecipesController {

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
   private Button addRecipeButton;

   @FXML
   private Button closeCreateButton, closeRecipeButton, addTag, recipeAddIngredient, imageSelect, saveButton;

   @FXML
   private TextField recipeName, recipeTag, recipeIngredients, recipePreparationSteps, recipeETA;
   
   @FXML
   private ComboBox<String> recipeCategory;

   @FXML
   private Pane recipeDetailsPane, addRecipePane, myRecipesPane;

   private File selectedImageFile;

   @FXML
   private ListView<String> tagsListView; // To display added tags
   private ObservableList<String> tags = FXCollections.observableArrayList();
   private ObservableList<String> ingredients = FXCollections.observableArrayList();

   @FXML
   private void initialize() {

      recipeCategory.getItems().addAll("dinner", "lunch", "breakfast", "snack", "other");

      addTag.setOnAction(event -> {
         try {
            String tag = recipeTag.getText().trim();
            if (!tag.isEmpty()) {
               tags.add(tag);  // Add the tag to the list
               recipeTag.clear();  // Clear the input field after adding the tag
            }
         } catch (Exception e) {
            e.printStackTrace();
         }
      });

      recipeAddIngredient.setOnAction(event -> {
         try {
            String ingredient = recipeIngredients.getText().trim();
            if (!ingredient.isEmpty()) {
               ingredients.add(ingredient);  // Add the tag to the list
               recipeIngredients.clear();  // Clear the input field after adding the tag
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

      //closeRecipeButton.setOnAction(event -> {
         //try {
            //myRecipesPane.setVisible(true);
            //recipeDetailsPane.setVisible(false);
         //} catch (Exception e) {
            //e.printStackTrace();
         //}
      //});

      closeCreateButton.setOnAction(event -> {
         try {
            myRecipesPane.setVisible(true);
            addRecipePane.setVisible(false);
         } catch (Exception e) {
            e.printStackTrace();
         }
      });

      addRecipeButton.setOnAction(event -> {
         try {
            myRecipesPane.setVisible(false);
            addRecipePane.setVisible(true);
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

   private void saveIngredient() {

      if (isFormValid()) {
         // Get values from input fields
         String Name = recipeName.getText();
         String Category = recipeCategory.getValue();
         String PrepSteps = recipePreparationSteps.getText();
         String CookTime = recipeETA.getText();


         // Print the values to the terminal for testing
         System.out.println("Recipe's Name: " + Name);
         System.out.println("Category: " + Category);
         System.out.println("Tags: " + tags);
         System.out.println("Ingredients: " + ingredients);
         System.out.println("PrepSteps: " + CookTime);
         System.out.println("ETA for Cooking: " + CookTime);
         //System.out.println("Selected Image: " + selectedImageFile.getAbsolutePath());

         // In the future, we will use these values to add to the database
         // Example: db.insertIngredient(ingredientName, quantity, unit, location, expirationDate);
         
         addRecipePane.setVisible(false);
         myRecipesPane.setVisible(true);
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
      return !recipeName.getText().trim().isEmpty() &&
             !ingredients.isEmpty() &&
             !recipePreparationSteps.getText().trim().isEmpty() &&
             !recipeETA.getText().trim().isEmpty();
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
}
