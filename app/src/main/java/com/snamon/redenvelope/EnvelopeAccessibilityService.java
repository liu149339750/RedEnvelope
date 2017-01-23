package com.snamon.redenvelope;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import com.fcbox.rxbus.RxBus;
import com.fcbox.rxbus.Subscribe;
import com.snamon.redenvelope.common.util.Log;
import com.snamon.redenvelope.common.util.SystemUtil;
import com.snamon.redenvelope.event.OpenStatusEvent;
import com.snamon.redenvelope.ui.MainActivity;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.System.currentTimeMillis;

/**
 * 自动抢红包核心类 .
 */
public class EnvelopeAccessibilityService extends AccessibilityService {
    private static String TAG = EnvelopeAccessibilityService.class.getSimpleName();
    private Context mContext;
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
     * 控制是否停止抢红包 .
     */
    private AtomicBoolean stopBoolean = new AtomicBoolean(true);
    private long lastNotifytime;
    private String lastWxActivityName;

    private MediaPlayer mp;

    public EnvelopeAccessibilityService(){
        mContext = this;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("onCreate");
        mContext = this;
        mp = MediaPlayer.create(mContext.getApplicationContext(), R.raw.redsound);
        RxBus.getDefault().register(mContext);
    }

    @Override
    protected void onServiceConnected() {
        //服务连接回调
//        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
//        manager.moveTaskToFront(mContext.getApplicationContext().getTaskId(),0) ;
        if (!MainActivity.class.getCanonicalName().equals(SystemUtil.getTopActivityName(mContext))) {
            Log.i("MainActivity不处于栈顶，调到栈顶 .");
            MainActivity.startMe(mContext);
        }
    }

