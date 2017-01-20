package com.snamon.redenvelope.ui;

import android.content.Intent;
import android.provider.Settings;

import com.snamon.redenvelope.R;
import com.snamon.redenvelope.widget.BaseActivity;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class TestActivity extends BaseActivity {

    private SweetAlertDialog mSweetAlertDialog;
    @Override
    protected void init() {
        viewClick(R.id.test_btn_dialog)
                .subscribe(aVoid -> {
                    showDialog();
                });
    }

    @Override
    protected int initLayoutRes() {
        return R.layout.activity_test;
    }

    private void showDialog(){
        mSweetAlertDialog = new SweetAlertDialog(this)
                .setCustomImage(R.mipmap.dialog_bg)
                .setContentText("进入")
                .setConfirmText("去设置")
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
