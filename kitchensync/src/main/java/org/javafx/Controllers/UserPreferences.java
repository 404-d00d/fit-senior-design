package org.javafx.Controllers;

import java.util.Set;

public class UserPreferences {

    private Set<String> allergies;   // e.g. ["nuts", "shellfish"]
    private Set<String> dislikes;    // e.g. ["cilantro", "licorice"]
    private boolean vegan;
    private boolean vegetarian;
    // ... any other fields ...

    public UserPreferences(Set<String> allergies, Set<String> dislikes, boolean vegan, boolean vegetarian) {
        this.allergies = allergies;
        this.dislikes = dislikes;
        this.vegan = vegan;
        this.vegetarian = vegetarian;
    }

    public boolean hasAllergyTo(String ingredient) {
        // Very naive check: you might want synonyms or partial matches.
        // For a real system, you'd do something more robust.
        return allergies.stream()
            .anyMatch(a -> ingredient.toLowerCase().contains(a.toLowerCase()));
    }

    public boolean dislikes(String ingredient) {
        return dislikes.stream()
            .anyMatch(d -> ingredient.toLowerCase().contains(d.toLowerCase()));
    }

    public boolean isVegan() {
        return vegan;
    }

    public boolean isVegetarian() {
        return vegetarian;
    }
    
    // getters/setters as needed
}