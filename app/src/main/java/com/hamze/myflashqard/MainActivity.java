package com.hamze.myflashqard;


import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
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
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity { //implements View.OnClickListener


    private TextView editText_wordcnt;
    private TextView editText_flashcard_name;

    //buttons
    private Button button_save;
    private Button button_open;
    private Button button_reset;
    private Button button_start;
    private Button[] all_buttons; //initiate it after initiation of above buttons
    private Boolean[] all_buttons_enable_status;

    // Error code holder
    private error error_obj = new error();

    //define an empty flashcard collection. this is the main data structure which the file will be read in to it.
    private flashcard_collectin my_fc_col = new flashcard_collectin(this);


    private ProgressBar progressBar_open;
    private ProgressBar progressBar_save;
    private ProgressBar progressBar_reset;

    private int count = 1;

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

        //reset flashcard button
        button_reset = (Button) findViewById(R.id.button_reset);
        button_reset.setOnClickListener(button_reset_OnClickListener);


        //start flashcard button
        button_start = (Button) findViewById(R.id.button_start);
        button_start.setOnClickListener(button_start_OnClickListener);


        //all buttons
        all_buttons = new Button[]{button_save, button_open, button_reset, button_start};
        all_buttons_enable_status = new Boolean[all_buttons.length];
        for (Boolean b : all_buttons_enable_status)
            b = false;

        editText_wordcnt = (TextView) findViewById(R.id.editText_wordcnt);
        editText_flashcard_name = (TextView) findViewById(R.id.editText_flashcard_name);


        //Open and Save flashcard progress bar
        progressBar_open = (ProgressBar) findViewById(R.id.progressBar_open);
        progressBar_save = (ProgressBar) findViewById(R.id.progressBar_save);
        progressBar_reset = (ProgressBar) findViewById(R.id.progressBar_reset);


        // request for application permission at run time.
        // Note that permissions should be set inside AndroidManifest.xml file beforehand too.
        permission_request();


    }


    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //On click listener for button_open
    final View.OnClickListener button_open_OnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {

            final CharSequence fq_names[] = new CharSequence[]{
                    "English",
                    "Turkish",
                    "Test1",
                    "Test2",
                    "NoExist"
            };

            final CharSequence fq_files[] = new CharSequence[]{
                    "english_vocab.fq",
                    "turkish.fq",
                    "test_set.fq",
                    "test_set2.fq",
                    "NoExitTest"
            };

            //Dialog for selecting one file
            AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
            mBuilder.setTitle("Select one");
            mBuilder.setIcon(android.R.drawable.arrow_up_float);
            mBuilder.setItems(fq_names, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // the user clicked on fq_files [which]

                    //run a AsyncTask (a kind of thread) for file opening. Because it takes some seconds.
                    OpenerTaskClass my_reader_task = new OpenerTaskClass();
                    my_reader_task.execute((String) fq_files[which]); // execute(Params...). pass parameter to task.

                }
            });
            AlertDialog dialog = mBuilder.create();
            dialog.show();


        } //onClick
    }; //button_open_OnClickListener


    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //On click listener for button_save
    final View.OnClickListener button_save_OnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {

            //run a AsyncTask (a kind of thread) for file saving. Because it takes some seconds.
            SaverTaskClass my_saver_task = new SaverTaskClass();
            my_saver_task.execute(0); // execute(Params...)

        } //onClick
    }; //button_save_OnClickListener


    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //On click listener for reset_save
    final View.OnClickListener button_reset_OnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {

            //show a confirmation dialog before run.
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Reset data")
                    .setMessage("Do you really want to reset this flashcard?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int whichButton) {
                            //run a AsyncTask (a kind of thread) for fq reseting. Because it takes some seconds.
                            // it includes delete of file, and re-open the flash file
                            ReseterTaskClass my_reseter_task = new ReseterTaskClass();
                            my_reseter_task.execute(0); // execute(Params...)
                        }
                    })
                    .setNegativeButton(android.R.string.no, null).show();

        } //onClick
    }; //button_reset_OnClickListener



    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //On click listener for button_start
    final View.OnClickListener button_start_OnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {


        } //onClick
    }; //button_start_OnClickListener


    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    private void error_dialog(error error_obj) {

        //Dialog for error information
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
        mBuilder.setTitle("Error!");
        mBuilder.setIcon(android.R.drawable.ic_dialog_alert);
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


    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    private void disable_interface() {
        for (Button b : all_buttons)
            b.setEnabled(false);
        return;
    } //disable_interface

    private void save_interface_enable_status() {
        for (int i = 0; i < all_buttons.length; i++)
            all_buttons_enable_status[i] = all_buttons[i].isEnabled();
        return;
    } //save interface

    private void load_interface_enable_status() {
        for (int i = 0; i < all_buttons.length; i++)
            all_buttons[i].setEnabled(all_buttons_enable_status[i]);
        return;
    } //load interface


    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    // read below two sources to learn how to use AsyncTask for background jobs with progress report
    //  1-https://developer.android.com/reference/android/os/AsyncTask.html
    //  2-http://www.concretepage.com/android/android-asynctask-example-with-progress-bar
    class OpenerTaskClass extends AsyncTask<String, Integer, Boolean> { //params, progress, result

        @Override
        protected void onPreExecute() {
            //txt.setText("Task Starting...");
            progressBar_open.setVisibility(ProgressBar.VISIBLE);
            save_interface_enable_status();
            disable_interface(); //prevent multiple times opening
        }

        @Override
        protected Boolean doInBackground(String... params) {
            /*
            publishProgress(50);
            */

            return (my_fc_col.Read_fq_from_file(params[0], error_obj));
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            //txt.setText("Running..." + values[0]);
            //progressBar_open.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(Boolean result) {

            progressBar_open.setVisibility(ProgressBar.INVISIBLE);

            if (result) {
                my_fc_col.Check_integrity();
                //Print File Info on TextBoxes
                editText_wordcnt.setText(String.valueOf(my_fc_col.total_card_num));
                editText_flashcard_name.setText(my_fc_col.box_name);
                button_save.setEnabled(true);
                button_reset.setEnabled(true);
                button_start.setEnabled(true);
            } else {
                error_dialog(error_obj);
                load_interface_enable_status(); //allow opening again. because the last opening was not successful.
            }
        }

    }//OpenerTaskClass


    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    class SaverTaskClass extends AsyncTask<Integer, Integer, Boolean> { //params, progress, result

        @Override
        protected void onPreExecute() {
            //txt.setText("Task Starting...");
            progressBar_save.setVisibility(ProgressBar.VISIBLE);
            save_interface_enable_status();
            disable_interface(); // prevent multiple times saving
        }

        @Override
        protected Boolean doInBackground(Integer... params) { //in this code, params not used
            /*
            publishProgress(50);
            */
            return (my_fc_col.Write_fq_to_file(error_obj));
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            //txt.setText("Running..." + values[0]);
            //progressBar_save.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(Boolean result) {

            progressBar_save.setVisibility(ProgressBar.INVISIBLE);

            if (result) {

                my_fc_col.close();
                button_open.setEnabled(true);

                //Clear File Info on TextBoxes
                editText_wordcnt.setText("0");
                editText_flashcard_name.setText("-");

                //Dialog for "Saved Successfully".
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
                mBuilder.setTitle(" ");
                mBuilder.setIcon(android.R.drawable.checkbox_on_background);
                mBuilder.setMessage("Saved Successfully");
                //mBuilder.setView(); //good tutorial : https://www.youtube.com/watch?v=plnLs6aST1M
                AlertDialog dialog = mBuilder.create();
                dialog.show();
            } else {
                error_dialog(error_obj);
                load_interface_enable_status(); //allow saving again. because the last time it was not successful.
            }
        }

    }//SaverTaskClass


    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    class ReseterTaskClass extends AsyncTask<Integer, Integer, Boolean> { //params, progress, result

        @Override
        protected void onPreExecute() {
            //txt.setText("Task Starting...");
            progressBar_reset.setVisibility(ProgressBar.VISIBLE);
            save_interface_enable_status();
            disable_interface(); // prevent multiple times saving
        }

        @Override
        protected Boolean doInBackground(Integer... params) { //in this code, params not used
            /*
            publishProgress(50);
            */
            return (my_fc_col.reset(error_obj));
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            //txt.setText("Running..." + values[0]);
            //progressBar_reset.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(Boolean result) {

            progressBar_reset.setVisibility(ProgressBar.INVISIBLE);
            load_interface_enable_status();

            if (result) {
                //Dialog for "Reset Successfully".
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
                mBuilder.setIcon(android.R.drawable.checkbox_on_background);
                mBuilder.setTitle(" ");
                mBuilder.setMessage("Reseted Successfully");
                AlertDialog dialog = mBuilder.create();
                dialog.show();

            } else {
                error_dialog(error_obj);
            }
        }

    }//SaverTaskClass


}//public class MainActivity

//TODO: later change XML parser to resolve the problem with modifying special characters.
//TODO: function for compare or sort
//TODO: change file format: each card should has an ID, attribs: difficulty, terminology, book, ..
//TODO: compress/encrypt the file
//TODO: File open from/ Save to Dropbox or google drive.
//TODO: Automatice UML extract (schematic about how classes access each other
//TODO: add document folder incluidng readme.txt and schematic visio files to project.