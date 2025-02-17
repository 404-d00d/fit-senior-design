package org.javafx.Controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.javafx.Main.Main;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;

public class CommunityRecipesController {

   @FXML
   private ComboBox<String> sortBy, ingredientDropDown, categoryDropDown, tagsDropDown;

   @FXML
   private Button addMealButton, userDashboardButton, mealPlannerButton, myRecipesButton, inventoryButton,
                  inboxButton, browseRecipesButton, profileButton, settingsButton, myListsButton, menuButton;

   @FXML
   private Pane menuPane;

   @FXML private FlowPane recipeFlowPane;

   private DynamoDbClient database;
   private AwsBasicCredentials awsCreds ;
   private Map<String, AttributeValue> item = new HashMap<>();

   private void initializeDatabase() {
      try {
          Region region = Region.US_EAST_1;
          database = DynamoDbClient.builder()
              .region(region)
              .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
              .build();
      } catch (Exception e) {
          System.err.println("Failed to initialize DynamoDB client: " + e.getMessage());
          e.printStackTrace();
      }
   }

   @FXML
   private void initialize() {

      initializeDatabase();

      item.put("Recipe", AttributeValue.builder().s(Integer.toString(0)).build());
      item.put(Integer.toString(0), AttributeValue.builder().s("test").build());
      PutItemRequest request = PutItemRequest.builder().tableName("Recipes").item(item).build();
      database.putItem(request);

      fetchCommunityRecipes();
      
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
            Main.showCommunityRecipesScreen();
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
      button.setStyle("-fx-background-color: orange; -fx-text-fill: white; -fx-wrap-text: true; -fx-font-size: 40px;");
   }

   private void handleMouseExited(MouseEvent event) {
      Button button = (Button) event.getSource();
      // Reset style when mouse exits
      button.setStyle("-fx-background-color: transparent; -fx-text-fill: black; -fx-wrap-text: true; -fx-font-size: 40px;");
   }

   private void fetchCommunityRecipes() {
      try {
          ScanRequest scanRequest = ScanRequest.builder()
              .tableName("CommunityRecipes")
              .build();
  
          ScanResponse scanResponse = database.scan(scanRequest);
          List<Map<String, AttributeValue>> items = scanResponse.items();
  
          for (Map<String, AttributeValue> item : items) {
              String recipeName = item.get("RecipeName").s();
              String description = item.get("Description").s();
  
              // Create Recipe Card
              HBox recipeCard = new HBox();
              recipeCard.setSpacing(10);
              recipeCard.setPadding(new Insets(10));
              recipeCard.setStyle("-fx-background-color: #f0f0f0; -fx-border-radius: 5px; -fx-padding: 10px;");
  
              Label nameLabel = new Label(recipeName);
              nameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
  
              Label descLabel = new Label(description);
              descLabel.setWrapText(true);
              descLabel.setMaxWidth(400);
  
              recipeCard.getChildren().addAll(nameLabel, descLabel);
  
              Platform.runLater(() -> recipeFlowPane.getChildren().add(recipeCard));
          }
  
      } catch (DynamoDbException e) {
          System.err.println("Failed to fetch recipes: " + e.getMessage());
      }
  }
  

}
