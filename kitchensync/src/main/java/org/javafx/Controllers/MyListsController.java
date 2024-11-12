package org.javafx.Controllers;

import org.javafx.Main.Main;
import org.javafx.Item.Item;

import java.util.ArrayList;

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

public class MyListsController {

   @FXML
   private Button userDashboardButton, mealPlannerButton, myRecipesButton, inventoryButton,
                  inboxButton, browseRecipesButton, profileButton, settingsButton, myListsButton,
                  addIngredientButton, menuButton, closeButton, saveButton, addListButton;

   @FXML
   private Pane menuPane, myListsPane, addIngredientPane;

   @FXML
   private TextField productQuantity, ingredientName;   

   @FXML
   private ComboBox<String> productUnit;

   @FXML
   private ListView<Item> myListsView;  // ListView to display ingredients

   @FXML
   private void initialize() {

      myListsView.setCellFactory(param -> new IngredientCell());

      ArrayList<Item> ingredientList = new ArrayList<Item>();

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

            //use these to store the info from the form
            //also will need a way to know which list to add this to
            String name = ingredientName.getText();
            String quantity = productQuantity.getText();
            String unit = productUnit.getValue();

            if (isInputValid(name, quantity, unit)) {
               // Create a new item and add it to the list
               Item newIngredient = new Item(name, quantity, Integer.parseInt(quantity), unit, "Sample Location", "2050-12-13");
               myListsView.getItems().add(newIngredient);

               ingredientName.clear();
               productQuantity.clear();
               productUnit.setValue(null);

               // Close the input pane and display the list pane
               myListsPane.setVisible(true);
               addIngredientPane.setVisible(false);
            }

            // pull up window to transfer data from that to new item class
            //Item newIngredient = new Item("Toilet Paper Moonshine", "0", 1, "gallon", "Shed in Tampa", ("2050-12-13").toString());
            //ingredientList.add(newIngredient);
            //for (int x = 0; x < ingredientList.size(); x++) {
               //System.out.println(ingredientList.get(x).getName());
            //}
            //System.out.println("WHERE IS MY "+newIngredient.getName());
         } catch (Exception e) {
            e.printStackTrace();
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
      button.setStyle("-fx-background-color: orange; -fx-text-fill: white; -fx-wrap-text: true; -fx-font-size: 40px;");
   }

   private void handleMouseExited(MouseEvent event) {
      Button button = (Button) event.getSource();
      // Reset style when mouse exits
      button.setStyle("-fx-background-color: transparent; -fx-text-fill: black; -fx-wrap-text: true; -fx-font-size: 40px;");
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
}
