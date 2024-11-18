package org.javafx.Controllers;

import java.time.LocalDate;

import org.javafx.Main.Main;

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
   private DatePicker datePicker;

   @FXML
   private Text breakfastMeal, lunchMeal, dinnerMeal, snacksMeal;

   @FXML
   private PieChart dailyNutritionalBreakdown;

   @FXML
   private Pane menuPane, calanderPane;

   @FXML
   private GridPane dayView, weekView, monthView;

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
      loadCalendarView();

      // Add listener to update view when dropdown changes
      calanderViewDropdown.setOnAction(event -> loadCalendarView());

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
      // TODO: Implement the day view with drag-and-drop functionality for meals
   }

  private void loadWeekView() {
      // TODO: Implement the week view with time slots for meals
   }

  private void loadMonthView() {
      // TODO: Implement the month view showing an overview of meals
   }

  private void loadDailyMeals(LocalDate date) {
      // TODO: Implement logic to load daily meals into breakfastMeal, lunchMeal, etc.
      breakfastMeal.setText("Breakfast: [Meal for " + date.toString() + "]");
      lunchMeal.setText("Lunch: [Meal for " + date.toString() + "]");
      dinnerMeal.setText("Dinner: [Meal for " + date.toString() + "]");
      snacksMeal.setText("Snacks: [Meal for " + date.toString() + "]");
   }

  private void updateNutritionalBreakdown() {
      String selectedMeal = nutritionalMeals.getValue();
      // TODO: Implement logic to update the PieChart dailyNutritionalBreakdown
      // based on selectedMeal (e.g., "Breakfast", "Total Per Serving")
   }

  private void openAddMealDialog() {
      // TODO: Implement the add meal dialog, allowing users to select recipes or recommend meals
   }
}
