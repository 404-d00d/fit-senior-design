package org.javafx.Recipe;

public class Recipe {
    private String prepTime;
    private String cookTime;
    private int complexity;
    private String[] equipment;
    private String[] ingredients;
    private String[] steps;

    public Recipe(String prepTime, String cookTime, int complexity, String[] equipment, String[] ingredients, String[] steps) {
        this.prepTime = prepTime;
        this.cookTime = cookTime;
        this.complexity = complexity;
        this.equipment = equipment;
        this.ingredients = ingredients;
        this.steps = steps;
    }

    public void setPrepTime(String prepTime) {
        this.prepTime = prepTime;
    }

    public String getPrepTime() {
        return this.prepTime;
    }

    public void setCookTime(String cookTime) {
        this.cookTime = cookTime;
    }

    public String getCookTime() {
        return this.cookTime;
    }

    public void setComplexity(int complexity) {
        this.complexity = complexity;
    }

    public int getComplexity() {
        return this.complexity;
    }

    public void setEquipment(String oldEquipment, String newEquipment) {
        for(int i = 0; i < this.equipment.length; i++) {
            if(this.equipment[i].equals(oldEquipment)) {
                this.equipment[i] = newEquipment;
            }
        }
    }

    public String[] getEquipment() {
        return this.equipment;
    }

    public void setIngredients(String oldIngredients, String newIngredients) {
        for(int i = 0; i < this.ingredients.length; i++) {
            if(this.ingredients[i].equals(oldIngredients)) {
                this.ingredients[i] = newIngredients;
            }
        }
    }

    public String[] getIngredients() {
        return this.ingredients;
    }

    public void setSteps(int step, String instruction) {
        this.steps[step] = instruction;
    }

    public String[] getSteps() {
        return this.steps;
    }
}
