package org.javafx.Controllers;

import org.javafx.Main.Main;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

public class RecipeCardController {

   @FXML
   private ImageView recipeImage;
   @FXML
   private Label recipeName;
   @FXML
   private StackPane recipeCardPane;

   private int recipeId;

   

   public void setRecipeData(int id, String name, Image image) {
      this.recipeId = id;
      recipeName.setText(name);
      recipeImage.setImage(image);
   }

   @FXML
   private void initialize() {
      recipeCardPane.setOnMouseClicked(event -> {

         // When clicked, show details in MyRecipesController
         MyRecipesController.showRecipeDetails(recipeId, recipeName.getText(), recipeImage.getImage());
      });
   }
}