    @Override
    public void onInterrupt() {
        Toast.makeText(mContext, "中断抢红包服务", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (stopBoolean.get()) {
            Log.e(TAG,"clsName:"+event.getClassName());
            return;
        }
        int eventType = event.getEventType();
        switch (eventType) {
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                handleNotification(event);//通知改变(Toast也会调用这个方法)
                break;
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                String clsName = event.getClassName().toString();
                lastWxActivityName = clsName;
                Log.i("窗口发生变化的事件类 ： " + clsName);
                if (ACTIVTY_NAME_WX_CHAT.equals(clsName)) {
                    lastNotifytime = currentTimeMillis();
                    if (firstEntryBoolean.get()) {
                        //说明是从通知栏进入到Chat页面 初始化参数
                        firstEntryBoolean.set(false);
                        mockClickEnvelope();
                    }
                }
                if (ACTIVITY_NAME_WX_ENVELOPE.equals(clsName)) {
                    mockOpenEnvelope();//打开红包
                }
                if (ACTIVITY_NAME_WX_REDPACK_DETAIL.equals(clsName)) {
                    mockEnvelopDetailClick();
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
//                    Toast.makeText(mContext, "间隔太短，说明是从通知栏来的导致listvie变化", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!isNewRedpackMsg()) {
                    return;
                }
                playSound();
                mockClickEnvelope();
                break;
        }
    }

    @Subscribe
    public void onEvent(OpenStatusEvent event) {
        Log.i(event.isStop ? "停止抢红包" : "开启抢红包");
        stopBoolean.set(event.isStop);
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
                Log.i("关闭提示实名认证的页面");
            }
            accessibilityNodeInfo.recycle();
        }
    }

    @Nullable
    private List<AccessibilityNodeInfo> genEnvelopeNodeInfos() {
        AccessibilityNodeInfo accessibilityNodeInfo = getRootInActiveWindow();
        if (accessibilityNodeInfo != null) {
            List<AccessibilityNodeInfo> envelopeNodeInfos = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(ID_CHAT_REDPACK_VIEW);
            accessibilityNodeInfo.recycle();
            return envelopeNodeInfos;
        }
        return null;
    }

    /**
     * 模拟点击 对话列表中最后一个红包(现在不管有多少红包，总之只会处理最后一个红包，因为无法判断某个红包是否被点击过)
     */
    private void mockClickEnvelope() {
        List<AccessibilityNodeInfo> nodeInfos = genEnvelopeNodeInfos();
        if (nodeInfos != null && nodeInfos.size() > 0) {
            nodeInfos.get(nodeInfos.size() - 1).performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }
    }

    /**
     * 模拟点击"开"红包信封 .
     */
    private void mockOpenEnvelope() {
        AccessibilityNodeInfo accessibilityNodeInfo = getRootInActiveWindow();
        if (accessibilityNodeInfo != null) {
            List<AccessibilityNodeInfo> accessibilityNodeInfos = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(ID_OPEN_ENVELOPE_VIEW);
            for (AccessibilityNodeInfo ani : accessibilityNodeInfos) {
                ani.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
            accessibilityNodeInfo.recycle();
        }
    }

    /**
     * 模拟红包详情  点击返回
     */
    private void mockEnvelopDetailClick() {
        AccessibilityNodeInfo accessibilityNodeInfo = getRootInActiveWindow();
        if (accessibilityNodeInfo != null) {
            //获取消息
            List<AccessibilityNodeInfo> moneyNodeInfos = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/bbe");
            if (moneyNodeInfos != null && moneyNodeInfos.size() != 0) {
                CharSequence moneyCharSequence = moneyNodeInfos.get(0).getText();
                if (moneyCharSequence != null) {
                    Log.i("恭喜你，抢到" + moneyCharSequence.toString() + "元！");
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
                Log.i("关闭红包详情页面");
            }
            accessibilityNodeInfo.recycle();
        }
    }

    /**
     * 监听通知栏消息，并判断是否是 微信红包消息
     */
    private void handleNotification(AccessibilityEvent event) {
        Parcelable data = event.getParcelableData();
        if (data instanceof Notification) {
            Notification notification = (Notification) data;
            CharSequence title = notification.extras.getCharSequence(Notification.EXTRA_TITLE);
            CharSequence text = notification.extras.getCharSequence(Notification.EXTRA_TEXT);
            CharSequence sub_text = notification.extras.getCharSequence(Notification.EXTRA_SUB_TEXT);
            CharSequence info = notification.extras.getCharSequence(Notification.EXTRA_INFO_TEXT);
            if (checkWxEnvelope(title) || checkWxEnvelope(text) || checkWxEnvelope(sub_text) || checkWxEnvelope(info)) {
                //说明是红包
                PendingIntent contentIntent = notification.contentIntent;
                if (contentIntent != null) {
                    try {
                        playSound();
                        firstEntryBoolean.set(true); //说明第一次点击这个人的红包界面
                        contentIntent.send();
                    } catch (PendingIntent.CanceledException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                Log.i("收到微信通知,但不是微信红包.");
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
        if(rootNodeinfo == null){
            return false;
        }
        List<AccessibilityNodeInfo> nodeinfos = rootNodeinfo.findAccessibilityNodeInfosByViewId(ID_CHAT_LISTVIEW); //获取聊天页面的ListView
        if (nodeinfos.isEmpty()) {
            rootNodeinfo.recycle(); //记得回收
            return false;
        }
        AccessibilityNodeInfo listviewNodeInfo = nodeinfos.get(0);
        AccessibilityNodeInfo childNodeInfo = listviewNodeInfo.getChild(listviewNodeInfo.getChildCount() - 1);
        if(childNodeInfo == null){
            rootNodeinfo.recycle();
            return false;
        }
        List<AccessibilityNodeInfo> redpackNodeInfos = childNodeInfo.findAccessibilityNodeInfosByViewId(ID_CHAT_REDPACK_VIEW); //获取最后一个聊天消息Item里面是否有红包节点
        if (redpackNodeInfos.isEmpty()) { //不是红包消息
            rootNodeinfo.recycle();
            return false;
        }
        rootNodeinfo.recycle();
        return true;
    }

    /**
     * 播放抢红包声音.
     */
    private void playSound() {
        mp.reset();
        mp = MediaPlayer.create(mContext.getApplicationContext(), R.raw.redsound);
        mp.start();

    }
}
