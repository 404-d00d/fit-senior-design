package org.javafx.Controllers;

// Java Imports
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;

// External Libraries
import org.javafx.Main.Main;
import org.javafx.Recipe.Recipe;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

// JavaFX Imports
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.text.Text;


// ==============================================================================
//  MealPlannerController handles meal planning, navigation, and UI interactions
// ==============================================================================

public class MealPlannerController {

    // ==================
    // FXML UI COMPONENTS
    // ==================

    @FXML private ComboBox<String> calanderViewDropdown, nutritionalMeals, mealSlot;
    @FXML private Button addMealButton, userDashboardButton, mealPlannerButton, myRecipesButton, inventoryButton,
                        closeButton, inboxButton, browseRecipesButton, profileButton, settingsButton, myListsButton, 
                        menuButton, addMealToPlan, prevStep, nextStep, closeRecipeButton;
    @FXML private DatePicker datePicker, dateInView, mealPlanDate;
    @FXML private PieChart dailyNutritionalBreakdown;
    @FXML private ScrollPane mealDetailsPane;
    @FXML private Text mealNameTXT, complecityTXT, servingsTXT, prepTimeTXT, passiveTimeTXT, cookTimeTXT, totalTimeTXT, 
                        stepOfTXT, recipeCookingNameTXT, breakfastMeal, lunchMeal, dinnerMeal, snacksMeal;
    @FXML private ListView<String> specialEqupmentListView, recipeIngredientsListView, ingredientsArea;
    @FXML private TextArea descriptionArea, stepArea;
    @FXML private Pane menuPane, calanderPane, AddMealMenu, recipeCookingPane;
    @FXML private GridPane dayView, weekView, monthView;
    @FXML private ImageView recipeImages;

    // ==============================
    // DATA STORAGE & CONSTANTS
    // ==============================
    
    private Map<LocalDate, List<Map<String, Object>>> mealPlans = new HashMap<>();
    private Label draggedBlock;
    private MyListsController myListsController;
    private int currentStep = 0, displayStep = 0;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("d'th'");

    // ====================================================================
    // INITIALIZATION
    // These methods handle the controller setup when the FXML is loaded
    // ====================================================================

    @FXML
    private void initialize() {

        // Load meal plans from JSON
        loadMealPlansFromJson();

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
        calanderViewDropdown.getItems().addAll("Day View", "Week View", "Month View");
        calanderViewDropdown.setValue("Week View");
        datePicker.setValue(LocalDate.now());
        datePicker.setStyle("-fx-font-size: 18px;");
        dateInView.setValue(LocalDate.now());
        dateInView.setStyle("-fx-font-size: 18px;");
        
        // Load the default views
        loadDailyMeals(LocalDate.now());
        loadCalendarView();
        calculateMealNutrition();

        // UI event handlers
        calanderViewDropdown.setOnAction(event -> loadCalendarView());
        dateInView.setOnAction(event -> updateDateInView());
        nutritionalMeals.getItems().addAll("Breakfast", "Lunch", "Dinner", "Snacks", "All Meals", "Total Per Serving");
        nutritionalMeals.setOnAction(event -> calculateMealNutrition());
        addMealButton.setOnAction(event -> addMeal());
        closeButton.setOnAction(event -> closeMealDetails());
        closeRecipeButton.setOnAction(event -> closeRecipeCookingPane());
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
        calanderPane.setVisible(true);
    }

    private void closeRecipeCookingPane() {
        calanderPane.setVisible(true);
        recipeCookingPane.setVisible(false);
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
        String selectedView = calanderViewDropdown.getValue();
    
        // Hide all views initially
        dayView.setVisible(false);
        weekView.setVisible(false);
        monthView.setVisible(false);
    
        // Show the appropriate view based on the selection
        switch (selectedView) {
            case "Day View":
                dayView.setVisible(true);
                loadDayView();
                break;
            case "Week View":
                weekView.setVisible(true);
                loadWeekView();
                break;
            case "Month View":
                monthView.setVisible(true);
                loadMonthView();
                break;
        }
    }

