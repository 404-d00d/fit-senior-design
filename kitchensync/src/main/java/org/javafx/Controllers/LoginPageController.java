package org.javafx.Controllers;

import org.javafx.Main.Main;

import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.transform.Scale;
import javafx.stage.Screen;

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

        // Get the user's screen dimensions
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        double screenWidth = screenBounds.getWidth();
        double screenHeight = screenBounds.getHeight();

        // Define base resolution
        double baseWidth = 1920.0;  // Base width used in design
        double baseHeight = 1080.0;  // Base height used in design

        // Calculate the scale factors for both width and height
        double scaleX = screenWidth / baseWidth;
        double scaleY = screenHeight / baseHeight;

        // Initialize a Scale transformation
        Scale scale = new Scale();

        // Set the scale based on the screen size
        scale.setX(scaleX);
        scale.setY(scaleY);

        // Set the pivot point for scaling at the top-left (0, 0)
        scale.setPivotX(0);
        scale.setPivotY(0);

        // Apply the scale transformation to the loginPane
        loginPane.getTransforms().setAll(scale);

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
