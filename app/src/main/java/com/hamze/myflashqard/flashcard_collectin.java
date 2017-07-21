/**
 * Created by hamze on 6/16/2017.
 */

package com.hamze.myflashqard;

import android.content.res.AssetManager;
import android.os.Environment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Stack;
import java.util.concurrent.ThreadLocalRandom;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;


//A flash card collection is kept in an object of this class.
public class flashcard_collectin {

    //constants
    private static final int MAX_STAGE_NUM = 50;

    //XML tags and attributes (shorter tags can be replaced for more file compression.
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
    public int total_card_num;

    private stage[] stage_list; // each stage includes a list of cards

    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    // Constructor
    flashcard_collectin() {
        authoremail = "";
        license = "";
        author = "";
        comment = "";
        writerversion = "";
        flashqardversion = "";
        box_name = "";
        total_card_num = 0;
        IsOpen = false;
        file_path = "";

        stage_list = new stage[MAX_STAGE_NUM];
        for (int i = 0; i < MAX_STAGE_NUM; i++) {
            stage_list[i] = new stage();
        }

    }// constructor

    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    // read XML file into flash card array
    public boolean Read_file_to_array(String fpath, AssetManager as_mng, String error_msg) {

        file_path = fpath;

        //XML file parser
        XmlPullParserFactory factory;
        XmlPullParser xpp = null;
        try {
            factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            xpp = factory.newPullParser();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
            return false;
        }


        InputStream in_st = null;
        String indent = "";
        int card_cnt = 0;
        String last_att_val = "";
        int stage_counter = -1;
        Stack<String> tag_stack = new Stack<String>();//stack for keeping last open tag during parsing
        boolean first_lang = true; //auxiliary variable to differentiate first and second TG_TRD tage

        try {

            in_st = as_mng.open(file_path);
            xpp.setInput(in_st, null);
            int eventType = xpp.getEventType();

            //main parsing loop
            while (eventType != XmlPullParser.END_DOCUMENT) {

                if (eventType == XmlPullParser.START_DOCUMENT) {
                    //Log.d("hamze_tag", "Start document"); //System.out.println("Start document");

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
                                stage_list[stage_counter].cards.peekLast().First_Language = xpp.getAttributeValue(0);
                                first_lang = false;
                            } else {
                                stage_list[stage_counter].cards.peekLast().Second_Language = xpp.getAttributeValue(0);
                                first_lang = true;
                            }

                            break;

                        case TG_STACK: //stack
                            if (stage_counter == -1) {
                                stage_counter = 0;
                                stage_list[0].stage_type = 0; //0: stack type
                            } else {
                                //error: just one stack should exist
                                error_msg = "more than 1 stack";
                                return false;
                            }

                            break;

                        case TG_STAGE: //stage
                            stage_counter++;
                            stage_list[stage_counter].stage_type = 1; //1: active stage type
                            break;

                        case TG_CARD: //card
                            first_lang = true;
                            if (!xpp.getAttributeValue(0).equals(ATTR_VOC)) {
                                error_msg = "unknown " + TG_CARD + " type" + String.valueOf(card_cnt);
                                return false;
                            }
                            stage_list[stage_counter].cards.add(new vocabulary_card());
                            stage_list[stage_counter].cards.peekLast().card_group = stage_counter;
                            break;

                        default:

                    }//switch


                    if (xpp.getAttributeCount() > 0) {
                        last_att_val = xpp.getAttributeValue(0);
                    }

                    //Log.d("hamze_tag", indent+"Start tag "+xpp.getName()+ " " + String.valueOf(xpp.getAttributeCount()) );
                    //indent = indent + "   "; // add one space


                } else if (eventType == XmlPullParser.END_TAG) {
                    //indent = indent.substring(0, indent.length()-3); //remove on space
                    //Log.d("hamze_tag", indent+"End tag "+xpp.getName());

                    if (xpp.getName().equals(TG_CARD)) {
                        card_cnt++;
                    }

                    tag_stack.pop(); //remove last open tag

                } else if (eventType == XmlPullParser.TEXT) {
                    // if (xpp.getText().trim().length() > 0 ) //text is not white space
                    //     Log.d("hamze_tag", indent+"Text: "+ xpp.getText().trim() );

                    switch (tag_stack.peek()) { //check last open tag to see TEXT belongs to whome
                        case TG_CARD: //card
                            break;

                        case TG_TRD: //translation document
                            break;

                        case TG_HTM: //html
                            //getText replace some special characthers automatically! I revert some back.
                            //TODO: later check whether getText modify any other charackter than belows...

                            if (!first_lang) //!first_lang, because first_lang is already toggled in start tag.
                                stage_list[stage_counter].cards.peekLast().Text_of_First_Language = xpp.getText().replaceAll("<", "&lt;").replaceAll("&gt;", "&amp;gt;").replaceAll("&nbsp;", "&amp;nbsp;"); //.replaceAll("&amp;", "&amp;amp;");
                            else
                                stage_list[stage_counter].cards.peekLast().Text_of_Second_Language = xpp.getText().replaceAll("<", "&lt;").replaceAll("&gt;", "&amp;gt;").replaceAll("&nbsp;", "&amp;nbsp;"); //.replaceAll("&amp;", "&amp;amp;");

                            break;


                        case TG_EXM: //example
                            stage_list[stage_counter].cards.peekLast().Text_of_examples = xpp.getText();
                            break;

                        case TG_WDTP: //word type
                            stage_list[stage_counter].cards.peekLast().Text_of_wordtype = xpp.getText();
                            break;

                        case TG_USR_WT: //user defined word type
                            stage_list[stage_counter].cards.peekLast().Text_of_userdefinedwordtype = xpp.getText();
                            break;

                        case TG_CMNT: //comment
                            stage_list[stage_counter].cards.peekLast().Text_of_comments = xpp.getText();
                            break;

                        case TG_SYN: //synonym
                            stage_list[stage_counter].cards.peekLast().Text_of_synonyms = xpp.getText();
                            break;

                        case TG_ANT: //antonym
                            stage_list[stage_counter].cards.peekLast().Text_of_antonyms = xpp.getText();
                            break;

                        case TG_CR_DATE: //date created
                            stage_list[stage_counter].cards.peekLast().The_statistics.dateCreated = xpp.getText();
                            break;

                        case TG_ANS: //answer
                            stage_list[stage_counter].cards.peekLast().The_statistics.answer_value.add(xpp.getText());
                            stage_list[stage_counter].cards.peekLast().The_statistics.answer_date.add(last_att_val);
                            break;

                        default:
                            //throw new IllegalArgumentException("Invalid day of the week: " + dayOfWeekArg);
                    }//switch

                }//TEXT

                eventType = xpp.next(); //read next XML token from XML file.

            }//while

