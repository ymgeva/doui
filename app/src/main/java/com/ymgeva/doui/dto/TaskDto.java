package com.ymgeva.doui.dto;

import android.graphics.Bitmap;

import java.util.Date;

/**
 * Created by Yoav on 4/16/15.
 */
public class TaskDto {

    public String objectId;
    public String title;
    public String text;
    public Date date;
    public Object assignedTo;
    public Object createdBy;
    public boolean reminder;
    public boolean notifyWhenDone;
    public Date reminderTime;
    public Bitmap image;
    public boolean done;

    public TaskDto(String objectId,String title, String text, Date date, Object assignedTo, Object createdBy, boolean reminder, boolean notifyWhenDone, Date reminderTime, Bitmap image, boolean done) {
        this.objectId = objectId;
        this.title = title;
        this.text = text;
        this.date = date;
        this.assignedTo = assignedTo;
        this.createdBy = createdBy;
        this.reminder = reminder;
        this.notifyWhenDone = notifyWhenDone;
        this.reminderTime = reminderTime;
        this.image = image;
        this.done = done;

    }
}
