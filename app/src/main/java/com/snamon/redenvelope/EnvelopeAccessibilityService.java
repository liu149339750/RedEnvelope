package com.snamon.redenvelope;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.app.PendingIntent;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import com.snamon.redenvelope.ui.LoggWrap;
import com.snamon.redenvelope.utils.Log;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.System.currentTimeMillis;

/**
 * .6.5.3
 */

public class EnvelopeAccessibilityService extends AccessibilityService {
    private static String TAG = EnvelopeAccessibilityService.class.getSimpleName();
    /**
     * 聊天页面的ListView资源id
     */
    public static final String ID_CHAT_LISTVIEW = "com.tencent.mm:id/a1d";
    /**
     * 红包节点资源id
     */
    public static final String ID_CHAT_REDPACK_VIEW = "com.tencent.mm:id/a48";
    /**
     * 红包信封窗口模样的页面 开按钮
     */
    static final String ID_OPEN_ENVELOPE_VIEW = "com.tencent.mm:id/be_";
    /**
     * 取消实名认证 按钮id
     */
    static final String ID_CANCEL_SMRZ_VIEW = "com.tencent.mm:id/a_x";
    /**
     * 微信的包名
     */
    static final String WECHAT_PACKAGENAME = "com.tencent.mm";
    /**
     * 红包消息的关键字
     */
    static final String KEY_TEXT_REDPACK_ = "[微信红包]";
    /**
     * 微信聊天页面,或者是微信主页面
     */
    static final String ACTIVTY_NAME_WX_CHAT = "com.tencent.mm.ui.LauncherUI";
    /**
     * 微信信封页面 窗口
     */
    static final String ACTIVITY_NAME_WX_ENVELOPE = "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI";
    /**
     * 微信红包详情页
     */
    static final String ACTIVITY_NAME_WX_REDPACK_DETAIL = "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI";
    /**
     * t微信提示实名认证页面
     */
    static final String ACTIVITY_NAME_WX_NEED_SMRZ_TIP = "com.tencent.mm.ui.base.h";

    /**
     * 返回聊天界面控制 .
     */
    private final AtomicBoolean firstEntryBoolean = new AtomicBoolean(false);

    /**
     * 处理当前的红包position .
     */
    private int currentIndex = 0;
    private int totalCount = 0;
    private long lastNotifytime;
    private String lastWxActivityName;

