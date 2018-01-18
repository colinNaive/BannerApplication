package com.ctrip.bannerapplication;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.ctrip.bannerapplication.not_package.BannerDelegate;
import com.ctrip.bannerapplication.not_package.CommonUtil;
import com.ctrip.bannerapplication.packaged.AutoSlideView;

import java.util.ArrayList;
import java.util.List;

public class PackagedBannerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_packaged);
        //步骤一
        AutoSlideView autoSlideView = (AutoSlideView) findViewById(R.id.banner);
        //步骤二
        autoSlideView.init(true);
        int[] pics = {R.drawable.pic1, R.drawable.pic2, R.drawable.pic3, R.drawable.pic4, R.drawable.pic5};
        List<View> views = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            ImageView iv = new ImageView(this);
            iv.setImageResource(pics[i]);
            views.add(iv);
        }
        //步骤三
        autoSlideView.setData(views, R.drawable.ball_drawable, CommonUtil.dp2px(this, 8), CommonUtil.dp2px(this, 10));
        //步骤四
        autoSlideView.startScroll();
    }
}
