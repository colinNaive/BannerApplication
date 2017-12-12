package com.ctrip.bannerapplication;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    public BannerDelegate bannerDelegate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bannerDelegate = new BannerDelegate(getWindow().getDecorView(), this);
        int[] sourceList = {R.drawable.pic1, R.drawable.pic2, R.drawable.pic3, R.drawable.pic4, R.drawable.pic5};
        bannerDelegate.setHandler(this);
        bannerDelegate.refreshBanner(sourceList);
    }
}
