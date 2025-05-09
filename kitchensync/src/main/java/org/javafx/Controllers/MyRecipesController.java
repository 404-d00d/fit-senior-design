package org.javafx.Controllers;

// Java Imports
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.javafx.Controllers.Ingredient;
import org.javafx.Main.Main;
import org.javafx.Recipe.Recipe;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import javafx.animation.Animation;
import javafx.animation.ScaleTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;


// =================================================================================
//  MyRecipesController handles user made recipes, navigation, and UI interactions
// =================================================================================

public class MyRecipesController {

   // ==================
   // FXML UI COMPONENTS
   // ==================

   // Navigation Buttons
   @FXML private Button menuButton, inventoryButton, myRecipesButton, inboxButton, browseRecipesButton, 
                     profileButton, settingsButton, myListsButton, mealPlannerButton, userDashboardButton;

   // Recipe Managment Buttons
   @FXML private Button addRecipeButton, closeRecipeButton, addTagButton, addIngredientButton, imageSelectButton, 
                     saveButton, addStepButton, addEquipmentButton, prevStepButton, nextStepButton;
   
   // Popup & Step Navigation 
   @FXML private Button closeP1Button, closeP2Button, backButton, nextButton, cookItButton, closeRecipeDetailsButton,
                     prevStep, nextStep, addCollectionButton, recipeReviewsButton, recipeNotesButton;

   // Filter Buttons
   @FXML private Button ingredientFilter, tagsFilter, resetFilters;

   // UI Containers & Layouts
   @FXML private VBox menuPane, collectionsButtons;
   @FXML private Pane myRecipesPane, myRecipeMainPane, addRecipePaneP1, addRecipePaneP2, 
                  recipeDetailsPane, recipeCookingPane;
   @FXML private FlowPane recipeFlowPane, chipPreview, recipeTagFlowPane;
   @FXML private ComboBox<String> categoryFilter;

   // Recipe Form Inputs
   @FXML private TextField recipeName, recipeTag, ingredientEntry, amountEntry, equipmentEntry;
   @FXML private TextField recipeETAPassive, recipeETA, recipeETAPrep, recipeYield;
   @FXML private TextArea prepStepField, stepArea, recipeDetailDescription, recipeDescription, recipeNotesArea;
   @FXML private ComboBox<String> recipeCategory, recipeCollection, ingredientUnitEntry, sortBy;
   @FXML private HBox recipeImagesHbox;

   // Recipe Detail & Cooking Display
   @FXML private Text recipePrepTimeTXT, recipePassiveTimeTXT, recipeCookTimeTXT, recipeTotalTimeTXT, 
                   recipeComplexityTXT, recipeServingsTXT, stepIndex, stepOfTXT, noRecipesTXT, recipeNameTXT, 
                   recipeCookingNameTXT;
   @FXML private ListView<String> ingredientsArea, specialEquipmentTXTArea;
   @FXML private ImageView recipeImages, imagePreview, recipeDetailsImages;
   @FXML private TextField searchBar;
   @FXML private Text localRatingStars;
   @FXML private Text communityRatingStars;  

   // Table Views for Ingredients & Equipment
   @FXML private TableView<Ingredient> ingredientTable;
   @FXML private TableView<Ingredient> recipeIngredients;
   @FXML private TableView<String> equipmentTable;
   @FXML private TableColumn<String, String> equipmentList;
   @FXML private TableColumn<Ingredient, String> ingredientList, amountList;
   @FXML private TableColumn<Ingredient, String> ingredientListView, amountListView;

   // Review Pane
   @FXML private Pane recipeReviews;
   @FXML private ScrollPane reviewPane;
   @FXML private Text recipeReviewName, localRatingStarsReviews, communityRatingStarsReview;
   @FXML private ComboBox<String> reviewType;
   @FXML private Button postCommentButton, postCommentCloseButton;

   // Tutorial
   @FXML private javafx.scene.canvas.Canvas maskCanvas;
   @FXML private AnchorPane tutorialOverlay;
   @FXML private Text tutorialText;
   @FXML private Button nextTutorialButton, skipTutorialButton;
   @FXML private Rectangle highlightBox;
   @FXML private VBox overlayText;
   private int tutorialStep = 0;
   private final String[] tutorialMessages = {
      // Sidebar Navigation
      "Welcome to your MyRecipes! Click 'Next' to continue.",
      "Click 'Menu' to navigate to another screen",

      // Recipe List & Search
      "This is your personal recipe collection — all the meals you've saved live here.",
      "Use the search bar to find a specific recipe quickly by name or tag.",
      "You can filter recipes by category, cook time, or available ingredients using the filters.",

      // Recipe Interaction
      "Click a recipe card to view full details, ingredients, and steps.",
      "Right click a recipe card to show more options such as edit, favorite, or delete",

      // Cooking Features
      "When viewing a recipe, which is as simple as clicking on a recipe, press 'Cook' to enter cooking mode.",
      "You can add images to specific steps to help you follow complex instructions.",

      // Organization & Tags
      "Tags help categorize your recipes. Try tagging by meal type (like breakfast) or dietary need (like vegetarian).",
      "Recipes can also be sorted by prep time or serving size — just use the sort dropdown.",

      // Done
      "You're all set to manage and cook your favorite meals. Enjoy using My Recipes!"
  };


   // ==============================
   // DATA STORAGE & CONSTANTS
   // ==============================

   // Data Storage & Processing
   private ObservableList<String> tags = FXCollections.observableArrayList();
   private ObservableList<Ingredient> ingredients = FXCollections.observableArrayList();
   private ObservableList<String> equipment = FXCollections.observableArrayList();
   private List<String> preparationSteps = new ArrayList<>();
   private ObservableList<Recipe> recipeList = FXCollections.observableArrayList();
   private Map<String, List<Integer>> recipeCollections = new HashMap<>();
   private Map<Integer, VBox> recipeWidgets = new HashMap<>();
   private ObservableList<Image> recipeThumbnails = FXCollections.observableArrayList();
   private Map<Integer, String> stepImageFileMap = new HashMap<>();
   private Map<Integer, Image> stepImageMap = new HashMap<>();

   private static final String RECIPES_FILE_PATH = "recipes.json";
   private static final String LOCAL_REVIEWS_FILE_PATH = "reviews.json";
   private static final String COLLECTIONS_FILE_PATH = "collections.json";

   private File selectedImageFile;
   private Image selectedImage;
   private int currentStep = 0;
   private int displayStep = 0;
   private String currentCollection = "All Recipes";

   private Recipe currentRecipe;
   private VBox currentRecipeCard;

   // Selected filters for recipesloadFlavorMatrix
   private Set<String> selectedIngredients = new HashSet<>();
   private Set<String> selectedTags = new HashSet<>();
   private Set<String> availableIngredients = new HashSet<>();
   private Set<String> availableTags = new HashSet<>();

   // Ingredient Suggestion and Substitution Items
   private IngredientSuggestionAndSubstitutions suggestionService;
   private UserPreferences userPrefs;

   // ====================================================================
   // Initialization & UI Setup
   // ====================================================================

