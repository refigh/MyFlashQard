/**
 * Created by hamze on 6/16/2017.
 */

package com.hamze.myflashqard;


import android.content.res.AssetManager; // TODO: remove GUI-related, non-standard java libs
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.Stack;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;


//A flash card collection is kept in an object of this class.
public class flashcard_collection {

    //Constants

    //TODO: what if the file has more stage than MAX_STAGE_NUM number? program craches!
    //0 : inactive cards(stack)
    //1 : new stage cards (mostly un-reviewed cards)
    //2...MAX_STAGE_NUM-2 (MAX_STAGE_NUM-3 number): cards under review
    //MAX_STAGE_NUM-1 : final successful stage
    //min = 4, Max = 10 (due to Ratingbar presentation issue)
    private static final int MAX_STAGE_NUM = 8;

    //defined for each stage. Card stays in each stage at least this time before next review.
    // unit: millisecond
    private long[] MIN_REVIEW_TIME;

    //XML tags and attributes
    //TODO: simplify XML tag names to reduce file size.
    private static final String TG_TRD = "translationdocument";
    private static final String TG_USR_WT = "userdefinedwordtype";
    private static final String TG_EXM = "examples";
    private static final String TG_WDTP = "wordtype";
    private static final String TG_CMNT = "comments";
    private static final String TG_SYN = "synonyms";
    private static final String TG_ANT = "antonyms";
    private static final String TG_STAT = "statistics";
    private static final String TG_CR_DATE = "dateCreated";
    private static final String TG_CARD = "card";
    private static final String TG_HTM = "html";
    private static final String TG_ANS = "answer";
    private static final String TG_STACK = "stack";
    private static final String TG_STAGE = "stage";
    private static final String ATTR_VOC = "vocabulary"; //card type
    //private static final String ATTR_REG = "regular";    //card type  //TODO: add regular card.

    private static final String FOLDER_NAME_ON_STORAGE = "Flashqard";

    //date format. used in some date format conversions
    private SimpleDateFormat date_format;

    //active card (is expected to be shown on the GUI)
    private vocabulary_card active_card;

    //variables
    private boolean IsOpen;
    private String file_path;

    private String authoremail;
    private String license;
    private String author;
    private String comment;
    private String writerversion;
    private String flashqardversion;

    public String box_name;

