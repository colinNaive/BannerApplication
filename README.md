如果项目急用，可直接下载demo！非常容易使用！轮播组件都已经封装好！点我下载

1.

背景

在做Android产品详情页的时候，我也造了一次轮子——把轮播图自己实现了一遍。经过产品经理的一次又一次的改版要求，我认为这个轮播的实现还是不错的。在完成需求的同时，我也规避掉了潜在的危险，比如内存泄漏问题。其实，一个简单的轮播图，要想真正应用到实际上线项目中，还是有很多细节值得仔细斟酌的，并且轮播流程也有很多说道的。下面来看下效果图（gif的问题，看起来可能卡顿，实际轮播很流畅）：



2.

ViewPager如何无限循环起来？

重点是在ViewPager的adapter里。原理是这样：给ViewPager设置无限多个item（不用担心ViewPager的会内存溢出，因为它有缓存机制！），如果实际只有三张图，等滑到第四个的时候，就是把第一张图add到ViewPager里。这样我们只负责给ViewPager添加item就好了，具体的内存操作交给ViewPager去实现就好了。


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
3.

ViewPager如何动起来？

viewPager使用起来大家肯定驾轻就熟了，可如何让ViewPager滑动起来呢？

本文使用的是Handler的sendMessageDelayed方法，来让以下代码块每2s执行一次：

//ViewPager轮播
currentViewPagerItem++;
fragment.bannerDelegate.banner.setCurrentItem(currentViewPagerItem);
这样还没有完，少了ViewPager的OnPageChangeListener，ViewPager也是轮播不起来：

如图，OnPageChangeListener方法有两个重要方法：onPageSelected和onPageScrollStateChanged方法。

○ onPageSelected方法在每一个page选中时回调，在这里去更新currentViewPagerItem的值。

○ onPageScrollChanged方法紧跟着onPageSelected回调，这里去通过handler的sendMessageDelayed延时2s发送消息。



4.

手指触摸时停止轮播，手指离开时恢复轮播

通过上文的描述可知，viewPager的轮播其实是通过handler的handleMessage中去把viewPager移到下一个位置。那么要想让手指触摸时停止轮播，只需发一个空message；在手指放开时，再调用sendMessageDelayed方法。



5.

避免内存泄漏

在任何使用handler的地方，都应该注意是否有内存泄漏的风险。因为如果handler在Activity finish掉之后，还陆续需要handleMessage时，Activity是不会被成功销毁的，如果多个Activity都无法被销毁，就有可能产生内存泄漏。

通常避免handler内存泄漏的方法有两种：

○ 把handler中持有的fragment对象设置为弱引用。

○ 在fragment的onDestroy方法里handler.removeCallbacksAndMessages(null)。

看过handler源码的人都知道，通过handler 来post runnable或者sendEmptyMessage，其实都会转成Message，放到消息队列里，所以清空消息队列就意味着把Handler重置了。

本文采用的是第一种方法。



6.

具体实现步骤

【1】初始化ViewPager

private void initBannerView(ProductInfo productInfo) {
        RelativeLayout bannerLayout = (RelativeLayout) mRootView.findViewById(R.id.banner_layout);
        bannerLayout.getLayoutParams().height = CommonUtil.getScreenWidth(context) * 247 / 375;
        //得到ViewPager的数据源
        List<View> items = new ArrayList<>();
        final int size = productInfo.getImgList().size();
        for (int i = 0; i < size; i++) {
            if (!isValidUrl(productInfo.getImgList().get(i))) {
                continue;
            }
            View view = inflater.inflate(R.layout.vacation_detail_banner_item, null);
            //图片
            ImageView img = (ImageView) view.findViewById(R.id.img);
            ImageLoaderHelper.displaySmallImage(img, productInfo.getImgList().get(i).getUrlList().get(0).getValue());
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (size < 2) {
                        return;
                    }
                    int productId = VacationDetailUtils.getProductId();
                    int saleCityId = VacationDetailUtils.getSaleCityId();
                    int departCityId = VacationDetailUtils.getDepartCityId();
                    String url = GetEnvH5URL() + "vacations/tour/detail_picture_list?productId=" + productId + "&saleCityId=" + saleCityId + "&departCityId=" + departCityId;
                    CtripH5Manager.openUrl(context, url, null);
                    //埋点
                    VacationDetailBuryPoint.LogAction("picture-more");
                }
            });

            items.add(view);
        }
        if (size == 0) {
            View view = inflater.inflate(R.layout.vacation_detail_banner_item, null);
            ImageView img = (ImageView) view.findViewById(R.id.img);
            ImageLoaderHelper.displaySmallImage(img, "https://pic.c-ctrip.com/vacation_v2/h5/group_travel/pic_none.png");
            items.add(view);
        }
        //设置ViewPager的adapter
        BannerPagerAdapter adapter = new BannerPagerAdapter(items);
        banner = (ViewPager) mRootView.findViewById(R.id.banner);
        banner.setAdapter(adapter);
        //设置ViewPager切换时间
        VacationDetailUtils.controlViewPagerSpeed(context, banner, 1000);
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
public void startScroll() {
        //开始轮播
        if (mHandler.hasMessages(BannerHandler.MSG_UPDATE_IMAGE)) {
            mHandler.removeMessages(BannerHandler.MSG_UPDATE_IMAGE);
        }
        mHandler.sendEmptyMessageDelayed(BannerHandler.MSG_UPDATE_IMAGE, 500);
    }
