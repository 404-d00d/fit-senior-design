package org.javafx.Controllers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.javafx.Item.Item;

import org.javafx.Main.Main;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.scene.control.TextInputDialog;
import java.util.Optional;

public class InventoryDashboardController {

   @FXML
   private Button addIngredientSpacesButton, addIngredientPlacesButton, spacesButton, placesButton, menuButton, inventoryButton, myRecipesButton, inboxButton,
                  browseRecipesButton, profileButton, settingsButton, myListsButton, cancelButton, manualButton, closeIngredient, addTagButton, imageSelect, 
                  removeButton, saveButton, productIDSearchButton, addSpace, addCategory, userDashboardButton, mealPlannerButton, fridgeButton, freezerButton, 
                  pantryButton, gridViewButton, listViewButton, allFiltersButton, barcodeButton, receiptButton;

   @FXML
   private VBox menuPane, categoryContainer, spacesButtons;

   @FXML
   private TextField productID, productName, productQuantity, productTag;

   @FXML
   private ComboBox<String> productUnit, productLoc, locationFilter, categoryFilter, tagsFilter, sortByFilter;

   @FXML
   private DatePicker productEXPDate;

   @FXML
   private Pane uploadPane, spacesPane, placesPane, inventoryPane, addIngredientMenuPane;

   @FXML
   private FlowPane ingredientGrid, chipPreview;

   @FXML
   private ScrollPane gridPane;

   @FXML
   private ImageView imagePreview;

   @FXML
   private ListView<String> ingredientList;

   private boolean spaces = true;
   private boolean places = false;
   private boolean placesGrid = false; 
   private boolean placesList = true;

   private File selectedImageFile;

   private ObservableList<String> tags = FXCollections.observableArrayList();

   private ArrayList<Item> ingredientInventory = new ArrayList<Item>(); //ingredient interface 

   private Map<String, Map<String, FlowPane>> spacesCategories = new HashMap<>();
   private Map<String, List<String>> defaultCategories; // Map to hold categories for each space

   private List<String> customSpaces = new ArrayList<>();

   private Image selectedImage;

   private String currentSpace; // Track the currently selected space

   private static final String SPACES_FILE_PATH = "spacesAndCategories.json"; // File for storing spaces and categories

   private int nextIngredientID = 1;

