package com.hamze.myflashqard;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AppKeyPair;

/**
 * Created by hamze on 9/14/2017.
 */

//Dropbox: I registered application on my dropbox (hamze60@gmail.com)
//Tutorial 1: https://www.numetriclabz.com/upload-files-to-dropbox-using-core-api-android-tutorial/
//Tutorial 2: https://www.dropbox.com/developers-v1/core/start/android

public class dropbox {

    // Replace APP_KEY with your APP_KEY
    final static private String APP_KEY = "wryp2qsk27carsq";
    // Relace APP_SECRET with your APP_SECRET
    final static private String APP_SECRET = "o3y34w2uassu4bd";
    private DropboxAPI<AndroidAuthSession> mDBApi;

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

    } //Dropbox_initialize_session


    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------
    // finishing Dropbbox authentication
    public boolean dropbox_finish_authentication(error error_obj) {

        if (mDBApi.getSession().authenticationSuccessful()) {
            try {
                // Required to complete auth, sets the access token on the session
                mDBApi.getSession().finishAuthentication();

                // If we do not want to re-authenticate every time, we have to store the below retrieve token in the SharedPreferences
                String accessToken = mDBApi.getSession().getOAuth2AccessToken();

            } catch (IllegalStateException e) {
                error_obj.set_error_code(14); //"Cannot authenticate to Dropbox"
                return false;
            }
        }

        return true;
    }//dropbox_finish_authentication

}
