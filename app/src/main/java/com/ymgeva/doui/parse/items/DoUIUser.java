package com.ymgeva.doui.parse.items;

import com.parse.ParseClassName;
import com.parse.ParseUser;

/**
 * Created by Yoav on 4/16/15.
 */
@ParseClassName("_User")
public class DoUIUser extends ParseUser {

    public String getDisplayName() {return getString("name");}
    public DoUIUser getPartner(){return (DoUIUser)getParseUser("partner");}

}