    private void loadDayView() {
        dayView.getChildren().clear(); // Clear previous content
        dayView.getRowConstraints().clear(); // Clear row constraints
        dayView.getColumnConstraints().clear(); // Clear column constraints
    
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
    
        // Add time labels to the left column
        for (int hour = 0; hour < 24; hour++) {
            Label timeLabel = new Label(String.format("%02d:00", hour)); // Format as "HH:00"
            timeLabel.setStyle("-fx-font-size: 14px; -fx-padding: 5px; -fx-font-weight: bold;");
            GridPane.setConstraints(timeLabel, 0, hour); // Column 0, corresponding row
            dayView.getChildren().add(timeLabel);
        }
    
        // Load meals for the selected day and render them
        List<Map<String, Object>> mealsForDay = mealPlans.getOrDefault(datePicker.getValue(), new ArrayList<>());

        System.out.println(mealsForDay);
    
        for (Map<String, Object> mealData : mealsForDay) {
            createTimeBlocksForMeal(mealData, dayView, 1); // Column 1 for meals
        }
    
        dayView.setGridLinesVisible(false); // Refresh grid lines
        dayView.setGridLinesVisible(true);

        stylePastDates();
    }
    
    private void loadWeekView() {
        weekView.getChildren().clear();
        weekView.getRowConstraints().clear();
        weekView.getColumnConstraints().clear();
    
        // Set up 25 rows for 24 hours and one row for the day label
        for (int i = 0; i < 25; i++) {
            RowConstraints rowConstraints = new RowConstraints();
            rowConstraints.setPercentHeight(100.0 / 25);
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
    
        // Add hour labels in the left-most column
        for (int hour = 0; hour < 24; hour++) {
            String hourText = String.format("%02d:00", hour); // Display in HH:00 format
            Label hourLabel = new Label(hourText);
            hourLabel.setStyle("-fx-font-size: 14px; -fx-padding: 5px; -fx-font-weight: bold;");
            GridPane.setConstraints(hourLabel, 0, hour + 1); // +1 to account for the title row
            weekView.getChildren().add(hourLabel);
        }
    
        // Add day labels at the top row
        for (int i = 0; i < 7; i++) {
            LocalDate currentDate = startDate.plusDays(i);
            String dayOfWeek = currentDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
            String dayLabel = dayOfWeek + ", " + currentDate.format(DateTimeFormatter.ofPattern("MM/dd"));
            Text dayTitle = new Text(dayLabel);
            dayTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
            GridPane.setConstraints(dayTitle, i + 1, 0); // +1 to skip the hour column
            weekView.getChildren().add(dayTitle);
    
            // Get meals for the current day
            List<Map<String, Object>> mealsForDay = mealPlans.getOrDefault(currentDate, new ArrayList<>());

            
    
            // Render time blocks for meals
            for (Map<String, Object> mealData : mealsForDay) {
                createTimeBlocksForMeal(mealData, weekView, i + 1); // Add blocks to the correct day column
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
                    dayBox.setStyle("-fx-padding: 5px;");
    
                    // Add day number
                    Text dayNumber = new Text(String.valueOf(dayCount));
                    dayNumber.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
                    dayBox.getChildren().add(dayNumber);
    
                    // Display meals
                    int mealCount = 0;
                    for (Map<String, Object> mealData : mealsForDay) {
                        if (mealCount < 3) { // Limit to 3 meals per day
                            String mealName = (String) mealData.get("name");
                            Label mealLabel = new Label(mealName);
                            mealLabel.setStyle("-fx-font-size: 12px;");
                            dayBox.getChildren().add(mealLabel);
                            mealCount++;
                        } else {
                            break;
                        }
                    }
    
                    if (mealCount > 3) {
                        Label moreLabel = new Label("+" + (mealCount - 3) + " more");
                        moreLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: gray;");
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
                        String mealName = (String) mealData.get("name");
                        if (hour >= 7 && hour < 10) {
                            breakfast = mealName;
                        } else if (hour >= 12 && hour < 14) {
                            lunch = mealName;
                        } else if (hour >= 18 && hour < 20) {
                            dinner = mealName;
                        } else {
                            snacks = mealName;
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
        // TODO: Implement a dialog where the user can select the meal and times.
    
        // Once a meal is selected, create meal time blocks:
        LocalDate selectedDate = datePicker.getValue();
    
        Recipe newMeal = new Recipe(0, "Sample Meal", null, null, null, 30, 60, 30, 0, 0, null, null, null, null); // Replace with selected meal details
        addMealToPlan(newMeal, selectedDate, 0, selectedDate, 1, selectedDate, 2);
        
        loadDailyMeals(selectedDate);
        loadCalendarView();
        calculateMealNutrition();
    }

    private void addMealToPlan(Recipe meal, LocalDate prepDate, int prepHour, LocalDate passiveDate, int passiveHour, LocalDate cookDate, int cookHour) {
        // Add prepTime block
        if (meal.getPrepTime() > 0) {
            addTimeBlock("prepTime", meal, prepDate, prepHour, meal.getPrepTime());
        }
        // Add passiveTime block
        if (meal.getPassiveTime() > 0) {
            addTimeBlock("passiveTime", meal, passiveDate, passiveHour, meal.getPassiveTime());
        }
        // Add cookTime block
        if (meal.getCookTime() > 0) {
            addTimeBlock("cookTime", meal, cookDate, cookHour, meal.getCookTime());
        }
        saveMealPlansToJson();
    }

    private void addMeal() {
        openAddMealDialog();
        calculateMealNutrition();
    }

    private void deleteMealFromPlan(String mealName) {
        for (LocalDate date : new ArrayList<>(mealPlans.keySet())) { // Iterate over a copy of the keys
            List<Map<String, Object>> mealsForDay = mealPlans.get(date);
            if (mealsForDay != null) {
                mealsForDay.removeIf(meal -> {
                    // Remove the meal if the name matches
                    return mealName.equals(meal.get("name"));
                });
                // Remove the date entry if no meals are left
                if (mealsForDay.isEmpty()) {
                    mealPlans.remove(date);
                }
            }
        }
        saveMealPlansToJson(); // Persist changes to the JSON file
        loadCalendarView(); // Refresh UI
        calculateMealNutrition();
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

    private void addTimeBlock(String blockType, Recipe meal, LocalDate date, int hour, int duration) {
        Map<String, Object> timeBlock = new HashMap<>();
        timeBlock.put("id", meal.getID());
        timeBlock.put("recipeID", meal.getID());
        timeBlock.put("name", meal.getName());
        timeBlock.put("isMealMade", false);
        timeBlock.put("date", date.toString());
        timeBlock.put("duration", duration);
        timeBlock.put("hour", hour);
    
        List<Map<String, Object>> mealsForDay = mealPlans.computeIfAbsent(date, k -> new ArrayList<>());
        mealsForDay.add(Map.of(blockType, timeBlock));
    }

    private void createTimeBlocksForMeal(Map<String, Object> mealData, GridPane targetView, int dayColumn) {

        for (String timePartKey : List.of("prepTime", "passiveTime", "cookTime")) {
            Map<String, Object> timePart = (Map<String, Object>) mealData.get(timePartKey);
    
            if (timePart != null) {
                String blockDate = (String) timePart.get("date");
                int hour = ((Number) timePart.get("hour")).intValue();
                int duration = ((Number) timePart.get("duration")).intValue();
    
                if (duration > 0) {
                    // If the block's date is not the same as the current day's date, skip it
                    LocalDate blockLocalDate = LocalDate.parse(blockDate);
                    if (!blockLocalDate.equals(datePicker.getValue())) {
                        continue;
                    }
    
                    // Assign colors based on the time part
                    String color;
                    switch (timePartKey) {
                        case "prepTime":
                            color = "lightgreen";
                            break;
                        case "passiveTime":
                            color = "lightyellow";
                            break;
                        case "cookTime":
                            color = "lightcoral";
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
            String blockId = String.join("|", mealName, timePartKey, blockDate, String.valueOf(hour));
    
            Label block = new Label(mealName + " (" + timePartKey + ": " + duration + " mins)");
            block.setStyle("-fx-background-color: " + color + "; -fx-padding: 10px; -fx-font-size: 14px; -fx-border-radius: 5px;");
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
        calanderPane.setVisible(false);

        displayStep = 0;

        recipeCookingNameTXT.setText(name);

        // Populate ingredient list with checkboxes
        ingredientsArea.getItems().clear();
        for (String ingredient : recipe.getIngredients()) {
            ingredientsArea.getItems().add("⬜ " + ingredient);
        }
        recipeImages.setImage(image);

        ObservableList<String> preparationSteps = FXCollections.observableArrayList(recipe.getSteps());

        stepOfTXT.setText("Step " + 1 + " of " + preparationSteps.size());
        stepArea.setText(preparationSteps.get(0));

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
                        // Fetch an image (assuming path is stored in the recipe)
                        //Image mealImage = new Image("file:" + recipe.getImagePath());
    
                        // Open recipe details with extracted info
                        System.out.println("show details");
                        showRecipeDetails(mealName, null, recipe);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert("Error", "Failed to open recipe details.");
                }
            }
        });
    }

    private void openRecipeDetails(Recipe recipe) {
        // Set recipe details in the mealDetailsPane
        mealNameTXT.setText(recipe.getName());
        complecityTXT.setText(String.valueOf(recipe.getComplexity()));
        servingsTXT.setText("Servings: " + String.valueOf(recipe.getServings()));
        prepTimeTXT.setText("Prep Time: " + String.valueOf(recipe.getPrepTime()));
        passiveTimeTXT.setText("Passive Time: " + String.valueOf(recipe.getPassiveTime()));
        cookTimeTXT.setText("Cook Time: " + String.valueOf(recipe.getCookTime()));

        int totalTime = recipe.getPrepTime() + recipe.getPassiveTime() + recipe.getCookTime();

        totalTimeTXT.setText("Total Time: " + String.valueOf(totalTime));
    
        // Populate ingredients
        recipeIngredientsListView.getItems().clear();
        recipeIngredientsListView.getItems().addAll(recipe.getIngredients());
    
        // Populate special equipment
        specialEqupmentListView.getItems().clear();
        specialEqupmentListView.setItems(FXCollections.observableArrayList(recipe.getEquipment()));
    
        // Set description
        descriptionArea.setText(recipe.getDescription());
    
        // Show the details pane
        mealDetailsPane.setVisible(true);
    }

    private Recipe getRecipeById(int id) {
        try (FileReader reader = new FileReader("recipes.json")) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<Recipe>>() {}.getType();
            List<Recipe> recipes = gson.fromJson(reader, type);

            for (Recipe recipe : recipes) {
                System.out.println("Checking recipe: " + recipe.getID());
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
                System.out.println("Checking recipe: " + recipe.getName());
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
                for (String ingredient : recipe.getIngredients()) {
                    Ingredient parsedIngredient = parseIngredient(ingredient);
                    Map<String, Object> ingredientData = new HashMap<>();
                    ingredientData.put("name", parsedIngredient.getName());
                    ingredientData.put("amount", Double.parseDouble(parsedIngredient.getAmount()));
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
                    totalCalories += (double) ingredient.get("calories");
                    totalProtein += (double) ingredient.get("protein");
                    totalCarbs += (double) ingredient.get("carbs");
                    totalFats += (double) ingredient.get("fats");
                }
            }
            
            // Update PieChart UI with labels containing values
            dailyNutritionalBreakdown.getData().add(new PieChart.Data("Protein: " + totalProtein + "g", totalProtein));
            dailyNutritionalBreakdown.getData().add(new PieChart.Data("Carbs: " + totalCarbs + "g", totalCarbs));
            dailyNutritionalBreakdown.getData().add(new PieChart.Data("Fats: " + totalFats + "g", totalFats));

            // Add total calories as a tooltip over the pie chart
            Tooltip calorieTooltip = new Tooltip("Total Calories: " + totalCalories + " kcal");
            Tooltip.install(dailyNutritionalBreakdown, calorieTooltip);

            for (PieChart.Data data : dailyNutritionalBreakdown.getData()) {
                data.getNode().setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
            }
    
        } else {
            System.out.println("Failed to fetch nutrition data.");
        }
    }
    
    private void deductFromInventory(Ingredient ingredient) {
        List<Map<String, Object>> inventory = getInventory();
        for (Map<String, Object> item : inventory) {
            if (item.get("name").equals(ingredient.getName()) &&
                item.get("unit").equals(ingredient.getUnit())) {
                int availableQuantity = Integer.parseInt((String) item.get("quantity"));
                int requiredQuantity = Integer.parseInt(ingredient.getAmount());
                if (availableQuantity >= requiredQuantity) {
                    item.put("quantity", String.valueOf(availableQuantity - requiredQuantity));
                } else {
                    // Handle case where inventory is insufficient
                    System.out.println("Not enough inventory for " + ingredient.getName());
                }
                break;
            }
        }
        saveInventory(inventory);
    }

    private List<Map<String, Object>> generateNeededIngredientsList() {
        List<Map<String, Object>> neededIngredients = new ArrayList<>();
    
        for (Map.Entry<LocalDate, List<Map<String, Object>>> entry : mealPlans.entrySet()) {
            for (Map<String, Object> meal : entry.getValue()) {
                // Skip if the meal is already made
                if (Boolean.TRUE.equals(meal.get("isMealMade"))) {
                    continue;
                }
    
                int recipeID = meal.containsKey("recipeID") ? (int) meal.get("recipeID") : -1;
                if (recipeID == -1) {
                    continue; // Skip invalid meal entries
                }
    
                Recipe recipe = getRecipeById(recipeID);
                if (recipe != null) {
                    for (String ingredient : recipe.getIngredients()) {
                        Ingredient parsedIngredient = parseIngredient(ingredient);
    
                        // Check inventory for the ingredient
                        boolean foundInInventory = false;
                        for (Map<String, Object> inventoryItem : getInventory()) {
                            String inventoryName = (String) inventoryItem.getOrDefault("name", "");
                            String inventoryUnit = (String) inventoryItem.getOrDefault("unit", "");
                            int availableQuantity = inventoryItem.get("quantity") instanceof Number
                                    ? ((Number) inventoryItem.get("quantity")).intValue()
                                    : 0;
    
                            if (inventoryName.equals(parsedIngredient.getName()) &&
                                inventoryUnit.equals(parsedIngredient.getUnit())) {
    
                                int requiredQuantity = parsedIngredient.getAmount().contains(".")
                                        ? (int) Math.ceil(Double.parseDouble(parsedIngredient.getAmount()))
                                        : Integer.parseInt(parsedIngredient.getAmount());
    
                                if (availableQuantity >= requiredQuantity) {
                                    foundInInventory = true;
                                    break;
                                }
                            }
                        }
    
                        if (!foundInInventory) {
                            // Add ingredient to the needed list
                            Map<String, Object> neededItem = new HashMap<>();
                            neededItem.put("name", parsedIngredient.getName());
                            neededItem.put("quantity", parsedIngredient.getAmount());
                            neededItem.put("unit", parsedIngredient.getUnit());
                            neededItem.put("meal", meal.get("name"));
                            neededIngredients.add(neededItem);
                        }
                    }
                }
            }
        }
        return neededIngredients;
    }

    public void refreshNeededIngredients() {
        List<Map<String, Object>> neededIngredients = generateNeededIngredientsList();
        if (myListsController != null) {
            myListsController.updateNeededIngredientsList(neededIngredients);
        } else {
            System.err.println("MyListsController reference is null. Needed Ingredients update skipped.");
        }
    }

    private List<Map<String, Object>> getInventory() {
        // Load inventory from the JSON file or database
        try (FileReader reader = new FileReader("itemInventory.json")) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<Map<String, Object>>>() {}.getType();
            return gson.fromJson(reader, type);
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
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
            MenuItem deleteItem = new MenuItem("Delete");
            MenuItem editItem = new MenuItem("Edit");
            MenuItem markAsMade = new MenuItem("Mark as Made");

            deleteItem.setOnAction(e -> {
                try {
                    // Extract meal name from block ID
                    Map<String, String> blockData = parseBlockId(block.getId());
                    String mealName = blockData.get("mealName");
    
                    // Delete all time blocks for the meal
                    deleteMealFromPlan(mealName);
    
                    // Update the UI
                    loadCalendarView();
                    refreshNeededIngredients();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showAlert("Error", "Failed to delete the meal.");
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
    
            contextMenu.getItems().addAll(markAsMade, editItem, deleteItem);
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
        if (parts.length != 4) {
            throw new IllegalArgumentException("Invalid block ID format: " + blockId);
        }
    
        Map<String, String> parsedData = new HashMap<>();
        parsedData.put("mealName", parts[0]);
        parsedData.put("timeBlockType", parts[1]);
        parsedData.put("date", parts[2]);
        parsedData.put("hour", parts[3]);
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
                LocalDate blockDate = getDateFromBlock(block); // Implement this to parse date from block
                if (blockDate.isBefore(LocalDate.now())) {
                    block.setStyle(block.getStyle() + "-fx-opacity: 0.5;");
                }
            }
        });
    }

    private LocalDate getDateFromBlock(Label block) {
        String blockId = block.getId(); // Format: "mealName-date-hour"
        String[] parts = blockId.split("-");
        if (parts.length >= 3) {
            try {
                return LocalDate.parse(parts[1], DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }


}