   @FXML
   private void initialize() {

      setupDefaultCategories();

      loadPersistedSpacesAndCategories(); // Load spaces and categories from JSON
      loadIngredientInventory();
      loadCategoriesForSpace("Fridge");
      loadCategoriesForSpace("Freezer");
      loadCategoriesForSpace("Pantry");

      // Fetch ingredients from the database and update views
      List<Item> ingredients = fetchIngredientsFromDatabase();

      // Set current space to default (e.g., "Fridge")
      currentSpace = "Fridge";

      // Set up the default view for Fridge when the pane loads
      loadCategoriesForSpace("Fridge");
      styleActiveButton(fridgeButton);

      // Set up button event handlers to load categories
      fridgeButton.setOnAction(event -> {
         loadCategoriesForSpace("Fridge");
         styleActiveButton(fridgeButton);
         currentSpace = "Fridge";
      });

      freezerButton.setOnAction(event -> {
            loadCategoriesForSpace("Freezer");
            styleActiveButton(freezerButton);
            currentSpace = "Freezer";
      });

      pantryButton.setOnAction(event -> {
            loadCategoriesForSpace("Pantry");
            styleActiveButton(pantryButton);
            currentSpace = "Pantry";
      });

      productUnit.getItems().addAll("kg", "g", "l", "ml", "oz", "lbs");

      // Add the space to the productLoc ComboBox
      if (!productLoc.getItems().contains("Fridge")) {
         productLoc.getItems().add("Fridge");
      }

      if (!productLoc.getItems().contains("Freezer")) {
         productLoc.getItems().add("Freezer");
      }

      if (!productLoc.getItems().contains("Pantry")) {
         productLoc.getItems().add("Pantry");
      }

      addSpace.setOnAction(event -> addSpace());

      addCategory.setOnAction(event -> addCategory());

      gridViewButton.setOnAction(event -> {
         ingredientList.setVisible(false);
         placesGrid = true;
         gridPane.setVisible(true);
         placesList = false;

         updateIngredientGrid(fetchIngredientsFromDatabase());
      });
      setClickEffect(gridViewButton);

      listViewButton.setOnAction(event -> {
         gridPane.setVisible(false);
         placesGrid = false;
         ingredientList.setVisible(true);
         placesList = true;

         updateIngredientList(fetchIngredientsFromDatabase());
      });
      setClickEffect(listViewButton);

      //<ListView fx:id="tagsListView" /> this can be added to see them in the ui
      //Later, we can replace the ListView with a more visually appealing UI (e.g., chips or tags).
      addTagButton.setOnAction(event -> addTag());

      imageSelect.setOnAction(event -> SelectImage());

      saveButton.setOnAction(event -> {
         try {
            saveIngredient();
         } catch (Exception e) {
            e.printStackTrace();
         }
      });

      productIDSearchButton.setOnAction(event -> {
         try {
            fetchProductDetails(productID.getText());
         } catch (Exception e) {
            e.printStackTrace();
         }
      });

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

      addIngredientPlacesButton.setOnAction(event -> {
         placesPane.setVisible(false);
         places = true;
         uploadPane.setVisible(true);
      });
      
      addIngredientSpacesButton.setOnAction(event -> {
         spacesPane.setVisible(false);
         spaces = true;
         uploadPane.setVisible(true);
      });

      spacesButton.setOnAction(event -> {
         placesPane.setVisible(false);
         places = false;
         spacesPane.setVisible(true);
         spaces = true;
      });

      setClickEffect(spacesButton);

      placesButton.setOnAction(event -> {
         spacesPane.setVisible(false);
         spaces = false;
         placesPane.setVisible(true);
         places = true;

         if (placesGrid) {
            updateIngredientGrid(fetchIngredientsFromDatabase());
         } else {
            updateIngredientList(fetchIngredientsFromDatabase());
         }
      });

      setClickEffect(placesButton);

      manualButton.setOnAction(event -> {
         uploadPane.setVisible(false);
         addIngredientMenuPane.setVisible(true);
      });

      barcodeButton.setOnAction(event -> {
         uploadPane.setVisible(false);
         onBarcodeUpload();
      });

      receiptButton.setOnAction(event -> {
         uploadPane.setVisible(false);
         onReceiptUpload();
      });

      closeIngredient.setOnAction(event -> {
         try {
            addIngredientMenuPane.setVisible(false);

            if (spaces) {
               spacesPane.setVisible(true);
            }
            else if (places) {
               placesPane.setVisible(true);
            }

         } catch (Exception e) {
            e.printStackTrace();
         }
      });

      cancelButton.setOnAction(event -> {
         try {
            uploadPane.setVisible(false);

            if (spaces) {
               spacesPane.setVisible(true);
            }
            else if (places) {
               placesPane.setVisible(true);
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

      // Switch to My Lists Screen
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

      ingredientList.setCellFactory(listView -> {
         return new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                  super.updateItem(item, empty);
                  if (empty || item == null) {
                     setText(null);
                  } else {
                     setText(item);
                     setStyle("-fx-font-size: 30px;"); // Set the desired font size for placeListView
                  }
            }
         };
      });
      
   }

   // Utility method to show an alert
   private void showAlert(String title, String content) {
      Alert alert = new Alert(Alert.AlertType.WARNING);
      alert.setTitle(title);
      alert.setContentText(content);
      alert.showAndWait();
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

   private void setClickEffect(Button button) {
      button.setOnMouseClicked(this::handleMouseClicked);
   }

   private void handleMouseClicked(MouseEvent event) {

      if(spaces) {
         spacesButton.setStyle("-fx-border-color: orange; -fx-background-color: darkgrey; -fx-background-radius: 50; -fx-border-radius: 50; -fx-border-width: 4; -fx-font-size: 30;");
         placesButton.setStyle("-fx-border-color: transparent; -fx-background-color: darkgrey; -fx-background-radius: 50; -fx-border-radius: 50; -fx-border-width: 4; -fx-font-size: 30;");
      
      } else if (places) {
         spacesButton.setStyle("-fx-border-color: transparent; -fx-background-color: darkgrey; -fx-background-radius: 50; -fx-border-radius: 50; -fx-border-width: 4; -fx-font-size: 30;");
         placesButton.setStyle("-fx-border-color: orange; -fx-background-color: darkgrey; -fx-background-radius: 50; -fx-border-radius: 50; -fx-border-width: 4; -fx-font-size: 30;");
      
      } 
      
      if (placesList) {
         gridViewButton.setStyle("-fx-border-color: transparent; -fx-background-color: darkgrey; -fx-background-radius: 50; -fx-border-radius: 50; -fx-border-width: 4; -fx-font-size: 30;");
         listViewButton.setStyle("-fx-border-color: orange; -fx-background-color: darkgrey; -fx-background-radius: 50; -fx-border-radius: 50; -fx-border-width: 4; -fx-font-size: 30;");

      } else if (placesGrid) {
         gridViewButton.setStyle("-fx-border-color: orange; -fx-background-color: darkgrey; -fx-background-radius: 50; -fx-border-radius: 50; -fx-border-width: 4; -fx-font-size: 30;");
         listViewButton.setStyle("-fx-border-color: transparent; -fx-background-color: darkgrey; -fx-background-radius: 50; -fx-border-radius: 50; -fx-border-width: 4; -fx-font-size: 30;");
      
      }
   }

   // Add tag as a chip in FlowPane
   private void addTag() {
      String tag = productTag.getText().trim();
      if (!tag.isEmpty() && !tags.contains(tag)) {
         tags.add(tag);
         productTag.clear();
         updateTagView();
      }
   }

   // Update FlowPane to display chips
   private void updateTagView() {
      chipPreview.getChildren().clear();
      for (String tag : tags) {
         HBox chip = createTagChip(tag);
         chipPreview.getChildren().add(chip);
      }
   }

   // Create a chip with a delete option for each tag
   private HBox createTagChip(String tagText) {
      Label tagLabel = new Label(tagText);
      Button removeButton = new Button("X");
      
      removeButton.setOnAction(event -> {
          tags.remove(tagText);
          updateTagView();
      });

      HBox tagChip = new HBox(tagLabel, removeButton);
      tagChip.setSpacing(5);
      tagChip.setStyle("-fx-background-color: #e0e0e0; -fx-padding: 5 10; -fx-border-radius: 10; -fx-background-radius: 10;");
      return tagChip;
   }

   private void updateIngredientGrid(List<Item> ingredients) {
      // Clear the existing ingredient cards to prevent duplicates
      ingredientGrid.getChildren().clear();
  
      // Add each ingredient as a card to the FlowPane
      for (Item ingredient : ingredients) {
          try {
              // Load the FXML for the ingredient card
              FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/javafx/Resources/ingredientCard.fxml"));
              VBox ingredientCard = loader.load();
  
              // Set ingredient card details
              IngredientCardController controller = loader.getController();
              controller.setIngredientData(
                  ingredient.getID(),
                  ingredient.getName() + " - " + ingredient.getQuantity() + " " + ingredient.getUnit(),
                  null, // we need to add a image ref to the ingredients
                  this
              );
  
              // Add the card to the FlowPane
              ingredientGrid.getChildren().add(ingredientCard);
          } catch (IOException e) {
              e.printStackTrace();
          }
      }
  }

   private void updateIngredientList(List<Item> ingredients) {
      ObservableList<String> ingredientItems = FXCollections.observableArrayList();
  
      // Add each ingredient to the list
      for (Item ingredient : ingredients) {
          ingredientItems.add(ingredient.getName() + " - " + ingredient.getQuantity() + " " + ingredient.getUnit());
      }
  
      ingredientList.setItems(ingredientItems); // Set items to ListView
   }
   
   @FXML
   private void onReceiptUpload() {
      FileChooser fileChooser = new FileChooser();
      fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
      fileChooser.setTitle("Select Receipt Image(s)");
      Stage stage = (Stage) manualButton.getScene().getWindow();
      List<File> files = fileChooser.showOpenMultipleDialog(stage);

      if (files != null && !files.isEmpty()) {
         for (File file : files) {
               String absolutePathToScript = new File("src/main/python/ReceiptModule.py").getAbsolutePath();
               String imagePath = file.getAbsolutePath();

               String command = String.format("python \"%s\" \"%s\"", absolutePathToScript, imagePath);

               try {
                  ProcessBuilder pb = new ProcessBuilder(command.split(" "));
                  pb.directory(new File(System.getProperty("user.dir")));
                  pb.redirectErrorStream(true);
                  Process process = pb.start();

                  BufferedReader inputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                  StringBuilder output = new StringBuilder();
                  String line;

                  // Capture the output from the Python script
                  while ((line = inputReader.readLine()) != null) {
                     output.append(line);
                  }

                  // Print the output for debugging purposes
                  System.out.println("Python Script Output: " + output.toString());

                  // Check if output is not empty
                  if (output.length() == 0) {
                     showAlert("Error", "No response from receipt scanner.");
                     return;
                  }

                  try {
                     // Parse the JSON response from Python script
                     JsonObject jsonObject = JsonParser.parseString(output.toString()).getAsJsonObject();
                     String status = jsonObject.get("status").getAsString();

                     if ("success".equals(status)) {
                           JsonArray upcCodes = jsonObject.getAsJsonArray("upc_codes");
                           for (int i = 0; i < upcCodes.size(); i++) {
                              String upcCode = upcCodes.get(i).getAsString();
                              // Call the barcode processing function to retrieve product details
                              fetchProductDetails(upcCode);
                           }
                     } else {
                           String message = jsonObject.has("message") ? jsonObject.get("message").getAsString() : "Unknown error occurred.";
                           showAlert("Error", "An error occurred: " + message);
                     }
                  } catch (JsonSyntaxException e) {
                     showAlert("Error", "Failed to parse response from receipt scanner.");
                     e.printStackTrace();
                  }

               } catch (IOException e) {
                  e.printStackTrace();
                  showAlert("Error", "An error occurred while executing the receipt image processing.");
               }
         }
      }
   }

   @FXML
   private void onBarcodeUpload() {
      FileChooser fileChooser = new FileChooser();
      fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
      fileChooser.setTitle("Select Barcode Image(s)");
      Stage stage = (Stage) manualButton.getScene().getWindow();
      List<File> files = fileChooser.showOpenMultipleDialog(stage);

      if (files != null && !files.isEmpty()) {
         for (File file : files) {
               String absolutePathToScript = new File("src/main/python/BarcodeModule.py").getAbsolutePath();
               String imagePath = file.getAbsolutePath();

               String command = String.format("python \"%s\" \"%s\"", absolutePathToScript, imagePath);

               try {
                  ProcessBuilder pb = new ProcessBuilder(command.split(" "));
                  pb.directory(new File(System.getProperty("user.dir")));
                  pb.redirectErrorStream(true);
                  Process process = pb.start();

                  BufferedReader inputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                  StringBuilder output = new StringBuilder();
                  String line;

                  // Capture the output from the Python script
                  while ((line = inputReader.readLine()) != null) {
                     output.append(line);
                  }

                  // Print the output for debugging purposes
                  System.out.println("Python Script Output: " + output.toString());

                  // Check if output is not empty
                  if (output.length() == 0) {
                     showAlert("Error", "No response from barcode scanner.");
                     return;
                  }

                  try {
                     // Parse the JSON response from Python script
                     JsonObject jsonObject = JsonParser.parseString(output.toString()).getAsJsonObject();
                     String status = jsonObject.get("status").getAsString();

                     if ("success".equals(status)) {
                           JsonArray results = jsonObject.getAsJsonArray("results");
                           for (int i = 0; i < results.size(); i++) {
                              JsonObject result = results.get(i).getAsJsonObject();
                              String resultStatus = result.get("status").getAsString();
                              String barcode = result.get("barcode").getAsString();

                              if ("success".equals(resultStatus)) {
                                 String productName = result.get("product_name").getAsString();
                                 String quantityInfo = result.has("quantity") ? result.get("quantity").getAsString() : null;

                                 // Prefill manual form with product name and quantity
                                 prefillManualForm(productName, Integer.toString(parseQuantity(quantityInfo)), parseUnit(quantityInfo));
                              } else if ("not_found".equals(resultStatus)) {
                                 showAlert("Product Not Found", "No product information found for barcode: " + barcode + ", please enter manually.");
                              } else if ("error".equals(resultStatus)) {
                                 String message = result.get("message").getAsString();
                                 showAlert("Error", "An error occurred: " + message);
                              }
                           }
                     } else {
                           String message = jsonObject.has("message") ? jsonObject.get("message").getAsString() : "Unknown error occurred.";
                           showAlert("Error", "An error occurred: " + message);
                     }
                  } catch (JsonSyntaxException e) {
                     showAlert("Error", "Failed to parse response from barcode scanner.");
                     e.printStackTrace();
                  }

               } catch (IOException e) {
                  e.printStackTrace();
                  showAlert("Error", "An error occurred while executing the barcode image processing.");
               }
         }
      }
   }

   // Helper method to parse quantity and unit from the quantity string
   private Integer parseQuantity(String quantityInfo) {
      if (quantityInfo == null) return null;

      try {
         // Try to extract a number from the quantity information
         String quantityString = quantityInfo.split(" ")[0]; // Assuming quantity is the first word
         return Integer.parseInt(quantityString);
      } catch (NumberFormatException e) {
         // If we can't parse the quantity, return null
         return null;
      }
   }

   private String parseUnit(String quantityInfo) {
      if (quantityInfo == null) return null;

      String[] parts = quantityInfo.split(" ");
      return (parts.length > 1) ? parts[1] : null; // Return unit if available
   }

   private void fetchProductDetails(String upc) {
      try {
          ProcessBuilder pb = new ProcessBuilder("python", "src/main/python/get_product_details.py", upc);
          Process process = pb.start();

          BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
          String jsonResponse = in.lines().collect(Collectors.joining());
          process.waitFor();

          if (!jsonResponse.isEmpty()) {
              Gson gson = new Gson();
              JsonObject jsonObject = gson.fromJson(jsonResponse, JsonObject.class);
              String name = jsonObject.get("name").getAsString();
              String quantity = jsonObject.get("quantity").getAsString();
              String unit = jsonObject.get("unit").getAsString();

              Item item = new Item(name, 0, Integer.parseInt(quantity), unit, null, null);
              prefillManualForm(item.getName(), Integer.toString(item.getQuantity()), item.getUnit());
          }
      } catch (Exception e) {
          e.printStackTrace();
      }
  }


   // Helper method to style the active space button
   private void styleActiveButton(Button activeButton) {
      // Reset styles for all space buttons within the VBox
      for (Node node : spacesButtons.getChildren()) {
          if (node instanceof Button) {
              ((Button) node).setStyle("-fx-background-color: darkgrey; -fx-background-radius: 50; -fx-border-radius: 50; -fx-border-width: 4; -fx-font-size: 30;");
          }
      }
  
      // Highlight the active button
      activeButton.setStyle("-fx-background-color: orange; -fx-background-radius: 50; -fx-border-color: black; -fx-border-radius: 50; -fx-border-width: 4; -fx-font-size: 30;");
  }

   private void prefillManualForm(String itemName, String quantity, String unit) {
      productName.setText(itemName != null ? itemName : "");
      productQuantity.setText(quantity != null ? quantity : "");
      productUnit.setValue(unit != null ? unit : "");
      addIngredientMenuPane.setVisible(true);
      uploadPane.setVisible(false);
   }

   private void saveIngredient() {

      if (isFormValid()) {
         // Get values from input fields
         String ingredientName = productName.getText();
         String quantity = productQuantity.getText();
         String unit = productUnit.getValue();
         String location = productLoc.getValue();
         LocalDate expirationDate = productEXPDate.getValue();
         String convertedDate = expirationDate.toString();

         // Format date for display
         //DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
         //String expirationDateString = expirationDate.format(formatter);

         // Create new ingredient item and add to inventory
         Item newIngredient = new Item(ingredientName, 0, Integer.parseInt(quantity), unit, location, convertedDate);
         
         addIngredientToDatabase(newIngredient);

         try {
            // Load the FXML for the ingredient card
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/javafx/Resources/ingredientCard.fxml"));
            VBox ingredientCard = loader.load();

            // Set ingredient card details
            IngredientCardController controller = loader.getController();
            controller.setIngredientData(newIngredient.getID(), ingredientName + " - " + quantity + " " + unit, selectedImage, this);

            // Store the controller in the properties map for later access
            ingredientCard.getProperties().put("controller", controller);

            Map<String, FlowPane> locationCategories = spacesCategories.get(location);

           // 1. Add ingredient to "All Items" by default
           FlowPane allItemsPane = locationCategories.get("All Items");
           if (allItemsPane != null) {
               allItemsPane.getChildren().add(createDuplicateNode(ingredientCard));
           }

           // 2. Determine additional categories based on tags and expiration date
           List<String> categories = tags.stream().map(String::toLowerCase).collect(Collectors.toList());
           if (expirationDate != null && expirationDate.isBefore(LocalDate.now().plusDays(3))) {
               categories.add("expiring soon");
           }

            // Add ingredient to each relevant category, matching case-insensitively
            for (Map.Entry<String, FlowPane> entry : locationCategories.entrySet()) {
               String category = entry.getKey().toLowerCase(); // Convert category name to lowercase
               FlowPane categoryPane = entry.getValue();
               
               if (categories.contains(category)) {
                  categoryPane.getChildren().add(createDuplicateNode(ingredientCard));
               }
            }

            // Clear the input fields after saving
            productName.clear();
            productQuantity.clear();
            productUnit.setValue(null);
            productLoc.setValue(null);
            productEXPDate.setValue(null);
            tags.clear();

            // Hide add ingredient pane and show main inventory view
            addIngredientMenuPane.setVisible(false);
            if (spaces) {
               spacesPane.setVisible(true);
            } else if (places) {
               placesPane.setVisible(true);
            }

         } catch (IOException e) {
             e.printStackTrace();
         }
      } else {
         // Show alert if the form is not valid
         Alert alert = new Alert(AlertType.ERROR);
         alert.setTitle("Form Error");
         alert.setHeaderText(null);
         alert.setContentText("Please fill in all required fields.");
         alert.showAndWait();
     }
   }

   private VBox createIngredientCard(Item ingredient) {
    try {
        // Load the FXML for the ingredient card
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/javafx/Resources/ingredientCard.fxml"));
        VBox ingredientCard = loader.load();

        // Set ingredient card details
        IngredientCardController controller = loader.getController();
        controller.setIngredientData(
            ingredient.getID(),
            ingredient.getName() + " - " + ingredient.getQuantity() + " " + ingredient.getUnit(),
            null, // Assuming image is null for now
            this
        );

        return ingredientCard;
    } catch (IOException e) {
        e.printStackTrace();
        return null;
    }
}

   // Helper method to duplicate the ingredient card
   private Node createDuplicateNode(VBox originalCard) {
      try {
         // Load the FXML again to create a fresh duplicate
         FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/javafx/Resources/ingredientCard.fxml"));
         VBox duplicateCard = loader.load();

         // Transfer data from the original card to the duplicate
         IngredientCardController originalController = (IngredientCardController) originalCard.getProperties().get("controller");
         IngredientCardController duplicateController = loader.getController();
         
         // Set the same data on the duplicate
         duplicateController.setIngredientData(originalController.getIngredientId(), 
                                                originalController.getIngredientName(), 
                                                originalController.getImage(), 
                                                this);
         return duplicateCard;
      } catch (IOException e) {
         e.printStackTrace();
         return null;
      }
   }

   private boolean isFormValid() {
      return !productName.getText().trim().isEmpty() && !productQuantity.getText().trim().isEmpty();
   }

   private void SelectImage() {
      FileChooser fileChooser = new FileChooser();
      fileChooser.setTitle("Select Ingredient Image");

      // Restrict to image file types
      fileChooser.getExtensionFilters().add(
         new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
      );

      Stage stage = (Stage) imageSelect.getScene().getWindow();  // Get the current stage
      selectedImageFile = fileChooser.showOpenDialog(stage);

      if (selectedImageFile != null) {
         // Convert file to Image
        selectedImage = new Image(selectedImageFile.toURI().toString());
        imagePreview.setImage(selectedImage);
      }
   }

   private String barcodePythonScript(String imagePath) {
      try {
          ProcessBuilder pb = new ProcessBuilder("python", "src/main/python/BarcodeModule.py", imagePath);
          Process process = pb.start();

          BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
          String result = in.readLine();
          process.waitFor();
          return result;
      } catch (Exception e) {
          e.printStackTrace();
          return null;
      }
   }

   private List<Item> receiptPythonScript(String imagePath) {
      try {
          ProcessBuilder pb = new ProcessBuilder("python", "src/main/python/ReceiptModule.py", imagePath);
          Process process = pb.start();

          BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
          String jsonResponse = in.lines().collect(Collectors.joining());
          process.waitFor();

          if (!jsonResponse.isEmpty()) {
              Gson gson = new Gson();
              Type itemListType = new TypeToken<List<Item>>() {}.getType();
              return gson.fromJson(jsonResponse, itemListType);
          }
      } catch (Exception e) {
          e.printStackTrace();
      }
      return null;
   }


   // Method to open the edit form with the recipe's current details
   public void openEditIngriedent(int ingredientId) {
      // Logic to open edit pane and load recipe details by ID
      System.out.println("Edit recipe with ID: " + ingredientId);
   }

   // Method to delete the recipe by ID
   public void deleteIngriendent(int ingredientId) {
      // Logic to remove the recipe from data source and refresh UI
      System.out.println("Delete recipe with ID: " + ingredientId);

      // Remove from database, then remove from frontend

      // Access and remove the recipe card widget

      //work on having this remove the card from all categories it might be apart of

      //VBox ingredientCard = recipeWidgets.get(ingredientId);
      //if (ingredientCard != null) {
          //recipeFlowPane.getChildren().remove(ingredientCard);
          //recipeWidgets.remove(ingredientId);  // Clean up from the map
      //}
   }

   private void addSpace() {
      TextInputDialog dialog = new TextInputDialog();
      dialog.setTitle("Add New Space");
      dialog.setHeaderText("Enter the name for the new space:");
      dialog.setContentText("Space Name:");
  
      Optional<String> result = dialog.showAndWait();
      result.ifPresent(name -> {
          String spaceName = name.trim();
          if (!spaceName.isEmpty() && !customSpaces.contains(spaceName) && !spacesCategories.containsKey(spaceName)) {
              customSpaces.add(spaceName);
              productLoc.getItems().add(spaceName);
              addSpaceButton(spaceName);
  
              // Add initial categories for the new space
              Map<String, FlowPane> initialCategories = new HashMap<>();
              initialCategories.put("All Items", createCategoryPane("All Items"));
              initialCategories.put("Expiring Soon", createCategoryPane("Expiring Soon"));
              spacesCategories.put(spaceName, initialCategories);
  
              savePersistedSpacesAndCategories();
          } else {
              showAlert("Invalid Space", "Space already exists or is empty.");
          }
      });
  }

   private void addCategory() {
      if (currentSpace == null) {
         showAlert("No Space Selected", "Please select a space before adding a category.");
         return;
      }

      // Use a single-element array to make the reference "effectively final"
      final Map<String, FlowPane>[] categoriesWrapper = new Map[]{spacesCategories.get(currentSpace)};
      if (categoriesWrapper[0] == null) {
         categoriesWrapper[0] = new HashMap<>();
         spacesCategories.put(currentSpace, categoriesWrapper[0]);
      }

      // Show dialog to get category name from the user
      TextInputDialog dialog = new TextInputDialog();
      dialog.setTitle("Add New Category");
      dialog.setHeaderText("Enter the name of the new category:");

      Optional<String> result = dialog.showAndWait();
      result.ifPresent(categoryName -> {
         // Add category if it doesn't already exist
         if (!categoriesWrapper[0].containsKey(categoryName)) {
            FlowPane newCategoryPane = createCategoryPane(categoryName);
            categoriesWrapper[0].put(categoryName, newCategoryPane);

            // Remove placeholder if it exists
            categoryContainer.getChildren().removeIf(node -> node instanceof Text && ((Text) node).getText().equals("No categories available. Please add categories."));

            // Update UI
            Text categoryHeader = new Text(categoryName);
            categoryHeader.setFont(new Font("System Bold", 36));
            categoryHeader.setStyle("-fx-fill: #333;");
            categoryContainer.getChildren().addAll(categoryHeader, newCategoryPane);

            // Persist changes
            savePersistedSpacesAndCategories();
         } else {
            showAlert("Category Exists", "A category with this name already exists in the current space.");
         }
      });
   }

   private void addSpaceButton(String spaceName) {
      Button newSpaceButton = new Button(spaceName);
      newSpaceButton.setStyle("-fx-background-color: darkgrey; -fx-background-radius: 50; -fx-border-radius: 50; -fx-border-width: 4; -fx-font-size: 30; -fx-text-fill: black;");
      newSpaceButton.setPrefHeight(60);
      newSpaceButton.setPrefWidth(346);

      newSpaceButton.setOnAction(event -> {
         loadCategoriesForSpace(spaceName);
         styleActiveButton(newSpaceButton);
         currentSpace = spaceName;
      });

      // Add context menu for deletion
      newSpaceButton.setOnContextMenuRequested(event -> {
         Alert alert = new Alert(AlertType.CONFIRMATION);
         alert.setTitle("Delete Space");
         alert.setHeaderText("Are you sure you want to delete this space?");
         alert.setContentText(spaceName);
         Optional<ButtonType> result = alert.showAndWait();
         if (result.isPresent() && result.get() == ButtonType.OK) {
               deleteSpace(spaceName);
         }
      });

      spacesButtons.getChildren().add(newSpaceButton);
   }

   // Method to delete a space
   private void deleteSpace(String spaceName) {
      // Remove the space button from the UI
      spacesButtons.getChildren().removeIf(node -> node instanceof Button && ((Button) node).getText().equals(spaceName));
      
      // Remove the space from the spacesCategories map and customSpaces list
      spacesCategories.remove(spaceName);
      customSpaces.remove(spaceName);
      
      // Persist changes by saving the updated JSON
      savePersistedSpacesAndCategories();
   }

   private void loadPersistedSpacesAndCategories() {
      File file = new File(SPACES_FILE_PATH);
      boolean isFileValid = file.exists() && file.length() > 0;
  
      if (isFileValid) {
          try (Reader reader = new FileReader(file)) {
              JsonElement jsonElement = JsonParser.parseReader(reader);
              if (jsonElement.isJsonObject()) {
                  JsonObject jsonObject = jsonElement.getAsJsonObject();
  
                  // Load spaces (custom and default)
                  JsonArray spacesArray = jsonObject.getAsJsonArray("spaces");

                  System.out.println(productLoc.getItems());

                  for (JsonElement spaceElement : spacesArray) {
                     String spaceName = spaceElement.getAsString();
  
                     // Add the space if it doesn't exist
                     if (!spacesCategories.containsKey(spaceName)) {
                        spacesCategories.put(spaceName, new HashMap<>());
                     }

                     if (!customSpaces.contains(spaceName) && !spaceName.equals("Fridge") && !spaceName.equals("Freezer") && !spaceName.equals("Pantry")) {
                        customSpaces.add(spaceName);
                     }
  
                     if (!spaceExists(spaceName)) {
                        addSpaceButton(spaceName);
                     }

                     // Add the space to the productLoc ComboBox
                     if (!productLoc.getItems().contains(spaceName)) {
                        productLoc.getItems().add(spaceName);
                     }
                  }
  
                  // Load categories for each space
                  JsonObject categoriesObject = jsonObject.getAsJsonObject("categories");
                  for (Map.Entry<String, JsonElement> entry : categoriesObject.entrySet()) {
                     String spaceName = entry.getKey();
                     JsonArray categoriesArray = entry.getValue().getAsJsonArray();
                     // Ensure categories map exists for the given space
                     Map<String, FlowPane> categories = spacesCategories.computeIfAbsent(spaceName, k -> new HashMap<>());
  
                     // Ensure categories are only loaded once to avoid duplicates
                     for (JsonElement categoryElement : categoriesArray) {
                        String categoryName = categoryElement.getAsString();
                        if (!categories.containsKey(categoryName)) {
                           categories.put(categoryName, createCategoryPane(categoryName));
                        }
                     }
                  }
              }
          } catch (IOException e) {
              e.printStackTrace();
          }
      } else {
          // If the file doesn't exist or is empty, set up default spaces and categories
          setupDefaultSpacesAndCategories();
          savePersistedSpacesAndCategories(); // Save defaults after initialization
      }
   }

   // Helper method to check if a space button already exists in the UI
   private boolean spaceExists(String spaceName) {
      for (Node node : spacesButtons.getChildren()) {
         if (node instanceof Button && ((Button) node).getText().equals(spaceName)) {
            return true;
         }
      }
      return false;
   }

   private void savePersistedSpacesAndCategories() {
      try (Writer writer = new FileWriter(SPACES_FILE_PATH)) {
          JsonObject jsonObject = new JsonObject();
  
          // Write default spaces first, then custom spaces
          JsonArray spacesArray = new JsonArray();
          List<String> orderedSpaces = List.of("Fridge", "Freezer", "Pantry");
          orderedSpaces.forEach(spacesArray::add);
          customSpaces.stream().filter(space -> !orderedSpaces.contains(space)).forEach(spacesArray::add);
          jsonObject.add("spaces", spacesArray);
  
          // Save categories for each space in correct order
          JsonObject categoriesObject = new JsonObject();
          for (Map.Entry<String, Map<String, FlowPane>> entry : spacesCategories.entrySet()) {
              String spaceName = entry.getKey();
              Map<String, FlowPane> categoriesMap = entry.getValue();
  
              // Ensure "All Items" and "Expiring Soon" are first
              JsonArray categoriesArray = new JsonArray();
              if (categoriesMap.containsKey("All Items")) {
                  categoriesArray.add("All Items");
              }
              if (categoriesMap.containsKey("Expiring Soon")) {
                  categoriesArray.add("Expiring Soon");
              }
  
              // Add remaining categories in alphabetical order (or original order)
              categoriesMap.keySet().stream()
                  .filter(category -> !category.equals("All Items") && !category.equals("Expiring Soon"))
                  .forEach(categoriesArray::add);
  
              categoriesObject.add(spaceName, categoriesArray);
          }
          jsonObject.add("categories", categoriesObject);
  
          // Write to the file
          Gson gson = new GsonBuilder().setPrettyPrinting().create();
          gson.toJson(jsonObject, writer);
  
      } catch (IOException e) {
          e.printStackTrace();
      }
  } 

   private void setupDefaultCategories() {
      defaultCategories = new HashMap<>();

      // Define default categories for each space
      defaultCategories.put("Fridge", List.of("All Items", "Expiring Soon", "Ready to Eat", "Dairy", "Meat & Seafood"));
      defaultCategories.put("Freezer", List.of("All Items","Expiring Soon", "Frozen Foods", "Meat & Seafood", "Vegetables"));
      defaultCategories.put("Pantry", List.of("All Items", "Expiring Soon", "Canned Goods", "Dry Foods", "Condiments"));
   }

   private void loadCategoriesForSpace(String space) {
      // Clear existing categories in the VBox
      categoryContainer.getChildren().clear();
  
      // Retrieve or initialize categories for the specified space
      Map<String, FlowPane> categories = spacesCategories.get(space);
  
      if (categories == null) {
          categories = new LinkedHashMap<>(); // Use LinkedHashMap to maintain order
          for (String category : defaultCategories.getOrDefault(space, List.of())) {
              categories.put(category, createCategoryPane(category));
          }
          spacesCategories.put(space, categories);
          savePersistedSpacesAndCategories(); // Save changes to keep things consistent
      }
  
      // Create a list of category names in the correct order
      List<String> orderedCategories = new ArrayList<>();
      if (categories.containsKey("All Items")) {
          orderedCategories.add("All Items");
      }
      if (categories.containsKey("Expiring Soon")) {
          orderedCategories.add("Expiring Soon");
      }
      // Add remaining categories in their natural order or as specified in the defaultCategories
      for (String category : categories.keySet()) {
          if (!category.equals("All Items") && !category.equals("Expiring Soon")) {
              orderedCategories.add(category);
          }
      }
  
      // Add each category to the UI in the correct order
      for (String categoryName : orderedCategories) {
          FlowPane categoryPane = categories.get(categoryName);
  
          // Create a header for the category
          Text categoryHeader = new Text(categoryName);
          categoryHeader.setFont(new Font("System Bold", 36));
          categoryHeader.setStyle("-fx-fill: #333;");
  
          // Add context menu for deletion
          categoryHeader.setOnContextMenuRequested(event -> {
              Alert alert = new Alert(AlertType.CONFIRMATION);
              alert.setTitle("Delete Category");
              alert.setHeaderText("Are you sure you want to delete this category?");
              alert.setContentText(categoryName);
              Optional<ButtonType> result = alert.showAndWait();
              if (result.isPresent() && result.get() == ButtonType.OK) {
                  deleteCategory(space, categoryName);
              }
          });
  
          // Add the header and FlowPane to the VBox container
          categoryContainer.getChildren().addAll(categoryHeader, categoryPane);
      }
  
      // If no categories were found, add a placeholder message
      if (categories.isEmpty()) {
          Text placeholder = new Text("No categories available. Please add categories.");
          placeholder.setFont(new Font("System Bold", 24));
          placeholder.setStyle("-fx-fill: #777;");
          categoryContainer.getChildren().add(placeholder);
      }
  }
   
   // Method to delete a category
   private void deleteCategory(String spaceName, String categoryName) {
      // Retrieve the categories for the given space
      Map<String, FlowPane> categories = spacesCategories.get(spaceName);
  
      if (categories != null && categories.containsKey(categoryName)) {
         // Remove the category from the map
         categories.remove(categoryName);

         // Persist changes by saving the updated JSON
         savePersistedSpacesAndCategories();

         // Refresh the UI after deletion
         loadCategoriesForSpace(spaceName);
      }
  }

   private FlowPane createCategoryPane(String categoryName) {
      FlowPane categoryPane = new FlowPane();
      categoryPane.setPrefHeight(200);
      categoryPane.setPrefWidth(1400);
      categoryPane.setStyle("-fx-background-color: lightgray; -fx-hgap: 10; -fx-vgap: 10;");
      return categoryPane;
  }

   private void setupDefaultSpacesAndCategories() {
      for (Map.Entry<String, List<String>> entry : defaultCategories.entrySet()) {
         String spaceName = entry.getKey();
         List<String> categoriesList = entry.getValue();

         // Add space button if it doesn't exist already
         if (!spaceExists(spaceName)) {
               addSpaceButton(spaceName);

               // Use LinkedHashMap to ensure order is maintained
               Map<String, FlowPane> categories = new LinkedHashMap<>();
               for (String category : categoriesList) {
                  categories.put(category, createCategoryPane(category));
               }
               spacesCategories.put(spaceName, categories);
         }
      }
   }

   private void loadIngredientInventory() {
      File file = new File("itemInventory.json");
  
      if (file.exists() && file.length() > 0) { // Check if the file exists and is not empty
          try (Reader reader = new FileReader(file)) {
              Gson gson = new Gson();
              Item[] items = gson.fromJson(reader, Item[].class); // Deserialize JSON to an array of Items
              ingredientInventory.clear(); // Clear the existing inventory
              ingredientInventory.addAll(List.of(items)); // Add all items to the inventory list
  
              // Determine the highest ID
              int maxID = 0;
              for (Item item : ingredientInventory) {
                  maxID = Math.max(maxID, item.getID());
              }
              nextIngredientID = maxID + 1; // Set the next ID
              System.out.println("Inventory successfully loaded from JSON file.");
          } catch (IOException e) {
              e.printStackTrace();
              showAlert("Load Error", "Failed to load inventory from JSON file.");
          }
      } else {
          System.out.println("No inventory file found. Starting with an empty inventory.");
      }
  }
  

   private void addIngredientToDatabase(Item item) {
      // Placeholder for adding the ingredient to a database

      item.setID(nextIngredientID);
      nextIngredientID++;
      ingredientInventory.add(item);
      File file = new File("itemInventory.json");

      try {
          // Read existing items from the file
          List<Item> existingItems = new ArrayList<>();
          if (file.exists() && file.length() > 0) {
              try (Reader reader = new FileReader(file)) {
                  Gson gson = new Gson();
                  Item[] items = gson.fromJson(reader, Item[].class);
                  if (items != null) {
                      existingItems = new ArrayList<>(List.of(items));
                  }
              }
          }
  
          // Add the new item to the list
          existingItems.add(item);
  
          // Write the updated list back to the file
          try (Writer writer = new FileWriter(file)) {
              Gson gson = new GsonBuilder().setPrettyPrinting().create();
              gson.toJson(existingItems, writer);
              //System.out.println("Item added to inventory: " + item.getName());
          }
  
      } catch (IOException e) {
          e.printStackTrace();
          showAlert("Save Error", "Failed to save item to inventory.");
      }
      // In the future, replace this with an actual database call.
  }
      
  
   private List<Item> fetchIngredientsFromDatabase() {
      try (Reader reader = new FileReader("itemInventory.json")) {
          Gson gson = new Gson();
          Type itemListType = new TypeToken<List<Item>>() {}.getType();
          ingredientInventory = gson.fromJson(reader, itemListType);
  
          if (ingredientInventory == null) {
              ingredientInventory = new ArrayList<>();
          }
  
          System.out.println("Inventory loaded from JSON file.");
      } catch (IOException e) {
          e.printStackTrace();
          showAlert("Load Error", "Failed to load inventory from JSON file.");
      }
  
      // Update categories for spaces after loading ingredients
      for (Item ingredient : ingredientInventory) {
          String location = ingredient.getLocation();
          Map<String, FlowPane> locationCategories = spacesCategories.get(location);
          
          if (locationCategories != null) {
              // Add ingredient to "All Items" category
              FlowPane allItemsPane = locationCategories.get("All Items");
              if (allItemsPane != null) {
                  allItemsPane.getChildren().add(createIngredientCard(ingredient));
              }
  
              // Add ingredient to other categories based on tags or expiration date
              List<String> categories = new ArrayList<>();
              if (ingredient.getExpirDate() != null && LocalDate.parse(ingredient.getExpirDate()).isBefore(LocalDate.now().plusDays(3))) {
                  categories.add("Expiring Soon");
              }
  
              // Add ingredient to the appropriate categories
              for (String category : categories) {
                  FlowPane categoryPane = locationCategories.get(category);
                  if (categoryPane != null) {
                      categoryPane.getChildren().add(createIngredientCard(ingredient));
                  }
              }
          }
      }
  
      return ingredientInventory;
  }

}
