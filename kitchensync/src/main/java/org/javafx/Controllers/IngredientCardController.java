package org.javafx.Controllers;

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

   private InventoryDashboardController InventoryDashboardController;

   public void setIngredientData(int id, String name, Image image, InventoryDashboardController controller) {
      this.ingredientId = id;
      this.InventoryDashboardController = controller; 
      ingredientName.setText(name);
      ingredientImage.setImage(image);
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

   @FXML
   private void initialize() {

      // Create the context menu with Edit and Delete options
      ContextMenu contextMenu = new ContextMenu();
      MenuItem deleteItem = new MenuItem("Delete");

      contextMenu.getItems().addAll(deleteItem);

      // Add event handlers for Edit and Delete, linking to MyRecipesController
      deleteItem.setOnAction(e -> InventoryDashboardController.deleteIngriendent(ingredientId));


      // Handle right-click for context menu and left-click for details
      ingredientCardPane.setOnMouseClicked(event -> {

         if (event.getButton() == MouseButton.SECONDARY) {
            contextMenu.show(ingredientCardPane, event.getScreenX(), event.getScreenY());
         } else if (event.getButton() == MouseButton.PRIMARY) {
            InventoryDashboardController.openEditIngriedent(ingredientId);
         }
      });
   }

}


