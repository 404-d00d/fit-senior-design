package org.javafx.Controllers;

import java.io.File;

import javafx.fxml.FXML;
import org.javafx.Item.Item;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.input.MouseButton;

public class IngredientCardController {

   @FXML
   private ImageView ingredientImage;
   @FXML
   private Label ingredientName;
   @FXML
   private VBox ingredientCardPane;
   
   private int ingredientId;
   private String imagePath;
   private Item ingredient;
   private InventoryDashboardController InventoryDashboardController;

   MenuItem deleteItem = new MenuItem("Delete");
   MenuItem trackItem = new MenuItem("Track Item");
   MenuItem untrackItem = new MenuItem("Untrack Item");
   MenuItem editTracking = new MenuItem("Edit Tracking Settings");


   public void setIngredientData(int id, String name, String imagePath, InventoryDashboardController controller) {
         this.ingredientId = id;
         this.InventoryDashboardController = controller;
         this.ingredient = controller.getIngredientById(id);
         ingredientName.setText(name);
   
         if (imagePath != null) {
            try {
               Image image = new Image(new File("src/main/resources/org/javafx/Resources/Item Images", imagePath).toURI().toString());
               this.ingredientImage.setImage(image);
            } catch (Exception e) {
               e.printStackTrace();
            }
         }
   
         // Store this controller instance in the VBox node for later access
         ingredientCardPane.getProperties().put("controller", this);
   }

   // Getter methods to access data for duplication
   public int getIngredientId() {
      return ingredientId;
   }

   public String getIngredientName() {
      return ingredientName.getText();
   }

   public Image getImage() {
      return ingredientImage.getImage();
   }

   public String getImagePath() {
      return imagePath;
   }

   @FXML
   private void initialize() {

      // Create context menu
      ContextMenu contextMenu = new ContextMenu();

      // Add event handlers for Edit and Delete, linking to MyRecipesController
      deleteItem.setOnAction(e -> InventoryDashboardController.deleteIngredient(ingredientId));

      // Track Item Action
      trackItem.setOnAction(event -> {
         ingredient.setAutoTrack(true);
         ingredient.setMinThreshold(0); // Default min quantity
         InventoryDashboardController.updateInventoryData();

         contextMenu.getItems().remove(trackItem);
         contextMenu.getItems().add(1, untrackItem);
         contextMenu.getItems().add(editTracking);
      });


      // Untrack Item Action
      untrackItem.setOnAction(event -> {
         ingredient.setAutoTrack(false);
         InventoryDashboardController.updateInventoryData();

         // Update menu dynamically
         contextMenu.getItems().remove(untrackItem);
         contextMenu.getItems().remove(editTracking);
         contextMenu.getItems().add(1, trackItem);
         System.out.print(contextMenu.getItems());
      });

      // Edit Tracking Settings
      editTracking.setOnAction(event -> InventoryDashboardController.openTrackingSettings(ingredient));

      // Add default items
      contextMenu.getItems().add(deleteItem);

      // Initialize tracking menu state
      if (ingredient != null && ingredient.getAutoTrack()) {
         contextMenu.getItems().add(untrackItem);
         contextMenu.getItems().add(editTracking);
      } else {
         contextMenu.getItems().add(trackItem);
      }

      // Handle right-click for context menu and left-click for details
      ingredientCardPane.setOnMouseClicked(event -> {

         if (event.getButton() == MouseButton.SECONDARY) {
            updateContextMenu(contextMenu);
            contextMenu.show(ingredientCardPane, event.getScreenX(), event.getScreenY());
         } else if (event.getButton() == MouseButton.PRIMARY) {
            InventoryDashboardController.openEditIngredient(ingredientId);
         }
      });  
   }

   private void updateContextMenu(ContextMenu contextMenu) {

      contextMenu.getItems().clear(); // Clear existing items
      contextMenu.getItems().add(deleteItem);
  
      if (ingredient != null && ingredient.getAutoTrack()) {
          contextMenu.getItems().add(untrackItem);
          contextMenu.getItems().add(editTracking);
      } else {
          contextMenu.getItems().add(trackItem);
      }
  }
}


