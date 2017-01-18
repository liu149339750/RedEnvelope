package com.snamon.redenvelope;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.app.PendingIntent;
import android.os.Parcelable;
import android.support.annotation.Nullable;
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
    private final AtomicBoolean firstEntryBoolean = new AtomicBoolean(false);

    /**
     * 处理当前的红包position .
     */
    private int currentIndex = 0;

    private int totalCount = 0;

    /**
     * 当前红包的节点信息 .
     */
//    private List<AccessibilityNodeInfo> envelopeNodeInfos;
    @Override
    public void onInterrupt() {
    }

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
                    if (firstEntryBoolean.get()) {
                        //说明是从通知栏进入到Chat页面 初始化参数
                        firstEntryBoolean.set(false);
                        initData();
                    }
                    handNextEnvelope();
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
     * 检测红包是否已处理完成
     */
    private boolean checkHandleEnvelopeComplete() {
        return currentIndex == 0;
    }

    private void initData() {
        List<AccessibilityNodeInfo> nodeInfos = genEnvelopeNodeInfos();
        if (nodeInfos != null) {
            totalCount = nodeInfos.size();
            currentIndex = totalCount; //从最后一个红包开始处理 .
            LoggWrap.i("总共有" + totalCount + "个红包需要处理 .");
        }
    }

    @Nullable
    private List<AccessibilityNodeInfo> genEnvelopeNodeInfos() {
        AccessibilityNodeInfo accessibilityNodeInfo = getRootInActiveWindow();
        if (accessibilityNodeInfo != null) {
            // TODO: 2017/1/18 snamon 这里有一个问题，假如此红包已抢，我们也要模拟点击一次，不过不会联网，性能影响不大，是否可优化？
            List<AccessibilityNodeInfo> envelopeNodeInfos = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(
                    "com.tencent.mm:id/a48");
            accessibilityNodeInfo.recycle();
            return envelopeNodeInfos;
        }
        return null;
    }

    /**
     * 处理下一个红包 .
     */
    private void handNextEnvelope() {

        if (!checkHandleEnvelopeComplete()) {
            --currentIndex;
            LoggWrap.i("处理" + (currentIndex + 1) + "号红包 .");
            //重新获取红包节点
            List<AccessibilityNodeInfo> nodeInfos = genEnvelopeNodeInfos();
            if (nodeInfos != null) {
                if (nodeInfos.size() == totalCount) {
                    AccessibilityNodeInfo envelopeNodeInfo = nodeInfos.get(currentIndex);
                    envelopeNodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                } else {
                    //防止在拆多个红包，红包节点个数改变 . 需重新开始遍历
                    LoggWrap.i("红包节点个数已改变 .");
                    initData();
                    handNextEnvelope();
                }
            }
        }else {
            LoggWrap.i("红包处理完成 .");
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
            accessibilityNodeInfo.recycle();
            for (AccessibilityNodeInfo ani : accessibilityNodeInfos) {
                ani.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                LoggWrap.i("模拟拆红包///");
            }
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
                LoggWrap.i("关闭红包详情页面");
            } else {
                LoggWrap.e("没找到关闭按钮.");
            }
        }
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
                        firstEntryBoolean.set(true); //说明第一次点击这个人的红包界面
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
