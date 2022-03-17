package com.ffcc66.diary.base;
import android.database.Cursor;
import android.support.v7.app.AppCompatDelegate;

import com.facebook.stetho.Stetho;
import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.ffcc66.diary.bean.Tag;

import org.litepal.LitePal;
import org.litepal.LitePalApplication;
import org.litepal.tablemanager.Connector;

import java.util.Calendar;
import java.util.Date;

import okhttp3.OkHttpClient;

public class Application extends LitePalApplication {
    @Override
    public void onCreate() {
        super.onCreate();

        Config.init(getContext());
        if (Config.isFirstOpen()) {
            Connector.getDatabase();
            Tag tag = LitePal.where("name = ?","默认").findFirst(Tag.class);
            if (tag == null) {
                tag = new Tag("默认");
                tag.save();
            }
            Config.setIsFirstOpen(false);
            Config.setFirstUseTime(new Date().getTime());
        }

        if (Config.isCanAutoSwitchNightModel()) {
            Calendar now = Calendar.getInstance();

            String[] time = Config.getNightModelTime().split(":");
            int hour = Integer.valueOf(time[0]);
            int minute = Integer.valueOf(time[1]);

            if (now.get(Calendar.HOUR_OF_DAY) > hour) {
                Config.setIsNightModel(true);
            } else if (now.get(Calendar.HOUR_OF_DAY) == hour) {
                if (now.get(Calendar.MINUTE) >= minute) {
                    Config.setIsNightModel(true);
                } else {
                    Config.setIsNightModel(false);
                }
            } else {
                Config.setIsNightModel(false);
            }

            if (now.get(Calendar.HOUR_OF_DAY) < 6) {
                Config.setIsNightModel(true);
            } else {
                Config.setIsNightModel(false);
            }


//            if ((now.get(Calendar.HOUR_OF_DAY)>=hour) && (now.get(Calendar.MINUTE)>=minute))  {
//
//            } else if((now.get(Calendar.HOUR_OF_DAY)<=5) && (now.get(Calendar.MINUTE)<=59)) {
//                Config.setIsNightModel(true);
//            }else {
//                Config.setIsNightModel(false);
//            }
        }
        AppCompatDelegate.setDefaultNightMode(Config.isNightModel() ? AppCompatDelegate.MODE_NIGHT_YES: AppCompatDelegate.MODE_NIGHT_NO);


        Stetho.initializeWithDefaults(this);
        new OkHttpClient.Builder()
                .addNetworkInterceptor(new StethoInterceptor())
                .build();
    }
}
