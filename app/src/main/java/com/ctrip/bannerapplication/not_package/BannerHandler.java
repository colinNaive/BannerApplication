package com.ctrip.bannerapplication.not_package;

import android.os.Handler;
import android.os.Message;

import com.ctrip.bannerapplication.NotPackagedBannerActivity;

import java.lang.ref.WeakReference;

/**
 * @author Zhenhua on 2017/9/27.
 * @email zhshan@ctrip.com ^.^
 */
public class BannerHandler extends Handler {
    private String TAG = "SZH";
    private WeakReference<NotPackagedBannerActivity> mWeakReference;
    //轮播间隔时间
    public static final int MSG_DELAY = 3000;
    //轮播
    public static final int MSG_UPDATE_IMAGE = 1;
    //暂停轮播
    public static final int MSG_KEEP_SILENT = 2;
    //恢复轮播
    public static final int MSG_BREAK_SILENT = 3;
    //记录最新的页号
    public static final int MSG_PAGE_CHANGED = 4;
    private int currentViewPagerItem = BannerDelegate.MAX_VALUE / 2;

    public BannerHandler(NotPackagedBannerActivity activity) {
        mWeakReference = new WeakReference<NotPackagedBannerActivity>(activity);
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        NotPackagedBannerActivity activity = mWeakReference.get();
        if (activity == null || activity.bannerDelegate == null || activity.bannerDelegate.mHandler == null) {
            return;
        }
        //当队列中有消息时，移除消息
        if ((activity.bannerDelegate.mHandler.hasMessages(MSG_UPDATE_IMAGE)) && (currentViewPagerItem != BannerDelegate.MAX_VALUE / 2)) {
            activity.bannerDelegate.mHandler.removeMessages(MSG_UPDATE_IMAGE);
        }
        switch (msg.what) {
            case MSG_UPDATE_IMAGE:
                //ViewPager轮播
                currentViewPagerItem++;
                activity.bannerDelegate.banner.setCurrentItem(currentViewPagerItem);
                activity.bannerDelegate.mHandler.sendEmptyMessageDelayed(MSG_UPDATE_IMAGE, MSG_DELAY);
                break;
            case MSG_KEEP_SILENT:
                //不发消息
                break;
            case MSG_BREAK_SILENT:
                //恢复轮播
                activity.bannerDelegate.mHandler.sendEmptyMessageDelayed(MSG_UPDATE_IMAGE, MSG_DELAY);
                break;
            case MSG_PAGE_CHANGED:
                currentViewPagerItem = msg.arg1;
                break;
        }
    }
}
