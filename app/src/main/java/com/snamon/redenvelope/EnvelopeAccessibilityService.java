package com.snamon.redenvelope;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.app.PendingIntent;
import android.os.Parcelable;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * .
 */

public class EnvelopeAccessibilityService extends AccessibilityService {

    /**
     * 返回聊天界面控制 .
     */
    private final AtomicBoolean backChatBoolean = new AtomicBoolean(false);

    /**
     * 处理当前的红包position .
     */
    private int currentPosition = -1;

    /**
     * 当前红包的节点信息 .
     */
    private List<AccessibilityNodeInfo> envelopeNodeInfos;

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
                if ("com.tencent.mm.ui.LauncherUI".equals(clsName)) {
                    //点击红包
                    if (!backChatBoolean.get()) {
                        initEnvelopeNodeIfo();
                    }
                }
                if ("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI".equals(clsName)) {
                    //打开红包
                    moneyReceiveClick();
                }
                if ("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI".equals(clsName)) {
                    moneyDetailClick();
                }
                break;

        }
    }

    /**
     * 模拟红包详情 .
     */
    private void moneyDetailClick() {

        AccessibilityNodeInfo accessibilityNodeInfo = getRootInActiveWindow();

        if (accessibilityNodeInfo != null) {
            //获取消息
            List<AccessibilityNodeInfo> moneyNodeInfos = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(
                    "com.tencent.mm:id/bbe");
            if (moneyNodeInfos != null && moneyNodeInfos.size() != 0) {
                CharSequence moneyCharSequence = moneyNodeInfos.get(0).getText();
                if (moneyCharSequence != null) {
                    LoggWrap.i("恭喜你，抢到" + moneyCharSequence.toString() + "元！");
                }
            }
            //点击返回
            List<AccessibilityNodeInfo> backNodeInfos = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(
                    "com.tencent.mm:id/gr");
            accessibilityNodeInfo.recycle();
            if (backNodeInfos != null && backNodeInfos.size() != 0) {
                backNodeInfos.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                backChatBoolean.set(true);
                LoggWrap.i("关闭红包详情页面");
                handNextEnvelope();
            } else {
                LoggWrap.e("没找到关闭按钮.");
            }
        }


    }


    /**
     * 初始化红包节点信息 .
     */
    private void initEnvelopeNodeIfo() {

        AccessibilityNodeInfo accessibilityNodeInfo = getRootInActiveWindow();
        if (accessibilityNodeInfo != null) {
            // TODO: 2017/1/18 snamon 这里有一个问题，假如此红包已抢，我们也要模拟点击一次，不过不会联网，性能影响不大，是否可优化？
            envelopeNodeInfos = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/a48");
            accessibilityNodeInfo.recycle();
            if (envelopeNodeInfos != null) {
                LoggWrap.i("待处理红包数：" + envelopeNodeInfos.size());
            }
            handNextEnvelope();
        }

    }

    /**
     * 处理下一个红包 .
     */
    private void handNextEnvelope() {
        currentPosition++;
        if (envelopeNodeInfos != null && currentPosition < envelopeNodeInfos.size()) {
            LoggWrap.i("处理第" +currentPosition +"个红包 .");
            AccessibilityNodeInfo envelopeNodeInfo = envelopeNodeInfos.get(currentPosition);
            envelopeNodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        } else {
            //数据复位
            currentPosition = -1;
            envelopeNodeInfos = null;
            backChatBoolean.set(false);
        }

    }

    /**
     * 模拟点击"开"红包 .
     */
    private void moneyReceiveClick() {
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
            if (checkWxEnvelope(title) || checkWxEnvelope(text) || checkWxEnvelope(sub_text) || checkWxEnvelope(info)) {
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
            } else {
                LoggWrap.i("收到微信通知,但不是微信红包.");
            }
        }
    }

    private boolean checkWxEnvelope(CharSequence content) {
        return content != null && content.toString().contains("[微信红包]");
    }


}
