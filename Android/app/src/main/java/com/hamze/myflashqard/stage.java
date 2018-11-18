/**
 * Created by hamze on 7/19/2017.
 */


package com.hamze.myflashqard;
import java.util.LinkedList;


//each stage keeps collection of flash cards
public class stage {

    private LinkedList<vocabulary_card> cards;

    private int stage_type; //all inactive stages should be at the end of list.
    public static final int STACK_STAGE = 0;  // cards which will not be reviewed
    public static final int INACTIVE_STAGE = -1;
    public static final int ACTIVE_STAGE = 1;


    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    // Constructor
    stage(){
        set_Stage_type(INACTIVE_STAGE);
        cards = new LinkedList<vocabulary_card>();
        cards.clear();
    }


    //TODO: implement this method.
    //Sort cards by date
    public void sort_cards() { return ; }

    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    // Get/Set functions

    public int get_card_count() { return cards.size(); }

    //in java non-primitive objects does not clone by assignment, they point to the same object.
    public LinkedList<vocabulary_card> get_cards() { return cards; }

    public int get_Stage_type(){ return stage_type; }

    public void set_Stage_type(int val){ stage_type = val; }

    // not tested yet
    public int get_stage_id(flashcard_collection fc){
        int i = 0;
        for (i = 0; i < flashcard_collection.getMaxStageNum(); i++) {
            if (fc.stage_list[i] == this)
                return i;
        }

        return -1; // never should reach here
    }

}
