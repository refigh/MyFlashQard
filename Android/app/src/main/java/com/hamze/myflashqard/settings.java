/**
 * Created by hamze on 7/21/2017.
 */

//TODO: save this file and load it at startup

package com.hamze.myflashqard;

import java.util.LinkedList;

public class settings {

    public String app_sys_folder;
    public String app_GoogleDrive_folder;
    public LinkedList<users> user_list;

    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    // Constructor
    settings() {
        app_sys_folder = "";
        app_GoogleDrive_folder = "";
        user_list = new LinkedList();
    }

}