   @FXML
   private void initialize() {

      maskCanvas = new Canvas(tutorialOverlay.getWidth(), tutorialOverlay.getHeight());
      if (!tutorialOverlay.getChildren().contains(maskCanvas)) {
         tutorialOverlay.getChildren().add(maskCanvas);
         tutorialOverlay.setVisible(true);
         overlayText.setVisible(true);
      }
     
      maskCanvas.setMouseTransparent(true);

      if (!TutorialManager.isCompleted("MyRecipes")) {
         startTutorial();
      }

      // Set tutorial button actions
      nextTutorialButton.setOnAction(event -> showNextTutorialStep());
      skipTutorialButton.setOnAction(event -> endTutorial());

      initializeCollections();   // Load collections and ensure default ones exist
      loadRecipes();            // Load recipes from JSON
      configureDropdowns();      // Set up dropdown values
      configureIngredientTable(); // Configure ingredient table
      configureEquipmentTable();  // Configure equipment table
      setSortingHandlers();      // Set up sorting for recipes
      setNavigationButtonHandlers();   // Assign button actions for navigation
      setHoverEffects();         // Apply hover effects to UI elements
      setupUIEventHandlers();    // Configure buttons and step navigation
      populateFilterOptions(); // Ensure filters get populated
      setupMultiSelectFilters(); // Set up Filter Buttons

      String flavorMatrixPath = "flavorMatrix.json";
      String substitutionsPath = "substitutions.json";

      // Load data
      Map<String, List<String>> flavorMatrix = loadFlavorMatrix(flavorMatrixPath);
      Map<String, List<SubstitutionOption>> knownSubsraw = loadSubstitutions(substitutionsPath);
      Map<String, List<SubstitutionOption>> knownSubs = IngredientSuggestionAndSubstitutions.normalizeSubstitutionKeys(knownSubsraw);

      // Create the service
      suggestionService = new IngredientSuggestionAndSubstitutions(flavorMatrix, knownSubs);

      // User preferences from a user profile, load them; for now, just an example
      userPrefs = new UserPreferences(
         Set.of("shellfish"),  // allergies
         Set.of("cilantro"),   // dislikes
         false,                // vegan?
         false                 // vegetarian?
      );

      recipeNotesButton.setOnAction(event -> openRecipeNotesPopup());
      recipeReviewsButton.setOnAction(event -> openReviewPage());

      reviewType.getItems().addAll( "All", "Community", "Local");
      postCommentButton.setOnAction(event -> openReviewPopup()); 

      searchBar.textProperty().addListener((observable, oldValue, newValue) -> filterRecipesBySearch(newValue));

      // Attach event listeners for filtering
      categoryFilter.setOnAction(event -> filterRecipes());
      sortBy.setOnAction(event -> filterRecipes());
      postCommentCloseButton.setOnAction(event -> closeReviewWindow());

      // Initialize the first step
      preparationSteps.add("");
      currentStep = 0;
      displayStep = 0;
      updateStepView();

      cookItButton.setOnAction(event -> {

         if (!recipeThumbnails.isEmpty() && recipeThumbnails.size() > 1) {
            recipeImages.setImage(recipeThumbnails.get(1));

         } else {
            recipeImages.setImage(recipeThumbnails.get(0));
         }

         recipeCookingPane.setVisible(true);
         recipeDetailsPane.setVisible(false);
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

      closeRecipeDetailsButton.setOnAction(event -> {
         try {

            currentRecipeCard.setOnMouseEntered(null);
            currentRecipeCard.setOnMouseExited(null);
            currentRecipeCard.setOnMouseMoved(null);            

            applyHoverEffect(currentRecipeCard, currentRecipe);

            myRecipesPane.setVisible(true);
            recipeDetailsPane.setVisible(false);
            
         } catch (Exception e) {
            e.printStackTrace();
         }
      });

      closeRecipeButton.setOnAction(event -> {
         try {
            saveRecipeNotes();
            myRecipesPane.setVisible(true);
            recipeCookingPane.setVisible(false);
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
            clearForms();
            myRecipesPane.setVisible(false);
            addRecipePaneP1.setVisible(true);
         } catch (Exception e) {
            e.printStackTrace();
         }
      });
   }

   private void showNextTutorialStep() {
      if (tutorialStep < tutorialMessages.length) {
          tutorialText.setText(tutorialMessages[tutorialStep]);
  
          switch (tutorialStep) {
              // Sidebar Menu Buttons
              case 1: moveHighlight(menuButton); tutorialOverlay.setStyle("-fx-background-color: transparent;"); break;
              case 2: moveHighlight(collectionsButtons); break;
              case 3: moveHighlight(searchBar); break;
              case 4: moveHighlight(ingredientFilter); break;
              case 5: moveHighlight(recipeFlowPane); break;
              case 6: moveHighlight(recipeFlowPane); break;
              case 7: moveHighlight(recipeFlowPane); break;
              case 8: moveHighlight(tagsFilter); break;
              case 9: moveHighlight(sortBy); break;
              case 10: highlightBox.setVisible(false); break;
  
              default:
                  highlightBox.setVisible(false); // Hide highlight after last step
          }
  
          tutorialStep++;
      } else {
          endTutorial();
          TutorialManager.markCompleted("MyRecipes");
      }
  }
  
  private void moveHighlight(Node target) {
      if (target == null) return;

      // Ensure highlight box is on top and visible
      highlightBox.setVisible(true);
      highlightBox.toFront();

      // Get bounds of the target in scene coordinates
      Bounds boundsInScene = target.localToScene(target.getBoundsInLocal());

      // Convert to local coordinates relative to the overlay
      Point2D topLeft = tutorialOverlay.sceneToLocal(boundsInScene.getMinX(), boundsInScene.getMinY());

      double x = topLeft.getX();
      double y = topLeft.getY();
      double width = target.getBoundsInLocal().getWidth();
      double height = target.getBoundsInLocal().getHeight();

      // Adjust for potential scale or padding (OPTIONAL - tweak if it's slightly off)
      double padding = 6.0; // optional extra padding for visual emphasis
      x -= padding;
      y -= padding;
      width += 2 * padding;
      height += 2 * padding;

      // Draw dark mask around this rectangle
      drawSpotlightMask(x, y, width, height);

      // Move and size the highlight box
      highlightBox.setLayoutX(x);
      highlightBox.setLayoutY(y);
      highlightBox.setWidth(width);
      highlightBox.setHeight(height);

      // Optional: Add a drop shadow or border glow to make it really pop
      highlightBox.setStyle("-fx-stroke: #FF7F11; -fx-stroke-width: 3; -fx-fill: transparent;");
   }


   private void drawSpotlightMask(double x, double y, double width, double height) {
      var gc = maskCanvas.getGraphicsContext2D();
      // Force canvas to match overlay
      maskCanvas.setWidth(tutorialOverlay.getWidth());
      maskCanvas.setHeight(tutorialOverlay.getHeight());

      tutorialOverlay.setPickOnBounds(false);
      maskCanvas.setMouseTransparent(true);

  
      double canvasWidth = maskCanvas.getWidth();
      double canvasHeight = maskCanvas.getHeight();
  
      // Clear everything
      gc.clearRect(0, 0, canvasWidth, canvasHeight);
  
      // Set semi-transparent overlay color
      gc.setFill(javafx.scene.paint.Color.rgb(0, 0, 0, 0.6));
  
      // Draw top mask
      gc.fillRect(0, 0, canvasWidth, y);
  
      // Draw left mask
      gc.fillRect(0, y, x, height);
  
      // Draw right mask
      gc.fillRect(x + width, y, canvasWidth - (x + width), height);
  
      // Draw bottom mask
      gc.fillRect(0, y + height, canvasWidth, canvasHeight - (y + height));
  }
  
  

   private void clearMask() {
      var gc = maskCanvas.getGraphicsContext2D();
      gc.clearRect(0, 0, maskCanvas.getWidth(), maskCanvas.getHeight());
   }

   private void startTutorial() {
      tutorialStep = 0;
  
      // Set visible first
      tutorialOverlay.setVisible(true);
      overlayText.setVisible(true);
      maskCanvas.setVisible(true);
  
      // Clear any old content and force redraw
      clearMask();
      showNextTutorialStep();
  
      // Force elements on top
      tutorialText.toFront();
      nextTutorialButton.toFront();
      skipTutorialButton.toFront();
  
      startHighlightPulse();
  }
  
  
  private void endTutorial() {
      tutorialOverlay.setVisible(false);
      overlayText.setVisible(false);
      maskCanvas.setVisible(false);
      clearMask();
      TutorialManager.markCompleted("MyRecipes");
  }
  

   private void startHighlightPulse() {
      if (highlightBox == null) return;

      ScaleTransition pulse = new ScaleTransition(Duration.seconds(1), highlightBox);
      pulse.setFromX(1.0);
      pulse.setToX(1.05);
      pulse.setFromY(1.0);
      pulse.setToY(1.05);
      pulse.setCycleCount(Animation.INDEFINITE);
      pulse.setAutoReverse(true);
      pulse.play();
   }



   private void initializeCollections() {
      loadCollectionsFromJson(); 
      recipeCollections.putIfAbsent("All Recipes", new ArrayList<>());
      recipeCollections.putIfAbsent("Favorites", new ArrayList<>());
      saveCollectionsToJson(); 
      loadCollectionButtons();
   }

   private void loadRecipes() {
      recipeList.addAll(loadRecipesFromJson()); 
      loadRecipesCards(); 
      recipeCollection.getItems().clear();
      recipeCollection.getItems().setAll(recipeCollections.keySet());
   }

   private void configureDropdowns() {
      recipeCategory.getItems().addAll("Dinner", "Lunch", "Breakfast", "Snack", "Other");
      ingredientUnitEntry.getItems().addAll("g", "kg", "ml", "l", "tsp", "tbsp", "cup", "oz", "lb", "pinch", "dash");
      sortBy.getItems().addAll("A-Z", "Z-A", "Complexity", "Prep Time", "Cook Time");
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

   private void setNavigationButtonHandlers() {
      inventoryButton.setOnAction(event -> navigateToScreen("Inventory"));
      myRecipesButton.setOnAction(event -> navigateToScreen("MyRecipes"));
      inboxButton.setOnAction(event -> navigateToScreen("Inbox"));
      browseRecipesButton.setOnAction(event -> navigateToScreen("CommunityRecipes"));
      profileButton.setOnAction(event -> navigateToScreen("Profile"));
      settingsButton.setOnAction(event -> navigateToScreen("Settings"));
      myListsButton.setOnAction(event -> navigateToScreen("MyLists"));
      userDashboardButton.setOnAction(event -> navigateToScreen("UserDashboard"));
      mealPlannerButton.setOnAction(event -> navigateToScreen("MealPlanner"));
   }

   private void navigateToScreen(String screen) {
         try {
            switch (screen) {
               case "Inventory": Main.showInventoryScreen(); break;
               case "MyRecipes": Main.showMyRecipesScreen(); break;
               case "Inbox": Main.showInboxScreen(); break;
               case "CommunityRecipes": Main.showCommunityRecipesScreen(); break;
               case "Profile": Main.showUserProfileScreen(); break;
               case "Settings": Main.showUserSettingsScreen(); break;
               case "MyLists": Main.showMyListsScreen(); break;
               case "UserDashboard": Main.showUserDashboardScreen(); break;
               case "MealPlanner" : Main.showMealPlannerScreen(); break;
               default: System.out.println("Screen not implemented: " + screen);
            }
         } catch (Exception e) {
            e.printStackTrace();
         }
   }

   private void setHoverEffects() {
      setHoverEffect(inventoryButton);
      setHoverEffect(myRecipesButton);
      setHoverEffect(inboxButton);
      setHoverEffect(browseRecipesButton);
      setHoverEffect(profileButton);
      setHoverEffect(settingsButton);
      setHoverEffect(myListsButton);
      setHoverEffect(userDashboardButton);
      setHoverEffect(mealPlannerButton);
   }

   private void setupUIEventHandlers() {
      addTagButton.setOnAction(event -> addTag());
      addIngredientButton.setOnAction(event -> addIngredient());
      imageSelectButton.setOnAction(event -> SelectImage());
      saveButton.setOnAction(event -> saveRecipe());
      addStepButton.setOnAction(event -> addStep());
      prevStepButton.setOnAction(event -> navigateStep(-1));
      nextStepButton.setOnAction(event -> navigateStep(1));
      addEquipmentButton.setOnAction(event -> addEquipment());
      prevStep.setOnAction(event -> detailsSteps(-1));
      nextStep.setOnAction(event -> detailsSteps(1));
      addCollectionButton.setOnAction(event -> openAddCollectionForm());
   }

   private void setSortingHandlers() {
      sortBy.setOnAction(event -> handleSortBySelection());
   }

   // ===================================================
   // Navigation & UI Handling
   // Handles menu interactions and screen transitions
   // ===================================================

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

   // =======================================================
   // Recipe Management
   // Handles adding, editing, deleting, and saving recipes
   // =======================================================

   private String getStarsString(int rating) {
      // rating from 0 to 5, for instance
      // Return something like "★★★★☆" or "★★☆☆☆"
      StringBuilder stars = new StringBuilder();
      for (int i = 1; i <= 5; i++) {
          if (i <= rating) {
              stars.append("★");
          } else {
              stars.append("☆");
          }
      }
      return stars.toString();
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

      if (currentStep == 0 && preparationSteps.size() == 0) {
         preparationSteps.add(currentStep, prepStepField.getText().trim());
      }

      // Save current step text before adding a new step
      if (currentStep >= 0 && currentStep < preparationSteps.size()) {
         preparationSteps.set(currentStep, prepStepField.getText().trim());
         System.out.println(preparationSteps.get(currentStep));
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
         recipeImages.setImage(recipeThumbnails.get(0));
      } else {
         stepArea.setText(preparationSteps.get(displayStep));
         stepOfTXT.setText("Step " + (displayStep + 1) + " of " + preparationSteps.size());

         if (stepImageMap.containsKey(displayStep)) {
            recipeImages.setImage(stepImageMap.get(displayStep));
        } else if (stepImageMap.containsKey(0)) {
            recipeImages.setImage(stepImageMap.get(0)); // fallback to main image
        }
      }

      updateThumbnailHighlight();
   }

   private void updateThumbnailHighlight() {
      for (int i = 0; i < recipeImagesHbox.getChildren().size(); i++) {
          ImageView iv = (ImageView) recipeImagesHbox.getChildren().get(i);
          if (i == displayStep + 1) {
              iv.setStyle("-fx-border-color: #FF7F11; -fx-border-width: 3px;");
          } else {
              iv.setStyle("-fx-border-color: transparent;");
          }
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

   private void saveRecipe() {
      if (!isFormValid(recipeName.getText(),
                       recipeCategory.getValue(),
                       recipeYield.getText(),
                       recipeDescription.getText(),
                       recipeETAPrep.getText(),
                       recipeETAPassive.getText(),
                       recipeETA.getText(),
                       new ArrayList<>(ingredients),
                       new ArrayList<>(preparationSteps))) {
          return;
      }
  
      int id = -1;
      Recipe existingRecipe = null;
      
      // Find existing recipe by name
      for (int i = 0; i < recipeList.size(); i++) {
          if (recipeList.get(i).getName().equalsIgnoreCase(recipeName.getText())) {
              id = i;
              existingRecipe = recipeList.get(i);
              break;
          }
      }
  
      // Build a new Recipe object with typed lists
      Recipe updatedRecipe = new Recipe(
          (id == -1 ? recipeList.size() : id), // new ID if not found
          recipeName.getText(),
          recipeCategory.getValue(),
          recipeCollection.getValue(),
          recipeDescription.getText(),
          Integer.parseInt(recipeETAPrep.getText()),
          Integer.parseInt(recipeETAPassive.getText()),
          Integer.parseInt(recipeETA.getText()),
          calculateRecipeComplexity(),
          Integer.parseInt(recipeYield.getText()),
          new ArrayList<>(tags),
          new ArrayList<>(ingredients),
          new ArrayList<>(equipment),
          new ArrayList<>(preparationSteps)
      );
  
      if (existingRecipe != null) {
          // Update the existing recipe in the list safely
          recipeList.set(id, updatedRecipe);
      } else {
          // If recipe is new, add it instead of trying to update an index that doesn't exist
          recipeList.add(updatedRecipe);
      }
  
      // Remove old recipe card if it exists
      if (recipeWidgets.containsKey(id)) {
          VBox oldRecipeCard = recipeWidgets.get(id);
          recipeFlowPane.getChildren().remove(oldRecipeCard);
          recipeWidgets.remove(id);
      }
  
      try {
         FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/javafx/Resources/FXMLs/RecipeCard.fxml"));
         VBox recipeCard = loader.load();
         RecipeCardController controller = loader.getController();

         // Save the image
         String imageName = recipeName.getText() + ".png";
         if (selectedImageFile != null) {
            copyImageToResources(selectedImageFile, imageName);
         }

         File imageFile = new File("src/main/resources/org/javafx/Resources/Recipe Images/" + imageName);
         Image image = imageFile.exists() ? new Image(imageFile.toURI().toString()) : null;

         controller.setRecipeData(updatedRecipe, image, this, "myrecipes");
         recipeFlowPane.getChildren().add(recipeCard);
         recipeWidgets.put(updatedRecipe.getID(), recipeCard);
         recipeCard.setUserData(controller);

         // Remove the old hover effect
         recipeCard.setOnMouseEntered(null);
         recipeCard.setOnMouseExited(null);
         recipeCard.setOnMouseMoved(null);
         
         applyHoverEffect(recipeCard, updatedRecipe);
         clearForms();
         updateTagView();
         updateStepView();
         loadRecipesCards();

         addRecipePaneP2.setVisible(false);
         myRecipesPane.setVisible(true);

         // Save the updated list to JSON
         saveRecipesToJson(recipeList);
  
      } catch (Exception e) {
          e.printStackTrace();
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
      recipeCollection.setValue(null);
      tags.clear();
      ingredients.clear();
      preparationSteps.clear();
      prepStepField.clear();
      recipeETA.clear();
      recipeETAPassive.clear();
      recipeETAPrep.clear();
      selectedImageFile = null;

      // Clear previous images
      recipeImagesHbox.getChildren().clear();
      recipeThumbnails.clear();

   }

   private boolean isFormValid(String name, String category, String servings,
                               String description, String prepTime, String passiveTime, String cookTime,
                               List<Ingredient> ingredientsList, List<String> stepsList) {

      if (name == null || name.isEmpty()) {
         showAlert("Error", "Missing Name", "Please enter a valid recipe name.");
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
      if (description == null || description.isEmpty()) {
         showAlert("Error", "Missing Description", "Please add a description.");
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
      // Check ingredient list
      if (ingredientsList == null || ingredientsList.isEmpty()) {
         showAlert("Error", "Missing Ingredients", "Please add at least one ingredient.");
         return false;
      }
      if (stepsList == null || stepsList.isEmpty()) {
         showAlert("Error", "Missing Steps", "Please add at least one step.");
         return false;
      }
      return true;
   }

   public void showRecipeDetails(int recipeId, String name, Image image, Recipe recipe) {
      currentRecipe = recipe;
      currentRecipeCard = recipeWidgets.get(recipe.getID());
      displayStep = 0;

      myRecipesPane.setVisible(false);
      recipeDetailsPane.setVisible(true);
      recipeNameTXT.setText(name);

      recipeImagesHbox.getChildren().clear();
      recipeThumbnails.clear();
      stepImageMap.clear();

      // 1) Generate or load suggestions if needed
      handleSuggestionsForRecipe(recipe);

      // Load main image
      File mainImageFile = new File("src/main/resources/org/javafx/Resources/Recipe Images/" + recipe.getName() + ".png");
      if (mainImageFile.exists()) {
         Image mainImage = new Image(mainImageFile.toURI().toString());
         recipeThumbnails.add(mainImage);
         stepImageMap.put(0, mainImage);
         stepImageFileMap.put(0, mainImageFile.getAbsolutePath());
         recipeImagesHbox.getChildren().add(createThumbnail(mainImage, mainImageFile.getAbsolutePath(), 0));
      }

      // Load step images
      for (int i = 1; ; i++) {
         File stepImageFile = new File("src/main/resources/org/javafx/Resources/Recipe Images/" + recipe.getName() + "_step" + i + ".png");
         if (!stepImageFile.exists()) break;
         Image stepImg = new Image(stepImageFile.toURI().toString());
         recipeThumbnails.add(stepImg);
         stepImageMap.put(i, stepImg);
         stepImageFileMap.put(i, stepImageFile.getAbsolutePath());
         recipeImagesHbox.getChildren().add(createThumbnail(stepImg, stepImageFile.getAbsolutePath(), i));
      }

      if (!recipeThumbnails.isEmpty()) {
         recipeDetailsImages.setImage(recipeThumbnails.get(0));
      } else {
         recipeDetailsImages.setImage(null);
      }

      localRatingStars.setText("Local Review: " + getStarsString(recipe.getLocalRating()));
      communityRatingStars.setText("Community Review: " + getStarsString(recipe.getCommunityRating()));
      recipeNotesArea.setText(recipe.getRecipeNotes());
      localRatingStarsReviews.setText("Local Review: " + getStarsString(recipe.getLocalRating()));
      communityRatingStarsReview.setText("Community Review: " + getStarsString(recipe.getCommunityRating()));
      recipeReviewName.setText(name);

      recipeDetailDescription.setStyle("-fx-font-size: 20px;");
      //recipeIngredients.setStyle("-fx-control-inner-background: #2E2E2E; -fx-font-size: 20px; -fx-text-fill: white; -fx-border-color: #FF7F11;");
      specialEquipmentTXTArea.setStyle("-fx-font-size: 20px;");
      ingredientsArea.setStyle("-fx-control-inner-background: #2E2E2E; -fx-font-size: 20px; -fx-text-fill: white; -fx-border-color: #FF7F11;");
      stepArea.setStyle("-fx-font-size: 20px;");

      recipeServingsTXT.setText("Servings: " + recipe.getServings());
      recipePrepTimeTXT.setText("Prep Time: " + recipe.getPrepTime() + " Minutes");
      recipePassiveTimeTXT.setText("Passive Time: " + recipe.getPassiveTime() + " Minutes");
      recipeCookTimeTXT.setText("Cook Time: " + recipe.getCookTime() + " Minutes");
      int totalTime = recipe.getPrepTime() + recipe.getPassiveTime() + recipe.getCookTime();
      recipeTotalTimeTXT.setText("Total: " + totalTime + " Minutes");
      recipeComplexityTXT.setText("Complexity: " + getComplexityLabel(recipe.getComplexity()));

      recipeDetailDescription.setText(recipe.getDescription());

      // Convert ingredients to strings for the ListView
      List<String> ingredientStrings = recipe.getIngredients().stream()
         .map(ing -> ing.getName() + ": " + ing.getAmount() + " " + ing.getUnit())
         .collect(Collectors.toList());
      

      // Populate ingredients Table
      ingredientListView.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getName()));
      amountListView.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getAmount() + " " + data.getValue().getUnit()));

      recipeIngredients.setRowFactory(tv -> {
         TableRow<Ingredient> row = new TableRow<>();
         Tooltip tooltip = new Tooltip();
     
         row.setOnMouseEntered(event -> {
            if (!row.isEmpty()) {
                Ingredient ingredient = row.getItem();
                List<SubstitutionOption> subs = suggestionService.getSubstitutions(ingredient.getName(), userPrefs, "general");
    
                if (!subs.isEmpty()) {
                    StringBuilder tooltipText = new StringBuilder("Substitutions for " + ingredient.getName() + ":\n\n");
                    for (SubstitutionOption option : subs) {
                        tooltipText.append("- ").append(option.getIngredientName())
                                   .append("\n  Notes: ").append(option.getNotes()).append("\n\n");
                    }
                    tooltip.setText(tooltipText.toString());
    
                    // Show instantly at mouse location
                    tooltip.show(row, event.getScreenX(), event.getScreenY() + 10);
                }
            }
        });
    
        row.setOnMouseExited(event -> tooltip.hide());
    
        row.setOnMouseMoved(event -> {
            if (tooltip.isShowing()) {
                tooltip.setX(event.getScreenX());
                tooltip.setY(event.getScreenY() + 10);
            }
        });
    
        return row;
    });
     
      
      ingredients.clear();
      ingredients.addAll(recipe.getIngredients());
      
      recipeIngredients.setItems(ingredients);
      recipeIngredients.setEditable(false);
      ingredientListView.setCellFactory(TextFieldTableCell.forTableColumn());
      amountListView.setCellFactory(TextFieldTableCell.forTableColumn());

      recipeIngredients.setFixedCellSize(40); // Adjust row height if needed
      recipeIngredients.prefHeightProperty().bind(
         recipeIngredients.fixedCellSizeProperty().multiply(Bindings.size(recipeIngredients.getItems()).add(2)) // +1 for header
      );

      specialEquipmentTXTArea.setItems(FXCollections.observableArrayList(recipe.getEquipment()));

      recipeTagFlowPane.getChildren().clear();
      for (String tag : recipe.getTags()) {
         Label tagLabel = new Label(tag);
         tagLabel.setStyle("-fx-background-color: #555555; -fx-text-fill: white; -fx-padding: 5 10; -fx-border-radius: 10; -fx-background-radius: 10; -fx-font-size: 16px; -fx-border-color: #FF7F11;");
         recipeTagFlowPane.getChildren().add(tagLabel);
      }

      recipeCookingNameTXT.setText(name);

      // Also for the cooking pane
      ingredientsArea.setCellFactory(listView -> new ListCell<>() {
         private final CheckBox checkBox = new CheckBox();
         private final Label nameLabel = new Label();
         private final Label amountLabel = new Label();
         private final HBox hbox = new HBox(10);
         private final Tooltip tooltip = new Tooltip();
     
         {
             hbox.setAlignment(Pos.CENTER_LEFT);
             hbox.setPadding(new Insets(2));
             HBox spacer = new HBox();
             spacer.setMinWidth(20);
             spacer.setPrefWidth(20);
             HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
             hbox.getChildren().addAll(checkBox, nameLabel, spacer, amountLabel);
     
             // Tooltip behavior
             hbox.setOnMouseExited(event -> tooltip.hide());
     
             hbox.setOnMouseMoved(event -> {
                 if (tooltip.isShowing()) {
                     tooltip.setX(event.getScreenX());
                     tooltip.setY(event.getScreenY() + 10);
                 }
             });
         }
     
         @Override
         protected void updateItem(String item, boolean empty) {
             super.updateItem(item, empty);
             if (empty || item == null) {
                 setGraphic(null);
                 tooltip.hide();
                 return;
             }
     
             String[] parts = item.split(":");
             String name = parts[0].trim();
             String amountUnit = parts.length > 1 ? parts[1].trim() : "";
     
             nameLabel.setText(name);
             nameLabel.setWrapText(true);
             nameLabel.setMaxWidth(500);
             nameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");
     
             amountLabel.setText(amountUnit);
             amountLabel.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");
             checkBox.setSelected(false);
     
             // Check for substitutions
             List<SubstitutionOption> subs = suggestionService.getSubstitutions(name, userPrefs, "general");
             if (!subs.isEmpty()) {
                 StringBuilder tooltipText = new StringBuilder("Substitutes:\n");
                 for (SubstitutionOption sub : subs) {
                     tooltipText.append("- ").append(sub.getIngredientName())
                                .append("\n  Notes: ").append(sub.getNotes()).append("\n");
                 }
     
                 tooltip.setText(tooltipText.toString());
     
                 hbox.setOnMouseEntered(event -> {
                     tooltip.show(hbox, event.getScreenX(), event.getScreenY() + 10);
                 });
     
             } else {
                 hbox.setOnMouseEntered(null);
                 tooltip.hide();
             }
     
             setGraphic(hbox);
         }
     });

     ingredientsArea.setItems(FXCollections.observableArrayList(ingredientStrings));

      preparationSteps = new ArrayList<>(recipe.getSteps());
      stepOfTXT.setText("Step 1 of " + preparationSteps.size());
      stepArea.setText(preparationSteps.isEmpty() ? "" : preparationSteps.get(0));
   }

   // Method to open the edit form with the recipe's current details
   public void openEditRecipe(Recipe recipe) {
      // Logic to open edit pane and load recipe details by ID

      myRecipesPane.setVisible(false);
      addRecipePaneP1.setVisible(true);

      // Set Values to be edited
      recipeName.setText(recipe.getName());
      recipeCategory.setValue(recipe.getCategory());
      recipeCollection.setValue(recipe.getCollection());
      recipeYield.setText(Integer.toString(recipe.getServings()));
      recipeDescription.setText(recipe.getDescription());
      recipeETAPrep.setText(Integer.toString(recipe.getPrepTime()));
      recipeETAPassive.setText(Integer.toString(recipe.getPassiveTime()));
      recipeETA.setText(Integer.toString(recipe.getCookTime()));

      // Re-populate step image maps and HBox
      stepImageMap.clear();
      stepImageFileMap.clear();
      recipeImagesHbox.getChildren().clear();

      // Main image
      File mainImageFile = new File("src/main/resources/org/javafx/Resources/Recipe Images/" + recipe.getName() + ".png");
      if (mainImageFile.exists()) {
         Image mainImage = new Image(mainImageFile.toURI().toString());
         stepImageMap.put(0, mainImage);
         stepImageFileMap.put(0, mainImageFile.getAbsolutePath());

         imagePreview.setImage(mainImage);
      }

      // Step images
      for (int i = 1; ; i++) {
         File stepImageFile = new File("src/main/resources/org/javafx/Resources/Recipe Images/" + recipe.getName() + "_step" + i + ".png");
         if (!stepImageFile.exists()) break;

         Image stepImage = new Image(stepImageFile.toURI().toString());
         stepImageMap.put(i, stepImage);
         stepImageFileMap.put(i, stepImageFile.getAbsolutePath());
      }

      // Display all thumbnails
      refreshImageThumbnails();

      tags.clear();
      tags.addAll(recipe.getTags());
      updateTagView();

      equipment.clear();
      equipment.addAll(recipe.getEquipment()); 

      ingredients.clear();
      for (Ingredient ing : recipe.getIngredients()) {
         ingredients.add(ing);
      }

      preparationSteps.clear();
      preparationSteps.addAll(recipe.getSteps());
      currentStep = 0;
      updateStepView();
   }

   public void deleteRecipe(Recipe recipe) {
      System.out.println("Deleting recipe: " + recipe.getName());
  
      // Remove the recipe from the JSON list
      recipeList.removeIf(r -> r.getID() == recipe.getID());
      saveRecipesToJson(recipeList);
  
      // Remove the recipe card from the UI
      VBox recipeCard = recipeWidgets.get(recipe.getID());
      if (recipeCard != null) {
          recipeFlowPane.getChildren().remove(recipeCard);
          recipeWidgets.remove(recipe.getID());
      }
  
      // Remove the corresponding image file
      deleteRecipeImage(recipe.getName());
  }

   // ==================================================
   // Recipe Sorting & Filtering
   // Handles sorting and filtering of recipe cards
   // ==================================================

   private void handleSortBySelection() {
      String selectedSort = sortBy.getValue();

      if (selectedSort == null) return; // Do nothing if no sort option is selected

      switch (selectedSort) {
         case "A-Z":
               recipeList.sort((r1, r2) -> r1.getName().compareToIgnoreCase(r2.getName()));
               break;
         case "Z-A":
               recipeList.sort((r1, r2) -> r2.getName().compareToIgnoreCase(r1.getName()));
               break;
         case "Complexity":
               recipeList.sort((r1, r2) -> Integer.compare(r1.getComplexity(), r2.getComplexity()));
               break;
         case "Prep Time":
               recipeList.sort((r1, r2) -> Integer.compare(r1.getPrepTime(), r2.getPrepTime()));
               break;
         case "Cook Time":
               recipeList.sort((r1, r2) -> Integer.compare(r1.getCookTime(), r2.getCookTime()));
               break;
         default:
               break;
      }

      // Refresh the recipe cards in the FlowPane
      updateRecipeCards();
   }

   private void updateRecipeCards() {
      recipeFlowPane.getChildren().clear();
  
      for (Recipe recipe : recipeList) {
          VBox recipeCard = recipeWidgets.get(recipe.getID());
          if (recipeCard != null) {
              recipeFlowPane.getChildren().add(recipeCard);
          }
      }
   }

   private void loadRecipesCards() {
      recipeFlowPane.getChildren().clear(); // Clear any existing children before repopulating
  
      for (Recipe recipe : recipeList) {
          try {
              FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/javafx/Resources/FXMLs/RecipeCard.fxml"));
              VBox recipeCard = loader.load();
              RecipeCardController controller = loader.getController();
  
              // Load the recipe's image from the "Recipe Images" folder
              File imageFile = new File("src/main/resources/org/javafx/Resources/Recipe Images/" + recipe.getName() + ".png");
              Image image = imageFile.exists() ? new Image(imageFile.toURI().toString()) : null;
  
              controller.setRecipeData(recipe, image, this, "myrecipes"); // Pass recipe data and image
              
              recipeFlowPane.getChildren().add(recipeCard);
              recipeWidgets.put(recipe.getID(), recipeCard);
              recipeCard.setUserData(controller);
  
              applyHoverEffect(recipeCard, recipe);
  
              if (noRecipesTXT.isVisible()) {
                  noRecipesTXT.setVisible(false);
              }
          } catch (IOException e) {
              e.printStackTrace();
          }
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

   private void populateFilterOptions() {
      Set<String> ingredientSet = new HashSet<>();
      Set<String> tagSet = new HashSet<>();
      Set<String> categorySet = new HashSet<>();

      // Collect unique ingredients and tags from existing recipes
      for (Recipe recipe : recipeList) {
         // Now each ingredient is typed
         for (Ingredient ing : recipe.getIngredients()) {
            ingredientSet.add(ing.getName());
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
      categoryFilter.getItems().clear();
      categoryFilter.getItems().add("All Categories"); // Default option
      categoryFilter.getItems().addAll(categorySet);
      categoryFilter.setValue("All Categories"); // Set the default selection

      selectedIngredients.clear();
      selectedTags.clear();
   }

   @FXML
   private void clearAllFilters() {
      selectedIngredients.clear();
      selectedTags.clear();
      categoryFilter.setValue("All Categories"); // Reset to default
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
      categoryFilter.setOnAction(event -> filterRecipes());
   }
  
   private void filterRecipes() {

        // 1) First, figure out what was chosen in the ComboBoxes
        String selectedCategory = categoryFilter.getValue();
        String selectedSort = sortBy.getValue();
        boolean filterByCategory = !selectedCategory.equals("All Categories");
    
        // 2) Start with all recipes
        List<Recipe> matchingRecipes = new ArrayList<>(recipeList);
    
        // 3) Filter by category, ingredients, tags
        matchingRecipes.removeIf(recipe -> {
         boolean matchesCategory = !filterByCategory || recipe.getCategory().equalsIgnoreCase(selectedCategory);

         // Check if any of the recipe's ingredient names match the selected ingredients
         boolean matchesIngredients = selectedIngredients.isEmpty() ||
             recipe.getIngredients().stream()
                   .anyMatch(ing -> selectedIngredients.contains(ing.getName()));

         boolean matchesTags = selectedTags.isEmpty() ||
             recipe.getTags().stream()
                   .anyMatch(selectedTags::contains);

         return !(matchesCategory && matchesIngredients && matchesTags);
      });
    
        // 4) Sort the filtered list based on selectedSort
        if (selectedSort != null) {
            switch (selectedSort) {
                case "A-Z":
                    matchingRecipes.sort((r1, r2) -> r1.getName().compareToIgnoreCase(r2.getName()));
                    break;
                case "Z-A":
                    matchingRecipes.sort((r1, r2) -> r2.getName().compareToIgnoreCase(r1.getName()));
                    break;
                case "Complexity":
                    matchingRecipes.sort(Comparator.comparingInt(Recipe::getComplexity));
                    break;
                case "Prep Time":
                    matchingRecipes.sort(Comparator.comparingInt(Recipe::getPrepTime));
                    break;
                case "Cook Time":
                    matchingRecipes.sort(Comparator.comparingInt(Recipe::getCookTime));
                    break;
                default:
                    // do nothing or handle gracefully
                    break;
            }
        }
    
        // 5) Clear and rebuild the recipeFlowPane with the final filtered & sorted list
        recipeFlowPane.getChildren().clear();

        System.out.println(matchingRecipes);
    
        Set<VBox> added = new HashSet<>();
        for (Recipe recipe : matchingRecipes) {
            VBox card = recipeWidgets.get(recipe.getID());
            if (card != null && added.add(card)) {
                
                recipeFlowPane.getChildren().add(card);
            }
        }
    }
  
   private void filterRecipesBySearch(String query) {
      recipeFlowPane.getChildren().clear(); // Clear current displayed recipes
  
      if (query == null || query.trim().isEmpty()) {
          // If search is empty, show all recipes
          updateRecipeCards();
          return;
      }
  
      // Convert query to lowercase for case-insensitive matching
      String lowerCaseQuery = query.toLowerCase();
  
      for (Recipe recipe : recipeList) {
         // Convert each ingredient to lowercase name
         boolean ingredientMatch = recipe.getIngredients().stream()
               .anyMatch(ing -> ing.getName().toLowerCase().contains(lowerCaseQuery));
         boolean tagMatch = recipe.getTags().stream()
               .anyMatch(t -> t.toLowerCase().contains(lowerCaseQuery));

         if (recipe.getName().toLowerCase().contains(lowerCaseQuery) || ingredientMatch || tagMatch) {
            VBox card = recipeWidgets.get(recipe.getID());
            if (card != null) {
               recipeFlowPane.getChildren().add(card);
            }
         }
      }
   }

   // ===================================================
   // Recipe Collections Management
   // Handles recipe categorization into collections
   // ===================================================

   public void addRecipeToFavorites(Recipe recipe) {
      if (!recipeCollections.get("Favorites").contains(recipe.getID())) {
          recipeCollections.get("Favorites").add(recipe.getID());
          saveCollectionsToJson();
          System.out.println("Recipe added to Favorites.");
      } else {
          showAlert("Info", "Already in Favorites", "This recipe is already in the Favorites collection.");
      }
  }

   public void openAddToCollectionForm(Recipe recipe) {
      ChoiceDialog<String> dialog = new ChoiceDialog<>("All Recipes", recipeCollections.keySet());
      dialog.setTitle("Add to Collection");
      dialog.setHeaderText("Select a Collection");
      dialog.setContentText("Collection:");

      Optional<String> result = dialog.showAndWait();
      result.ifPresent(collection -> {
         if (!recipeCollections.containsKey(collection)) {
            recipeCollections.put(collection, new ArrayList<>());
         }
         if (!recipeCollections.get(collection).contains(recipe.getID())) {
            recipeCollections.get(collection).add(recipe.getID());
            saveCollectionsToJson();
            System.out.println("Recipe added to collection: " + collection);
         } else {
            showAlert("Info", "Already in Collection", "This recipe is already in the selected collection.");
         }
      });
   }

   private void openAddCollectionForm() {
      TextInputDialog dialog = new TextInputDialog();
      dialog.setTitle("New Collection");
      dialog.setHeaderText("Add a New Recipe Collection");
      dialog.setContentText("Collection Name:");
  
      Optional<String> result = dialog.showAndWait();
      result.ifPresent(collectionName -> {
          if (!recipeCollections.containsKey(collectionName)) {
              recipeCollections.put(collectionName, new ArrayList<>());
              saveCollectionsToJson();
              Button newButton = createCollectionButton(collectionName);
              collectionsButtons.getChildren().add(newButton);
          } else {
              showAlert("Error", "Duplicate Collection", "A collection with this name already exists.");
          }
      });
   }

   private void deleteCollection(String collectionName) {
      if (!collectionName.equals("All Recipes") && !collectionName.equals("Favorites")) {
         Alert confirmation = new Alert(AlertType.CONFIRMATION);
         confirmation.setTitle("Delete Collection");
         confirmation.setHeaderText("Are you sure you want to delete this collection?");
         confirmation.setContentText("Collection: " + collectionName);
         confirmation.getDialogPane().setPrefSize(300, 150); // Scale down the popup size
         Optional<ButtonType> result = confirmation.showAndWait();

         if (result.isPresent() && result.get() == ButtonType.OK) {
               recipeCollections.remove(collectionName);
               saveCollectionsToJson();
               loadCollectionButtons(); // Refresh buttons
         }
      } else {
         showAlert("Error", "Cannot Delete", "This collection cannot be deleted.");
      }
   }

   private void filterRecipesByCollection(String collectionName) {
      currentCollection = collectionName;
      recipeFlowPane.getChildren().clear();
  
      List<Integer> recipeIDs = recipeCollections.getOrDefault(collectionName, new ArrayList<>());
  
      for (Recipe recipe : recipeList) {
          if (recipeIDs.contains(recipe.getID()) || collectionName.equals("All Recipes")) {
              recipeFlowPane.getChildren().add(recipeWidgets.get(recipe.getID()));
          }
      }
   }

   private Button createCollectionButton(String collectionName) {
      Button button = new Button(collectionName);
      button.setStyle("-fx-background-color: #3C3C3C; -fx-text-fill: white; -fx-font-size: 18px; -fx-border-radius: 50; -fx-background-radius: 50; -fx-font-weight: bold;");
      button.setPrefWidth(120); // Set button width
      button.setPrefHeight(40); // Set button height

      button.setOnAction(event -> {
         filterRecipesByCollection(collectionName);
         updateButtonStyles(button);
      });

      button.setOnMouseEntered(event -> {
         if (!collectionName.equals(currentCollection)) {
            button.setStyle("-fx-background-color: #FF7F11; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 18px; -fx-border-radius: 50; -fx-background-radius: 50;");
         }
      });

      button.setOnMouseExited(event -> {
         if (collectionName.equals(currentCollection)) {
            button.setStyle("-fx-background-color: #FF7F11; -fx-text-fill: white; -fx-font-size: 18px; -fx-border-radius: 50; -fx-background-radius: 50; -fx-font-weight: bold;");
         } else {
            button.setStyle("-fx-background-color: #3C3C3C; -fx-text-fill: white; -fx-font-size: 18px; -fx-border-radius: 50; -fx-background-radius: 50; -fx-font-weight: bold;");
         }
      });

      // Add delete option for non-default collections
      if (!collectionName.equals("All Recipes") && !collectionName.equals("Favorites")) {
         ContextMenu contextMenu = new ContextMenu();
         MenuItem deleteItem = new MenuItem("Delete Collection");
         deleteItem.setOnAction(e -> deleteCollection(collectionName));
         contextMenu.getItems().add(deleteItem);
         button.setOnContextMenuRequested(e -> contextMenu.show(button, e.getScreenX(), e.getScreenY()));
      }

      // Apply selected style if it's the current collection
      if (collectionName.equals(currentCollection)) {
         button.setStyle("-fx-background-color: #FF7F11; -fx-text-fill: white; -fx-font-size: 18px; -fx-border-radius: 50; -fx-background-radius: 50; -fx-font-weight: bold;");
      }

      return button;
   }

   private void updateButtonStyles(Button activeButton) {
      for (var node : collectionsButtons.getChildren()) {
          if (node instanceof Button) {
              Button button = (Button) node;
              if (button == activeButton) {
                  button.setStyle("-fx-background-color: #FF7F11; -fx-text-fill: white; -fx-font-size: 18px; -fx-border-radius: 50; -fx-background-radius: 50; -fx-font-weight: bold;");
              } else {
                  button.setStyle("-fx-background-color: #3C3C3C; -fx-text-fill: white; -fx-font-size: 18px; -fx-border-radius: 50; -fx-background-radius: 50; -fx-font-weight: bold;");
              }
          }
      }
   }

   private void loadCollectionButtons() {
      collectionsButtons.getChildren().clear(); // Clear existing buttons
  
      for (String collectionName : recipeCollections.keySet()) {
          Button collectionButton = createCollectionButton(collectionName);
          collectionsButtons.getChildren().add(collectionButton);
      }
   }

   public void removeFromCurrentCollection(Recipe recipe) {
      String currentCollection = getCurrentCollection(); // Implement this to track the current collection name
      if (currentCollection.equals("All Recipes")) {
          showAlert("Error", "Cannot Remove from All Recipes", "You cannot remove a recipe from the All Recipes collection.");
          return;
      }
  
      List<Integer> recipeIDs = recipeCollections.getOrDefault(currentCollection, new ArrayList<>());
      if (recipeIDs.contains(recipe.getID())) {
          recipeIDs.remove((Integer) recipe.getID());
          saveCollectionsToJson();
          filterRecipesByCollection(currentCollection);
          System.out.println("Recipe removed from collection: " + currentCollection);
      } else {
          showAlert("Info", "Not in Collection", "This recipe is not part of the current collection.");
      }
   }

   private String getCurrentCollection() {
      return currentCollection;
   }

   // ========================================================
   // Recipe Image Handling
   // Handles selecting, copying, and deleting recipe images
   // ========================================================

   @FXML
   private void SelectImage() {
      FileChooser fileChooser = new FileChooser();
      fileChooser.setTitle("Select Recipe Images");
      fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
  
      List<File> selectedFiles = fileChooser.showOpenMultipleDialog((Stage) imagePreview.getScene().getWindow());
  
      if (selectedFiles != null && !selectedFiles.isEmpty()) {
          for (File file : selectedFiles) {
              Image image = new Image(file.toURI().toString());
  
              // Prompt user for step assignment
              TextInputDialog dialog = new TextInputDialog("0");
              dialog.setTitle("Assign Image to Step");
              dialog.setHeaderText("Assign this image to a step (0 = Main Image)");
              dialog.setContentText("Step:");
  
              Optional<String> result = dialog.showAndWait();
              result.ifPresent(stepStr -> {
                  try {
                      int stepIndex = Integer.parseInt(stepStr);
                      stepImageMap.put(stepIndex, image);
                      stepImageFileMap.put(stepIndex, file.getAbsolutePath());
                      refreshImageThumbnails(); // Update HBox
                  } catch (NumberFormatException e) {
                      showAlert("Error", "Invalid Step", "Please enter a valid step number.");
                  }
              });
          }
      }
  }

   private void refreshImageThumbnails() {
      recipeImagesHbox.getChildren().clear();

      stepImageMap.entrySet().stream()
         .sorted(Map.Entry.comparingByKey())
         .forEach(entry -> {
            int stepIndex = entry.getKey();
            Image image = entry.getValue();
            String path = stepImageFileMap.get(stepIndex);
            VBox thumbnailBox = createThumbnail(image, path, stepIndex);
            recipeImagesHbox.getChildren().add(thumbnailBox);
      });
   }

   private VBox createThumbnail(Image image, String imagePath, int index) {
      ImageView thumbnail = new ImageView(image);
      thumbnail.setFitWidth(150);
      thumbnail.setFitHeight(150);
      thumbnail.setPreserveRatio(true);
  
      Label label = new Label(index == 0 ? "Main Image" : "Step " + index);
      label.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");
  
      VBox wrapper = new VBox(thumbnail, label);
      wrapper.setAlignment(Pos.CENTER);
      wrapper.setSpacing(5);
  
      ContextMenu contextMenu = new ContextMenu();
      MenuItem removeItem = new MenuItem("Remove");
      removeItem.setOnAction(e -> {
         stepImageMap.remove(index);
         stepImageFileMap.remove(index);
         refreshImageThumbnails();
     });
      contextMenu.getItems().add(removeItem);
  
      thumbnail.setOnMouseClicked(event -> imagePreview.setImage(image));
      thumbnail.setOnContextMenuRequested(e -> contextMenu.show(thumbnail, e.getScreenX(), e.getScreenY()));
  
      return wrapper;
  }

   @FXML
   private void SaveRecipeImages(String recipeName) {
      File destinationFolder = new File("src/main/resources/org/javafx/Resources/Recipe Images");

      if (!destinationFolder.exists() && !destinationFolder.mkdirs()) {
         System.err.println("Failed to create recipe images directory.");
         return;
      }

      for (int i = 0; i < recipeThumbnails.size(); i++) {
         String imageName = (i == 0) ? recipeName + ".jpg" : recipeName + "_step" + i + ".jpg";
         File destinationFile = new File(destinationFolder, imageName);

         File sourceFile = new File(stepImageFileMap.get(i));

         try (InputStream input = sourceFile.toURI().toURL().openStream()) {
               Files.copy(input, destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
               System.out.println("Saved image: " + destinationFile.getAbsolutePath());
         } catch (IOException e) {
               e.printStackTrace();
         }
      }
   }

   private void copyImageToResources(File sourceFile, String destinationFileName) throws IOException {
      File destinationFolder = new File("src/main/resources/org/javafx/Resources/Recipe Images");
  
      if (!destinationFolder.exists() && !destinationFolder.mkdirs()) {
          throw new IOException("Failed to create directory: " + destinationFolder.getAbsolutePath());
      }
  
      File destinationFile = new File(destinationFolder, destinationFileName);
      try {
          Files.copy(sourceFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
          System.out.println("Image successfully copied to: " + destinationFile.getAbsolutePath());
      } catch (IOException e) {
          System.err.println("Error copying image: " + e.getMessage());
          throw e;
      }
  }

   private void deleteRecipeImage(String recipeName) {
      File imageFile = new File("src/main/resources/org/javafx/Resources/Recipe Images/" + recipeName + ".png");

      if (imageFile.exists()) {
         if (imageFile.delete()) {
            System.out.println("Image deleted: " + imageFile.getName());
         } else {
            System.err.println("Failed to delete image: " + imageFile.getName());
         }
      } else {
         System.out.println("No image found for: " + recipeName);
      }
   }

   // =====================================================================
   // Data Persistence (Saving & Loading JSON)
   // Handles reading and writing recipes and collections from JSON files
   // =====================================================================

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

   private void saveCollectionsToJson() {
      File file = new File(COLLECTIONS_FILE_PATH);
      try (Writer writer = new FileWriter(file)) {
         Gson gson = new GsonBuilder().setPrettyPrinting().create();
         gson.toJson(recipeCollections, writer);
         System.out.println("Collections successfully saved to JSON.");
      } catch (IOException e) {
         e.printStackTrace();
         System.out.println("Save Error - Failed to save collections to JSON file.");
      }
   }

   private void loadCollectionsFromJson() {
      File file = new File(COLLECTIONS_FILE_PATH);
      if (file.exists() && file.length() > 0) {
         try (Reader reader = new FileReader(file)) {
            Gson gson = new Gson();
            recipeCollections = gson.fromJson(reader, Map.class);
            if (recipeCollections == null) recipeCollections = new HashMap<>();
            System.out.println("Collections successfully loaded from JSON.");
         } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Load Error - Failed to load collections from JSON file.");
         }
      }
   }

   // ====================================================
   // Utility & Helper Methods
   // General helper functions for alerts and validation
   // ====================================================

   private void showAlert(String title, String header, String content) {
      Alert alert = new Alert(AlertType.ERROR);
      alert.setTitle(title);
      alert.setHeaderText(header);
      alert.setContentText(content);
      alert.showAndWait();
   }

   private void showAlert(String title, String content) {
      Alert alert = new Alert(Alert.AlertType.WARNING);
      alert.setTitle(title);
      alert.setHeaderText(null);
      alert.setContentText(content);
      alert.showAndWait();
   }

   private void saveRecipeNotes() {
      if (currentRecipe != null) {
          currentRecipe.setRecipeNotes(recipeNotesArea.getText());
          saveRecipesToJson(recipeList);
      }
  }

   private void openRecipeNotesPopup() {
      if (currentRecipe == null) return;

      Stage popupStage = new Stage();
      popupStage.initModality(Modality.APPLICATION_MODAL);
      popupStage.setTitle("Edit Recipe Notes");

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

      Button saveButton = new Button("Save");
      saveButton.setStyle(
         "-fx-background-color: #FF7F11; -fx-text-fill: white; " +
         "-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 8;"
      );

      Button cancelButton = new Button("Cancel");
      cancelButton.setStyle(
         "-fx-background-color: #555555; -fx-text-fill: white; " +
         "-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 8;"
      );

      saveButton.setOnAction(event -> {
         currentRecipe.setRecipeNotes(notesArea.getText());
         recipeNotesArea.setText(notesArea.getText());
         saveRecipeNotes();
         popupStage.close();
      });

      cancelButton.setOnAction(event -> popupStage.close());

      HBox buttonBox = new HBox(10, saveButton, cancelButton);
      buttonBox.setAlignment(Pos.CENTER);

      vbox.getChildren().addAll(notesLabel, notesArea, buttonBox);
      Scene scene = new Scene(vbox, 450, 350);
      popupStage.setScene(scene);
      popupStage.showAndWait();
   }


   private void openReviewPage() {
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
              //saveReview(currentRecipe.getID(), titleField.getText(), bodyArea.getText(), ratingBox.getValue());
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

   private Map<String, List<String>> loadFlavorMatrix(String path) {
      try (Reader reader = new FileReader(path)) {
          Gson gson = new Gson();
          Type type = new TypeToken<Map<String, List<String>>>() {}.getType();
          return gson.fromJson(reader, type);
      } catch (IOException e) {
          e.printStackTrace();
          return new HashMap<>();
      }
   }

   private Map<String, List<SubstitutionOption>> loadSubstitutions(String path) {
      try (Reader reader = new FileReader(path)) {
          Gson gson = new Gson();
          Type type = new TypeToken<Map<String, List<SubstitutionOption>>>() {}.getType();
          return gson.fromJson(reader, type);
      } catch (IOException e) {
          e.printStackTrace();
          return new HashMap<>();
      }
   }

   private void handleSuggestionsForRecipe(Recipe recipe) {
      // If no suggestions are stored, generate them
      if (recipe.getSuggestedIngredients() == null || recipe.getSuggestedIngredients().isEmpty()) {
         // 1) Gather typed ingredient names
         List<String> allIngredients = recipe.getIngredients().stream()
                                             .map(Ingredient::getName)
                                             .collect(Collectors.toList());
      
         // 2) Generate up to 3 suggestions
         List<String> suggestions = suggestionService.generateIngredientSuggestions(allIngredients, userPrefs, 3);
      
         // 3) Store them
         recipe.setSuggestedIngredients(suggestions);
      
         // 4) Optionally re-save so next time it’s consistent
         saveRecipesToJson(recipeList);
      }
  
      // Then show them
      List<String> suggestions = recipe.getSuggestedIngredients();
      if (suggestions != null && !suggestions.isEmpty()) {
         System.out.println("Suggestions for " + recipe.getName() + ": " + suggestions);
         // suggestionsArea.setItems(FXCollections.observableArrayList(suggestions));
      }
   }

   private void showSubstitutions(String ingredientName) {
      // Let’s call the service
      // Optionally pass a "context" if it’s known—e.g. "baking"
      List<SubstitutionOption> subs = suggestionService.getSubstitutions(ingredientName, userPrefs, "general");

      if (subs.isEmpty()) {
         Alert alert = new Alert(Alert.AlertType.INFORMATION);
         alert.setTitle("No Substitutions Found");
         alert.setHeaderText("No known substitutions for: " + ingredientName);
         alert.setContentText("Try again with a different ingredient or update your substitution data.");
         alert.showAndWait();
         return;
      }

      // Build a small UI or just show them in an alert
      StringBuilder sb = new StringBuilder("Substitutions for " + ingredientName + ":\n\n");
      for (SubstitutionOption opt : subs) {
         sb.append("- ").append(opt.getIngredientName())
            .append(" (Context: ").append(opt.getContext()).append(")\n")
            .append("  Notes: ").append(opt.getNotes()).append("\n\n");
      }

      Alert alert = new Alert(Alert.AlertType.INFORMATION);
      alert.setTitle("Substitutions");
      alert.setHeaderText("Possible Substitutions for: " + ingredientName);
      alert.setContentText(sb.toString());
      alert.showAndWait();
   }

   private boolean userPrefsAllows(String ingredient, UserPreferences prefs) {
      if (prefs.hasAllergyTo(ingredient)) {
          return false;
      }
      if (prefs.dislikes(ingredient)) {
          return false;
      }
      if (prefs.isVegan() && isAnimalProduct(ingredient)) {
          return false;
      }
      return true;
   }
   
   private boolean isAnimalProduct (String ingredient) {
      return false;
   }
   
}
