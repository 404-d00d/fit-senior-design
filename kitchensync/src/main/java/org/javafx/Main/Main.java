package org.javafx.Main;

import java.io.IOException;

import org.javafx.Controllers.MyRecipesController;
import org.javafx.Controllers.MealPlannerController;
import org.javafx.Controllers.MyListsController;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.transform.Scale;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class Main extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage primaryStage) throws Exception {

        Main.primaryStage = primaryStage;
        primaryStage.setTitle("KitchenSync");

        // Set the initial size to 1280x720
        primaryStage.setWidth(1280);
        primaryStage.setHeight(720);

        // Optionally center the stage on the screen
        primaryStage.centerOnScreen();

        showLoginScreen();  // Show the login screen on app startup
    }

    public static void showLoginScreen() throws IOException {
        Parent loginScreen = FXMLLoader.load(Main.class.getResource("/org/javafx/Resources/FXMLs/LoginPage.fxml"));
        primaryStage.setScene(new Scene(loginScreen, 1280, 720));
        setScale(loginScreen);
        primaryStage.show();
    }


    // Load the dashboard screen (user dashboard after login)
    public static void showDashboardScreen() throws IOException {
        Parent dashboardScreen = FXMLLoader.load(Main.class.getResource("/org/javafx/Resources/FXMLs/User Dashboard.fxml"));
        primaryStage.setScene(new Scene(dashboardScreen, 1280, 720));
        
        // Apply the scaling transformation to fit 1920x1080 content into 1280x720
        setScale(dashboardScreen);
        primaryStage.show();
    }

    // Load the sign-up screen
    public static void showSignUpScreen() throws IOException {
        Parent signUpScreen = FXMLLoader.load(Main.class.getResource("/org/javafx/Resources/FXMLs/Signuppage.fxml"));
        primaryStage.setScene(new Scene(signUpScreen, 1280, 720));

        // Apply the scaling transformation to fit 1920x1080 content into 1280x720
        setScale(signUpScreen);
        primaryStage.show();
    }

    // Load the inventory screen 
    public static void showInventoryScreen() throws IOException {
        Parent inventoryScreen = FXMLLoader.load(Main.class.getResource("/org/javafx/Resources/FXMLs/InventoryDashboard.fxml"));
        primaryStage.setScene(new Scene(inventoryScreen, 1280, 720));
        
        // Apply the scaling transformation to fit 1920x1080 content into 1280x720
        setScale(inventoryScreen);
        primaryStage.show();
    }

    // Load the MyRecipes screen 
    public static void showMyRecipesScreen() throws IOException {
        Parent myRecipesScreen = FXMLLoader.load(Main.class.getResource("/org/javafx/Resources/FXMLs/MyRecipes.fxml"));
        primaryStage.setScene(new Scene(myRecipesScreen, 1280, 720));

        // Apply the scaling transformation to fit 1920x1080 content into 1280x720
        setScale(myRecipesScreen);
        primaryStage.show();
    }

    // Load the MyLists screen 
    public static void showMyListsScreen() throws IOException {
        Parent myListsScreen = FXMLLoader.load(Main.class.getResource("/org/javafx/Resources/FXMLs/MyLists.fxml"));
        primaryStage.setScene(new Scene(myListsScreen, 1280, 720));

        // Apply the scaling transformation to fit 1920x1080 content into 1280x720
        setScale(myListsScreen);
        primaryStage.show();
    }

    // Load the UserDashboard screen 
    public static void showUserDashboardScreen() throws IOException {
        Parent userDashboardScreen = FXMLLoader.load(Main.class.getResource("/org/javafx/Resources/FXMLs/User Dashboard.fxml"));
        primaryStage.setScene(new Scene(userDashboardScreen, 1280, 720));

        // Apply the scaling transformation to fit 1920x1080 content into 1280x720
        setScale(userDashboardScreen);
        primaryStage.show();
    }

    // Load the MealPlanner screen 
    public static void showMealPlannerScreen() throws IOException {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("/org/javafx/Resources/FXMLs/MealPlanner.fxml"));
        Parent mealPlannerScreen = loader.load();

        // Get the controller and set up dependencies
        MealPlannerController mealPlannerController = loader.getController();
        MyListsController myListsController = new MyListsController();
        mealPlannerController.setMyListController(myListsController);

        // Apply the scaling transformation to fit 1920x1080 content into 1280x720
        setScale(mealPlannerScreen);
        primaryStage.show();
    }

    // Load the CommunityRecipes screen 
    public static void showCommunityRecipesScreen() throws IOException {
        Parent CommunityRecipesScreen = FXMLLoader.load(Main.class.getResource("/org/javafx/Resources/FXMLs/CommunityRecipes.fxml"));
        primaryStage.setScene(new Scene(CommunityRecipesScreen, 1280, 720));

        // Apply the scaling transformation to fit 1920x1080 content into 1280x720
        setScale(CommunityRecipesScreen);
        primaryStage.show();
    }

    // Load the UserInbox screen 
    public static void showInboxScreen() throws IOException {
        Parent InboxScreen = FXMLLoader.load(Main.class.getResource("/org/javafx/Resources/FXMLs/UserInbox.fxml"));
        primaryStage.setScene(new Scene(InboxScreen, 1280, 720));

        // Apply the scaling transformation to fit 1920x1080 content into 1280x720
        setScale(InboxScreen);
        primaryStage.show();
    }

    // Load the UserProfile screen 
    public static void showUserProfileScreen() throws IOException {
        Parent UserProfile = FXMLLoader.load(Main.class.getResource("/org/javafx/Resources/FXMLs/UserProfile.fxml"));
        primaryStage.setScene(new Scene(UserProfile, 1280, 720));

        // Apply the scaling transformation to fit 1920x1080 content into 1280x720
        setScale(UserProfile);
        primaryStage.show();
    }

    // Load the UserSettings screen 
    public static void showUserSettingsScreen() throws IOException {
        Parent UserSettings = FXMLLoader.load(Main.class.getResource("/org/javafx/Resources/FXMLs/UserSettings.fxml"));
        primaryStage.setScene(new Scene(UserSettings, 1280, 720));

        // Apply the scaling transformation to fit 1920x1080 content into 1280x720
        setScale(UserSettings);
        primaryStage.show();
    }

    public static void setScale(Parent root) {
        // Original design dimensions (1920x1080)
        double originalWidth = 1920;
        double originalHeight = 1080;
    
        // Add a Group to wrap the root for scaling
        Group group = new Group(root);
    
        // Create a Scale transformation
        Scale scale = new Scale(1, 1);
    
        // Add the scale transformation to the group
        group.getTransforms().add(scale);
    
        // Bind the scale factors to the primaryStage dimensions dynamically
        scale.xProperty().bind(primaryStage.widthProperty().divide(originalWidth));

        /*
        scale.xProperty().bind(Bindings.min(
            primaryStage.widthProperty().divide(originalWidth),
            primaryStage.heightProperty().divide(originalHeight)
        ));
        scale.yProperty().bind(scale.xProperty()); // Maintain uniform scaling
        */

        scale.yProperty().bind(primaryStage.heightProperty().divide(originalHeight));
    
        // Create a Scene with the Group
        Scene scene = new Scene(group, originalWidth, originalHeight);
    
        // Set the scene on the primary stage
        primaryStage.setScene(scene);
    
        // Ensure resizable
        primaryStage.setResizable(true);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
