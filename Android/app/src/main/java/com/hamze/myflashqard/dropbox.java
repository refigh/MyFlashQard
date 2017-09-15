package com.hamze.myflashqard;

import android.os.Environment;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AppKeyPair;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * Created by hamze on 9/14/2017.
 */

//TODO: Find a way to avoid dangerous mistakes that may cause to loss of data in download/upload.

//Dropbox: I registered application on my dropbox (hamze60@gmail.com)
//Tutorial 1: https://www.numetriclabz.com/upload-files-to-dropbox-using-core-api-android-tutorial/
//Tutorial 2: https://www.dropbox.com/developers-v1/core/start/android
//Dropbox document: https://www.dropboxstatic.com/static/developers/dropbox-android-sdk-1.6.3-docs/com/dropbox/client2/DropboxAPI.html

public class dropbox {

    // Replace APP_KEY with your APP_KEY
    final static private String APP_KEY = "wryp2qsk27carsq";

    // Replace APP_SECRET with your APP_SECRET
    final static private String APP_SECRET = "o3y34w2uassu4bd";

    // For now we chose is similar to folder on Android storage. but can be user selectable later
    final static private String FOLDER_NAME_ON_DROPBOX = flashcard_collectin.getFolderNameOnStorage();

    private DropboxAPI<AndroidAuthSession> mDBApi;

    private boolean dropbox_connection_initialization_started;
    private boolean dropbox_connected;


    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //constructor
    public dropbox() {
        mDBApi = null;
        dropbox_connection_initialization_started = false;
        dropbox_connected = false;
    }//dropbox


    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------

    /**
     * Initialize the Session of the Key pair to authenticate with dropbox
     * Start the authentication flow.
     * If Dropbox app is installed, SDK will switch to it otherwise it will fallback to the browser.
     */
    public void dropbox_initialize_session() {

        // store app key and secret key
        // Pass app key pair to the new DropboxAPI object.
        AppKeyPair appKeys = new AppKeyPair(APP_KEY, APP_SECRET);
        AndroidAuthSession session = new AndroidAuthSession(appKeys);
        mDBApi = new DropboxAPI<AndroidAuthSession>(session);

        // start authentication.
        mDBApi.getSession().startOAuth2Authentication(MainActivity.getContext());

        dropbox_connection_initialization_started = true;

        return;

    } //Dropbox_initialize_session


    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    // Finishing Dropbox authentication
    public boolean dropbox_finish_authentication(error error_obj) {

        //then current method can not be run again, without initialization step run first again .
        dropbox_connection_initialization_started = false;

        if (mDBApi.getSession().authenticationSuccessful()) {
            try {
                // Required to complete auth, sets the access token on the session
                mDBApi.getSession().finishAuthentication();

                // TODO: If we do not want to re-authenticate every time, we have to store the below retrieve token in the SharedPreferences
                String accessToken = mDBApi.getSession().getOAuth2AccessToken();

                dropbox_connected = true;
                return true;

            } catch (IllegalStateException e) {
                error_obj.set_error_code(14); //"Cannot authenticate to Dropbox"
                return false;
            }
        } else {
            error_obj.set_error_code(14); //"Cannot authenticate to Dropbox"
            return false;
        }


    }//dropbox_finish_authentication


    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    // Upload all files to dropbox
    public boolean dropbox_upload(CharSequence filelist[], String FolderNameOnStorage, error error_obj) {

        if (!dropbox_connected) {
            error_obj.set_error_code(15); //"Before upload/download, connect to Dropbox first"
            return false;
        }

        //First check that Ex-Storage is mounted or not.
        String storage_state = Environment.getExternalStorageState();
        if (!storage_state.equals("mounted")) {
            error_obj.set_error_code(3); //"Ex-Storage not mounted!"
            return false;
        }

        File fq_folder = null;
        File fq_file = null;
        String path_on_dropbox = "";

        String storage_root_path = Environment.getExternalStorageDirectory().getPath();
        fq_folder = new File(storage_root_path, FolderNameOnStorage);
        DropboxAPI.Entry response = null;

        try {
            for (int i = 0; i < filelist.length; i++) {

                // file path on the dropbox
                path_on_dropbox = FOLDER_NAME_ON_DROPBOX + "/" + (String) filelist[i];

                // file path on external storage
                fq_file = new File(fq_folder, (String) filelist[i]);

                // there is no file for uploading, skip it, go to next file.
                if (!fq_file.exists())
                    continue;

                // read the file and put it on dropbox
                //TODO: check if files are same, do not upload them....(if possible)
                //TODO: it seems dropbox automatically does not upload and overwrite a file if files is exactly similar. confirm this by test some big files.
                FileInputStream inputStream = new FileInputStream(fq_file);
                response = mDBApi.putFileOverwrite(path_on_dropbox, inputStream, fq_file.length(), null);

            }//for all files

        } catch (Exception e) {
            //TODO: there are various uploading error situations. consider them later. see dropbox documents from link on top of file.
            e.printStackTrace();
            if (response.rev.isEmpty())
                error_obj.set_error_code(17); //"File can not be uploaded"
            else
                error_obj.set_error_code(16); //"Error in Dropbox or file access during upload"
            return false;
        }

        return true;

    }//dropbox_upload


    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    // Download all files from  dropbox

    public boolean dropbox_download(CharSequence filelist[], String FolderNameOnStorage, error error_obj) {

        if (!dropbox_connected) {
            error_obj.set_error_code(15); //"Before upload/download, connect to Dropbox first"
            return false;
        }

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

        String storage_root_path = Environment.getExternalStorageDirectory().getPath();
        fq_folder = new File(storage_root_path, FolderNameOnStorage);
        DropboxAPI.Entry response = null;

        try {
            for (int i = 0; i < filelist.length; i++) {

                // file path on the dropbox
                path_on_dropbox = FOLDER_NAME_ON_DROPBOX + "/" + (String) filelist[i];

                // file path on external storage
                fq_file = new File(fq_folder, (String) filelist[i]);
                fq_file_temp = new File(fq_folder, (String) filelist[i] + ".2"); //save temporarily in *.2 file

                // get the file from dropbox and put it on storage//TODO: check if files are same, do not download them....(if possible)
                outputStream = new FileOutputStream(fq_file_temp);

                //capture error if file does not exist
                try {
                    response = mDBApi.getFile(path_on_dropbox, null, outputStream, null).getMetadata();
                } catch (Exception e) { //catch none-existed file //TODO: handle other type of errors here too.

                    // close and delete it.
                    if (outputStream != null)
                        outputStream.close();

                    if (!fq_file_temp.delete()) {
                        error_obj.set_error_code(13); //"File can not be deleted"
                        return false;
                    }

                    continue; // next file
                }//catch

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

        return true;
    }//dropbox_download


    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------

    public boolean isInitilized_started() {
        return dropbox_connection_initialization_started;
    }


}
