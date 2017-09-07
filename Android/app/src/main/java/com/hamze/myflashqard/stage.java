/**
 * Created by hamze on 7/19/2017.
 */


package com.hamze.myflashqard;
import java.util.LinkedList;


//each object keep an stage collection of a flash card collection.
public class stage {

    public int stage_type; // -1: inactive, 0: stack  1: active stage.  all inactive stages should be at the end of list.
    public LinkedList<vocabulary_card> cards;

    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    // Constructor
    stage(){
        stage_type = -1; //set to inactive
        cards = new LinkedList<vocabulary_card>();
        cards.clear();
    }


}
