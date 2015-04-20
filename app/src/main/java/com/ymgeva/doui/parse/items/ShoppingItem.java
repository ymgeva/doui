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
@ParseClassName("ShoppingItem")
public class ShoppingItem extends ParseObject {

    public String getItemText(){return getString(DoUIContract.ShoppingItemEntry.COLUMN_TEXT);}
    public void setItemText(String str) {put(DoUIContract.ShoppingItemEntry.COLUMN_TEXT,str);}

    public String getCreatedBy(){return getString(DoUIContract.ShoppingItemEntry.COLUMN_CREATED_BY);}
    public void setCreatedBY(String user) {put(DoUIContract.ShoppingItemEntry.COLUMN_CREATED_BY,user);}

    public boolean getUrgent(){return getBoolean(DoUIContract.ShoppingItemEntry.COLUMN_URGENT);}
    public void setUrgent(boolean bool){put(DoUIContract.ShoppingItemEntry.COLUMN_URGENT,bool);}

    public int getQuantity(){return getInt(DoUIContract.ShoppingItemEntry.COLUMN_QUANTITY);}
    public void setQuantity(int quantity){put(DoUIContract.ShoppingItemEntry.COLUMN_QUANTITY,quantity);}

    public boolean getDone(){return  getBoolean(DoUIContract.TaskItemEntry.COLUMN_DONE);}
    public void setDone(boolean done){put(DoUIContract.TaskItemEntry.COLUMN_DONE,done);}

    public static ParseQuery<ShoppingItem> getQuery() {
        return ParseQuery.getQuery(ShoppingItem.class);
    }
}
