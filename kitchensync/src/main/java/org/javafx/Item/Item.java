package org.javafx.Item;

import java.time.LocalDate;

public class Item {
    private String name;
    private String ID;
    private Integer quantity;
    private String location;
    private String expirDate;
    private String unit;

    public Item(String name, String ID, Integer quantity, String unit, String location, String expirDate){
        this.name = name;
        this.ID = ID;
        this.quantity = quantity;
        this.unit = unit;
        this.location = location;
        this.expirDate = expirDate;
    }

    public String getName(){
        return this.name;
    }

    public String getID(){
        return this.ID;
    }

    public Integer getQuantity(){
        return this.quantity;
    }

    public String getUnit(){
        return this.unit;
    }

    public String getLocation(){
        return this.location;
    }

    public String getExpirDate(){
        return this.expirDate;
    }

    public void modifyQuantity(Integer newValue) {
        // if user sets quantity of item in list to 0 or lower, remove this item
        if (this.quantity <= 0) {
            this.name = null;
            this.ID = null;
            this.quantity = null;
        }
        else {
            this.quantity = newValue;
        }
    }

}
