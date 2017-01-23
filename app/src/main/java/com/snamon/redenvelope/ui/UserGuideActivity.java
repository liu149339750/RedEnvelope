package com.snamon.redenvelope.ui;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import com.snamon.redenvelope.EnvelopeGlobal;
import com.snamon.redenvelope.ui.frame.EmptyFragmentActivity;

/**
 * 启动引导图页面。
 */
public class UserGuideActivity extends EmptyFragmentActivity {

    public static void startMe(Context context) {
        context.startActivity(new Intent(context, UserGuideActivity.class));
    }

    @Override
    public Fragment getContentFragment() {
        UserGuideFragment fragment = UserGuideFragment.instantiate();
        fragment.setOnFinishGuideListener(() -> {
            MainActivity.startMe(mActivity);
            EnvelopeGlobal.getSp().setFirstAccess(false);
            finish();
        });
        return fragment;
    }

}
