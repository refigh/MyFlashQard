package com.hamze.myflashqard;

import android.util.Log;

/**
 * Created by hamzeh on 7/27/2017.
 */

public class error {

    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    // Constructor
    error() {
        error_description = new String[32];
        error_description[0] = "No Error!";
        error_description[1] = "No flashcard open";
        error_description[2] = "No file path to save!";
        error_description[3] = "Ex-Storage not mounted!";
        error_description[4] = "Program folder not created. No storage permission";
        error_description[5] = "Check storage permission to External memory!";
        error_description[6] = "more than 1 stack";
        error_description[7] = "unknown card type";
        error_description[8] = "File Not Found";
        error_description[9] = "XML parser error";
        error_description[10] = "XML parser can not initialize";
        error_description[11] = "already open. close it first!";
        error_description[12] = "";
        error_description[13] = "";
        error_description[14] = "";
        error_description[15] = "";
        error_description[16] = "";
        error_description[17] = "";
        error_description[18] = "";
        error_description[19] = "";
        error_description[20] = "";
        error_description[21] = "";
        error_description[22] = "";
        error_description[23] = "";
        error_description[24] = "";
        error_description[25] = "";
        error_description[26] = "";
        error_description[27] = "";
        error_description[28] = "";
        error_description[29] = "";
        error_description[30] = "";
        error_description[31] = "";

        latest_error_code = 0;
        latest_error_string = error_description[latest_error_code];
    }

    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    public void set_error_code(int er_code) {
        latest_error_code = er_code;
        latest_error_string = error_description[latest_error_code];

        //write error to log file
        Log.d("Flashqard_error_tag", " Error code: " + String.valueOf(latest_error_code) + " Error description: " + latest_error_string);
    }

    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    public void set_error_code(int er_code, int aux_data) {
        latest_error_code = er_code;

        switch (latest_error_code) {
            case 7:
                latest_error_string = error_description[latest_error_code] + " - card num: " + String.valueOf(aux_data);
                break;

            default:
                latest_error_string = error_description[latest_error_code];
        }//switch

        //write error to log file
        Log.d("Flashqard_error_tag", " Error code: " + String.valueOf(latest_error_code) + " Error description: " + latest_error_string);
    }

    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    public String get_error_description() {
        return latest_error_string;
    }


    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    public int get_error_code() {
        return latest_error_code;
    }


    //constants
    private final String[] error_description;

    //variables
    private int latest_error_code;
    private String latest_error_string;


}
