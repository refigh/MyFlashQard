package com.hamze.myflashqard;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
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
 * Created by hamzeh on 8/14/2017.
 */

public class StudyActivity extends Activity {


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

    private flashcard_collectin my_fc_col;

    //info of last shown card
    private vocabulary_card cur_card;
    private int cur_stage_id;

    //date format
    SimpleDateFormat df = new SimpleDateFormat("d.M.yyyy");

    //Today
    private Calendar cal;
    private Date today;
    private String today_formated;

    //random generator
    Random rand;


    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study);


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

        //Rating bar
        ratingBar_NumCorrect = (RatingBar) findViewById(R.id.ratingBar_NumCorrect);
        ratingBar_NumCorrect.setNumStars(my_fc_col.getMaxStageNum() - 2); //number of times that a card is answered correctly. including the final stage.
        ratingBar_NumCorrect.setRating(0);
        ratingBar_NumCorrect.setStepSize(1);

        //info of last showed card
        cur_card = null;
        cur_stage_id = -1;

        //access to the unique flashcard collection.
        my_fc_col = MainActivity.getFlashcard();

        // frame holders
        tabHost = (TabHost) findViewById(android.R.id.tabhost);
        tabHost.setup();

        TabWidget tab_widgets = tabHost.getTabWidget();
        FrameLayout tab_contents = tabHost.getTabContentView();

        // Get the original tab titles and remove them from the viewgroup.
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
        today_formated = df.format(today);


    }//onClick

    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //On click listener for button_close_study
    final View.OnClickListener button_close_study_OnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            //study session is closed, but the current state of my_fc_col is not touched.

            my_fc_col.update_card_count();

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

            //reset variables
            String string_side1 = "";
            String string_side2 = "";
            String string_examp = "";
            String string_comment = "";
            String string_synonym = "";
            String string_antonym = "";
            int dest_stage_id = -1;
            ratingBar_NumCorrect.setRating(0);


            // Before showing next card, move the currently shown card to proper stage.
            // in 2 situations there is no previous card: before first card, and after the last card 
            // (when there is no card to study)
            if (cur_card != null) {

                //in current algorithm we only care about last answering info.
                cur_card.The_statistics.answer_date.clear();
                cur_card.The_statistics.answer_value.clear();

                // correct card should be moved to next stage.
                // Wrong card should be moved to start stage.
                if (checkBox_correct.isChecked()) {  //correct answer

                    cur_card.The_statistics.answer_value.add("true");
                    if (cur_stage_id < (my_fc_col.getMaxStageNum() - 1))
                        dest_stage_id = cur_stage_id + 1; // go next
                    else
                        dest_stage_id = cur_stage_id; //stay in final stage

                } else { //wrong answer
                    cur_card.The_statistics.answer_value.add("false");
                    dest_stage_id = 1;
                }
                cur_card.The_statistics.answer_date.add(today_formated);


                //move it to dest stage
                my_fc_col.stage_list[cur_stage_id].cards.remove(cur_card); // must return true...
                my_fc_col.stage_list[dest_stage_id].cards.addLast(cur_card);
                my_fc_col.stage_list[dest_stage_id].stage_type = 1; //active stage, if it is not.

            } //manage previously shown card


            //select next card
            // no card will be selected from Final stage. then search is started from stage before to last
            cur_stage_id = my_fc_col.getMaxStageNum() - 2;
            boolean not_found = true;
            while (true) { //not_found) {
                stage cur_stage = my_fc_col.stage_list[cur_stage_id];

                //card not found
                if (cur_stage_id < 1)
                    break;

                //skip inactive or empty stages
                else if ((cur_stage.stage_type == -1) || cur_stage.cards.isEmpty()) {
                    cur_stage_id--;
                    continue;
                }

                // select next card from first stage:
                // when head has no date, it should not happen in any stage other than start stage,
                // then we assume we are in stage 1. From stage 1 (fresh cards) select randomly,
                else if ((cur_stage_id == 1) || (cur_stage.cards.getFirst().The_statistics.answer_date.isEmpty())) {
                    not_found = false;
                    rand = new Random();
                    int index = rand.nextInt(cur_stage.cards.size()); // 0 to card_num -1
                    cur_card = cur_stage.cards.get(index);
                    break;
                }

                // select next card from stage_num > 1
                else {
                    // find time difference with head of current stage
                    // note that, head of state, is oldest card.
                    Date head_date = null;
                    try {
                        head_date = df.parse(cur_stage.cards.getFirst().The_statistics.answer_date.getFirst());
                    } catch (ParseException e) {
                        //TODO: assign new type of error due to time format error
                        e.printStackTrace();
                    }

                    long t1 = today.getTime(); //time in milliseconds
                    long t2 = head_date.getTime();
                    long time_dif_milisec = t1 - t2;

                    // cards can not be reviewed earlier than one day
                    if (time_dif_milisec > my_fc_col.getMIN_REVIEW_TIME(cur_stage_id)) {
                        cur_card = cur_stage.cards.getFirst();
                        not_found = false;
                        break;
                    } else {
                        //skip this stage, since it's head (and then rest ofs cards) is not old enough
                        cur_stage_id--;
                    }
                }
            }//while - search for next card


            // give value to strings and convert them to HTML format
            if (not_found) {
                cur_card = null;
                cur_stage_id = -1;
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
                // set how many times the card is answered correctly
                ratingBar_NumCorrect.setRating(cur_stage_id - 1);
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

            checkBox_correct.setChecked(false);

            //by default, 2nd side of card should be invisible
            textView_side2.setVisibility(TextView.INVISIBLE);
            tabHost.getTabContentView().setVisibility(FrameLayout.INVISIBLE);



            //make the title of non-empty texts, as underlined.
            TabWidget tab_widgets = tabHost.getTabWidget();
            for (int index = 0; index < tab_widgets.getChildCount(); index++) {
                TextView tv =  (TextView)tab_widgets.getChildAt(index).findViewById(android.R.id.title);
                tv.setPaintFlags(tv.getPaintFlags() & ~Paint.UNDERLINE_TEXT_FLAG); // remove underline
            }
            if  (!string_examp.isEmpty()) {
                TextView tv =  (TextView)tab_widgets.getChildAt(0).findViewById(android.R.id.title);
                tv.setPaintFlags(tv.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG); // add underline
            }
            if (!string_comment.isEmpty()){
                TextView tv =  (TextView)tab_widgets.getChildAt(1).findViewById(android.R.id.title);
                tv.setPaintFlags(tv.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG); // add underline
            }
            if (!string_synonym.isEmpty()){
                TextView tv =  (TextView)tab_widgets.getChildAt(2).findViewById(android.R.id.title);
                tv.setPaintFlags(tv.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG); // add underline
            }
            if (!string_antonym.isEmpty()){
                TextView tv =  (TextView)tab_widgets.getChildAt(3).findViewById(android.R.id.title);
                tv.setPaintFlags(tv.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG); // add underline
            }



            return;

        } //onClick
    }; //button_next_OnClickListener


    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //On click listener for comment button
    final View.OnClickListener imageButton_comment_OnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {

            // put comment on currently shown card (if exist).
            if (cur_card != null) {

                // Set up text input
                final EditText input = new EditText(StudyActivity.this);
                input.setSingleLine(false);
                input.setLines(4);
                input.setMaxLines(5);
                input.setGravity(Gravity.LEFT | Gravity.TOP);
                input.setHorizontalScrollBarEnabled(false); //this

                String comment = cur_card.Text_of_comments;
                comment += "Comment for revision: ";
                input.setText(comment);

                //show a dialog to get user's comment
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



