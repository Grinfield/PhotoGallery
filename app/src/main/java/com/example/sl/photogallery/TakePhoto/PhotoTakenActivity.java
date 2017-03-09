package com.example.sl.photogallery.TakePhoto;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.transition.Fade;
import android.transition.Slide;
import android.view.Window;

import com.example.sl.photogallery.SingleFragmentActivity;

/**
 * Created by sl on 2017/2/28.
 */

public class PhotoTakenActivity extends SingleFragmentActivity {

    @Override
    protected String getTag() {
        return null;
    }

    @Override
    protected Fragment createFragment() {
        return new PhotoTakeFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
            getWindow().setEnterTransition(new Slide());
            getWindow().setExitTransition(new Fade());
            super.onCreate(savedInstanceState);
        }
        super.onCreate(savedInstanceState);
    }
}
