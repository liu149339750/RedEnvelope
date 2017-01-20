package com.snamon.redenvelope.ui;

import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.snamon.redenvelope.R;
import com.snamon.redenvelope.common.util.Log;
import com.snamon.redenvelope.widget.BaseFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 引导页面。
 */
public class UserGuideFragment extends BaseFragment {
    private final String TAG = UserGuideFragment.class.getSimpleName();
    @BindView(R.id.pager)
    public ViewPager mPager;
    private RadioButton[] mDotViews;
    @BindView(R.id.dot_group)
    public RadioGroup mDotGroup;
    @BindView(R.id.radiobutton1)
    public RadioButton mRadioButton1;
    @BindView(R.id.radiobutton2)
    public RadioButton mRadioButton2;
    @BindView(R.id.radiobutton3)
    public RadioButton mRadioButton3;

    private OnFinishGuideListener mObserVerListener;

    public static UserGuideFragment instantiate() {
        return new UserGuideFragment();
    }

    @Override
    protected void init(View view, Bundle savedInstanceState) {
        MyAdapter adapter = new MyAdapter();
        adapter.guideResId = getFirstGuideRes();
        mPager.setOffscreenPageLimit(2);
        mPager.setAdapter(adapter);
        mPager.addOnPageChangeListener(mOnPageChangeListener);

        mDotViews = new RadioButton[3];
        mDotViews[0] = mRadioButton1;
        mDotViews[1] = mRadioButton2;
        mDotViews[2] = mRadioButton3;
    }

    @Override
    protected int genLayoutRes() {
        return R.layout.fragment_user_guide;
    }

    private Object[][] getFirstGuideRes() {
        return new Object[][]{{R.mipmap.guide_1}, {R.mipmap.guide_2}, {R.mipmap.guide_3}};
    }

    class MyAdapter extends PagerAdapter {
        Object guideResId[][];

        @Override
        public int getCount() {
            return guideResId == null ? 0 : guideResId.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Log.i(TAG, "instantiateItem position:" + position);
            Object object[] = guideResId[position];
            View view = View.inflate(mContext, R.layout.fragment_vp_page, null);

            ImageView imageView = ButterKnife.findById(view, R.id.image_guide);
            imageView.setImageResource(Integer.parseInt(object[0].toString()));

            if (position == guideResId.length - 1) {
                ButterKnife.findById(view, R.id.view_begin_use).setVisibility(View.VISIBLE);
                viewClick(ButterKnife.findById(view, R.id.btn_begin_use))
                        .subscribe(aVoid -> {
                            if (mObserVerListener != null) {
                                mObserVerListener.onFinishGuide();
                            }
                        });
            } else {
                view.findViewById(R.id.view_begin_use).setVisibility(View.INVISIBLE);
            }
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }

    OnPageChangeListener mOnPageChangeListener = new OnPageChangeListener() {
        @Override
        public void onPageSelected(int pos) {
            if (pos == mDotViews.length - 1) {
                mDotGroup.setVisibility(View.INVISIBLE);
            } else {
                mDotGroup.setVisibility(View.VISIBLE);
            }
            mDotViews[pos].setChecked(true);
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }
    };

    public void setOnFinishGuideListener(OnFinishGuideListener listener) {
        this.mObserVerListener = listener;
    }

    public interface OnFinishGuideListener {
        void onFinishGuide();
    }

}
