package org.javafx.Controllers;

import java.io.File;
// Java Imports
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;

import org.javafx.Item.Item;
// External Libraries
import org.javafx.Main.Main;
import org.javafx.Recipe.Recipe;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyStringWrapper;
// JavaFX Imports
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;


// ==============================================================================
//  MealPlannerController handles meal planning, navigation, and UI interactions
// ==============================================================================

public class MealPlannerController {

    // ==================
    // FXML UI COMPONENTS
    // ==================

    @FXML private ComboBox<String> calendarViewDropdown, nutritionalMeals, mealSlot, mealReadiness, mealType, sortBy;
    @FXML private Button addMealButton, userDashboardButton, mealPlannerButton, myRecipesButton, inventoryButton,
                        closeButton, inboxButton, browseRecipesButton, profileButton, settingsButton, myListsButton, 
                        menuButton, addMealToPlan, prevStep, nextStep, closeRecipeButton, ingredientFilter, tagsFilter, resetFilters;
    @FXML private DatePicker datePicker, dateInView, mealPlanDate;
    @FXML private PieChart dailyNutritionalBreakdown;
    @FXML private ScrollPane mealDetailsPane;
    @FXML private Text mealNameTXT, complexityTXT, servingsTXT, prepTimeTXT, passiveTimeTXT, cookTimeTXT, totalTimeTXT, 
                        stepOfTXT, recipeCookingNameTXT, breakfastMeal, lunchMeal, dinnerMeal, snacksMeal, noRecipesTXT,
                        mealSelectTXT;
    @FXML private ListView<String> specialEquipmentListView, ingredientsArea;
    @FXML private TextArea descriptionArea, stepArea;
    @FXML private Pane menuPane, calendarPane, AddMealMenu, recipeCookingPane, mealPlannerMainPane, recipeFlowPane;
    @FXML private GridPane dayView, weekView, monthView;
    @FXML private ImageView recipeImages;
    @FXML private TextField searchBar;
    
    @FXML private TableView<Ingredient> ingredientTable;
    @FXML private TableColumn<Ingredient, String> ingredientList, amountList;
    private ObservableList<Ingredient> ingredients = FXCollections.observableArrayList();

    @FXML private ComboBox<String> prepHourPicker, passiveHourPicker, cookHourPicker;

    @FXML private ScrollPane dayViewScroll;
    @FXML private ScrollPane weekViewScroll;
    @FXML private ScrollPane monthViewScroll;

    // ==============================
    // DATA STORAGE & CONSTANTS
    // ==============================
    
    private Map<LocalDate, List<Map<String, Object>>> mealPlans = new HashMap<>();
    private Label draggedBlock;
    private MyListsController myListsController;
    private int currentStep = 0, displayStep = 0;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("d'th'");
    private ObservableList<Recipe> recipeList = FXCollections.observableArrayList();
    private Map<Integer, VBox> recipeWidgets = new HashMap<>();
    private ArrayList<Item> ingredientInventory = new ArrayList<Item>(); //ingredient interface 

    // Selected filters for recipes
    private Set<String> selectedIngredients = new HashSet<>();
    private Set<String> selectedTags = new HashSet<>();
    private Set<String> availableIngredients = new HashSet<>();
    private Set<String> availableTags = new HashSet<>();

    private List<String> preparationSteps = new ArrayList<>();
    private ObservableList<Image> recipeThumbnails = FXCollections.observableArrayList();
    private Map<Integer, String> stepImageFileMap = new HashMap<>();
    private Map<Integer, Image> stepImageMap = new HashMap<>();

    private static final String RECIPES_FILE_PATH = "recipes.json";

    private Recipe selectedRecipe;
    private int nextIngredientID = 1;

    private static int nextMealBlockID = 1;

    // ====================================================================
    // INITIALIZATION
    // These methods handle the controller setup when the FXML is loaded
    // ====================================================================

    @FXML
    private void initialize() {

        loadIngredientInventory(); // Load Users Item Inventory from JSON
        loadMealPlansFromJson(); // Load meal plans from JSON
        loadRecipes(); // Load Users Recipes from JSON
        populateFilterOptions(); // Ensure filters get populated
        setupMultiSelectFilters(); // Set up Filter Buttons

        sortBy.getItems().addAll("A-Z", "Z-A", "Complexity", "Prep Time", "Cook Time");

        menuButton.setOnAction(event -> toggleMenuPane());
        
        // Set up navigation and button interactions
        setNavigationButtonHandlers();
        setHoverEffect(userDashboardButton);
        setHoverEffect(mealPlannerButton);
        setHoverEffect(myRecipesButton);
        setHoverEffect(inventoryButton);
        setHoverEffect(inboxButton);
        setHoverEffect(browseRecipesButton);
        setHoverEffect(profileButton);
        setHoverEffect(settingsButton);

        // Configure the calendar dropdown and date pickers
        calendarViewDropdown.getItems().addAll("Day View", "Week View", "Month View");
        calendarViewDropdown.setValue("Week View");
        datePicker.setValue(LocalDate.now());
        datePicker.setStyle("-fx-font-size: 18px;");
        dateInView.setValue(LocalDate.now());
        dateInView.setStyle("-fx-font-size: 18px;");

        mealReadiness.getItems().addAll("All", "Ready to Cook", "Requires Prep", "Partial Recipes", "Incomplete");
        mealReadiness.setValue("All");
        mealReadiness.setOnAction(event -> filterRecipes());


        for (int i = 0; i < 24; i++) {
            String label = String.format("%02d %s", (i % 12 == 0 ? 12 : i % 12), (i < 12 ? "AM" : "PM"));
        
            prepHourPicker.getItems().add(label);
            passiveHourPicker.getItems().add(label);
            cookHourPicker.getItems().add(label);
        }
        
        // Default time suggestions
        prepHourPicker.setValue("09 AM");
        passiveHourPicker.setValue("12 PM");
        cookHourPicker.setValue("06 PM");
        
        // Load the default views
        loadDailyMeals(LocalDate.now());
        loadCalendarView();
        //calculateMealNutrition();

        // UI event handlers
        calendarViewDropdown.setOnAction(event -> loadCalendarView());
        dateInView.setOnAction(event -> updateDateInView());
        nutritionalMeals.getItems().addAll("Breakfast", "Lunch", "Dinner", "Snacks", "All Meals", "Total Per Serving");
        nutritionalMeals.setOnAction(event -> calculateMealNutrition());
        addMealButton.setOnAction(event -> openAddMealDialog());
        closeButton.setOnAction(event -> closeMealDetails());
        closeRecipeButton.setOnAction(event -> closeRecipeCookingPane());
        addMealToPlan.setOnAction(event -> addMeal());
        
        // Attach event listeners for filtering
        resetFilters.setOnAction(event -> clearAllFilters());
        sortBy.setOnAction(event -> filterRecipes());

        prevStep.setOnAction(event -> detailsSteps(-1));
        nextStep.setOnAction(event -> detailsSteps(1));
    }

    // =============================================
    // Navigation & UI Handling
    // Handles menu navigation and button actions
    // =============================================

    private void setNavigationButtonHandlers() {
        inventoryButton.setOnAction(event -> navigateToScreen("Inventory"));
        myRecipesButton.setOnAction(event -> navigateToScreen("MyRecipes"));
        inboxButton.setOnAction(event -> navigateToScreen("Inbox"));
        browseRecipesButton.setOnAction(event -> navigateToScreen("CommunityRecipes"));
        profileButton.setOnAction(event -> navigateToScreen("Profile"));
        settingsButton.setOnAction(event -> navigateToScreen("Settings"));
        myListsButton.setOnAction(event -> navigateToScreen("MyLists"));
        userDashboardButton.setOnAction(event -> navigateToScreen("UserDashboard"));
    }

    // Navigate between steps
    private void detailsSteps(int direction) {
        // Save the current step text before navigating
        if (displayStep >= 0 && displayStep < preparationSteps.size()) {
        preparationSteps.set(displayStep, stepArea.getText().trim());
        }

        // Calculate new step index
        int newStep = displayStep + direction;
        if (newStep >= 0 && newStep < preparationSteps.size()) {
        displayStep = newStep;
        updateDetailsStepView();
        }
    }

    // Update TextArea and stepIndex label to display the current step
    private void updateDetailsStepView() {
        if (preparationSteps.isEmpty()) {
        stepArea.setText("");
        stepOfTXT.setText("Step 1 of 1");
        recipeImages.setImage(recipeThumbnails.get(0));
        } else {
        stepArea.setText(preparationSteps.get(displayStep));
        stepOfTXT.setText("Step " + (displayStep + 1) + " of " + preparationSteps.size());

        if (stepImageMap.containsKey(displayStep)) {
            recipeImages.setImage(stepImageMap.get(displayStep));
        } else if (stepImageMap.containsKey(0)) {
            recipeImages.setImage(stepImageMap.get(0)); // fallback to main image
        }
        }
    }


