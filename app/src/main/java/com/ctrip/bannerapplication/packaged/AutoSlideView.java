package com.ctrip.bannerapplication.packaged;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Scroller;

import com.ctrip.bannerapplication.R;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Zhenhua on 2018/1/9.
 * @email zhshan@ctrip.com ^.^
 * 使用说明：
 * 1.得到轮播图
 * AutoSlideView autoSlideView = view.findViewById(R.id.banner);
 * 2.初始化，并配置是否可滑动
 * autoSlideView.init(true);
 * 3.参数一:view数组; 参数二:小球; 参数三:小球大小; 参数四:小球距离底部距离
 * autoSlideView.setData(views, R.drawable.ball_drawable, CommonUtil.dp2px(mContext, 8), CommonUtil.dp2px(mContext, 120));
 * 4.开始滑动
 * autoSlideView.startScroll();
 * 5.在fragment的destroy方法中把autoSlideView停止轮播
 * autoSlideView.cancel();
 */

public class AutoSlideView extends FrameLayout implements ViewPager.OnPageChangeListener {

    private static final int MSG_UPDATE_IMAGE = 1;
    private static final int MSG_PAGE_CHANGED = 2;
    private static final int INTERVAL = 5000;

    private int MAX_VALUE = 2000;
    private ViewPager mViewPager;
    private LinearLayout mIndexBallLayout;
    private Context mContext;
    private ViewPagerAdapter mViewPagerAdapter;
    private List<View> mItems;
    private boolean canScroll;

    private Handler mHandler = new Handler() {
        private int currentItem = 0;

        @Override
        public void handleMessage(Message msg) {
            if (this.hasMessages(MSG_UPDATE_IMAGE) && currentItem != 0) {
                this.removeMessages(MSG_UPDATE_IMAGE);
            }
            switch (msg.what) {
                case MSG_UPDATE_IMAGE:
                    currentItem++;
                    mViewPager.setCurrentItem(currentItem);
                    break;
                case MSG_PAGE_CHANGED:
                    currentItem = msg.arg1;
                    break;
            }
        }
    };

