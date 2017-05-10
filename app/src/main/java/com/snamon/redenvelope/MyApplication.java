package com.snamon.redenvelope;

import android.app.Application;

import com.snamon.redenvelope.common.util.Log;
import com.snamon.redenvelope.common.util.SystemUtil;

/**
 * @author joychine on 2017/1/20. 14 09
 * @email joychine@qq.com
 */

public class MyApplication extends Application {

    private String mWeichatVersion;
    private static final String WEICHAT_PACKAGENAME = "com.tencent.mm";
    private static final String TAG = "MyApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.setDebug(BuildConfig.DEBUG);
    }

    public void checkWeichatVersion() {
        mWeichatVersion = SystemUtil.getVersionName(this, WEICHAT_PACKAGENAME);
        if(mWeichatVersion == null) {
            Log.v(TAG,"weichat not installed");
        }
    }

    public String getWeichatVersion() {
        if(mWeichatVersion == null) {
            checkWeichatVersion();
        }
        return mWeichatVersion;
    }
}
