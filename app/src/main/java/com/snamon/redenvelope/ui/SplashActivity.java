package com.snamon.redenvelope.ui;

import android.content.Intent;
import android.os.SystemClock;

import com.snamon.redenvelope.EnvelopeGlobal;
import com.snamon.redenvelope.R;
import com.snamon.redenvelope.common.util.Log;
import com.snamon.redenvelope.widget.BaseActivity;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * 闪屏界面 初始化界面用 .
 */
public class SplashActivity extends BaseActivity {

    @Override
    protected void init() {

        Observable.just("").compose(this.bindToLifecycle())
                .map(aBoolean -> {
                    //初始化全局变量 ，application初始化会造成黑屏现象
                    EnvelopeGlobal.init(SplashActivity.this);
                    boolean firstAccess = EnvelopeGlobal.getSp().getFirstAccess();
                    if (!firstAccess) {
                        // TODO: 2017/1/19 snamon 这里延时2秒 可以加广告.
                        SystemClock.sleep(2000);
                    }
                    return firstAccess;
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aBoolean -> {
                    if (aBoolean) {
                        //进入引导
                        Log.i("进入引导页面 .");
                        UserGuideActivity.startMe(this);
                    } else {
                        Log.i("进入主面 .");
                        startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    }
                    finish();
                });

    }

    @Override
    protected int initLayoutRes() {
        return R.layout.activity_splash;
    }
}