    public AutoSlideView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
    }

    public AutoSlideView(@NonNull Context context) {
        super(context);
        this.mContext = context;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        View view = LayoutInflater.from(mContext).inflate(R.layout.auto_slide_view_layout, null);
        this.mViewPager = (ViewPager) view.findViewById(R.id.banner);
        this.mIndexBallLayout = (LinearLayout) view.findViewById(R.id.index_ball_layout);
        this.mViewPager.setOnPageChangeListener(this);
        addView(view);
    }

    private void initBalls(int count, int imgId, int sideLength, int bottomMargin) {
        mIndexBallLayout.removeAllViews();
        if (count <= 1) {
            return;
        }
        ViewGroup.MarginLayoutParams linearLayoutParams = (ViewGroup.MarginLayoutParams) mIndexBallLayout.getLayoutParams();
        linearLayoutParams.bottomMargin = bottomMargin;

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(sideLength, sideLength);
        for (int i = 0; i < count; i++) {
            View view = new View(mContext);
            if (i == 0) {
                view.setSelected(true);
            }
            params.setMargins(sideLength, 0, 0, 0);
            view.setLayoutParams(params);
            view.setBackgroundResource(imgId);
            mIndexBallLayout.addView(view);
        }
    }

    private void refrshBalls(int pos) {
        int count = mIndexBallLayout.getChildCount();
        for (int i = 0; i < count; i++) {
            View view = mIndexBallLayout.getChildAt(i);
            if (pos % count == i) {
                view.setSelected(true);
            } else {
                view.setSelected(false);
            }
        }
    }

    /**
     * 需要暴露给用户使用的方法
     *
     * @param canScroll
     */
    public void init(boolean canScroll) {
        this.canScroll = canScroll;
        mItems = new ArrayList<>();
        mViewPagerAdapter = new ViewPagerAdapter(mItems, MAX_VALUE);
        mViewPager.setAdapter(mViewPagerAdapter);
        controlViewPagerSpeed(mContext, mViewPager, 1000);
    }

    public void setData(List<View> items, int imgId, int sideLength, int bottomMargin) {
        //数据层改动
        mItems.clear();
        for (int i = 0; i < items.size(); i++) {
            mItems.add(items.get(i));
        }
        //通知UI层
        mViewPagerAdapter.notifyDataSetChanged();
        //指示球
        initBalls(items.size(), imgId, sideLength, bottomMargin);
    }

    public void cancel() {
        try {
            if (mHandler != null) {
                mHandler.removeCallbacksAndMessages(null);
                mHandler = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        recordPosition(position);
        refrshBalls(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        switch (state) {
            case ViewPager.SCROLL_STATE_DRAGGING:
                pauseScroll();
                break;
            case ViewPager.SCROLL_STATE_IDLE:
                keepScroll();
                break;
        }
    }

    public interface OnPageChangeListener {
        void onPageScrolled(int i, float v, int i1);

        void onPageSelected(int position);
    }

    /**
     * 轮播相关方法
     *
     * @param pos
     */
    private void recordPosition(int pos) {
        if (mHandler != null) {
            mHandler.sendMessage(Message.obtain(mHandler, MSG_PAGE_CHANGED, pos, 0));
        }
    }

    private void pauseScroll() {
        if (!canScroll) {
            return;
        }
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
    }

    private void keepScroll() {
        if (!canScroll) {
            return;
        }
        if (mHandler != null) {
            mHandler.sendEmptyMessageDelayed(MSG_UPDATE_IMAGE, INTERVAL);
        }
    }

    public void startScroll() {
        if (!canScroll) {
            return;
        }
        if (mHandler == null) {
            return;
        }
        this.post(new Runnable() {
            @Override
            public void run() {
                if (mHandler.hasMessages(MSG_UPDATE_IMAGE)) {
                    mHandler.removeMessages(MSG_UPDATE_IMAGE);
                }
                keepScroll();
            }
        });
    }

    /**
     * 自定义PagerAdapter
     */
    class ViewPagerAdapter extends PagerAdapter {

        private List<View> items;
        private int maxValue;

        public ViewPagerAdapter(List<View> items, int maxValue) {
            this.items = items;
            this.maxValue = maxValue;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            position %= items.size();
            if (position < 0) {
                position = items.size() + position;
            }
            View view = items.get(position);
            ViewParent viewParent = view.getParent();
            if (viewParent != null) {
                ViewGroup parent = (ViewGroup) viewParent;
                parent.removeView(view);
            }
            container.addView(view);
            return view;
        }

        @Override
        public int getCount() {
            if (items.size() > 1) {
                return maxValue;
            }
            return items.size();
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {

        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
    }

    /**
     * 反射 修改滑动速度
     */
    private FixedSpeedScroller mScroller = null;

    //设置ViewPager的滑动时间
    private void controlViewPagerSpeed(Context context, ViewPager viewpager, int DurationSwitch) {
        try {
            Field mField;

            mField = ViewPager.class.getDeclaredField("mScroller");
            mField.setAccessible(true);

            mScroller = new FixedSpeedScroller(context);
            mScroller.setmDuration(DurationSwitch);
            mField.set(viewpager, mScroller);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class FixedSpeedScroller extends Scroller {
        private int mDuration = 1500; // 默认滑动速度 1500ms

        public FixedSpeedScroller(Context context) {
            super(context);
        }

        public FixedSpeedScroller(Context context, Interpolator interpolator) {
            super(context, interpolator);
        }

        @Override
        public void startScroll(int startX, int startY, int dx, int dy, int duration) {
            // Ignore received duration, use fixed one instead
            super.startScroll(startX, startY, dx, dy, mDuration);
        }

        @Override
        public void startScroll(int startX, int startY, int dx, int dy) {
            // Ignore received duration, use fixed one instead
            super.startScroll(startX, startY, dx, dy, mDuration);
        }

        /**
         * set animation time
         *
         * @param time
         */
        public void setmDuration(int time) {
            mDuration = time;
        }

        /**
         * get current animation time
         *
         * @return
         */
        public int getmDuration() {
            return mDuration;
        }
    }

}
