package com.ctrip.bannerapplication;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import java.util.List;

/**
 * @author Zhenhua on 2017/9/27.
 * @email zhshan@ctrip.com ^.^
 */
public class BannerPagerAdapter extends PagerAdapter {
    private final static String TAG = "SZH";
    private List<View> items;

    public BannerPagerAdapter(List<View> items) {
        this.items = items;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        //取模，使得ViewPager的view能依次轮询下去
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
            return BannerDelegate.MAX_VALUE;
        }
        return items.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object o) {
        return view == (View) o;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {

    }
}
