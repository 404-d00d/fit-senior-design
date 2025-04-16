package org.javafx.Controllers;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.javafx.Main.Main;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Popup;

public class UserDashboardController {

   @FXML private Button userDashboardButton, mealPlannerButton, myRecipesButton, inventoryButton,
                  inboxButton, browseRecipesButton, profileButton, settingsButton, myListsButton;

   @FXML private AnchorPane basePane;

   @FXML private TextField searchBar;

   @FXML private AnchorPane tutorialOverlay;

   @FXML private Text tutorialText, noMealsTXT;

   @FXML private Button nextTutorialButton, skipTutorialButton;

   @FXML private Rectangle highlightBox;

   @FXML private HBox mealPlans;

   private int tutorialStep = 0;
   private final String[] tutorialMessages = {
      // Sidebar Navigation
      "Welcome to your Dashboard! Click 'Next' to continue.",
      "On the Dashboard you can see the next few meals you have planned aswell as recent notifications, and then items in your shopping list",
      "This is your Meal Planner. Plan meals efficiently with available ingredients.",
      "View your saved recipes in 'My Recipes'.",
      "Check 'Inventory' to keep track of ingredients in real-time.",
      "Access your Inbox for messages and notifications.",
      "Find new meal ideas in 'Find Recipes'.",
      "Update your profile information here.",
      "Customize your preferences in 'Settings'.",
      "Manage your shopping lists in 'My Lists'.",
      
      // Dashboard Overview
      //"Here’s your Notifications section. Stay updated on important alerts.",
      //"This is your Shopping List. Track what you need to buy.",
      //"Finally, your Meal Planner overview. See upcoming meals at a glance.",
      "You're all set! Enjoy using KitchenSync!"
  };


