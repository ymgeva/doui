package com.ymgeva.doui.dto;

/**
 * Created by Yoav on 4/16/15.
 */
public class GeneralDto {

    public String objectId;
    public String text;
    public Object createdBy;
    public Object assignedTo;
    public boolean urgent;
    public boolean notifyWhenDone;
    public boolean done;


    public GeneralDto(String objectId,String text, Object createdBy, Object assignedTo, boolean urgent, boolean notifyWhenDone, boolean done) {
        this.objectId = objectId;
        this.text = text;
        this.createdBy = createdBy;
        this.assignedTo = assignedTo;
        this.urgent = urgent;
        this.notifyWhenDone = notifyWhenDone;
        this.done = done;
    }
}
