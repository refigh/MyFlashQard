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


/**
 * Created by Hamzeh on 8/14/2017.
 */

public class StudyActivity extends Activity {

    //----------------------------------------------------------------
    //----------------------------- Data objects
    //----------------------------------------------------------------
    //temp_access to the flashcard collection. A single object of this class exists in application.
    private flashcard_collection my_fc_col_temp_ptr;


    //----------------------------------------------------------------
    //----------------------------- GUI objects
    //----------------------------------------------------------------
    //buttons
    private Button button_close_study;
    private Button button_show;
    private Button button_next;
    private Button button_skip;
    private ImageButton imageButton_comment;

    //text boxes
    private TextView textView_side1;
    private TextView textView_side2;
    private TextView textView_example;
    private TextView textView_comment;
    private TextView textView_synonym;
    private TextView textView_antonym;
    private TextView textView_labels;

    //frame holder
    private TabHost tabHost;

    //check box
    private CheckBox checkBox_correct;

    //rating bar
    private RatingBar ratingBar_NumCorrect;

    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    // me: always will run when this GUI starts.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study);

        //----------------------------------------------------------------
        //----------------------------- Data objects
        //----------------------------------------------------------------
        //in java non-primitive objects does not clone by assignment, they point to the same object.
        my_fc_col_temp_ptr = MainActivity.getFlashcard();
        my_fc_col_temp_ptr.nullify_active_card();


        //----------------------------------------------------------------
        //----------------------------- GUI objects
        //----------------------------------------------------------------
        //show button
        button_show = (Button) findViewById(R.id.button_show_card);
        button_show.setOnClickListener(button_show_OnClickListener);

        //close study button
        button_close_study = (Button) findViewById(R.id.button_close_study);
        button_close_study.setOnClickListener(button_close_study_OnClickListener);

        //next button
        button_next = (Button) findViewById(R.id.button_next_card);
        button_next.setOnClickListener(button_next_OnClickListener);

        //skip button
        button_skip = (Button) findViewById(R.id.button_skip_card);
        button_skip.setOnClickListener(button_skip_OnClickListener);


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

        textView_labels = (TextView) findViewById(R.id.textView_labels);
        textView_labels.setMovementMethod(new ScrollingMovementMethod());



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


    }//onClick


    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //On click listener for button_close_study
    // leave this open GUI
    final View.OnClickListener button_close_study_OnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            //study session is closed, but the current state of my_fc_col_temp_ptr is not touched, until
            // manually saved, ... or to be continued.
            // TODO: show an start to notify "unsaved session"

            // deactivate the current active card
            my_fc_col_temp_ptr.nullify_active_card();

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
    //On click listener for skip_card
    // postpone reviewing the active card to tomorrow.
    final View.OnClickListener button_skip_OnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {

            // Before go to next card, move active card to proper stage.
            my_fc_col_temp_ptr.skip_active_card_without_review();

            // find next card
            vocabulary_card next_card = my_fc_col_temp_ptr.find_next_card_for_review();

            // update info of next card to GUI
            display_card_info(next_card);

            return;
        } //onClick
    }; //button_skip_OnClickListener



    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //On click listener for button_next
    // close current active card and go to next card
    final View.OnClickListener button_next_OnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {

            // Before go to next card, move current active card to new stage
            my_fc_col_temp_ptr.move_active_card_after_review(checkBox_correct.isChecked());

            // Find next card
            vocabulary_card next_card = my_fc_col_temp_ptr.find_next_card_for_review();

            // update info of next card to GUI
            display_card_info(next_card);

            return;
        } //onClick
    }; //button_next_OnClickListener


    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //On click listener for comment button
    //// add some user comment for active card.
    final View.OnClickListener imageButton_comment_OnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {

            vocabulary_card active_card = my_fc_col_temp_ptr.get_active_card();
            if (active_card != null) {

                // Set up a text input
                final EditText input = new EditText(StudyActivity.this);
                input.setSingleLine(false);
                input.setLines(4);
                input.setMaxLines(5);
                input.setGravity(Gravity.LEFT | Gravity.TOP);
                input.setHorizontalScrollBarEnabled(false); //this

                //load the existing comment's text + pre-text for new comment into text box
                String comment = active_card.Text_of_comments;
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
                                active_card.Text_of_comments = input.getText().toString();
                            }//onClick
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .show();
            }

            return;

        } //onClick
    }; //imageButton_comment_OnClickListener


    //------------------------------------------------------------
    //------------ update info of next card to GUI
    //------------------------------------------------------------
    private void display_card_info(vocabulary_card next_card){

        String string_side1;
        String string_side2;
        String string_examp;
        String string_comment;
        String string_synonym;
        String string_antonym;
        String string_labels;
        ratingBar_NumCorrect.setRating(0);

        // give value to strings and convert them to HTML format
        if (next_card == null) {
            string_side1 = "فعلا کارتی موجود نیست.";
            string_side2 = "";
            string_examp = "";
            string_comment = "";
            string_synonym = "";
            string_antonym = "";
            string_labels = "";
            ratingBar_NumCorrect.setRating(0);
        } else {
            string_side1 = next_card.Text_of_First_Language;
            string_side2 = next_card.Text_of_Second_Language;
            string_examp = next_card.Text_of_examples;
            string_comment = next_card.Text_of_comments;
            string_synonym = next_card.Text_of_synonyms;
            string_antonym = next_card.Text_of_antonyms;
            string_labels = next_card.Text_of_labels;
            ratingBar_NumCorrect.setRating(next_card.get_stage_id_of_card(my_fc_col_temp_ptr) - 1);
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

        textView_labels.setText(string_labels);
        textView_labels.scrollTo(0, 0);

        // reset the check-box
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
    };


}//class StudyActivity



