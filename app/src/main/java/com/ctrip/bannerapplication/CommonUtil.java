package com.ctrip.bannerapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.view.ViewPager;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by xhhe on 2015/12/21.
 */
public class CommonUtil {
    /**
     * 得到屏幕的宽度
     *
     * @param context
     * @return
     */
    public static int getScreenWidth(Context context) {
        if (null == context) {
            return 0;
        }
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    /**
     * 得到屏幕的高度
     *
     * @param context
     * @return
     */
    public static int getScreenHeight(Context context) {
        if (null == context) {
            return 0;
        }
        return context.getResources().getDisplayMetrics().heightPixels;
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dp2px(Context context, float dpValue) {
        if (null == context) {
            return 0;
        }
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 将sp值转换为px值，保证文字大小不变
     * @param context
     * @param spValue
     * @return
     */
    public static int sp2px(Context context, float spValue) {
        if (null == context) {
            return 0;
        }
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }


    /**
     * 使用java正则表达式去掉多余的.与0
     *
     * @param s
     * @return
     */
    public static String subZeroAndDot(String s) {
        if (s == null) return "";
        if (s.indexOf(".") > 0) {
            s = s.replaceAll("0+?$", "");//去掉多余的0
            s = s.replaceAll("[.]$", "");//如最后一位是.则去掉
        }
        return s;
    }

    private static FixedSpeedScroller mScroller = null;

    //设置ViewPager的滑动时间
    public static void controlViewPagerSpeed(Context context, ViewPager viewpager, int DurationSwitch) {
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

    /**
     * 获取状态栏的高度
     * @param context
     * @return
     */
    public static int getBarHeight(Context context){
        Class<?> c = null;
        Object obj = null;
        Field field = null;
        int x = 0, sbar = 38;//默认为38，貌似大部分是这样的

        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            sbar = context.getResources().getDimensionPixelSize(x);

        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return sbar;
    }
    /**
     * convert base64 to bitmap
     * @author Zhenhua created at 2016/11/14 16:48
     */
    public static Bitmap base64ToBitmap(String b){
        Bitmap bitmap = null;
        try {
            byte[] bytes = Base64.decode(b, Base64.DEFAULT);
            bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
        }catch (Exception e){
            bitmap = null;
        }

        return bitmap;
    }
    /**
     * convert bitmap to base64
     * @author Zhenhua created at 2016/11/14 16:48
     */
    public static String bitmapToBase64(Bitmap bitmap){
        String b = null;
        ByteArrayOutputStream output = null;
        try {
            output = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output);
            byte[] result = output.toByteArray();
            b = Base64.encodeToString(result, Base64.DEFAULT);
        }catch (Exception e){
            b = null;
        }finally {
            try {
                output.close();
            }catch (Exception ignored){

            }
        }
        return b;
    }

    public static Map<String, String> splitQuery(Uri url) throws UnsupportedEncodingException {
        Map<String, String> query_pairs = new LinkedHashMap<String, String>();
        String query = url.getQuery();
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
        }
        return query_pairs;
    }

}
