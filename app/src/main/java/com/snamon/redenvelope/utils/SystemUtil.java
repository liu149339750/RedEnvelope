package com.snamon.redenvelope.utils;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityManager;

import com.snamon.redenvelope.ui.LoggWrap;

import java.util.List;

/**
 * .
 */

public class SystemUtil {

    public static boolean hasAccessibilityService(Context context, @NonNull String serviceName) {
        AccessibilityManager am = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> accessibilityServiceInfos = am.getInstalledAccessibilityServiceList();
        for (AccessibilityServiceInfo accessibilityServiceInfo : accessibilityServiceInfos) {
            LoggWrap.i("isEnableAccessibilityService id : " + accessibilityServiceInfo.getId());
            if (serviceName.equals(accessibilityServiceInfo.getId())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isAccessibilitySettingsOn(Context mContext, String service) {
        int accessibilityEnabled = 0;
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                    mContext.getApplicationContext().getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
            LoggWrap.i("accessibilityEnabled = " + accessibilityEnabled);
        } catch (Settings.SettingNotFoundException e) {
            LoggWrap.e("Error finding setting, default accessibility to not found: " + e.getMessage());
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled == 1) {
            LoggWrap.i("***ACCESSIBILITY IS ENABLED*** -----------------");
            String settingValue = Settings.Secure.getString(
                    mContext.getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();

                    LoggWrap.i("-------------- > accessibilityService :: " + accessibilityService + " " + service);
                    if (accessibilityService.equalsIgnoreCase(service)) {
                        LoggWrap.i("We've found the correct setting - accessibility is switched on!");
                        return true;
                    }
                }
            }
        } else {
            LoggWrap.i("***ACCESSIBILITY IS DISABLED***");
        }

        return false;
    }

    public static String getTopActivityClassName(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTasks = manager.getRunningTasks(1);
        ActivityManager.RunningTaskInfo cinfo = runningTasks.get(0);
        ComponentName component = cinfo.topActivity;
        Log.e("current activity is ", component.getClassName());
        return component.getClassName();
    }


}
