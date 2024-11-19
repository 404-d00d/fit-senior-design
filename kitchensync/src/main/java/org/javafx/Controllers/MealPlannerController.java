package org.javafx.Controllers;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;


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
        dateInView.setValue(currentDate);

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
        
        LocalDate selectedDate = datePicker.getValue();
        List<Recipe> mealsForDay = getMealsForDay(selectedDate);
        
        int row = 0;
        for (Recipe meal : mealsForDay) {
            Text mealBlock = new Text(meal.getName() + "\nPrep: " + meal.getPrepTime() + " mins\nCook: " + meal.getCookTime() + " mins");
            GridPane.setConstraints(mealBlock, 0, row++);
            dayView.getChildren().add(mealBlock);
        }

        dayView.setGridLinesVisible(false);
        dayView.setGridLinesVisible(true);
    }
    
    private void loadWeekView() {
        weekView.getChildren().clear();
        
        LocalDate startDate = datePicker.getValue();
    
        for (int i = 0; i < 7; i++) {
            LocalDate currentDate = startDate.plusDays(i);
            List<Recipe> mealsForDay = getMealsForDay(currentDate);
            StringBuilder mealSummary = new StringBuilder(currentDate.getDayOfWeek().toString()).append("\n");
            
            for (Recipe meal : mealsForDay) {
                mealSummary.append(meal.getName()).append("\n");
            }

            Text dayBlock = new Text(mealSummary.toString());
            dayBlock.setStyle("-fx-font-size: 16px; -fx-text-fill: black;");
            GridPane.setConstraints(dayBlock, i, 0);
            weekView.getChildren().add(dayBlock);
        }

        weekView.setGridLinesVisible(false);
        weekView.setGridLinesVisible(true);
    }
    
    private void loadMonthView() {
        monthView.getChildren().clear();
        
        LocalDate currentMonth = dateInView.getValue().withDayOfMonth(1);
        int daysInMonth = currentMonth.lengthOfMonth();
    
        int dayCount = 1;
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 7; col++) {
                if (dayCount > daysInMonth) break;

                LocalDate dayDate = currentMonth.withDayOfMonth(dayCount);
                List<Recipe> mealsForDay = getMealsForDay(dayDate);
                StringBuilder mealSummary = new StringBuilder(dayDate.getDayOfWeek().toString()).append("\n");

                for (Recipe meal : mealsForDay) {
                    mealSummary.append(meal.getName()).append("\n");
                }

                Text dayBlock = new Text(mealSummary.toString());
                dayBlock.setStyle("-fx-font-size: 16px; -fx-text-fill: black;");
                dayBlock.setWrappingWidth(150); // Set wrapping to handle longer texts
                GridPane.setConstraints(dayBlock, col, row);
                monthView.getChildren().add(dayBlock);

                dayCount++;
            }
        }

        monthView.setGridLinesVisible(false);
        monthView.setGridLinesVisible(true);
    }
    
    private void addMealToPlan(LocalDate date, Recipe meal) {
        mealPlans.computeIfAbsent(date, k -> new ArrayList<>()).add(meal);
        saveMealPlansToJson();
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
        // TODO: Implement the dialog to let the user choose a meal

        // Once a meal is selected:
        LocalDate selectedDate = datePicker.getValue();

        Recipe newMeal = new Recipe(0, "Sample Meal", null, null, null, 30, 60, 30, 0, 0, null, null, null, null); // Replace with selected meal details
        addMealToPlan(selectedDate, newMeal);
    
        loadDailyMeals(selectedDate);
        loadCalendarView();
        updateNutritionalBreakdown();
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
