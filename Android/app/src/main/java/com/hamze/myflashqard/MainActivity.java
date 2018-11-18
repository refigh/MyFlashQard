package com.hamze.myflashqard;


import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cloudrail.si.CloudRail;
import com.cloudrail.si.services.Dropbox;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;


public class MainActivity extends AppCompatActivity {


    //info text box
    private TextView editText_wordcnt;
    private TextView editText_progress;
    private TextView editText_flashcard_name;

    //buttons
    private Button button_save_close;
    private Button button_open;
    private Button button_reset;
    private Button button_start;
    private Button button_Nosave_close;
    private ImageButton button_connect_cloud;
    private ImageButton button_download;
    private ImageButton button_upload;

    //Todo: it does not include cloud storage buttons, later rename it properly
    private Button[] all_buttons; //initiate it after initiation of above buttons
    private Boolean[] all_buttons_enable_status;

    // Error code holder
    private error error_obj = new error();

    //define an empty flashcard collection. this is the main data structure which the file will be read in to it.
    // it is static final, so other activities can access it easily as singleton.
    //  read more: https://stackoverflow.com/questions/4878159/whats-the-best-way-to-share-data-between-activities
    private static final flashcard_collection my_fc_col = new flashcard_collection();

    //progress bar
    private ProgressBar progressBar_open;
    private ProgressBar progressBar_save;
    private ProgressBar progressBar_reset;
    private ProgressBar progressBar_upload;
    private ProgressBar progressBar_download;

    // keep current context in static field, so other statics can access it.
    // read more: https://stackoverflow.com/questions/4391720/how-can-i-get-a-resource-content-from-a-static-context
    private static Context mContext;


    // Add a different request code for every activity you are starting from here
    private static final int SECOND_ACTIVITY_RESULT_CODE = 0;


    //normal variables
    private int count = 1;

    private final CharSequence fq_names[] = new CharSequence[]{
            "English",
            "Turkish",
            "Test1",
            "Test2",
            "NoExist"
    };

    private final CharSequence fq_files[] = new CharSequence[]{
            "english_vocab.fq",
            "turkish.fq",
            "test_set_01.fq",
            "test_set_02.fq",
            "NoExitTest"
    };

    // Cloud storage variables
    //----------------------------------
    private final String CLOUDRAIL_KEY = "59e223d6a5a11670ad75c24c"; //Received from CloudRail site

    //Dropbox, get both App Key (Client ID) and App Secret (Client Secret) from dropbox site
    private final String DROPBOX_REDIRECT_URL = "https://auth.cloudrail.com/com.hamze.myflashqard"; // same as URL set inside dropbox site
    private final String DROPBOX_CLIENT_ID = "wryp2qsk27carsq";
    private final String DROPBOX_CLIENT_SECRET = "o3y34w2uassu4bd";
    private boolean dropbox_is_connected;

    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //open flashcard button
        button_open = (Button) findViewById(R.id.button_open);
        button_open.setOnClickListener(button_open_OnClickListener);

        //save flashcard button
        button_save_close = (Button) findViewById(R.id.button_save_close);
        button_save_close.setOnClickListener(button_save_close_close_OnClickListener);

        //reset flashcard button
        button_reset = (Button) findViewById(R.id.button_reset);
        button_reset.setOnClickListener(button_reset_OnClickListener);


        //start flashcard button
        button_start = (Button) findViewById(R.id.button_start);
        button_start.setOnClickListener(button_start_OnClickListener);

        //Close without saving flashcard button
        button_Nosave_close = (Button) findViewById(R.id.button_Nosave_close);
        button_Nosave_close.setOnClickListener(button_Nosave_close_OnClickListener);

        //connect to internet folder
        button_connect_cloud = (ImageButton) findViewById(R.id.button_connect_cloud);
        button_connect_cloud.setOnClickListener(button_connect_cloud_OnClickListener);

        //download from internet folder
        button_download = (ImageButton) findViewById(R.id.button_download);
        button_download.setOnClickListener(button_download_OnClickListener);

        //upload to internet folder
        button_upload = (ImageButton) findViewById(R.id.button_upload);
        button_upload.setOnClickListener(button_upload_OnClickListener);


        //all buttons
        all_buttons = new Button[]{button_save_close, button_open, button_reset, button_start, button_Nosave_close}; //, button_connect_cloud, button_download, button_upload};
        all_buttons_enable_status = new Boolean[all_buttons.length];
        for (Boolean b : all_buttons_enable_status)
            b = false;

