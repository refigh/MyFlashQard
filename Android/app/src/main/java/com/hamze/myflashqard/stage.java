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

    //A very old dummy constant time (represents time=-inf)
    Calendar OLD_TIME;

    public enum STAGE_TYPES {
        STACK_STAGE,
        INACTIVE_STAGE, //all inactive stages should be at the end of list.
        ACTIVE_STAGE
    }
    private STAGE_TYPES stage_type;


    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    // Constructor
    stage(){
        set_Stage_type(STAGE_TYPES.INACTIVE_STAGE);

        cards = new LinkedList<vocabulary_card>();
        cards.clear();

        date_format = new SimpleDateFormat("d.M.yyyy");

        //just a very old dummy time (meaning time=-inf)
        OLD_TIME = Calendar.getInstance();
        OLD_TIME.set(Calendar.YEAR, 1000); //year=1000(very old)
    }


    //Sort cards of this stage by 'answer date'.
    //Sort algorithm type: insertion sort.
    //comparision is done from right, not left (for quick skipping already sorted list)
    public boolean sort_cards() {

        int card_num = get_card_count();

        if (card_num <= 1 )
            return true;

        //perform sorting if not sorted
        if (!is_sorted()) {
            vocabulary_card temp_card = null;
            for (int i = 1; i < card_num; i++) { //0: not needed
                temp_card = get_cards().get(i);
                get_cards().remove(temp_card);
                insert_card_into_sorted_list(temp_card, i);
            }
        }
        // it expected to always be true, but it is only for diagnostics of sorting function.
        return is_sorted();
    }


    //check if stage is sorted or not
    private boolean is_sorted() {

        boolean sorted = true;
        Date temp_card1_date = null;
        Date temp_card2_date = null;

        for (int i = 0; i < get_card_count()-1; i++) {

            temp_card1_date = get_card_answer_date(get_cards().get(i));
            temp_card2_date = get_card_answer_date(get_cards().get(i+1));

            if (temp_card1_date.compareTo(temp_card2_date) > 0){
                sorted = false;
                break;
            }
        }
        return sorted;
    }


    //insert a new card into a (presumably sorted) stage (like insertion sort, from index 0<= to <sub_list_size )
    public void insert_card_into_sorted_list(vocabulary_card new_card, int sub_list_size) {

        //read new card's date
        Date new_card_date = get_card_answer_date(new_card);

        // find right place for new card
        int indx = sub_list_size;
        Date card_date_temp = null;

        do {
            indx--;
            if (indx == -1)
                break;
            else {
                card_date_temp = get_card_answer_date(get_cards().get(indx));
            }

        } while (new_card_date.compareTo(card_date_temp) < 0); // same as insertion sort, we stop by an index

        get_cards().add(indx+1, new_card);

    }


    private Date get_card_answer_date(vocabulary_card card) {
        Date card_date_temp = null;

        //read card time
        try {
            if (card.The_statistics.answer_date.isEmpty())
                card_date_temp = OLD_TIME.getTime();
            else
                card_date_temp = date_format.parse(card.The_statistics.answer_date.getFirst());
        } catch (ParseException e) {
            e.printStackTrace();
            //TODO: add error code
        }
        return card_date_temp;
    }


    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    // Get/Set functions

    public int get_card_count() { return cards.size(); }

    //in java non-primitive objects does not clone by assignment, they point to the same object.
    public LinkedList<vocabulary_card> get_cards() { return cards; }

    public STAGE_TYPES get_Stage_type(){ return stage_type; }

    public void set_Stage_type(STAGE_TYPES val){ stage_type = val; }

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