    //list of stages. Each stage includes a list of cards
    public stage[] stage_list;



    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    // Constructor
    flashcard_collection() {
        authoremail = "";
        license = "";
        author = "";
        comment = "";
        writerversion = "";
        flashqardversion = "";
        box_name = "";
        IsOpen = false;
        file_path = "";

        date_format = new SimpleDateFormat("d.M.yyyy");

        active_card = null;

        //Creating empty stage list and cards
        stage_list = new stage[MAX_STAGE_NUM];
        for (int i = 0; i < MAX_STAGE_NUM; i++) {
            stage_list[i] = new stage();
        }

        //Generate the MIN_REVIEW_TIME array
        MIN_REVIEW_TIME = new long[MAX_STAGE_NUM];
        for (int i = 0; i < MAX_STAGE_NUM; i++) {
            //values for state 0 and Final are not important (X:don't care)
            // X, 1, 2, 4, 8, 16, 32, ...
            long number_of_days = (long) Math.pow(2, (i - 1)); //TODO: make formulation optional, linear
            MIN_REVIEW_TIME[i] = number_of_days * 24 * 60 * 60 * 1000; //convert from day to millisecond;

            //reduce a small number (about 3 hours) for: avoid marginal calculation problem,
            //   or +/- GMT time change during day-light-saving...
            MIN_REVIEW_TIME[i] -= (3 * 60 * 60 * 1000);
        }

    }// constructor


    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    // read XML file into flash card object
    public boolean Read_fq_from_file(String path, error error_obj) {

        file_path = path;
        int card_cnt = 0;
        String last_att_val = "";
        int stage_counter = -1;
        Stack<String> tag_stack = new Stack<String>();//stack for keeping last open tag during parsing
        boolean first_lang = true; //auxiliary variable to differentiate first and second TG_TRD tage

        if (IsOpen) {
            error_obj.set_error_code(11); //"already open. close it first!"
            return false;
        }

        // XML file parser
        XmlPullParserFactory factory;
        XmlPullParser xpp = null;
        try {
            factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            xpp = factory.newPullParser();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
            error_obj.set_error_code(10); // "XML parser can not initialize"
            return false;
        }

        // first check that the file exist on External storage or not.
        // If it exists, it means user resume his/her previous session,
        // otherwise, user start a flashcard a new.
        String storage_state = Environment.getExternalStorageState();
        if (!storage_state.equals("mounted")) {
            error_obj.set_error_code(3); //"Ex-Storage not mounted!"
            return false;
        }

        File fq_folder = null;
        File fq_file = null;
        AssetManager asset_mng = MainActivity.getContext().getAssets();
        InputStream in_strm = null;

        String storage_root_path = Environment.getExternalStorageDirectory().getPath();
        fq_folder = new File(storage_root_path, FOLDER_NAME_ON_STORAGE); //This path is created in write_fq_to_file method.
        fq_file = new File(fq_folder, file_path);  // file path on external storage  for opening

        // select source of file
        try {
            if (fq_file.exists())
                in_strm = new FileInputStream(fq_file); //from external storage
            else
                in_strm = asset_mng.open(file_path); //from Asset
        } catch (IOException e) {
            e.printStackTrace();
            error_obj.set_error_code(8); //"File Not Found"
            return false;
        }

        //Reading and Parsing the XML file
        try {
            xpp.setInput(in_strm, null);
            int eventType = 0;
            eventType = xpp.getEventType();

            //main parsing loop
            while (eventType != XmlPullParser.END_DOCUMENT) {

                if (eventType == XmlPullParser.START_DOCUMENT) {
                } else if (eventType == XmlPullParser.START_TAG) {
                    tag_stack.push(xpp.getName()); //push latest open tag into stack

                    switch (xpp.getName()) { //check tag name
                        case "flashcards":
                            authoremail = xpp.getAttributeValue(0);
                            license = xpp.getAttributeValue(1);
                            author = xpp.getAttributeValue(2);
                            comment = xpp.getAttributeValue(3);
                            writerversion = xpp.getAttributeValue(4);
                            flashqardversion = xpp.getAttributeValue(5);
                            break;

                        case "box":
                            box_name = xpp.getAttributeValue(0);
                            break;

                        case TG_TRD: // translation document
                            if (first_lang) {
                                stage_list[stage_counter].get_cards().peekLast().First_Language = xpp.getAttributeValue(0);
                                first_lang = false;
                            } else {
                                stage_list[stage_counter].get_cards().peekLast().Second_Language = xpp.getAttributeValue(0);
                                first_lang = true;
                            }

                            break;

                        case TG_STACK: //stack
                            if (stage_counter == -1) {
                                stage_counter = 0;
                                stage_list[0].set_Stage_type(stage.STAGE_TYPES.STACK_STAGE);
                            } else {
                                //error: just one stack should exist
                                error_obj.set_error_code(6);
                                return false;
                            }

                            break;

                        case TG_STAGE: //stage
                            stage_counter++;
                            stage_list[stage_counter].set_Stage_type(stage.STAGE_TYPES.ACTIVE_STAGE);
                            break;

                        case TG_CARD: //card
                            first_lang = true;
                            if (!xpp.getAttributeValue(0).equals(ATTR_VOC)) {
                                error_obj.set_error_code(7, card_cnt);//invalid card is card number card_cnt
                                return false;
                            }
                            stage_list[stage_counter].get_cards().add(new vocabulary_card());
                            stage_list[stage_counter].get_cards().peekLast().card_group = stage_counter;
                            break;

                        default:

                    }//switch


                    if (xpp.getAttributeCount() > 0) {
                        last_att_val = xpp.getAttributeValue(0);
                    }


                } else if (eventType == XmlPullParser.END_TAG) {

                    if (xpp.getName().equals(TG_CARD)) {
                        card_cnt++;
                    }

                    tag_stack.pop(); //remove last open tag

                } else if (eventType == XmlPullParser.TEXT) {

                    switch (tag_stack.peek()) { //check last open tag to see TEXT belongs to whome
                        case TG_CARD: //card
                            break;

                        case TG_TRD: //translation document
                            break;

                        case TG_HTM: //html
                            // Origianlly we replace "<" with "&lt;" in text file to prevent parssing problem.
                            // here getText function replace back some special characthers like "&lt;" automatically!
                            // This is good for display them on application, but later later when we want to
                            // save the file again, we again use such method, meaning "&lt;" instead of "<".
                            // refer to Write_fq_to_file(), HTML part, to see those replacement.
                            //TODO: later check this more if necessary...
                            if (!first_lang) //!first_lang, because first_lang is already toggled in start tag.
                                stage_list[stage_counter].get_cards().peekLast().Text_of_First_Language = xpp.getText();//.replaceAll("<", "&lt;").replaceAll("&gt;", "&amp;gt;").replaceAll("&nbsp;", "&amp;nbsp;"); //.replaceAll("&amp;", "&amp;amp;");
                            else
                                stage_list[stage_counter].get_cards().peekLast().Text_of_Second_Language = xpp.getText();//.replaceAll("<", "&lt;").replaceAll("&gt;", "&amp;gt;").replaceAll("&nbsp;", "&amp;nbsp;"); //.replaceAll("&amp;", "&amp;amp;");

                            break;


                        case TG_EXM: //example
                            stage_list[stage_counter].get_cards().peekLast().Text_of_examples = xpp.getText();
                            break;

                        case TG_WDTP: //word type
                            stage_list[stage_counter].get_cards().peekLast().Text_of_wordtype = xpp.getText();
                            break;

                        case TG_USR_WT: //user defined word type
                            stage_list[stage_counter].get_cards().peekLast().Text_of_userdefinedwordtype = xpp.getText();
                            break;

                        case TG_CMNT: //comment
                            stage_list[stage_counter].get_cards().peekLast().Text_of_comments = xpp.getText();
                            break;

                        case TG_SYN: //synonym
                            stage_list[stage_counter].get_cards().peekLast().Text_of_synonyms = xpp.getText();
                            break;

                        case TG_ANT: //antonym
                            stage_list[stage_counter].get_cards().peekLast().Text_of_antonyms = xpp.getText();
                            break;

                        case TG_CR_DATE: //date created
                            stage_list[stage_counter].get_cards().peekLast().The_statistics.dateCreated = xpp.getText();
                            break;

                        case TG_ANS: //answer
                            stage_list[stage_counter].get_cards().peekLast().The_statistics.answer_value.add(xpp.getText());
                            stage_list[stage_counter].get_cards().peekLast().The_statistics.answer_date.add(last_att_val);
                            break;

                        default:
                            //throw new IllegalArgumentException("Invalid day of the week: " + dayOfWeekArg);
                    }//switch

                }//TEXT

                eventType = xpp.next(); //read next XML token from XML file.

            }//while

            //close the files
            in_strm.close();

        } catch (XmlPullParserException e) {
            e.printStackTrace();
            error_obj.set_error_code(9); //"XML parser error"
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            error_obj.set_error_code(12); //"Error in accessing the file"
            return false;
        }

        //Sorting cards in each stage, by 'answering date'
        boolean sorting_ok = true;
        for (int i = 1; i < MAX_STAGE_NUM; i++)  // skip 0: stack stage
            sorting_ok &= stage_list[i].sort_cards();
        if (!sorting_ok)
            error_obj.set_error_code(20); //"Error in Sorting, after opening the file"


        IsOpen = true;
        return true;
    } //method: Read_file_to_array


    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    // write flashcard object into XML file
    public boolean Write_fq_to_file(error error_obj) {

        //flash card should be opened first
        if (!IsOpen) {
            error_obj.set_error_code(1); //"No flashcard open"
            return false;
        }

        if (file_path.equals("")) {
            error_obj.set_error_code(2); //"No file path to save!"
            return false;
        }

        String storage_state = Environment.getExternalStorageState();
        if (!storage_state.equals("mounted")) {
            error_obj.set_error_code(3); //"Ex-Storage not mounted!"
            return false;
        }

        // write permission should be given before in manifest file: read this: https://stackoverflow.com/questions/3853472/creating-a-directory-in-sdcard-fails
        // moreover, maybe external memory access permission should be set manually for the app.
        // if the newly created folder is not visible in PC, don't worry! it is related to a issue called  MTP protocol,....
        //   read more here: https://android.stackexchange.com/questions/53422/folder-on-phone-not-showing-in-windows
        //   but simple solution is to use " AirDroid" tool to access phone from pc.
        String storage_root_path = Environment.getExternalStorageDirectory().getPath();
        File fq_folder = new File(storage_root_path, FOLDER_NAME_ON_STORAGE); //path of new folder on root directory
        if (!fq_folder.exists()) {
            if (!fq_folder.mkdirs()) {// this will create folder.
                error_obj.set_error_code(4); // "Program folder not created. No storage permission"
                return false;
            }
        }

        File fq_file = new File(fq_folder, file_path);  // file path for saving
        FileWriter writer = null;
        try {
            writer = new FileWriter(fq_file, false); // true/false: append or not

            //write header
            writer.write("<flashcards ");
            writer.write("authoremail=\"" + authoremail + "\" ");
            writer.write("license=\"" + license + "\" ");
            writer.write("author=\"" + author + "\" ");
            writer.write("comment=\"" + comment + "\" ");
            writer.write("writerversion=\"" + writerversion + "\" ");
            writer.write("flashqardversion=\"" + flashqardversion + "\" >\n");
            writer.write(" <box name=\"" + box_name + "\" >\n");


            int stg_cnt = 0;
            if (stage_list[0].get_Stage_type() == stage.STAGE_TYPES.INACTIVE_STAGE) { //even if flashcard collection is empty, write empty stack
                writer.write("  <" + TG_STACK + ">\n");
                writer.write("  </" + TG_STACK + ">\n");
            } else

                while (stg_cnt < MAX_STAGE_NUM) {

                    if (stage_list[stg_cnt].get_Stage_type() == stage.STAGE_TYPES.INACTIVE_STAGE) //from now on, all are inactive.
                        break;

                    if (stg_cnt == 0)
                        writer.write("  <" + TG_STACK + ">\n");
                    else
                        writer.write("  <" + TG_STAGE + ">\n");

                    while (stage_list[stg_cnt].get_card_count() > 0) {

                        //pop the first card from list and write it to the file
                        vocabulary_card curr_card = stage_list[stg_cnt].get_cards().pollFirst();

                        writer.write("   <" + TG_CARD + " type=\"" + ATTR_VOC + "\" >\n");

                        writer.write("    <" + TG_TRD + " language=\"" + curr_card.First_Language + "\" >\n");
                        writer.write("     <" + TG_HTM + ">" + curr_card.Text_of_First_Language.replaceAll("<", "&lt;").replaceAll("&gt;", "&amp;gt;").replaceAll("&nbsp;", "&amp;nbsp;") + "</" + TG_HTM + ">\n");
                        writer.write("    </" + TG_TRD + ">\n");

                        writer.write("    <" + TG_TRD + " language=\"" + curr_card.Second_Language + "\" >\n");
                        writer.write("     <" + TG_HTM + ">" + curr_card.Text_of_Second_Language.replaceAll("<", "&lt;").replaceAll("&gt;", "&amp;gt;").replaceAll("&nbsp;", "&amp;nbsp;") + "</" + TG_HTM + ">\n");
                        writer.write("    </" + TG_TRD + ">\n");

                        writer.write("    <" + TG_EXM + ">" + curr_card.Text_of_examples + "</" + TG_EXM + ">\n");

                        writer.write("    <" + TG_WDTP + ">" + curr_card.Text_of_wordtype + "</" + TG_WDTP + ">\n");


                        if (!curr_card.Text_of_userdefinedwordtype.equals(""))
                            writer.write("    <" + TG_USR_WT + ">" + curr_card.Text_of_userdefinedwordtype + "</" + TG_USR_WT + ">\n");

                        writer.write("    <" + TG_CMNT + ">" + curr_card.Text_of_comments + "</" + TG_CMNT + ">\n");

                        writer.write("    <" + TG_SYN + ">" + curr_card.Text_of_synonyms + "</" + TG_SYN + ">\n");

                        writer.write("    <" + TG_ANT + ">" + curr_card.Text_of_antonyms + "</" + TG_ANT + ">\n");

                        writer.write("    <" + TG_STAT + ">\n");
                        writer.write("     <" + TG_CR_DATE + ">" + curr_card.The_statistics.dateCreated + "</" + TG_CR_DATE + ">\n");
                        if (curr_card.The_statistics.answer_date != null){
                            for (int j = 0; j < curr_card.The_statistics.answer_date.size(); j++) {

                                //TODO: how to use assert?
                                //assert(curr_card.The_statistics.answer_date.size() == curr_card.The_statistics.answer_value.size());
                                if (curr_card.The_statistics.answer_date.size() != curr_card.The_statistics.answer_value.size()) {
                                    error_obj.set_error_code(21); //"Error in card statistics, answer_date.size != answer_value.size";
                                    return false;
                                }


                                String str1 = curr_card.The_statistics.answer_date.get(j);
                                String str2 = curr_card.The_statistics.answer_value.get(j);
                                writer.write("     <" + TG_ANS + " date=\"" + str1 + "\" >" + str2 + "</" + TG_ANS + ">\n");
                            }
                        }

                        writer.write("    </" + TG_STAT + ">\n");

                        writer.write("   </" + TG_CARD + ">\n");
                    }//while card

                    if (stg_cnt == 0)
                        writer.write("  </" + TG_STACK + ">\n");
                    else
                        writer.write("  </" + TG_STAGE + ">\n");

                    stg_cnt++;
                }//while stage

            writer.write(" </box>" + "\n");
            writer.write("</flashcards>" + "\n");

            writer.flush();
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
            error_obj.set_error_code(5); // "Check storage permission to External memory!";
            return false;
        }


        return true;
    } // Write_array_2_file


    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    // Shuffle all cards in each stage/stack //TODO: take care of time order in stage >= 2
    public void shuffle(int mode) { //mode=  1:all stack and stages   2:only stages
        int stg_cnt = 0;
        if (mode == 1)
            stg_cnt = 0; //including stack
        else if (mode == 2)
            stg_cnt = 1; //jump over stack
        else
            return; //wrong code, return without suffle.


        while ((stage_list[stg_cnt].get_Stage_type() != stage.STAGE_TYPES.INACTIVE_STAGE) && (stg_cnt < MAX_STAGE_NUM)) {
            int size = stage_list[stg_cnt].get_card_count();
            for (int i = 0; i < size; i++) {

                Random rand = new Random();
                int ind = rand.nextInt(size); // 0 to size -1

                vocabulary_card temp = stage_list[stg_cnt].get_cards().remove(ind);
                stage_list[stg_cnt].get_cards().addFirst(temp);
            }
            stg_cnt++;
        }

    } //shuffle


    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    public boolean Check_integrity() {

        //TODO: check all cards inside any stage, except stage 0 & 1, are sorted by last correct answer time.

        //TODO: no card should be in inactive stages.
        return true;
    }// Check_integrity


    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //Close and clear the open flashcard collection
    public void close() {
        authoremail = "";
        license = "";
        author = "";
        comment = "";
        writerversion = "";
        flashqardversion = "";
        box_name = "";
        IsOpen = false;
        file_path = "";

        for (int i = 0; i < MAX_STAGE_NUM; i++) {
            stage_list[i].set_Stage_type(stage.STAGE_TYPES.INACTIVE_STAGE);
            stage_list[i].get_cards().clear(); // clear the link list in each stage
        }

        return;
    }// close


    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    // Reset user's statistics for a flash card:
    // 1-delete user file (on external memory, if exists)
    // 2-reload file from asset to external memory.
    public Boolean reset(error error_obj) {

        //flash card should be opened first
        if (!IsOpen) {
            error_obj.set_error_code(1); //"No flashcard open"
            return false;
        }


        //clear current open data
        for (int i = 0; i < MAX_STAGE_NUM; i++) {
            stage_list[i].set_Stage_type(stage.STAGE_TYPES.INACTIVE_STAGE);
            stage_list[i].get_cards().clear(); // clear the link list in each stage
        }


        //delete file on external memory if exist

        //first check that the file exist on External storage or not.
        String storage_state = Environment.getExternalStorageState();
        if (!storage_state.equals("mounted")) {
            error_obj.set_error_code(3); //"Ex-Storage not mounted!"
            return false;
        }

        File fq_folder = null;
        File fq_file = null;

        String storage_root_path = Environment.getExternalStorageDirectory().getPath();
        fq_folder = new File(storage_root_path, FOLDER_NAME_ON_STORAGE); //This path is created in write_fq_to_file method.
        fq_file = new File(fq_folder, file_path);  // file path on external storage  for opening

        //now delete it, if exists
        if (fq_file.exists())
            if (!fq_file.delete()) {
                error_obj.set_error_code(13); //"File can not be deleted"
                return false;
            }


        //reload file from asset
        IsOpen = false;
        if (!Read_fq_from_file(file_path, error_obj))
            return false;

        return true;
    }//reset



    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    // total card num (stack + active stages). we do not expect any card to be in inactive stages.
    public int get_total_card_num(){
        int temp_count = 0;
        //count cards (stack+active stage.). we do not expect any card to be in inactive stages.
        for (int i = 0; i < MAX_STAGE_NUM; i++) {
            if (stage_list[i].get_Stage_type() == stage.STAGE_TYPES.INACTIVE_STAGE)
                break;
            temp_count += stage_list[i].get_card_count();
        }
        return temp_count;
    }


    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    // after answering, move the active card (shown on GUI) to proper place.
    public void move_active_card_after_review(boolean is_card_passed, boolean is_card_easy){


        // in 2 situations there is no active card, before starting the review, and after finishing
        // all cards.
        if (active_card != null) {

            String today_formatted = getToday_formatted(0);

            //update date and answer (for now, only info of last card review is stored)
            active_card.The_statistics.answer_date.clear();
            active_card.The_statistics.answer_date.add(today_formatted);
            active_card.The_statistics.answer_value.clear();
            active_card.The_statistics.answer_value.add(is_card_easy ? "true" : (is_card_passed ? "true" : "false"));

            int cur_stage_id = active_card.get_stage_id_of_card(this);

            //TODO: add error codes
            assert( (1 <= cur_stage_id) && (cur_stage_id <= (getMaxStageNum() - 1))  );

            //find destination stage (for correct or wrong answer)
            int dest_stage_id = -1;

            // move easy cards directly to last stage
            if (is_card_easy) {
                dest_stage_id = (getMaxStageNum() - 1);

            } else if (is_card_passed) {
                if (cur_stage_id < (getMaxStageNum() - 1))
                    dest_stage_id = cur_stage_id + 1; // go next
                else
                    dest_stage_id = cur_stage_id; //stay in final stage (? just in case, we should never review last stage)

            } else {
                //TODO: moving the wrong card into start-state or prev. state should be an option.
                //dest_stage_id = 1;
                if (cur_stage_id > 1) //TODO: starting state, should be a constant, not 1
                    dest_stage_id = cur_stage_id - 1;
                else
                    dest_stage_id = 1;
            }

            //move active_card to END of dest stage (no need to sort)
            //even if it is from/to same stage, it is removed and added to put card to the end
            stage_list[cur_stage_id].get_cards().remove(active_card); // TODO:must return true...
            stage_list[dest_stage_id].get_cards().addLast(active_card);

            //activate the stages, if they are not.
            for (int i = 1; i <= dest_stage_id; i++) {
                stage_list[i].set_Stage_type(stage.STAGE_TYPES.ACTIVE_STAGE);
            }

            nullify_active_card();
        } // if (active_card != null)

    }// void move_active_card_after_review(boolean is_card_passed)


    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    // without answering, keep the active card (shown on GUI) in the same stage, but change
    // the reviewing time-tag to be reviewed on tomorrow.
    public void skip_active_card_without_review(){


        if (active_card != null) {

            int cur_stage_id = active_card.get_stage_id_of_card(this);

            //below formulation set the card's date exactly to be reviewed on tomorrow (+1 is for tomorrow).
            int day_offset = -(int) Math.ceil(getMIN_REVIEW_TIME(cur_stage_id)/(24.0 * 60 * 60 * 1000));
            day_offset += 1; // tomorrow

            String date_formatted =  getToday_formatted(day_offset); //"11.11.2017";

            active_card.The_statistics.answer_date.clear();
            active_card.The_statistics.answer_date.add(date_formatted);
            active_card.The_statistics.answer_value.clear();
            active_card.The_statistics.answer_value.add("false"); //"true"


            // after changing the card's date, remove and re-insert it into same stage.
            stage_list[cur_stage_id].get_cards().remove(active_card);
            stage_list[cur_stage_id].insert_card_into_sorted_list(active_card, stage_list[cur_stage_id].get_card_count());


            nullify_active_card();
        } //if (active_card != null)

    }//void skip_active_card_without_review()


    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //Find next card to review, after searching
    //card selection policy: FC-FS, older cards have higher priority. Because number of cards is
    //large. Then, for reviewing a limited number of cards per day, it is good to have a small
    //moving active set, than a huge slow-moving set.
    //
    //Then, we start to search from top stage backward. In each stage, we start from head (older) card.
    //No card will be selected from final stage (finish stage). Then search is started from stage
    //before to last
    //
    public vocabulary_card find_next_card_for_review(){

        vocabulary_card next_card = null;
        int stage_id = getMaxStageNum() - 2; // sweeping all stages from end to start
        while (true) {

            //finally, card was not found
            if (stage_id < 1)
                break;

            stage cur_stage = stage_list[stage_id];
            //skip inactive or empty stages
            if ((cur_stage.get_Stage_type() == stage.STAGE_TYPES.INACTIVE_STAGE) || cur_stage.get_cards().isEmpty()) {
                stage_id--;
                continue;
            }

            // head-card is fresh (no date), it is selected.
            // when head has no date, it is expected to not happen other than start stage (a very fresh card)
            // but it may happen in other states due to manual card movement (in PC tool)
            else if (cur_stage.get_cards().getFirst().The_statistics.answer_date.isEmpty() ) {
                next_card = cur_stage.get_cards().getFirst();
                break;
            }

            // head card has date (already reviewed)
            else {
                // find time difference between now and head-card
                // note that, head-card of each stage is the oldest.
                Date headcard_date = null;
                try {
                    headcard_date = date_format.parse(cur_stage.get_cards().getFirst().The_statistics.answer_date.getLast());
                } catch (ParseException e) {
                    //TODO: assign new type of error due to time format error
                    e.printStackTrace();
                }

                //times in milliseconds
                long t1 = getToday(0).getTime();
                long t2 = headcard_date.getTime();
                long time_dif_milisec = t1 - t2;

                // cards should stay for a minimum specified time in each stage to be review again.
                if (time_dif_milisec > getMIN_REVIEW_TIME(stage_id)) {

                    // have randomness, if there are more than one card with same time-tag.
                    int count = -1;
                    Date card_date_temp = null;
                    do{
                        count++;
                        if (count == cur_stage.get_card_count())
                            break;
                        else
                            try {
                                card_date_temp = date_format.parse(cur_stage.get_cards().get(count).The_statistics.answer_date.getLast());
                            } catch (ParseException e) {
                                e.printStackTrace();
                                //TODO: add error code
                            }
                    }while (card_date_temp.compareTo(headcard_date) == 0);

                    Random rand = new Random();
                    int index = rand.nextInt(count); // 0 to count-1
                    next_card = cur_stage.get_cards().get(index);
                    break;
                } else {
                    //skip this stage, since it's head card (oldest, and then rest ofs cards) is not old enough
                    stage_id--;
                }
            }
        }//while - search for next card

        active_card = next_card;

        return active_card;
    }


    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    // Get today (+ day_offset)
    Date getToday(int day_offset) {
        //Today
        Calendar cal = Calendar.getInstance();

        //Today + day_offset
        cal.add(Calendar.DATE, day_offset);

        //our precision is day.
        cal.clear(cal.MILLISECOND);
        cal.clear(cal.SECOND);
        cal.clear(cal.MINUTE);
        //cal.clear(cal.HOUR_OF_DAY);
        //cal.clear(cal.HOUR);
        cal.set(Calendar.HOUR_OF_DAY, 0); //two above did not work, because they consider GMT time

        Date today = cal.getTime();

        return today;
    }


    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    // Get today (plus day_offset), with my format
    String getToday_formatted(int day_offset){
        Date today = getToday(day_offset);
        String today_formatted = date_format.format(today);
        return today_formatted;
    }


    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    // Get functions
    public static int getMaxStageNum() {
        return MAX_STAGE_NUM;
    }

    public long getMIN_REVIEW_TIME(int i) {
        return MIN_REVIEW_TIME[i];
    }

    public static String getFolderNameOnStorage() {
        return FOLDER_NAME_ON_STORAGE;
    }

    public int getUser_progress_value() {
        //MAX =  number_of_active_cards * (MAX_STAGE_NUM-2)

        int temp = 0;

        for (int i = 2; i < MAX_STAGE_NUM; i++) {
            if (stage_list[i].get_Stage_type() == stage.STAGE_TYPES.INACTIVE_STAGE)
                break;
            temp += (stage_list[i].get_card_count())*(i-1);
        }
        temp = (int) Math.floor((temp * 100.0) / ((get_total_card_num() - stage_list[0].get_card_count() ) * (MAX_STAGE_NUM-2) ));

        return temp;
    }

    //get already active card (without searching)
    public vocabulary_card get_active_card() {
        return active_card;
    }

    //clear active card
    public void nullify_active_card() {
        active_card = null;
    }



}
