package com.example.sl.photogallery.ViewPhotos;

import android.support.v4.app.Fragment;

import com.example.sl.photogallery.SingleFragmentActivity;

/**
 * Created by sl on 2016/11/15.
 */

public class PhotoPageActivity extends SingleFragmentActivity {


    @Override
    protected Fragment createFragment() {
        return new PhotoPageFragment();
    }
}
