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
        dayView.getRowConstraints().clear();

        // Set up rows for each hour of the day
        for (int i = 0; i < 25; i++) {
            RowConstraints rowConstraints = new RowConstraints();
            rowConstraints.setPercentHeight(100.0 / 25);
            dayView.getRowConstraints().add(rowConstraints);
        }

        LocalDate selectedDate = datePicker.getValue();
        String dayOfWeek = selectedDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        String dayLabel = dayOfWeek + ", " + DATE_FORMATTER.format(selectedDate);

        Text dayTitle = new Text(dayLabel);
        dayTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        GridPane.setConstraints(dayTitle, 0, 0);
        dayView.getChildren().add(dayTitle);

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

        // Set up 7 columns for the days of the week
        for (int i = 0; i < 7; i++) {
            ColumnConstraints colConstraints = new ColumnConstraints();
            colConstraints.setPercentWidth(100.0 / 7);
            weekView.getColumnConstraints().add(colConstraints);
        }

        LocalDate startDate = datePicker.getValue();
    
        for (int i = 0; i < 7; i++) {
            LocalDate currentDate = startDate.plusDays(i);
            List<Recipe> mealsForDay = getMealsForDay(currentDate);

            int row = 1;
            for (Recipe meal : mealsForDay) {
                Text mealBlock = new Text(meal.getName() + "\nPrep: " + meal.getPrepTime() + " mins\nCook: " + meal.getCookTime() + " mins");
                GridPane.setConstraints(mealBlock, 0, row++);
                dayView.getChildren().add(mealBlock);
            }

            String dayOfWeek = currentDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
            String dayLabel = dayOfWeek + ", " + DATE_FORMATTER.format(currentDate);
            Text dayTitle = new Text(dayLabel);

            dayTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
            GridPane.setConstraints(dayTitle, i, 0);
            weekView.getChildren().add(dayTitle);
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
    
        // Create the time blocks for the meal and add them to the day view
        createTimeBlocksForMeal(selectedDate, newMeal, "Lunch");
        
        loadDailyMeals(selectedDate);
        loadCalendarView();
        updateNutritionalBreakdown();
    }

    private void createTimeBlocksForMeal(LocalDate date, Recipe meal, String mealSlot) {
        // Determine the starting hour based on the meal slot
        int startHour;
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
    
        // Add Prep Time Block
        if (meal.getPrepTime() > 0) {
            Label prepBlock = new Label(meal.getName() + " - Prep: " + meal.getPrepTime() + " mins");
            prepBlock.setStyle("-fx-background-color: lightblue; -fx-padding: 5px; -fx-font-size: 14px;");
            makeTimeBlockDraggable(prepBlock);
            int rowForPrep = startHour + 1; // +1 to account for the header row
            GridPane.setConstraints(prepBlock, 0, 1);
            dayView.getChildren().add(prepBlock);
        }
    
        // Add Passive Time Block (if applicable)
        if (meal.getPassiveTime() > 0) {
            Label passiveBlock = new Label(meal.getName() + " - Passive: " + meal.getPassiveTime() + " mins");
            passiveBlock.setStyle("-fx-background-color: lightgreen; -fx-padding: 5px; -fx-font-size: 14px;");
            makeTimeBlockDraggable(passiveBlock);
            int rowForPassive = startHour + (meal.getPrepTime() / 60) + 1; // Starts after prep time
            GridPane.setConstraints(passiveBlock, 0, rowForPassive);
            dayView.getChildren().add(passiveBlock);
        }
    
        // Add Cook Time Block
        if (meal.getCookTime() > 0) {
            Label cookBlock = new Label(meal.getName() + " - Cook: " + meal.getCookTime() + " mins");
            cookBlock.setStyle("-fx-background-color: salmon; -fx-padding: 5px; -fx-font-size: 14px;");
            makeTimeBlockDraggable(cookBlock);
            int rowForCook = startHour + (meal.getPrepTime() + meal.getPassiveTime()) / 60 + 1; // Starts after prep and passive times
            GridPane.setConstraints(cookBlock, 0, rowForCook);
            dayView.getChildren().add(cookBlock);
        }
    
        // Refresh grid lines to ensure proper visualization
        dayView.setGridLinesVisible(false);
        dayView.setGridLinesVisible(true);
    }

    private void makeTimeBlockDraggable(Label block) {
        block.setOnDragDetected(event -> {
            javafx.scene.input.Dragboard db = block.startDragAndDrop(TransferMode.MOVE);
            javafx.scene.image.WritableImage snapshot = block.snapshot(null, null);
            db.setDragView(snapshot);
            javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
            content.putString(block.getText());
            db.setContent(content);
            event.consume();
        });
    
        block.setOnDragOver(event -> {
            if (event.getGestureSource() != block && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });
    
        block.setOnDragDropped(event -> {
            javafx.scene.input.Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                ((Label) event.getGestureTarget()).setText(db.getString());
                success = true;
                // Adjust position to handle half-hour placement using setLayoutY()
                block.setLayoutY(event.getY());
            }
            event.setDropCompleted(success);
            event.consume();
        });
    
        block.setOnDragDone(event -> {
            if (event.getTransferMode() == TransferMode.MOVE) {
                ((Pane) block.getParent()).getChildren().remove(block);
            }
            event.consume();
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
