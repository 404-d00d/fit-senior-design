package Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import Main.Main;

public class LoginPageController {

   @FXML
   private TextField usernameField;  // TextField for username input

   @FXML
   private PasswordField passwordField;  // PasswordField for password input

   @FXML
   private Button loginButton;

   @FXML
   private Button signUpButton;

   @FXML
    private void initialize() {
        // Bind login button action to load the dashboard
        loginButton.setOnAction(event -> {
            // Add login validation logic here
            try {
                Main.showDashboardScreen();  // Switch to the dashboard screen after login
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Bind sign-up button action to load the sign-up screen
        signUpButton.setOnAction(event -> {
            try {
                Main.showSignUpScreen();  // Switch to the sign-up screen
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
