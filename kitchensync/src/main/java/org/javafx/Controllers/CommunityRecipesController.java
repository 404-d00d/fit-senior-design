package org.javafx.Controllers;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.UUID;

import org.javafx.Main.Main;
import org.javafx.Recipe.Recipe;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
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
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

public class CommunityRecipesController {

   @FXML private ComboBox<String> sortBy, categoryDropDown, recipeCategory, recipeCollection, ingredientUnitEntry;
   @FXML private TextField searchBar;
   @FXML private FlowPane recipeFlowPane, recipeTagFlowPane, chipPreview;

   @FXML
   private Button addMealButton, userDashboardButton, mealPlannerButton, myRecipesButton, inventoryButton,
                  inboxButton, browseRecipesButton, profileButton, settingsButton, myListsButton, menuButton,
                  cookItButton, closeRecipeDetailsButton, prevStep, nextStep, postRecipe, manualButton, recipeUploadMyrecipes,
                  cancelButton, imageSelectButton, closeP1Button, addEquipmentButton, nextButton, addIngredientButton,
                  prevStepButton, nextStepButton, addStepButton, closeP2Button, addTagButton, backButton, uploadButton;

   // Filter Buttons
   @FXML private Button ingredientFilter, tagsFilter, resetFilters;

   @FXML
   private Pane menuPane, myRecipesPane, recipeReviews, recipeDetailsPane, uploadPane, addRecipePaneP2, addRecipePaneP1;

   private DynamoDbClient database;
   private S3Client s3Client;
   private AwsBasicCredentials awsCreds ;

   private List<Map<String, AttributeValue>> recipes;
   private List<VBox> recipeCards = new ArrayList<>();

   @FXML private Text noRecipesTXT;
   @FXML private HBox suggestedRecipesBox;

   @FXML private ImageView recipeDetailsImages, imagePreview;
   @FXML private TextArea recipeDetailDescription, stepArea, recipeDescription, prepStepField;
   @FXML private PieChart recipeCalories;
   @FXML private Text recipeNameTXT, recipePrepTimeTXT, recipeServingsTXT, recipePassiveTimeTXT, recipeCookTimeTXT, recipeTotalTimeTXT, recipeComplexityTXT,
                      stepOfTXT, stepIndex;

   // Table Views for Ingredients & Equipment
   @FXML private TableView<Ingredient> ingredientTable;
   @FXML private TableView<String> equipmentTable;
   @FXML private TableColumn<String, String> equipmentList;
   @FXML private TableColumn<Ingredient, String> ingredientList, amountList;

   @FXML private ListView<String> ingredientsArea, specialEquipmentTXTArea, recipeIngredients;

   @FXML private TextField UploadRecipeName, recipeYield, recipeETAPrep, recipeETAPassive, recipeETA, equipmentEntry, ingredientEntry,
                           amountEntry, recipeTag;
   
   private int currentStep = 0;
   private int displayStep = 0;

   private File selectedImageFile;
   private Image selectedImage;

   private static final String RECIPES_FILE_PATH = "recipes.json";

   // ==============================
   // DATA STORAGE & CONSTANTS
   // ==============================
   private ObservableList<String> tags = FXCollections.observableArrayList();
   private ObservableList<Ingredient> ingredients = FXCollections.observableArrayList();
   private List<String> ingredientEntries = new ArrayList<>();
   private ObservableList<String> equipment = FXCollections.observableArrayList();
   private List<String> preparationSteps = new ArrayList<>();
   private ObservableList<Recipe> recipeList = FXCollections.observableArrayList();

   private static final String S3_BUCKET_NAME = "kitchensyncimages";
   private static final String S3_BASE_URL = "https://" + S3_BUCKET_NAME + ".s3.amazonaws.com/";

   private void initializeDatabaseAndS3() {
      try {
         Region region = Region.US_EAST_1;
         database = DynamoDbClient.builder()
              .region(region)
              .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
              .build();
         s3Client = S3Client.builder()
              .region(region)
              .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
              .build();
      } catch (Exception e) {
          System.err.println("Failed to initialize DynamoDB client: " + e.getMessage());
          e.printStackTrace();
      }
   }

