package org.javafx.Controllers;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.javafx.Main.Main;
import org.javafx.Recipe.Recipe;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.ColumnConstraints;

public class MealPlannerController {

    @FXML
    private ComboBox<String> calanderViewDropdown, nutritionalMeals;

    @FXML
    private Button addMealButton, userDashboardButton, mealPlannerButton, myRecipesButton, inventoryButton,
                    inboxButton, browseRecipesButton, profileButton, settingsButton, myListsButton, menuButton;

    @FXML
    private DatePicker datePicker, dateInView;

    @FXML
    private Text breakfastMeal, lunchMeal, dinnerMeal, snacksMeal;

    @FXML
    private PieChart dailyNutritionalBreakdown;

    @FXML
    private Pane menuPane, calanderPane;

    @FXML
    private GridPane dayView, weekView, monthView;

    private Map<LocalDate, List<Recipe>> mealPlans = new HashMap<>();

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("d'th'");

    private Label draggedBlock;

    @FXML
    private void initialize() {

        menuButton.setOnAction(event -> toggleMenuPane());
        
        // Setup navigation buttons
        setNavigationButtonHandlers();
        setHoverEffect(userDashboardButton);
        setHoverEffect(mealPlannerButton);
        setHoverEffect(myRecipesButton);
        setHoverEffect(inventoryButton);
        setHoverEffect(inboxButton);
        setHoverEffect(browseRecipesButton);
        setHoverEffect(profileButton);
        setHoverEffect(settingsButton);

        // Set default view for calendar
        calanderViewDropdown.getItems().addAll("Day View", "Week View", "Month View");
        calanderViewDropdown.setValue("Week View");

        // Initialize date pickers with current date
        LocalDate currentDate = LocalDate.now();
        datePicker.setValue(currentDate);
        datePicker.setStyle("-fx-font-size: 18px;");
        dateInView.setValue(currentDate);
        dateInView.setStyle("-fx-font-size: 18px;");

        // Load initial data
        loadDailyMeals(currentDate);
        loadCalendarView();

        // Add listener to update view when dropdown changes
        calanderViewDropdown.setOnAction(event -> loadCalendarView());

        // Add listener to update the date in view when a new date is picked
        dateInView.setOnAction(event -> updateDateInView());

        // Set up nutritional breakdown combo box options
        nutritionalMeals.getItems().addAll("Breakfast", "Lunch", "Dinner", "Snacks", "All Meals", "Total Per Serving");
        nutritionalMeals.setOnAction(event -> updateNutritionalBreakdown());

        // Initialize add meal button
        addMealButton.setOnAction(event -> openAddMealDialog());

        // Setup date picker for day selection
        datePicker.setValue(LocalDate.now());
        datePicker.setOnAction(event -> loadDailyMeals(datePicker.getValue()));

        // Load initial data
        loadDailyMeals(LocalDate.now());
        loadCalendarView(); // Ensure datePicker is properly set before loading the view

        // Initialize add meal button
        addMealButton.setOnAction(event -> openAddMealDialog());
    }

