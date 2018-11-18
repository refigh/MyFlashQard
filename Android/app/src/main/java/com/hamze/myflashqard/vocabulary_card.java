/**
 * Created by hamze on 6/15/2017.
 */

package com.hamze.myflashqard;

//each individual card is stored is below class.
public class vocabulary_card {


    public int card_group; // -1: invalid, 0: stack  1,2,3...: stages
    public String First_Language;
    public String Text_of_First_Language;  //html content
    public String Second_Language;
    public String Text_of_Second_Language; //html content
    public String Text_of_examples;
    public String Text_of_wordtype;
    public String Text_of_userdefinedwordtype;
    public String Text_of_comments;
    public String Text_of_synonyms;
    public String Text_of_antonyms;
    public statistics The_statistics;

    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    // Constructor
    vocabulary_card(){
        card_group = -1; //set to invalid
        First_Language = "";
        Text_of_First_Language = "";
        Second_Language = "";
        Text_of_Second_Language = "";
        Text_of_examples = "";
        Text_of_wordtype = "";
        Text_of_userdefinedwordtype = "";
        Text_of_comments = "";
        Text_of_synonyms = "";
        Text_of_antonyms = "";
        The_statistics = new statistics();
    }

    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    // Search in a card for a text. return true if it exist  //TODO: write search function
    public boolean search_in_card(){
        return true;
    }//search_in_card


    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    // in which stage this card is
    public int get_stage_id_of_card(flashcard_collection fc){
        int i = 0;

        if (fc == null)
            return -1;

        for (i = 0; i < flashcard_collection.getMaxStageNum(); i++) {
            if (fc.stage_list[i] != null)
                if (fc.stage_list[i].get_cards() != null)
                    if (fc.stage_list[i].get_cards().contains(this))
                        return i;
        }

        return -1; // if card is inside the flashcard_collection, never should reach here.
    }

}


//TODO: change file format with shorter TAGs.