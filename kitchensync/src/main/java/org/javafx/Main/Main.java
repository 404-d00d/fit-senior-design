package org.javafx.Main;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;

public class Main extends Application {

    private static Stage primaryStage;

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


        // Apply the scaling transformation to fit 1920x1080 content into 1280x720
        setScale(loginScreen);

        primaryStage.show();
    }

    // Load the dashboard screen (user dashboard after login)
    public static void showDashboardScreen() throws IOException {
        Parent dashboardScreen = FXMLLoader.load(Main.class.getResource("/org/javafx/Resources/User Dashboard.fxml"));
        primaryStage.setScene(new Scene(dashboardScreen, 1280, 720));
        
        // Apply the scaling transformation to fit 1920x1080 content into 1280x720
        setScale(dashboardScreen);
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
        
        // Apply the scaling transformation to fit 1920x1080 content into 1280x720
        setScale(inventoryScreen);
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

    public static void setScale(Parent root) {
        // Original design dimensions (1920x1080)
        double originalWidth = 1920;
        double originalHeight = 1080;

        // Target dimensions (1280x720)
        double targetWidth = 1280;
        double targetHeight = 720;

        // Calculate scale factors for width and height
        double scaleX = targetWidth / originalWidth;
        double scaleY = targetHeight / originalHeight;

        // Use the smaller scale factor to maintain the aspect ratio
        double scaleFactor = Math.min(scaleX, scaleY);

        // Apply the scaling transformation
        Scale scale = new Scale(scaleFactor, scaleFactor, 0, 0);
        root.getTransforms().setAll(scale);
    }



    public static void main(String[] args) {
        launch(args);
    }
}
