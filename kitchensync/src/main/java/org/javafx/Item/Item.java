package org.javafx.Item;

import java.time.LocalDate;

import javafx.scene.image.Image;

public class Item {
    private String name;
    private Integer ID;
    private Integer quantity;
    private String location;
    private String expirDate;
    private String unit;
    private String imagePath;

    public Item(String name, Integer ID, Integer quantity, String unit, String location, String expirDate, String imagePath){
        this.name = name;
        this.ID = ID;
        this.quantity = quantity;
        this.unit = unit;
        this.location = location;
        this.expirDate = expirDate;
        this.imagePath = imagePath;
    }

    public String getName(){
        return this.name;
    }

    public Integer getID(){
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

    public String  getImagePath(){
        return this.imagePath;
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

    public void setID(Integer value) {
        this.ID = value;
    }

}
