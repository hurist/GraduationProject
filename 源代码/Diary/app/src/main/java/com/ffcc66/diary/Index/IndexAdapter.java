package com.ffcc66.diary.Index;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.ffcc66.diary.R;
import com.ffcc66.diary.bean.Diary;
import com.ffcc66.diary.util.DateUtil;
import com.ffcc66.diary.util.FileUtils;
import com.ffcc66.diary.util.WeatherUtil;

import java.io.File;
import java.util.List;

public class IndexAdapter extends BaseMultiItemQuickAdapter<Diary, BaseViewHolder> {

    Context context;
    private DateUtil dateUtil;

    public IndexAdapter(@Nullable List<Diary> data, Context context) {
        super(data);
        this.context = context;
        addItemType(Diary.BIG_IMG, R.layout.item_big_img);
        addItemType(Diary.SMALL_IMG, R.layout.item_small_img);
    }

    @Override
    protected void convert(BaseViewHolder helper, Diary item) {
        dateUtil = new DateUtil(item.getData());
        switch (helper.getItemViewType()) {
            case Diary.BIG_IMG:
                initBigImg(helper, item);
                break;
            case Diary.SMALL_IMG:
                initSmallImg(helper, item);
                break;
        }


    }

    private void initSmallImg(BaseViewHolder helper, Diary item) {
        helper.setText(R.id.day, dateUtil.getDayStr())
                .setText(R.id.week, dateUtil.getWeek())
                .setText(R.id.date, dateUtil.getYearStr()+"年"+dateUtil.getMonthStr()+"月")
                .setText(R.id.time, dateUtil.getTimeString())
                .setText(R.id.content, item.getContent());

        boolean weatherVisible = !(item.getWeather() == 0);
        helper.setGone(R.id.img_weather, weatherVisible);
        if (weatherVisible) {
            helper.setImageResource(R.id.img_weather,
                    FileUtils.getResource("weather_"+item.getWeather(),"mipmap",context));
        }

        boolean locationVisible = !(item.getLocation().equals(""));
        helper.setGone(R.id.dot, locationVisible);
        helper.setGone(R.id.location,locationVisible);
        if (locationVisible) {
            helper.setText(R.id.location, item.getLocation());
        }

        boolean imgVisible = !(item.getImg().equals(""));
        helper.setGone(R.id.img_cover,imgVisible);
        if (imgVisible) {
            File imageFile = new File(context.getExternalFilesDir("image").getAbsolutePath()+File.separator+item.getImg());
            Log.e(TAG, "convert: "+imageFile.getAbsolutePath());
            Glide.with(context).load(imageFile)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into((ImageView)helper.getView(R.id.img_cover));
        }
    }

    private void initBigImg(BaseViewHolder helper, Diary item) {
        helper.setText(R.id.date, dateUtil.getDateString())
                .setText(R.id.content, item.getContent())
                .setText(R.id.time, dateUtil.getTimeString());

        boolean weatherVisible = !(item.getWeather() == 0);
        helper.setGone(R.id.dot_one, weatherVisible);
        helper.setGone(R.id.weather, weatherVisible);
        if (weatherVisible) {
            helper.setText(R.id.weather, WeatherUtil.getWeatherString(item.getWeather(), context));
        }

        boolean locationVisible = !(item.getLocation().equals(""));
        helper.setGone(R.id.dot_two, locationVisible);
        helper.setGone(R.id.location, locationVisible);
        if (locationVisible) {
            helper.setText(R.id.location, item.getLocation());
        }

        boolean imgVisible = !(item.getImg().equals(""));
        helper.setGone(R.id.img_cover,imgVisible);
        if (imgVisible) {
            File imageFile = new File(context.getExternalFilesDir("image").getAbsolutePath()+File.separator+item.getImg());
            Glide.with(context).load(imageFile)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into((ImageView) helper.getView(R.id.img_cover));
        }
    }
}
