package org.javafx.Controllers;

// Custom classes to manage ingredients
public class Ingredient {
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

