package com.snamon.redenvelope.ui;

import android.media.MediaPlayer;

import com.snamon.redenvelope.R;
import com.snamon.redenvelope.widget.BaseActivity;
public class TestActivity extends BaseActivity {

    MediaPlayer mp ;
    @Override
    protected void init() {
        mp=MediaPlayer.create(this, R.raw.redsound);
        viewClick(R.id.text_btn_play)
                .subscribe(aVoid -> {
                    mp.reset();
                    mp = MediaPlayer.create(TestActivity.this, R.raw.redsound);
                    mp.start();
                });

    }

    @Override
    protected int initLayoutRes() {
        return R.layout.activity_test;
    }
}