    private void navigateToScreen(String screen) {
        try {
            switch (screen) {
                case "Inventory": Main.showInventoryScreen(); break;
                case "MyRecipes": Main.showMyRecipesScreen(); break;
                case "Inbox": Main.showInboxScreen(); break;
                case "CommunityRecipes": Main.showCommunityRecipesScreen(); break;
                case "Profile": Main.showUserProfileScreen(); break;
                case "Settings": Main.showUserSettingsScreen(); break;
                case "MyLists": Main.showMyListsScreen(); break;
                case "UserDashboard": Main.showUserDashboardScreen(); break;
                default: System.out.println("Screen not implemented: " + screen);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void closeMealDetails() {
        AddMealMenu.setVisible(false);
        mealDetailsPane.setVisible(false);
        //reset mealDetails pane to null
        calendarPane.setVisible(true);
    }

    private void closeRecipeCookingPane() {
        calendarPane.setVisible(true);
        recipeCookingPane.setVisible(false);
    }

    private void setHoverEffect(Button button) {
        button.setOnMouseEntered(this::handleMouseEntered);
        button.setOnMouseExited(this::handleMouseExited);
    }  

    private void handleMouseEntered(MouseEvent event) {
        Button button = (Button) event.getSource();
        // Change style when mouse enters
        button.setStyle("-fx-background-color: #FF7F11; -fx-text-fill: white; -fx-font-size: 30px; -fx-font-weight: bold; -fx-background-radius: 50; ");
    }

    private void handleMouseExited(MouseEvent event) {
        Button button = (Button) event.getSource();
        // Reset style when mouse exits
        button.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 30px; -fx-font-weight: bold; -fx-background-radius: 50; ");
    }

    private void toggleMenuPane() {
        try {
            menuPane.setVisible(!menuPane.isVisible());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // ===================================================================
    // Meal Planning & Calendar Views: 
    // Handles calendar UI updates, date changes, and meal organization
    // ===================================================================

    private void updateDateInView() {
        LocalDate selectedDate = dateInView.getValue();
        if (selectedDate != null) {
            datePicker.setValue(selectedDate);
            loadCalendarView();
            calculateMealNutrition();
        }
    }

    private void loadCalendarView() {
        String selectedView = calendarViewDropdown.getValue();
    
        // Hide all scroll panes initially
        dayViewScroll.setVisible(false);
        weekViewScroll.setVisible(false);
        monthViewScroll.setVisible(false);
    
        // Show the appropriate view based on the selection
        switch (selectedView) {
            case "Day View":
                dayViewScroll.setVisible(true);
                loadDayView();
                break;
            case "Week View":
                weekViewScroll.setVisible(true);
                loadWeekView();
                break;
            case "Month View":
                monthViewScroll.setVisible(true);
                loadMonthView();
                break;
        }
    }

    private void loadDayView() {
        dayView.getChildren().clear(); // Clear previous content
        dayView.getRowConstraints().clear(); // Clear row constraints
        dayView.getColumnConstraints().clear(); // Clear column constraints

        dayView.setStyle("-fx-border-color: white; -fx-grid-lines-visible: true;");

    
        // Set up a column for time labels and another for meal blocks
        ColumnConstraints timeColumn = new ColumnConstraints();
        timeColumn.setPercentWidth(15); // 15% for time labels
        dayView.getColumnConstraints().add(timeColumn);
    
        ColumnConstraints mealColumn = new ColumnConstraints();
        mealColumn.setPercentWidth(85); // 85% for meals
        dayView.getColumnConstraints().add(mealColumn);
    
        // Set up 24 rows for the hours of the day
        for (int i = 0; i < 24; i++) {
            RowConstraints row = new RowConstraints();
            row.setPercentHeight(100.0 / 24); // Divide grid equally by 24 rows
            dayView.getRowConstraints().add(row);
        }
    
        // Add time labels with AM/PM
        for (int hour = 0; hour < 24; hour++) {
            String label = String.format("%d %s", 
                (hour % 12 == 0 ? 12 : hour % 12), 
                (hour < 12 ? "AM" : "PM"));
            Label timeLabel = new Label(label);
            timeLabel.setStyle("-fx-font-size: 18px; -fx-padding: 5px; -fx-font-weight: bold; -fx-text-fill: white;");
            GridPane.setConstraints(timeLabel, 0, hour);
            dayView.getChildren().add(timeLabel);
        }
    
        // Load meals for the selected day and render them
        List<Map<String, Object>> mealsForDay = mealPlans.getOrDefault(datePicker.getValue(), new ArrayList<>());

        System.out.println(mealsForDay);
    
        for (Map<String, Object> mealData : mealsForDay) {
            createTimeBlocksForMeal(mealData, dayView, 1, datePicker.getValue()); // Column 1 for meals
        }
    
        dayView.setGridLinesVisible(false); // Refresh grid lines
        dayView.setGridLinesVisible(true);
        

        stylePastDates();
    }
    
    private void loadWeekView() {
        weekView.getChildren().clear();
        weekView.getRowConstraints().clear();
        weekView.getColumnConstraints().clear();

        weekView.setStyle("-fx-border-color: white; -fx-grid-lines-visible: true;");
    
        // Set up 25 rows for 24 hours and one row for the day label
        for (int i = 0; i < 25; i++) {
            RowConstraints rowConstraints = new RowConstraints();
            rowConstraints.setPrefHeight(Region.USE_COMPUTED_SIZE); // Let it calculate needed height
            rowConstraints.setVgrow(Priority.ALWAYS);  // Allow the row to grow if space available
            weekView.getRowConstraints().add(rowConstraints);
        }
    
        // Set up 8 columns: one for the hours, and seven for each day of the week
        ColumnConstraints hourColumn = new ColumnConstraints();
        hourColumn.setPercentWidth(10); // Set 10% of width for the hour labels
        weekView.getColumnConstraints().add(hourColumn);
    
        for (int i = 0; i < 7; i++) {
            ColumnConstraints dayColumn = new ColumnConstraints();
            dayColumn.setPercentWidth(90.0 / 7); // Split remaining 90% across 7 days
            weekView.getColumnConstraints().add(dayColumn);
        }
    
        LocalDate startDate = datePicker.getValue();
    
        // Time labels (1:00 AM – 12:00 PM)
        for (int hour = 0; hour < 24; hour++) {
            String label = String.format("%d %s", 
                (hour % 12 == 0 ? 12 : hour % 12), 
                (hour < 12 ? "AM" : "PM"));
            Label hourLabel = new Label(label);
            hourLabel.setStyle("-fx-font-size: 18px; -fx-padding: 5px; -fx-font-weight: bold; -fx-text-fill: white;");
            GridPane.setConstraints(hourLabel, 0, hour + 1); // +1 skips header
            weekView.getChildren().add(hourLabel);
        }
    
        // Add day labels at the top row
        for (int i = 0; i < 7; i++) {
            LocalDate currentDate = startDate.plusDays(i);
            String dayOfWeek = currentDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
            String dayLabel = dayOfWeek + ", " + currentDate.format(DateTimeFormatter.ofPattern("MM/dd"));
            Text dayTitle = new Text(dayLabel);
            dayTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-fill: white;");
            GridPane.setConstraints(dayTitle, i + 1, 0); // +1 to skip the hour column
            weekView.getChildren().add(dayTitle);
    
            // Get meals for the current day
            List<Map<String, Object>> mealsForDay = mealPlans.getOrDefault(currentDate, new ArrayList<>());
    
            // Render time blocks for meals
            for (Map<String, Object> mealData : mealsForDay) {
                createTimeBlocksForMeal(mealData, weekView, i + 1, currentDate); // Add blocks to the correct day column
            }
        }
    
        weekView.setGridLinesVisible(false);
        weekView.setGridLinesVisible(true);

        stylePastDates();
    }
    
    private void loadMonthView() {
        monthView.getChildren().clear();
        monthView.getRowConstraints().clear();
        monthView.getColumnConstraints().clear();

        monthView.setStyle("-fx-border-color: white; -fx-grid-lines-visible: true;");
    
        // Ensure we have 7 columns for the days of the week
        for (int col = 0; col < 7; col++) {
            ColumnConstraints colConstraints = new ColumnConstraints();
            colConstraints.setPercentWidth(100.0 / 7); // Each column takes 1/7th of the width
            monthView.getColumnConstraints().add(colConstraints);
        }
    
        // Ensure we have 6 rows (maximum needed for a month view)
        for (int row = 0; row < 6; row++) {
            RowConstraints rowConstraints = new RowConstraints();
            rowConstraints.setPercentHeight(100.0 / 6); // Each row takes 1/6th of the height
            monthView.getRowConstraints().add(rowConstraints);
        }
    
        LocalDate currentMonth = dateInView.getValue() != null
                ? dateInView.getValue().withDayOfMonth(1)
                : LocalDate.now().withDayOfMonth(1);
        int daysInMonth = currentMonth.lengthOfMonth();
        DayOfWeek firstDayOfWeek = currentMonth.getDayOfWeek();
        int startColumn = firstDayOfWeek.getValue() % 7;
    
        int dayCount = 1;
        boolean startAddingDays = false;
    
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 7; col++) {
                if (row == 0 && col == startColumn) {
                    startAddingDays = true;
                }
    
                if (startAddingDays && dayCount <= daysInMonth) {
                    LocalDate dayDate = currentMonth.withDayOfMonth(dayCount);
                    List<Map<String, Object>> mealsForDay = getMealsForDay(dayDate);
    
                    VBox dayBox = new VBox();
    
                    // Add day number
                    Text dayNumber = new Text(String.valueOf(dayCount));
                    dayNumber.setStyle("-fx-font-size: 22px; -fx-fill: white; -fx-font-weight: bold;");

                    dayBox.getChildren().add(dayNumber);
                    dayBox.setStyle("-fx-border-color: white; -fx-padding: 5px;");
    
                    int mealCount = 0;
                    for (Map<String, Object> mealData : mealsForDay) {
                        if (mealCount < 3) {
                            String mealName = null;
                            for (String timeKey : List.of("cookTime", "prepTime", "passiveTime")) {
                                if (mealData.containsKey(timeKey)) {
                                    Map<String, Object> block = (Map<String, Object>) mealData.get(timeKey);
                                    if (block != null && block.get("name") != null) {
                                        mealName = (String) block.get("name");
                                        break;
                                    }
                                }
                            }

                            if (mealName != null && !mealName.isBlank()) {
                                Label mealLabel = new Label(mealName);
                                mealLabel.setWrapText(true);  // ⬅️ add this for long titles
                                mealLabel.setMaxWidth(Double.MAX_VALUE);
                                mealLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: white; -fx-font-weight: bold;");
                                VBox.setVgrow(mealLabel, Priority.ALWAYS); // optional for better layout
                                dayBox.getChildren().add(mealLabel);
                                mealCount++;
                            }
                        } else {
                            break;
                        }
                    }

    
                    if (mealCount > 3) {
                        Label moreLabel = new Label("+" + (mealCount - 3) + " more");
                        moreLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: white;");
                        dayBox.getChildren().add(moreLabel);
                    }
    
                    GridPane.setConstraints(dayBox, col, row);
                    monthView.getChildren().add(dayBox);
                    dayCount++;
                }
            }
        }
    
        monthView.setGridLinesVisible(false);
        monthView.setGridLinesVisible(true);

        stylePastDates();
    }

    private void loadDailyMeals(LocalDate date) {
        List<Map<String, Object>> mealsForDay = mealPlans.getOrDefault(date, new ArrayList<>());
    
        // Initialize default meal names for breakfast, lunch, dinner, and snacks
        String breakfast = "[No Meal Planned]";
        String lunch = "[No Meal Planned]";
        String dinner = "[No Meal Planned]";
        String snacks = "[No Meal Planned]";
    
        for (Map<String, Object> mealData : mealsForDay) {
            for (String timePartKey : List.of("prepTime", "passiveTime", "cookTime")) {
                Map<String, Object> timePart = (Map<String, Object>) mealData.get(timePartKey);
                if (timePart != null) {
                    String blockDate = (String) timePart.get("date");
                    int hour = ((Number) timePart.get("hour")).intValue();
    
                    // Only process if the block date matches the selected date
                    if (LocalDate.parse(blockDate).equals(date)) {
                        Map<String, Object> block = (Map<String, Object>) mealData.get(timePartKey);
                        if (block != null) {
                            String mealName = (String) block.get("name");
                            String type = (String) block.get("mealType");
                            if (type != null) {
                                switch (type.toLowerCase()) {
                                    case "breakfast": breakfast = mealName; break;
                                    case "lunch": lunch = mealName; break;
                                    case "dinner": dinner = mealName; break;
                                    case "snack": snacks = mealName; break;
                                }
                            }
                        }
                    }
                }
            }
        }
    
        breakfastMeal.setText("Breakfast: " + breakfast);
        lunchMeal.setText("Lunch: " + lunch);
        dinnerMeal.setText("Dinner: " + dinner);
        snacksMeal.setText("Snacks: " + snacks);
    }

    // ===========================================================
    // Meal Plan Handling: 
    // Handles adding, modifying, and removing meals in the plan
    // ===========================================================
    
    private void openAddMealDialog() {
        calendarPane.setVisible(false);
        AddMealMenu.setVisible(true);
    }

    private void addMealToPlan(Recipe meal, LocalDate prepDate, int prepHour, LocalDate passiveDate, int passiveHour, LocalDate cookDate, int cookHour) {
        String mealTypeValue = mealSlot.getValue(); // Get selected value

        String mealGroupId = UUID.randomUUID().toString();
    
        Map<String, Object> fullMealMap = new HashMap<>();
    
        // Add prepTime block
        if (meal.getPrepTime() > 0) {
            fullMealMap.put("prepTime", createTimeBlock("prepTime", meal, prepDate, prepHour, meal.getPrepTime(), mealTypeValue, mealGroupId));
        }
    
        // Add passiveTime block
        if (meal.getPassiveTime() > 0) {
            fullMealMap.put("passiveTime", createTimeBlock("passiveTime", meal, passiveDate, passiveHour, meal.getPassiveTime(), mealTypeValue, mealGroupId));
        }
    
        // Add cookTime block
        if (meal.getCookTime() > 0) {
            fullMealMap.put("cookTime", createTimeBlock("cookTime", meal, cookDate, cookHour, meal.getCookTime(), mealTypeValue, mealGroupId));
        }
    
        // Save the unified meal under the date of the cook block (or prep if no cook)
        LocalDate mainDate = cookDate != null ? cookDate : prepDate;
        List<Map<String, Object>> mealsForDay = mealPlans.computeIfAbsent(mainDate, k -> new ArrayList<>());
        mealsForDay.add(fullMealMap);
    
        saveMealPlansToJson();
    }

    private void addMeal() {
        
        if (selectedRecipe == null) {
            showAlert("No Recipe Selected", "Please select a recipe to add.");
            return;
        }
    
        LocalDate selectedDate = mealPlanDate.getValue();
        if (selectedDate == null) {
            showAlert("No Date Selected", "Please select a date to plan this meal.");
            return;
        }
    
        int prepHour = parseHourLabel(prepHourPicker.getValue());
        int passiveHour = parseHourLabel(passiveHourPicker.getValue());
        int cookHour = parseHourLabel(cookHourPicker.getValue());

        addMealToPlan(selectedRecipe, selectedDate, prepHour, selectedDate, passiveHour, selectedDate, cookHour);
    
        AddMealMenu.setVisible(false);
        calendarPane.setVisible(true);
        loadCalendarView();
        loadDailyMeals(selectedDate);
        //calculateMealNutrition();

        refreshNeededIngredients();
    }

    private void deleteMealById(int targetId) {
        for (LocalDate date : new ArrayList<>(mealPlans.keySet())) {
            List<Map<String, Object>> mealsForDay = mealPlans.get(date);
            if (mealsForDay != null) {
                mealsForDay.removeIf(meal -> {
                    for (String blockType : List.of("prepTime", "passiveTime", "cookTime")) {
                        if (meal.containsKey(blockType)) {
                            Map<String, Object> block = (Map<String, Object>) meal.get(blockType);
                            if (block != null && ((Number) block.get("id")).intValue() == targetId) {
                                return true;
                            }
                        }
                    }
                    return false;
                });
                if (mealsForDay.isEmpty()) {
                    mealPlans.remove(date);
                }
            }
        }
    
        saveMealPlansToJson();
        loadCalendarView();
        refreshNeededIngredients();
    }
    
    

    private void markMealAsMade(LocalDate date, String blockId) {
        try {
            Map<String, String> blockData = parseBlockId(blockId);
            String mealName = blockData.get("mealName");
    
            List<Map<String, Object>> mealsForDay = mealPlans.getOrDefault(date, new ArrayList<>());
            for (Map<String, Object> meal : mealsForDay) {
                if (mealName.equals(meal.get("name"))) {
                    meal.put("isMealMade", true);
                    break;
                }
            }
    
            saveMealPlansToJson();
            loadDailyMeals(date);
        } catch (Exception e) {
            e.printStackTrace();
        }

        archiveMadeMeals(); // Archive meals after marking as made
    }

    private void archiveMadeMeals() {
        List<Map<String, Object>> archive = new ArrayList<>();
        mealPlans.forEach((date, meals) -> {
            meals.removeIf(meal -> {
                if ((boolean) meal.get("isMealMade")) {
                    archive.add(meal);
                    return true; // Remove from current plan
                }
                return false;
            });
        });
        saveToJson(archive, "mealArchive.json");
        saveMealPlansToJson();
    }

    // ===========================================================================
    // Time Blocks & Drag-Drop Interactions
    // Handles time blocks within the calendar, including drag-and-drop movement
    // ===========================================================================

    private void enableTimeBlockInteraction(Label block, GridPane targetView) {
        final long[] pressTime = new long[1]; // Stores the time of press
        final double[] startX = new double[1]; // Stores initial X position
        final double[] startY = new double[1]; // Stores initial Y position
    
        block.setOnMousePressed(event -> {
            pressTime[0] = System.currentTimeMillis(); // Store time when pressed
            startX[0] = event.getSceneX(); // Store initial X position
            startY[0] = event.getSceneY(); // Store initial Y position
        });
    
        block.setOnMouseReleased(event -> {
            long releaseTime = System.currentTimeMillis();
            double endX = event.getSceneX();
            double endY = event.getSceneY();
    
            long clickDuration = releaseTime - pressTime[0]; // Calculate duration
    
            // If the duration is short and the block didn't move much, treat it as a click
            if (clickDuration < 200 && Math.abs(startX[0] - endX) < 5 && Math.abs(startY[0] - endY) < 5) {
                openRecipeOnClick(block);
            }
        });
    
        // Enable dragging for repositioning meals
        makeTimeBlockDraggable(block, targetView);
    }

    private boolean validateTimeBlockOrder(Map<String, Object> mealData) {
        Map<String, Object> prepTime = (Map<String, Object>) mealData.get("prepTime");
        Map<String, Object> passiveTime = (Map<String, Object>) mealData.get("passiveTime");
        Map<String, Object> cookTime = (Map<String, Object>) mealData.get("cookTime");
    
        int prepHour = prepTime != null ? ((Number) prepTime.get("hour")).intValue() : Integer.MAX_VALUE;
        int passiveHour = passiveTime != null ? ((Number) passiveTime.get("hour")).intValue() : Integer.MAX_VALUE;
        int cookHour = cookTime != null ? ((Number) cookTime.get("hour")).intValue() : Integer.MIN_VALUE;
    
        if (cookHour < Math.min(prepHour, passiveHour)) {
            return false; // Cook starts before prep or passive ends
        }
    
        return true;
    }

    private Map<String, Object> createTimeBlock(String blockType, Recipe meal, LocalDate date, int hour, int duration, String mealType, String mealGroupId) {
        Map<String, Object> timeBlock = new HashMap<>();
        timeBlock.put("id", nextMealBlockID++);
        timeBlock.put("recipeID", meal.getID());
        timeBlock.put("name", meal.getName());
        timeBlock.put("isMealMade", false);
        timeBlock.put("date", date.toString());
        timeBlock.put("duration", duration);
        timeBlock.put("hour", hour);
        timeBlock.put("mealType", mealType);
        timeBlock.put("mealGroupId", mealGroupId);
        return timeBlock;
    }

    private void createTimeBlocksForMeal(Map<String, Object> mealData, GridPane targetView, int dayColumn, LocalDate currentDate) {

        for (String timePartKey : List.of("prepTime", "passiveTime", "cookTime")) {
            Map<String, Object> timePart = (Map<String, Object>) mealData.get(timePartKey);
    
            if (timePart != null) {
                String blockDate = (String) timePart.get("date");
                int hour = ((Number) timePart.get("hour")).intValue();
                int duration = ((Number) timePart.get("duration")).intValue();
    
                if (duration > 0) {
                    // If the block's date is not the same as the current day's date, skip it
                    LocalDate blockLocalDate = LocalDate.parse(blockDate);
                    if (!blockLocalDate.isEqual(currentDate)) {
                        continue;
                    }
    
                    // Assign colors based on the time part
                    String color;
                    switch (timePartKey) {
                        case "prepTime":
                            color = "#4CAF50";
                            break;
                        case "passiveTime":
                            color = "FFC107";
                            break;
                        case "cookTime":
                            color = "FF5733";
                            break;
                        default:
                            color = "lightgray";
                    }
    
                    addBlockForTimePart(mealData, timePartKey, targetView, dayColumn, hour + 1, color);
                }
            }
        }
    }

    private void addBlockForTimePart(Map<String, Object> mealData, String timePartKey, GridPane targetView, int dayColumn, int currentRow, String color) {
        Map<String, Object> timePart = (Map<String, Object>) mealData.get(timePartKey);
        int duration = ((Number) timePart.get("duration")).intValue();
    
        if (duration > 0) {
            String mealName = (String) timePart.get("name");

            String blockDate = (String) timePart.get("date");
            int hour = ((Number) timePart.get("hour")).intValue();
    
            // Create a unique ID for the block
            String mealGroupId = (String) timePart.get("mealGroupId");
            String blockId = String.join("|", mealName, timePartKey, blockDate, String.valueOf(hour), mealGroupId);
            
    
            Label block = new Label(mealName + "\n(" + timePartKey + ": " + duration + " mins)");
            block.setWrapText(true);
            block.setMinHeight(Region.USE_PREF_SIZE);
            block.setPrefHeight(Region.USE_COMPUTED_SIZE);

            switch (timePartKey) {
                case "prepTime":
                block.setStyle("-fx-background-color: " + color + "; -fx-text-fill: black; -fx-padding: 10px; -fx-font-size: 18px; -fx-border-radius: 10px;");
                    break;
                case "passiveTime":
                block.setStyle("-fx-background-color: " + color + "; -fx-text-fill: black; -fx-padding: 10px; -fx-font-size: 18px; -fx-border-radius: 10px;");
                    break;
                case "cookTime":
                block.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-padding: 10px; -fx-font-size: 18px; -fx-border-radius: 10px;");
                    break;
                default:
                block.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-padding: 10px; -fx-font-size: 18px; -fx-border-radius: 10px;");
            }

            block.setId(blockId); // Assign the ID to the block
    
            GridPane.setConstraints(block, dayColumn, currentRow);

            GridPane.setRowSpan(block, (int) Math.ceil(duration / 60.0));
            targetView.getChildren().add(block);
    
            makeTimeBlockDraggable(block, targetView);
            addRightClickOptions(block, LocalDate.parse(blockDate)); // Add context menu
            openRecipeOnClick(block); // Add left-click to view recipe
        }
    }

    private void makeTimeBlockDraggable(Label block, GridPane targetView) {
        // Drag Start
        block.setOnDragDetected(event -> {
            //System.out.println("Drag started for block: " + block.getText());
            javafx.scene.input.Dragboard db = block.startDragAndDrop(TransferMode.MOVE);
            javafx.scene.image.WritableImage snapshot = block.snapshot(null, null);
            db.setDragView(snapshot);
        
            javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
            content.putString(block.getText()); // Attach text to the dragboard for debugging
            db.setContent(content);
        
            draggedBlock = block; // Store the reference to the dragged block
            event.consume();
        });
    
        // Allow Drag Over
        targetView.setOnDragOver(event -> {
            if (event.getDragboard().hasString()) {
                //System.out.println("Drag over at X: " + event.getX() + ", Y: " + event.getY());
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });
    
        // Handle Drop
        targetView.setOnDragDropped(event -> {
            javafx.scene.input.Dragboard db = event.getDragboard();
            if (db.hasString() && draggedBlock != null) {
                //System.out.println("Dropped block: " + draggedBlock.getText());
                double x = event.getX();
                double y = event.getY();
        
                // Calculate new column and row
                int columnCount = targetView.getColumnConstraints().size();
                int rowCount = targetView.getRowConstraints().size();
        
                int newColumn = Math.min((int) (x / (targetView.getWidth() / columnCount)), columnCount - 1);
                int newRow = Math.min((int) (y / (targetView.getHeight() / rowCount)), rowCount - 1);
        
                //System.out.println("New position - Column: " + newColumn + ", Row: " + newRow);
        
                if (newRow >= 1 && newRow < rowCount && newColumn > 0) {
                    targetView.getChildren().remove(draggedBlock);
                    GridPane.setColumnIndex(draggedBlock, newColumn);
                    GridPane.setRowIndex(draggedBlock, newRow);
                    targetView.getChildren().add(draggedBlock);
        
                    // Update meal plan
                    LocalDate newDate = datePicker.getValue().plusDays(newColumn - 1); // Column index maps to days
                    int newHour = newRow - 1; // Row index maps to hours
                    updateMealPlanForDraggedBlock(draggedBlock, newDate, newHour);
                }
        
                draggedBlock = null;
                event.setDropCompleted(true);
            } else {
                event.setDropCompleted(false);
            }
            event.consume();
        });
    }

    // Update meal block when dragged
    private void updateMealPlanForDraggedBlock(Label block, LocalDate newDate, int newHour) {
        try {
            Map<String, String> blockData = parseBlockId(block.getId());
            String blockType = blockData.get("timeBlockType");
            LocalDate oldDate = LocalDate.parse(blockData.get("date"));
            int oldHour = Integer.parseInt(blockData.get("hour"));
    
            List<Map<String, Object>> mealsForOldDate = mealPlans.getOrDefault(oldDate, new ArrayList<>());
            Map<String, Object> targetMeal = null;
            Map<String, Object> targetBlock = null;
    
            // Find the target meal and block
            for (Map<String, Object> meal : mealsForOldDate) {
                if (meal.containsKey(blockType)) {
                    Map<String, Object> timeBlock = (Map<String, Object>) meal.get(blockType);
                    if (oldDate.toString().equals(timeBlock.get("date")) && oldHour == ((Number) timeBlock.get("hour")).intValue()) {
                        targetBlock = timeBlock;
                        targetMeal = meal;
                        break;
                    }
                }
            }
    
            if (targetMeal != null && targetBlock != null) {
                // Remove the block from the old date's meal list
                mealsForOldDate.remove(targetMeal);
                if (mealsForOldDate.isEmpty()) {
                    mealPlans.remove(oldDate);
                }
    
                // Update the block's date and hour
                targetBlock.put("date", newDate.toString());
                targetBlock.put("hour", newHour);
    
                // Add the updated meal back to the new date's meal list
                List<Map<String, Object>> mealsForNewDate = mealPlans.computeIfAbsent(newDate, k -> new ArrayList<>());
                mealsForNewDate.add(targetMeal);
    
                saveMealPlansToJson();
            }

            loadDailyMeals(newDate); // Refresh UI for the new date
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =====================================================
    // Recipe Handling:
    // Handles retrieving, displaying, and editing recipes
    // =====================================================

    public void showRecipeDetails(String name, Image image, Recipe recipe) {

        recipeCookingPane.setVisible(true);
        calendarPane.setVisible(false);

        displayStep = 0;

        recipeCookingNameTXT.setText(name);

        // Populate ingredient list with checkboxes
        ingredientsArea.getItems().clear();
        for (Ingredient ingredient : recipe.getIngredients()) {
            ingredientsArea.getItems().add("⬜ " + ingredient.getName());
        }
        
        recipeImages.setImage(image);

        preparationSteps = new ArrayList<>(recipe.getSteps());

        stepOfTXT.setText("Step 1 of " + preparationSteps.size());
        stepArea.setText(preparationSteps.isEmpty() ? "" : preparationSteps.get(0));

    }

    private void openRecipeOnClick(Label block) {
        block.setOnMouseClicked(event -> {
            if (event.getButton() == javafx.scene.input.MouseButton.PRIMARY) {
                try { 
                    // Extract meal details from block ID
                    Map<String, String> blockData = parseBlockId(block.getId());
                    String mealName = blockData.get("mealName");
                    
                    // Retrieve recipe from storage
                    Recipe recipe = getRecipeByName(mealName);
                    System.out.println(recipe);
                    if (recipe != null) {
                        File imageFile = new File("src/main/resources/org/javafx/Resources/Recipe Images/" + recipe.getName() + ".png");
                        Image image = imageFile.exists() ? new Image(imageFile.toURI().toString()) : null;
                        showRecipeDetails(mealName, image, recipe);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert("Error", "Failed to open recipe details.");
                }
            }
        });
    }

    private Recipe getRecipeById(int id) {
        try (FileReader reader = new FileReader("recipes.json")) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<Recipe>>() {}.getType();
            List<Recipe> recipes = gson.fromJson(reader, type);

            for (Recipe recipe : recipes) {
                if (recipe.getID() == (id)) {
                    return recipe;
                }
            }
            System.out.println("No recipe found with name: " + id);
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Recipe getRecipeByName(String mealName) {
        try (FileReader reader = new FileReader("recipes.json")) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<Recipe>>() {}.getType();
            List<Recipe> recipes = gson.fromJson(reader, type);
    
            for (Recipe recipe : recipes) {
                if (recipe.getName().equals(mealName)) {
                    return recipe;
                }
            }
            System.out.println("No recipe found with name: " + mealName);
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // =========================================================
    // Inventory & Nutritional Breakdown
    // Handles inventory tracking and displaying nutrition data
    // ==========================================================

    private void calculateMealNutrition() {
        dailyNutritionalBreakdown.getData().clear();

        LocalDate selectedDate = datePicker.getValue();  // Get selected date
        List<Map<String, Object>> mealsForDay = mealPlans.getOrDefault(selectedDate, new ArrayList<>());
    
        List<Map<String, Object>> cookPhaseMeals = new ArrayList<>();

        for (Map<String, Object> meal : mealsForDay) {
            if (meal.containsKey("cookTime")) {  // Only include meals with a cook phase
                cookPhaseMeals.add(meal);
            }
        }
    
        if (cookPhaseMeals.isEmpty()) {
            System.out.println("No meals to be cooked today.");
            dailyNutritionalBreakdown.getData().clear();
            return;
        }
    
        List<Map<String, Object>> ingredients = new ArrayList<>();
        
        for (Map<String, Object> meal : cookPhaseMeals) {

            Map<String, Object> cookTimeData = (Map<String, Object>) meal.get("cookTime");
            if (cookTimeData == null) continue;

            String mealName = (String) cookTimeData.get("name");


            Recipe recipe = getRecipeByName(mealName);

            if (recipe != null) {
                for (Ingredient ingredient : recipe.getIngredients()) {
                    Map<String, Object> ingredientData = new HashMap<>();
                    ingredientData.put("name", ingredient.getName());
                    ingredientData.put("amount", parseAmount(ingredient.getAmount()));
                    ingredients.add(ingredientData);
                }
            }
        }

        // Calculate nutrition based on cookPhaseMeals
        List<Map<String, Object>> nutritionData = MacroCalculator.calculateNutrition(ingredients);

        if (nutritionData != null) {
            dailyNutritionalBreakdown.getData().clear();
            double totalCalories = 0, totalProtein = 0, totalCarbs = 0, totalFats = 0;
    
            for (Map<String, Object> ingredient : nutritionData) {
                if (!ingredient.containsKey("error")) {
                    totalCalories += roundToTwo((double) ingredient.get("calories"));
                    totalProtein += (double) ingredient.get("protein");
                    totalCarbs += (double) ingredient.get("carbs");
                    totalFats += (double) ingredient.get("fats");
                }
            }
            
            // Update PieChart UI with labels containing values
            dailyNutritionalBreakdown.getData().add(new PieChart.Data("Protein: " + roundToTwo(totalProtein) + "g", totalProtein));
            dailyNutritionalBreakdown.getData().add(new PieChart.Data("Carbs: " + roundToTwo(totalCarbs) + "g", totalCarbs));
            dailyNutritionalBreakdown.getData().add(new PieChart.Data("Fats: " + roundToTwo(totalFats) + "g", totalFats));

            // Add total calories as a tooltip over the pie chart
            Tooltip calorieTooltip = new Tooltip("Total Calories: " + totalCalories + " kcal");
            Tooltip.install(dailyNutritionalBreakdown, calorieTooltip);

            for (PieChart.Data data : dailyNutritionalBreakdown.getData()) {
                data.getNode().setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
            }
            
        } else {
            System.out.println("Failed to fetch nutrition data.");
        }
    }
    
    private void deductFromInventory(Ingredient ingredient) {


    }

    private List<Map<String, Object>> generateNeededIngredientsList() {
        Map<String, Map<String, Object>> aggregatedMap = new HashMap<>();
        LocalDate today = LocalDate.now();
        LocalDate oneWeekFromNow = today.plusDays(7);
    
        for (Map.Entry<LocalDate, List<Map<String, Object>>> entry : mealPlans.entrySet()) {
            LocalDate mealDate = entry.getKey();
    
            if (mealDate.isBefore(today) || mealDate.isAfter(oneWeekFromNow)) {
                continue;
            }
    
            for (Map<String, Object> meal : entry.getValue()) {
                if (meal == null || meal.isEmpty()) continue;
    
                Map.Entry<String, Object> blockEntry = meal.entrySet().iterator().next();
                Map<String, Object> block = (Map<String, Object>) blockEntry.getValue();
    
                if (block == null || Boolean.TRUE.equals(block.get("isMealMade"))) continue;
    
                int recipeID = block.containsKey("recipeID") ? ((Number) block.get("recipeID")).intValue() : -1;
                if (recipeID == -1) {
                    System.out.println("Skipping: Missing recipe ID");
                    continue;
                }
    
                Recipe recipe = getRecipeById(recipeID);
                if (recipe == null) {
                    System.out.println("Recipe not found for ID: " + recipeID);
                    continue;
                }
    
                System.out.println("Loaded Recipe: " + recipe.getName());
    
                for (Ingredient parsedIngredient : recipe.getIngredients()) {
                    boolean foundInInventory = false;
    
                    for (Item inventoryItem : ingredientInventory) {
                        String inventoryName = inventoryItem.getName();
                        String inventoryUnit = inventoryItem.getUnit();
                        double availableQuantity = inventoryItem.getQuantity(); // consider making quantity in Item a double
    
                        if (inventoryName.equals(parsedIngredient.getName()) &&
                            inventoryUnit.equals(parsedIngredient.getUnit())) {
    
                            double requiredQuantity = parseAmount(parsedIngredient.getAmount());
    
                            if (availableQuantity >= requiredQuantity) {
                                foundInInventory = true;
                                break;
                            }
                        }
                    }
    
                    if (!foundInInventory) {
                        String key = parsedIngredient.getName().toLowerCase() + "|" + parsedIngredient.getUnit().toLowerCase();
                        double requiredQuantity = parseAmount(parsedIngredient.getAmount());
    
                        if (aggregatedMap.containsKey(key)) {
                            Map<String, Object> existing = aggregatedMap.get(key);
                            double updatedQuantity = (double) existing.get("quantity") + requiredQuantity;
                            existing.put("quantity", updatedQuantity);
                            existing.put("meal", existing.get("meal") + ", " + block.get("name"));
                        } else {
                            Map<String, Object> neededItem = new HashMap<>();
                            neededItem.put("name", parsedIngredient.getName());
                            neededItem.put("quantity", requiredQuantity);
                            neededItem.put("unit", parsedIngredient.getUnit());
                            neededItem.put("meal", block.get("name"));
                            aggregatedMap.put(key, neededItem);
                        }
                    }
                }
            }
        }
    
        return new ArrayList<>(aggregatedMap.values());
    }

    public void refreshNeededIngredients() {
        List<Map<String, Object>> neededIngredients = generateNeededIngredientsList();

        System.out.println("=== Needed Ingredients List ===");
        for (Map<String, Object> ingredient : neededIngredients) {
            System.out.println("- " + ingredient.get("quantity") + " " +
                            ingredient.get("unit") + " of " +
                            ingredient.get("name"));
        }

        if (myListsController != null) {
            myListsController.updateNeededIngredientsList(neededIngredients);
        } else {
            System.err.println("MyListsController reference is null. Needed Ingredients update skipped.");
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

            System.out.println("Inventory successfully loaded from JSON file.");

            int maxID = ingredientInventory.stream().mapToInt(Item::getID).max().orElse(0);
            nextIngredientID = maxID + 1;

          } catch (IOException e) {
              e.printStackTrace();
              showAlert("Load Error", "Failed to load inventory from JSON file.");
          }
      } else {
          System.out.println("No inventory file found. Starting with an empty inventory.");
      }
    }

    // ======================================================================
    // Data Persistence (Saving & Loading JSON):
    // Handles reading and writing meal plans, inventory, and archived data
    // ======================================================================
    
    private void saveInventory(List<Map<String, Object>> inventory) {
        try (FileWriter writer = new FileWriter("itemInventory.json")) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(inventory, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveToJson(Object data, String fileName) {
        try (FileWriter writer = new FileWriter(fileName)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(data, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void saveMealPlansToJson() {
        try (FileWriter writer = new FileWriter("mealPlans.json")) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(mealPlans, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadMealPlansFromJson() {
        try (FileReader reader = new FileReader("mealPlans.json")) {
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                    .setPrettyPrinting()
                    .create();
    
            Type type = new TypeToken<Map<LocalDate, List<Map<String, Object>>>>() {}.getType();
            mealPlans = gson.fromJson(reader, type);
    
            if (mealPlans == null) {
                mealPlans = new HashMap<>();
            }
        } catch (IOException e) {
            mealPlans = new HashMap<>();
            e.printStackTrace();
        }
    }   


    // ==============================================================================
    // Right-Click Context Menu Actions: 
    // Handles meal editing, deletion, and marking as made via right-click options
    // ==============================================================================  

    private void addRightClickOptions(Label block, LocalDate date) {
        block.setOnContextMenuRequested(event -> {
            ContextMenu contextMenu = new ContextMenu();
            MenuItem deleteTimeBlock = new MenuItem("Delete Time Block");
            MenuItem deleteEntireMeal = new MenuItem("Delete Entire Meal");
            MenuItem editItem = new MenuItem("Edit");
            MenuItem markAsMade = new MenuItem("Mark as Made");

            deleteTimeBlock.setOnAction(e -> {
                try {
                    Map<String, String> blockData = parseBlockId(block.getId());
                    int mealId = findBlockIdFromMealPlans(blockData);
            
                    if (mealId != -1) {
                        deleteMealById(mealId);
                    } else {
                        showAlert("Error", "Unable to locate the meal block.");
                    }
            
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showAlert("Error", "Failed to delete the meal.");
                }
            });

            deleteEntireMeal.setOnAction(e -> {
                try {
                    Map<String, String> blockData = parseBlockId(block.getId());
                    String blockDate = blockData.get("date");
                    LocalDate dateTime = LocalDate.parse(blockDate);
            
                    List<Map<String, Object>> meals = mealPlans.getOrDefault(dateTime, new ArrayList<>());
            
                    String mealGroupId = blockData.get("mealGroupId");

                    meals.removeIf(meal -> {
                        for (String part : List.of("prepTime", "passiveTime", "cookTime")) {
                            if (meal.containsKey(part)) {
                                Map<String, Object> blockMap = (Map<String, Object>) meal.get(part);
                                if (blockMap != null && mealGroupId.equals(blockMap.get("mealGroupId"))) {
                                    return true;
                                }
                            }
                        }
                        return false;
                    });
                    
            
                    if (meals.isEmpty()) {
                        mealPlans.remove(date);
                    }
            
                    saveMealPlansToJson();
                    loadCalendarView();
                    refreshNeededIngredients();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showAlert("Error", "Failed to delete entire meal.");
                }
            });

            editItem.setOnAction(e -> {
                try {
                    // Parse block ID to get details about the meal
                    Map<String, String> blockData = parseBlockId(block.getId());
                    String mealName = blockData.get("mealName");
                    LocalDate oldDate = LocalDate.parse(blockData.get("date"));
                    int oldHour = Integer.parseInt(blockData.get("hour"));
            
                    // Prompt the user for new date and time (replace this with your actual UI logic)
                    LocalDate newDate = showDatePickerDialog("Select New Date", oldDate);
                    int newHour = showHourPickerDialog("Select New Time", oldHour);
            
                    // Update the mealPlans
                    List<Map<String, Object>> mealsForOldDate = mealPlans.getOrDefault(oldDate, new ArrayList<>());
                    Map<String, Object> targetMeal = null;
            
                    for (Map<String, Object> meal : mealsForOldDate) {
                        if (mealName.equals(meal.get("name"))) {
                            targetMeal = meal;
                            break;
                        }
                    }
            
                    if (targetMeal != null) {
                        // Remove from old date
                        mealsForOldDate.remove(targetMeal);
                        if (mealsForOldDate.isEmpty()) {
                            mealPlans.remove(oldDate);
                        }
            
                        // Update date and hour
                        targetMeal.put("date", newDate.toString());
                        targetMeal.put("hour", newHour);
            
                        // Add to new date
                        List<Map<String, Object>> mealsForNewDate = mealPlans.computeIfAbsent(newDate, k -> new ArrayList<>());
                        mealsForNewDate.add(targetMeal);
            
                        // Save updated mealPlans
                        saveMealPlansToJson();
            
                        // Refresh calendar view to reflect changes
                        loadCalendarView();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showAlert("Error", "Failed to edit the meal.");
                }
            });

            markAsMade.setOnAction(e -> {
                markMealAsMade(date, block.getId());
                block.setStyle(block.getStyle() + "-fx-opacity: 0.5;"); // Grey out the block
            });
    
            contextMenu.getItems().addAll(markAsMade, deleteTimeBlock, deleteEntireMeal, editItem);

            contextMenu.show(block, event.getScreenX(), event.getScreenY());
        });
    }

    private LocalDate showDatePickerDialog(String title, LocalDate defaultDate) {
        DatePicker datePicker = new DatePicker(defaultDate);
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.getDialogPane().setContent(datePicker);
        alert.showAndWait();
    
        return datePicker.getValue();
    }

    private int showHourPickerDialog(String title, int defaultHour) {
        ComboBox<Integer> hourPicker = new ComboBox<>();
        for (int i = 0; i < 24; i++) {
            hourPicker.getItems().add(i);
        }
        hourPicker.setValue(defaultHour);
    
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.getDialogPane().setContent(hourPicker);
        alert.showAndWait();
    
        return hourPicker.getValue();
    }

    private int findBlockIdFromMealPlans(Map<String, String> blockData) {
        String targetName = blockData.get("mealName");
        String timeBlockType = blockData.get("timeBlockType");
        String date = blockData.get("date");
        int hour = Integer.parseInt(blockData.get("hour"));
    
        List<Map<String, Object>> meals = mealPlans.getOrDefault(LocalDate.parse(date), new ArrayList<>());
        for (Map<String, Object> meal : meals) {
            if (meal.containsKey(timeBlockType)) {
                Map<String, Object> block = (Map<String, Object>) meal.get(timeBlockType);
                if (block != null && targetName.equals(block.get("name")) &&
                    hour == ((Number) block.get("hour")).intValue()) {
                    return ((Number) block.get("id")).intValue();
                }
            }
        }
    
        return -1; // not found
    }

    // ==========================
    // Recipe Card Methods: 
    // Get and Populate the Recipe Pane
    // ==========================

    private List<Recipe> loadRecipesFromJson() {
      File file = new File(RECIPES_FILE_PATH);
      boolean isFileValid = file.exists() && file.length() > 0;
   
      if (isFileValid) {
         try (Reader reader = new FileReader(file)) {
            Gson gson = new Gson();
            Recipe[] recipesArray = gson.fromJson(reader, Recipe[].class);
            if (recipesArray != null) {
               System.out.println("Recipes successfully loaded from JSON.");
               return new ArrayList<>(List.of(recipesArray));
            }
         } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Load Error - Failed to load recipes from JSON file.");
         }
      } else {
         System.out.println("No recipe file found. Starting with an empty recipe list.");
      }
      return new ArrayList<>();
   }

    private void loadRecipes() {
        recipeList.addAll(loadRecipesFromJson()); 
        loadRecipesCards();
     }

    private void loadRecipesCards() {
        recipeFlowPane.getChildren().clear(); // Clear any existing children before repopulating
    
        for (Recipe recipe : recipeList) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/javafx/Resources/FXMLs/RecipeCard.fxml"));
                VBox recipeCard = loader.load();
                RecipeCardController controller = loader.getController();
    
                // Load the recipe's image from the "Recipe Images" folder
                File imageFile = new File("src/main/resources/org/javafx/Resources/Recipe Images/" + recipe.getName() + ".png");
                Image image = imageFile.exists() ? new Image(imageFile.toURI().toString()) : null;
    
                controller.setRecipeData(recipe, image, this, "mealPlanner"); // Pass recipe data and image
                
                recipeFlowPane.getChildren().add(recipeCard);
                recipeWidgets.put(recipe.getID(), recipeCard);
                recipeCard.setUserData(controller);
    
                applyHoverEffect(recipeCard, recipe);
    
                if (noRecipesTXT.isVisible()) {
                    noRecipesTXT.setVisible(false);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void applyHoverEffect(VBox recipeCard, Recipe recipe) {
        Tooltip tooltip = new Tooltip();
        tooltip.setStyle(
                          "-fx-background-color: #3C3C3C; " +
                          "-fx-text-fill: white; " +
                          "-fx-padding: 10; " +
                          "-fx-font-size: 14px; " +
                          "-fx-border-radius: 10; " +
                          "-fx-background-radius: 10; " +
                          "-fx-border-color: #FF7F11;"
                       );
        
        // Set tooltip max width and enable wrapping
        tooltip.setMaxWidth(300);  // Set maximum width to prevent excessive horizontal stretching
        tooltip.setWrapText(true); // Ensure text wraps to the next line
    
        String tooltipContent = String.format(
            "Name: %s%nServings: %d%nPrep Time: %d min%nCook Time: %d min%nDescription: %s%nTags: %s",
            recipe.getName(),
            recipe.getServings(),
            recipe.getPrepTime(),
            recipe.getCookTime(),
            recipe.getDescription(),
            String.join(", ", recipe.getTags())
        );
    
        tooltip.setText(tooltipContent);
    
        // Attach tooltip explicitly to the VBox (recipe card)
        recipeCard.setOnMouseEntered(event -> {
            if (!tooltip.isShowing()) {
                tooltip.show(recipeCard, event.getScreenX(), event.getScreenY() + 10);
            }
        });
    
        recipeCard.setOnMouseExited(event -> tooltip.hide());
    
        recipeCard.setOnMouseMoved(event -> {
            tooltip.setX(event.getScreenX());
            tooltip.setY(event.getScreenY() + 10);
        });
    }

    public void showRecipeDetails(int recipeId, String recipeName, Image image, Recipe recipe) {

        mealDetailsPane.setVisible(true);
    
        mealNameTXT.setText(recipeName);
        complexityTXT.setText("Complexity: " + recipe.getComplexity());
        descriptionArea.setText(recipe.getDescription());

        descriptionArea.setWrapText(true);

        // Bind height to text content
        descriptionArea.setPrefRowCount(Math.max(5, descriptionArea.getText().split("\n").length + 1));

        descriptionArea.textProperty().addListener((obs, oldText, newText) -> {
            int lines = newText.split("\n").length + (newText.length() / 40); // crude line estimate
            descriptionArea.setPrefRowCount(Math.max(5, lines));
        });

        
        servingsTXT.setText("Servings: " + recipe.getServings());
    
        prepTimeTXT.setText("Prep Time: " + recipe.getPrepTime() + " min");
        passiveTimeTXT.setText("Passive Time: " + recipe.getPassiveTime() + " min");
        cookTimeTXT.setText("Cook Time: " + recipe.getCookTime() + " min");
    
        int totalTime = recipe.getPrepTime() + recipe.getPassiveTime() + recipe.getCookTime();
        totalTimeTXT.setText("Total Time: " + totalTime + " min");
    
        // Populate equipment list
        specialEquipmentListView.getItems().clear();
        if (recipe.getEquipment() != null) {
            specialEquipmentListView.getItems().addAll(recipe.getEquipment());
        }

        specialEquipmentListView.setFixedCellSize(40); // Adjust cell height if needed
        specialEquipmentListView.prefHeightProperty().bind(
            specialEquipmentListView.fixedCellSizeProperty().multiply(Bindings.size(specialEquipmentListView.getItems()))
        );
    
        // Populate ingredients Table
        ingredientList.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getName()));
        amountList.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getAmount() + " " + data.getValue().getUnit()));
        
        ingredients.clear();
        ingredients.addAll(recipe.getIngredients());
        
        ingredientTable.setItems(ingredients);
        ingredientTable.setEditable(false);
        ingredientList.setCellFactory(TextFieldTableCell.forTableColumn());
        amountList.setCellFactory(TextFieldTableCell.forTableColumn());

        ingredientTable.setFixedCellSize(40); // Adjust row height if needed
        ingredientTable.prefHeightProperty().bind(
            ingredientTable.fixedCellSizeProperty().multiply(Bindings.size(ingredientTable.getItems()).add(2)) // +1 for header
        );


        // Pre-select current date and a default meal slot
        mealPlanDate.setValue(LocalDate.now());
        if (mealSlot.getItems().isEmpty()) {
            mealSlot.getItems().addAll("Breakfast", "Lunch", "Dinner", "Snack");
        }
        
        this.selectedRecipe = recipe;
        
        mealSlot.setValue("Dinner"); // Default selection
        String mealTypeFromPlan = fetchMealTypeForRecipe(recipe.getID());

        if (mealTypeFromPlan != null && !mealTypeFromPlan.isEmpty()) {

            mealTypeFromPlan = mealTypeFromPlan.substring(0, 1).toUpperCase() + mealTypeFromPlan.substring(1).toLowerCase();
            mealSlot.setValue(mealTypeFromPlan);
        }

    
    }
    
    
    // ==========================
    // Utility Methods: 
    // General helper functions
    // ==========================

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private Map<String, String> parseBlockId(String blockId) {
        String[] parts = blockId.split("\\|");
        if (parts.length != 5) {
            throw new IllegalArgumentException("Invalid block ID format: " + blockId);
        }
    
        Map<String, String> parsedData = new HashMap<>();
        parsedData.put("mealName", parts[0]);
        parsedData.put("timeBlockType", parts[1]);
        parsedData.put("date", parts[2]);
        parsedData.put("hour", parts[3]);
        parsedData.put("mealGroupId", parts[4]);
        return parsedData;
    }

    private List<Map<String, Object>> getMealsForDay(LocalDate date) {
        // Retrieve the list of meals for the given date, or return an empty list if none exist
        return mealPlans.getOrDefault(date, new ArrayList<>());
    }

    private Ingredient parseIngredient(String ingredient) {
        String[] parts = ingredient.split(":");
        String name = parts[0].trim();
        String[] quantityUnit = parts[1].trim().split(" ");
        String amount = quantityUnit[0];
        String unit = quantityUnit[1];
    
        return new Ingredient(name, amount, unit);
    }

    public void setMyListController(MyListsController controller) {
        this.myListsController = controller;
    }

    private int parseHourLabel(String label) {
        String[] parts = label.split(" ");
        int hour = Integer.parseInt(parts[0]);
        String meridian = parts[1];
    
        if (meridian.equals("PM") && hour != 12) {
            hour += 12;
        } else if (meridian.equals("AM") && hour == 12) {
            hour = 0;
        }
    
        return hour;
    }

    private double parseAmount(String amountStr) {
        if (amountStr == null || amountStr.isBlank()) return 0;
    
        amountStr = amountStr.trim().toLowerCase();
    
        // Handle non-numeric keywords gracefully
        if (amountStr.matches(".*[a-z].*") && !amountStr.contains("/")) {
            return 0; // Skip values like "to taste", "pinch", "some"
        }
    
        try {
            // Handle whole number + fraction like "1 1/2"
            if (amountStr.contains(" ")) {
                String[] parts = amountStr.split(" ");
                return parseAmount(parts[0]) + parseAmount(parts[1]);
            }
    
            // Handle fractions like "1/2"
            if (amountStr.contains("/")) {
                String[] fraction = amountStr.split("/");
                if (fraction.length == 2) {
                    double numerator = Double.parseDouble(fraction[0]);
                    double denominator = Double.parseDouble(fraction[1]);
                    return numerator / denominator;
                }
            }
    
            // Otherwise, try parsing as a decimal
            return Double.parseDouble(amountStr);
    
        } catch (NumberFormatException e) {
            // If anything goes wrong, just return 0 and print debug info
            System.err.println("Could not parse amount: '" + amountStr + "'");
            return 0;
        }
    }
    
    private double roundToTwo(double val) {
        return Math.round(val * 100.0) / 100.0;
    }

    private String fetchMealTypeForRecipe(int recipeId) {
        for (Recipe recipe : recipeList) {
        
            if (recipe.getID() == recipeId) {
                return recipe.getCategory() != null ? recipe.getCategory() : "Other";
            }
        }
        return null; // If not found
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
    
    // =======================================================
    // Recipe Filters: 
    // Handles reicpe filtering options
    // =======================================================

    private void populateFilterOptions() {
        Set<String> ingredientSet = new HashSet<>();
        Set<String> tagSet = new HashSet<>();
        Set<String> categorySet = new HashSet<>();
  
        // Collect unique ingredients and tags from existing recipes
        for (Recipe recipe : recipeList) {
            for (Ingredient ingredient : recipe.getIngredients()) {
                ingredientSet.add(ingredient.getName());
            }
            
            tagSet.addAll(recipe.getTags());

           categorySet.add(capitalizeWords(recipe.getCategory() != null ? recipe.getCategory() : "Other"));
        }
  
        // Ensure default options exist if the user has no recipes
        if (ingredientSet.isEmpty()) {
           ingredientSet.addAll(Arrays.asList("Flour", "Sugar", "Salt", "Butter", "Eggs", "Milk"));
        }
        if (tagSet.isEmpty()) {
           tagSet.addAll(Arrays.asList("Vegetarian", "Vegan", "Gluten-Free", "Dairy-Free", "Spicy", "Quick Meal"));
        }
        if (categorySet.isEmpty()) {
           categorySet.addAll(Arrays.asList("Breakfast", "Lunch", "Dinner", "Snack", "Dessert", "Other"));
        }
   
  
        // Set up available options
        availableIngredients = ingredientSet;
        availableTags = tagSet;
  
        // Populate the category filter with options
        mealType.getItems().clear();
        mealType.getItems().add("All Categories"); // Default option
        mealType.getItems().addAll(categorySet);
        mealType.setValue("All Categories"); // Set the default selection
  
        selectedIngredients.clear();
        selectedTags.clear();
    }

    @FXML
    private void clearAllFilters() {
       selectedIngredients.clear();
       selectedTags.clear();
       mealType.setValue("All Categories"); // Reset to default
       sortBy.setValue("A-Z");
       filterRecipes(); // Refresh recipe list
    }

    private void setupMultiSelectFilters() {
        ingredientFilter.setOnAction(event -> {
            selectedIngredients = showMultiSelectDialog("Select Ingredients", availableIngredients, selectedIngredients);
            filterRecipes();
        });
    
        tagsFilter.setOnAction(event -> {
            selectedTags = showMultiSelectDialog("Select Tags", availableTags, selectedTags);
            filterRecipes();
        });
    
        resetFilters.setOnAction(event -> clearAllFilters());
        mealType.setOnAction(event -> filterRecipes());
    }

    private void filterRecipes() {
        String selectedCategory = mealType.getValue();
        String selectedSort = sortBy.getValue();
        String readinessFilter = mealReadiness.getValue();
    
        boolean filterByCategory = !selectedCategory.equals("All Categories");
    
        // 2) Start with all recipes
        List<Recipe> matchingRecipes = new ArrayList<>(recipeList);
    
        // 3) Filter by category, ingredients, tags
        matchingRecipes.removeIf(recipe -> {
            boolean matchesCategory = !filterByCategory || 
                (recipe.getCategory() != null && recipe.getCategory().equalsIgnoreCase(selectedCategory));
            boolean matchesIngredients = selectedIngredients.isEmpty()
                || recipe.getIngredients().stream().anyMatch(ingredient -> selectedIngredients.contains(ingredient.getName()));
            boolean matchesTags = selectedTags.isEmpty()
                || recipe.getTags().stream().anyMatch(selectedTags::contains);
            return !(matchesCategory && matchesIngredients && matchesTags);
        });
    
        // 4) Filter by meal readiness — AFTER matchingRecipes is initialized
        if (readinessFilter != null && !readinessFilter.equals("All")) {
            matchingRecipes.removeIf(recipe -> !getRecipeReadiness(recipe).equals(readinessFilter));
        }
    
        // 5) Sort the filtered list based on selectedSort
        if (selectedSort != null) {
            switch (selectedSort) {
                case "A-Z":
                    matchingRecipes.sort((r1, r2) -> r1.getName().compareToIgnoreCase(r2.getName()));
                    break;
                case "Z-A":
                    matchingRecipes.sort((r1, r2) -> r2.getName().compareToIgnoreCase(r1.getName()));
                    break;
                case "Complexity":
                    matchingRecipes.sort(Comparator.comparingInt(Recipe::getComplexity));
                    break;
                case "Prep Time":
                    matchingRecipes.sort(Comparator.comparingInt(Recipe::getPrepTime));
                    break;
                case "Cook Time":
                    matchingRecipes.sort(Comparator.comparingInt(Recipe::getCookTime));
                    break;
            }
        }
    
        // 6) Clear and rebuild the recipeFlowPane
        recipeFlowPane.getChildren().clear();
        Set<VBox> added = new HashSet<>();
        for (Recipe recipe : matchingRecipes) {
            VBox card = recipeWidgets.get(recipe.getID());
            if (card != null && added.add(card)) {
                recipeFlowPane.getChildren().add(card);
            }
        }
    }
    

    private String getRecipeReadiness(Recipe recipe) {
        int matched = 0;
        int total = recipe.getIngredients().size();
        boolean requiresPrep = false;
    
        for (Ingredient needed : recipe.getIngredients()) {
            for (Item have : ingredientInventory) {
                boolean nameMatch = have.getName().equalsIgnoreCase(needed.getName());
                boolean unitMatch = have.getUnit().equalsIgnoreCase(needed.getUnit());
                double neededAmt = parseAmount(needed.getAmount());
                double availableAmt = have.getQuantity();
    
                if (nameMatch && unitMatch && availableAmt >= neededAmt) {
                    matched++;
                    if (needed.getName().toLowerCase().contains("thaw") || needed.getAmount().toLowerCase().contains("frozen")) {
                        requiresPrep = true;
                    }
                    break;
                }
            }
        }
    
        if (matched == total && !requiresPrep) return "Ready to Cook";
        if (matched == total && requiresPrep) return "Requires Prep";
        if (matched > 0) return "Partial Recipes";
        return "Incomplete";
    }
    
    

    private Set<String> showMultiSelectDialog(String title, Set<String> availableOptions, Set<String> selectedOptions) {
        Stage dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.setTitle(title);

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));

        // Search Bar
        TextField searchField = new TextField();
        searchField.setPromptText("Search...");

        // ListView with checkboxes
        ListView<CheckBox> listView = new ListView<>();
        ObservableList<CheckBox> checkBoxes = FXCollections.observableArrayList();

        // Populate checkboxes
        for (String option : availableOptions) {
            CheckBox checkBox = new CheckBox(option);
            checkBox.setSelected(selectedOptions.contains(option));
            checkBoxes.add(checkBox);
        }

        listView.setItems(checkBoxes);

        // Search functionality
        searchField.textProperty().addListener((obs, oldText, newText) -> {
            listView.setItems(checkBoxes.filtered(cb -> cb.getText().toLowerCase().contains(newText.toLowerCase())));
        });

        // Buttons
        Button applyButton = new Button("Apply");
        Button cancelButton = new Button("Cancel");

        applyButton.setOnAction(event -> {
            selectedOptions.clear();
            for (CheckBox checkBox : checkBoxes) {
                if (checkBox.isSelected()) {
                    selectedOptions.add(checkBox.getText());
                }
            }
            dialogStage.close();
        });

        cancelButton.setOnAction(event -> dialogStage.close());

        HBox buttonBox = new HBox(10, applyButton, cancelButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        vbox.getChildren().addAll(searchField, listView, buttonBox);
        Scene scene = new Scene(vbox, 300, 400);
        dialogStage.setScene(scene);
        dialogStage.showAndWait();

        return selectedOptions;
   }

    // =======================================================
    // Visual Enhancements: 
    // Handles UI styling for past dates and time indicators
    // =======================================================

    private void addCurrentTimeIndicator(GridPane view) {
        LocalDate today = LocalDate.now();
        if (datePicker.getValue().equals(today)) {
            java.util.Timer timer = new java.util.Timer(true);
            timer.scheduleAtFixedRate(new java.util.TimerTask() {
                @Override
                public void run() {
                    javafx.application.Platform.runLater(() -> {
                        // Remove old indicator
                        view.getChildren().removeIf(node -> "time-indicator".equals(node.getId()));
    
                        // Add new indicator
                        int currentHour = java.time.LocalTime.now().getHour();
                        javafx.scene.shape.Line line = new javafx.scene.shape.Line(0, 0, view.getWidth(), 0);
                        line.setStyle("-fx-stroke: red; -fx-stroke-width: 2;");
                        line.setId("time-indicator");
                        GridPane.setConstraints(line, 1, currentHour); // Place in the current hour row
                        view.getChildren().add(line);
                    });
                }
            }, 0, 60000); // Update every minute
        }
    }

    private void stylePastDates() {
        dayView.getChildren().forEach(node -> {
            if (node instanceof Label) {
                Label block = (Label) node;
                LocalDate blockDate = getDateFromBlock(block.getId());
    
                if (blockDate != null && blockDate.isBefore(LocalDate.now())) {
                    block.setStyle(block.getStyle() + "-fx-opacity: 0.5; -fx-text-fill: gray;");
                }
            }
        });
    }
    

    private LocalDate getDateFromBlock(String blockId) {
        if (blockId == null || !blockId.contains("|")) {
            return null; // Instead of throwing an exception
        }
    
        try {
            String[] parts = blockId.split("\\|");
            return LocalDate.parse(parts[2]); // This is the 'date' part from the block ID
        } catch (Exception e) {
            System.err.println("Error parsing date from blockId: " + blockId);
            return null;
        }
    }

}
