package com.snamon.redenvelope;

import android.app.Application;

import com.snamon.redenvelope.common.util.Log;

/**
 * @author joychine on 2017/1/20. 14 09
 * @email joychine@qq.com
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Log.setDebug(BuildConfig.DEBUG);
    }
}
