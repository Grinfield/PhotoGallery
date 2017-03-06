package com.example.sl.photogallery.LoginIn;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.sl.photogallery.R;
import com.example.sl.photogallery.ViewPhotos.PhotoGalleryFragment;
import com.example.sl.photogallery.VisibleFragment;
import com.googlecode.flickrjandroid.oauth.OAuth;

/**
 * Created by sl on 2017/2/28.
 */
public class LoginFragment extends VisibleFragment{
    // UI references.
    private TextView mUserNameText;
    private ImageView mUserIconView;
    private Button mUserPhotoButton;
    private Button mLoginButton;
    private View mUserInfoLayout;
    private SharedPreferences mPreferences;
    private LoginUtil mLoginUtil;
    private OAuth mOauth;
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
                //Intent intent = new Intent(getActivity(), LoginActivity.class);
                //startActivity(intent);
            }
        });

        mUserPhotoButton = (Button)v.findViewById(R.id.self_photos_button);
        mUserPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fm.beginTransaction();
                fragmentTransaction.hide(LoginFragment.this);
                fragmentTransaction.add(R.id.fragmentContainer, PhotoGalleryFragment.getInstance(mOauth), "viewTab")
                                    .commit();
                fm.executePendingTransactions();
                mFragmentShowedListener.onFragmentShowed();
            }
        });

        if (mOauth != null && mOauth.getUser() != null) {
            load(mOauth);
        }
    }

    public void load(OAuth oauth) {
        //mPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if (oauth != null){
            String userName = oauth.getUser().getUsername();
            String userId = oauth.getUser().getId();
            String userUrl = oauth.getUser().getPhotosurl();
            String userProfile = oauth.getUser().getProfileurl();
            Log.i("LoginFragment", "userId: " + userId + " userUrl: " + userUrl + " userProfile: " + userProfile);

            if (userName != null){
                mUserInfoLayout.setVisibility(View.VISIBLE);
                mUserNameText.setText(userName);
                mLoginButton.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.log_out_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_log_out:
                mLoginUtil.clearOAuthToken();
                Fragment currentFragment = getFragmentManager().findFragmentByTag("loginTab");
                FragmentTransaction fragTransaction = getFragmentManager().beginTransaction();
                fragTransaction.detach(currentFragment);
                fragTransaction.attach(currentFragment);
                fragTransaction.commit();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
