package org.javafx.Controllers;

import java.io.File;
import javafx.scene.control.Label;
import java.time.LocalDate;

import org.javafx.Main.Main;
import org.javafx.Recipe.Recipe;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;


public class MyRecipesController {

   @FXML
   private Button menuButton, inventoryButton, myRecipesButton, inboxButton, browseRecipesButton, profileButton, settingsButton,
                  closeP1Button, closeP2Button, addRecipeButton, closeRecipeButton, addTagButton, addIngredientButton, imageSelectButton, saveButton,
                  nextStep, prevStep, userDashboardButton, mealPlannerButton, myListsButton, addEquipmentButton, prevStepButton, nextStepButton,
                  addStepButton, backButton, nextButton;

   @FXML
   private VBox menuPane;

   @FXML
   private TextField recipeName, recipeTag, ingredientEntry, recipeETAPassive, recipeETA, recipeETAPrep, recipeYield, amountEntry, equipmentEntry;
   
   @FXML
   private ComboBox<String> recipeCategory, recipeCollection, ingredientUnitEntry;

   @FXML
   private Pane myRecipesPane, myRecipeMainPane, addRecipePaneP1, addRecipePaneP2, recipeDetailsPane, recipeCookingPane;

   private File selectedImageFile;

   @FXML
   private TextArea prepStepField, recipeDescription;

   @FXML
   private Text yieldTXT, prepTXT, cookTXT, totalTXT, estCostTXT, specialEquipmentTXT, stepOfTXT, noRecipesTXT, recipeNameTXT, stepIndex;

   @FXML
   private ImageView recipeImages, imagePreview;

   private Image selectedImage;

   @FXML
   private FlowPane recipeFlowPane, chipPreview;

   @FXML
   private TableView<Ingredient> ingredientTable;

   @FXML
   private TableView<String> equipmentTable;

   @FXML
   private TableColumn<String, String> equipmentList;

   @FXML
   private TableColumn<Ingredient, String> ingredientList;

   @FXML
   private TableColumn<Ingredient, String> amountList;

   @FXML
   private ListView<String> ingredientsArea;

   @FXML
   private ObservableList<String> tags = FXCollections.observableArrayList();
   private ObservableList<Ingredient> ingredients = FXCollections.observableArrayList();
   private Map<Integer, VBox> recipeWidgets = new HashMap<>();
   private ObservableList<String> equipment = FXCollections.observableArrayList();
   private List<String> preparationSteps = new ArrayList<>();
   private int currentStep = 0;

   private DynamoDbClient database;
   private Map<String, AttributeValue> item = new HashMap<>();

