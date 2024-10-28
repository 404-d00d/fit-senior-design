package org.javafx.Main;

import java.io.IOException;

import org.javafx.Controllers.MyRecipesController;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.transform.Scale;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class Main extends Application {

    private static Stage primaryStage;
    private static MyRecipesController myRecipesController;

    @Override
    public void start(Stage primaryStage) throws Exception {
        Main.primaryStage = primaryStage;
        primaryStage.setTitle("KitchenSync");
        showLoginScreen();  // Show the login screen on app startup
    }

    // Load the login screen
    public static void showLoginScreen() throws IOException {
        Parent loginScreen = FXMLLoader.load(Main.class.getResource("/org/javafx/Resources/LoginPage.fxml"));
        primaryStage.setScene(new Scene(loginScreen, 1280, 720));
        primaryStage.setResizable(true);
        primaryStage.show();
    }

    // Load the dashboard screen (user dashboard after login)
    public static void showDashboardScreen() throws IOException {
        Parent dashboardScreen = FXMLLoader.load(Main.class.getResource("/org/javafx/Resources/User Dashboard.fxml"));
        primaryStage.setScene(new Scene(dashboardScreen, 1280, 720));
        primaryStage.show();
    }

    // Load the sign-up screen
    public static void showSignUpScreen() throws IOException {
        Parent signUpScreen = FXMLLoader.load(Main.class.getResource("/org/javafx/Resources/Signuppage.fxml"));
        primaryStage.setScene(new Scene(signUpScreen, 1280, 720));
        primaryStage.show();
    }

    // Load the inventory screen 
    public static void showInventoryScreen() throws IOException {
        Parent inventoryScreen = FXMLLoader.load(Main.class.getResource("/org/javafx/Resources/InventoryDashboard.fxml"));
        primaryStage.setScene(new Scene(inventoryScreen, 1280, 720));
        primaryStage.show();
    }

    // Load the MyRecipes screen 
    public static void showMyRecipesScreen() throws IOException {
        Parent myRecipesScreen = FXMLLoader.load(Main.class.getResource("/org/javafx/Resources/MyRecipes.fxml"));
        primaryStage.setScene(new Scene(myRecipesScreen, 1280, 720));
        primaryStage.show();
    }

    // Load the MyLists screen 
    public static void showMyListsScreen() throws IOException {
        Parent myListsScreen = FXMLLoader.load(Main.class.getResource("/org/javafx/Resources/MyLists.fxml"));
        primaryStage.setScene(new Scene(myListsScreen, 1280, 720));
        primaryStage.show();
    }

    public static void setScale(Pane pane) {
        // Get the user's screen dimensions
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        double screenWidth = screenBounds.getWidth();
        double screenHeight = screenBounds.getHeight();

        // Define base resolution
        double baseWidth = 1280;  // Base width used in design
        double baseHeight = 720;  // Base height used in design

        // Calculate the scale factors for both width and height
        double scaleX = baseWidth / screenWidth;
        double scaleY = baseHeight / screenHeight;

        // Initialize a Scale transformation
        Scale scale = new Scale();

        // Set the scale based on the screen size
        scale.setX(scaleX);
        scale.setY(scaleY);

        // Set the pivot point for scaling at the top-left (0, 0)
        scale.setPivotX(0);
        scale.setPivotY(0);

        System.err.println(screenWidth/baseWidth);
        System.err.println(scaleX);

        // Apply the scale transformation to the loginPane
        pane.getTransforms().setAll(scale);
    }



    public static void main(String[] args) {
        launch(args);
    }
}
