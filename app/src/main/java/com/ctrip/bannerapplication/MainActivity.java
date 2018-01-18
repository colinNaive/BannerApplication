package com.ctrip.bannerapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

/**
 * @author Zhenhua on 2018/1/18.
 * @email zhshan@ctrip.com ^.^
 */

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView tv1 = (TextView) findViewById(R.id.packaged_banner);
        tv1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, PackagedBannerActivity.class);
                MainActivity.this.startActivity(intent);
            }
        });
        TextView tv2 = (TextView) findViewById(R.id.not_packaged_banner);
        tv2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, NotPackagedBannerActivity.class);
                MainActivity.this.startActivity(intent);
            }
        });
    }
}
