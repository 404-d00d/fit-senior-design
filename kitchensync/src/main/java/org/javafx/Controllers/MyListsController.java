package org.javafx.Controllers;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.javafx.Item.Item;
import org.javafx.Main.Main;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class MyListsController {

   @FXML
   private Button userDashboardButton, mealPlannerButton, myRecipesButton, inventoryButton,
                  inboxButton, browseRecipesButton, profileButton, settingsButton, myListsButton,
                  addIngredientButton, menuButton, closeButton, saveButton, addListButton, neededIngredients;

   @FXML
   private Pane menuPane, myListsPane, addIngredientPane, editPreferences;

   @FXML
   private TextField productQuantity, ingredientName;   

   @FXML
   private ComboBox<String> productUnit;

   @FXML
   private ListView<Item> myListsView; // ListView to display ingredients in the selected list

   private ObservableList<String> lists = FXCollections.observableArrayList(); // Observable list to store list names
   private Map<String, ObservableList<Item>> ingredientsMap = new HashMap<>(); // Map to store ingredients for each list

   private static final String LISTS_JSON_FILE = "lists.json"; // JSON file for saving lists

   @FXML
   private VBox listPane;

   private Button currentSelectedButton = null;

   private MealPlannerController mealPlannerController;

   @FXML
   private void initialize() {
      // Load lists from JSON file
      loadListsFromJson();

      listPane.setSpacing(10);  // Add 10 pixels of vertical space between buttons

      // Ensure "Needed Ingredients" exists as a default list
      if (!lists.contains("Needed Ingredients")) {
         lists.add("Needed Ingredients");
         ingredientsMap.put("Needed Ingredients", FXCollections.observableArrayList());
         saveListsToJson(); // Save changes to JSON to persist the default list
      }

      // Set action for the Needed Ingredients button
      neededIngredients.setOnAction(event -> {
         myListsView.setItems(ingredientsMap.get("Needed Ingredients"));
         highlightSelectedButton(neededIngredients);
      });

      // Set the Needed Ingredients button as selected by default
      highlightSelectedButton(neededIngredients);
      myListsView.setItems(ingredientsMap.get("Needed Ingredients"));

      // Set the listener to add a new list using a dialog instead of a text field
      addListButton.setOnAction(event -> {
         addNewListWithDialog();
      });

      myListsView.setCellFactory(param -> new IngredientCell());

      productUnit.getItems().addAll("kg", "g", "l", "ml", "oz", "lbs");

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

      closeButton.setOnAction(event -> {
         try {
            addIngredientPane.setVisible(false);
            myListsPane.setVisible(true);
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

      // Switch to Lists Screen
      myListsButton.setOnAction(event -> {
         try {
            Main.showMyListsScreen();
         } catch (Exception e) {
            e.printStackTrace();
         }
      });

      setHoverEffect(myListsButton);

      addIngredientButton.setOnAction(event -> {
         try {

            myListsPane.setVisible(false);
            addIngredientPane.setVisible(true);

         } catch (Exception e) {
            e.printStackTrace();
         }
      });

      saveButton.setOnAction(event -> {
         try {
             String name = ingredientName.getText();
             String quantity = productQuantity.getText();
             String unit = productUnit.getValue();
     
             if (isInputValid(name, quantity, unit)) {
                 String selectedList = getSelectedListFromPane();
     
                 if (selectedList != null) {
                     ObservableList<Item> ingredientList = ingredientsMap.get(selectedList);
                     if (ingredientList != null) {
                         // Generate the ID based on the number of entries in the list
                         int newId = ingredientList.size() + 1;
     
                         // Create a new Item with the generated ID
                         Item newIngredient = new Item(name, newId, Integer.parseInt(quantity), unit, "Sample Location", "2050-12-13", null, false, 0);
     
                         // Add the new ingredient to the list
                         ingredientList.add(newIngredient);
                         myListsView.setItems(ingredientList);
     
                         // Save changes to JSON
                         saveListsToJson();
     
                         // Clear the input fields
                         ingredientName.clear();
                         productQuantity.clear();
                         productUnit.setValue(null);
     
                         // Close the input pane and display the list pane
                         myListsPane.setVisible(true);
                         addIngredientPane.setVisible(false);
                     } else {
                         showAlert("Error", "List Not Found", "Please select a valid list to add the ingredient.");
                     }
                 } else {
                     showAlert("Error", "No List Selected", "Please select a list to add the ingredient.");
                 }
             }
         } catch (Exception e) {
             e.printStackTrace();
             showAlert("Error", "Save Failed", "An unexpected error occurred while saving the ingredient.");
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

   private boolean isInputValid(String name, String quantity, String unit) {
      if (name == null || name.isEmpty()) {
         showAlert("Error", "Missing Ingredient Name", "Please enter a valid ingredient name.");
         return false;
      }
      if (quantity == null || quantity.isEmpty()) {
         showAlert("Error", "Missing Quantity", "Please enter a valid quantity.");
         return false;
      }
      try {
         Integer.parseInt(quantity);
      } catch (NumberFormatException e) {
         showAlert("Error", "Invalid Quantity", "Quantity must be a number.");
         return false;
      }
      if (unit == null) {
         showAlert("Error", "Missing Unit", "Please select a unit of measurement.");
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

   public void setMealPlannerController(MealPlannerController controller) {
      this.mealPlannerController = controller;
   }

   public void updateNeededIngredientsList(List<Map<String, Object>> neededIngredients) {
      // Fetch the "Needed Ingredients" list from the map
      ObservableList<Item> neededIngredientsList = ingredientsMap.get("Needed Ingredients");
  
      if (neededIngredientsList == null) {
          // If the list doesn't exist, create and initialize it
          neededIngredientsList = FXCollections.observableArrayList();
          ingredientsMap.put("Needed Ingredients", neededIngredientsList);
      } else {
          // Clear existing items in the list
          neededIngredientsList.clear();
      }
  
      // Convert the list of maps into `Item` objects and add them to the "Needed Ingredients" list
      for (Map<String, Object> ingredient : neededIngredients) {
          String name = (String) ingredient.get("name");
          int quantity = ((Number) ingredient.get("quantity")).intValue();
          String unit = (String) ingredient.get("unit");
          String location = (String) ingredient.getOrDefault("meal", "Unknown Meal");
  
          // Create an Item instance and add it to the list
          Item item = new Item(name, neededIngredientsList.size() + 1, quantity, unit, location, "N/A", null, false, 0);
          neededIngredientsList.add(item);
      }
  
      // Update the ListView to display the updated "Needed Ingredients" list
      if (currentSelectedButton != null && "Needed Ingredients".equals(currentSelectedButton.getText())) {
          myListsView.setItems(neededIngredientsList);
      }
  
      // Save changes to JSON to persist the updates
      saveListsToJson();
  }

   private void loadListsFromJson() {
      try (FileReader reader = new FileReader(LISTS_JSON_FILE)) {
          Gson gson = new Gson();
          Type type = new TypeToken<Map<String, ArrayList<Item>>>() {}.getType();
          Map<String, ArrayList<Item>> savedLists = gson.fromJson(reader, type);
          if (savedLists != null) {
              savedLists.forEach((key, value) -> {
                  lists.add(key);
                  ingredientsMap.put(key, FXCollections.observableArrayList(value));
                  addListButtonToPane(key); // Add button for each loaded list
              });
          }
      } catch (IOException e) {
          System.err.println("Could not load lists: " + e.getMessage());
          // Initialize an empty JSON file if not found
          saveListsToJson();
      }
  }

   private void saveListsToJson() {
      try (FileWriter writer = new FileWriter(LISTS_JSON_FILE)) {
         Gson gson = new Gson();
         Map<String, ArrayList<Item>> savedLists = new HashMap<>();
         ingredientsMap.forEach((key, value) -> savedLists.put(key, new ArrayList<>(value)));
         gson.toJson(savedLists, writer);
      } catch (IOException e) {
         System.err.println("Could not save lists: " + e.getMessage());
      }
   }

   private void addListButtonToPane(String listName) {

      // Check if the button already exists
      if (listPane.getChildren().stream().anyMatch(node -> node instanceof Button && ((Button) node).getText().equals(listName))) {
         return; // The button already exists, so do not add it again
      }

      Button listButton = new Button(listName);
      listButton.setStyle("-fx-background-color: Grey; -fx-pref-width: 346px; -fx-pref-height: 60px; -fx-background-radius: 50; -fx-border-radius: 50; -fx-border-width: 4; -fx-font-size: 30; -fx-text-fill: white; -fx-text-alignment: center;");
      
      listButton.setOnAction(event -> {
          highlightSelectedButton(listButton);
          
          // If a list is selected (i.e., not deselected), update the list view
          if (currentSelectedButton == listButton) {
              myListsView.setItems(ingredientsMap.get(listName));
          }
      });
  
      // Add context menu for deleting lists using setOnContextMenuRequested
      ContextMenu contextMenu = new ContextMenu();
      MenuItem deleteItem = new MenuItem("Delete List");
      deleteItem.setOnAction(event -> {
          lists.remove(listName);
          ingredientsMap.remove(listName);
          listPane.getChildren().remove(listButton);
          saveListsToJson(); // Save changes to JSON
      });
      contextMenu.getItems().add(deleteItem);
  
      // Set the event to display the context menu on right-click
      listButton.setOnContextMenuRequested(event -> {
          contextMenu.show(listButton, event.getScreenX(), event.getScreenY());
      });
  
      listPane.getChildren().add(listButton);
   }

   private String getSelectedListFromPane() {
      if (currentSelectedButton != null) {
          return currentSelectedButton.getText();
      }
      return null;
  }

   private void highlightSelectedButton(Button selectedButton) {
      // Reset the style for the previously selected button if there is one
      if (currentSelectedButton != null) {
         currentSelectedButton.setStyle("-fx-background-color: Grey; -fx-pref-width: 346px; -fx-pref-height: 60px; -fx-background-radius: 50; -fx-border-radius: 50; -fx-border-width: 4; -fx-font-size: 30; -fx-text-fill: black; -fx-text-alignment: center;");
      }

      // Set the new selected button and change its style
      selectedButton.setStyle("-fx-background-color:  #FF7F11; -fx-pref-width: 346px; -fx-pref-height: 60px; -fx-background-radius: 50; -fx-border-radius: 50; -fx-border-width: 4; -fx-font-size: 30; -fx-text-fill: White; -fx-font-weight: bold; -fx-text-alignment: center;");
      currentSelectedButton = selectedButton;
   }

   private void addNewListWithDialog() {
      TextInputDialog dialog = new TextInputDialog();
      dialog.setTitle("Add New List");
      dialog.setHeaderText("Enter the name for the new list:");
      dialog.setContentText("List Name:");

      Optional<String> result = dialog.showAndWait();
      result.ifPresent(name -> {
         String listName = name.trim();
         if (!listName.isEmpty() && !lists.contains(listName)) {
               lists.add(listName);
               ingredientsMap.put(listName, FXCollections.observableArrayList());
               addListButtonToPane(listName); // Add button to VBox for the new list
               saveListsToJson(); // Save changes to JSON
         } else {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Invalid List");
            alert.setHeaderText(null);
            alert.setContentText("List already exists or name is empty.");
            alert.showAndWait();
         }
      });
   }

}
