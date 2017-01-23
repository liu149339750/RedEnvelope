package com.snamon.redenvelope.widget;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jakewharton.rxbinding.view.RxView;
import com.snamon.redenvelope.R;
import com.trello.rxlifecycle.components.support.RxFragment;

import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import cn.pedant.SweetAlert.SweetAlertDialog;
import rx.Observable;

public abstract class BaseFragment extends RxFragment {
    protected Context mContext;
    private View mContentView;
    private SweetAlertDialog mSweetAlertDialog;
    private Unbinder mUnbinder;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 抽取静态方法newInstance()传递进来的arguments
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContentView = inflater.inflate(genLayoutRes(), container, false);
        mUnbinder = ButterKnife.bind(this , mContentView);
        initProxy(savedInstanceState);
        return mContentView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mUnbinder.unbind();
        mContext = null;
    }

    public Observable<Void> viewClick(@IdRes int resId) {
        return viewClick(mContentView.findViewById(resId));
    }

    public Observable<Void> viewClick(@NonNull View view) {
        return RxView.clicks(view)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .compose(this.bindToLifecycle());
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
        mSweetAlertDialog.hide();
    }

    /**
     * 释放加载对话框.
     */
    public void dismissLoadingDialog() {
        mSweetAlertDialog.dismiss();
    }

    protected abstract void init(View view, Bundle savedInstanceState);

    @LayoutRes
    protected abstract int genLayoutRes();


    @SuppressWarnings("unchecked")
    private void initProxy(Bundle savedInstanceState) {

        initLoadingDialog();
        init(mContentView, savedInstanceState);
    }

    private void initLoadingDialog() {
        mSweetAlertDialog = new SweetAlertDialog(mContext , SweetAlertDialog.PROGRESS_TYPE);
        mSweetAlertDialog.getProgressHelper().setBarColor(R.color.colorPrimary);
        mSweetAlertDialog.setTitleText("Loading");
        mSweetAlertDialog.setCancelable(false);
    }
}
