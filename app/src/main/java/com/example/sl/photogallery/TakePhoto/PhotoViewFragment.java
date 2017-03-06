package com.example.sl.photogallery.TakePhoto;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.sl.photogallery.ImageCache.ImageUtil;

/**
 * Created by sl on 2017/3/1.
 */

public class PhotoViewFragment extends DialogFragment {
    public static final String EXTRA_IMAGE_PATH = "com.example.sl.photogallery.photo_path";
    private ImageUtil mImageUtil = new ImageUtil();
    private ImageView mImageView;

    public static PhotoViewFragment newInstance(String path){
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_IMAGE_PATH, path);
        PhotoViewFragment fragment = new PhotoViewFragment();
        fragment.setArguments(args);
        fragment.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState){
        mImageView = new ImageView(getActivity());
        String imagePath = (String)getArguments().getSerializable(EXTRA_IMAGE_PATH);
        Bitmap bitMap = mImageUtil.decodeSampledBitmapFromFile(imagePath, 0, 0);
        mImageView.setImageBitmap(bitMap);
        return mImageView;
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        mImageUtil.cleanImageView(mImageView);
    }
}
