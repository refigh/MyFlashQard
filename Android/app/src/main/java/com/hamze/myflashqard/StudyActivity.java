package com.hamze.myflashqard;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.Spanned;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;


/**
 * Created by Hamzeh on 8/14/2017.
 */

public class StudyActivity extends Activity {

    //----------------------------------------------------------------
    //----------------------------- Data objects
    //----------------------------------------------------------------
    //temp_access to the unique flashcard collection.
    private flashcard_collectin my_fc_col_temp_ptr;

    //info of last shown card
    private vocabulary_card cur_card;
    private int cur_stage_id; // TODO: should be asked from cur_card.


    //----------------------------------------------------------------
    //----------------------------- GUI objects
    //----------------------------------------------------------------
    //buttons
    private Button button_close_study;
    private Button button_show;
    private Button button_next;
    private ImageButton imageButton_comment;

    //text boxes
    private TextView textView_side1;
    private TextView textView_side2;
    private TextView textView_example;
    private TextView textView_comment;
    private TextView textView_synonym;
    private TextView textView_antonym;

    //frame holder
    private TabHost tabHost;

    //check box
    private CheckBox checkBox_correct;

    //rating bar
    private RatingBar ratingBar_NumCorrect;

    //date format
    SimpleDateFormat df = new SimpleDateFormat("d.M.yyyy");

    //Today
    private Calendar cal;
    private Date today;
    private String today_formatted;

