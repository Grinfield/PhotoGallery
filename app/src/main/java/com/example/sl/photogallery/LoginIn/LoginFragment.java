package com.example.sl.photogallery.LoginIn;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.sl.photogallery.BaseFragment;
import com.example.sl.photogallery.R;
import com.example.sl.photogallery.ViewPhotos.PhotoGalleryFragment;
import com.googlecode.flickrjandroid.oauth.OAuth;

/**
 * Created by sl on 2017/2/28.
 */
public class LoginFragment extends BaseFragment {
    public static final String  ACTION_SEND_OAUTH = "com.example.sl.photogallery.SEND_OAUTH";
    public static final String OAUTH_KEY = "oauth";

    // UI references.
    private TextView mUserNameText;
    private ImageView mUserIconView;
    private Button mUserPhotoButton;
    private Button mLoginButton;
    private View mUserInfoLayout;
    private SharedPreferences mPreferences;
    private LoginUtil mLoginUtil;
    private OAuth mOauth;
    private boolean isLogin = false;
    private OnFragmentShowedListener mFragmentShowedListener;

    public interface OnFragmentShowedListener{
        void onFragmentShowed();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLoginUtil = new LoginUtil((AppCompatActivity) getActivity());
        mOauth = mLoginUtil.getSavedOAuthToken();
        setRetainInstance(true);
        setHasOptionsMenu(true);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mFragmentShowedListener = (OnFragmentShowedListener)activity;
        }catch (ClassCastException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onReceiveOauth(OAuth oauth) {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = (View)getActivity().getLayoutInflater().inflate(R.layout.login_in_fragment, container, false);
        initView(v);

        return v;
    }

    private void initView(View v) {
        mUserIconView = (ImageView)v.findViewById(R.id.user_image_icon);

        mUserNameText = (TextView)v.findViewById(R.id.user_name_text_view);
        mUserInfoLayout = (View)v.findViewById(R.id.user_info_layout);
        mUserInfoLayout.setVisibility(View.INVISIBLE);
        mLoginButton = (Button) v.findViewById(R.id.email_sign_in_button);
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new LoginTask((AppCompatActivity) getActivity()).execute();
            }
        });

        mUserPhotoButton = (Button)v.findViewById(R.id.self_photos_button);
        mUserPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fm.beginTransaction();
                fragmentTransaction.hide(LoginFragment.this);
                PhotoGalleryFragment fragment = (PhotoGalleryFragment) fm.findFragmentByTag("viewTab");
                if (fragment != null){
                    fragmentTransaction.show(fragment);
                }else
                    fragmentTransaction.add(R.id.fragmentContainer, PhotoGalleryFragment.getInstance(mOauth), "viewTab");
                fragmentTransaction.commit();
                fm.executePendingTransactions();
                mFragmentShowedListener.onFragmentShowed();
            }
        });

        load(mOauth);
    }

    public void load(OAuth oauth) {
        if (oauth != null && oauth.getUser().getId() != null){
            String userName = oauth.getUser().getUsername();
            if (userName != null){
                isLogin = true;
                mUserInfoLayout.setVisibility(View.VISIBLE);
                mUserNameText.setText(userName);
                mLoginButton.setVisibility(View.INVISIBLE);
            }
        }else {
            mUserInfoLayout.setVisibility(View.INVISIBLE);
            mLoginButton.setVisibility(View.VISIBLE);
        }
        sendOauthBroadcast(oauth);
    }

    private void sendOauthBroadcast(OAuth oauth) {
        Intent intent = new Intent(ACTION_SEND_OAUTH);
        intent.putExtra(OAUTH_KEY, oauth);
        getActivity().sendBroadcast(intent);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        getActivity().getMenuInflater().inflate(R.menu.log_out_menu, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem logItem = menu.findItem(R.id.menu_log_out);
        if (isLogin){
            logItem.setTitle(R.string.menu_logout_string);
        }else {
            logItem.setTitle(R.string.menu_login_string);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_log_out:
                isLogin = false;
                mLoginUtil.clearOAuthToken();
                mOauth = mLoginUtil.getSavedOAuthToken();
                load(mOauth);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
