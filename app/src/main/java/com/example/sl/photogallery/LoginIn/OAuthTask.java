/**
 *
 */
package com.example.sl.photogallery.LoginIn;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.example.sl.photogallery.Model.FlickrFetcher;
import com.example.sl.photogallery.PhotoGalleryActivity;
import com.googlecode.flickrjandroid.REST;
import com.googlecode.flickrjandroid.oauth.OAuth;
import com.googlecode.flickrjandroid.oauth.OAuthInterface;

/**
 * Created by sl on 2017/3/2.
 */

public class OAuthTask extends AsyncTask<String, Integer, OAuth> {
    private static final String TAG = "OAuthTask";
    private static final String LOG_TAG = "OAuthTask";
    private AppCompatActivity mActivity;
    private LoginUtil mLoginUtil = null;

    public OAuthTask(AppCompatActivity activity) {
        super();
        mActivity = activity;
        mLoginUtil = new LoginUtil(mActivity);
    }

    @Override
    protected OAuth doInBackground(String... params) {
        String oauthToken = params[0];
        String oauthTokenSecret = params[1];
        String verifier = params[2];
        Log.i(TAG, "token: " + oauthToken + " tokenSecret: " + oauthTokenSecret + " verifier: " + verifier);
        //Flickr f = FlickrManager.getInstance().getFlickr();
        //OAuthInterface oauthApi = f.getOAuthInterface();
        try {
            OAuthInterface oAuth = new OAuthInterface(FlickrFetcher.API_KEY, FlickrFetcher.SECRET_KEY, new REST());
            return oAuth.getAccessToken(oauthToken, oauthTokenSecret, verifier);
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
            return null;
        }
    }

    @Override
    protected void onPostExecute(OAuth result) {
        Log.i(TAG, "print OAuth: " + result);
        //mActivity.finish();
        if (mActivity == null || !(mActivity instanceof PhotoGalleryActivity))
            return;

        mLoginUtil.onOAuthDone(result);
    }
}