        //info text box
        editText_wordcnt = (TextView) findViewById(R.id.editText_wordcnt);
        editText_progress = (TextView) findViewById(R.id.editText_progress);
        editText_flashcard_name = (TextView) findViewById(R.id.editText_flashcard_name);

        //progress bars
        progressBar_open = (ProgressBar) findViewById(R.id.progressBar_open);
        progressBar_save = (ProgressBar) findViewById(R.id.progressBar_save);
        progressBar_reset = (ProgressBar) findViewById(R.id.progressBar_reset);
        progressBar_upload = (ProgressBar) findViewById(R.id.progressBar_upload);
        progressBar_download = (ProgressBar) findViewById(R.id.progressBar_download);


        // keep current context
        mContext = MainActivity.this;

        // request for application permission at run time.
        // Note that permissions should be set inside AndroidManifest.xml file beforehand too.
        permission_request();

        //Cloud storage
        CloudRail.setAppKey(CLOUDRAIL_KEY);
        CloudRail.setAdvancedAuthenticationMode(true);
        dropbox_is_connected = false;


    }//onCreate

    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    @Override
    protected void onResume() {
        super.onResume();

        /*
        //resume Dropbox authentication.
        if (my_dropbox.isInitilized_started()) {
            //dropbox successfully connected
            if (my_dropbox.dropbox_finish_authentication(error_obj)) {
                button_download.setEnabled(true);
                button_upload.setEnabled(true);
            } else {
                error_dialog(error_obj);
            }
        }
        */

    }//onResume


    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //On click listener for button_open
    final View.OnClickListener button_open_OnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {

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
                }//onClick
            });
            AlertDialog dialog = mBuilder.create();
            dialog.show();

        } //onClick
    }; //button_open_OnClickListener


    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //On click listener for button save and close
    final View.OnClickListener button_save_close_close_OnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {

            //run a AsyncTask (a kind of thread) for file saving. Because it takes some seconds.
            SaverTaskClass my_saver_task = new SaverTaskClass();
            my_saver_task.execute(0); // execute(Params...)

        } //onClick
    }; //button_save_close_close_OnClickListener


    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //On click listener for button reset
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
                        }//onClick
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .show();

        } //onClick
    }; //button_reset_OnClickListener


    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //On click listener for button_start
    final View.OnClickListener button_start_OnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {

            Intent study_act = new Intent(MainActivity.this, StudyActivity.class);
            startActivityForResult(study_act, SECOND_ACTIVITY_RESULT_CODE);

        } //onClick
    }; //button_start_OnClickListener


    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    // This method is called when the second activity finishes
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SECOND_ACTIVITY_RESULT_CODE) {
            if (resultCode == RESULT_OK) {
                //String result=data.getStringExtra("result");
            }
            if (resultCode == RESULT_CANCELED) {
                //Write your code if there's no result
                update_text_boxes();
            }
        }
    }//onActivityResult


    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //On click listener for close without save button
    final View.OnClickListener button_Nosave_close_OnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {

            disable_interface();

            my_fc_col.close();

            //Clear File Info on TextBoxes
            update_text_boxes();

            button_open.setEnabled(true);

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
    public static Context getContext() {
        return mContext;
    }//getContext


    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    public static flashcard_collection getFlashcard() {
        return my_fc_col;
    }//getFlashcard


    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    public void update_text_boxes() {
        editText_wordcnt.setText(String.valueOf(my_fc_col.get_total_card_num() - my_fc_col.stage_list[0].get_card_count())); //show only active card count
        editText_progress.setText(String.valueOf(my_fc_col.getUser_progress_value()) + " %");
        editText_flashcard_name.setText(my_fc_col.box_name);
        return;
    }


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
                update_text_boxes();
                button_save_close.setEnabled(true);
                button_reset.setEnabled(true);
                button_start.setEnabled(true);
                button_Nosave_close.setEnabled(true);
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

            //TODO: below line is commented, because the saved HTML code, can not be opened again in XML parser. because of special character issue.
            //return true;
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
                //Clear File Info on TextBoxes
                update_text_boxes();

                button_open.setEnabled(true);

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
                update_text_boxes();

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


    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //On click listener for button_connect_cloud
    final View.OnClickListener button_connect_cloud_OnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            Cloud_login_Task cloud_login_task = new Cloud_login_Task();
            cloud_login_task.execute();  //TODO: re-executing cause program crash, why? should use another class for logout?
        } //onClick
    }; //button_connect_cloud_OnClickListener


    class Cloud_login_Task extends AsyncTask {
        Context context;

        //constructor
        public Cloud_login_Task() {
            context = MainActivity.getContext();
        } //constructor

        @Override
        protected Object doInBackground(Object[] objects) {
            Dropbox dropbox = new Dropbox(context,
                    DROPBOX_CLIENT_ID,
                    DROPBOX_CLIENT_SECRET,
                    DROPBOX_REDIRECT_URL,
                    "CLOUD_STATE");

            dropbox.useAdvancedAuthentication();

            // Dropbox Login/Logout
            if (dropbox_is_connected) {
                dropbox.logout();
                dropbox_is_connected = false;
            } else {
                dropbox.login();
                dropbox_is_connected = true;
            }
            return null;
        }//doInBackground

    } // Cloud_login_Task

    //pass browser output to this app
    private static final String BROWSABLE = "android.intent.category.BROWSABLE";

    @Override
    protected void onNewIntent(Intent intent) {
        if (intent.getCategories().contains(BROWSABLE)) {
            CloudRail.setAuthenticationResponse(intent);
        }
        super.onNewIntent(intent);
    }

    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //On click listener for button_download
    final View.OnClickListener button_download_OnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {

            //show a confirmation dialog before run.
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Download data")
                    .setMessage("Download may overwrite your data. Continue?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int whichButton) {
                            //run a AsyncTask (a kind of thread) for downloading. Because it takes some seconds.
                            // it includes delete of file, and re-open the flash file
                            DownloadTaskClass my_downloader_task = new DownloadTaskClass();
                            my_downloader_task.execute(fq_files); // execute(Params...). pass parameter to task.
                        }//onClick
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .show();

        } //onClick
    }; //button_download_OnClickListener

    class DownloadTaskClass extends AsyncTask<CharSequence[], Integer, Boolean> { //params, progress, result

        @Override
        protected void onPreExecute() {
            //txt.setText("Task Starting...");
            progressBar_download.setVisibility(ProgressBar.VISIBLE);
            save_interface_enable_status();
            disable_interface();
            button_connect_cloud.setEnabled(false);
            button_download.setEnabled(false);
            button_upload.setEnabled(false);
        }

        @Override
        protected Boolean doInBackground(CharSequence[]... params) {
            /*
            publishProgress(50);
            */
            Dropbox dropbox = new Dropbox(
                    MainActivity.getContext(),
                    DROPBOX_CLIENT_ID,
                    DROPBOX_CLIENT_SECRET,
                    DROPBOX_REDIRECT_URL,
                    "CLOUD_STATE");

            dropbox.useAdvancedAuthentication();

            /*
            if (!dropbox_is_connected) {
                error_obj.set_error_code(15); //"Before upload/download, connect to Dropbox first"
                return false;
            }
            */

            //first check that Ex-Storage is mounted or not.
            String storage_state = Environment.getExternalStorageState();
            if (!storage_state.equals("mounted")) {
                error_obj.set_error_code(3); //"Ex-Storage not mounted!"
                return false;
            }

            File fq_folder = null;
            File fq_file = null;
            File fq_file_temp = null;
            String path_on_dropbox = "";
            FileOutputStream outputStream = null;
            String FolderNameOnStorage = my_fc_col.getFolderNameOnStorage();

            String storage_root_path = Environment.getExternalStorageDirectory().getPath();
            fq_folder = new File(storage_root_path, FolderNameOnStorage);
            if (!fq_folder.exists()) {
                if (!fq_folder.mkdirs()) {// this will create folder.
                    error_obj.set_error_code(4); // "Program folder not created. No storage permission"
                    return false;
                }
            }

            //DropboxAPI.Entry response = null;
            CharSequence filelist[] = params[0];
            String FOLDER_NAME_ON_DROPBOX = flashcard_collection.getFolderNameOnStorage();

            try {
                for (int i = 0; i < filelist.length; i++) {

                    // file path on the dropbox
                    path_on_dropbox = "/" + FOLDER_NAME_ON_DROPBOX + "/" + (String) filelist[i];

                    // file path on external storage
                    fq_file = new File(fq_folder, (String) filelist[i]);
                    fq_file_temp = new File(fq_folder, (String) filelist[i] + ".2"); //save temporarily in *.2 file

                    // get the file from dropbox and put it on storage//TODO: check if files are same, do not download them....(if possible)
                    outputStream = new FileOutputStream(fq_file_temp);

                    // error if file does not exist
                    //response = mDBApi.getFile(path_on_dropbox, null, outputStream, null).getMetadata();

                    if (!dropbox.exists(path_on_dropbox)) { // none-existed file //TODO: handle other type of errors here too.

                        // close and delete it.
                        if (outputStream != null)
                            outputStream.close();

                        if (!fq_file_temp.delete()) {
                            error_obj.set_error_code(13); //"File can not be deleted"
                            return false;
                        }

                        continue; // next file
                    }

                    InputStream dropbox_stream = dropbox.download(path_on_dropbox);

                    //copy dropbox stream to file stream
                    byte[] buffer = new byte[1024];
                    while (true) {
                        int bytesRead = dropbox_stream.read(buffer);
                        if (bytesRead == -1)
                            break;
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    dropbox_stream.close();

                    if (outputStream != null)
                        outputStream.close();

                    // Now we are sure that file is downloaded but as a temporary file *.2 .
                    // then we delete the already existing file and replace it with downloaded one

                    if (fq_file.exists())
                        if (!fq_file.delete()) {
                            error_obj.set_error_code(13); //"File can not be deleted"
                            return false;
                        }

                    if (!fq_file_temp.renameTo(fq_file)) {
                        error_obj.set_error_code(18); //"File can not be renamed"
                        return false;
                    }

                }//for all files
            } catch (Exception e) {
                e.printStackTrace();
                error_obj.set_error_code(19); //"Error in Dropbox or file access during download"
                //TODO: there are various uploading error situations. consider them later. see dropbox documents from link on top of file.
                return false;
            }

            //return (my_dropbox.dropbox_download(params[0], my_fc_col.getFolderNameOnStorage(), error_obj));
            return true;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            //txt.setText("Running..." + values[0]);
            //progressBar_download.setProgress(values[0]);
            //TODO: add this part for download
        }

        @Override
        protected void onPostExecute(Boolean result) {

            progressBar_download.setVisibility(ProgressBar.INVISIBLE);
            load_interface_enable_status();
            button_connect_cloud.setEnabled(true);
            button_download.setEnabled(true);
            button_upload.setEnabled(true);

            if (result) {

                //Dialog for "downloaded Successfully".
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
                mBuilder.setIcon(android.R.drawable.checkbox_on_background);
                mBuilder.setTitle(" ");
                mBuilder.setMessage("downloaded Successfully");
                AlertDialog dialog = mBuilder.create();
                dialog.show();

            } else {
                error_dialog(error_obj);
            }
        }

    }//DownloadTaskClass


    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //On click listener for button_upload
    final View.OnClickListener button_upload_OnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {

            //show a confirmation dialog before run.
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Upload data")
                    .setMessage("Upload may overwrite your data. Continue?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int whichButton) {
                            //run a AsyncTask (a kind of thread) for uploading. Because it takes some seconds.
                            // it includes delete of file, and re-open the flash file
                            UploadTaskClass my_uploader_task = new UploadTaskClass();
                            my_uploader_task.execute(fq_files); // execute(Params...). pass parameter to task.
                        }//onClick
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .show();

        } //onClick
    }; //button_upload_OnClickListener

    class UploadTaskClass extends AsyncTask<CharSequence[], Integer, Boolean> { //params, progress, result

        @Override
        protected void onPreExecute() {
            //txt.setText("Task Starting...");
            progressBar_upload.setVisibility(ProgressBar.VISIBLE);
            save_interface_enable_status();
            disable_interface();
            button_connect_cloud.setEnabled(false);
            button_download.setEnabled(false);
            button_upload.setEnabled(false);
        }

        @Override
        protected Boolean doInBackground(CharSequence[]... params) {
            /*
            publishProgress(50);
            */
            Dropbox dropbox = new Dropbox(
                    MainActivity.getContext(),
                    DROPBOX_CLIENT_ID,
                    DROPBOX_CLIENT_SECRET,
                    DROPBOX_REDIRECT_URL,
                    "CLOUD_STATE");

            dropbox.useAdvancedAuthentication();

            /*
            if (!dropbox_is_connected) {
                error_obj.set_error_code(15); //"Before upload/download, connect to Dropbox first"
                return false;
            }
            */

            //First check that Ex-Storage is mounted or not.
            String storage_state = Environment.getExternalStorageState();
            if (!storage_state.equals("mounted")) {
                error_obj.set_error_code(3); //"Ex-Storage not mounted!"
                return false;
            }

            File fq_folder = null;
            File fq_file = null;
            String path_on_dropbox = "";
            String FolderNameOnStorage = my_fc_col.getFolderNameOnStorage();

            String storage_root_path = Environment.getExternalStorageDirectory().getPath();
            fq_folder = new File(storage_root_path, FolderNameOnStorage);
            //DropboxAPI.Entry response = null;
            CharSequence filelist[] = params[0];

            String FOLDER_NAME_ON_DROPBOX = flashcard_collection.getFolderNameOnStorage();
            if (!dropbox.exists("/" + FOLDER_NAME_ON_DROPBOX)) {
                dropbox.createFolder("/" + FOLDER_NAME_ON_DROPBOX);
            }

            //TODO: check if there are enough space dropbox to upload

            //try {
            for (int i = 0; i < filelist.length; i++) {

                // file path on the dropbox (for cloudrail api, it must start with "/")
                path_on_dropbox = "/" + FOLDER_NAME_ON_DROPBOX + "/" + (String) filelist[i];

                // file path on external storage
                fq_file = new File(fq_folder, (String) filelist[i]);

                // there is no file for uploading, skip it, go to next file.
                if (!fq_file.exists())
                    continue;

                // read the file and put it on dropbox
                //TODO: check if files are same, do not upload them....(if possible)
                //TODO: it seems dropbox automatically does not upload and overwrite a file if files is exactly similar. confirm this by test some big files.
                FileInputStream inputStream = null;
                try {
                    inputStream = new FileInputStream(fq_file);
                } catch (FileNotFoundException e) {
                    error_obj.set_error_code(17); //"File can not be uploaded"
                    e.printStackTrace();
                }
                //response = mDBApi.putFileOverwrite(path_on_dropbox, inputStream, fq_file.length(), null);

                dropbox.upload(
                        path_on_dropbox,
                        inputStream,
                        fq_file.length(),
                        true
                );

            }//for all files
            //TODO: there are various uploading error situations. consider them later. see dropbox documents from link on top of file.

            return true;

            //return (my_dropbox.dropbox_upload(params[0], my_fc_col.getFolderNameOnStorage(), error_obj));

        }//doInBackground

        @Override
        protected void onProgressUpdate(Integer... values) {
            //txt.setText("Running..." + values[0]);
            //progressBar_upload.setProgress(values[0]);
            //TODO: add this part for upload
        }//onProgressUpdate

        @Override
        protected void onPostExecute(Boolean result) {
            progressBar_upload.setVisibility(ProgressBar.INVISIBLE);
            load_interface_enable_status();
            button_connect_cloud.setEnabled(true);
            button_download.setEnabled(true);
            button_upload.setEnabled(true);

            if (result) {
                //Dialog for "uploaded Successfully".
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
                mBuilder.setIcon(android.R.drawable.checkbox_on_background);
                mBuilder.setTitle(" ");
                mBuilder.setMessage("Uploaded Successfully");
                AlertDialog dialog = mBuilder.create();
                dialog.show();

            } else {
                error_dialog(error_obj);
            }
        }//onPostExecute

    }//UploadTaskClass

}//public class MainActivity


//TODO: use should not loose its data after software or dictionary udpdate....
//TODO: check and ask user that the clock of phone is update.
//TODO : what is difference two forms of closing application? I see edited text are reloaded if you minimize the app and open it.
//TODO: how many save is ok on flash card? how to avoid saving on a similar location?
//TODO: function for compare or sort
//TODO: change file format: each card should has an ID, attribs: difficulty, terminology, book, ..
//TODO: compress/encrypt the file
//TODO: File open from/ Save to Dropbox or google drive.
//TODO: Automatic UML extract (schematic about how classes access each other
//TODO: add document folder including readme.txt and schematic visio files to project.
//TODO: animated stars in ring bar
//TODO: add pronunciation.
//TODO: add comment for each card
//TODO: add explanations and text in readme.txt about scheduling algorithm.
//TODO: when there is no card to review, show "waiting day for next card".
//TODO: some texboxes should not be editable.