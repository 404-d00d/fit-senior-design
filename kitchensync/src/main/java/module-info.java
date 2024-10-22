module org.javafx {
    requires transitive javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;

    // Opens for reflection and FXML loading
    opens org.javafx.Controllers to javafx.fxml;

    // Open the Main package to javafx.graphics for JavaFX runtime access
    opens org.javafx.Main to javafx.graphics;

    exports org.javafx.Main;
}
