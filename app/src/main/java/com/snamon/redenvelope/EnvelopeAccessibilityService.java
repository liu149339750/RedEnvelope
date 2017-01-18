package com.snamon.redenvelope;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.app.PendingIntent;
import android.os.Parcelable;
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
                handleNotification(event);
                break;
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                String clsName = event.getClassName().toString();
                LoggWrap.i("窗口发生变法的事件类 ： " + clsName);
                if("com.tencent.mm.ui.LauncherUI".equals(clsName)){
                    //点击红包
                    clickEnvelope();
                }
                if("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI".equals(clsName)){
                    //打开红包
                    openEnvelope();
                }
                if("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI".equals(clsName)){
                    envelopeDetail(event);
                }
                mockClick();
                break;

        }
    }

    private void envelopeDetail(AccessibilityEvent event) {

        AccessibilityNodeInfo accessibilityNodeInfo = getRootInActiveWindow();
        //获取消息
        List<CharSequence> eventText= event.getText();
        for (CharSequence cs: eventText){
            LoggWrap.i("红包详情页面文本内容：" + cs);
        }
        //点击返回
        if(accessibilityNodeInfo!=null){
            List<AccessibilityNodeInfo> accessibilityNodeInfos =
                    accessibilityNodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/gs");

            LoggWrap.i("返回到微信首页：");
            accessibilityNodeInfo.recycle();
            for (AccessibilityNodeInfo ani : accessibilityNodeInfos) {
                ani.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                LoggWrap.i("关闭红包详情页面");
            }
        }

    }

    private void clickEnvelope() {

        AccessibilityNodeInfo accessibilityNodeInfo = getRootInActiveWindow();
        if (accessibilityNodeInfo != null) {
            List<AccessibilityNodeInfo> accessibilityNodeInfos =
                    accessibilityNodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/a48");
            LoggWrap.i("打开红包数：" + accessibilityNodeInfos.size());
            accessibilityNodeInfo.recycle();
            for (AccessibilityNodeInfo ani : accessibilityNodeInfos) {
                ani.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                LoggWrap.i("模拟打开红包///");
            }
        }

    }

    private void openEnvelope(){
        AccessibilityNodeInfo accessibilityNodeInfo = getRootInActiveWindow();
        if (accessibilityNodeInfo != null) {
            List<AccessibilityNodeInfo> accessibilityNodeInfos =
                    accessibilityNodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/be_");
            LoggWrap.i("拆红包数：" + accessibilityNodeInfos.size());
            accessibilityNodeInfo.recycle();
            for (AccessibilityNodeInfo ani : accessibilityNodeInfos) {
                ani.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                LoggWrap.i("模拟拆红包///");
            }
        }
    }

    private void mockClick() {
        AccessibilityNodeInfo accessibilityNodeInfo = getRootInActiveWindow();
        if (accessibilityNodeInfo != null) {
            List<AccessibilityNodeInfo> accessibilityNodeInfos =
                    accessibilityNodeInfo.findAccessibilityNodeInfosByViewId("com.snamon.redenvelope:id/btn_mock_wx");
            accessibilityNodeInfo.recycle();
            for (AccessibilityNodeInfo ani : accessibilityNodeInfos) {
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
            if(checkWxEnvelope(title) || checkWxEnvelope(text) || checkWxEnvelope(sub_text) || checkWxEnvelope(info)){
                LoggWrap.i("收到微信通知,是微信红包.");
                //说明是红包
                PendingIntent contentIntent = notification.contentIntent;
                if (contentIntent != null) {
                    try {
                        contentIntent.send();
                    } catch (PendingIntent.CanceledException e) {
                        e.printStackTrace();
                    }
                }
            }else{
                LoggWrap.i("收到微信通知,但不是微信红包.");
            }
        }
    }

    private boolean checkWxEnvelope(CharSequence content){
        return content != null && content.toString().contains("[微信红包]");
    }


}
