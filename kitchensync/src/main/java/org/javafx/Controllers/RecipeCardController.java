package org.javafx.Controllers;

import java.io.IOException;

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
   private CommunityRecipesController communityRecipesController;
   private MealPlannerController mealPlannerController;

   // View type flag ("myrecipes" or "community")
   private String viewType;

   public void setRecipeData(Recipe newRecipe, Image image, Object controller, String viewType) {
      this.recipe = newRecipe;
      this.recipeId = newRecipe.getID();
      String fixedName = capitalizeWords(newRecipe.getName());
      recipeName.setText(fixedName);
      recipeImage.setImage(image);
      this.viewType = viewType;

      if ("myrecipes".equals(viewType)) {
         this.myRecipesController = (MyRecipesController) controller;
      }

      if ("community".equals(viewType)) {
         this.communityRecipesController = (CommunityRecipesController) controller;
      }

      if ("mealPlanner".equals(viewType)) {
         this.mealPlannerController = (MealPlannerController) controller;
      }

      configureContextMenu();
   }

   /**
    * Configure the context menu based on the view type.
    */
   private void configureContextMenu() {
      ContextMenu contextMenu = new ContextMenu();
      
      if ("community".equals(viewType)) {

         MenuItem reviewsAndFeedback = new MenuItem("Reviews And Feedback");
         reviewsAndFeedback.setOnAction(e -> RecipeReviewsAndFeedBack());
         
         // if/when we figure this out its here
         MenuItem shareItem = new MenuItem("Share Recipe");
         shareItem.setOnAction(e -> handleShareRecipe());

         MenuItem saveRecipeItem = new MenuItem("Save Recipe");
         saveRecipeItem.setOnAction(e -> {
            try {
               communityRecipesController.saveRecipe(recipe, recipeImage.getImage());
            } catch (IOException e1) {
               e1.printStackTrace();
            }
         });
         
         contextMenu.getItems().addAll(reviewsAndFeedback, saveRecipeItem);
      } else if ("mealPlanner".equals(viewType)) {

      } else {
         // Default: MyRecipes context menu items
         MenuItem editItem = new MenuItem("Edit");
         MenuItem deleteItem = new MenuItem("Delete");
         MenuItem addToCollectionItem = new MenuItem("Add to Collection");
         MenuItem addToFavoritesItem = new MenuItem("Add to Favorites");
         MenuItem removeFromCollectionItem = new MenuItem("Remove from Current Collection");
         
         contextMenu.getItems().addAll(editItem, deleteItem, addToCollectionItem, addToFavoritesItem, removeFromCollectionItem);
         
         // Event handlers for MyRecipes actions.
         editItem.setOnAction(e -> myRecipesController.openEditRecipe(recipe));
         deleteItem.setOnAction(e -> myRecipesController.deleteRecipe(recipe));
         addToCollectionItem.setOnAction(e -> myRecipesController.openAddToCollectionForm(recipe));
         addToFavoritesItem.setOnAction(e -> myRecipesController.addRecipeToFavorites(recipe));
         removeFromCollectionItem.setOnAction(e -> myRecipesController.removeFromCurrentCollection(recipe));
      }
      
      // Set up mouse-click behavior.
      recipeCardPane.setOnMouseClicked(event -> {
         if (event.getButton() == MouseButton.SECONDARY) {
            contextMenu.show(recipeCardPane, event.getScreenX(), event.getScreenY());
         } else if (event.getButton() == MouseButton.PRIMARY) {
            // In the myRecipes view, call the controller's method.
            if ("myrecipes".equals(viewType)) {
               myRecipesController.showRecipeDetails(recipeId, recipeName.getText(), recipeImage.getImage(), recipe);
            } else if ("community".equals(viewType)) {
               communityRecipesController.showRecipeDetails(recipeId, recipeName.getText(), recipeImage.getImage(), recipe);
            } else if ("mealPlanner".equals(viewType)) {
               mealPlannerController.showRecipeDetails(recipeId, recipeName.getText(), recipeImage.getImage(), recipe);
            } 
         }
      });
   }

   private void RecipeReviewsAndFeedBack() {

      communityRecipesController.showRecipeReviewsAndFeedback(recipeId, recipeName.getText(), recipeImage.getImage(), recipe);
   }

   private void handleShareRecipe() {
      // Implement share functionality.
      System.out.println("Community view: Sharing recipe " + recipe.getName());
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

}