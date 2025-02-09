package org.javafx.Controllers;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.javafx.Main.Main;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

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

    private List<Map<String, String>> loadUsersFromJson() {
        File file = new File("userLoginData.json");
    
        if (!file.exists() || file.length() == 0) {
            System.out.println("User data file not found or empty.");
            return new ArrayList<>();
        }
    
        try (FileReader reader = new FileReader(file)) {
            Gson gson = new Gson();
            Type listType = new TypeToken<List<Map<String, String>>>() {}.getType();
            return gson.fromJson(reader, listType);
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

   @FXML
    private void initialize() {

        List<Map<String, String>> userdata = loadUsersFromJson();
        

        //Main.setScale(loginPane);

        // Bind login button action to load the dashboard
        loginButton.setOnAction(event -> {
            // Add login validation logic here

            boolean loginCheck = false;

            for (Map<String, String> logins : userdata) {
                if (usernameField.getText().equals(logins.get("username")) && passwordField.getText().equals(logins.get("password"))) {
                    System.out.println("Login Credentials match");
                    loginCheck = true;
                    break;
                }
            }
            if (loginCheck) {
                try {
                    Main.showDashboardScreen();  // Switch to the dashboard screen after login
            
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else {
                System.out.println("Your login credentials do not match");
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
