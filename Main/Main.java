package Main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        loadScreen("/resources/User Dashboard.fxml");
    }
    
    //testing thing 

    //@Override
    //public void start(Stage primaryStage) throws Exception {
        //FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/UploadOptions.fxml"));
        //Scene scene = new Scene(loader.load());
        //primaryStage.setScene(scene);
        //primaryStage.setTitle("Upload Options");
        //primaryStage.show();
    //}

    // Method to load and switch between screens
    public void loadScreen(String fxmlFile) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
        Parent screen = loader.load();
        Scene scene = new Scene(screen);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}