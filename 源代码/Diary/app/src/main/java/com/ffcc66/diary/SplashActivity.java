package com.ffcc66.diary;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.ffcc66.diary.base.BaseActivity;

import butterknife.BindView;

public class SplashActivity extends BaseActivity {

    @BindView(R.id.name)
    TextView name;
    @BindView(R.id.startImg)
    ImageView startImg;

    @Override
    public int initLayout() {
        return R.layout.activity_splash;
    }

    @Override
    public void initView() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Typeface typeface = Typeface.createFromAsset(getAssets(), "font/kunstlerscript.ttf");
        name.setTypeface(typeface);


    }

    @Override
    public void initData() {
        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            SplashActivity.this.finish();
        },3000);//3000表示延迟的毫秒数。
    }
}
