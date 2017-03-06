package com.example.sl.photogallery.LoginIn;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.example.sl.photogallery.R;
import com.googlecode.flickrjandroid.oauth.OAuth;
import com.googlecode.flickrjandroid.oauth.OAuthToken;
import com.googlecode.flickrjandroid.people.User;

import java.util.Locale;

/**
 * Created by sl on 2017/3/4.
 */

public class LoginUtil {
    private static final String TAG = "PhotoGalleryActivity";
    private AppCompatActivity mActivity;

    //save OAuth params with shared preferences
    public static final String PREF_OAUTH_CALLBACK = "oauth_callback";
    public static final String PREF_OAUTH_TOKEN = "oauth_token";
    public static final String PREF_OAUTH_TOKEN_SECRET = "oauth_token_secret";
    public static final String PREF_OAUTH_USR_NAME = "oauth_user_name";
    public static final String PREF_OAUTH_USER_ID = "oauth_user_id";

    //used for login
    public static final String OAUTH_CALLBACK = "oauth://com.example.sl";
    public static final String PARAM_OAUTH_CONSUMER_KEY = "oauth_consumer_key";
    private static final String PARAM_OAUTH_NONCE = "oauth_nonce";
    private static final String PARAM_OAUTH_TIMESTAMP = "oauth_timestamp";
    private static final String PARAM_OAUTH_SIGNATURE = "oauth_signature_method";
    private static final String PARAM_OAUTH_VERSION = "oauth_version";

    public LoginUtil(AppCompatActivity context){
        mActivity = context;
    }
    public LoginUtil(){

    }

    public void onGetOAuthToken(Intent intent) {
        Log.i(TAG, "intent: " + intent);
        Uri uri = intent.getData();
        String query = uri.getQuery();
        Log.i(TAG, "query: " + query );
        String[] data = query.split("&");
        OAuth oauth = getSavedOAuthToken();
        //String oauthToken = data[0].substring(data[0].indexOf("=") + 1);
        String oauthVerifier = data[1].substring(data[1].indexOf("=") + 1);
        Log.i(TAG, "oauthToken: " + oauth.getToken().getOauthToken());
        Log.i(TAG, "oauthVerifier: " + oauthVerifier);
        //OAuth oauth = FlickrLoginManager.getOAuthToken();
        //String tokenSecret = CacheManager.getStringCacheData(KEY_TOKEN_SECRET);
        //SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        //String tokenSecret = mSharedPreferences.getString(FlickrFetcher.PREF_OAUTH_TOKEN_SECRET, null);
        //Log.i(TAG, "oauth:" + tokenSecret);
        /*
        if (oauth == null || oauth.getToken() == null || oauth.getToken().getOauthTokenSecret() == null) {
            dismissLoadingDialog();
            showToastErrorMessage("Authorization failed");
            return;
        }
        */

        /*
        if (oauth != null && oauth.getToken() != null
                && oauth.getToken().getOauthTokenSecret() != null){
        }
        */
        OAuthTask task = new OAuthTask(mActivity);
        task.execute(oauth.getToken().getOauthToken(), oauth.getToken().getOauthTokenSecret(), oauthVerifier);
    }

    public OAuth getSavedOAuthToken() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mActivity);
        String oauthTokenString = prefs.getString(PREF_OAUTH_TOKEN, null);
        String tokenSecret = prefs.getString(PREF_OAUTH_TOKEN_SECRET, null);
        if (oauthTokenString == null && tokenSecret == null) {
            return null;
        }
        OAuth oauth = new OAuth();
        String userName = prefs.getString(PREF_OAUTH_USR_NAME, null);
        String userId = prefs.getString(PREF_OAUTH_USER_ID, null);
        if (userId != null) {
            User user = new User();
            user.setUsername(userName);
            user.setId(userId);
            oauth.setUser(user);
        }

        OAuthToken oauthToken = new OAuthToken();
        oauth.setToken(oauthToken);
        oauthToken.setOauthToken(oauthTokenString);
        oauthToken.setOauthTokenSecret(tokenSecret);

        Log.i(TAG, "oauthToken: " + oauthTokenString + " tokenSecret: " + tokenSecret +
                " userName: " + userName + " userId: " + userId);
        return oauth;
    }

    public void clearOAuthToken(){
        saveOAuthToken(null, null, null, null);
    }

    public void saveOAuthToken(String userName, String userId, String token,
                               String tokenSecret) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mActivity);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(PREF_OAUTH_TOKEN, token);
        editor.putString(PREF_OAUTH_TOKEN_SECRET, tokenSecret);
        editor.putString(PREF_OAUTH_USR_NAME, userName);
        editor.putString(PREF_OAUTH_USER_ID, userId);
        editor.apply();
    }

    // checks if auth is failed or succeeded
    public void onOAuthDone(OAuth result) {
        if (result == null) {
            Toast.makeText(mActivity, "Authorization failed",
                    Toast.LENGTH_LONG).show();
        } else {
            User user = result.getUser();
            OAuthToken token = result.getToken();
            if (user == null
                    || user.getId() == null
                    || token == null
                    || token.getOauthToken() == null
                    || token.getOauthTokenSecret() == null) {
                Toast.makeText(mActivity, "Authorization failed",
                        Toast.LENGTH_LONG).show();
                return;
            }
            String message = String.format(Locale.US,
                            "Authorization Succeed: user=%s, userId=%s, oauthToken=%s, tokenSecret=%s",
                            user.getUsername(), user.getId(),
                            token.getOauthToken(), token.getOauthTokenSecret());
            Toast.makeText(mActivity, message, Toast.LENGTH_LONG).show();
            saveOAuthToken(user.getUsername(), user.getId(),
                    token.getOauthToken(), token.getOauthTokenSecret());

            FragmentManager fm = mActivity.getSupportFragmentManager();
            LoginFragment fragment = (LoginFragment) fm.findFragmentById(R.id.fragmentContainer);
            fragment.load(result);
        }
    }
}
