package com.snamon.redenvelope.utils;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.util.Log;

import java.util.List;

/**
 * @author joychine on 2017/1/19. 13 38
 * @email joychine@qq.com
 */

public class SystemUitls {

    public static String getTopActivityClassName(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTasks = manager.getRunningTasks(1);
        ActivityManager.RunningTaskInfo cinfo = runningTasks.get(0);
        ComponentName component = cinfo.topActivity;
        Log.e("current activity is ", component.getClassName());
        return component.getClassName();
    }
}
