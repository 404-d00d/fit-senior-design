package org.javafx.Item;

public class Item {
    private String name;
    private String ID;
    private Integer quantity;

    public Item(String name, String ID, Integer quantity){
        this.name = name;
        this.ID = ID;
        this.quantity = quantity;
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