    private void toggleMenuPane() {
        try {
            menuPane.setVisible(!menuPane.isVisible());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
                case "Inventory":
                    Main.showInventoryScreen();
                    break;
                case "MyRecipes":
                    Main.showMyRecipesScreen();
                    break;
                case "CommunityRecipes":
                    Main.showCommunityRecipesScreen();
                    break;
                case "MyLists":
                    Main.showMyListsScreen();
                    break;
                case "UserDashboard":
                    Main.showUserDashboardScreen();
                    break;
                default:
                    System.out.println("Screen not implemented yet: " + screen);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    private void updateDateInView() {
        LocalDate selectedDate = dateInView.getValue();
        if (selectedDate != null) {
            datePicker.setValue(selectedDate);
            loadCalendarView();
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
        dayView.getChildren().clear();  // Clear previous content
        dayView.getRowConstraints().clear(); // Clear previous row constraints
        dayView.getColumnConstraints().clear();  // Clear previous column constraints

        // Set up a new column to the far left for displaying the hours of the day
        ColumnConstraints timeColumn = new ColumnConstraints();
        timeColumn.setPercentWidth(15); // Set 10% of width for the time labels
        dayView.getColumnConstraints().add(timeColumn);

        // Add a column for actual meal planning (rest of the width)
        ColumnConstraints contentColumn = new ColumnConstraints();
        contentColumn.setPercentWidth(85);
        dayView.getColumnConstraints().add(contentColumn);

        // Set up rows for each hour of the day
        for (int i = 0; i < 25; i++) {
            RowConstraints rowConstraints = new RowConstraints();
            rowConstraints.setPercentHeight(100.0 / 25);
            dayView.getRowConstraints().add(rowConstraints);
        }

        LocalDate selectedDate = dateInView.getValue();
        String dayOfWeek = selectedDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        String dayLabel = dayOfWeek + ", " + DATE_FORMATTER.format(selectedDate);

        // Place the day title in column 1, row 0
        Text dayTitle = new Text(dayLabel);
        dayTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        GridPane.setConstraints(dayTitle, 1, 0);
        dayView.getChildren().add(dayTitle);

        // Add hour labels to the far left column
        for (int hour = 0; hour < 24; hour++) {
            String hourText = String.format("%02d:00", hour);  // Display in HH:00 format
            Label hourLabel = new Label(hourText);
            hourLabel.setStyle("-fx-font-size: 14px; -fx-padding: 5px; -fx-font-weight: bold;");
            GridPane.setConstraints(hourLabel, 0, hour + 1);  // +1 to account for the title row
            dayView.getChildren().add(hourLabel);
        }

        // Load meals for the selected day
        List<Recipe> mealsForDay = getMealsForDay(selectedDate);
        for (Recipe meal : mealsForDay) {
            // Add the time blocks for each meal to the day view
            createTimeBlocksForMeal(selectedDate, meal, "Lunch", 1); // Assume "Lunch" for now; this can be made dynamic
        }

        dayView.setGridLinesVisible(false);
        dayView.setGridLinesVisible(true);
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
            String dayLabel = dayOfWeek + ", " + DATE_FORMATTER.format(currentDate);
            Text dayTitle = new Text(dayLabel);
            dayTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
            GridPane.setConstraints(dayTitle, i + 1, 0); // +1 to skip the hour column
            weekView.getChildren().add(dayTitle);
        }

        // Add meal blocks to each day in the week view
        for (int i = 0; i < 7; i++) {
            LocalDate currentDate = startDate.plusDays(i);
            List<Recipe> mealsForDay = getMealsForDay(currentDate);

            for (Recipe meal : mealsForDay) {
                // Add the time blocks for each meal to the week view
                createTimeBlocksForMeal(currentDate, meal, "Lunch", i + 1); // Pass the appropriate column for the day
            }
        }

        weekView.setGridLinesVisible(false);
        weekView.setGridLinesVisible(true);
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

        // Get the first day of the selected month or current month if none is selected
        LocalDate currentMonth = dateInView.getValue() != null ? dateInView.getValue().withDayOfMonth(1) : LocalDate.now().withDayOfMonth(1);
        int daysInMonth = currentMonth.lengthOfMonth();
    
        // Determine which column the first day of the month falls on (Sunday = 0, Saturday = 6)
        DayOfWeek firstDayOfWeek = currentMonth.getDayOfWeek();
        int startColumn = firstDayOfWeek.getValue() % 7; // DayOfWeek starts from Monday (1) to Sunday (7)

        int dayCount = 1;
        boolean startAddingDays = false;

        // Iterate through rows and columns to fill the calendar grid
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 7; col++) {
                if (row == 0 && col == startColumn) {
                    startAddingDays = true; // Start adding days when reaching the correct starting column
                }

                if (startAddingDays && dayCount <= daysInMonth) {
                    LocalDate dayDate = currentMonth.withDayOfMonth(dayCount);
                    List<Recipe> mealsForDay = getMealsForDay(dayDate);

                    // Create a VBox to hold the day number and meal labels
                    VBox dayBox = new VBox();
                    dayBox.setStyle("-fx-padding: 5px;");

                    // Add day number
                    Text dayNumber = new Text(String.valueOf(dayCount));
                    dayNumber.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
                    dayBox.getChildren().add(dayNumber);

                    // Limit number of displayed meals to avoid overflow
                    int maxMealsToShow = 3;
                    int mealCount = 0;
                    for (Recipe meal : mealsForDay) {
                        if (mealCount < maxMealsToShow) {
                            Label mealLabel = new Label(meal.getName());
                            mealLabel.setStyle("-fx-font-size: 12px;");
                            dayBox.getChildren().add(mealLabel);
                            mealCount++;
                        } else {
                            break;
                        }
                    }

                    // Add an "additional items" indicator if there are more meals than can be displayed
                    if (mealsForDay.size() > maxMealsToShow) {
                        Label moreLabel = new Label("+" + (mealsForDay.size() - maxMealsToShow) + " more");
                        moreLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: gray;");
                        dayBox.getChildren().add(moreLabel);
                    }

                    // Add the VBox to the month view
                    GridPane.setConstraints(dayBox, col, row);
                    monthView.getChildren().add(dayBox);

                    dayCount++;
                }
            }
        }

        monthView.setGridLinesVisible(false);
        monthView.setGridLinesVisible(true);
    }
    
