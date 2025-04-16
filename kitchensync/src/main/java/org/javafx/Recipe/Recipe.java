package org.javafx.Recipe;

import java.util.ArrayList;
import java.util.List;
import org.javafx.Controllers.Ingredient;

public class Recipe {
    private int id;
    private String name;
    private String category;
    private String collection;
    private int servings;
    private String description;
    private int prepTime;
    private int passiveTime;
    private int cookTime;
    private int complexity;
    
    private List<String> tags;
    private List<String> specialEquipment;
    private List<Ingredient> ingredients; 
    private List<String> steps;

    private int localRating;
    private int communityRating;
    private String recipeNotes;
    private List<String> suggestedIngredients;
    private int randIngredientID;

    private String userId;    // The ID of the user who owns this recipe
    private String recipeDBId; // e.g. primary key in DynamoDB (UUID) for that user

    public Recipe(int id, String name, String category, String collection, String description, int prepTime, int passiveTime, int cookTime, int complexity, int servings, 
                    List<String> tags, ArrayList<Ingredient> ingredients, List<String> specialEquipment, List<String> steps) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.collection = collection;
        this.description = description;
        this.prepTime = prepTime;
        this.passiveTime = passiveTime;
        this.cookTime = cookTime;
        this.complexity = complexity;
        this.servings = servings;
        this.tags = tags;
        this.ingredients = ingredients;
        this.specialEquipment = specialEquipment;
        this.steps = steps;

        this.localRating = 0;
        this.communityRating = 0;
        this.recipeNotes = null;

        this.randIngredientID = 0;
        this.suggestedIngredients = null;

        this.userId = null;
        this.recipeDBId = null;

    }

	 // Getters and setters for all fields
    public int getID() { return id; }
    public void setID(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getCollection() { return collection; }
    public void setCollection(String collection) { this.collection = collection; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getPrepTime() { return prepTime; }
    public void setPrepTime(int prepTime) { this.prepTime = prepTime; }

    public int getPassiveTime() { return passiveTime; }
    public void setPassiveTime(int passiveTime) { this.passiveTime = passiveTime; }

    public int getCookTime() { return cookTime; }
    public void setCookTime(int cookTime) { this.cookTime = cookTime; }

    public int getComplexity() { return complexity; }
    public void setComplexity(int complexity) { this.complexity = complexity; }

    public int getServings() { return servings; }
    public void setServings(int servings) { this.servings = servings; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public List<Ingredient> getIngredients() { return ingredients; }
    public void setIngredients(List<Ingredient> ingredients) { this.ingredients = ingredients; }

    public List<String> getEquipment() { return specialEquipment; }
    public void setEquipment(List<String> equipment) { this.specialEquipment = equipment; }

    public List<String> getSteps() { return steps; }
    public void setSteps(List<String> steps) { this.steps = steps; }

    public int getLocalRating() { return localRating; }
    public void setLocalRating(int localRating) { this.localRating = localRating; }

    public int getCommunityRating() { return communityRating; }
    public void setCommunityRating(int communityRating) { this.communityRating = communityRating; }

    public String getRecipeNotes() { return recipeNotes; }
    public void setRecipeNotes(String recipeNotes) { this.recipeNotes = recipeNotes; }

    public List<String> getSuggestedIngredients() { return suggestedIngredients; }
    public void setSuggestedIngredients(List<String> suggestedIngredients) { this.suggestedIngredients = suggestedIngredients; }

    public int getRandIngredientID() { return randIngredientID; }
    public void setRandIngredientID(int randIngredientID) { this.randIngredientID = randIngredientID; }

    public String getUserID() { return userId; }
    public void setUserID(String userId) { this.userId = userId; }
    
    public String getRecipeDBId() { return recipeDBId; }
    public void setRecipeDBId(String recipeDBId) { this.recipeDBId = recipeDBId; }

    @Override
    public String toString() {
        return this.name;
    }
}