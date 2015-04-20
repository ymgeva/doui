package com.ymgeva.doui.parse.items;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.ymgeva.doui.data.DoUIContract;

import java.util.Date;

/**
 * Created by Yoav on 4/16/15.
 */
@ParseClassName("GeneralItem")
public class GeneralItem extends ParseObject {

    public String getItemText(){return getString(DoUIContract.GeneralItemEntry.COLUMN_TEXT);}
    public void setItemText(String str) {put(DoUIContract.GeneralItemEntry.COLUMN_TEXT,str);}

    public String getAssignedTo(){return getString(DoUIContract.GeneralItemEntry.COLUMN_ASSIGNED_TO);}
    public void setAssignedTo(String user) {put(DoUIContract.GeneralItemEntry.COLUMN_ASSIGNED_TO,user);}

    public String getCreatedBy(){return getString(DoUIContract.GeneralItemEntry.COLUMN_CREATED_BY);}
    public void setCreatedBY(String user) {put(DoUIContract.GeneralItemEntry.COLUMN_CREATED_BY,user);}

    public boolean getNotifyWhenDone(){return getBoolean(DoUIContract.GeneralItemEntry.COLUMN_NOTIFY_WHEN_DONE);}
    public void setNotifyWhenDone(boolean bool){put(DoUIContract.GeneralItemEntry.COLUMN_NOTIFY_WHEN_DONE,bool);}

    public boolean getUrgent(){return getBoolean(DoUIContract.GeneralItemEntry.COLUMN_URGENT);}
    public void setUrgent(boolean bool){put(DoUIContract.GeneralItemEntry.COLUMN_URGENT,bool);}

    public boolean getDone(){return  getBoolean(DoUIContract.TaskItemEntry.COLUMN_DONE);}
    public void setDone(boolean done){put(DoUIContract.TaskItemEntry.COLUMN_DONE,done);}

    public static ParseQuery<GeneralItem> getQuery() {
        return ParseQuery.getQuery(GeneralItem.class);
    }

}