    private void addMealToPlan(LocalDate date, Recipe meal) {
        mealPlans.computeIfAbsent(date, k -> new ArrayList<>()).add(meal);
        //saveMealPlansToJson();
    }

    private List<Recipe> getMealsForDay(LocalDate date) {
        return mealPlans.getOrDefault(date, new ArrayList<>());
    }    

    private void loadDailyMeals(LocalDate date) {
        List<Recipe> mealsForDay = getMealsForDay(date);
    
        // Assuming you have breakfast, lunch, dinner, and snacks
        breakfastMeal.setText("Breakfast: " + getMealName(mealsForDay, 0));
        lunchMeal.setText("Lunch: " + getMealName(mealsForDay, 1));
        dinnerMeal.setText("Dinner: " + getMealName(mealsForDay, 2));
        snacksMeal.setText("Snacks: " + getMealName(mealsForDay, 3));
    }
    
    private String getMealName(List<Recipe> meals, int index) {
        if (index < meals.size()) {
            return meals.get(index).getName();
        }
        return "[No Meal Planned]";
    }

    private void updateNutritionalBreakdown() {
        String selectedMeal = nutritionalMeals.getValue();
        // TODO: Implement logic to update the PieChart dailyNutritionalBreakdown
        // based on selectedMeal (e.g., "Breakfast", "Total Per Serving")
    }

    private void openAddMealDialog() {
        // TODO: Implement a dialog where the user can select the meal and times.
    
        // Once a meal is selected, create meal time blocks:
        LocalDate selectedDate = datePicker.getValue();
    
        Recipe newMeal = new Recipe(0, "Sample Meal", null, null, null, 30, 60, 30, 0, 0, null, null, null, null); // Replace with selected meal details
        addMealToPlan(selectedDate, newMeal);
        
        loadDailyMeals(selectedDate);
        loadCalendarView();
        updateNutritionalBreakdown();
    }

    private void createTimeBlocksForMeal(LocalDate date, Recipe meal, String mealSlot, int dayColumn) {
        int startHour;
    
        // Determine starting row based on meal slot (e.g., Breakfast, Lunch, etc.)
        switch (mealSlot) {
            case "Breakfast":
                startHour = 8; // Breakfast starts at 8 AM
                break;
            case "Lunch":
                startHour = 12; // Lunch starts at 12 PM (noon)
                break;
            case "Dinner":
                startHour = 18; // Dinner starts at 6 PM
                break;
            default:
                startHour = 8; // Default to breakfast if none is specified
                break;
        }
    
        // Starting row (+1 for title row)
        int currentRow = startHour + 1;
    
        // Determine which view to add the meal to: dayView or weekView
        GridPane targetView = dayView.isVisible() ? dayView : weekView;
    
        // Add Prep Time Block
        if (meal.getPrepTime() > 0) {
            Label prepBlock = new Label(meal.getName() + " - Prep: " + meal.getPrepTime() + " mins");
            prepBlock.setStyle("-fx-background-color: lightblue; -fx-padding: 5px; -fx-font-size: 15px;");
            prepBlock.setId(meal.getName() + "-prep-" + dayColumn + "-" + currentRow); // Set unique ID for drag and drop
    
            // Remove existing block if already added to avoid duplicate children
            targetView.getChildren().remove(prepBlock);
            makeTimeBlockDraggable(prepBlock, targetView);
    
            int rowSpan = (int) Math.ceil(meal.getPrepTime() / 60.0);
            GridPane.setConstraints(prepBlock, dayColumn, currentRow); // Set to appropriate row
            GridPane.setRowSpan(prepBlock, rowSpan); // Span multiple rows for the duration of the time block
            targetView.getChildren().add(prepBlock);
            currentRow += rowSpan;
        }
    
        // Add Passive Time Block (if applicable)
        if (meal.getPassiveTime() > 0) {
            Label passiveBlock = new Label(meal.getName() + " - Passive: " + meal.getPassiveTime() + " mins");
            passiveBlock.setStyle("-fx-background-color: lightgreen; -fx-padding: 5px; -fx-font-size: 15px;");
            passiveBlock.setId(meal.getName() + "-passive-" + dayColumn + "-" + currentRow); // Set unique ID for drag and drop
    
            // Remove existing block if already added to avoid duplicate children
            targetView.getChildren().remove(passiveBlock);
            makeTimeBlockDraggable(passiveBlock, targetView);
    
            int rowSpan = (int) Math.ceil(meal.getPassiveTime() / 60.0);
            GridPane.setConstraints(passiveBlock, dayColumn, currentRow);
            GridPane.setRowSpan(passiveBlock, rowSpan);
            targetView.getChildren().add(passiveBlock);
            currentRow += rowSpan;
        }
    
        // Add Cook Time Block
        if (meal.getCookTime() > 0) {
            Label cookBlock = new Label(meal.getName() + " - Cook: " + meal.getCookTime() + " mins");
            cookBlock.setStyle("-fx-background-color: salmon; -fx-padding: 5px; -fx-font-size: 15px;");
            cookBlock.setId(meal.getName() + "-cook-" + dayColumn + "-" + currentRow); // Set unique ID for drag and drop
    
            // Remove existing block if already added to avoid duplicate children
            targetView.getChildren().remove(cookBlock);
            makeTimeBlockDraggable(cookBlock, targetView);
    
            int rowSpan = (int) Math.ceil(meal.getCookTime() / 60.0);
            GridPane.setConstraints(cookBlock, dayColumn, currentRow);
            GridPane.setRowSpan(cookBlock, rowSpan);
            targetView.getChildren().add(cookBlock);
        }
    
        // Refresh grid lines to ensure proper visualization
        targetView.setGridLinesVisible(false);
        targetView.setGridLinesVisible(true);
    }

