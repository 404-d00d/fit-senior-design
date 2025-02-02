package org.javafx.Controllers;

import java.util.ArrayList;
import java.util.List;

import org.javafx.Main.Main;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Popup;

public class UserDashboardController {

   @FXML
   private Button userDashboardButton, mealPlannerButton, myRecipesButton, inventoryButton,
                  inboxButton, browseRecipesButton, profileButton, settingsButton, myListsButton;

   @FXML
   private AnchorPane basePane;

   @FXML
   private TextField searchBar;

   @FXML
   private void initialize() {

      // Initialize AutoCompleteTextField
      setupCustomAutoComplete(searchBar, basePane);

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

   private void handleMouseExited(MouseEvent event) {
      Button button = (Button) event.getSource();
      // Reset style when mouse exits
      button.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 28px; -fx-font-weight: bold; -fx-background-radius: 25;");
   }
}