总结一下ViewPager的初始化：
首先，设置ViewPager的数据源；

其次，设置ViewPager的adapter；

然后，设置ViewPager的滑动速度；

接着，设置ViewPager的touch事件，使得手指放在ViewPager上时，滚动停止；手指离开时，滚动继续；

最后，通过handler发送消息，使ViewPager轮播起来。

另外，还应注意：ViewPager的onPageChangeListener的几个回调方法的回调时机。在setCurrentItem之后，onPageSelected会先回调，然后onPageScrollChanged方法会回调。所以，onPageSelected时，把viewpager的位置更新；onPageChangeListener时，把viewPager的位置加1，在setCurrentItem。

【2】设置ViewPager的adapter

public class BannerPagerAdapter extends PagerAdapter {
    private final static String TAG = VacationDetailUtils.TAG;
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
【3】设置handler
public class BannerHandler extends Handler {
    private String TAG = VacationDetailUtils.TAG;
    private WeakReference<VacationDetailFragment> mWeakReference;
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

    public BannerHandler(VacationDetailFragment fragment) {
        mWeakReference = new WeakReference<VacationDetailFragment>(fragment);
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        VacationDetailFragment fragment = mWeakReference.get();
        if (fragment == null || fragment.bannerDelegate == null || fragment.bannerDelegate.mHandler == null) {
            return;
        }
        //当队列中有消息时，移除消息
        if ((fragment.bannerDelegate.mHandler.hasMessages(MSG_UPDATE_IMAGE)) && (currentViewPagerItem != BannerDelegate.MAX_VALUE / 2)) {
            fragment.bannerDelegate.mHandler.removeMessages(MSG_UPDATE_IMAGE);
        }
        switch (msg.what) {
            case MSG_UPDATE_IMAGE:
                //ViewPager轮播
                currentViewPagerItem++;
                fragment.bannerDelegate.banner.setCurrentItem(currentViewPagerItem);
                fragment.bannerDelegate.mHandler.sendEmptyMessageDelayed(MSG_UPDATE_IMAGE, MSG_DELAY);
                break;
            case MSG_KEEP_SILENT:
                //不发消息
                break;
            case MSG_BREAK_SILENT:
                //恢复轮播
                fragment.bannerDelegate.mHandler.sendEmptyMessageDelayed(MSG_UPDATE_IMAGE, MSG_DELAY);
                break;
            case MSG_PAGE_CHANGED:
                currentViewPagerItem = msg.arg1;
                break;
        }
    }
}


接下来，我会附上demo（点击查看demo）。如果觉得对你的开发有启发，烦请给上你的star。另外有任何问题，可以邮件或留言联系我，我的邮箱zhshan@ctrip.com。



~~~~~~~~~~~~~~~~~~~华丽丽的分割线~~~~~~~~~~~~~~~~~~~~~~~~

在这一版轮播图上线之后，暴露出了一些问题。我这边在改版之后，就轮播流程重新强调一下！



轮播流程

这里为什么要介绍一下轮播流程？如果不了解轮播的流程，很有可能影响轮播图的展示效果。比如，在第一版上线的详情页就存在以下几个问题：

（1）由于详情页使用了缓存机制，进入页面首先加载缓存，等到接口请求回来，再去重新刷新UI。因为这样，我的轮播图会先初始化一次，等到接口请求回来再请求一次。出现的现象就是，轮播图首先展示出来，过大于1秒后，轮播图又被销毁，重新初始化（这尼玛，很坑爹啊，那还用缓存干啥）。

（2）进入页面就开始轮播，这导致轮播的前两幅图片非常快！

在项目不急时，我就开始了对详情页的轮播图的优化，并总结了轮播图的轮播流程：

○ 在页面加载时，就去初始化轮播图ViewPager，并且在这个页面只进行这一次（与轮播无关）。

○ 在接口请求成功之后，只去更新数据源，然后notifyDataChanged（与轮播无关）。

○ startScroll方法在有数据之后就只调用一次，即使后面有数据更新，也不调用该方法（与轮播有关）。

○ 当接口返回数据与缓存数据一致时，不再去刷新轮播图（与轮播无关）。



~~~~~~~~~~~~~~~~~~~华丽丽的分割线~~~~~~~~~~~~~~~~~~~~~~~~

通过对轮播图的深入解读，我已经对轮播图的整个流程都很理解了，为了能给大家日常开发带来便捷，我特意进行了一下封装。大家日后需要轮播图组件，直接拿来用就好了！！

欢迎点击下载demo！！（点我！）



