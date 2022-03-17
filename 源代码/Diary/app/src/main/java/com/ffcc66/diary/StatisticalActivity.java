package com.ffcc66.diary;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.ffcc66.diary.base.BaseActivity;
import com.ffcc66.diary.base.Config;
import com.ffcc66.diary.bean.Diary;
import com.ffcc66.diary.util.DateUtil;

import org.litepal.LitePal;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;

public class StatisticalActivity extends BaseActivity {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.dayNum)
    TextView tvDayNum;
    @BindView(R.id.count)
    TextView tvCount;
    @BindView(R.id.wordCount)
    TextView tvWordCount;
    @BindView(R.id.imgCount)
    TextView tvImgCount;

    private int count=0,wordCount=0,imgCount=0,dayNum=0;

    @Override
    public int initLayout() {
        return R.layout.activity_statistical;
    }

    @Override
    public void initView() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());
        long firstUseDate = Config.getFirstUseTime();   //获取首次打开的时间
        if (firstUseDate != 0) {
            Date first = new Date(firstUseDate);
            Date now = new Date();
            dayNum = DateUtil.differentDays(first, now) + 1; //计算天数
        }

        List<Diary> list = LitePal.findAll(Diary.class);
        for (Diary diary: list) {
            count += 1;
            wordCount += diary.getContent().length();   //计算字数
            if (!diary.getImg().equals("")) {
                imgCount += 1;                          //计算图片数
            }
        }

        tvDayNum.setText(dayNum+"");
        tvCount.setText(count+"");
        tvImgCount.setText(imgCount+"");
        tvWordCount.setText(wordCount+"");
    }

    @Override
    public void initData() {

    }
}
