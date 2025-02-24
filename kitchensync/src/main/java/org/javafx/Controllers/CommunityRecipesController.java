package org.javafx.Controllers;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.javafx.Main.Main;
import org.javafx.Recipe.Recipe;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;

public class CommunityRecipesController {

   @FXML private ComboBox<String> sortBy, ingredientDropDown, categoryDropDown, tagsDropDown;
   @FXML private TextField searchBar;
   @FXML private FlowPane recipeFlowPane, recipeTagFlowPane;

   @FXML
   private Button addMealButton, userDashboardButton, mealPlannerButton, myRecipesButton, inventoryButton,
                  inboxButton, browseRecipesButton, profileButton, settingsButton, myListsButton, menuButton,
                  cookItButton, closeRecipeDetailsButton, prevStep, nextStep;

   @FXML
   private Pane menuPane, myRecipesPane, recipeReviews, recipeDetailsPane;

   private DynamoDbClient database;
   private AwsBasicCredentials awsCreds ;

   private List<Map<String, AttributeValue>> recipes;
   private List<VBox> recipeCards = new ArrayList<>();
   private List<String> preparationSteps = new ArrayList<>();

   @FXML private Text noRecipesTXT;
   @FXML private HBox suggestedRecipesBox;

   @FXML private ImageView recipeDetailsImages;
   @FXML private TextArea recipeDetailDescription, stepArea;
   @FXML private PieChart recipeCalories;
   @FXML private Text recipeNameTXT, recipePrepTimeTXT, recipeServingsTXT, recipePassiveTimeTXT, recipeCookTimeTXT, recipeTotalTimeTXT, recipeComplexityTXT,
                      stepOfTXT;

   @FXML private ListView<String> ingredientsArea, specialEquipmentTXTArea, recipeIngredients;
   
   private int currentStep = 0;
   private int displayStep = 0;


   private void initializeDatabase() {
      try {
          Region region = Region.US_EAST_1;
          database = DynamoDbClient.builder()
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

      // awsCreds = AwsBasicCredentials.create();
   

      initializeDatabase();
      loadCommunityRecipes();
      configureSortBy();
      configureFilters();

      searchBar.textProperty().addListener((obs, oldText, newText) -> filterRecipesBySearch(newText));
      prevStep.setOnAction(event -> detailsSteps(-1));
      nextStep.setOnAction(event -> detailsSteps(1));

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

   private void configureFilters() {
      ingredientDropDown.setOnAction(e -> filterRecipes());
      categoryDropDown.setOnAction(e -> filterRecipes());
      tagsDropDown.setOnAction(e -> filterRecipes());
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

               // Load image if exists (you can adjust path as needed)
               File imageFile = new File("src/main/resources/org/javafx/Resources/Recipe Images/" + recipeName + ".png");
               Image image = imageFile.exists() ? new Image(imageFile.toURI().toString()) : null;

               // Set data on the card
               controller.setRecipeData(recipe, image, this, "community"); // null since no editing/deleting in community view

               // Add to UI
               recipeFlowPane.getChildren().add(recipeCard);
               recipeCards.add(recipeCard);

               applyHoverEffect(recipeCard, recipe);

         } catch (Exception e) {
               e.printStackTrace();
         }
      }

      // Show or hide 'noRecipesTXT' based on recipe availability
      noRecipesTXT.setVisible(recipeCards.isEmpty());
   }

   private void applyHoverEffect(VBox recipeCard, Recipe recipe) {
      Tooltip tooltip = new Tooltip();
      tooltip.setStyle("-fx-background-color: rgba(0, 0, 0, 0.8); -fx-text-fill: white; -fx-padding: 10; -fx-font-size: 14px; -fx-border-radius: 10; -fx-background-radius: 10;");
      
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
      String ingredientFilter = ingredientDropDown.getValue();
      String categoryFilter = categoryDropDown.getValue();
      String tagsFilter = tagsDropDown.getValue();
  
      List<Map<String, AttributeValue>> filtered = recipes.stream()
          .filter(item -> ingredientFilter == null || 
              item.containsKey("ingredients") && item.get("ingredients").l().stream().anyMatch(ing -> ing.s().contains(ingredientFilter))
          )
          .filter(item -> categoryFilter == null || 
              item.containsKey("category") && item.get("category").s().equals(categoryFilter)
          )
          .filter(item -> tagsFilter == null || 
              item.containsKey("tags") && item.get("tags").l().stream().anyMatch(tag -> tag.s().equals(tagsFilter))
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
          
          // Pass "community" as the viewType to set up the community-specific context menu.
          controller.setRecipeData(recipe, null, this,"community");
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
      recipeDetailsImages.setImage(image);

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
         tagLabel.setStyle("-fx-background-color: #e0e0e0; -fx-padding: 5 10; -fx-border-radius: 10; -fx-background-radius: 10; -fx-margin: 5;");
         
         // Add the label to the FlowPane
         recipeTagFlowPane.getChildren().add(tagLabel);
      }

      preparationSteps = FXCollections.observableArrayList(recipe.getSteps());

      stepOfTXT.setText("Step " + 1 + " of " + preparationSteps.size());
      stepArea.setText(preparationSteps.get(0));

      // Styling
      specialEquipmentTXTArea.setStyle("-fx-font-size: 16px; -fx-padding: 5px;");
      recipeDetailDescription.setStyle("-fx-font-size: 16px; -fx-padding: 5px;");
      stepArea.setStyle("-fx-font-size: 16px; -fx-padding: 5px;");

      recipePrepTimeTXT.setStyle("-fx-font-size: 18px; -fx-padding: 5px;");
      recipePassiveTimeTXT.setStyle("-fx-font-size: 18px; -fx-padding: 5px;");
      recipeCookTimeTXT.setStyle("-fx-font-size: 18px; -fx-padding: 5px;");
      recipeTotalTimeTXT.setStyle("-fx-font-size: 18px; -fx-padding: 5px;");

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