   @FXML
   private void initialize() {

      // Check if tutorial was completed (this should be saved in user preferences later)
      boolean tutorialCompleted = false; // Change this to read from a settings file later

      if (!tutorialCompleted) {
         startTutorial();
      }

      // Set tutorial button actions
      nextTutorialButton.setOnAction(event -> showNextTutorialStep());
      skipTutorialButton.setOnAction(event -> endTutorial());

      // Initialize AutoCompleteTextField
      setupCustomAutoComplete(searchBar, basePane);
      loadUpcomingMealsPreview();

      // Add search functionality
      searchBar.setOnKeyPressed(event -> {
         switch (event.getCode()) {
             case ENTER: // Check for the Enter key
                 performSearch(searchBar.getText());
                 break;
             default:
                 // Do nothing for other keys
                 break;
         }
     });

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
            Main.showInboxScreen();
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
            Main.showUserProfileScreen();
         } catch (Exception e) {
            e.printStackTrace();
         }
      });

      setHoverEffect(profileButton);
      
      // Switch to Browse Settings Screen
      settingsButton.setOnAction(event -> {
         try {
            Main.showUserSettingsScreen();
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

   }

   private void showNextTutorialStep() {
      if (tutorialStep < tutorialMessages.length) {
          tutorialText.setText(tutorialMessages[tutorialStep]);
  
          switch (tutorialStep) {
              // Sidebar Menu Buttons
              case 1: moveHighlight(userDashboardButton); break;
              case 2: moveHighlight(mealPlannerButton); break;
              case 3: moveHighlight(myRecipesButton); break;
              case 4: moveHighlight(inventoryButton); break;
              case 5: moveHighlight(inboxButton); break;
              case 6: moveHighlight(browseRecipesButton); break;
              case 7: moveHighlight(profileButton); break;
              case 8: moveHighlight(settingsButton); break;
              case 9: moveHighlight(myListsButton); break;
  
              // Dashboard Main Areas
              //case 9: moveHighlight(notificationsPanel); break;
              //case 10: moveHighlight(shoppingListPanel); break;
              //case 11: moveHighlight(mealPlannerOverview); break;
  
              default:
                  highlightBox.setVisible(false); // Hide highlight after last step
          }
  
          tutorialStep++;
      } else {
          endTutorial();
      }
  }
  
   private void moveHighlight(Button target) {
      highlightBox.setLayoutX(target.getLayoutX());
      highlightBox.setLayoutY(target.getLayoutY()+24);
      highlightBox.setWidth(264);
      highlightBox.setHeight(48);
      highlightBox.setVisible(true);
   }

   private void startTutorial() {
      tutorialOverlay.setVisible(true);
      tutorialStep = 0;
      showNextTutorialStep();
   }
  
   private void endTutorial() {
      tutorialOverlay.setVisible(false);
      // Here, save a preference to mark the tutorial as completed
   }

   private void setupCustomAutoComplete(TextField searchBar, AnchorPane basePane) {
      // Create a list of all possible suggestions
      List<String> allSuggestions = new ArrayList<>();
      allSuggestions.add("Meal Planner");
      allSuggestions.add("Inventory");
      allSuggestions.add("Recipes");
      allSuggestions.add("Lists");
      allSuggestions.add("Settings");
  
      // Create a ListView to show suggestions
      ListView<String> suggestionListView = new ListView<>();
      suggestionListView.setMaxHeight(100); // Limit the height of the dropdown
  
      // Create a Popup to display the ListView
      Popup suggestionPopup = new Popup();
      suggestionPopup.setAutoHide(true);
      suggestionPopup.getContent().add(suggestionListView);
  
      // Add a listener to filter suggestions dynamically
      searchBar.textProperty().addListener((observable, oldValue, newValue) -> {
          if (newValue == null || newValue.trim().isEmpty()) {
              suggestionPopup.hide();
          } else {
              // Filter suggestions
              List<String> filteredSuggestions = new ArrayList<>();
              for (String suggestion : allSuggestions) {
                  if (suggestion.toLowerCase().contains(newValue.toLowerCase())) {
                      filteredSuggestions.add(suggestion);
                  }
              }
  
              if (!filteredSuggestions.isEmpty()) {
                  ObservableList<String> items = FXCollections.observableArrayList(filteredSuggestions);
                  suggestionListView.setItems(items);
  
                  // Position the popup below the search bar
                  suggestionPopup.show(basePane, 
                      searchBar.localToScene(0, 0).getX() + basePane.getScene().getWindow().getX(), 
                      searchBar.localToScene(0, 0).getY() + searchBar.getHeight() + basePane.getScene().getWindow().getY());
              } else {
                  suggestionPopup.hide();
              }
          }
      });
  
      // Handle selection from the ListView
      suggestionListView.setOnMouseClicked(event -> {
          String selectedItem = suggestionListView.getSelectionModel().getSelectedItem();
          if (selectedItem != null) {
              searchBar.setText(selectedItem);
              performSearch(selectedItem);
              suggestionPopup.hide();
          }
      });
  
      // Handle keyboard navigation in the ListView
      searchBar.setOnKeyPressed(event -> {
          if (suggestionPopup.isShowing()) {
              if (event.getCode() == KeyCode.DOWN) {
                  suggestionListView.requestFocus();
                  suggestionListView.getSelectionModel().select(0);
              }
          }
      });
  
      suggestionListView.setOnKeyPressed(event -> {
          if (event.getCode() == KeyCode.ENTER) {
              String selectedItem = suggestionListView.getSelectionModel().getSelectedItem();
              if (selectedItem != null) {
                  searchBar.setText(selectedItem);
                  performSearch(selectedItem);
                  suggestionPopup.hide();
              }
          } else if (event.getCode() == KeyCode.ESCAPE) {
              suggestionPopup.hide();
          }
      });
  }

   private void performSearch(String query) {
      if (query == null || query.trim().isEmpty()) {
         System.out.println("Empty search query.");
         return;
      }

      // Search in the active screen (e.g., Dashboard)
      if (!searchInActiveScreen(query)) {
         // If not found, search in other screens
         searchInOtherScreens(query);
      }
   }

   private boolean searchInActiveScreen(String query) {
      System.out.println("Searching in the active screen: " + query);
      // Add logic to search dashboard content
      return false; // Placeholder
   }

   private void searchInOtherScreens(String query) {
      System.out.println("Searching in other screens: " + query);
      // Example: Extend to query recipes or inventory from the database
   }

   private void setHoverEffect(Button button) {
      button.setOnMouseEntered(this::handleMouseEntered);
      button.setOnMouseExited(this::handleMouseExited);
   }

   private void handleMouseEntered(MouseEvent event) {
      Button button = (Button) event.getSource();
      // Change style when mouse enters
      button.setStyle("-fx-background-color: #FF7F11; -fx-text-fill: white; -fx-font-size: 28px; -fx-font-weight: bold; -fx-background-radius: 25;");
   }
   
   //w
   private void handleMouseExited(MouseEvent event) {
      Button button = (Button) event.getSource();
      // Reset style when mouse exits
      button.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 28px; -fx-font-weight: bold; -fx-background-radius: 25;");
   }

   private void loadUpcomingMealsPreview() {
      mealPlans.getChildren().clear();

      try (FileReader reader = new FileReader("mealPlans.json")) {
         Gson gson = new GsonBuilder()
               .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
               .create();

         Type type = new TypeToken<Map<LocalDate, List<Map<String, Object>>>>() {}.getType();
         Map<LocalDate, List<Map<String, Object>>> mealPlanMap = gson.fromJson(reader, type);

         List<Map<String, Object>> cookBlocks = new ArrayList<>();

         if (mealPlanMap == null || mealPlanMap.isEmpty()) {
            return; // or optionally show a "no meals planned" UI message
        }

         for (Map.Entry<LocalDate, List<Map<String, Object>>> entry : mealPlanMap.entrySet()) {
               for (Map<String, Object> meal : entry.getValue()) {
                  if (meal.containsKey("cookTime")) {
                     Map<String, Object> cookBlock = (Map<String, Object>) meal.get("cookTime");
                     cookBlocks.add(cookBlock);
                  }
               }
         }

         // Sort by date then meal type order
         cookBlocks.sort((a, b) -> {
               LocalDate dateA = LocalDate.parse((String) a.get("date"));
               LocalDate dateB = LocalDate.parse((String) b.get("date"));

               int dateCompare = dateA.compareTo(dateB);
               if (dateCompare != 0) return dateCompare;

               // Order: Breakfast < Lunch < Dinner < Snack
               List<String> order = List.of("Breakfast", "Lunch", "Dinner", "Snack");
               return Integer.compare(order.indexOf(a.get("mealType")), order.indexOf(b.get("mealType")));
         });

         // Limit to first 7 meals
         cookBlocks = new ArrayList<>(cookBlocks.stream().limit(7).collect(Collectors.toList()));

         if (cookBlocks.size() > 0) {
            noMealsTXT.setVisible(false);
         }

         for (Map<String, Object> block : cookBlocks) {
               String name = (String) block.get("name");
               String mealType = (String) block.get("mealType");
               String date = (String) block.get("date");
               int recipeID = ((Number) block.get("recipeID")).intValue();

               VBox previewCard = createMealPreviewCard(name, mealType, date, recipeID);
               mealPlans.getChildren().add(previewCard);
         }

      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   private VBox createMealPreviewCard(String name, String type, String date, int recipeID) {
      VBox card = new VBox();
      card.setSpacing(10);
      card.setPrefSize(200, 300);
      card.setMaxSize(200, 300);
      card.setMinSize(200, 300);
      card.setStyle(
          "-fx-background-color: #2A2A2A;" +
          "-fx-border-radius: 10;" +
          "-fx-background-radius: 10;"
      );
  
      // Recipe image section
      ImageView imageView = new ImageView();
      imageView.setFitWidth(180); // Slightly smaller than card width
      imageView.setFitHeight(180);
      imageView.setPreserveRatio(false);
  
      File imageFile = new File("src/main/resources/org/javafx/Resources/Recipe Images/" + name + ".png");
      if (imageFile.exists()) {
          Image img = new Image(imageFile.toURI().toString());
          imageView.setImage(img);
      }
  
      VBox imageContainer = new VBox(imageView);
      imageContainer.setStyle("-fx-alignment: center;");
      imageContainer.setPrefHeight(200);
      imageContainer.setMaxWidth(200);
  
      // Name label
      Label nameLabel = new Label(name);
      nameLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 18px;");
      nameLabel.setWrapText(true);
      nameLabel.setPrefWidth(180);
  
      // Meta label (type + date)
      Label metaLabel = new Label(type + " — " + LocalDate.parse(date).format(DateTimeFormatter.ofPattern("MMM d")));
      metaLabel.setStyle("-fx-text-fill: #FF7F11; -fx-font-size: 18px; -fx-font-weight: bold;");
      metaLabel.setWrapText(true);
      metaLabel.setPrefWidth(180);
  
      VBox textContainer = new VBox(nameLabel, metaLabel);
      textContainer.setSpacing(5);
      textContainer.setPrefHeight(100);
      textContainer.setStyle("-fx-alignment: center-left; -fx-padding: 5 10 0 10;");
  
      card.getChildren().addAll(imageContainer, textContainer);
      return card;
  }
  

}
