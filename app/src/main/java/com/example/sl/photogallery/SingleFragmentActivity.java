package com.example.sl.photogallery;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by sl on 2016/10/31.
 */
//create a common abstract activity
public abstract class SingleFragmentActivity extends AppCompatActivity {

    protected abstract Fragment createFragment();

    protected int getLayoutResId(){
        return R.layout.activity_fragment;
    }

    protected abstract String getTag();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_fragment);
        setContentView(getLayoutResId());
        addFragment( );
    }

    public void addFragment() {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        Fragment targetFragment = fm.findFragmentById(R.id.fragmentContainer);
        if(targetFragment == null){
            targetFragment = createFragment();
            fm.beginTransaction().add(R.id.fragmentContainer, targetFragment, getTag()).commit();
        }
    }
}
