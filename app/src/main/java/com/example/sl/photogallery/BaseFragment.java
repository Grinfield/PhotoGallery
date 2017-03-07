package com.example.sl.photogallery;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;

import com.example.sl.photogallery.LoginIn.LoginFragment;
import com.example.sl.photogallery.Service.PollService;
import com.googlecode.flickrjandroid.oauth.OAuth;

/**
 * Created by sl on 2016/11/14.
 */

public abstract class BaseFragment extends Fragment {
    public static final String TAG = "BaseFragment";

    private BroadcastReceiver mOnShowNotification = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(getActivity(), "Got a broadcast: " + intent.getAction(), Toast.LENGTH_LONG).show();
            //If we receive this, we are visible, so cancel the notification
            Log.i(TAG, "Canceling notification");
            setResultCode(Activity.RESULT_CANCELED);
        }
    };

    private BroadcastReceiver mOnReceiveOAuth = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "Got a broadcast: " + intent.getAction());
            OAuth oAuth = (OAuth)intent.getSerializableExtra(LoginFragment.OAUTH_KEY);
            onReceiveOauth(oAuth);
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(PollService.ACTION_SHOW_NOTIFICATION);
        getActivity().registerReceiver(mOnShowNotification, filter, PollService.PERM_PRIVATE, null);
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mOnShowNotification);
    }

    public abstract void onReceiveOauth(OAuth oauth);

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IntentFilter filter = new IntentFilter(LoginFragment.ACTION_SEND_OAUTH);
        getActivity().registerReceiver(mOnReceiveOAuth, filter, PollService.PERM_PRIVATE, null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(mOnReceiveOAuth);
    }
}