   @FXML
   private void initialize() {
      Region region = Region.US_EAST_1;
      database = DynamoDbClient.builder()
      .region(region)
      .build();

      //Main.setScale(myRecipeMainPane);

      recipeCategory.getItems().addAll("dinner", "lunch", "breakfast", "snack", "other");
      ingredientUnitEntry.getItems().addAll("g", "kg", "ml", "l", "tsp", "tbsp", "cup", "oz", "lb", "pinch", "dash");

      // Set up TableView columns for ingredients
      ingredientList.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getName()));
      amountList.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getAmount() + " " + data.getValue().getUnit()));
      ingredientTable.setItems(ingredients);

      // Enable table editing
      ingredientTable.setEditable(true);
      ingredientList.setCellFactory(TextFieldTableCell.forTableColumn());
      amountList.setCellFactory(TextFieldTableCell.forTableColumn());

         // Context menu for deletion
      ingredientTable.setRowFactory(tv -> {
         TableRow<Ingredient> row = new TableRow<>();
         ContextMenu contextMenu = new ContextMenu();
         MenuItem deleteItem = new MenuItem("Delete");
         deleteItem.setOnAction(event -> ingredients.remove(row.getItem()));
         contextMenu.getItems().add(deleteItem);
         row.setContextMenu(contextMenu);
         return row;
      });

      // Set up TableView for equipment
      equipmentList.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue()));
      equipmentTable.setItems(equipment);

      // Enable table editing
      equipmentTable.setEditable(true);
      equipmentList.setCellFactory(TextFieldTableCell.forTableColumn());

         // Context menu for deletion
         equipmentTable.setRowFactory(tv -> {
         TableRow<String> row = new TableRow<>();
         ContextMenu contextMenu = new ContextMenu();
         MenuItem deleteItem = new MenuItem("Delete");
         deleteItem.setOnAction(event -> equipment.remove(row.getItem()));
         contextMenu.getItems().add(deleteItem);
         row.setContextMenu(contextMenu);
         return row;
      });

      addTagButton.setOnAction(event -> addTag());
      addIngredientButton.setOnAction(event -> addIngredient());
      imageSelectButton.setOnAction(event -> SelectImage());
      saveButton.setOnAction(event -> saveRecipe());
      addStepButton.setOnAction(event -> addStep());
      prevStepButton.setOnAction(event -> navigateStep(-1));
      nextStepButton.setOnAction(event -> navigateStep(1));
      addEquipmentButton.setOnAction(event -> addEquipment());

      // Initialize the first step
      preparationSteps.add("");
      currentStep = 0;
      updateStepView();
   
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

      closeRecipeButton.setOnAction(event -> {
         try {
            myRecipesPane.setVisible(true);
            recipeDetailsPane.setVisible(false);
         } catch (Exception e) {
            e.printStackTrace();
         }
      });

      closeP1Button.setOnAction(event -> {
         try {
            myRecipesPane.setVisible(true);
            addRecipePaneP1.setVisible(false);
            clearForms();
         } catch (Exception e) {
            e.printStackTrace();
         }
      });

      closeP2Button.setOnAction(event -> {
         try {
            myRecipesPane.setVisible(true);
            addRecipePaneP2.setVisible(false);
            clearForms();
         } catch (Exception e) {
            e.printStackTrace();
         }
      });

      backButton.setOnAction(event -> {
         try {
            addRecipePaneP2.setVisible(false);
            addRecipePaneP1.setVisible(true);
         } catch (Exception e) {
            e.printStackTrace();
         }
      });

      nextButton.setOnAction(event -> {
         try {
            addRecipePaneP1.setVisible(false);
            addRecipePaneP2.setVisible(true);
         } catch (Exception e) {
            e.printStackTrace();
         }
      });

      addRecipeButton.setOnAction(event -> {
         try {
            myRecipesPane.setVisible(false);
            addRecipePaneP1.setVisible(true);
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

   // Add ingredient to table
   private void addIngredient() {
      String name = ingredientEntry.getText().trim();
      String amount = amountEntry.getText().trim();
      String unit = ingredientUnitEntry.getValue();

      if (!name.isEmpty() && !amount.isEmpty() && unit != null) {
          ingredients.add(new Ingredient(name, amount, unit));
          ingredientEntry.clear();
          amountEntry.clear();
          ingredientUnitEntry.setValue(null);
      }
  }

   // Add a new step to the steps list
   private void addStep() {
      // Save current step text before adding a new step
      if (currentStep >= 0 && currentStep < preparationSteps.size()) {
         preparationSteps.set(currentStep, prepStepField.getText().trim());
      }

      // Add a new blank step
      preparationSteps.add("");
      currentStep = preparationSteps.size() - 1;
      updateStepView();
   }

   // Navigate between steps
   private void navigateStep(int direction) {
      // Save the current step text before navigating
      if (currentStep >= 0 && currentStep < preparationSteps.size()) {
         preparationSteps.set(currentStep, prepStepField.getText().trim());
      }

      // Calculate new step index
      int newStep = currentStep + direction;
      if (newStep >= 0 && newStep < preparationSteps.size()) {
         currentStep = newStep;
         updateStepView();
      }
   }

   // Update TextArea and stepIndex label to display the current step
   private void updateStepView() {
      if (preparationSteps.isEmpty()) {
         prepStepField.setText("");
         stepIndex.setText("Step 1 of 1");
      } else {
         prepStepField.setText(preparationSteps.get(currentStep));
         stepIndex.setText("Step " + (currentStep + 1) + " of " + preparationSteps.size());
      }
   }

   // Add equipment to table
   private void addEquipment() {
      String equipmentName = equipmentEntry.getText().trim();
      if (!equipmentName.isEmpty()) {
          equipment.add(equipmentName);
          equipmentEntry.clear();
      }
   }

    // Add tag as a chip in FlowPane
    private void addTag() {
      String tag = recipeTag.getText().trim();
      if (!tag.isEmpty() && !tags.contains(tag)) {
         tags.add(tag);
         recipeTag.clear();
         updateTagView();
      }
   }

   // Update FlowPane to display chips
   private void updateTagView() {
      chipPreview.getChildren().clear();
      for (String tag : tags) {
         HBox chip = createTagChip(tag);
         chipPreview.getChildren().add(chip);
      }
   }

   // Create a chip with a delete option for each tag
   private HBox createTagChip(String tagText) {
      Label tagLabel = new Label(tagText);
      Button removeButton = new Button("X");
      
      removeButton.setOnAction(event -> {
          tags.remove(tagText);
          updateTagView();
      });

      HBox tagChip = new HBox(tagLabel, removeButton);
      tagChip.setSpacing(5);
      tagChip.setStyle("-fx-background-color: #e0e0e0; -fx-padding: 5 10; -fx-border-radius: 10; -fx-background-radius: 10;");
      return tagChip;
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

   private void saveRecipe() {

      // Get values from input fields
      String name = recipeName.getText(); //Required
      String category = recipeCategory.getValue(); //Required
      String collection = recipeCollection.getValue();
      int servings = Integer.parseInt(recipeYield.getText()); //Required
      String description = recipeDescription.getText(); //Required
      int prepTime = Integer.parseInt(recipeETAPrep.getText()); //Required
      int passiveTime = Integer.parseInt(recipeETAPassive.getText()); //Required
      int cookTime = Integer.parseInt(recipeETA.getText()); //Required
      int complexity = calculateRecipeComplexity(); 
      String[] tagsArray = tags.toArray(new String[0]); 
      String[] equipmentArray = equipment.toArray(new String[0]);
      // Convert ingredients to a string array representation
      String[] ingredientsArray = ingredients.stream()
       .map(ingredient -> ingredient.getName() + ": " + ingredient.getAmount() + " " + ingredient.getUnit())
       .toArray(String[]::new);
      String[] stepsArray = preparationSteps.toArray(new String[0]); //Required

      //get num of entries in db and then add 1
      Integer id = 0;

      // Create a new Recipe object with all required fields
      Recipe newRecipe = new Recipe(id, name, category, collection, description, prepTime, passiveTime, cookTime, complexity, servings, tagsArray, ingredientsArray, equipmentArray, stepsArray);
      item.put(Integer.toString(id), AttributeValue.builder().s(newRecipe.getName()).build());
      PutItemRequest request = PutItemRequest.builder().tableName("Recipes").item(item).build();
      database.putItem(request);
      // Save recipe to database or use it as needed
      // Example: addRecipeToDatabase(newRecipe);


      if (isFormValid(name, category, recipeYield.getText(), description, recipeETAPrep.getText(), recipeETAPassive.getText(), recipeETA.getText(), ingredientsArray, stepsArray)) {

         // Concatenate all preparation steps
        String PrepSteps = String.join("\n", preparationSteps);
         
         try { 
            // Load the RecipeCard FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/javafx/Resources/RecipeCard.fxml"));
            VBox recipeCard = loader.load();

            // Get the RecipeCardController and pass `this` as MyRecipesController
            RecipeCardController controller = loader.getController();
            controller.setRecipeData(newRecipe, selectedImage, this); // Pass the new Recipe object

            // Add the recipe card to the FlowPane
            recipeFlowPane.getChildren().add(recipeCard);

            recipeWidgets.put(id, recipeCard);  // Store the card with its ID

            if(noRecipesTXT.isVisible()) {
               noRecipesTXT.setVisible(false);
            }

            clearForms();

            updateTagView();
            updateStepView();

            addRecipePaneP2.setVisible(false);
            myRecipesPane.setVisible(true);
         
         } catch (Exception e) {
            e.printStackTrace();
         }
      }
   }

   private int calculateRecipeComplexity() {
      int complexity = 1; // Start with a base complexity
  
      // Increase complexity based on the number of ingredients
      if (ingredients.size() > 10) {
          complexity += 2;
      } else if (ingredients.size() > 5) {
          complexity += 1;
      }
  
      // Increase complexity based on the total prep and cook time
      int totalTime = Integer.parseInt(recipeETA.getText()) + Integer.parseInt(recipeETAPrep.getText());
      if (totalTime > 60) {
          complexity += 2;
      } else if (totalTime > 30) {
          complexity += 1;
      }
  
      // Increase complexity based on the number of steps
      if (preparationSteps.size() > 10) {
          complexity += 2;
      } else if (preparationSteps.size() > 5) {
          complexity += 1;
      }
  
      return complexity;
   }

  private String getComplexityLabel(int complexity) {
      if (complexity <= 3) {
         return "Very Easy";
      } else if (complexity <= 6) {
         return "Easy";
      }else if (complexity <= 9) {
         return "Medium";
      }else if (complexity <= 12) {
         return "Hard";
      } else {
         return "Very hard";
      }
   }

   private void clearForms(){

      // Clear form inputs
      recipeName.clear();
      recipeCategory.setValue(null);
      tags.clear();
      ingredients.clear();
      preparationSteps.clear();
      prepStepField.clear();
      recipeETA.clear();
      recipeETAPrep.clear();
      selectedImageFile = null;
   }

   private boolean isFormValid(String name, String category, String servings, String decsription, String prepTime, String passiveTime, String cookTime, String[] ingredientsArray, String[] stepsArray) {
      if (name == null || name.isEmpty()) {
         showAlert("Error", "Missing Ingredient Name", "Please enter a valid recipe name.");
         return false;
      }
      if (category == null || category.isEmpty()) {
         showAlert("Error", "Missing Category", "Please select a valid category.");
         return false;
      }
      try {
         Integer.parseInt(servings);
      } catch (NumberFormatException e) {
         showAlert("Error", "Invalid Serving Amount", "Servings must be a number.");
         return false;
      }
      if (decsription == null || decsription.isEmpty()) {
         showAlert("Error", "Missing Decsription", "Please add a decsription.");
         return false;
      }
      try {
         Integer.parseInt(prepTime);
      } catch (NumberFormatException e) {
         showAlert("Error", "Invalid Prep Time", "Prep time must be a number.");
         return false;
      }
      try {
         Integer.parseInt(passiveTime);
      } catch (NumberFormatException e) {
         showAlert("Error", "Invalid Passive Time", "Passive time must be a number.");
         return false;
      }
      try {
         Integer.parseInt(cookTime);
      } catch (NumberFormatException e) {
         showAlert("Error", "Invalid Cook Time", "Cook time must be a number.");
         return false;
      }
      if (ingredientsArray == null) {
         showAlert("Error", "Missing Ingredients", "Please add ingredients.");
         return false;
      }
      if (stepsArray == null) {
         showAlert("Error", "Missing Steps", "Please add steps.");
         return false;
      }
      return true;
   }

   private void showAlert(String title, String header, String content) {
      Alert alert = new Alert(AlertType.ERROR);
      alert.setTitle(title);
      alert.setHeaderText(header);
      alert.setContentText(content);
      alert.showAndWait();
   }

   private void SelectImage() {
      FileChooser fileChooser = new FileChooser();
      fileChooser.setTitle("Select Ingredient Image");

      // Restrict to image file types
      fileChooser.getExtensionFilters().add(
         new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
      );

      Stage stage = (Stage) imageSelectButton.getScene().getWindow();  // Get the current stage
      selectedImageFile = fileChooser.showOpenDialog(stage);

      if (selectedImageFile != null) {
         // Convert file to Image
        selectedImage = new Image(selectedImageFile.toURI().toString());
        imagePreview.setImage(selectedImage);
      }
   }

   public void showRecipeDetails(int recipeId, String name, Image image) {
      myRecipesPane.setVisible(false);
      recipeDetailsPane.setVisible(true);
      recipeNameTXT.setText(name);
      recipeImages.setImage(image);

      //create ref using the recipe id to populate the other information
      yieldTXT.setText("Yield: " + "");
      prepTXT.setText("Prep Time: " + "" + "Minutes");
      cookTXT.setText("Cook Time: " + "" + "Minutes");

      int totalTime = 0; // add the prep and cook times

      totalTXT.setText("Total: " + totalTime + "Minutes");
      estCostTXT.setText("EST Cost: $" + ""); //add a cost calculator ref
      specialEquipmentTXT.setText("Special Equipment: " + "");
      stepOfTXT.setText("Step " + "" + " of " + "");
   }

   // Method to open the edit form with the recipe's current details
   public void openEditRecipe(int recipeId) {
      // Logic to open edit pane and load recipe details by ID
      System.out.println("Edit recipe with ID: " + recipeId);


   }

   // Method to delete the recipe by ID
   public void deleteRecipe(int recipeId) {
      // Logic to remove the recipe from data source and refresh UI
      System.out.println("Delete recipe with ID: " + recipeId);

      // Remove from database, then remove from frontend

      // Access and remove the recipe card widget
      VBox recipeCard = recipeWidgets.get(recipeId);
      if (recipeCard != null) {
          recipeFlowPane.getChildren().remove(recipeCard);
          recipeWidgets.remove(recipeId);  // Clean up from the map
      }
   }
}

// Custom classes to manage ingredients and steps
class Ingredient {
   private String name;
   private String amount;
   private String unit;

   public Ingredient(String name, String amount, String unit) {
      this.name = name;
      this.amount = amount;
      this.unit = unit;
   }

   public String getName() { return name; }
   public String getAmount() { return amount; }
   public String getUnit() { return unit; }
}

