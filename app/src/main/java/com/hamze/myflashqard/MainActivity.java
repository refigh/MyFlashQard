package com.hamze.myflashqard;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity { //implements View.OnClickListener


    private TextView editText_wordcnt;
    private TextView editText_Error_indicator;
    private TextView editText_flashcard_name;

    //buttons
    private Button button_open;
    private Button button_save;


    //define an empty flashcard collection. this is the main data structure which the file will be read in to it.
    private flashcard_collectin my_fc_col = new flashcard_collectin();


    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    //mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_dashboard:
                    //mTextMessage.setText(R.string.title_dashboard);
                    return true;
                case R.id.navigation_notifications:
                    //mTextMessage.setText(R.string.title_notifications);
                    return true;
            }
            return false;
        }

    };


    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // mTextMessage = (TextView) findViewById(R.id.editText3 );
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        //open flashcard button
        button_open = (Button) findViewById(R.id.button_open);
        button_open.setOnClickListener(button_open_OnClickListener);

        //Save flashcard button
        button_save = (Button) findViewById(R.id.button_save);
        button_save.setOnClickListener(button_save_OnClickListener);


        editText_wordcnt = (TextView) findViewById(R.id.editText_wordcnt);
        editText_Error_indicator = (TextView) findViewById(R.id.editText_Error_indicator);
        editText_flashcard_name = (TextView) findViewById(R.id.editText_flashcard_name);

    }


    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //On click listener for button_open
    final View.OnClickListener button_open_OnClickListener = new View.OnClickListener() {
        public void onClick(final View v) {

            String file_path = "";
            String error_msg = "";

            //file_path = "test_set.fq";
            //file_path = "test_set2.fq";
            file_path = "english_vocab.fq";
            //file_path = "turkish.fq";

            if (my_fc_col.Read_file_to_array(file_path, getAssets(), error_msg)) {
                my_fc_col.Check_integrity();
                //Print File Info on TextBoxes
                editText_wordcnt.setText(String.valueOf(my_fc_col.total_card_num));
                editText_flashcard_name.setText(my_fc_col.box_name);
            } else {
                error_message(error_msg);
            }


        } //onClick
    }; //button_open_OnClickListener


    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //On click listener for button_save
    final View.OnClickListener button_save_OnClickListener = new View.OnClickListener() {
        public void onClick(final View v) {

            String error_msg = "";

            if (my_fc_col.Write_array_2_file(error_msg)) {
                my_fc_col.Check_integrity();
            } else {
                error_message(error_msg);
            }

        } //onClick
    }; //button_save_OnClickListener


    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    private void error_message(String s) {
        editText_Error_indicator.setText(s);
    }


}//public class MainActivity

//TODO: later change XML parser to resolve the problem with modifying special characters.
//TODO: ask user for disk access
//TODO: function for compare or sort
//TODO: change file format: each card should has an ID, attribs: difficulty, terminology, book, ..
//TODO: convert error message to error code
//TODO: compress/encrypt the file
//TODO: File open and saving issue
//TODO: Automatice UML extract (schematic about how classes access each other
//TODO: add document folder incluidng readme.txt and schematic visio files to project.