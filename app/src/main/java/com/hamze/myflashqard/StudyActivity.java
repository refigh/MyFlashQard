package com.hamze.myflashqard;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

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

        //text boxes
        textView_side2 = (TextView) findViewById(R.id.textView_side2);

        //text boxes
        textView_example = (TextView) findViewById(R.id.textView_example);

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

            //below is a temporary card review algorithm. only study stage[1] and
            // remove the correct cards.

            //remove the correct card
            if (ind != -1) { //not first time
                if (checkBox_correct.isChecked())
                    my_fc_col.stage_list[1].cards.remove(ind);
            }

            //show a random card from stage 1, and remove it if it is correct!
            int size = my_fc_col.stage_list[1].cards.size();
            ind = ThreadLocalRandom.current().nextInt(0, size);
            vocabulary_card temp = my_fc_col.stage_list[1].cards.get(ind);

            textView_side1.setText(android.text.Html.fromHtml( "<div>Card Side 1:</div>" + temp.Text_of_First_Language , Html.FROM_HTML_MODE_LEGACY));
            textView_side2.setText(android.text.Html.fromHtml( "<div>Card Side 2:</div>" + temp.Text_of_Second_Language , Html.FROM_HTML_MODE_LEGACY));
            textView_example.setText("Examples:\n" + temp.Text_of_examples);

            //by default, 2nd side of card should be invisible
            textView_side2.setVisibility(TextView.INVISIBLE);
            textView_example.setVisibility(TextView.INVISIBLE);

            return;

        } //onClick
    }; //button_next_OnClickListener


}//class StudyActivity



