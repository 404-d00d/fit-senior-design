package org.javafx.Item;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import javafx.scene.image.Image;

public class Item {
    private String name;
    private Integer ID;
    private Integer quantity;
    private String location;
    private String expirDate;
    private String unit;
    private String imagePath;
    private Set<String> tags;

    public Item(String name, Integer ID, Integer quantity, String unit, String location, String expirDate, String imagePath){
        this.name = name;
        this.ID = ID;
        this.quantity = quantity;
        this.unit = unit;
        this.location = location;
        this.expirDate = expirDate;
        this.imagePath = imagePath;
        this.tags = new HashSet<>();
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getID() { return ID; }
    public void setID(Integer ID) { this.ID = ID; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getExpirDate() { return expirDate; }
    public void setExpirDate(String expirDate) { this.expirDate = expirDate; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public Set<String> getTags() {
        return this.tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
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
