package org.javafx.Controllers;

public class SubstitutionOption {
    
    private String ingredientName;  // e.g. "almond milk", "flax + water"
    private String context;        // e.g. "baking", "general", "moisture"
    private String notes;          // e.g. "Adds a mild nutty flavor"

    public SubstitutionOption(String ingredientName, String context, String notes) {
        this.ingredientName = ingredientName;
        this.context = context;
        this.notes = notes;
    }

    public String getIngredientName() {
        return ingredientName;
    }
    public String getContext() {
        return context;
    }
    public String getNotes() {
        return notes;
    }
    
    // Optionally, you might have setters, builder pattern, etc.
}