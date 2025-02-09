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
import java.util.HashSet;
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
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
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
import javafx.scene.control.ProgressBar;
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
import java.util.Set;

public class InventoryDashboardController {

   @FXML
   private Button addIngredientSpacesButton, addIngredientPlacesButton, spacesButton, placesButton, menuButton, inventoryButton, myRecipesButton, inboxButton,
                  browseRecipesButton, profileButton, settingsButton, myListsButton, cancelButton, manualButton, closeIngredient, addTagButton, imageSelect, 
                  removeButton, saveButton, productIDSearchButton, addSpace, addCategory, userDashboardButton, mealPlannerButton, fridgeButton, freezerButton, 
                  pantryButton, gridViewButton, listViewButton, allFiltersButton, barcodeButton, receiptButton, clearFilters;

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
   private ProgressBar progressBar;

   @FXML
   private Label tipsLabel;

   @FXML
   private Text loadingTXT;

   private final List<String> tips = List.of(
    "Did you know? Scanned items can be categorized automatically!",
    "Tip: You can manually add items by clicking the 'Add Ingredient' button.",
    "Ensure the barcode or receipt image is clear for better accuracy.",
    "Organize your ingredients by tags for quick access!",
    "Pro Tip: Expiring soon items are highlighted for easy identification."
   );

