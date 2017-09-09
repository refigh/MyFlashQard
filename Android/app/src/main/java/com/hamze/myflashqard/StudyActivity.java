package com.hamze.myflashqard;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RatingBar;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;


/**
 * Created by hamzeh on 8/14/2017.
 */

public class StudyActivity extends Activity {


    //buttons
    private Button button_close_study;
    private Button button_show;
    private Button button_next;

    //text boxes
    private TextView textView_side1;
    private TextView textView_side2;
    private TextView textView_example;

    //check box
    private CheckBox checkBox_correct;

    //rating bar
    private RatingBar ratingBar_NumCorrect;

    private flashcard_collectin my_fc_col;

    //info of last showed card
    private vocabulary_card Lastcard;
    private int Lastcard_stage_num;

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

        //text boxes
        textView_side1 = (TextView) findViewById(R.id.textView_side1);
        textView_side1.setMovementMethod(new ScrollingMovementMethod());


        //text boxes
        textView_side2 = (TextView) findViewById(R.id.textView_side2);
        textView_side2.setMovementMethod(new ScrollingMovementMethod());

        //text boxes
        textView_example = (TextView) findViewById(R.id.textView_example);
        textView_example.setMovementMethod(new ScrollingMovementMethod());

        //check box
        checkBox_correct = (CheckBox) findViewById(R.id.checkBox_correct);
        checkBox_correct.setChecked(false);

        //Rating bar
        ratingBar_NumCorrect = (RatingBar) findViewById(R.id.ratingBar_NumCorrect);
        ratingBar_NumCorrect.setNumStars(my_fc_col.getMaxStageNum() - 2); //number of times that a card is answerd correctly. including the final stage.
        ratingBar_NumCorrect.setRating(0);
        ratingBar_NumCorrect.setStepSize(1);

        //info of last showed card
        Lastcard = null;
        Lastcard_stage_num = -1;

        //access to the uniqe flashcar collection.
        my_fc_col = MainActivity.getFlashcard();


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
            //study session is clossed, but the current state of my_fc_col is not touched.

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
                textView_example.setVisibility(TextView.INVISIBLE);
            } else {
                textView_side2.setVisibility(TextView.VISIBLE);
                textView_example.setVisibility(TextView.VISIBLE);
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
            int next_stage = -1;
            ratingBar_NumCorrect.setRating(0);


            // Before showing next card, move the currently shown card to proper stage.
            // in 2 situations there is no previous card: before first card, and after the last card (when there is no card to study)
            if (Lastcard != null) {

                //in current algorithm we only care about last answering info.
                Lastcard.The_statistics.answer_date.clear();
                Lastcard.The_statistics.answer_value.clear();

                // correct card should be moved to next stage.
                // Wrong card should be moved to start stage.
                if (checkBox_correct.isChecked()) {  //correct answer

                    Lastcard.The_statistics.answer_value.add("true");
                    if (Lastcard_stage_num < (my_fc_col.getMaxStageNum() - 1))
                        next_stage = Lastcard_stage_num + 1; // go next
                    else
                        next_stage = Lastcard_stage_num; //stay in final stage

                } else { //wrong answer
                    Lastcard.The_statistics.answer_value.add("false");
                    next_stage = 1;
                }
                Lastcard.The_statistics.answer_date.add(today_formated);


                //move it to next stage
                my_fc_col.stage_list[Lastcard_stage_num].cards.remove(Lastcard); // must return true...
                my_fc_col.stage_list[next_stage].cards.addLast(Lastcard);
                my_fc_col.stage_list[next_stage].stage_type = 1; //active stage, if it is not.

            } //manage previously shown card


            //select next card
            Lastcard_stage_num = my_fc_col.getMaxStageNum() - 2; // no card is selected from Final stage. then search is started from stage before to last
            boolean not_found = true;
            while (true) { //not_found) {
                stage Lastcard_stage = my_fc_col.stage_list[Lastcard_stage_num];

                if (Lastcard_stage_num < 1) //card not found
                    break;
                else if ((Lastcard_stage.stage_type == -1) || Lastcard_stage.cards.isEmpty()) { //skip inactive or empty stages
                    Lastcard_stage_num--;
                    continue;
                } else if ((Lastcard_stage_num == 1) || (Lastcard_stage.cards.getFirst().The_statistics.answer_date.isEmpty())) {
                    //when head has no date, it should not happen in any stage other than start stage, then we assume we are in stage 1
                    // from stage 1 (fresh cards) select randomly,
                    not_found = false;
                    rand = new Random();
                    int index = rand.nextInt(Lastcard_stage.cards.size()); // 0 to card_num -1
                    Lastcard = Lastcard_stage.cards.get(index);
                    break;
                } else {
                    // find time difference with head of current stage
                    Date head_date = null;
                    try {
                        head_date = df.parse(Lastcard_stage.cards.getFirst().The_statistics.answer_date.getFirst());
                    } catch (ParseException e) {
                        //TODO: assign new type of error due to time format error
                        e.printStackTrace();
                    }

                    long t1 = today.getTime(); //time in milliseconds
                    long t2 = head_date.getTime();
                    long time_dif_milisec = t1 - t2;

                    if (time_dif_milisec > my_fc_col.getMIN_REVIEW_TIME(Lastcard_stage_num)) { // cards can not be reviewed earlier than one day
                        Lastcard = Lastcard_stage.cards.getFirst();
                        not_found = false;
                        break;
                    } else {
                        //skip this stage, since it's head (and then whole cards) is not old enough
                        Lastcard_stage_num--;
                    }
                }
            }//while - search for next card


            // give value to strings and convert them to HTML format
            if (not_found) {
                Lastcard = null;
                Lastcard_stage_num = -1;
                string_side1 = "فعلا کارتی موجود نیست.";
                string_side2 = "";
                string_examp = "";
                ratingBar_NumCorrect.setRating(0);
            } else {
                string_side1 = Lastcard.Text_of_First_Language;
                string_side2 = Lastcard.Text_of_Second_Language;
                string_examp = Lastcard.Text_of_examples;
                // set how many times the card is answered correctly
                ratingBar_NumCorrect.setRating(Lastcard_stage_num - 1);
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

            checkBox_correct.setChecked(false);

            //by default, 2nd side of card should be invisible
            textView_side2.setVisibility(TextView.INVISIBLE);
            textView_example.setVisibility(TextView.INVISIBLE);


            return;

        } //onClick
    }; //button_next_OnClickListener


}//class StudyActivity