    //random generator
    Random rand;


    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study);

        //----------------------------------------------------------------
        //----------------------------- Data objects
        //----------------------------------------------------------------
        //in java non-primitive objects does not clone by assignment, they point to the same object.
        my_fc_col_temp_ptr = MainActivity.getFlashcard();

        cur_card = null;
        cur_stage_id = -1;


        //----------------------------------------------------------------
        //----------------------------- GUI objects
        //----------------------------------------------------------------
        //show button
        button_show = (Button) findViewById(R.id.button_show);
        button_show.setOnClickListener(button_show_OnClickListener);

        //close study button
        button_close_study = (Button) findViewById(R.id.button_close_study);
        button_close_study.setOnClickListener(button_close_study_OnClickListener);

        //next button
        button_next = (Button) findViewById(R.id.button_next);
        button_next.setOnClickListener(button_next_OnClickListener);

        //comment button
        imageButton_comment = (ImageButton) findViewById(R.id.imageButton_comment);
        imageButton_comment.setOnClickListener(imageButton_comment_OnClickListener);


        //text boxes. side 1 and side 2
        textView_side1 = (TextView) findViewById(R.id.textView_side1);
        textView_side1.setMovementMethod(new ScrollingMovementMethod());

        textView_side2 = (TextView) findViewById(R.id.textView_side2);
        textView_side2.setMovementMethod(new ScrollingMovementMethod());

        //text boxes
        textView_example = (TextView) findViewById(R.id.textView_example);
        textView_example.setMovementMethod(new ScrollingMovementMethod());

        textView_comment = (TextView) findViewById(R.id.textView_comment);
        textView_comment.setMovementMethod(new ScrollingMovementMethod());

        textView_synonym = (TextView) findViewById(R.id.textView_synonym);
        textView_synonym.setMovementMethod(new ScrollingMovementMethod());

        textView_antonym = (TextView) findViewById(R.id.textView_antonym);
        textView_antonym.setMovementMethod(new ScrollingMovementMethod());

        //check box
        checkBox_correct = (CheckBox) findViewById(R.id.checkBox_correct);
        checkBox_correct.setChecked(false);

        //Rating bar, number of times that a card is answered correctly.
        ratingBar_NumCorrect = (RatingBar) findViewById(R.id.ratingBar_NumCorrect);
        ratingBar_NumCorrect.setNumStars(my_fc_col_temp_ptr.getMaxStageNum() - 2); // including the final stage.
        ratingBar_NumCorrect.setRating(0);
        ratingBar_NumCorrect.setStepSize(1);

        // frame holders
        tabHost = (TabHost) findViewById(android.R.id.tabhost);
        tabHost.setup();

        TabWidget tab_widgets = tabHost.getTabWidget();
        FrameLayout tab_contents = tabHost.getTabContentView();

        // save the initial tab titles (defined in GUI) and remove them from the view-group, then
        // remove the tabs and then create tabs.
        TextView[] original_titles = new TextView[tab_widgets.getTabCount()];
        for (int index = 0; index < tab_widgets.getTabCount(); index++)
            original_titles[index] = (TextView) tab_widgets.getChildTabViewAt(index);
        tab_widgets.removeAllViews();

        // Ensure that all tab contents are not visible at startup.
        for (int index = 0; index < tab_contents.getChildCount(); index++)
            tab_contents.getChildAt(index).setVisibility(View.GONE);

        // Create the tabspec based on the textview children in the xml file.
        // Or create simple tabspec instances in any other way...
        for (int index = 0; index < original_titles.length; index++) {
            final TextView curtab_title = original_titles[index];
            final View curtab_content = tab_contents.getChildAt(index);

            //set tab tag
            TabHost.TabSpec curtab_Spec = tabHost.newTabSpec((String) curtab_title.getTag());
            //set content
            curtab_Spec.setContent(new TabHost.TabContentFactory() {
                @Override
                public View createTabContent(String tag) {
                    return curtab_content;
                }
            });

            //set tab title
            if (curtab_title.getBackground() == null)
                curtab_Spec.setIndicator(curtab_title.getText());
            else
                curtab_Spec.setIndicator(curtab_title.getText(), curtab_title.getBackground());

            tabHost.addTab(curtab_Spec);
        } // for

        //Today
        cal = Calendar.getInstance();
        cal.clear(cal.MILLISECOND); //our precision is day.
        cal.clear(cal.SECOND);
        cal.clear(cal.MINUTE);
        cal.clear(cal.HOUR);
        today = cal.getTime();
        today_formatted = df.format(today);


    }//onClick

    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //On click listener for button_close_study
    final View.OnClickListener button_close_study_OnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            //study session is closed, but the current state of my_fc_col_temp_ptr is not touched.

            my_fc_col_temp_ptr.update_card_count();

            Intent returnIntent = new Intent();
            setResult(Activity.RESULT_CANCELED, returnIntent);
            finish();   //StudyActivity.this.finish();
            return;

        } //onClick
    }; //button_close_study_OnClickListener


    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //On click listener for button show/hide
    final View.OnClickListener button_show_OnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {

            //toggle the visibility and second side of card, examples, etc.
            if (textView_side2.getVisibility() == TextView.VISIBLE) {
                textView_side2.setVisibility(TextView.INVISIBLE);
                tabHost.getTabContentView().setVisibility(FrameLayout.INVISIBLE);
            } else {
                textView_side2.setVisibility(TextView.VISIBLE);
                tabHost.getTabContentView().setVisibility(FrameLayout.VISIBLE);
                tabHost.setCurrentTab(0);
            }
            return;

        } //onClick
    }; //button_show_OnClickListener


    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //On click listener for button_next
    final View.OnClickListener button_next_OnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {

            //------------------------------------------------------------
            //------------ Manage current(active) and next card
            //------------------------------------------------------------
            // TODO: have this function out of GUI cur_card_move_outof_GUI(cur_card, checkBox_correct.isChecked(), today_formatted );

            // Before showing next card, move the currently shown card (cur_card) to proper stage.
            // in 2 situations there is no current card, before starting the review, and after finishing
            // all cards.
            if (cur_card != null) {

                //update date and answer (for now, only info of last card review is stored)
                cur_card.The_statistics.answer_date.clear();
                cur_card.The_statistics.answer_date.add(today_formatted);
                cur_card.The_statistics.answer_value.clear();
                cur_card.The_statistics.answer_value.add(checkBox_correct.isChecked() ? "true" : "false");

                //TODO: add error codes
                assert( (1 <= cur_stage_id) && (cur_stage_id <= (my_fc_col_temp_ptr.getMaxStageNum() - 1))  );

                //find destination stage (for correct or wrong answer)
                int dest_stage_id = -1;
                if (checkBox_correct.isChecked()) {
                    if (cur_stage_id < (my_fc_col_temp_ptr.getMaxStageNum() - 1))
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

                //move cur_card to dest stage
                //even if it is from/to same stage, it is removed and added to put card to the end
                my_fc_col_temp_ptr.stage_list[cur_stage_id].cards.remove(cur_card); // must return true...
                my_fc_col_temp_ptr.stage_list[dest_stage_id].cards.addLast(cur_card);
                my_fc_col_temp_ptr.stage_list[dest_stage_id].stage_type = 1; //activate the stage, if it is not.

                my_fc_col_temp_ptr.update_card_count(); //TODO: handle this automatically, ... or in a better way

            } //manage previously shown card (cur_card)


            /* Now, find next card:
            After below code cur_card points to next card, or null, cur_stage_id points to it's stage.

            card selection policy: FC-FS, older cards have higher priority. Because number of cards is
            large. Then, for reviewing a limited number of cards per day, it is good to have a small
            moving active set, than a huge slow-moving set.

            Then, we start to search from top stage backward. In each stage, we start from head (older) card.
            No card will be selected from final stage (finish stage). Then search is started from stage
            before to last.
            */

            cur_card = null;
            cur_stage_id = my_fc_col_temp_ptr.getMaxStageNum() - 2;
            boolean next_card_found = false;
            while (true) {

                //finally, card was not found
                if (cur_stage_id < 1)
                    break;

                stage cur_stage = my_fc_col_temp_ptr.stage_list[cur_stage_id];
                //skip inactive or empty stages
                if ((cur_stage.stage_type == -1) || cur_stage.cards.isEmpty()) {
                    cur_stage_id--;
                    continue;
                }

                // head-card is fresh (no date), it is selected.
                // when head has no date, it is expected to not happen other than start stage (a very fresh card)
                // but it may happen in other states due to manual card movement (in PC tool)
                else if (cur_stage.cards.getFirst().The_statistics.answer_date.isEmpty() ) {
                    cur_card = cur_stage.cards.getFirst();
                    next_card_found = true;
                    break;
                }

                // head card has date (already reviewed)
                else {
                    // find time difference between now and head-card
                    // note that, head-card of each stage is the oldest.
                    Date headcard_date = null;
                    try {
                        headcard_date = df.parse(cur_stage.cards.getFirst().The_statistics.answer_date.getFirst());
                    } catch (ParseException e) {
                        //TODO: assign new type of error due to time format error
                        e.printStackTrace();
                    }

                    //times in milliseconds
                    long t1 = today.getTime();
                    long t2 = headcard_date.getTime();
                    long time_dif_milisec = t1 - t2;

                    // cards should stay for a minimum specified time in each stage to be review again.
                    if (time_dif_milisec > my_fc_col_temp_ptr.getMIN_REVIEW_TIME(cur_stage_id)) {

                        // have randomness, if there are more than one card with same time-tag.
                        int count = -1;
                        Date card_date_temp = null;
                        do{
                            count++;
                            if (count == cur_stage.card_count)
                                break;
                            else
                                try {
                                    card_date_temp = df.parse(cur_stage.cards.get(count).The_statistics.answer_date.getFirst());
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                    //TODO: add error code
                                }
                        }while (card_date_temp.compareTo(headcard_date) == 0);

                        rand = new Random();
                        int index = rand.nextInt(count); // 0 to count-1
                        cur_card = cur_stage.cards.get(index);
                        next_card_found = true;
                        break;
                    } else {
                        //skip this stage, since it's head card (oldest, and then rest ofs cards) is not old enough
                        cur_stage_id--;
                    }
                }
            }//while - search for next card

            //------------------------------------------------------------
            //------------ update info of next card to GUI
            //------------------------------------------------------------
            String string_side1;
            String string_side2;
            String string_examp;
            String string_comment;
            String string_synonym;
            String string_antonym;
            ratingBar_NumCorrect.setRating(0);

            // give value to strings and convert them to HTML format
            if (!next_card_found) {
                cur_stage_id = -1; // no needed indeed, because cur_card is null. just for debugging.
                string_side1 = "فعلا کارتی موجود نیست.";
                string_side2 = "";
                string_examp = "";
                string_comment = "";
                string_synonym = "";
                string_antonym = "";
                ratingBar_NumCorrect.setRating(0);
            } else {
                string_side1 = cur_card.Text_of_First_Language;
                string_side2 = cur_card.Text_of_Second_Language;
                string_examp = cur_card.Text_of_examples;
                string_comment = cur_card.Text_of_comments;
                string_synonym = cur_card.Text_of_synonyms;
                string_antonym = cur_card.Text_of_antonyms;
                ratingBar_NumCorrect.setRating(cur_stage_id - 1); // show card stage-number
            }

            //convert above string HTML format
            Spanned html_side1, html_side2;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                html_side1 = Html.fromHtml(string_side1, Html.FROM_HTML_MODE_LEGACY);
                html_side2 = Html.fromHtml(string_side2, Html.FROM_HTML_MODE_LEGACY);
            } else {
                html_side1 = Html.fromHtml(string_side1);
                html_side2 = Html.fromHtml(string_side2);
            }


            //show the info of card
            textView_side1.setText(html_side1);
            textView_side1.scrollTo(0, 0);

            textView_side2.setText(html_side2);
            textView_side2.scrollTo(0, 0);

            textView_example.setText(string_examp);
            textView_example.scrollTo(0, 0);

            textView_comment.setText(string_comment);
            textView_comment.scrollTo(0, 0);

            textView_synonym.setText(string_synonym);
            textView_synonym.scrollTo(0, 0);

            textView_antonym.setText(string_antonym);
            textView_antonym.scrollTo(0, 0);

            // reset the box
            checkBox_correct.setChecked(false);

            //by default, 2nd side of card should be invisible
            textView_side2.setVisibility(TextView.INVISIBLE);
            tabHost.getTabContentView().setVisibility(FrameLayout.INVISIBLE);


            //If a tab contains some text, makes it's title underlined.
            TabWidget tab_widgets = tabHost.getTabWidget();
            FrameLayout tab_contents = tabHost.getTabContentView();

            for (int index = 0; index < tab_widgets.getChildCount(); index++) {
                View v_title = tab_widgets.getChildAt(index).findViewById(android.R.id.title);
                View v_content = tab_contents.getChildAt(index);
                TextView tv_title = (TextView) v_title;
                TextView tv_content = (TextView) v_content;

                if (tv_content.getText().toString().isEmpty())
                    tv_title.setPaintFlags(tv_title.getPaintFlags() & ~Paint.UNDERLINE_TEXT_FLAG); // remove underline
                else
                    tv_title.setPaintFlags(tv_title.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG); // add underline
            }





            return;
        } //onClick
    }; //button_next_OnClickListener


    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //On click listener for comment button
    //TODO: change current_card into a get_current_card (separating GUI from impl.)
    final View.OnClickListener imageButton_comment_OnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {

            // add some user comment for currently shown card (if exist).
            if (cur_card != null) {

                // Set up a text input
                final EditText input = new EditText(StudyActivity.this);
                input.setSingleLine(false);
                input.setLines(4);
                input.setMaxLines(5);
                input.setGravity(Gravity.LEFT | Gravity.TOP);
                input.setHorizontalScrollBarEnabled(false); //this

                //load the existing comment's text + pre-text for new comment into text box
                String comment = cur_card.Text_of_comments;
                comment += ("\n\r" + "Comment for revision: ");
                input.setText(comment);

                //show a dialog to get user's comment (will be added to already loaded text)
                new AlertDialog.Builder(StudyActivity.this)
                        .setView(input)
                        .setTitle("Enter your comment")
                        //.setMessage("Enter your comment")
                        //.setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                cur_card.Text_of_comments = input.getText().toString() + "\n\r";
                            }//onClick
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .show();

            } //manage previously shown card

            return;

        } //onClick
    }; //imageButton_comment_OnClickListener


}//class StudyActivity



