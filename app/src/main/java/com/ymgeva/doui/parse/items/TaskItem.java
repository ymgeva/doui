package com.ymgeva.doui.parse.items;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.ymgeva.doui.data.DoUIContract;

import java.util.Date;

/**
 * Created by Yoav on 4/16/15.
 */
@ParseClassName("TaskItem")
public class TaskItem extends ParseObject {

    public String getTitle(){return getString(DoUIContract.TaskItemEntry.COLUMN_TITLE);}
    public void setTitle(String str) {put(DoUIContract.TaskItemEntry.COLUMN_TITLE,str);}

    public String getItemDescription(){return getString(DoUIContract.TaskItemEntry.COLUMN_DESCRIPTION);}
    public void setItemDescription(String str) {put(DoUIContract.TaskItemEntry.COLUMN_DESCRIPTION,str);}

    public String getAssignedTo(){return getString(DoUIContract.TaskItemEntry.COLUMN_ASSIGNED_TO);}
    public void setAssignedTo(String  user) {put(DoUIContract.TaskItemEntry.COLUMN_ASSIGNED_TO,user);}

    public String getCreatedBy(){return getString(DoUIContract.TaskItemEntry.COLUMN_CREATED_BY);}
    public void setCreatedBY(String user) {put(DoUIContract.TaskItemEntry.COLUMN_CREATED_BY,user);}

    public boolean getReminder(){return getBoolean(DoUIContract.TaskItemEntry.COLUMN_REMINDER);}
    public void setReminder(boolean bool){put(DoUIContract.TaskItemEntry.COLUMN_REMINDER,bool);}

    public boolean getNotifyWhenDone(){return getBoolean(DoUIContract.TaskItemEntry.COLUMN_NOTIFY_WHEN_DONE);}
    public void setNotifyWhenDone(boolean bool){put(DoUIContract.TaskItemEntry.COLUMN_NOTIFY_WHEN_DONE,bool);}

    public Date getDate(){return getDate(DoUIContract.TaskItemEntry.COLUMN_DATE);}
    public void setDate(Date date){put(DoUIContract.TaskItemEntry.COLUMN_DATE,date);}

    public Date getReminderTime(){return getDate(DoUIContract.TaskItemEntry.COLUMN_REMINDER_TIME);}
    public void setReminderTime(Date date){put(DoUIContract.TaskItemEntry.COLUMN_REMINDER_TIME,date);}

    public ParseFile getImage(){return getParseFile(DoUIContract.TaskItemEntry.COLUMN_IMAGE);}
    public void serImage(ParseFile image){put(DoUIContract.TaskItemEntry.COLUMN_IMAGE,image);}

    public boolean getDone(){return  getBoolean(DoUIContract.TaskItemEntry.COLUMN_DONE);}
    public void setDone(boolean done){put(DoUIContract.TaskItemEntry.COLUMN_DONE,done);}

    public static ParseQuery<TaskItem> getQuery() {
        return ParseQuery.getQuery(TaskItem.class);
    }
}
