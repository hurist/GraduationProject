package com.ffcc66.diary.diarydetail;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.ffcc66.diary.R;
import com.ffcc66.diary.bean.Diary;
import com.ffcc66.diary.util.DateUtil;
import com.ffcc66.diary.util.FileUtils;

import java.io.File;
import java.util.ArrayList;

public class DiaryDetailViewPagerAdapter extends PagerAdapter {

    private Context context;
    private ArrayList<Diary> diaries;

    public DiaryDetailViewPagerAdapter(Context context, ArrayList<Diary> diaries) {
        this.context = context;
        this.diaries = diaries;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View view = LayoutInflater.from(context).inflate(R.layout.viewpager_diary_detail,null);

        Diary diary = diaries.get(position);
        DateUtil dateUtil = new DateUtil(diary.getData());

        TextView time = view.findViewById(R.id.time);
        TextView day = view.findViewById(R.id.day);
        TextView week = view.findViewById(R.id.week);
        TextView date = view.findViewById(R.id.date);
        ImageView weather = view.findViewById(R.id.img_weather);
        ImageView img = view.findViewById(R.id.image);

        TextView content = view.findViewById(R.id.content);
        TextView location = view.findViewById(R.id.location);


        time.setText(dateUtil.getTimeString()); //时间
        day.setText(dateUtil.getDayStr());      //天
        week.setText(dateUtil.getWeek());       //周几
        date.setText(dateUtil.getYearStr()+"年"+dateUtil.getMonthStr()+"月"); //年月
        weather.setImageResource(FileUtils.getResource("weather_"+diary.getWeather(), //设置天气图标
                "mipmap",context));
        content.setText(diary.getContent());    //日记内容
        location.setText(diary.getLocation());  //位置
        if (!diary.getImg().equals("")) {       //显示日记的图片
            File imageFile = new File(context.getExternalFilesDir("image").getAbsolutePath()
                    + File.separator + diary.getImg());
            Glide.with(context).load(imageFile.getAbsoluteFile())
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(img);
        } else {    //没有图片则隐藏图片的控件
            img.setVisibility(View.GONE);
        }

        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
//        super.destroyItem(container, position, object);
        container.removeView((View)object);
    }

    @Override
    public int getCount() {
        return diaries.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return view == o;
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

}
