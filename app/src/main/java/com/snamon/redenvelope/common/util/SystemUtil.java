package com.snamon.redenvelope.common.util;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityManager;

import java.util.List;

/**
 * 系统工具util .
 */
public class SystemUtil {

    public static boolean hasAccessibilityService(Context context, @NonNull String serviceName) {
        AccessibilityManager am = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> accessibilityServiceInfos = am.getInstalledAccessibilityServiceList();
        for (AccessibilityServiceInfo accessibilityServiceInfo : accessibilityServiceInfos) {
            Log.i(Log.TAG, "isEnableAccessibilityService id : " + accessibilityServiceInfo.getId());
            if (serviceName.equals(accessibilityServiceInfo.getId())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isAccessibilitySettingsOn(Context mContext, String clsName) {
        String service = mContext.getPackageName() + "/" + clsName;
        int accessibilityEnabled = 0;
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                    mContext.getApplicationContext().getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
//            LoggWrap.i("accessibilityEnabled = " + accessibilityEnabled);
        } catch (Settings.SettingNotFoundException e) {
            Log.e("Error finding setting, default accessibility to not found: " + e.getMessage());
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled == 1) {
//            LoggWrap.i("***ACCESSIBILITY IS ENABLED*** -----------------");
            String settingValue = Settings.Secure.getString(
                    mContext.getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();

//                    Log.i("-------------- > accessibilityService :: " + accessibilityService + " " + service);
                    if (accessibilityService.equalsIgnoreCase(service)) {
//                        LoggWrap.i("We've found the correct setting - accessibility is switched on!");
                        return true;
                    }
                }
            }
        } else {
            Log.i(Log.TAG, "***ACCESSIBILITY IS DISABLED***");
        }

        return false;
    }


    /**
     * 获取栈顶Activity
     * @deprecated  This method was deprecated in API level 21.
     */
    public static String getTopActivityName(Context context) {
        String topActivityName = null;
        ActivityManager activityManager =
                (ActivityManager) (context.getSystemService(android.content.Context.ACTIVITY_SERVICE));
        List<ActivityManager.RunningTaskInfo> runningTaskInfos = activityManager.getRunningTasks(1);
        if (runningTaskInfos != null) {
            ComponentName f = runningTaskInfos.get(0).topActivity;
            String topActivityClassName = f.getClassName();
            String temp[] = topActivityClassName.split("\\.");
            //栈顶Activity的名称
            topActivityName = temp[temp.length - 1];
//            int index=topActivityClassName.lastIndexOf(".");
//            //栈顶Activity所属进程的名称
//            processName=topActivityClassName.substring(0, index);
//            System.out.println("---->topActivityName="+topActivityName+",processName="+processName);

        }
        return topActivityName;
    }

}