   @FXML
   private void initialize() {

      setupDefaultCategories();

      loadPersistedSpacesAndCategories(); // Load spaces and categories from JSON
      loadIngredientInventory();

      loadCategoriesForSpace("Fridge");
      loadCategoriesForSpace("Freezer");
      loadCategoriesForSpace("Pantry");

      // Update Spaces View
      updateSpacesItemCards();

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
      sortByFilter.getItems().addAll("A-Z", "Z-A", "Quantity", "Expiration Date");

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

         updateIngredientGrid(ingredientInventory);
      });
      setClickEffect(gridViewButton);

      listViewButton.setOnAction(event -> {
         gridPane.setVisible(false);
         placesGrid = false;
         ingredientList.setVisible(true);
         placesList = true;

         updateIngredientList(ingredientInventory);
      });
      setClickEffect(listViewButton);

      //<ListView fx:id="tagsListView" /> this can be added to see them in the ui
      //Later, we can replace the ListView with a more visually appealing UI (e.g., chips or tags).
      addTagButton.setOnAction(event -> addTag());

      imageSelect.setOnAction(event -> SelectImage());

      // Populate filter combo boxes with initial data
      populateFilterOptions();

      // Set event handlers for filters
      locationFilter.setOnAction(event -> applyFilters());
      categoryFilter.setOnAction(event -> applyFilters());
      tagsFilter.setOnAction(event -> applyFilters());
      clearFilters.setOnAction(event -> clearAllFilters());

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
         uploadPane.setVisible(false);
      });

      setClickEffect(spacesButton);

      placesButton.setOnAction(event -> {
         uploadPane.setVisible(false);
         spacesPane.setVisible(false);
         spaces = false;
         placesPane.setVisible(true);
         places = true;

         if (placesGrid) {
            updateIngredientGrid(ingredientInventory);
         } else {
            updateIngredientList(ingredientInventory);
         }
      });

      setClickEffect(placesButton);

      sortByFilter.setOnAction(event -> handleSorting());

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

   /*
    * Filters
   */

   private void populateFilterOptions() {
      // Populate location filter
      locationFilter.getItems().clear();
      locationFilter.getItems().add("All Locations");
      locationFilter.getItems().addAll(productLoc.getItems());
  
      // Populate category filter
      categoryFilter.getItems().clear();
      categoryFilter.getItems().add("All Categories");
      defaultCategories.values().forEach(categories -> categoryFilter.getItems().addAll(categories));
  
      // Populate tags filter
      tagsFilter.getItems().clear();
      tagsFilter.getItems().add("All Tags");
      tags.forEach(tagsFilter.getItems()::add);
   }
   
   private void applyFilters() {
      String selectedLocation = locationFilter.getValue();
      String selectedCategory = categoryFilter.getValue();
      String selectedTag = tagsFilter.getValue();
  
      List<Item> filteredItems = new ArrayList<>(ingredientInventory);
  
      // Filter by location
      if (selectedLocation != null && !"All Locations".equals(selectedLocation)) {
          filteredItems = filteredItems.stream()
              .filter(item -> selectedLocation.equals(item.getLocation()))
              .collect(Collectors.toList());
      }
  
      // Filter by category
      if (selectedCategory != null && !"All Categories".equals(selectedCategory)) {
          filteredItems = filteredItems.stream()
              .filter(item -> item.getTags() != null && item.getTags().contains(selectedCategory.toLowerCase()))
              .collect(Collectors.toList());
      }
  
      // Filter by tag
      if (selectedTag != null && !"All Tags".equals(selectedTag)) {
          filteredItems = filteredItems.stream()
              .filter(item -> item.getTags() != null && item.getTags().contains(selectedTag.toLowerCase()))
              .collect(Collectors.toList());
      }
  
      // Update the UI with filtered results
      if (placesGrid) {
          updateIngredientGrid(filteredItems);
      } else if (placesList) {
          updateIngredientList(filteredItems);
      }
  }
   
   private void clearAllFilters() {
         locationFilter.setValue("All Locations");
         categoryFilter.setValue("All Categories");
         tagsFilter.setValue("All Tags");
         applyFilters();
   }
      

   /*
   *  Unorganized Things
   */

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

   private void handleSorting() {
      String selectedSort = sortByFilter.getValue();
      if (selectedSort == null) return;
  
      // Sort the `ingredientInventory` list based on the selected criteria
      switch (selectedSort) {
          case "A-Z":
              ingredientInventory.sort((i1, i2) -> i1.getName().compareToIgnoreCase(i2.getName()));
              break;
          case "Z-A":
              ingredientInventory.sort((i1, i2) -> i2.getName().compareToIgnoreCase(i1.getName()));
              break;
          case "Quantity":
              ingredientInventory.sort((i1, i2) -> Integer.compare(i2.getQuantity(), i1.getQuantity()));
              break;
          case "Expiration Date":
              ingredientInventory.sort((i1, i2) -> {
                  if (i1.getExpirDate() == null) return 1;
                  if (i2.getExpirDate() == null) return -1;
                  return LocalDate.parse(i1.getExpirDate()).compareTo(LocalDate.parse(i2.getExpirDate()));
              });
              break;
          default:
              break;
      }
  
      // Refresh the UI after sorting
      if (placesGrid) {
          updateIngredientGrid(ingredientInventory);
      } else if (placesList) {
          updateIngredientList(ingredientInventory);
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

      // Maintain a set of IDs to avoid duplicates
      Set<Integer> addedItemIds = new HashSet<>();

      for (Item ingredient : ingredients) {
         if (!addedItemIds.contains(ingredient.getID())) {
               addedItemIds.add(ingredient.getID());
               try {
                  FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/javafx/Resources/FXMLs/ingredientCard.fxml"));
                  VBox ingredientCard = loader.load();

                  IngredientCardController controller = loader.getController();
                  controller.setIngredientData(
                     ingredient.getID(),
                     ingredient.getName() + " - " + ingredient.getQuantity() + " " + ingredient.getUnit(),
                     null,
                     this
                  );

                  ingredientGrid.getChildren().add(ingredientCard);
               } catch (IOException e) {
                  e.printStackTrace();
               }
         }
      }
   }

   private void updateIngredientList(List<Item> ingredients) {
      ObservableList<String> ingredientItems = FXCollections.observableArrayList();
      for (Item ingredient : ingredients) {
          String display = ingredient.getName() + " - " + ingredient.getQuantity() + " " + ingredient.getUnit();
          if (ingredient.getExpirDate() != null) {
              display += " (Expires: " + ingredient.getExpirDate() + ")";
          }
          ingredientItems.add(display);
      }
      ingredientList.setItems(ingredientItems);
  }
   
   @FXML
   private void onReceiptUpload() {
      FileChooser fileChooser = new FileChooser();
      fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
      fileChooser.setTitle("Select Receipt Image(s)");
      Stage stage = (Stage) manualButton.getScene().getWindow();
      List<File> files = fileChooser.showOpenMultipleDialog(stage);

      if (files != null && !files.isEmpty()) {
         List<String> failureMessages = new ArrayList<>(); // Collect failures
         showLoadingScreen(); // Show the loading screen with progress bar

         // Define a Task to process files in the background
         Task<Void> receiptProcessingTask = new Task<>() {
               @Override
               protected Void call() throws Exception {
                  int totalTasks = files.size();
                  int completedTasks = 0;

                  for (File file : files) {
                     try {
                           String absolutePathToScript = new File("src/main/python/ReceiptModule.py").getAbsolutePath();
                           String imagePath = file.getAbsolutePath();

                           String command = String.format("python \"%s\" \"%s\"", absolutePathToScript, imagePath);

                           ProcessBuilder pb = new ProcessBuilder(command.split(" "));
                           pb.directory(new File(System.getProperty("user.dir")));
                           pb.redirectErrorStream(true);
                           Process process = pb.start();

                           BufferedReader inputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                           StringBuilder output = new StringBuilder();
                           String line;

                           while ((line = inputReader.readLine()) != null) {
                              output.append(line);
                           }

                           if (output.length() == 0) {
                              failureMessages.add("File: " + file.getName() + " - No response from receipt scanner.");
                              continue;
                           }

                           JsonObject jsonObject = JsonParser.parseString(output.toString()).getAsJsonObject();
                           String status = jsonObject.get("status").getAsString();

                           if ("success".equals(status)) {
                              JsonArray upcCodes = jsonObject.getAsJsonArray("upc_codes");
                              for (int i = 0; i < upcCodes.size(); i++) {
                                 String upcCode = upcCodes.get(i).getAsString();
                                 try {
                                       fetchProductDetails(upcCode);
                                 } catch (Exception e) {
                                       failureMessages.add("UPC Code: " + upcCode + " - Error fetching details.");
                                 }
                              }
                           } else {
                              String message = jsonObject.has("message") ? jsonObject.get("message").getAsString() : "Unknown error occurred.";
                              failureMessages.add("File: " + file.getName() + " - " + message);
                           }
                     } catch (IOException e) {
                           e.printStackTrace();
                           failureMessages.add("File: " + file.getName() + " - Error during processing.");
                     }

                     // Update progress
                     completedTasks++;
                     updateProgress(completedTasks, totalTasks);
                  }

                  return null;
               }
         };

         // Bind progress bar to task progress
         progressBar.progressProperty().bind(receiptProcessingTask.progressProperty());

         // Handle task completion
         receiptProcessingTask.setOnSucceeded(event -> {
               hideLoadingScreen(); // Hide the loading screen
               progressBar.progressProperty().unbind();
               progressBar.setProgress(0); // Reset progress bar

               if (!failureMessages.isEmpty()) {
                  showFailureAlert("Receipt Errors", "The following items failed:", String.join("\n", failureMessages));
               }
         });

         receiptProcessingTask.setOnFailed(event -> {
               hideLoadingScreen();
               progressBar.progressProperty().unbind();
               progressBar.setProgress(0);
               showFailureAlert("Error", "An unexpected error occurred during receipt processing.", receiptProcessingTask.getException().getMessage());
         });

         // Run the task in a new thread
         new Thread(receiptProcessingTask).start();
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
         showLoadingScreen();

         new Thread(() -> {
               try {
                  for (File file : files) {
                     String absolutePathToScript = new File("src/main/python/BarcodeModule.py").getAbsolutePath();
                     String imagePath = file.getAbsolutePath();

                     String command = String.format("python \"%s\" \"%s\"", absolutePathToScript, imagePath);

                     ProcessBuilder pb = new ProcessBuilder(command.split(" "));
                     pb.directory(new File(System.getProperty("user.dir")));
                     pb.redirectErrorStream(true);
                     Process process = pb.start();

                     BufferedReader inputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                     StringBuilder output = new StringBuilder();
                     String line;

                     while ((line = inputReader.readLine()) != null) {
                           output.append(line);
                     }

                     System.out.println("Python Script Output: " + output.toString());

                     if (output.length() == 0) {
                           showAlert("Error", "No response from barcode scanner.");
                           return;
                     }

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

                                 prefillManualForm(productName, Integer.toString(parseQuantity(quantityInfo)), parseUnit(quantityInfo));
                              } else if ("not_found".equals(resultStatus)) {
                                 showAlert("Product Not Found", "No product information found for barcode: " + barcode + ", please enter manually.");
                              }
                           }
                     } else {
                           showAlert("Error", "An error occurred while scanning barcodes.");
                     }
                  }
               } catch (IOException e) {
                  e.printStackTrace();
                  showAlert("Error", "An error occurred during barcode processing.");
               } finally {
                  hideLoadingScreen();
               }
         }).start();
      }
   }

   private void showLoadingScreen() {
      progressBar.setVisible(true);
      tipsLabel.setVisible(true);
      loadingTXT.setVisible(true);
  
      // Randomly select a tip to display
      String randomTip = tips.get((int) (Math.random() * tips.size()));
      tipsLabel.setText(randomTip);
   }
  
   private void hideLoadingScreen() {
         // Ensure this is run on the JavaFX application thread
         javafx.application.Platform.runLater(() -> {
            progressBar.setVisible(false);
            tipsLabel.setVisible(false);
            loadingTXT.setVisible(false);
         });
   }

   private void updateProgressBar(double progress) {
      progressBar.setProgress(progress);
   }

   // Helper method to parse quantity and unit from the quantity string
   private Integer parseQuantity(String quantityInfo) {
      if (quantityInfo == null) return null;
  
      try {
          // Extract the number from the quantity information
          String quantityString = quantityInfo.split(" ")[0];
          return Integer.parseInt(quantityString);
      } catch (NumberFormatException e) {
          return null;  // Return null if parsing fails
      }
  }
  
   private String parseUnit(String quantityInfo) {
      if (quantityInfo == null) return null;
   
      String[] parts = quantityInfo.split(" ");
      return (parts.length > 1) ? parts[1] : null;
   }

   private void fetchProductDetails(String upc) {
      try {
         ProcessBuilder pb = new ProcessBuilder("python", "src/main/python/ProductDetails.py", upc);
         pb.directory(new File(System.getProperty("user.dir")));
         pb.redirectErrorStream(true);
         Process process = pb.start();

         BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
         String jsonResponse = in.lines().collect(Collectors.joining());
         process.waitFor();

         if (!jsonResponse.isEmpty()) {
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(jsonResponse, JsonObject.class);
            if ("success".equals(jsonObject.get("status").getAsString())) {
                  JsonObject product = jsonObject.getAsJsonObject("product");
                  String name = product.has("product_name") ? product.get("product_name").getAsString() : null;
                  String quantityInfo = product.has("quantity") ? product.get("quantity").getAsString() : null;

                  prefillManualForm(name, quantityInfo != null ? parseQuantity(quantityInfo).toString() : null, parseUnit(quantityInfo));
            } else {
                  String message = jsonObject.has("message") ? jsonObject.get("message").getAsString() : "No product information in the database.";
                  showFailureAlert("Barcode Error", "UPC Code: " + upc, message);
            }
         } else {
            showFailureAlert("Barcode Error", "UPC Code: " + upc, "No response from the server.");
         }
      } catch (Exception e) {
         e.printStackTrace();
         showFailureAlert("Barcode Error", "UPC Code: " + upc, "An error occurred while processing.");
      }
   }

   private void showFailureAlert(String title, String header, String content) {
      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.setTitle(title);
      alert.setHeaderText(header);
      alert.setContentText(content);
      alert.showAndWait();
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

         // Convert tags to a set
         Set<String> itemTags = new HashSet<>(tags);

         // Format date for display
         //DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
         //String expirationDateString = expirationDate.format(formatter);

         // Create new ingredient item and add to inventory
         String imageFileName = selectedImageFile != null ? selectedImageFile.getName() : null;
         Item newIngredient = new Item(ingredientName, 0, Integer.parseInt(quantity), unit, location, convertedDate, imageFileName);
         newIngredient.setTags(itemTags);
         
         addIngredientToDatabase(newIngredient);

         try {
            // Load the FXML for the ingredient card
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/javafx/Resources/FXMLs/ingredientCard.fxml"));
            VBox ingredientCard = loader.load();

            // Set ingredient card details
            IngredientCardController controller = loader.getController();
            controller.setIngredientData(newIngredient.getID(), ingredientName + " - " + quantity + " " + unit, newIngredient.getImagePath(), this);

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
            imagePreview.setImage(null); // Clear the image preview
            selectedImage.cancel();

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
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/javafx/Resources/FXMLs/ingredientCard.fxml"));
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
         FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/javafx/Resources/FXMLs/ingredientCard.fxml"));
         VBox duplicateCard = loader.load();

         // Transfer data from the original card to the duplicate
         IngredientCardController originalController = (IngredientCardController) originalCard.getProperties().get("controller");
         IngredientCardController duplicateController = loader.getController();
         
         // Set the same data on the duplicate
         duplicateController.setIngredientData(originalController.getIngredientId(), 
                                                originalController.getIngredientName(), 
                                                originalController.getImagePath(), 
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
  
          // Copy the image to the resources folder
          String destinationFileName = selectedImageFile.getName();
          try {
              copyImageToResources(selectedImageFile, destinationFileName);
              System.out.println("Image copied to resources folder: " + destinationFileName);
          } catch (IOException e) {
              e.printStackTrace();
              showAlert("Error", "Failed to copy image to resources folder.");
          }
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
            for (Item item : ingredientInventory) {
               if (item.getTags() == null) {
                  item.setTags(new HashSet<>()); // Initialize tags if null
               }
            }

            int maxID = ingredientInventory.stream().mapToInt(Item::getID).max().orElse(0);
            nextIngredientID = maxID + 1;
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

      item.setID(nextIngredientID++);
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
      
   private void updateSpacesItemCards() {
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
  }

   private void copyImageToResources(File sourceFile, String destinationFileName) throws IOException {
      // Get the working directory and construct a relative path
      File destinationFolder = new File("src/main/resources/org/javafx/Resources/Item Images");

      // Ensure the folder exists
      if (!destinationFolder.exists()) {
         boolean created = destinationFolder.mkdirs(); // Create directories if missing
         if (!created) {
            throw new IOException("Failed to create directory: " + destinationFolder.getAbsolutePath());
         }
      }

      // Construct the full path to the destination file
      File destinationFile = new File(destinationFolder, destinationFileName);

      // Copy the file
      Files.copy(sourceFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

      System.out.println("Image successfully copied to: " + destinationFile.getAbsolutePath());
   }
}
