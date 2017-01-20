package com.snamon.redenvelope.ui.frame;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.snamon.redenvelope.R;

public abstract class EmptyFragmentActivity extends AppCompatActivity {
    protected String TAG;
    protected Activity mActivity;

    public EmptyFragmentActivity(){
        mActivity = this;
        TAG = getClass().getSimpleName();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_empty_fragment);
        Fragment fragment = getContentFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.layout_content, fragment).commit();
    }

    public abstract Fragment getContentFragment();
}