    @Override
    public void onInterrupt() {
        Toast.makeText(this, "中断抢红包服务", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Toast.makeText(this, "连接抢红包服务", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        int eventType = event.getEventType();
//        Log.e(TAG, "accessibiity_event来了。。：" + event.getEventType() + ",clsname:" + event.getClassName());
        switch (eventType) {
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                //通知改变(Toast也会调用这个方法)
                handleNotification(event);
                break;
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                String clsName = event.getClassName().toString();
                lastWxActivityName = clsName;
                LoggWrap.i("窗口发生变化的事件类 ： " + clsName);
                if (ACTIVTY_NAME_WX_CHAT.equals(clsName)) {
                    lastNotifytime = currentTimeMillis();
                    if (firstEntryBoolean.get()) {
                        //说明是从通知栏进入到Chat页面 初始化参数
                        firstEntryBoolean.set(false);
                        initData();
                    }
                    handNextEnvelope();
                }
                if (ACTIVITY_NAME_WX_ENVELOPE.equals(clsName)) {
                    //打开红包
                    moneyReceiveClick();
                }
                if (ACTIVITY_NAME_WX_REDPACK_DETAIL.equals(clsName)) {
                    moneyDetailClick();
                }
                if (ACTIVITY_NAME_WX_NEED_SMRZ_TIP.equals(clsName)) {
                    cloaseActivity(ID_CANCEL_SMRZ_VIEW);
                }
                break;
            case AccessibilityEvent.TYPE_VIEW_SCROLLED: //android.widget.ListView 的变化
                if (!ACTIVTY_NAME_WX_CHAT.equals(lastWxActivityName)) {
                    Log.e(TAG, "非聊天页面。。。");
                    return;
                }
                Log.e(TAG, "listvie内容变化");
                long duration = System.currentTimeMillis() - lastNotifytime;
                if (duration < 1000) {
                    lastNotifytime += duration;
                    Toast.makeText(this, "间隔太短，说明是从通知栏来的导致listvie变化", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!isNewRedpackMsg()) {
                    return;
                }
                initData();
                handNextEnvelope();
                break;
        }
    }

    /**
     * 关闭页面
     */
    private void cloaseActivity(String res_id) {
        AccessibilityNodeInfo accessibilityNodeInfo = getRootInActiveWindow();
        if (accessibilityNodeInfo != null) {
            List<AccessibilityNodeInfo> cancel_btns = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(res_id);
            if (!cancel_btns.isEmpty()) {
                cancel_btns.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                LoggWrap.i("关闭提示实名认证的页面");
            }
            accessibilityNodeInfo.recycle();
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
        } else {
            Log.e(TAG, "微信版本太低，请升级微信至6.3.27以上");
        }
    }

    @Nullable
    private List<AccessibilityNodeInfo> genEnvelopeNodeInfos() {
        AccessibilityNodeInfo accessibilityNodeInfo = getRootInActiveWindow();
        if (accessibilityNodeInfo != null) {
            // TODO: 2017/1/18 snamon 这里有一个问题，假如此红包已抢，我们也要模拟点击一次，不过不会联网，性能影响不大，是否可优化？
            List<AccessibilityNodeInfo> envelopeNodeInfos = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(ID_CHAT_REDPACK_VIEW);
            if (envelopeNodeInfos == null || envelopeNodeInfos.size() == 0) {
                Log.e(TAG, "非最新微信版本，尝试获取ver6.3.27版本的 红包借点集合");
                envelopeNodeInfos = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/a3p"); //ver 6.3.27
            }
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
            for (AccessibilityNodeInfo nodeinfo : nodeInfos) {
                Log.e(TAG, "nodeinfo.isinvalid:" + nodeinfo);
            }
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
        } else {
            LoggWrap.i("红包处理完成 .");
        }
    }

    /**
     * 模拟点击"开"红包 .
     */
    private void moneyReceiveClick() {
        AccessibilityNodeInfo accessibilityNodeInfo = getRootInActiveWindow();
        if (accessibilityNodeInfo != null) {
            List<AccessibilityNodeInfo> accessibilityNodeInfos = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(ID_OPEN_ENVELOPE_VIEW);
            if (accessibilityNodeInfos == null || accessibilityNodeInfos.size() == 0) {
                accessibilityNodeInfos = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/bdg"); //ver 6.3.27
            }
            for (AccessibilityNodeInfo ani : accessibilityNodeInfos) {
                ani.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                LoggWrap.i("模拟拆红包///");
            }
            accessibilityNodeInfo.recycle();
        }
    }

    /**
     * 模拟红包详情 .
     */
    private void moneyDetailClick() {
        AccessibilityNodeInfo accessibilityNodeInfo = getRootInActiveWindow();
        if (accessibilityNodeInfo != null) {
            //获取消息
            List<AccessibilityNodeInfo> moneyNodeInfos = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/bbe");
            if (moneyNodeInfos != null && moneyNodeInfos.size() != 0) {
                CharSequence moneyCharSequence = moneyNodeInfos.get(0).getText();
                if (moneyCharSequence != null) {
                    LoggWrap.i("恭喜你，抢到" + moneyCharSequence.toString() + "元！");
                }
            }
            //点击返回
            List<AccessibilityNodeInfo> backNodeInfos = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/gr");
            if (backNodeInfos == null || backNodeInfos.size() == 0) {
                backNodeInfos = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/ft");
                Log.e(TAG, "非最新微信版本，尝试获取ver6.3.27版本的 关闭按钮，获得按钮backNodeInfos.size：" + backNodeInfos.size());
            }
            if (!backNodeInfos.isEmpty()) {
                backNodeInfos.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                LoggWrap.i("关闭红包详情页面");
            }
            accessibilityNodeInfo.recycle();
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
        return content != null && content.toString().contains(KEY_TEXT_REDPACK_);
    }


    /**
     * 来的新消息是否是红包消息
     */
    public boolean isNewRedpackMsg() {
        AccessibilityNodeInfo rootNodeinfo = getRootInActiveWindow();
        if (rootNodeinfo != null) {
            List<AccessibilityNodeInfo> nodeinfos = rootNodeinfo.findAccessibilityNodeInfosByViewId(ID_CHAT_LISTVIEW); //获取聊天页面的ListView
            if (nodeinfos.isEmpty()) {
                return false;
            }
            AccessibilityNodeInfo listviewNodeInfo = nodeinfos.get(0);
            AccessibilityNodeInfo childNodeInfo = listviewNodeInfo.getChild(listviewNodeInfo.getChildCount() - 1);
            List<AccessibilityNodeInfo> redpackNodeInfos = childNodeInfo.findAccessibilityNodeInfosByViewId(ID_CHAT_REDPACK_VIEW); //获取最后一个聊天消息Item里面是否有红包节点
            if (redpackNodeInfos.isEmpty()) { //不是红包消息
                return false;
            }
        }
        return true;
    }
}
