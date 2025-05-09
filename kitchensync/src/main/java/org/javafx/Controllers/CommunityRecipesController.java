package org.javafx.Controllers;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.javafx.Item.Item;
import org.javafx.Main.Main;
import org.javafx.Recipe.Recipe;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
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
import javafx.stage.Modality;
import javafx.stage.Stage;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

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

   // Reviews
   @FXML private Text communityRatingStars, communityRatingStarsReview, recipeReviewName;
   @FXML private Button recipeReviewsButton, postCommentButton, postCommentCloseButton, recipeNotesButton; 
   @FXML private ComboBox<String> reviewType;
   @FXML private ScrollPane reviewPane;

   // ==============================
   // DATA STORAGE & CONSTANTS
   // ==============================
   private ObservableList<String> tags = FXCollections.observableArrayList();
   private ObservableList<Ingredient> ingredients = FXCollections.observableArrayList();
   private List<String> ingredientEntries = new ArrayList<>();
   private ObservableList<String> equipment = FXCollections.observableArrayList();
   private List<String> preparationSteps = new ArrayList<>();
   private ObservableList<Recipe> recipeList = FXCollections.observableArrayList();

   // Selected filters for recipes
   private Set<String> selectedIngredients = new HashSet<>();
   private Set<String> selectedTags = new HashSet<>();
   private Set<String> availableIngredients = new HashSet<>();
   private Set<String> availableTags = new HashSet<>();
   private Map<Integer, VBox> recipeWidgets = new HashMap<>();

   private static final String S3_BUCKET_NAME = "kitchensyncimages";
   private static final String S3_BASE_URL = "https://" + S3_BUCKET_NAME + ".s3.amazonaws.com/";

   private List<Recipe> communityRecipes = new ArrayList<>();
   private List<Recipe> filteredRecipes = new ArrayList<>();
   private final Map<String, VBox> recipeCardMap = new HashMap<>();

   private Recipe recipeBeingEdited = null;
   private Recipe currentRecipe;
   @FXML private VBox currentRecipeCard, reviewBoard;

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

      reviewPane.setStyle("-fx-background-color: transparent;");
      reviewBoard.setStyle("-fx-background-color: transparent;");

      cookItButton.setOnAction(e -> handleSaveAndCook());
      initializeDatabaseAndS3();
      loadCommunityRecipes();

      applyFiltersAndSort();
      buildRecipeCards();

      configureSortBy();
      configureFilters();
      configureDropdowns();
      configureEquipmentTable();
      configureIngredientTable();
      populateFilterOptions(); // Ensure filters get populated
      setupMultiSelectFilters(); // Set up Filter Buttons

      searchBar.textProperty().addListener((obs, oldText, newText) -> {
         applyFiltersAndSort();
         buildRecipeCards();
      });

      setupUIEventHandlers();

      updateSuggestedRecipes();

      recipeNotesButton.setOnAction(event -> openRecipeNotesPopup());
      recipeReviewsButton.setOnAction(event -> openReviewPage());

      reviewType.getItems().addAll( "All", "Community");
      postCommentButton.setOnAction(event -> openReviewPopup()); 
      postCommentCloseButton.setOnAction(event -> closeReviewWindow());
      
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
      uploadButton.setOnAction(event -> {
         if (recipeBeingEdited != null) {
             updateRecipe();  // If we’re in "edit mode", call update
         } else {
             uploadRecipe();  // If "edit mode" is not set, create a new recipe
         }
     });     
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
         headerLabel.setStyle("-fx-background-color: #2E2E2E; -fx-text-fill: white; -fx-font-size: 18px;");
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

   private void handleSaveAndCook() {
      if (currentRecipe == null) {
         showAlert("Error", "No Recipe Selected", "Please select a recipe first.");
         return;
      }
   
      // Save to local MyRecipes (optional - skip if already saved)
      try {
         boolean alreadySaved = recipeList.stream()
            .anyMatch(r -> r.getName().equals(currentRecipe.getName()));
   
         if (!alreadySaved) {
            saveRecipe(currentRecipe, new Image(S3_BASE_URL + currentRecipe.getUserID() + "-" + currentRecipe.getRecipeDBId() + ".jpg"));
            System.out.println("Recipe saved locally.");
         } else {
            System.out.println("Recipe already in MyRecipes.");
         }
      } catch (Exception ex) {
         ex.printStackTrace();
         showAlert("Save Failed", "Could not save recipe.", "Please try again.");
         return;
      }
   
      // Open the cooking screen with step view
      showRecipeDetails(
         currentRecipe.getID(),
         currentRecipe.getName(),
         new Image(S3_BASE_URL + currentRecipe.getUserID() + "-" + currentRecipe.getRecipeDBId() + ".jpg"),
         currentRecipe
      );
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
      preparationSteps = new ArrayList<>(recipe.getSteps());
      currentStep = 0;
      updateStepView();

      // Populate ingredients
      ingredients.clear();
      ingredientEntries.clear();
      for (Ingredient ing : recipe.getIngredients()) {
         // Optionally, if you need the string version:
         ingredientEntries.add(ing.getName() + ": " + ing.getAmount() + " " + ing.getUnit());
         ingredients.add(ing);
      }

      // Populate special equipment
      equipment.clear();
      equipment.addAll(recipe.getEquipment());

      // Populate tags
      tags.clear();
      tags.addAll(recipe.getTags());
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

      //Change to get the UsersID
      String userId = "testUserID123";  // e.g. "alice123"

      String recipeName = UploadRecipeName.getText();
      int prepTime = Integer.parseInt(recipeETAPrep.getText());
      int cookTime = Integer.parseInt(recipeETA.getText());
      int passiveTime = Integer.parseInt(recipeETAPassive.getText());
      int servings = Integer.parseInt(recipeYield.getText());
      String description = recipeDescription.getText();

      Map<String, AttributeValue> recipeItem = new HashMap<>();

      String recipeDBID = UUID.randomUUID().toString();

      
      recipeItem.put("Recipe", AttributeValue.builder().s(recipeDBID).build());
      recipeItem.put("UserId",    AttributeValue.builder().s(userId).build());
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
         // add multi image functions
         if (selectedImageFile != null) {
               String s3Key = userId + "-" + recipeDBID + ".jpg";
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

      int newId = 0;  // Start looking for the smallest ID at 0

      for (int id : existingIds) {
         if (id < newId) {
            // This existingId is less than the candidate — ignore and keep going
            // because maybe we find a match later or we never do.
         } else if (id == newId) {
            // The candidate ID is in use; move to the next integer
            newId++;
         } else {
            // id > newId → there's a gap
            // newId is already the first free ID
            break;
         }
      }

      // 'newId' is now guaranteed to be the smallest unused positive ID
      System.out.println("Next free ID is " + newId);

      recipe.setID(newId);
      System.out.println(recipe.getID());

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
            //System.out.println("Image successfully saved to: " + destinationFile.getAbsolutePath());
         } catch (Exception e) {
            //System.err.println("Failed to download image from URL: " + image.getUrl());
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
      List<Map<String, AttributeValue>> rawItems = fetchCommunityRecipes();
      this.communityRecipes = convertToRecipeList(rawItems);
  
      noRecipesTXT.setVisible(communityRecipes.isEmpty());
   }

   private void configureDropdowns() {
      recipeCategory.getItems().addAll("dinner", "lunch", "breakfast", "snack", "other");
      ingredientUnitEntry.getItems().addAll("g", "kg", "ml", "l", "tsp", "tbsp", "cup", "oz", "lb", "pinch", "dash");
   }

   private void configureFilters() {
      categoryDropDown.setOnAction(e -> filterRecipes());
   }

   /**
    * Builds (or rebuilds) the FlowPane UI from the filteredRecipes list.
    */
   private void buildRecipeCards() {
      recipeFlowPane.getChildren().clear();
      recipeCardMap.clear();

      if (filteredRecipes.isEmpty()) {
         noRecipesTXT.setVisible(true);
         return;
      }
      noRecipesTXT.setVisible(false);

      for (Recipe r : filteredRecipes) {
         VBox card = createRecipeCard(r);
         // If you prefer a different key, store by ID or a compound key:
         recipeCardMap.put(r.getName(), card);
         recipeFlowPane.getChildren().add(card);
      }
   }

   // Method to apply hover effect for displaying short recipe details
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
  
      // Create a tooltip string with added spacing (\n\n) between items
      String tooltipContent = String.format(
         "Name: %s\n\n" +
         "Servings: %d\n\n" +
         "Prep Time: %d min\n\n" +
         "Cook Time: %d min\n\n" +
         "Description: %s\n\n" +
         "Tags: %s\n\n" +
         "Local Rating: %d\n\n" +
         "Community Rating: %d\n\n" +
         "Recipe Notes: %s",
         recipe.getName(),
         recipe.getServings(),
         recipe.getPrepTime(),
         recipe.getCookTime(),
         recipe.getDescription(),
         String.join(", ", recipe.getTags()),
         recipe.getLocalRating(),
         recipe.getCommunityRating(),
         recipe.getRecipeNotes()
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

  private void saveRecipeNotes() {
      if (currentRecipe != null) {
         saveRecipesToJson(recipeList);
      }
   }

   private void openRecipeNotesPopup() {
      if (currentRecipe == null) return;
      
      // replace with Main.getCurrentUserID() in the testUserID123
      boolean isAuthor = currentRecipe.getUserID().equals("testUserID123");
  
      Stage popupStage = new Stage();
      popupStage.initModality(Modality.APPLICATION_MODAL);
      popupStage.setTitle(isAuthor ? "Edit Recipe Notes" : "View Recipe Notes");
  
      VBox vbox = new VBox(15);
      vbox.setPadding(new Insets(20));
      vbox.setAlignment(Pos.CENTER);
      vbox.setStyle("-fx-background-color: #2E2E2E;");
  
      Label notesLabel = new Label("Recipe Notes:");
      notesLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
  
      TextArea notesArea = new TextArea(currentRecipe.getRecipeNotes());
      notesArea.setWrapText(true);
      notesArea.setPrefHeight(200);
      notesArea.setStyle(
          "-fx-background-color: #444444; " +
          "-fx-text-fill: black; " +
          "-fx-border-radius: 8; -fx-background-radius: 8;"
      );
      notesArea.setEditable(isAuthor);
  
      Button closeButton = new Button(isAuthor ? "Save & Close" : "Close");
      closeButton.setStyle(
          "-fx-background-color: " + (isAuthor ? "#FF7F11" : "#555555") + ";" +
          " -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 8;"
      );
  
      closeButton.setOnAction(event -> {
          if (isAuthor) {
              currentRecipe.setRecipeNotes(notesArea.getText());
              saveRecipeNotes();
          }
          popupStage.close();
      });
  
      vbox.getChildren().addAll(notesLabel, notesArea, closeButton);
      Scene scene = new Scene(vbox, 450, 350);
      popupStage.setScene(scene);
      popupStage.showAndWait();
  }
  

  private void openReviewPage() {
      if (currentRecipe == null) return;

      recipeReviewName.setText(currentRecipe.getName());

      loadReviewsForRecipe(currentRecipe.getRecipeDBId());

      recipeDetailsPane.setVisible(false);
      recipeReviews.setVisible(true);
   }


   private void closeReviewWindow() {

      recipeDetailsPane.setVisible(true);
      recipeReviews.setVisible(false);

   }

   private void openReviewPopup() {
      if (currentRecipe == null) return;

      Stage reviewStage = new Stage();
      reviewStage.initModality(Modality.APPLICATION_MODAL);
      reviewStage.setTitle("Add Review");

      VBox vbox = new VBox(10);
      vbox.setPadding(new Insets(20));
      vbox.setAlignment(Pos.CENTER);
      vbox.setStyle("-fx-background-color: #2E2E2E;");

      Label titleLabel = new Label("Review Title:");
      titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");

      TextField titleField = new TextField();
      titleField.setStyle(" -fx-text-fill: black; -fx-border-radius: 8; -fx-background-radius: 8;");

      Label bodyLabel = new Label("Your Review:");
      bodyLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");

      TextArea bodyArea = new TextArea();
      bodyArea.setWrapText(true);
      bodyArea.setPrefHeight(100);
      bodyArea.setStyle(" -fx-text-fill: black; -fx-border-radius: 8; -fx-background-radius: 8;");

      Label ratingLabel = new Label("Rating:");
      ratingLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");

      ComboBox<Integer> ratingBox = new ComboBox<>();
      ratingBox.getItems().addAll(1, 2, 3, 4, 5);
      ratingBox.setValue(5);
      ratingBox.getStyleClass().add("combo-box");

      Button saveButton = new Button("Save Review");
      saveButton.setStyle("-fx-background-color: #FF7F11; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 8;");

      Button cancelButton = new Button("Cancel");
      cancelButton.setStyle("-fx-background-color: #555555; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 8;");

      saveButton.setOnAction(event -> {
         if (!titleField.getText().trim().isEmpty() && !bodyArea.getText().trim().isEmpty()) {
            saveReview(currentRecipe.getRecipeDBId(), titleField.getText(), bodyArea.getText(), ratingBox.getValue());
            reviewStage.close();
         } else {
            showAlert("Error", "Missing Fields", "Please fill in all fields before submitting.");
         }
      });

      cancelButton.setOnAction(event -> reviewStage.close());

      HBox buttonBox = new HBox(10, saveButton, cancelButton);
      buttonBox.setAlignment(Pos.CENTER);

      vbox.getChildren().addAll(titleLabel, titleField, bodyLabel, bodyArea, ratingLabel, ratingBox, buttonBox);
      Scene scene = new Scene(vbox, 400, 300);
      reviewStage.setScene(scene);
      reviewStage.showAndWait();
   }

   private void showAlert(String title, String header, String content) {
      Alert alert = new Alert(AlertType.ERROR);
      alert.setTitle(title);
      alert.setHeaderText(header);
      alert.setContentText(content);
      alert.showAndWait();
   }
  
   private void sortRecipes() {
      applyFiltersAndSort();
      buildRecipeCards();
   }

   private void saveReview(String recipeDBId, String title, String body, int rating) {
      Map<String, AttributeValue> reviewItem = new HashMap<>();
      reviewItem.put("ReviewID", AttributeValue.builder().s(UUID.randomUUID().toString()).build());
      reviewItem.put("RecipeID", AttributeValue.builder().s(recipeDBId).build());
      reviewItem.put("UserID", AttributeValue.builder().s("testUserID123").build());
      reviewItem.put("Title", AttributeValue.builder().s(title).build());
      reviewItem.put("Body", AttributeValue.builder().s(body).build());
      reviewItem.put("Rating", AttributeValue.builder().n(String.valueOf(rating)).build());
  
      try {
          database.putItem(PutItemRequest.builder()
              .tableName("RecipeReviews")
              .item(reviewItem)
              .build());
          System.out.println("Review saved successfully!");
      } catch (Exception e) {
          e.printStackTrace();
          System.out.println("Error saving review.");
      }
  }
  
   private void loadReviewsForRecipe(String recipeId) {
      try {
         reviewBoard.getChildren().clear();

         ScanRequest scanRequest = ScanRequest.builder()
            .tableName("RecipeReviews")
            .build();

         ScanResponse response = database.scan(scanRequest);
         List<Map<String, AttributeValue>> items = response.items();

         int totalRating = 0;
         int reviewCount = 0;

         for (Map<String, AttributeValue> item : items) {
            if (item.containsKey("RecipeID") && item.get("RecipeID").s().equals(recipeId)) {
                  String title = item.get("Title").s();
                  String body = item.get("Body").s();
                  int rating = Integer.parseInt(item.get("Rating").n());

                  totalRating += rating;
                  reviewCount++;
                  
                  VBox reviewCard = new VBox(8);
                  reviewCard.setStyle(
                     "-fx-background-color: #2E2E2E;" +
                     "-fx-padding: 12;" +
                     "-fx-background-radius: 12;" +
                     "-fx-border-color: #FF7F11;" +
                     "-fx-border-radius: 12;" +
                     "-fx-border-width: 2;"
                  );

                  Label titleLabel = new Label("★ " + rating + "/5 - " + title);
                  titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");

                  Label bodyLabel = new Label(body);
                  bodyLabel.setWrapText(true);
                  bodyLabel.setStyle("-fx-text-fill: #DDDDDD; -fx-font-size: 16px;");

                  reviewCard.getChildren().addAll(titleLabel, bodyLabel);

                  // Add Edit/Delete for current user
                  if (item.get("UserID").s().equals("testUserID123")) {
                     Button editBtn = new Button("Edit");
                     Button deleteBtn = new Button("Delete");

                     editBtn.setOnAction(e -> openReviewPopupWithPrefill(item)); // implement this
                     deleteBtn.setOnAction(e -> deleteReview(item.get("ReviewID").s()));

                     HBox actionBox = new HBox(10, editBtn, deleteBtn);
                     reviewCard.getChildren().add(actionBox);
                  }
                  
                  reviewBoard.getChildren().add(reviewCard);
            }
            reviewBoard.setSpacing(12);
         }

         // Update star display text
         if (reviewCount > 0) {
            double avgRating = (double) totalRating / reviewCount;
            communityRatingStars.setText(String.format("★ %.1f", avgRating));
            communityRatingStarsReview.setText(String.format("★ %.1f (%d reviews)", avgRating, reviewCount));
            if (currentRecipe != null && currentRecipe.getRecipeDBId().equals(recipeId)) {
               currentRecipe.setCommunityRating((int)Math.round(avgRating));
            }
         } else {
            communityRatingStars.setText("★ N/A");
            communityRatingStarsReview.setText("★ N/A");
         }

         //reviewPane.setContent(reviewListBox);
         

      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   private void openReviewPopupWithPrefill(Map<String, AttributeValue> reviewItem) {
      if (currentRecipe == null) return;
  
      String oldTitle = reviewItem.get("Title").s();
      String oldBody = reviewItem.get("Body").s();
      int oldRating = Integer.parseInt(reviewItem.get("Rating").n());
      String reviewID = reviewItem.get("ReviewID").s();
  
      Stage reviewStage = new Stage();
      reviewStage.initModality(Modality.APPLICATION_MODAL);
      reviewStage.setTitle("Edit Review");
  
      VBox vbox = new VBox(10);
      vbox.setPadding(new Insets(20));
      vbox.setAlignment(Pos.CENTER);
      vbox.setStyle("-fx-background-color: #2E2E2E;");
  
      Label titleLabel = new Label("Review Title:");
      titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
  
      TextField titleField = new TextField(oldTitle);
      titleField.setStyle("-fx-text-fill: black; -fx-border-radius: 8; -fx-background-radius: 8;");
  
      Label bodyLabel = new Label("Your Review:");
      bodyLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
  
      TextArea bodyArea = new TextArea(oldBody);
      bodyArea.setWrapText(true);
      bodyArea.setPrefHeight(100);
      bodyArea.setStyle("-fx-text-fill: black; -fx-border-radius: 8; -fx-background-radius: 8;");
  
      Label ratingLabel = new Label("Rating:");
      ratingLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
  
      ComboBox<Integer> ratingBox = new ComboBox<>();
      ratingBox.getItems().addAll(1, 2, 3, 4, 5);
      ratingBox.setValue(oldRating);
      ratingBox.getStyleClass().add("combo-box");
  
      Button saveButton = new Button("Update Review");
      saveButton.setStyle("-fx-background-color: #FF7F11; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 8;");
  
      Button cancelButton = new Button("Cancel");
      cancelButton.setStyle("-fx-background-color: #555555; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 8;");
  
      saveButton.setOnAction(event -> {
          if (!titleField.getText().trim().isEmpty() && !bodyArea.getText().trim().isEmpty()) {
              Map<String, AttributeValue> updatedReview = new HashMap<>();
              updatedReview.put("ReviewID", AttributeValue.builder().s(reviewID).build());
              updatedReview.put("RecipeID", AttributeValue.builder().s(currentRecipe.getRecipeDBId()).build());
              updatedReview.put("UserID", AttributeValue.builder().s("testUserID123").build());
              updatedReview.put("Title", AttributeValue.builder().s(titleField.getText().trim()).build());
              updatedReview.put("Body", AttributeValue.builder().s(bodyArea.getText().trim()).build());
              updatedReview.put("Rating", AttributeValue.builder().n(String.valueOf(ratingBox.getValue())).build());
  
              try {
                  database.putItem(PutItemRequest.builder()
                      .tableName("RecipeReviews")
                      .item(updatedReview)
                      .build());
  
                  System.out.println("Review updated successfully!");
                  loadReviewsForRecipe(currentRecipe.getRecipeDBId()); // Refresh display
              } catch (Exception e) {
                  e.printStackTrace();
                  showAlert("Error", "Update Failed", "Could not update your review.");
              }
  
              reviewStage.close();
          } else {
              showAlert("Error", "Missing Fields", "Please fill in all fields before submitting.");
          }
      });
  
      cancelButton.setOnAction(event -> reviewStage.close());
  
      HBox buttonBox = new HBox(10, saveButton, cancelButton);
      buttonBox.setAlignment(Pos.CENTER);
  
      vbox.getChildren().addAll(titleLabel, titleField, bodyLabel, bodyArea, ratingLabel, ratingBox, buttonBox);
      Scene scene = new Scene(vbox, 400, 300);
      reviewStage.setScene(scene);
      reviewStage.showAndWait();
  }
  

  private void deleteReview(String reviewId) {
      try {
         Map<String, AttributeValue> key = new HashMap<>();
         key.put("ReviewID", AttributeValue.builder().s(reviewId).build());

         database.deleteItem(builder -> builder
            .tableName("RecipeReviews")
            .key(key));

         System.out.println("Review deleted.");
         loadReviewsForRecipe(currentRecipe.getRecipeDBId()); // Refresh view
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   private String getReadinessStatus(Recipe recipe, List<String> inventory) {
      int total = recipe.getIngredients().size();
      int available = 0;
      boolean needsPrep = false;
  
      for (Ingredient ing : recipe.getIngredients()) {
          String name = ing.getName().toLowerCase();
          if (inventory.contains(name)) {
              available++;
              // you could flag needsPrep = true here if needed (e.g., frozen tag check)
          }
      }
  
      if (available == total) return needsPrep ? "prep" : "ready";
      if (available > 0) return "partial";
      return "incomplete";
  }

   private List<String> getUserInventory() {
      try (Reader reader = new FileReader("itemInventory.json")) {
         Gson gson = new Gson();
         Type listType = new TypeToken<List<Item>>(){}.getType();
         List<Item> inventoryItems = gson.fromJson(reader, listType);
         return inventoryItems.stream()
                  .map(item -> item.getName().toLowerCase())
                  .collect(Collectors.toList());
      } catch (Exception e) {
         e.printStackTrace();
         return new ArrayList<>();
      }
   }

   private List<Recipe> getSuggestedRecipes(List<String> userInventory, List<Recipe> allRecipes) {
      List<Recipe> fullMatches = new ArrayList<>();
      List<Recipe> partialMatches = new ArrayList<>();
      
      for (Recipe recipe : allRecipes) {
         List<Ingredient> recipeIngredients = recipe.getIngredients();
         boolean hasAllIngredients = true;
         int matchCount = 0;
         
         for (Ingredient ing : recipeIngredients) {
            String ingName = ing.getName();
            if (userInventory.contains(ingName.toLowerCase())) {  
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
         String imageUrl = S3_BASE_URL + recipe.getUserID() + "-" + recipe.getRecipeDBId() + ".jpg";
         Image image = new Image(imageUrl, true);
          
         // Pass "community" as the viewType to set up the community-specific context menu.
         controller.setRecipeData(recipe, image, this,"community");

         String status = getReadinessStatus(recipe, getUserInventory());
         String color;
         switch (status) {
            case "ready":      color = "#00FF00"; break; // green
            case "prep":       color = "#FFFF00"; break; // yellow
            case "partial":    color = "#FFA500"; break; // orange
            default:           color = "#FF0000"; break; // red
         }
         recipeCard.setStyle("-fx-border-color: " + color + "; -fx-border-width: 5; -fx-border-radius: 10; -fx-background-radius: 10;");


         recipeCard.setPrefSize(200, 300);
         recipeCard.setMinSize(200, 300);
         recipeCard.setMaxSize(200, 300);

         // Determine outline color based on completeness:
         List<String> inventory = getUserInventory();
         List<Ingredient> ingredients = recipe.getIngredients();

         int matchCount = 0;
         for (Ingredient ing : ingredients) {
            if (inventory.contains(ing.getName().toLowerCase())) {
               matchCount++;
            }
         }

         applyHoverEffect(recipeCard, recipe);

         return recipeCard;
      } catch (Exception e) {
          e.printStackTrace();
          return new VBox(new Label("Error loading recipe card"));
      }
  }

   public void openEditRecipeForm(Recipe recipe) {

      // replace "testUserID123" with like main.getCurrentUserID() or somthing

      if (!recipe.getUserID().equals("testUserID123")) {
         System.out.println("Cannot edit. You are not the owner.");
         return;
      }

      // Keep track of the recipe we are editing
      this.recipeBeingEdited = recipe;
      
      myRecipesPane.setVisible(false);
      addRecipePaneP1.setVisible(true);

      // Fill the fields with the existing recipe info
      UploadRecipeName.setText(recipe.getName());
      recipeCategory.setValue(recipe.getCategory());
      recipeYield.setText(String.valueOf(recipe.getServings()));
      recipeETAPrep.setText(String.valueOf(recipe.getPrepTime()));
      recipeETAPassive.setText(String.valueOf(recipe.getPassiveTime()));
      recipeETA.setText(String.valueOf(recipe.getCookTime()));
      recipeDescription.setText(recipe.getDescription());

      // 1) Steps
      preparationSteps.clear();
      preparationSteps.addAll(recipe.getSteps());
      currentStep = 0;
      updateStepView(); // or however you show the steps in your text field

      // 2) Ingredients
      ingredients.clear();
      ingredientEntries.clear();
      for (Ingredient ing : recipe.getIngredients()) {
         // Build a string representation if needed for DynamoDB or display
         String ingString = ing.getName() + ": " + ing.getAmount() + " " + ing.getUnit();
         ingredientEntries.add(ingString);
         ingredients.add(ing);
      }

      // 3) Equipment
      equipment.clear();
      equipment.addAll(recipe.getEquipment());

      // 4) Tags
      tags.clear();
      tags.addAll(recipe.getTags());
      updateTagView();

      selectedImageFile = null;
      imagePreview.setImage(null);

   }

   private void updateRecipe() {

      // Check ownership
      if (!recipeBeingEdited.getUserID().equals("testUserID123")) {
          System.out.println("Cannot edit. You are not the owner.");
          return;
      }
   
      // Validate the inputs
      if (!validateInputs()) {
         System.out.println("Please fill in all required fields for update.");
         return;
      }

      // Read updated data from UI
      String newName       = UploadRecipeName.getText();
      String newCategory   = recipeCategory.getValue();
      int newServings      = Integer.parseInt(recipeYield.getText());
      int newPrepTime      = Integer.parseInt(recipeETAPrep.getText());
      int newPassiveTime   = Integer.parseInt(recipeETAPassive.getText());
      int newCookTime      = Integer.parseInt(recipeETA.getText());
      String newDesc       = recipeDescription.getText();
   
      // Build the item to PUT in DynamoDB
      Map<String, AttributeValue> updatedItem = new HashMap<>();
      updatedItem.put("Recipe", AttributeValue.builder().s(recipeBeingEdited.getRecipeDBId()).build());
      updatedItem.put("UserId", AttributeValue.builder().s(recipeBeingEdited.getUserID()).build());
      updatedItem.put("name",   AttributeValue.builder().s(newName).build());
      updatedItem.put("prepTime", AttributeValue.builder().n(String.valueOf(newPrepTime)).build());
      updatedItem.put("cookTime", AttributeValue.builder().n(String.valueOf(newCookTime)).build());
      updatedItem.put("passiveTime", AttributeValue.builder().n(String.valueOf(newPassiveTime)).build());
      updatedItem.put("servings", AttributeValue.builder().n(String.valueOf(newServings)).build());
      updatedItem.put("description", AttributeValue.builder().s(newDesc).build());

      updatedItem.put("ingredients", AttributeValue.builder()
         .l(ingredientEntries.stream()
            .map(val -> AttributeValue.builder().s(val).build())
            .collect(Collectors.toList()))
         .build());

      updatedItem.put("steps", AttributeValue.builder()
         .l(preparationSteps.stream()
            .map(step -> AttributeValue.builder().s(step).build())
            .collect(Collectors.toList()))
         .build());

      updatedItem.put("tags", AttributeValue.builder()
         .l(tags.stream()
            .map(tag -> AttributeValue.builder().s(tag).build())
            .collect(Collectors.toList()))
         .build());

      updatedItem.put("equipment", AttributeValue.builder()
         .l(equipment.stream()
            .map(eq -> AttributeValue.builder().s(eq).build())
            .collect(Collectors.toList()))
         .build());

   
      try {
          database.putItem(PutItemRequest.builder()
                .tableName("Recipes")
                .item(updatedItem)
                .build());
   
          // If the user changed the image
          if (selectedImageFile != null) {
              String s3Key = recipeBeingEdited.getUserID() + "/" + recipeBeingEdited.getRecipeDBId() + ".jpg";
              s3Client.putObject(PutObjectRequest.builder()
                      .bucket(S3_BUCKET_NAME)
                      .key(s3Key)
                      .build(),
                      RequestBody.fromFile(selectedImageFile));
          }
   
          System.out.println("Recipe updated successfully!");
   
          // Clear the local reference
          recipeBeingEdited = null;
          selectedImageFile = null;
   
          // Reload community recipes to reflect changes
          loadCommunityRecipes();
          buildRecipeCards();
   
          // Close the form
          addRecipePaneP2.setVisible(false);
          myRecipesPane.setVisible(true);
   
      } catch (Exception e) {
          e.printStackTrace();
          System.out.println("Error updating recipe.");
      }
   }
   
   public void deleteRecipeFromDBAndS3(Recipe recipe) {
      // 1) Check ownership
      String currentUser = "testUserID123";
      if (!recipe.getUserID().equals(currentUser)) {
          System.out.println("You do not own this recipe. Can't delete.");
          return;
      }
  
      try {
          // 2) Delete from DynamoDB
          Map<String, AttributeValue> key = new HashMap<>();
          key.put("Recipe", AttributeValue.builder().s(recipe.getRecipeDBId()).build());

  
          database.deleteItem(builder -> builder
              .tableName("Recipes")
              .key(key)
          );
          System.out.println("Deleted recipe from DynamoDB.");
  
          // 3) Delete from S3
          String s3Key = recipe.getUserID() + "/" + recipe.getRecipeDBId() + ".jpg";
          s3Client.deleteObject(builder -> builder
              .bucket(S3_BUCKET_NAME)
              .key(s3Key)
          );
          System.out.println("Deleted image from S3.");
  
          // 4) Refresh UI
          loadCommunityRecipes();
          buildRecipeCards();
  
      } catch (Exception e) {
          e.printStackTrace();
          System.out.println("Error deleting recipe from DB or S3.");
      }
  }

   private void updateSuggestedRecipes() {
      List<String> userInventory = getUserInventory();
      List<Recipe> allRecipes = communityRecipes;  // This should fetch all available recipes
      List<Recipe> suggestedRecipes = getSuggestedRecipes(userInventory, allRecipes);
      
      // Clear the HBox and add each recipe card
      suggestedRecipesBox.getChildren().clear();
      for (Recipe recipe : suggestedRecipes) {
            VBox recipeCard = createRecipeCard(recipe);
            suggestedRecipesBox.getChildren().add(recipeCard);
      }
   }

   public void showRecipeDetails(int recipeId, String name, Image image, Recipe recipe) {
      currentRecipe = recipe;
      currentRecipeCard = recipeWidgets.get(recipe.getID());
      displayStep = 0;

      myRecipesPane.setVisible(false);
      recipeDetailsPane.setVisible(true);
      recipeNameTXT.setText(name);

      // **Load Image from S3**
      String imageUrl = S3_BASE_URL + recipe.getUserID() + "-" + recipe.getRecipeDBId() + ".jpg";
      recipeDetailsImages.setImage(new Image(imageUrl, true));

      recipeServingsTXT.setText("Servings: " + recipe.getServings());
      recipePrepTimeTXT.setText("Prep Time: " + recipe.getPrepTime() + " Minutes");
      recipePassiveTimeTXT.setText("Passive Time: " + recipe.getPassiveTime() + " Minutes");
      recipeCookTimeTXT.setText("Cook Time: " + recipe.getCookTime() + " Minutes");

      int totalTime = recipe.getPrepTime() + recipe.getPassiveTime() + recipe.getCookTime(); // add the prep and cook times

      recipeTotalTimeTXT.setText("Total: " + totalTime + " Minutes");
      recipeComplexityTXT.setText("Complexity: " + getComplexityLabel(recipe.getComplexity()));

      recipeDetailDescription.setText(recipe.getDescription());

      List<String> ingredientStrings = recipe.getIngredients().stream()
      .map(ing -> ing.getName() + ": " + ing.getAmount() + " " + ing.getUnit())
      .collect(Collectors.toList());
      recipeIngredients.setItems(FXCollections.observableArrayList(ingredientStrings));
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
         
         String userId    = item.containsKey("UserId")   ? item.get("UserId").s()   : null;
         String recipeDBID  = item.containsKey("Recipe") ? item.get("Recipe").s() : null;
         String recipeName = item.get("name").s();
         String description = item.get("description").s();
          
         // For tags, equipment, and steps, collect into lists:
         List<String> tagsList = item.containsKey("tags")
            ? item.get("tags").l().stream().map(AttributeValue::s).collect(Collectors.toList())
            : new ArrayList<>();

         // For equipment:
         List<String> equipmentList = item.containsKey("equipment")
            ? item.get("equipment").l().stream().map(AttributeValue::s).collect(Collectors.toList())
            : new ArrayList<>();

         // For steps:
         List<String> stepsList = item.containsKey("steps")
            ? item.get("steps").l().stream().map(AttributeValue::s).collect(Collectors.toList())
            : new ArrayList<>();

         // For ingredients, we want to map each string into an Ingredient:
         List<Ingredient> ingredientsList;
         if (item.containsKey("ingredients")) {
            List<String> ingStrings = item.get("ingredients").l().stream()
                  .map(AttributeValue::s)
                  .collect(Collectors.toList());
            ingredientsList = new ArrayList<>();
            for (String ingStr : ingStrings) {
               String[] parts = ingStr.split(":");
               if (parts.length == 2) {
                  String ingName = parts[0].trim();
                  String[] amtParts = parts[1].trim().split(" ", 2);
                  String amount = amtParts.length >= 1 ? amtParts[0].trim() : "";
                  String unit = amtParts.length == 2 ? amtParts[1].trim() : "";
                  ingredientsList.add(new Ingredient(ingName, amount, unit));
               } else {
                  ingredientsList.add(new Ingredient(ingStr, "", ""));
               }
            }
         } else {
            ingredientsList = new ArrayList<>();
         }

         Recipe recipe = new Recipe(
            -1,  // Dummy ID for community recipes
            recipeName,
            item.getOrDefault("category", AttributeValue.builder().s("Uncategorized").build()).s(),
            "Community",
            description,
            Integer.parseInt(item.getOrDefault("prepTime", AttributeValue.builder().n("0").build()).n()),
            Integer.parseInt(item.getOrDefault("passiveTime", AttributeValue.builder().n("0").build()).n()),
            Integer.parseInt(item.getOrDefault("cookTime", AttributeValue.builder().n("0").build()).n()),
            1,  // Default complexity
            Integer.parseInt(item.getOrDefault("servings", AttributeValue.builder().n("1").build()).n()),
            tagsList,
            (ArrayList<Ingredient>) ingredientsList,
            equipmentList,
            stepsList
         );


         recipe.setUserID(userId);
         recipe.setRecipeDBId(recipeDBID);

         allRecipes.add(recipe);
      }
      return allRecipes;
  }

   private String capitalizeWords(String input) {
      if (input == null || input.isEmpty()) {
         return input;
      }
      
      String[] words = input.split("\\s+");
      StringBuilder sb = new StringBuilder();
      
      for (String word : words) {
         if (word.length() > 0) {
            // Capitalize the first letter, lowercase the rest
            sb.append(Character.toUpperCase(word.charAt(0)))
               .append(word.substring(1).toLowerCase())
               .append(" ");
         }
      }
      
      // Trim any trailing space
      return sb.toString().trim();
   }

  // =======================================================
   // Recipe Filters: 
   // Handles reicpe filtering options
   // =======================================================

   private void populateFilterOptions() {
      Set<String> ingredientSet = new HashSet<>();
      Set<String> tagSet = new HashSet<>();
      Set<String> categorySet = new HashSet<>();

      // Collect unique ingredients and tags from existing recipes
      for (Recipe recipe : recipeList) {
         for (Ingredient ing : recipe.getIngredients()) {
            ingredientSet.add(ing.getName());  // Use the Ingredient object's name
         }
         tagSet.addAll(recipe.getTags());
         categorySet.add(capitalizeWords(recipe.getCategory()));
      }

      // Ensure default options exist if the user has no recipes
      if (ingredientSet.isEmpty()) {
         ingredientSet.addAll(Arrays.asList("Flour", "Sugar", "Salt", "Butter", "Eggs", "Milk"));
      }
      if (tagSet.isEmpty()) {
         tagSet.addAll(Arrays.asList("Vegetarian", "Vegan", "Gluten-Free", "Dairy-Free", "Spicy", "Quick Meal"));
      }
      if (categorySet.isEmpty()) {
         categorySet.addAll(Arrays.asList("Breakfast", "Lunch", "Dinner", "Snack", "Dessert", "Other"));
      }


      // Set up available options
      availableIngredients = ingredientSet;
      availableTags = tagSet;

      // Populate the category filter with options
      categoryDropDown.getItems().clear();
      categoryDropDown.getItems().add("All Categories"); // Default option
      categoryDropDown.getItems().addAll(categorySet);
      categoryDropDown.setValue("All Categories"); // Set the default selection

      selectedIngredients.clear();
      selectedTags.clear();
   }

   @FXML
   private void clearAllFilters() {
      selectedIngredients.clear();
      selectedTags.clear();
      categoryDropDown.setValue("All Categories"); // Reset to default
      sortBy.setValue("A-Z");
      filterRecipes(); // Refresh recipe list
   }

   private Set<String> showMultiSelectDialog(String title, Set<String> availableOptions, Set<String> selectedOptions) {
      Stage dialogStage = new Stage();
      dialogStage.initModality(Modality.APPLICATION_MODAL);
      dialogStage.setTitle(title);

      VBox vbox = new VBox(10);
      vbox.setPadding(new Insets(10));

      // Search Bar
      TextField searchField = new TextField();
      searchField.setPromptText("Search...");

      // ListView with checkboxes
      ListView<CheckBox> listView = new ListView<>();
      ObservableList<CheckBox> checkBoxes = FXCollections.observableArrayList();

      // Populate checkboxes
      for (String option : availableOptions) {
         CheckBox checkBox = new CheckBox(option);
         checkBox.setSelected(selectedOptions.contains(option));
         checkBoxes.add(checkBox);
      }

      listView.setItems(checkBoxes);

      // Search functionality
      searchField.textProperty().addListener((obs, oldText, newText) -> {
         listView.setItems(checkBoxes.filtered(cb -> cb.getText().toLowerCase().contains(newText.toLowerCase())));
      });

      // Buttons
      Button applyButton = new Button("Apply");
      Button cancelButton = new Button("Cancel");

      applyButton.setOnAction(event -> {
         selectedOptions.clear();
         for (CheckBox checkBox : checkBoxes) {
               if (checkBox.isSelected()) {
                  selectedOptions.add(checkBox.getText());
               }
         }
         dialogStage.close();
      });

      cancelButton.setOnAction(event -> dialogStage.close());

      HBox buttonBox = new HBox(10, applyButton, cancelButton);
      buttonBox.setAlignment(Pos.CENTER_RIGHT);

      vbox.getChildren().addAll(searchField, listView, buttonBox);
      Scene scene = new Scene(vbox, 300, 400);
      dialogStage.setScene(scene);
      dialogStage.showAndWait();

      return selectedOptions;
   }

   private void filterRecipes() {
      applyFiltersAndSort();
      buildRecipeCards();
   }

   private void setupMultiSelectFilters() {
      ingredientFilter.setOnAction(event -> {
         selectedIngredients = showMultiSelectDialog("Select Ingredients", availableIngredients, selectedIngredients);
         filterRecipes();
      });
   
      tagsFilter.setOnAction(event -> {
         selectedTags = showMultiSelectDialog("Select Tags", availableTags, selectedTags);
         filterRecipes();
      });
   
      resetFilters.setOnAction(event -> clearAllFilters());
      categoryDropDown.setOnAction(event -> filterRecipes());
   }

   /** 
    * Filters and sorts all community recipes in memory. 
    * The final result is stored in filteredRecipes, 
    * which can then be used by buildRecipeCards() to update the UI.
    */
   private void applyFiltersAndSort() {
      // 1) copy the full list
      List<Recipe> result = new ArrayList<>(communityRecipes);

      // 2) Category filter
      String selectedCat = categoryDropDown.getValue();
      if (selectedCat != null && !selectedCat.equalsIgnoreCase("All Categories")) {
         result.removeIf(r -> !r.getCategory().equalsIgnoreCase(selectedCat));
      }

      // 3) Search filter
      String query = searchBar.getText() == null ? "" : searchBar.getText().trim().toLowerCase();
      if (!query.isEmpty()) {
         result.removeIf(r -> {
            // example: search in name, description, tags
            if (r.getName().toLowerCase().contains(query)) return false;
            if (r.getDescription().toLowerCase().contains(query)) return false;
            for (String tag : r.getTags()) {
                  if (tag.toLowerCase().contains(query)) return false;
            }
            return true;
         });
      }

      // 4) Sorting
      String sort = sortBy.getValue();
      if (sort != null) {
         switch (sort) {
            case "A-Z":
                  result.sort(Comparator.comparing(r -> r.getName().toLowerCase()));
                  break;
            case "Z-A":
                  result.sort((r1, r2) -> r2.getName().compareToIgnoreCase(r1.getName()));
                  break;
            case "Prep Time":
                  result.sort(Comparator.comparingInt(Recipe::getPrepTime));
                  break;
            case "Cook Time":
                  result.sort(Comparator.comparingInt(Recipe::getCookTime));
                  break;
            // add complexity / other sorts as needed
         }
      }

      this.filteredRecipes = result;
   }

}