package org.javafx.Controllers;

import org.javafx.Recipe.Recipe;

import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.input.MouseButton;

public class RecipeCardController {

   private Recipe recipe;

   @FXML
   private ImageView recipeImage;
   @FXML
   private Label recipeName;
   @FXML
   private VBox recipeCardPane;

   private int recipeId;

   private MyRecipesController myRecipesController;

   public void setRecipeData(Recipe newRecipe, Image image, MyRecipesController controller) {
      this.recipe = newRecipe;
      this.recipeId = newRecipe.getID();
      this.myRecipesController = controller; 
      recipeName.setText(newRecipe.getName());
      recipeImage.setImage(image);
   }

   @FXML
   private void initialize() {

      // Create the context menu with Edit and Delete options
      ContextMenu contextMenu = new ContextMenu();
      MenuItem editItem = new MenuItem("Edit");
      MenuItem deleteItem = new MenuItem("Delete");
      MenuItem addToCollectionItem = new MenuItem("Add to Collection");
      MenuItem addToFavoritesItem = new MenuItem("Add to Favorites");
      MenuItem removeFromCollectionItem = new MenuItem("Remove from Current Collection");

      contextMenu.getItems().addAll(editItem, deleteItem, addToCollectionItem, addToFavoritesItem, removeFromCollectionItem);

      // Add event handlers for Edit and Delete, linking to MyRecipesController
      editItem.setOnAction(e -> myRecipesController.openEditRecipe(recipe));
      deleteItem.setOnAction(e -> myRecipesController.deleteRecipe(recipe));

      // Add event handler for Add to Collection
      addToCollectionItem.setOnAction(e -> myRecipesController.openAddToCollectionForm(recipe));

      // Add event handler for Add to Favorites
      addToFavoritesItem.setOnAction(e -> myRecipesController.addRecipeToFavorites(recipe));

      // Add event handler for Remove From Current Collection
      removeFromCollectionItem.setOnAction(e -> myRecipesController.removeFromCurrentCollection(recipe));

      // Handle right-click for context menu and left-click for details
      recipeCardPane.setOnMouseClicked(event -> {

         if (event.getButton() == MouseButton.SECONDARY) {
            contextMenu.show(recipeCardPane, event.getScreenX(), event.getScreenY());
         } else if (event.getButton() == MouseButton.PRIMARY) {
            myRecipesController.showRecipeDetails(recipeId, recipeName.getText(), recipeImage.getImage(), recipe);
         }
      });
   }
}