package Main;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        Main.primaryStage = primaryStage;
        showLoginScreen();  // Show the login screen on app startup
    }

    // Load the login screen
    public static void showLoginScreen() throws IOException {
        Parent loginScreen = FXMLLoader.load(Main.class.getResource("/resources/LoginPage.fxml"));
        primaryStage.setScene(new Scene(loginScreen));
        primaryStage.show();
    }

    // Load the dashboard screen (user dashboard after login)
    public static void showDashboardScreen() throws IOException {
        Parent dashboardScreen = FXMLLoader.load(Main.class.getResource("/resources/User Dashboard.fxml"));
        primaryStage.setScene(new Scene(dashboardScreen));
        primaryStage.show();
    }

    // Load the sign-up screen
    public static void showSignUpScreen() throws IOException {
        Parent signUpScreen = FXMLLoader.load(Main.class.getResource("/resources/Signuppage.fxml"));
        primaryStage.setScene(new Scene(signUpScreen));
        primaryStage.show();
    }

    // Load the inventory screen 
    public static void showInventoryScreen() throws IOException {
        Parent inventoryScreen = FXMLLoader.load(Main.class.getResource("/resources/InventoryDashboard.fxml"));
        primaryStage.setScene(new Scene(inventoryScreen));
        primaryStage.show();
    }

    // Load the MyRecipes screen 
    public static void showMyRecipesScreen() throws IOException {
        Parent myRecipesScreen = FXMLLoader.load(Main.class.getResource("/resources/MyRecipes.fxml"));
        primaryStage.setScene(new Scene(myRecipesScreen));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
