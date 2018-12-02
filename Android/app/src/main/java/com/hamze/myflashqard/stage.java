/**
 * Created by hamze on 7/19/2017.
 */


package com.hamze.myflashqard;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;


//each stage keeps collection of flash cards
public class stage {

    private LinkedList<vocabulary_card> cards;

    //date format. used in some date format conversions
    private SimpleDateFormat date_format;

    //just a very old dummy time (meaning time=-inf)
    Calendar OLD_TIME;

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

        date_format = new SimpleDateFormat("d.M.yyyy");

        //just a very old dummy time (meaning time=-inf)
        OLD_TIME = Calendar.getInstance();
        OLD_TIME.set(Calendar.YEAR, 1000); //year=1000(very old)
    }


    //Sort cards by date (sort algorithm type: insertion sort)
    //TODO: comparision should be done from right, not left (for quick skipping already sorted list)
    //      currently sorting is slow at load time. moreover, sorting should not change order
    //      of cards with same time tag
    public boolean sort_cards() {

        if (get_card_count() <= 1 )
            return true;

        // only go for sorting, if it is not already sorted
        if (!is_sorted()){
            vocabulary_card temp_card = null;

            for (int i = 0; i < get_card_count(); i++) {
                temp_card = get_cards().get(i);
                get_cards().remove(temp_card);
                insert_card_into_sorted_stage(temp_card, i);
            }
        }

        // it expected to always be true, that is only for diagnostics of sorting function.
        return is_sorted();
    }


    //check if stage is sorted or not
    private boolean is_sorted() {

        boolean sorted = true;
        vocabulary_card temp_card1 = null;
        vocabulary_card temp_card2 = null;
        Date temp_card1_date = null;
        Date temp_card2_date = null;

        for (int i = 0; i < get_card_count()-1; i++) {
            temp_card1 = get_cards().get(i);
            temp_card2 = get_cards().get(i+1);

            // assign temp very big time
            if (temp_card1.The_statistics.answer_date.isEmpty())
                temp_card1_date = OLD_TIME.getTime();
            else {
                try {
                    temp_card1_date = date_format.parse(temp_card1.The_statistics.answer_date.getFirst());
                } catch (ParseException e) {
                    //TODO: assign new type of error due to time format error
                    e.printStackTrace();
                }
            }

            if (temp_card2.The_statistics.answer_date.isEmpty())
                temp_card2_date = OLD_TIME.getTime();
            else {
                try {
                    temp_card2_date = date_format.parse(temp_card2.The_statistics.answer_date.getFirst());
                } catch (ParseException e) {
                    //TODO: assign new type of error due to time format error
                    e.printStackTrace();
                }
            }

            if (temp_card1_date.compareTo(temp_card2_date) > 0){
                sorted = false;
                break;
            }

        }

        return sorted;
    }



    //insert a new card into a (presumably sorted) stage (like insertion sort)
    public void insert_card_into_sorted_stage(vocabulary_card new_card, int sub_list_size) {

        // no_date is assumed to be "inf", or highest-priority
        if (new_card.The_statistics.answer_date.isEmpty()) {
            get_cards().addFirst(new_card);
            return;
        } else {

            //read new card's date
            Date new_card_date = null;
            try {
                new_card_date = date_format.parse(new_card.The_statistics.answer_date.getFirst());
            } catch (ParseException e) {
                //TODO: assign new type of error due to time format error
                e.printStackTrace();
            }

            // find right place for new card
            int indx = -1;
            Date card_date_temp = null;
            do {
                indx++;
                if (indx == sub_list_size) // same as insertion sort, we stop by an index
                    break;
                else
                    try {
                        if (get_cards().get(indx).The_statistics.answer_date.isEmpty())
                            card_date_temp = OLD_TIME.getTime();
                        else
                            card_date_temp = date_format.parse(get_cards().get(indx).The_statistics.answer_date.getFirst());
                    } catch (ParseException e) {
                        e.printStackTrace();
                        //TODO: add error code
                    }
            } while (new_card_date.compareTo(card_date_temp) > 0);

            get_cards().add(indx, new_card);
        }
    }

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
