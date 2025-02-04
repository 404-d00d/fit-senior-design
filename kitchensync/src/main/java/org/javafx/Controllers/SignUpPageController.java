package org.javafx.Controllers;

import org.javafx.Main.Main;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;


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
   private Pane signUpPane;

   private boolean validSignUp = false;

   private void saveUserToJson(String username, String email, String password) {
        File file = new File("userLoginData.json");
        List<Map<String, String>> users = new ArrayList<>();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        // Load existing users if the file exists
        if (file.exists() && file.length() > 0) {
            try (FileReader reader = new FileReader(file)) {
                Type listType = new TypeToken<List<Map<String, String>>>() {}.getType();
                users = gson.fromJson(reader, listType);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Check if the username or email already exists
        for (Map<String, String> user : users) {
            if (user.get("username").equals(username)) {
                System.out.println("Username already exists. Choose a different username.");
                return;
            }
            if (user.get("email").equals(email)) {
                System.out.println("Email already exists. Use a different email.");
                return;
            }
        }

        // Create a new user and add it to the list
        Map<String, String> newUser = new HashMap<>();
        newUser.put("username", username);
        newUser.put("email", email);
        newUser.put("password", password);
        users.add(newUser);

        // Write the updated list back to the file
        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(users, writer);
            System.out.println("User account saved successfully!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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

        //Main.setScale(signUpPane);

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

            boolean usernameTaken = false;

            System.out.println(usernameField.getText());
            System.out.println(emailField.getText());
            System.out.println(passwordField.getText());
            System.out.println(confirmPasswordField.getText());
            
            String username = usernameField.getText();
            String email = emailField.getText(); 
            String password = passwordField.getText();
            // place code to check if username is not in dB, if it is then it is taken
            // passwords must match to run code
            for (Map<String, String> usernames : userdata) {
                if (usernames.get("username").equals(username)) {
                    usernameTaken = true;
                    break;
                }
            }

            if (usernameTaken) {
                System.out.println("Username is already taken, try again.");
            }
            else if (!passwordField.getText().equals(confirmPasswordField.getText())) {
                System.out.println("Passwords do not match, try again.");
            }
            // ensure there are no empty fields in code
            else if (usernameField.getText().isEmpty() ||
                        emailField.getText().isEmpty() ||
                        passwordField.getText().isEmpty() ||
                        confirmPasswordField.getText().isEmpty()) {
                System.out.println("There is at least one empty field, try again.");
            }
            // account Creation logic here
            else {
                System.out.println("Account creation criteria met, continue.");
                validSignUp = true;
                saveUserToJson(username, email, password);
            }
            // Add login validation logic here
            if (validSignUp) {
                try {
                    Main.showDashboardScreen();  // Switch to the User Dashboard
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
