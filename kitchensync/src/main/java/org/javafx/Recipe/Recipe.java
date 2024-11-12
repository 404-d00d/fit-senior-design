package org.javafx.Recipe;

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
    private String[] tags;
    private String[] specialEquipment;
    private String[] ingredients;
    private String[] steps;

    public Recipe(int id, String name, String category, String collection, String description, int prepTime, int passiveTime, int cookTime, int complexity, int servings, 
                  String[] tags, String[] ingredients, String[] specialEquipment, String[] steps) {
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

    public String[] getTags() { return tags; }
    public void setTags(String[] tags) { this.tags = tags; }

    public String[] getIngredients() { return ingredients; }
    public void setIngredients(String[] ingredients) { this.ingredients = ingredients; }

    public String[] getEquipment() { return specialEquipment; }
    public void setEquipment(String[] equipment) { this.specialEquipment = equipment; }

    public String[] getSteps() { return steps; }
    public void setSteps(String[] steps) { this.steps = steps; }
}

// Custom classes to manage ingredients
class Ingredient {
    private String name;
    private String amount;
    private String unit;
 
    public Ingredient(String name, String amount, String unit) {
       this.name = name;
       this.amount = amount;
       this.unit = unit;
    }
 
    public String getName() { return name; }
    public String getAmount() { return amount; }
    public String getUnit() { return unit; }
 }
