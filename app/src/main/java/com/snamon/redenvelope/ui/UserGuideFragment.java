package com.snamon.redenvelope.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.snamon.redenvelope.R;
import com.snamon.redenvelope.utils.Log;

/**
 * 引导页面。
 */
public class UserGuideFragment extends Fragment implements OnClickListener {
    private final String TAG = UserGuideFragment.class.getSimpleName();
    private ViewPager mPager;
    private MyAdapter mAdapter;
    private RadioButton[] mDotViews;
    private RadioGroup mDotGroup;
    private OnFinishGuideListener mObserVerListener;

    public static UserGuideFragment instantiate(){
        return new UserGuideFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.fragment_user_guide, null);
        mAdapter = new MyAdapter();
        mAdapter.guideResId = getFirstGuideRes();
        mPager = (ViewPager) contentView.findViewById(R.id.pager);
        mPager.setOffscreenPageLimit(2);
        mPager.setAdapter(mAdapter);
        mPager.addOnPageChangeListener(mOnPageChangeListener);
        initDotViews(contentView);
        return contentView;
    }

    private void initDotViews(View contentView) {
        mDotGroup = (RadioGroup) contentView.findViewById(R.id.dot_group);
        mDotViews = new RadioButton[3];
        mDotViews[0] = (RadioButton) contentView.findViewById(R.id.radiobutton1);
        mDotViews[1] = (RadioButton) contentView.findViewById(R.id.radiobutton2);
        mDotViews[2] = (RadioButton) contentView.findViewById(R.id.radiobutton3);
    }

    private Object[][] getFirstGuideRes() {
        return new Object[][]{{R.drawable.user_guide_1}, {R.drawable.user_guide_2}, {R.drawable.user_guide_3}};
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
            View view = View.inflate(getActivity(),R.layout.fragment_vp_page, null);
            ImageView imageView = (ImageView) view.findViewById(R.id.image_guide);
            imageView.setImageResource(Integer.parseInt(object[0].toString()));
            if (position == guideResId.length - 1) {
                view.findViewById(R.id.view_begin_use).setVisibility(View.VISIBLE);
                view.findViewById(R.id.btn_begin_use).setOnClickListener(UserGuideFragment.this);
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_begin_use:
                if (mObserVerListener != null) {
                    mObserVerListener.onFinishGuide();
                }
                break;
            default:
                break;
        }
    }

    public void setOnFinishGuideListener(OnFinishGuideListener listener) {
        this.mObserVerListener = listener;
    }

    public interface OnFinishGuideListener {
        void onFinishGuide();
    }

}
