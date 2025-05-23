module org.javafx {
    requires transitive javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;

    // requires software.amazon.awssdk.dynamodb;
    requires software.amazon.awssdk.auth;
    requires com.google.gson;

    // AWS SDK DynamoDB
    requires software.amazon.awssdk.services.dynamodb;
    requires software.amazon.awssdk.regions;
    requires software.amazon.awssdk.core;
    requires software.amazon.awssdk.services.s3;
    
    requires software.amazon.awssdk.services.iam;
    requires javafx.base;

    opens org.javafx.Item to com.google.gson;
    opens org.javafx.Recipe to com.google.gson;
    

    // Opens for reflection and FXML loading
    opens org.javafx.Controllers to javafx.fxml, com.google.gson;

    // Open the Main package to javafx.graphics for JavaFX runtime access
    opens org.javafx.Main to javafx.graphics;

    exports org.javafx.Main;
}
