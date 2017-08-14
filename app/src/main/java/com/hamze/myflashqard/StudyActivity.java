package com.hamze.myflashqard;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


/**
 * Created by hamzeh on 8/14/2017.
 */

public class StudyActivity extends Activity {


    //buttons
    private Button button_close_study;

    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study);

        //Save flashcard button
        button_close_study = (Button) findViewById(R.id.button_close_study);
        button_close_study.setOnClickListener(button_close_study_OnClickListener);

    }

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

}
