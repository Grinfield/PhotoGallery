package com.example.sl.photogallery.LoginIn;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.example.sl.photogallery.FlickrFetcher;
import com.googlecode.flickrjandroid.REST;
import com.googlecode.flickrjandroid.auth.Permission;
import com.googlecode.flickrjandroid.oauth.OAuthInterface;
import com.googlecode.flickrjandroid.oauth.OAuthToken;

import java.net.URL;

/**
 * Created by sl on 2017/3/2.
 */

public class LoginTask extends AsyncTask<Void, Void, String>  {
    private static final String TAG = "LoginTask";
    /*
    private static final String ENDPOINT_OAUTH = "https://api.flickr.com/services/oauth/request_token";
    public static final String API_KEY = "77bd723be1c9570c9df9cb84109eebbe";
    public static final String SECRET_KEY = "724af57ae7331f98";

    private static final String OAUTH_CALLBACK = "oauth://com.example.sl";
    public static final String PARAM_OAUTH_TOKEN_SECTET = "oauth_token_secret";
    public static final String PARAM_OAUTH_CONSUMER_KEY = "oauth_consumer_key";
    private static final String PARAM_OAUTH_CALLBACK = "oauth_callback";
    private static final String PARAM_OAUTH_NONCE = "oauth_nonce";
    private static final String PARAM_OAUTH_TIMESTAMP = "oauth_timestamp";
    private static final String PARAM_OAUTH_SIGNATURE = "oauth_signature_method";
    private static final String PARAM_OAUTH_VERSION = "oauth_version";
    private static final String PARAM_OAUTH_TOKEN = "oauth_version";
    */
    //private static final Uri OAUTH_CALLBACK_URI = Uri.parse(FlickrLoginManager.CALLBACK_SCHEME + "://oauth");
    private ProgressDialog mProgressDialog;
    private AppCompatActivity mActivity;
    private SharedPreferences mSharedPreferences = null;
    private LoginUtil mLoginUtil = null;

    public LoginTask(AppCompatActivity context) {
        super();
        mActivity = context;
        mLoginUtil = new LoginUtil(mActivity);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mProgressDialog = new ProgressDialog(mActivity, AlertDialog.THEME_HOLO_LIGHT);
        mProgressDialog.setTitle("");
        mProgressDialog.setMessage("Generating the authorization request...");
        mProgressDialog.setCanceledOnTouchOutside(true);
        mProgressDialog.setCancelable(true);
        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dlg) {
                LoginTask.this.cancel(true);
            }
        });
        mProgressDialog.show();
    }

    @Override
    protected String doInBackground(Void... params) {

        try {
            OAuthInterface oAuth = new OAuthInterface(FlickrFetcher.API_KEY, FlickrFetcher.SECRET_KEY, new REST());
            OAuthToken oauthToken = oAuth.getRequestToken(LoginUtil.OAUTH_CALLBACK);
            Log.i(TAG, "oauthToken: " + oauthToken);
            saveCurrentToken(oauthToken.getOauthToken(), oauthToken.getOauthTokenSecret());
            Log.i(TAG, "oauthToken secret: " + oauthToken.getOauthTokenSecret());
            URL oauthUrl = oAuth.buildAuthenticationUrl(Permission.WRITE, oauthToken);
            //String oauthResponse = new FlickrFetcher().getUrl(oauthUrl.toString());
            //URL accessUrl = oAuth.getAccessToken(oauthToken, oauthToken.getOauthTokenSecret(), oauthToken.);
            Log.i(TAG, "oauthUrl: " + oauthUrl);
            //Log.i(TAG, "oauthResponse: " + oauthResponse);
            return oauthUrl.toString();
        } catch (Exception e) {
            Log.e("Error to oauth", e.getMessage());
            return "error:" + e.getMessage();
        }
    }

    private void saveCurrentToken(String token, String tokenSecret) {
        //mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mActivity);
        //mSharedPreferences.edit().putString(FlickrFetcher.PREF_OAUTH_TOKEN_SECRET, tokenSecret);
        //LoginActivity loginActivity = ((LoginActivity) mContext);
        mLoginUtil.saveOAuthToken(null, null, token, tokenSecret);
    }

    @Override
    protected void onPostExecute(String result) {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
        if (result == null || result.startsWith("error")) {
            Toast.makeText(mActivity, result, Toast.LENGTH_LONG).show();
            return;
        }
        mActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(result)));
    }
}
