package org.javafx.Controllers;

import org.javafx.Main.Main;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

public class LoginPageController {

   @FXML
   private TextField usernameField;  // TextField for username input
   @FXML
   private PasswordField passwordField;  // PasswordField for password input

   @FXML
   private Text usernameTXT;
   @FXML
   private Text loginTXT;
   @FXML
   private Text passwordTXT;

   @FXML
   private Rectangle backgroundLogin;
   @FXML
   private Separator seperatorLogin;
   @FXML
   private Circle circleLogin;

   @FXML
   private Button loginButton;
   @FXML
   private Button signUpButton;

   @FXML
   private AnchorPane loginPane;

   @FXML
    private void initialize() {

        //Main.setScale(loginPane);

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
