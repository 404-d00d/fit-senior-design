package org.javafx.Controllers;

import javafx.scene.control.ListCell;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import org.javafx.Item.Item;

public class IngredientCell extends ListCell<Item> {
    private HBox content;
    private Text nameText;
    private Text quantityText;
    private Text unitText;
    private CheckBox purchasedCheckBox;
    private Button deleteButton;

    public IngredientCell() {
        super();

        // Initialize the cell elements
        nameText = new Text();
        nameText.setFont(Font.font(30));
        quantityText = new Text();
        quantityText.setFont(Font.font(30));
        unitText = new Text();
        unitText.setFont(Font.font(30));

        purchasedCheckBox = new CheckBox();
        deleteButton = new Button("X");

        // Use an empty region to push delete button to the right
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Set up the layout and styling
        HBox textBox = new HBox(nameText, quantityText, unitText);
        textBox.setSpacing(10);

        content = new HBox(purchasedCheckBox, textBox, spacer, deleteButton);
        content.setSpacing(15);
        content.setMinHeight(40); // Set minimum height for row
        content.setStyle("-fx-padding: 10; -fx-alignment: CENTER_LEFT;"); // Add padding and alignment

        setGraphic(content);
    }

    @Override
    protected void updateItem(Item item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            // Update the text with ingredient details
            nameText.setText(item.getName());
            quantityText.setText(item.getQuantity() + " ");
            unitText.setText(item.getUnit());

            // Configure the purchased checkbox
            purchasedCheckBox.setSelected(false);
            purchasedCheckBox.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
                if (isNowSelected) {
                    nameText.setStyle("-fx-strikethrough: true; -fx-fill: grey;");
                    quantityText.setStyle("-fx-strikethrough: true; -fx-fill: grey;");
                    unitText.setStyle("-fx-strikethrough: true; -fx-fill: grey;");
                } else {
                    nameText.setStyle(null);
                    quantityText.setStyle(null);
                    unitText.setStyle(null);
                }
            });

            // Configure delete button action
            deleteButton.setOnAction(event -> getListView().getItems().remove(item));

            setGraphic(content);
        }
    }
}