    private void makeTimeBlockDraggable(Label block, GridPane targetView) {
        // Start Drag
        block.setOnDragDetected(event -> {
            javafx.scene.input.Dragboard db = block.startDragAndDrop(TransferMode.MOVE);
            javafx.scene.image.WritableImage snapshot = block.snapshot(null, null);
            db.setDragView(snapshot);
            javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
            content.putString(block.getId()); // Use block ID to uniquely identify the block
            db.setContent(content);
            event.consume();

            // Store the dragged block in the class-level variable
            draggedBlock = block;

            System.out.println(draggedBlock);
        });

        // Allow Drag Over on the target view (dayView or weekView)
        targetView.setOnDragOver(event -> {
            if (event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });

        // Handle Drop Event on the target view
        targetView.setOnDragDropped(event -> {
            javafx.scene.input.Dragboard db = event.getDragboard();
            boolean success = false;

            if (db.hasString() && draggedBlock != null) {
                // Calculate the new row index based on the Y-coordinate of the drop event
                double y = event.getY();
                double rowHeight = targetView.getHeight() / targetView.getRowConstraints().size(); // Assuming consistent row height
                int newRow = (int) Math.floor(y / rowHeight) + 1; // Calculate the row index

                // Ensure the new row is within valid bounds
                if (newRow >= 1 && newRow < targetView.getRowConstraints().size()) { // Avoid header row and ensure within bounds
                    targetView.getChildren().remove(draggedBlock); // Remove block from old position
                    GridPane.setRowIndex(draggedBlock, newRow); // Set new row index
                    targetView.getChildren().add(draggedBlock); // Add block to the new position
                    success = true;
                }
            }

            event.setDropCompleted(success);
            event.consume();

            // Clear the reference after drop
            draggedBlock = null;
        });
    }
    

    private void saveMealPlansToJson() {
        try (FileWriter writer = new FileWriter("mealPlans.json")) {
            Gson gson = new Gson();
            gson.toJson(mealPlans, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadMealPlansFromJson() {
        try (FileReader reader = new FileReader("mealPlans.json")) {
            Gson gson = new Gson();
            Type type = new TypeToken<Map<LocalDate, List<Recipe>>>() {}.getType();
            mealPlans = gson.fromJson(reader, type);
            if (mealPlans == null) {
                mealPlans = new HashMap<>();
            }
        } catch (IOException e) {
            mealPlans = new HashMap<>();
            System.err.println("Could not load meal plans: " + e.getMessage());
        }
    }

}
