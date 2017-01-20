/*
 * Copyright 2016 HiveBox.
 */

package com.snamon.redenvelope.widget;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.MenuRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.TextView;

import com.jakewharton.rxbinding.view.RxView;
import com.snamon.redenvelope.R;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import cn.pedant.SweetAlert.SweetAlertDialog;
import rx.Observable;

/**
 * Base activity.
 */
public abstract class BaseActivity extends RxAppCompatActivity {

    private View mContentView;
    @Nullable
    private Toolbar mToolbar;
    private ImageButton mNavigation;
    private SweetAlertDialog mSweetAlertDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContentView = View.inflate(this, initLayoutRes(), null);
        setContentView(mContentView);
        ButterKnife.bind(this);
        initProxy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (initMenuRes() < 0) return false;
        getMenuInflater().inflate(initMenuRes(), menu);
        return true;
    }

    /**
     * 隐藏键盘.
     */
    public void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE));
            imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    /**
     * 显示加载对话框.
     */
    public void showLoadingDialog(@NonNull String message) {
        mSweetAlertDialog.setContentText(message);
        mSweetAlertDialog.show();
    }

    /**
     * 隐藏加载对话框.
     */
    public void hideLoadingDialog() {
        if(mSweetAlertDialog!=null){
            mSweetAlertDialog.hide();
        }
    }

    /**
     * 释放加载对话框.
     */
    public void dismissLoadingDialog() {
        mSweetAlertDialog.dismiss();
    }

    public Observable<Void> viewClick(@IdRes int resId) {
        return viewClick(mContentView.findViewById(resId));
    }

    public Observable<Void> viewClick(@NonNull View view) {
        return RxView.clicks(view)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .compose(this.bindToLifecycle());
    }

    public void setTitle(@NonNull CharSequence title) {
        if (mToolbar == null) {
            throw new NullPointerException("Toolbar is null, should override method initToolbar first.");
        }
        TextView textView = (TextView) mToolbar.findViewById(R.id.toolbar_title);
        textView.setText(title);
    }


    /**
     * 隐藏Toolbar返回按钮.
     */
    protected void hideBackBtn() {
        if (mToolbar == null) {
            throw new NullPointerException("Toolbar is null, should override method initToolbar first.");
        }
        mNavigation.setVisibility(View.GONE);
    }

    /**
     * 添加fragment.
     */
    protected void addFragment(@NonNull BaseFragment fragment) {
        addFragment(fragment, fragment.getClass().getSimpleName());
    }

    /**
     * 添加fragment.
     */
    protected void addFragment(@NonNull BaseFragment fragment, @NonNull String tag) {
        addFragment(fragment, tag, false);
    }

    /**
     * 添加fragment.
     */
    protected void addFragment(@NonNull BaseFragment fragment, boolean root) {
        addFragment(fragment, fragment.getClass().getSimpleName(), root);
    }

    /**
     * 添加fragment.
     */
    protected void addFragment(@NonNull BaseFragment fragment, @NonNull String tag, boolean root) {
        int fragmentLayoutId = initFragmentContainerId();
        if (fragmentLayoutId == 0) {
            throw new NullPointerException("must invoke method initLayoutRes first.");
        }
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction()
                .replace(fragmentLayoutId, fragment, tag);
        if (!root) {
            transaction.addToBackStack(fragment.getClass().getSimpleName());
        }
        transaction.commit();
    }

    /**
     * 移除fragment.
     */
    protected void removeFragment() {
        onBackPressed();
    }

    protected abstract void init();

    @LayoutRes
    protected abstract int initLayoutRes();

    @Nullable
    protected Toolbar initToolbar() {
        return null;
    }

    @Nullable
    protected CountdownView initCountdownView() {
        return null;
    }

    @IdRes
    protected int initFragmentContainerId() {
        return 0;
    }

    /**
     * set the id of menu.
     *
     * @return if values is less then zero, and the activity will not show menu
     */
    @MenuRes
    protected int initMenuRes() {
        return -1;
    }

    private void configToolBar() {
        if (mToolbar == null) {
            return;
        }
        setSupportActionBar(mToolbar);

        mNavigation = (ImageButton) mToolbar.findViewById(R.id.iv_back);
        mNavigation.setOnClickListener(v -> onBackPressed());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayShowHomeEnabled(false); // 设置左上角图标是否可点击
            getSupportActionBar().setDisplayHomeAsUpEnabled(false); // 设置左上角图标是否有返回图标
        }

    }

    private void initLoadingDialog() {
        mSweetAlertDialog = new SweetAlertDialog(this , SweetAlertDialog.PROGRESS_TYPE);
        mSweetAlertDialog.getProgressHelper().setBarColor(R.color.colorPrimary);
        mSweetAlertDialog.setTitleText("Loading");
        mSweetAlertDialog.setCancelable(false);
    }

    @SuppressWarnings("unchecked")
    private void initProxy() {

        mToolbar = initToolbar();
        configToolBar();
        initLoadingDialog();

        init();
    }
}
