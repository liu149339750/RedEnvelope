package com.snamon.redenvelope.ui.frame;

import android.support.v4.app.Fragment;

import com.snamon.redenvelope.R;
import com.snamon.redenvelope.widget.BaseActivity;

public abstract class EmptyFragmentActivity extends BaseActivity {
    protected String TAG;

    public EmptyFragmentActivity(){
        TAG = getClass().getSimpleName();
    }

    @Override
    protected void init() {
        Fragment fragment = getContentFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.layout_content, fragment).commit();
    }

    @Override
    protected int initLayoutRes() {
        return R.layout.activity_empty_fragment;
    }

    public abstract Fragment getContentFragment();
}
