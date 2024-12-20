package Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import Main.Main;

public class SignUpPageController {

   @FXML
   private TextField usernameField;  // TextField for username input

   @FXML
   private TextField emailField;  // TextField for Email input

   @FXML
   private PasswordField passwordField;  // PasswordField for password input

   @FXML
   private PasswordField confirmPasswordField;

   @FXML
   private Button signUpButton;

   @FXML
   private Button loginButton;

   @FXML
    private void initialize() {
        // Bind login button action to load the login page
        loginButton.setOnAction(event -> {
            try {
                Main.showLoginScreen();  // Switch to the login screen
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Bind sign-up button action to create account and login
        signUpButton.setOnAction(event -> {
            // Add Account Creation logic here
            // Add login validation logic here
            try {
                Main.showDashboardScreen();  // Switch to the User Dashboard
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

}