   @FXML
   private void initialize() {

      awsCreds = AwsBasicCredentials.create(
         "AKIAS6J7QGOOS2VSJQNP",
         "RpYmWXTZAk4k33zL/tQYUDP/x+L7403SYAjwSx9Y"
      );

      initializeDatabaseAndS3();
      loadCommunityRecipes();
      configureSortBy();
      configureFilters();
      configureDropdowns();
      configureEquipmentTable();
      configureIngredientTable();

      searchBar.textProperty().addListener((obs, oldText, newText) -> filterRecipesBySearch(newText));

      setupUIEventHandlers();

      updateSuggestedRecipes();
      
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
            Main.showCommunityRecipesScreen();
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

      // Switch to Lists Screen
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
            Main.showMealPlannerScreen();
         } catch (Exception e) {
            e.printStackTrace();
         }
      });

      setHoverEffect(mealPlannerButton);

      closeRecipeDetailsButton.setOnAction(event -> {
         myRecipesPane.setVisible(true);
         recipeDetailsPane.setVisible(false);
      });
   }

   private void setupUIEventHandlers() {
      addTagButton.setOnAction(event -> addTag());
      addIngredientButton.setOnAction(event -> addIngredient());
      imageSelectButton.setOnAction(event -> selectImage());
      uploadButton.setOnAction(event -> uploadRecipe());
      addStepButton.setOnAction(event -> addStep());
      prevStepButton.setOnAction(event -> navigateStep(-1));
      nextStepButton.setOnAction(event -> navigateStep(1));
      addEquipmentButton.setOnAction(event -> addEquipment());
      prevStep.setOnAction(event -> detailsSteps(-1));
      nextStep.setOnAction(event -> detailsSteps(1));
      postRecipe.setOnAction(event -> openPostRecipeForm());
      cancelButton.setOnAction(event -> closeRecipeUploadOptions());
      closeP1Button.setOnAction(event -> closeRecipe1Upload());
      closeP2Button.setOnAction(event -> closeRecipe2Upload());
      nextButton.setOnAction(event -> nextToP2());
      backButton.setOnAction(event -> backToP1());
      manualButton.setOnAction(event -> openManualEntryForm());
      recipeUploadMyrecipes.setOnAction(event -> openRecipeSelection());

   }

   private void openManualEntryForm() {
      uploadPane.setVisible(false);
      addRecipePaneP1.setVisible(true);
   }

   private void openRecipeSelection() {
      recipeList.addAll(loadRecipesFromJson()); // Ensure recipes are loaded

      if (recipeList.isEmpty()) {   
         System.out.println("No saved recipes available to upload.");
         return;
      }

      ChoiceDialog<Recipe> dialog = new ChoiceDialog<>(recipeList.get(0), recipeList);
      dialog.setTitle("Select a Recipe");
      dialog.setHeaderText("Choose a recipe to upload");

      // Access the dialog pane to apply styling
      DialogPane dialogPane = dialog.getDialogPane();


      // Style the labels and combo box
      dialogPane.lookupAll(".label").forEach(label -> label.setStyle("-fx-text-fill: white; -fx-font-size: 16px;"));
      
      dialog.getDialogPane().getStylesheets().add(getClass().getResource("/org/javafx/Resources/css/styles.css").toExternalForm());
      dialog.getDialogPane().lookupAll(".combo-box").forEach(cb -> cb.getStyleClass().add("combo-box"));


      // Style the buttons
      dialogPane.getButtonTypes().forEach(buttonType -> {
         Button button = (Button) dialogPane.lookupButton(buttonType);
         button.setStyle("-fx-background-color: #FF7F11; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10;");
      });

      dialog.setGraphic(null);
      Label headerLabel = (Label) dialog.getDialogPane().lookup(".header-panel .label");
      if (headerLabel != null) {
         headerLabel.setStyle("\"-fx-background-color: #2E2E2E; -fx-text-fill: white; -fx-font-size: 18px;");
      }
      dialog.getDialogPane().setStyle("-fx-background-color: #2E2E2E; -fx-text-fill: white;");
      

      Optional<Recipe> result = dialog.showAndWait();
      result.ifPresent(this::fillRecipeDetails);
   }

   private void configureEquipmentTable() {
      equipmentList.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue()));
      equipmentTable.setItems(equipment);
      equipmentTable.setEditable(true);
      equipmentList.setCellFactory(TextFieldTableCell.forTableColumn());
      equipmentTable.setRowFactory(tv -> {
         TableRow<String> row = new TableRow<>();
         ContextMenu contextMenu = new ContextMenu();
         MenuItem deleteItem = new MenuItem("Delete");
         deleteItem.setOnAction(event -> equipment.remove(row.getItem()));
         contextMenu.getItems().add(deleteItem);
         row.setContextMenu(contextMenu);
         return row;
      });
   }

   private void configureIngredientTable() {
      ingredientList.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getName()));
      amountList.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getAmount() + " " + data.getValue().getUnit()));
      ingredientTable.setItems(ingredients);
      ingredientTable.setEditable(true);
      ingredientList.setCellFactory(TextFieldTableCell.forTableColumn());
      amountList.setCellFactory(TextFieldTableCell.forTableColumn());
      ingredientTable.setRowFactory(tv -> {
          TableRow<Ingredient> row = new TableRow<>();
          ContextMenu contextMenu = new ContextMenu();
          MenuItem deleteItem = new MenuItem("Delete");
          deleteItem.setOnAction(event -> ingredients.remove(row.getItem()));
          contextMenu.getItems().add(deleteItem);
          row.setContextMenu(contextMenu);
          return row;
      });
  }

   private void fillRecipeDetails(Recipe recipe) {

      // Ensure upload form is visible
      uploadPane.setVisible(false);
      addRecipePaneP1.setVisible(true);

      UploadRecipeName.setText(recipe.getName());
      recipeCategory.setValue(recipe.getCategory());
      recipeYield.setText(String.valueOf(recipe.getServings()));
      recipeETAPrep.setText(String.valueOf(recipe.getPrepTime()));
      recipeETAPassive.setText(String.valueOf(recipe.getPassiveTime()));
      recipeETA.setText(String.valueOf(recipe.getCookTime()));
      recipeDescription.setText(recipe.getDescription());
      
      // Populate steps
      preparationSteps.clear();
      preparationSteps.addAll(Arrays.asList(recipe.getSteps()));
      currentStep = 0;
      updateStepView();

      // Populate ingredients
      ingredients.clear();
      ingredientEntries.clear();

      for (String ing : recipe.getIngredients()) {
         ingredientEntries.add(ing); // Still store full string version

         // Try to split into name, amount, unit
         String[] parts = ing.split(":");
         if (parts.length == 2) {
            String name = parts[0].trim();
            String amountUnit = parts[1].trim();

            // Extract amount and unit
            String[] amountUnitParts = amountUnit.split(" ", 2);
            String amount = amountUnitParts.length >= 1 ? amountUnitParts[0].trim() : "";
            String unit = amountUnitParts.length == 2 ? amountUnitParts[1].trim() : "";

            ingredients.add(new Ingredient(name, amount, unit));
         } else {
            // If not parsable, fallback
            ingredients.add(new Ingredient(ing, "", ""));
         }
      }

      // Populate special equipment
      equipment.clear();
      equipment.addAll(Arrays.asList(recipe.getEquipment()));

      // Populate tags
      tags.clear();
      tags.addAll(Arrays.asList(recipe.getTags()));
      updateTagView();
  
      // Load Image
      File imageFile = new File("src/main/resources/org/javafx/Resources/Recipe Images/" + recipe.getName() + ".png");
      if (imageFile.exists()) {
         imagePreview.setImage(new Image(imageFile.toURI().toString()));
         selectedImageFile = imageFile;
      }
  }

   private void nextToP2() {
      addRecipePaneP1.setVisible(false);
      addRecipePaneP2.setVisible(true);
   }

   private void backToP1() {
      addRecipePaneP2.setVisible(false);
      addRecipePaneP1.setVisible(true);
   }

   // Add equipment to table
   private void addEquipment() {
      String equipmentName = equipmentEntry.getText().trim();
      if (!equipmentName.isEmpty()) {
            equipment.add(equipmentName);
            equipmentEntry.clear();
      }
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
      tagChip.setStyle(
                        "-fx-background-color: #555555; " +
                        "-fx-text-fill: white; " +
                        "-fx-padding: 5 10; " +
                        "-fx-border-radius: 10; " +
                        "-fx-background-radius: 10; " +
                        "-fx-font-size: 16px; " +
                        "-fx-border-color: #FF7F11;"
                     );
      return tagChip;
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

   private void selectImage() {
      FileChooser fileChooser = new FileChooser();
      fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
      selectedImageFile = fileChooser.showOpenDialog(new Stage());
  
      if (selectedImageFile != null) {
         imagePreview.setImage(new Image(selectedImageFile.toURI().toString()));
      }
  }

   private void uploadRecipe() {
      if (!validateInputs()) {
         System.out.println("Please fill in all required fields.");
         return;
      }

      String recipeName = UploadRecipeName.getText();
      int prepTime = Integer.parseInt(recipeETAPrep.getText());
      int cookTime = Integer.parseInt(recipeETA.getText());
      int passiveTime = Integer.parseInt(recipeETAPassive.getText());
      int servings = Integer.parseInt(recipeYield.getText());
      String description = recipeDescription.getText();

      Map<String, AttributeValue> recipeItem = new HashMap<>();
      String uniqueId = UUID.randomUUID().toString();
      recipeItem.put("Recipe", AttributeValue.builder().s(uniqueId).build());
      recipeItem.put("name", AttributeValue.builder().s(recipeName).build());
      recipeItem.put("prepTime", AttributeValue.builder().n(String.valueOf(prepTime)).build());
      recipeItem.put("cookTime", AttributeValue.builder().n(String.valueOf(cookTime)).build());
      recipeItem.put("passiveTime", AttributeValue.builder().n(String.valueOf(passiveTime)).build());
      recipeItem.put("servings", AttributeValue.builder().n(String.valueOf(servings)).build());
      recipeItem.put("description", AttributeValue.builder().s(description).build());

      recipeItem.put("ingredients", AttributeValue.builder()
         .l(ingredientEntries.stream()
            .map(val -> AttributeValue.builder().s(val).build())
            .collect(Collectors.toList()))
         .build());

      recipeItem.put("steps", AttributeValue.builder()
         .l(preparationSteps.stream()
            .map(step -> AttributeValue.builder().s(step).build())
            .collect(Collectors.toList()))
         .build());

      recipeItem.put("tags", AttributeValue.builder()
         .l(tags.stream()
            .map(tag -> AttributeValue.builder().s(tag).build())
            .collect(Collectors.toList()))
         .build());

      recipeItem.put("equipment", AttributeValue.builder()
         .l(equipment.stream()
            .map(eq -> AttributeValue.builder().s(eq).build())
            .collect(Collectors.toList()))
         .build());

      try {
         // Upload Recipe to DynamoDB
         database.putItem(PutItemRequest.builder()
               .tableName("Recipes")
               .item(recipeItem)
               .build());

         // Upload Image to S3
         if (selectedImageFile != null) {
               String s3Key = recipeName + ".jpg";
               s3Client.putObject(PutObjectRequest.builder()
                  .bucket("kitchensyncimages")
                  .key(s3Key)
                  .build(),
                  RequestBody.fromFile(selectedImageFile));

               System.out.println("Image uploaded to S3: " + s3Key);
         }

         System.out.println("Recipe uploaded successfully!");

         loadCommunityRecipes(); // Refresh community recipes

         addRecipePaneP2.setVisible(false);
         myRecipesPane.setVisible(true);

      } catch (Exception e) {
         e.printStackTrace();
         System.out.println("Error uploading recipe.");
      }
   }

   private boolean validateInputs() {
      return !UploadRecipeName.getText().isEmpty() &&
            !recipeETAPrep.getText().isEmpty() &&
            !recipeETA.getText().isEmpty() &&
            !recipeETAPassive.getText().isEmpty() &&
            !recipeYield.getText().isEmpty() &&
            !recipeDescription.getText().isEmpty();
   }

   private void addStep() {
      String stepText = prepStepField.getText();
      if (!stepText.isEmpty()) {
          preparationSteps.add(stepText);
          prepStepField.clear();
      }
   }

   private void closeRecipeUploadOptions() {
      myRecipesPane.setVisible(true);
      uploadPane.setVisible(false);
   }

   private void closeRecipe1Upload() {
      myRecipesPane.setVisible(true);
      addRecipePaneP1.setVisible(false);
   }

   private void closeRecipe2Upload() {
      myRecipesPane.setVisible(true);
      addRecipePaneP2.setVisible(false);
   }

   private void openPostRecipeForm() {
      myRecipesPane.setVisible(false);
      uploadPane.setVisible(true);
   }

   private List<Recipe> loadRecipesFromJson() {
      File file = new File(RECIPES_FILE_PATH);
      boolean isFileValid = file.exists() && file.length() > 0;
   
      if (isFileValid) {
         try (Reader reader = new FileReader(file)) {
            Gson gson = new Gson();
            Recipe[] recipesArray = gson.fromJson(reader, Recipe[].class);
            if (recipesArray != null) {
               System.out.println("Recipes successfully loaded from JSON.");
               return new ArrayList<>(List.of(recipesArray));
            }
         } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Load Error - Failed to load recipes from JSON file.");
         }
      } else {
         System.out.println("No recipe file found. Starting with an empty recipe list.");
      }
      return new ArrayList<>();
   }

   public void saveRecipe(Recipe recipe, Image image) throws IOException {
      
      recipeList.addAll(loadRecipesFromJson()); 

      List<Integer> existingIds = recipeList.stream()
      .map(Recipe::getID)
      .sorted()
      .collect(Collectors.toList());

      int newId = 1;

      for (int id : existingIds) {
         if (id == newId) {
             newId++; // Move to the next available number
         } else {
             break; // Found a gap, use this ID
         }
      }

      recipe.setID(newId);

      recipeList.add(recipe);

      // Ensure destination directory exists
      File destinationFolder = new File("src/main/resources/org/javafx/Resources/Recipe Images");
      if (!destinationFolder.exists() && !destinationFolder.mkdirs()) {
         throw new IOException("Failed to create directory: " + destinationFolder.getAbsolutePath());
      }

      // Define image file
      String imageName = recipe.getName() + ".png";
      File destinationFile = new File(destinationFolder, imageName);

      if (image.getUrl() != null) {
         try (InputStream in = URI.create(image.getUrl()).toURL().openStream()) {
            Files.copy(in, destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Image successfully saved to: " + destinationFile.getAbsolutePath());
         } catch (Exception e) {
            System.err.println("Failed to download image from URL: " + image.getUrl());
            e.printStackTrace();
         }
      } else {
            System.out.println("No valid URL for the image. Image not saved.");
      }
      
      saveRecipesToJson(recipeList);
  }

   private void saveRecipesToJson(List<Recipe> recipes) {
      System.out.println(recipes);

      File file = new File(RECIPES_FILE_PATH);
   
      try (Writer writer = new FileWriter(file)) {
         Gson gson = new GsonBuilder().setPrettyPrinting().create();
         gson.toJson(recipes, writer);
         System.out.println("Recipes successfully saved to JSON.");
      } catch (IOException e) {
         e.printStackTrace();
         System.out.println("Save Error - Failed to save recipes to JSON file.");
      }
   }

   private void setHoverEffect(Button button) {
      button.setOnMouseEntered(this::handleMouseEntered);
      button.setOnMouseExited(this::handleMouseExited);
   }  

   private void handleMouseEntered(MouseEvent event) {
      Button button = (Button) event.getSource();
      // Change style when mouse enters
      button.setStyle("-fx-background-color: #FF7F11; -fx-text-fill: white; -fx-font-size: 30px; -fx-font-weight: bold; -fx-background-radius: 50; ");
   }

   private void handleMouseExited(MouseEvent event) {
      Button button = (Button) event.getSource();
      // Reset style when mouse exits
      button.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 30px; -fx-font-weight: bold; -fx-background-radius: 50; ");
   }

   private List<Map<String, AttributeValue>> fetchCommunityRecipes() {
      List<Map<String, AttributeValue>> recipes = new ArrayList<>();
      try {
         ScanRequest scanRequest = ScanRequest.builder()
            .tableName("Recipes")
            .build();

         ScanResponse scanResponse = database.scan(scanRequest);
         recipes = scanResponse.items();

      } catch (DynamoDbException e) {
          System.err.println("Failed to fetch recipes: " + e.getMessage());
      }

      return recipes;
   }
  
   private void configureSortBy() {
      sortBy.getItems().addAll("A-Z", "Z-A", "Prep Time", "Cook Time");
      sortBy.setOnAction(e -> sortRecipes());
   }

   private void loadCommunityRecipes() {
      recipes = fetchCommunityRecipes();
      buildRecipeCards(recipes);
  
      if (recipes.isEmpty()) {
          noRecipesTXT.setVisible(true);
      } else {
          noRecipesTXT.setVisible(false);
      }
   }

   private void configureDropdowns() {
      recipeCategory.getItems().addAll("dinner", "lunch", "breakfast", "snack", "other");
      ingredientUnitEntry.getItems().addAll("g", "kg", "ml", "l", "tsp", "tbsp", "cup", "oz", "lb", "pinch", "dash");
   }

   private void configureFilters() {
      categoryDropDown.setOnAction(e -> filterRecipes());
   }

   private void buildRecipeCards(List<Map<String, AttributeValue>> items) {
      recipeFlowPane.getChildren().clear();
      recipeCards.clear();

      for (Map<String, AttributeValue> item : items) {

         AttributeValue nameAttr = item.get("name");
         AttributeValue descAttr = item.get("description");

         // Skip items with missing essential attributes
         if (nameAttr == null || descAttr == null) {
               continue;
         }

         String recipeName = nameAttr.s();
         String description = descAttr.s();

         try {
               FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/javafx/Resources/FXMLs/RecipeCard.fxml"));
               VBox recipeCard = loader.load();
               RecipeCardController controller = loader.getController();

               // Create a Recipe object with the necessary details
               Recipe recipe = new Recipe(
                  -1, // Dummy ID, as it's from community
                  recipeName,
                  item.getOrDefault("category", AttributeValue.builder().s("Uncategorized").build()).s(),
                  "Community",
                  description,
                  Integer.parseInt(item.getOrDefault("prepTime", AttributeValue.builder().n("0").build()).n()),
                  Integer.parseInt(item.getOrDefault("passiveTime", AttributeValue.builder().n("0").build()).n()),
                  Integer.parseInt(item.getOrDefault("cookTime", AttributeValue.builder().n("0").build()).n()),
                  1, // Default complexity
                  Integer.parseInt(item.getOrDefault("servings", AttributeValue.builder().n("1").build()).n()),
                  item.containsKey("tags") ? item.get("tags").l().stream().map(AttributeValue::s).toArray(String[]::new) : new String[]{},
                  item.containsKey("ingredients") ? item.get("ingredients").l().stream().map(AttributeValue::s).toArray(String[]::new) : new String[]{},
                  item.containsKey("equipment") ? item.get("equipment").l().stream().map(AttributeValue::s).toArray(String[]::new) : new String[]{},
                  item.containsKey("steps") ? item.get("steps").l().stream().map(AttributeValue::s).toArray(String[]::new) : new String[]{}
               );
               
               // **Construct S3 Image URL**
               String imageUrl = S3_BASE_URL + recipeName.replace(" ", "%20") + ".jpg"; // URL encode spaces
               Image image = new Image(imageUrl, true);

               // Set data on the card
               controller.setRecipeData(recipe, image, this, "community");

               // Add to UI
               recipeFlowPane.getChildren().add(recipeCard);
               recipeCards.add(recipeCard);

               applyHoverEffect(recipeCard, recipe);

               // Recipe Card Style to match theme but seems a bit too much
               /*
               recipeCard.setStyle(
                  "-fx-background-color: #444444; " +
                  "-fx-border-color: #FF7F11; " +
                  "-fx-border-radius: 10; " +
                  "-fx-padding: 10; " +
                  "-fx-background-radius: 10;"
               );
               */

         } catch (Exception e) {
               e.printStackTrace();
         }
      }

      // Show or hide 'noRecipesTXT' based on recipe availability
      noRecipesTXT.setVisible(recipeCards.isEmpty());
   }

   private void applyHoverEffect(VBox recipeCard, Recipe recipe) {
      Tooltip tooltip = new Tooltip();
      tooltip.setStyle(
                        "-fx-background-color: #3C3C3C; " +
                        "-fx-text-fill: white; " +
                        "-fx-padding: 10; " +
                        "-fx-font-size: 14px; " +
                        "-fx-border-radius: 10; " +
                        "-fx-background-radius: 10; " +
                        "-fx-border-color: #FF7F11;"
                     );
      
      // Set tooltip max width and enable wrapping
      tooltip.setMaxWidth(300);  // Set maximum width to prevent excessive horizontal stretching
      tooltip.setWrapText(true); // Ensure text wraps to the next line
  
      String tooltipContent = String.format(
          "Name: %s%nServings: %d%nPrep Time: %d min%nCook Time: %d min%nDescription: %s%nTags: %s",
          recipe.getName(),
          recipe.getServings(),
          recipe.getPrepTime(),
          recipe.getCookTime(),
          recipe.getDescription(),
          String.join(", ", recipe.getTags())
      );
  
      tooltip.setText(tooltipContent);
  
      // Attach tooltip explicitly to the VBox (recipe card)
      recipeCard.setOnMouseEntered(event -> {
          if (!tooltip.isShowing()) {
              tooltip.show(recipeCard, event.getScreenX(), event.getScreenY() + 10);
          }
      });
  
      recipeCard.setOnMouseExited(event -> tooltip.hide());
  
      recipeCard.setOnMouseMoved(event -> {
          tooltip.setX(event.getScreenX());
          tooltip.setY(event.getScreenY() + 10);
      });
  }
  
   private void sortRecipes() {
      if (recipes == null || recipes.isEmpty()) return;

      String criteria = sortBy.getValue();
      Comparator<Map<String, AttributeValue>> comparator;

      switch (criteria) {
         case "A-Z":
            comparator = Comparator.comparing(item -> item.get("name").s());
            break;
         case "Z-A":
            comparator = Comparator.comparing(item -> item.get("name").s(), Comparator.reverseOrder());
            break;
         case "Prep Time":
            comparator = Comparator.comparingInt(item -> Integer.parseInt(item.get("prepTime").n()));
            break;
         case "Cook Time":
            comparator = Comparator.comparingInt(item -> Integer.parseInt(item.get("cookTime").n()));
            break;
         default:
            return;
      }

      // Create a new modifiable list from the original unmodifiable list
      List<Map<String, AttributeValue>> modifiableRecipes = new ArrayList<>(recipes);
      modifiableRecipes.sort(comparator);

      buildRecipeCards(modifiableRecipes);
   }

   private void filterRecipes() {
      String categoryFilter = categoryDropDown.getValue();
  
      List<Map<String, AttributeValue>> filtered = recipes.stream()
          .filter(item -> categoryFilter == null || 
              item.containsKey("category") && item.get("category").s().equals(categoryFilter)
          )
          .collect(Collectors.toList());
  
      buildRecipeCards(filtered);
  }

   private void filterRecipesBySearch(String query) {
      if (query.isEmpty()) {
         buildRecipeCards(recipes);
         return;
      }

      String lowerCaseQuery = query.toLowerCase();
      List<Map<String, AttributeValue>> filtered = recipes.stream()
         .filter(item -> item.get("name").s().toLowerCase().contains(lowerCaseQuery)
            || item.get("description").s().toLowerCase().contains(lowerCaseQuery)
            || item.get("tags").l().stream().anyMatch(tag -> tag.s().toLowerCase().contains(lowerCaseQuery)))
         .collect(Collectors.toList());

      buildRecipeCards(filtered);
   }

   private List<String> getUserInventory() {
    // Retrieve the current user's inventory.
    // This could be loaded from a database, a file, or application state.
    // Here’s a dummy example:
    return Arrays.asList("tomato", "cheese", "basil", "olive oil");
   }

   private List<Recipe> getSuggestedRecipes(List<String> userInventory, List<Recipe> allRecipes) {
      List<Recipe> fullMatches = new ArrayList<>();
      List<Recipe> partialMatches = new ArrayList<>();
      
      for (Recipe recipe : allRecipes) {
         String[] recipeIngredients = recipe.getIngredients();
         boolean hasAllIngredients = true;
         int matchCount = 0;
         
         for (String ingredient : recipeIngredients) {
               if (userInventory.contains(ingredient.toLowerCase())) {  // assuming uniform casing
                  matchCount++;
               } else {
                  hasAllIngredients = false;
               }
         }
         
         if (hasAllIngredients) {
               fullMatches.add(recipe);
         } else if (matchCount > 0) {
               partialMatches.add(recipe);
         }
      }
      
      // Define the minimum number of recipes you want to display
      int threshold = 5;
      
      if (fullMatches.size() >= threshold) {
         return fullMatches;
      } else if (partialMatches.size() >= threshold) {
         return partialMatches;
      } else {
         // If there are still too few, fall back to a few random recipes.
         List<Recipe> randomRecipes = new ArrayList<>(allRecipes);
         Collections.shuffle(randomRecipes);
         return randomRecipes.subList(0, Math.min(threshold, randomRecipes.size()));
      }
   }

   private VBox createRecipeCard(Recipe recipe) {
      try {
         FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/javafx/Resources/FXMLs/RecipeCard.fxml"));
         VBox recipeCard = loader.load();
         RecipeCardController controller = loader.getController();

         // **Load Image from S3**
         String imageUrl = S3_BASE_URL + recipe.getName().replace(" ", "%20") + ".jpg"; // URL encode spaces
         Image image = new Image(imageUrl, true);
          
         // Pass "community" as the viewType to set up the community-specific context menu.
         controller.setRecipeData(recipe, image, this,"community");

         // Determine outline color based on completeness:
         List<String> inventory = getUserInventory();
         String[] ingredients = recipe.getIngredients();

         int matchCount = 0;
         for (String ingredient : ingredients) {
            if (inventory.contains(ingredient.toLowerCase())) {
               matchCount++;
            }
         }

         

         return recipeCard;
      } catch (Exception e) {
          e.printStackTrace();
          return new VBox(new Label("Error loading recipe card"));
      }
  }

   private void updateSuggestedRecipes() {
      List<String> userInventory = getUserInventory();
      List<Recipe> allRecipes = convertToRecipeList(recipes);  // This should fetch all available recipes
      List<Recipe> suggestedRecipes = getSuggestedRecipes(userInventory, allRecipes);
      
      // Clear the HBox and add each recipe card
      suggestedRecipesBox.getChildren().clear();
      for (Recipe recipe : suggestedRecipes) {
            VBox recipeCard = createRecipeCard(recipe);
            suggestedRecipesBox.getChildren().add(recipeCard);
      }
   }

   public void showRecipeDetails(int recipeId, String name, Image image, Recipe recipe) {

      displayStep = 0;

      myRecipesPane.setVisible(false);
      recipeDetailsPane.setVisible(true);
      recipeNameTXT.setText(name);

      // **Load Image from S3**
      String imageUrl = S3_BASE_URL + name.replace(" ", "%20") + ".jpg";
      recipeDetailsImages.setImage(new Image(imageUrl, true));

      recipeServingsTXT.setText("Servings: " + recipe.getServings());
      recipePrepTimeTXT.setText("Prep Time: " + recipe.getPrepTime() + " Minutes");
      recipePassiveTimeTXT.setText("Passive Time: " + recipe.getPassiveTime() + " Minutes");
      recipeCookTimeTXT.setText("Cook Time: " + recipe.getCookTime() + " Minutes");

      int totalTime = recipe.getPrepTime() + recipe.getPassiveTime() + recipe.getCookTime(); // add the prep and cook times

      recipeTotalTimeTXT.setText("Total: " + totalTime + " Minutes");
      recipeComplexityTXT.setText("Complexity: " + getComplexityLabel(recipe.getComplexity()));

      recipeDetailDescription.setText(recipe.getDescription());

      recipeIngredients.setItems(FXCollections.observableArrayList(recipe.getIngredients()));
      specialEquipmentTXTArea.setItems(FXCollections.observableArrayList(recipe.getEquipment()));
      recipeTagFlowPane.getChildren().clear();

      for (String tag : recipe.getTags()) {
         // Create a label for each tag
         Label tagLabel = new Label(tag);
         
         // Optionally style the label (for example, add padding, border, background, etc.)
         tagLabel.setStyle(
                           "-fx-background-color: #555555; " +
                           "-fx-text-fill: white; " +
                           "-fx-padding: 5 10; " +
                           "-fx-border-radius: 10; " +
                           "-fx-background-radius: 10; " +
                           "-fx-font-size: 16px; " +
                           "-fx-border-color: #FF7F11;"
                        );
         
         // Add the label to the FlowPane
         recipeTagFlowPane.getChildren().add(tagLabel);
      }

      preparationSteps = FXCollections.observableArrayList(recipe.getSteps());

      if (!preparationSteps.isEmpty()) {
         stepOfTXT.setText("Step 1 of " + preparationSteps.size());
         stepArea.setText(preparationSteps.get(0));
      } else {
         stepOfTXT.setText("Step 1 of 1");
         stepArea.setText("");
      }

      // Styling
      specialEquipmentTXTArea.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-fill: white;");
      recipeDetailDescription.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-fill: white;");
      stepArea.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-fill: white;");

      recipePrepTimeTXT.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-fill: white;");
      recipePassiveTimeTXT.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-fill: white;");
      recipeCookTimeTXT.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-fill: white;");
      recipeTotalTimeTXT.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-fill: white;");

   }

   public void showRecipeReviewsAndFeedback(int recipeId, String name, Image image, Recipe recipe) {

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
   
   // Navigate between steps
   private void detailsSteps(int direction) {
      // Save the current step text before navigating
      if (displayStep >= 0 && displayStep < preparationSteps.size()) {
         preparationSteps.set(displayStep, stepArea.getText().trim());
      }

      // Calculate new step index
      int newStep = displayStep + direction;
      if (newStep >= 0 && newStep < preparationSteps.size()) {
         displayStep = newStep;
         updateDetailsStepView();
      }
   }

   // Update TextArea and stepIndex label to display the current step
   private void updateDetailsStepView() {
      if (preparationSteps.isEmpty()) {
         stepArea.setText("");
         stepOfTXT.setText("Step 1 of 1");
      } else {
         stepArea.setText(preparationSteps.get(displayStep));
         stepOfTXT.setText("Step " + (displayStep + 1) + " of " + preparationSteps.size());
      }
   }

   private List<Recipe> convertToRecipeList(List<Map<String, AttributeValue>> items) {
      List<Recipe> allRecipes = new ArrayList<>();
      for (Map<String, AttributeValue> item : items) {
          // Check for essential fields before converting
          if (item.get("name") == null || item.get("description") == null) {
              continue;
          }
          String recipeName = item.get("name").s();
          String description = item.get("description").s();
          
          Recipe recipe = new Recipe(
              -1,  // Dummy ID for community recipes
              recipeName,
              item.getOrDefault("category", AttributeValue.builder().s("Uncategorized").build()).s(),
              "Community",  // Indicate the source
              description,
              Integer.parseInt(item.getOrDefault("prepTime", AttributeValue.builder().n("0").build()).n()),
              Integer.parseInt(item.getOrDefault("passiveTime", AttributeValue.builder().n("0").build()).n()),
              Integer.parseInt(item.getOrDefault("cookTime", AttributeValue.builder().n("0").build()).n()),
              1,  // Default complexity
              Integer.parseInt(item.getOrDefault("servings", AttributeValue.builder().n("1").build()).n()),
              item.containsKey("tags") ? item.get("tags").l().stream().map(AttributeValue::s).toArray(String[]::new) : new String[]{},
              item.containsKey("ingredients") ? item.get("ingredients").l().stream().map(AttributeValue::s).toArray(String[]::new) : new String[]{},
              item.containsKey("equipment") ? item.get("equipment").l().stream().map(AttributeValue::s).toArray(String[]::new) : new String[]{},
              item.containsKey("steps") ? item.get("steps").l().stream().map(AttributeValue::s).toArray(String[]::new) : new String[]{}
          );
          allRecipes.add(recipe);
      }
      return allRecipes;
  }

}