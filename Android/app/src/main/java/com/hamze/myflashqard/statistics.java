/**
 * Created by hamze on 6/15/2017.
 */

package com.hamze.myflashqard;
import java.util.LinkedList;


//Statistics for each single card (last time answering, answer correctness...)
// is saved in this structure.
public class statistics {

    public String dateCreated;
    public LinkedList<String> answer_date;
    public LinkedList<String> answer_value; //correct, wrong,...

    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    // Constructor
    statistics(){
        dateCreated = "";
        answer_date = new LinkedList<String>();
        answer_date.clear();
        answer_value = new LinkedList<String>();
        answer_value.clear();
    }

}

