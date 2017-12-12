package com.ctrip.bannerapplication;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Zhenhua on 2017/9/27.
 * @email zhshan@ctrip.com ^.^
 */
public class BannerDelegate {
    private static final String TAG = "SZH";

    public ViewPager banner;
    public BannerHandler mHandler;
    public static final int MAX_VALUE = 2000;
    private View mRootView;
    private Context context;
    private LayoutInflater inflater;

    public BannerDelegate(View mRootView, Context context) {
        this.mRootView = mRootView;
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    public void setHandler(MainActivity activity) {
        try {
            if (mHandler != null) {
                mHandler.removeCallbacksAndMessages(null);
                mHandler = null;
            }
            //初始化Handler
            mHandler = new BannerHandler(activity);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void refreshBanner(int[] sourceList) {
        try {
            //设置Banner数据
            initBannerView(sourceList);
            //设置底部信息
            setBottomInfo();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initBannerView(int[] sourceList) {
        RelativeLayout bannerLayout = (RelativeLayout) mRootView.findViewById(R.id.banner_layout);
        bannerLayout.getLayoutParams().height = CommonUtil.getScreenWidth(context) * 247 / 375;
        //得到ViewPager的数据源
        List<View> items = new ArrayList<>();
        final int size = sourceList.length;
        for (int i = 0; i < size; i++) {
            View view = inflater.inflate(R.layout.vacation_detail_banner_item, null);
            //图片
            ImageView img = (ImageView) view.findViewById(R.id.img);
            img.setBackgroundResource(sourceList[i]);
            items.add(view);
        }
        if (size == 0) {
            View view = inflater.inflate(R.layout.vacation_detail_banner_item, null);
            ImageView img = (ImageView) view.findViewById(R.id.img);
            img.setBackgroundResource(R.drawable.pic_none);
            items.add(view);
        }
        //设置ViewPager的adapter
        BannerPagerAdapter adapter = new BannerPagerAdapter(items);
        banner = (ViewPager) mRootView.findViewById(R.id.banner);
        banner.setAdapter(adapter);
        //设置ViewPager切换时间
        CommonUtil.controlViewPagerSpeed(context, banner, 1000);
        //当手指在触摸Banner时，暂停轮播
        banner.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int position) {
                //更新ViewPager的item位置
                mHandler.sendMessage(Message.obtain(mHandler, BannerHandler.MSG_PAGE_CHANGED, position, 0));
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                switch (state) {
                    case ViewPager.SCROLL_STATE_DRAGGING:
                        mHandler.sendEmptyMessage(BannerHandler.MSG_KEEP_SILENT);
                        break;
                    case ViewPager.SCROLL_STATE_IDLE:
                        mHandler.sendEmptyMessageDelayed(BannerHandler.MSG_UPDATE_IMAGE, BannerHandler.MSG_DELAY);
                        break;
                }
            }
        });

        //ViewPager初始位置
        banner.setCurrentItem(BannerDelegate.MAX_VALUE / 2);

        //开始轮播
        mRootView.postDelayed(new Runnable() {
            @Override
            public void run() {
                startScroll();
            }
        }, 200);
    }

    private void setBottomInfo() {
        //ViewPager底部信息
        TextView depart = (TextView) mRootView.findViewById(R.id.depart_city);
        TextView vendor = (TextView) mRootView.findViewById(R.id.vendor);
        TextView number = (TextView) mRootView.findViewById(R.id.number);
        //出发城市
        depart.setText("上海出发");
        //携程自营
        vendor.setText("携程国旅");
        mRootView.findViewById(R.id.divider_banner).setVisibility(View.VISIBLE);
        //编号
        number.setText("编号：16533");
        number.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                shareInfo2Copy(context, "16533");
                return true;
            }
        });
    }

    public static void shareInfo2Copy(Context context, String text) {
        try {
            ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            if (cm != null) {
                cm.setPrimaryClip(ClipData.newPlainText(null, text));
                Toast.makeText(context, "编号已复制", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {

        }
    }

    public void startScroll() {
        //开始轮播
        if (mHandler.hasMessages(BannerHandler.MSG_UPDATE_IMAGE)) {
            mHandler.removeMessages(BannerHandler.MSG_UPDATE_IMAGE);
        }
        mHandler.sendEmptyMessageDelayed(BannerHandler.MSG_UPDATE_IMAGE, 500);
    }

}
