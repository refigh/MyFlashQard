package com.hamze.myflashqard;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

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

    //chexk box
    private CheckBox checkBox_correct;

    private flashcard_collectin my_fc_col;
    private int ind;

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


        my_fc_col = MainActivity.getFlashcard();
        ind = -1;

    }//onClick

    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //On click listener for button_close_study
    final View.OnClickListener button_close_study_OnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {

            StudyActivity.this.finish();
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
        //@RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onClick(final View v) {

            String string_side1 = "";
            String string_side2 = "";
            String string_examp = "";
            vocabulary_card card_temp = null;

            //below is a temporary card review algorithm. only study stage[1] and
            // remove the correct cards.

            //before going to next card, move the correct currently shown card to another stage
            if (ind != -1) { //not first time of pressing "Next"

                //the correct card should be moved to another stage.
                if (checkBox_correct.isChecked())
                    if (!my_fc_col.stage_list[1].cards.isEmpty()) {

                        //for now, we jut move it to stage 2, later we correct this
                        card_temp = my_fc_col.stage_list[1].cards.remove(ind);

                        //below two lines are not correct: we never inactivate an empty stage
                        //active stages are existing stages, may containing 0 cards
                        //inactive stages are always at end, not in the middle.
                        //if ( my_fc_col.stage_list[1].cards.isEmpty())
                        //    my_fc_col.stage_list[1].stage_type = -1; //inactive

                        my_fc_col.stage_list[2].cards.addFirst(card_temp);
                        my_fc_col.stage_list[2].stage_type = 1; //active stage
                    }
            }


            //show a random card from stage 1
            int card_num = my_fc_col.stage_list[1].cards.size();

            if (card_num == 0) {
                string_side1 = "There is no card left in stage";
                string_side2 = "There is no card left in stage";
                string_examp = "There is no card left in stage";
            } else {
                //select a random card.
                Random rand = new Random();
                ind = rand.nextInt(card_num); // 0 to card_num -1
                card_temp = my_fc_col.stage_list[1].cards.get(ind);
                string_side1 = card_temp.Text_of_First_Language;
                string_side2 = card_temp.Text_of_Second_Language;
                string_examp = card_temp.Text_of_examples;
            }


            //converting to HTML format
            Spanned html_side1, html_side2;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                html_side1 = Html.fromHtml(string_side1, Html.FROM_HTML_MODE_LEGACY);
                html_side2 = Html.fromHtml(string_side2, Html.FROM_HTML_MODE_LEGACY);
            } else {
                html_side1 = Html.fromHtml(string_side1);
                html_side2 = Html.fromHtml(string_side2);
            }

            textView_side1.setText(html_side1);
            textView_side1.scrollTo(0,0);

            textView_side2.setText(html_side2);
            textView_side2.scrollTo(0,0);

            textView_example.setText(string_examp);
            textView_example.scrollTo(0,0);

            checkBox_correct.setChecked(false);

            //by default, 2nd side of card should be invisible
            textView_side2.setVisibility(TextView.INVISIBLE);
            textView_example.setVisibility(TextView.INVISIBLE);

            return;

        } //onClick
    }; //button_next_OnClickListener


}//class StudyActivity



