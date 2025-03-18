package org.javafx.Controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * This class encapsulates logic for:
 * 1) Generating ingredient suggestions.
 * 2) Assigning/looking up substitutions for existing recipe ingredients.
 * 3) Holding or referencing a known substitution list.
 *
 * Adapt as needed for your actual application design (service layers, data sources, etc.).
 */
public class IngredientSuggestionAndSubstitutions {

    // -------------------------------------------------------------------------
    // Data Sources
    // -------------------------------------------------------------------------
    
    /**
     * Example flavor matrix, with each ingredient mapping to a list of complementary ingredients.
     * In a real application, you might load this from JSON, a DB, or an external API.
     */
    private Map<String, List<String>> flavorMatrix; // e.g. { "tomato": ["basil", "olive oil"], ... }

    /**
     * Example substitution map, where each ingredient points to one or more known substitutions.
     * Each substitution could be a small object with context, notes, etc.
     */
    private Map<String, List<SubstitutionOption>> knownSubstitutions; // e.g. { "egg": [Option1, Option2], ... }

    // -------------------------------------------------------------------------
    // Constructor(s)
    // -------------------------------------------------------------------------
    
    public IngredientSuggestionAndSubstitutions(Map<String, List<String>> flavorMatrix,
                                               Map<String, List<SubstitutionOption>> knownSubstitutions) {
        this.flavorMatrix = flavorMatrix;
        this.knownSubstitutions = knownSubstitutions;
    }

    // -------------------------------------------------------------------------
    // 1) Ingredient Suggestion Generation
    // -------------------------------------------------------------------------

    /**
     * Generates complementary ingredient suggestions based on a recipe's existing ingredients
     * and a user's preferences (e.g., allergies, diet, dislikes).
     * 
     * @param recipeIngredients The current recipe's ingredient list
     * @param userPrefs         The user's dietary preferences (allergies, vegan, etc.)
     * @param limit             Max number of suggestions to return
     * @return A list of ingredient suggestions
     */
    public List<String> generateIngredientSuggestions(List<String> recipeIngredients,
                                                      UserPreferences userPrefs,
                                                      int limit) {
        // Collect potential complements from the flavor matrix
        Set<String> potential = new HashSet<>();
        
        for (String recipeIng : recipeIngredients) {
            if (flavorMatrix.containsKey(recipeIng)) {
                potential.addAll(flavorMatrix.get(recipeIng));
            }
        }

        // Filter out anything the user can't or won't eat
        List<String> filtered = filterByUserPreferences(new ArrayList<>(potential), userPrefs);

        // Shuffle or sort based on your logic (random, alphabetical, scoring, etc.)
        Collections.shuffle(filtered);

        // Return up to 'limit' items
        if (filtered.size() > limit) {
            return filtered.subList(0, limit);
        } else {
            return filtered;
        }
    }

    // -------------------------------------------------------------------------
    // 2) Ingredient Substitution Assignment
    // -------------------------------------------------------------------------

    /**
     * Returns a list of substitution options for a given ingredient, tailored to a user
     * if you want to apply dietary constraints.
     * 
     * @param ingredient  The ingredient in the recipe
     * @param userPrefs   The user's dietary constraints/preferences
     * @param context     Optional context (e.g. "baking", "savory", etc.) for more precise matching
     * @return A list of possible substitutions
     */
    public List<SubstitutionOption> getSubstitutions(String ingredient,
                                                     UserPreferences userPrefs,
                                                     String context) {
        if (!knownSubstitutions.containsKey(ingredient)) {
            return Collections.emptyList();
        }

        // Filter the known subs for this ingredient by user preferences and optionally context
        List<SubstitutionOption> options = knownSubstitutions.get(ingredient).stream()
            .filter(opt -> userPrefsAllows(opt.getIngredientName(), userPrefs))  // check allergies, etc.
            .filter(opt -> context == null 
                           || "general".equalsIgnoreCase(opt.getContext()) 
                           || opt.getContext().equalsIgnoreCase(context))
            .collect(Collectors.toList());

        return options;
    }

    // -------------------------------------------------------------------------
    // 3) Known Substitution List
    // -------------------------------------------------------------------------

    /**
     * Returns all the known substitutions in the system.
     * Could be used for an admin UI or debugging.
     */
    public Map<String, List<SubstitutionOption>> getAllKnownSubstitutions() {
        return knownSubstitutions;
    }

    // -------------------------------------------------------------------------
    // Helper Methods
    // -------------------------------------------------------------------------

    /**
     * Filters out ingredients based on user preferences (e.g., allergies, dietary restrictions).
     */
    private List<String> filterByUserPreferences(List<String> candidates, UserPreferences prefs) {
        return candidates.stream()
                         .filter(ing -> userPrefsAllows(ing, prefs))
                         .collect(Collectors.toList());
    }

    private boolean userPrefsAllows(String ingredient, UserPreferences prefs) {
        // Pseudocode: check allergies, dietary restrictions, user dislikes, etc.
        if (prefs.hasAllergyTo(ingredient)) {
            return false;
        }
        if (prefs.dislikes(ingredient)) {
            return false;
        }
        // If the user is vegan, maybe you check if the ingredient is an animal product
        if (prefs.isVegan() && isAnimalProduct(ingredient)) {
            return false;
        }
        // ... other checks ...
        return true;
    }

    /**
     * Dummy method to illustrate checking if something is animal-based.
     * In a real system, you'd have a more robust check or a data-driven approach.
     */
    private boolean isAnimalProduct(String ingredient) {
        // This could be a dictionary lookup. For now, just an example:
        String lower = ingredient.toLowerCase();
        return lower.contains("meat") || lower.contains("milk") || lower.contains("egg");
    }

}


