package com.ymgeva.doui.dto;

/**
 * Created by Yoav on 4/16/15.
 */
public class ShoppingDto {

    public String objectId;
    public String text;
    public Object createdBy;
    public boolean urgent;
    public int quantity;
    public boolean done;

    public ShoppingDto(String objectId,String text, Object createdBy, boolean urgent, int quantity, boolean done) {
        this.objectId = objectId;
        this.text = text;
        this.createdBy = createdBy;
        this.urgent = urgent;
        this.quantity = quantity;
        this.done = done;
    }
}
