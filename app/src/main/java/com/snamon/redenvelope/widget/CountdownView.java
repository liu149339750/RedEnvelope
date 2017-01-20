/*
 * Copyright 2016 HiveBox.
 */

package com.snamon.redenvelope.widget;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.snamon.redenvelope.R;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * @author snamon 2016-06-25
 * 倒计时控件.
 */
public class CountdownView extends LinearLayout {

    private static final int DEFAULT_TIME = 60;
    private int totalTime, currentTime;
    private TextView tv;
    private CompositeSubscription mCompositeSubscription;
    private CountdownFinishListener mCountdownFinishListener;

    /** 是否是暂停状态. **/
    private final AtomicBoolean mPauseStatus = new AtomicBoolean(false);
    /** 是否是消毁状态 处理拦截流的发送. **/
    private final AtomicBoolean mDestoryStatus = new AtomicBoolean(false);
    private final Object mCountdownSync = new Object();
    /** 是否是开始状态 防止多次启动. **/
    private boolean isStartStatus = false;

    private int mTextSize;
    private int mTextColor;


    public CountdownView(Context context) {
        super(context);
        init(context, null);
    }

    public CountdownView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CountdownView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public void setCountdownFinishListener(CountdownFinishListener listener) {
        this.mCountdownFinishListener = listener;
    }

    /**
     * 倒时计开始 .
     */
    public void start() {
        synchronized (mCountdownSync) {
            if (isStartStatus) {
                return;
            }
            isStartStatus = true;
            mDestoryStatus.set(false);
            mCompositeSubscription.add(createCountdownObservable().subscribe(new Action1<String>() {
                @Override
                public void call(String str) {
//                    Log.v("snamon" , "业务处理...");
                    //做业务处理
                    drawTime(currentTime);
                    if (currentTime == 0 && mCountdownFinishListener != null) {
                        mCountdownFinishListener.call();
                    }

                }
            }));
        }

    }

    /**
     * 销毁倒计时控件,清除内存 .
     */
    public void destory() {
        synchronized (mCountdownSync) {
            mDestoryStatus.set(true);
            mPauseStatus.set(false);
            isStartStatus = false;
            if (mCompositeSubscription != null) {
                mCompositeSubscription.clear();
            }
        }
    }

    /**
     * 倒计时暂停 .
     */
    public void pause() {
        synchronized (mCountdownSync) {
            mPauseStatus.set(true);
        }
    }

    /**
     * 倒计时恢复 .
     */
    public void resume() {
        synchronized (mCountdownSync) {
            if (mDestoryStatus.get()) {
                return;
            }
            mPauseStatus.set(false);
            mCountdownSync.notifyAll();
        }
    }

    /**
     * 倒计时重新开始 .
     */
    public void restart() {
        synchronized (mCountdownSync) {
            currentTime = totalTime;
            resume();
        }

    }

    @SuppressWarnings("unchecked")
    private void init(Context context, final AttributeSet attrs) {
        mCompositeSubscription = new CompositeSubscription();
        int bgId;
        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CountdownView);
            bgId = ta.getResourceId(R.styleable.CountdownView_bg, android.R.color.transparent);
            totalTime = ta.getInteger(R.styleable.CountdownView_time, DEFAULT_TIME);
            ta.recycle();
        } else {
            bgId = android.R.color.transparent;
            totalTime = DEFAULT_TIME;
        }
        mTextSize = 40;
        mTextColor = Color.rgb(255, 165, 79);
        currentTime = totalTime;
        setBackgroundResource(bgId);
        setOrientation(LinearLayout.HORIZONTAL);
        tv = new TextView(context);
        tv.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        addView(tv);
    }

    /**
     * 创建一个倒计时流
     */
    private Observable<String> createCountdownObservable() {
        return Observable.just("")
                .repeatWhen(observable -> observable.flatMap((Func1<Void, Observable<?>>) aVoid -> {
                    //是否重订阅
                    synchronized (mCountdownSync) {
                        if (mPauseStatus.get()) { //暂停了
//                                        Log.v("snamon", "暂停等待通知 .");
                            try {
                                mCountdownSync.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        currentTime = currentTime - 1;
                        if (currentTime == 0 || mDestoryStatus.get()) {
                            //不重订阅
//                                        Log.v("snamon", "不重订阅");
                            return Observable.empty();
                        } else {
                            //重订阅
//                                        Log.v("snamon", "重订阅");
                            return Observable.timer(1000, TimeUnit.MILLISECONDS);
                        }
                    }
                }))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private void drawTime(int time) {
//        Log.v("snamon", "画时间 " + time);
        String timeStr = String.valueOf(time);
        int len = timeStr.length();
        timeStr = len == 1 ? "00" + timeStr : len == 2 ? "0" + timeStr : timeStr;
        SpannableString msp = new SpannableString(timeStr);
        msp.setSpan(new TypefaceSpan("monospace"), 0, timeStr.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // 字体
        msp.setSpan(new AbsoluteSizeSpan(mTextSize, true), 0, timeStr.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // 大小
        msp.setSpan(new ForegroundColorSpan(mTextColor), 0, timeStr.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);// 字体颜色
        msp.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, timeStr.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);  // 粗体
        tv.setText(msp);
    }

    /**
     * 设置倒计时时间
     */
    public void setCountTime(int time) {
        totalTime = time;
    }

    public void setTextSize(int textSize) {
        mTextSize = textSize;
    }

    public void setTextColor(@ColorInt int color) {
        mTextColor = color;
    }

    /**
     * 设置倒计时的背景图片
     */
    public void setBackGround(@NonNull Drawable drawable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            setBackground(drawable);
        } else {
            setBackgroundDrawable(drawable);
        }
    }


    public interface CountdownFinishListener {

        void call();
    }
}