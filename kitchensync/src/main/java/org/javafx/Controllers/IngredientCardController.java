package org.javafx.Controllers;

import java.io.File;

import javafx.fxml.FXML;
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

   private InventoryDashboardController InventoryDashboardController;

   public void setIngredientData(int id, String name, String imagePath, InventoryDashboardController controller) {
         this.ingredientId = id;
         this.InventoryDashboardController = controller;
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

      // Create the context menu with Edit and Delete options
      ContextMenu contextMenu = new ContextMenu();
      MenuItem deleteItem = new MenuItem("Delete");

      contextMenu.getItems().addAll(deleteItem);

      // Add event handlers for Edit and Delete, linking to MyRecipesController
      deleteItem.setOnAction(e -> InventoryDashboardController.deleteIngredient(ingredientId));


      // Handle right-click for context menu and left-click for details
      ingredientCardPane.setOnMouseClicked(event -> {

         if (event.getButton() == MouseButton.SECONDARY) {
            contextMenu.show(ingredientCardPane, event.getScreenX(), event.getScreenY());
         } else if (event.getButton() == MouseButton.PRIMARY) {
            InventoryDashboardController.openEditIngredient(ingredientId);
         }
      });
   }

}


