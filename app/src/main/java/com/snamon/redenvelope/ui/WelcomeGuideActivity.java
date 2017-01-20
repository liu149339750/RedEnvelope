package com.snamon.redenvelope.ui;

import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.snamon.redenvelope.EnvelopeGlobal;
import com.snamon.redenvelope.R;
import com.snamon.redenvelope.data.adapter.GuideViewPagerAdapter;
import com.snamon.redenvelope.widget.BaseActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WelcomeGuideActivity extends BaseActivity {
    @BindView(R.id.vp_guide)
    public ViewPager vp;

    // 引导页图片资源
    private static final int[] pics = {R.layout.guide_view1,
            R.layout.guide_view2, R.layout.guide_view3};

    // 底部小点图片
    private ImageView[] dots;

    // 记录当前选中位置
    private int currentIndex;

    @Override
    protected void init() {
        List<View> views = new ArrayList<>();
        // 初始化引导页视图列表
        for (int i = 0; i < pics.length; i++) {
            View view = LayoutInflater.from(this).inflate(pics[i], null);
            if (i == pics.length - 1) {
                viewClick(ButterKnife.findById(view, R.id.btn_enter))
                        .subscribe(aVoid -> {
                            enterMainActivity();
                        });
            }
            views.add(view);

        }

        vp = (ViewPager) findViewById(R.id.vp_guide);
        GuideViewPagerAdapter adapter = new GuideViewPagerAdapter(views);
        vp.setAdapter(adapter);
        vp.addOnPageChangeListener(new PageChangeListener());
        initDots();
        viewClick(R.id.tv_skip)
                .subscribe(aVoid -> {
                    enterMainActivity();
                });
    }

    @Override
    protected int initLayoutRes() {
        return R.layout.activity_welcome_guide;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    private void initDots() {
        LinearLayout ll = (LinearLayout) findViewById(R.id.ll);
        dots = new ImageView[pics.length];

        // 循环取得小点图片
        for (int i = 0; i < pics.length; i++) {
            // 得到一个LinearLayout下面的每一个子元素
            dots[i] = (ImageView) ll.getChildAt(i);
            dots[i].setEnabled(false);// 都设为灰色
        }
        currentIndex = 0;
        dots[currentIndex].setEnabled(true); // 设置为白色，即选中状态
    }

    /**
     * 设置当前指示点
     */
    private void setCurDot(int position) {
        if (position < 0 || position > pics.length || currentIndex == position) {
            return;
        }
        dots[position].setEnabled(true);
        dots[currentIndex].setEnabled(false);
        currentIndex = position;
    }

    private void enterMainActivity() {
        Intent intent = new Intent(WelcomeGuideActivity.this,
                MainActivity.class);
        startActivity(intent);
        EnvelopeGlobal.getSp().setFirstAccess(false);
        finish();
    }

    private class PageChangeListener implements ViewPager.OnPageChangeListener {
        @Override
        public void onPageScrollStateChanged(int position) {

        }

        @Override
        public void onPageScrolled(int position, float arg1, int arg2) {

        }

        @Override
        public void onPageSelected(int position) {
            // 设置底部小点选中状态
            setCurDot(position);
        }

    }
}
