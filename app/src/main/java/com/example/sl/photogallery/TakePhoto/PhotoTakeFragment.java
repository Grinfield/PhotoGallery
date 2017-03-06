package com.example.sl.photogallery.TakePhoto;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.example.sl.photogallery.ImageCache.ImageUtil;
import com.example.sl.photogallery.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Created by sl on 2017/2/28.
 */

public class PhotoTakeFragment extends Fragment{
    private static final String TAG = "PhotoTakeFragment";
    private Camera mCamera;
    private ImageUtil mImageUtil = new ImageUtil();
    private static final String PREF_PHOTO_NAME = "photoName";
    private SharedPreferences mPrefs;

    private Button mTakeButton;
    private ImageView mShowPhotoView;
    private ImageButton mAddEffectButton;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private View mProgressBarContainer;

    private Camera.ShutterCallback mShutterCallback = new Camera.ShutterCallback() {
        @Override
        public void onShutter() {
            mProgressBarContainer.setVisibility(View.VISIBLE);
            //mCamera.enableShutterSound(true);
        }
    };

    private Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            String photoPath = null;
            String randomPrefix = null;
            File path = null;
            File directory = null;
            OutputStream os = null;
            boolean isTaken = false;

            if (isExternalStorageAvailable()){
                randomPrefix = UUID.randomUUID().toString();
                directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
            }else {
                directory = getActivity().getFilesDir();
            }
            path = new File(directory, randomPrefix + ".jpg");

            try {
                os = new FileOutputStream(path);
                os.write(data);
                os.close();
                isTaken = true;
            }catch (IOException e){
                isTaken = false;
            }finally {
                try {
                    if(os != null){
                        os.close();
                    }
                }catch(IOException ioe){
                    ioe.printStackTrace();
                    isTaken = false;
                }
            }

            if (isTaken){
                photoPath = randomPrefix + ".jpg";
                mPrefs.edit().putString(PREF_PHOTO_NAME, photoPath).apply();
                if (mShowPhotoView != null){
                    int pixels = (int)ImageUtil.dp2px(getActivity(), 56);
                    Bitmap bitmap = mImageUtil.decodeSampledBitmapFromFile(path.toString(), pixels , pixels);
                    mShowPhotoView.setImageBitmap(bitmap);
                }
            }
            mProgressBarContainer.setVisibility(View.INVISIBLE);
        }
    };

    public boolean isExternalStorageAvailable(){
        boolean externalStorageAvailable = Environment
                .getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        return externalStorageAvailable;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }


    @Nullable
    @SuppressWarnings("deprecation")
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = getActivity().getLayoutInflater().inflate(R.layout.photo_taken_fragment, container, false);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mTakeButton = (Button)v.findViewById(R.id.take_button);
        mShowPhotoView = (ImageView) v.findViewById(R.id.photo_taken_view);
        mAddEffectButton = (ImageButton)v.findViewById(R.id.adding_effect_button);
        mSurfaceView = (SurfaceView)v.findViewById(R.id.surfaceView);

        mTakeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkCameraHardware(getActivity())){
                    if (mCamera != null){
                        mCamera.takePicture(mShutterCallback, null, mPictureCallback);
                    }
                }else {
                    mTakeButton.setEnabled(false);
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage("You have no camera device.");
                    builder.setTitle("Notice");
                    builder.setIcon(android.R.drawable.ic_dialog_alert);
                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    builder.show();
                }
            }
        });

        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    if (mCamera != null){
                        mCamera.setPreviewDisplay(holder);
                    }
                }catch (IOException e){
                    e.printStackTrace();
                    Log.e(TAG, "Error setting up preview display", e);
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                if (mSurfaceHolder.getSurface() == null)
                    return;

                if (mCamera != null){
                    mCamera.stopPreview();

                    Camera.Parameters parameters = mCamera.getParameters();
                    List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
                    Camera.Size suitableSize = null;
                    suitableSize = getSuitableSize(sizes, width, height);
                    parameters.setPreviewSize(suitableSize.width, suitableSize.height);
                    parameters.setPictureSize(suitableSize.width, suitableSize.height);
                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);

                    mCamera.setDisplayOrientation(90);
                    mCamera.setParameters(parameters);
                    try {
                        mCamera.startPreview();
                    }catch (Exception e){
                        Log.e(TAG, "Could not start preview", e);
                        mCamera.release();
                    }
                }
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                if (mCamera != null){
                    mCamera.stopPreview();
                }
            }
        });

        mShowPhotoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String photoPath = mPrefs.getString(PREF_PHOTO_NAME, null);
                if (photoPath != null){
                    File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
                    File file = new File(directory, photoPath);
                    FragmentManager fm = getActivity().getSupportFragmentManager();
                    PhotoViewFragment viewFragment = PhotoViewFragment.newInstance(file.toString());
                    viewFragment.show(fm, "photoView");
                }
            }
        });

        mProgressBarContainer = (View)v.findViewById(R.id.progressBarContainer);
        mProgressBarContainer.setVisibility(View.INVISIBLE);

        return v;
    }

    private boolean checkCameraHardware(Context context) {
        PackageManager pm = context.getPackageManager();
        if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)
                || pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)
                || Camera.getNumberOfCameras() > 0){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        String savedPhotoName = mPrefs.getString(PREF_PHOTO_NAME, null);
        if (savedPhotoName != null){
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
            File file = new File(path, savedPhotoName);
            Log.i(TAG, "photo path: " + file);
            int pixels = (int)ImageUtil.dp2px(getActivity(), 56);
            Bitmap bitmap = mImageUtil.decodeSampledBitmapFromFile(file.toString(), pixels, pixels);
            if (mShowPhotoView != null){
                mShowPhotoView.setImageBitmap(bitmap);
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Camera will be open.");
        builder.setTitle("Notice:");
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                getActivity().finish();
            }
        });
        builder.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        mCamera = Camera.open();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mCamera != null){
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mImageUtil.cleanImageView(mShowPhotoView);
    }

    @SuppressWarnings("deprecation")
    private Camera.Size getSuitableSize(List<Camera.Size> sizes, int width, int height){
        Collections.sort(sizes, new Comparator<Camera.Size>(){
            @Override
            public int compare(Camera.Size o1, Camera.Size o2) {
                Integer area1 = o1.height * o1.width;
                Integer area2 = o2.height * o2.width;
                return area1.compareTo(area2);
            }
        });

        for (int i = 0; i < sizes.size(); i++){
            Camera.Size size = sizes.get(i);
            Log.i(TAG, size.width + "x" + size.height);
        }

        Camera.Size bestSize = null;
        int targetArea = width * height;
        for (int i = 0; i < sizes.size() - 1; i++){
            Camera.Size s1 = sizes.get(i);
            Camera.Size s2 = sizes.get(i + 1);
            if (s1.width * s2.height >= targetArea
                    && s2.height * s2.width <= targetArea) {
                bestSize = s1;
            }else if (s1.width * s1.height <= targetArea
                    && s2.height * s2.width >= targetArea){
                bestSize = s2;
            }
        }
        Log.i(TAG, "tagetSize: " + width + "x" + height);
        Log.i(TAG, "bestSize: " + bestSize.width + "x" + bestSize.height);
        return bestSize;
    }

    private Camera.Size getSuitablePictureSize(List<Camera.Size> sizes, int width, int height){
        return null;
    }
}
