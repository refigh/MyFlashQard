package com.hamze.myflashqard;


import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
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
    private Button button_save;
    private Button button_open;


    // Error code holder
    private error error_obj = new error();

    //define an empty flashcard collection. this is the main data structure which the file will be read in to it.
    private flashcard_collectin my_fc_col = new flashcard_collectin(this);


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

        // request for application permission at run time.
        // Note that permissions should be set inside AndroidManifest.xml file before hand.
        permission_request();


    }


    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //On click listener for button_open
    final View.OnClickListener button_open_OnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {

            String file_path = "";

            //file_path = "test_set.fq";
            //file_path = "test_set2.fq";
            file_path = "english_vocab.fq";
            //file_path = "turkish.fq";

            if (my_fc_col.Read_file_to_array(file_path, error_obj)) {
                my_fc_col.Check_integrity();
                //Print File Info on TextBoxes
                editText_wordcnt.setText(String.valueOf(my_fc_col.total_card_num));
                editText_flashcard_name.setText(my_fc_col.box_name);
            } else {
                error_dialog(error_obj);
            }


        } //onClick
    }; //button_open_OnClickListener


    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //On click listener for button_save
    final View.OnClickListener button_save_OnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {

            if (my_fc_col.Write_array_2_file(error_obj)) {

                //Dialog for "Saved Successfully".
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
                mBuilder.setTitle("!");
                mBuilder.setMessage("Saved Successfully");
                //mBuilder.setView(); //good tutorial : https://www.youtube.com/watch?v=plnLs6aST1M
                AlertDialog dialog = mBuilder.create();
                dialog.show();

            } else {
                error_dialog(error_obj);
            }

        } //onClick
    }; //button_save_OnClickListener


    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    private void error_dialog(error error_obj) {

        //Dialog for error information
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
        mBuilder.setTitle("Error!");
        mBuilder.setMessage("Error code " + String.valueOf(error_obj.get_error_code()) + ": " + error_obj.get_error_description());
        //mBuilder.setView(); //good tutorial : https://www.youtube.com/watch?v=plnLs6aST1M
        AlertDialog dialog = mBuilder.create();
        dialog.show();

    }


    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    private void permission_request() {

        int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 0;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //hamze: Program comes here, when user has denied the request for first time...
                //       user can be informed to manually set the permission by a dialog!?
                //       Later read more about this here:
                //       https://stackoverflow.com/questions/32347532/android-m-permissions-confused-on-the-usage-of-shouldshowrequestpermissionrati

            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }

            // hamze: I did not do anything if the permission is not granted. do it later
            //        according to ref: https://developer.android.com/training/permissions/requesting.html#handle-response

            //TODO: read as general information: what are these function? getExternalFilesDir(String), getFilesDir();
        }

    }//permission_request


}//public class MainActivity

//TODO: later change XML parser to resolve the problem with modifying special characters.
//TODO: ask user for disk access.
//TODO: show loading, saving.. dialog box for big files.
//TODO: function for compare or sort
//TODO: change file format: each card should has an ID, attribs: difficulty, terminology, book, ..
//TODO: compress/encrypt the file
//TODO: File open and saving issue
//TODO: Automatice UML extract (schematic about how classes access each other
//TODO: add document folder incluidng readme.txt and schematic visio files to project.