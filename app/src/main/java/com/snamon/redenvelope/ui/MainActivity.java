package com.snamon.redenvelope.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.widget.Button;

import com.fcbox.rxbus.RxBus;
import com.snamon.redenvelope.EnvelopeAccessibilityService;
import com.snamon.redenvelope.R;
import com.snamon.redenvelope.common.util.SystemUtil;
import com.snamon.redenvelope.event.OpenStatusEvent;
import com.snamon.redenvelope.widget.BaseActivity;

import butterknife.BindString;
import butterknife.BindView;
import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * 主界面 .
 */
public class MainActivity extends BaseActivity {

    private SweetAlertDialog mSweetAlertDialog;
    @BindString(R.string.prompt_open_service)
    public String mPromptOpenService;
    @BindString(R.string.open_service_title)
    public String mOpenServiceTitle;
    @BindView(R.id.main_btn_grab)
    public Button mBtnGrab;

    private OpenStatusEvent mOpenStatusEvent;

    public static void startMe(Context context){
        Intent intent = new Intent(context,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if(false == context instanceof Activity){
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    @Override
    protected void init() {
        mOpenStatusEvent = new OpenStatusEvent(true);
        if(!SystemUtil.isAccessibilitySettingsOn(this, EnvelopeAccessibilityService.class.getCanonicalName())){
            showDialog();
        }

        //监听"抢"
        viewClick(R.id.main_btn_grab)
                .doOnNext(aVoid -> mOpenStatusEvent.isStop = !mOpenStatusEvent.isStop)
                .subscribe(aVoid -> {
                    //初始化数据
                    if(!SystemUtil.isAccessibilitySettingsOn(this, EnvelopeAccessibilityService.class.getCanonicalName())){
                        showDialog();
                    }else{
                        mBtnGrab.setText(mOpenStatusEvent.isStop?"抢":"停");
                        RxBus.getDefault().post(mOpenStatusEvent);
                    }
                });

    }

    @Override
    protected int initLayoutRes() {
        return R.layout.activity_main;
    }

    private void showDialog(){
        mSweetAlertDialog = new SweetAlertDialog(this)
                .setCustomImage(R.mipmap.dialog_bg)
                .setTitleText(mOpenServiceTitle)
                .setContentText(mPromptOpenService)
                .setConfirmText("点击开启")
                .setConfirmClickListener(sweetAlertDialog -> {
                    //跳转到辅助界面
                    Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                    startActivity(intent);
                    if (mSweetAlertDialog.isShowing()) {
                        mSweetAlertDialog.dismiss();
                    }
                });
        mSweetAlertDialog.show();
    }
}
