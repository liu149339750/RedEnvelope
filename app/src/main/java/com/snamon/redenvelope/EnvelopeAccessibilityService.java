package com.snamon.redenvelope;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.app.PendingIntent;
import android.os.Parcelable;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

/**
 * .
 */

public class EnvelopeAccessibilityService extends AccessibilityService {

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        int eventType = event.getEventType();
        switch (eventType) {
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                //通知改变(Toast也会调用这个方法)
                Log.v("snamon", "EnvelopeAccessibilityService noticaton .");
                handleNotification(event);
                break;
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                LoggWrap.i("TYPE_WINDOW_STATE_CHANGED");
                String clsName = event.getClassName().toString();
                LoggWrap.i(clsName);
                mockClick();
                break;

            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                LoggWrap.i("TYPE_WINDOW_CONTENT_CHANGED");
                break;

        }
    }

    private void mockClick() {
        AccessibilityNodeInfo accessibilityNodeInfo = getRootInActiveWindow();
        if(accessibilityNodeInfo!=null){
            List<AccessibilityNodeInfo> accessibilityNodeInfos = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId("com.snamon.redenvelope:id/btn_mock_wx");
            accessibilityNodeInfo.recycle();
            for (AccessibilityNodeInfo ani:accessibilityNodeInfos){
                ani.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                LoggWrap.i("模拟点击了///");
            }
        }
    }

    @Override
    public void onInterrupt() {

    }

    private void handleNotification(AccessibilityEvent event) {
        Parcelable data = event.getParcelableData();
        if (data instanceof Notification) {
            Notification notification = (Notification) data;
            CharSequence title = notification.extras.getCharSequence(Notification.EXTRA_TITLE);
            CharSequence text = notification.extras.getCharSequence(Notification.EXTRA_TEXT);
            CharSequence sub_text = notification.extras.getCharSequence(Notification.EXTRA_SUB_TEXT);
            CharSequence info = notification.extras.getCharSequence(Notification.EXTRA_INFO_TEXT);
            // TODO: 2017/1/17 snamon 检测值

            PendingIntent contentIntent = notification.contentIntent;
            if(contentIntent !=null){
                try {
                    contentIntent.send();
                } catch (PendingIntent.CanceledException e) {
                    e.printStackTrace();
                }
            }

        }

    }


}