            //Log.d("hamze_tag", "End document");

            total_card_num = card_cnt;


            //Log.d("hamze_tag", "card count" + String.valueOf(total_card_num) );


            //close the files
            in_st.close();
            as_mng.close();

        } catch (IOException e) {
            e.printStackTrace();
            error_msg = "File Not Found";
            return false;
        } catch (XmlPullParserException e) {
            e.printStackTrace();
            error_msg = "XML parser error";
            return false;
        }

        IsOpen = true;
        return true;
    } //method: Read_file_to_array


    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    // write flashcard array into XML file
    public boolean Write_array_2_file(String error_msg) {

        //flash card should be opened first
        if (!IsOpen) {
            error_msg = "No flashcard open";
            return false;
        }

        if (file_path.equals("")) {
            error_msg = "No file path to save!";
            return false;
        }

        String storage_state = Environment.getExternalStorageState();
        if (!storage_state.equals("mounted")) {
            error_msg = "Ex-Storage not mounted!";
            return false;
        }

        // write permission should be given before in manifest file: read this: https://stackoverflow.com/questions/3853472/creating-a-directory-in-sdcard-fails
        // moreover, maybe external memory access permission should be set manually for the app.
        // if the newly created folder is not visible in PC, don't worry! it is related to a issue called  MTP protocol,....
        //   read more here: https://android.stackexchange.com/questions/53422/folder-on-phone-not-showing-in-windows
        //   but simple solution is to use " AirDroid" tool to access phone from pc.
        String storage_root_path = Environment.getExternalStorageDirectory().getPath();
        File fq_folder = new File(storage_root_path, "Flashqard"); //path of new folder on root directory
        if (!fq_folder.exists()) {
            if (!fq_folder.mkdirs()) {// this will create folder.
                error_msg = "Program folder not created. No write permission";
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
            if (stage_list[0].stage_type == -1) { //even if flashcard collection is empty, write empty stack
                writer.write("  <" + TG_STACK + ">\n");
                writer.write("  </" + TG_STACK + ">\n");
            } else

                while ((stage_list[stg_cnt].stage_type != -1) && (stg_cnt < MAX_STAGE_NUM)) {

                    if (stg_cnt == 0)
                        writer.write("  <" + TG_STACK + ">\n");
                    else
                        writer.write("  <" + TG_STAGE + ">\n");

                    while (stage_list[stg_cnt].cards.size() > 0) {

                        //pop the first card from list and write it to the file
                        vocabulary_card curr_card = stage_list[stg_cnt].cards.pollFirst();

                        writer.write("   <" + TG_CARD + " type=\"" + ATTR_VOC + "\" >\n");

                        writer.write("    <" + TG_TRD + " language=\"" + curr_card.First_Language + "\" >\n");
                        writer.write("     <" + TG_HTM + ">" + curr_card.Text_of_First_Language + "</" + TG_HTM + ">\n");
                        writer.write("    </" + TG_TRD + ">\n");

                        writer.write("    <" + TG_TRD + " language=\"" + curr_card.Second_Language + "\" >\n");
                        writer.write("     <" + TG_HTM + ">" + curr_card.Text_of_Second_Language + "</" + TG_HTM + ">\n");
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
                        if (curr_card.The_statistics.answer_date != null)
                            for (int j = 0; j < curr_card.The_statistics.answer_date.size(); j++)
                                writer.write("     <" + TG_ANS + " date=\"" + curr_card.The_statistics.answer_date.get(j) + "\" >" + curr_card.The_statistics.answer_value.get(j) + "</" + TG_ANS + ">\n");

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
            error_msg = "check write permission!";
            return false;
        }


        return true;
    } // Write_array_2_file


    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    // shuffle all cards in each stage/stack
    public void shuffle(int mode) { //mode=  1:all stack and stages   2:only stages

        int stg_cnt = 0;
        if (mode == 1)
            stg_cnt = 0; //including stack
        else if (mode == 2)
            stg_cnt = 1; //jump over stack
        else
            return; //wrong code, return without suffle.


        while ((stage_list[stg_cnt].stage_type != -1) && (stg_cnt < MAX_STAGE_NUM)) {
            int size = stage_list[stg_cnt].cards.size();
            for (int i = 0; i < size; i++) {
                int ind = ThreadLocalRandom.current().nextInt(0, size);
                vocabulary_card temp = stage_list[stg_cnt].cards.remove(ind);
                stage_list[stg_cnt].cards.addFirst(temp);
            }
            stg_cnt++;
        }

    } //shuffle


    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    public boolean Check_integrity() {


        return true;
    }// Check_integrity


}
