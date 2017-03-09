package com.example.sl.photogallery;

import android.app.ActivityOptions;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;
import com.example.sl.photogallery.LoginIn.LoginFragment;
import com.example.sl.photogallery.LoginIn.LoginUtil;
import com.example.sl.photogallery.TakePhoto.PhotoTakeFragment;
import com.example.sl.photogallery.TakePhoto.PhotoTakenActivity;
import com.example.sl.photogallery.ViewPhotos.PhotoGalleryFragment;
import com.example.sl.photogallery.Model.FlickrFetcher;
import com.googlecode.flickrjandroid.oauth.OAuth;

public class PhotoGalleryActivity extends SingleFragmentActivity
        implements BottomNavigationBar.OnTabSelectedListener, LoginFragment.OnFragmentShowedListener{
    private static final String TAG = "PhotoGalleryActivity";

    private BottomNavigationBar mBottomNavigationBar;
    private PhotoGalleryFragment mPhotoGalleryFragment;
    private PhotoTakeFragment mPhotoTakeFragment;
    private LoginFragment mLoginFragment;
    private LoginUtil mLoginUtil;

    @Override
    public void onFragmentShowed() {
        if (mBottomNavigationBar != null){
            PhotoGalleryFragment photoGalleryFragment =
                    (PhotoGalleryFragment)getSupportFragmentManager().findFragmentByTag("viewTab");
            if (photoGalleryFragment != null){
                mPhotoGalleryFragment = photoGalleryFragment;
                mPhotoGalleryFragment.mIsPersonal = true;
                mPhotoGalleryFragment.updateItems();
            }
            mBottomNavigationBar.selectTab(0, false);
        }
    }

    @Override
    protected String getTag() {
        return "viewTab";
    }

    @Override
    protected Fragment createFragment() {
        mPhotoGalleryFragment = new PhotoGalleryFragment();
        return mPhotoGalleryFragment;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);

        if (intent.ACTION_SEARCH.equals(intent.getAction())){
            PhotoGalleryFragment fragment = (PhotoGalleryFragment)getSupportFragmentManager().findFragmentById(R.id.fragmentContainer);
            String query = intent.getStringExtra(SearchManager.QUERY);
            Log.i(TAG, "Received a new search query: " + query);
            PreferenceManager.getDefaultSharedPreferences(this).edit().putString(FlickrFetcher.PREF_SEARCH_QUERY, query).commit();
            fragment.updateItems();
        }
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_bottom_bar;
    }

    @Override
    protected void onStart() {
        super.onStart();

    }


    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        String scheme = intent.getScheme();
        Log.i(TAG, "scheme: " + scheme);
        if ("oauth".equals(scheme)){
            mLoginUtil = new LoginUtil(this);
            OAuth savedToken = mLoginUtil.getSavedOAuthToken();
            if (savedToken == null || savedToken.getUser() == null){
                mLoginUtil.onGetOAuthToken(intent);
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBottomNavigationBar = (BottomNavigationBar) findViewById(R.id.bottom_navigation_bar_container);

        BottomNavigationItem imageItem = new BottomNavigationItem(R.drawable.ic_action_images, "view photo");
        imageItem.setActiveColorResource(R.color.colorAccent);
        BottomNavigationItem takePhotoItem = new BottomNavigationItem(R.drawable.ic_action_camera, "take photo");
        takePhotoItem.setActiveColorResource(R.color.colorAccent);
        BottomNavigationItem loginInItem = new BottomNavigationItem(R.drawable.ic_action_login, "login");
        loginInItem.setActiveColorResource(R.color.colorAccent);

        //mBottomNavigationBar.setMode(BottomNavigationBar.MODE_SHIFTING);
        mBottomNavigationBar.setBackgroundResource(android.R.color.white);
        mBottomNavigationBar.addItem(imageItem)
                .addItem(takePhotoItem)
                .addItem(loginInItem)
                .setFirstSelectedPosition(0)
                .initialise();
        mBottomNavigationBar.setTabSelectedListener(this);
    }

    @Override
    public void onTabReselected(int position) {

    }

    public void hideFragments(FragmentTransaction fragmentTransaction){
        if (mPhotoGalleryFragment != null){
            fragmentTransaction.hide(mPhotoGalleryFragment);
        }
        if (mLoginFragment != null){
            fragmentTransaction.hide(mLoginFragment);
        }
    }

    @Override
    public void onTabSelected(int position) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        hideFragments(fragmentTransaction);

        switch (position){
            case 0:
                if (mPhotoGalleryFragment == null){
                    mPhotoGalleryFragment = new PhotoGalleryFragment();
                    fragmentTransaction.add(R.id.fragmentContainer, mPhotoGalleryFragment, "viewTab");
                }else
                    fragmentTransaction.show(mPhotoGalleryFragment);
                break;
            case 1:
                /*
                if (fragment == photoTakenFragment)
                    break;
                if (photoTakenFragment == null){
                    photoTakenFragment = new PhotoTakeFragment();
                }
                fragmentTransaction.replace(R.id.fragmentContainer, photoTakenFragment).commit();
                mBottomNavigationBar.hide(true);
                */
                Intent intent = new Intent(this, PhotoTakenActivity.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                    startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
                }else startActivity(intent);

                break;
            case 2:
                Log.i(TAG, "LoginFragment: " + mLoginFragment);
                if (mLoginFragment == null){
                    mLoginFragment = new LoginFragment();
                    fragmentTransaction.add(R.id.fragmentContainer, mLoginFragment, "loginTab");
                }else
                    fragmentTransaction.show(mLoginFragment);
                break;
            default:
                break;
        }

        fragmentTransaction.commit();
    }

    @Override
    public void onTabUnselected(int position) {

    }
}
