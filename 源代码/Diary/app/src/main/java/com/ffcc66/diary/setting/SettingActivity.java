package com.ffcc66.diary.setting;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.ffcc66.diary.MainActivity;
import com.ffcc66.diary.R;
import com.ffcc66.diary.base.BaseActivity;
import com.ffcc66.diary.base.Config;
import com.ffcc66.diary.base.GlobalValues;
import com.ffcc66.diary.writediary.WriteDiaryAvtivity;

import java.util.Calendar;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.OnClick;

import static android.graphics.Color.GRAY;
import static android.icu.text.DateTimePatternGenerator.DAY;

public class SettingActivity extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.remind_switch)
    Switch switchRemind;
    @BindView(R.id.remind_time)
    TextView tvRemindTime;
    @BindView(R.id.auto_night_model_switch)
    Switch switchAutoSwitchNightModel;
    @BindView(R.id.night_model_time)
    TextView tvNightModelTime;
    @BindView(R.id.record_future_switch)
    Switch switchRecordFutureDiary;
    @BindView(R.id.record_location_switch)
    Switch switchRecordLocation;
    @BindView(R.id.record_weather_swith)
    Switch switchRecordWeather;
    @BindView(R.id.auto_save_swith)
    Switch switchAutoSave;

    private String nightModelStartTime = "";
    private String nightModelEndTime = "";
    private boolean hasSetEndTime = false;

    @Override
    public int initLayout() {
        return R.layout.activity_setting;
    }

    @Override
    public void initView() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(view -> finish());
        switchRemind.setChecked(Config.isCanRemind());
        tvRemindTime.setText("每天提醒时间  "+Config.getRemindTime());
        if (!Config.isCanRemind()) {
           tvRemindTime.setTextColor(GRAY);
           tvRemindTime.setClickable(false);
           tvRemindTime.setEnabled(false);
        }
        switchAutoSwitchNightModel.setChecked(Config.isCanAutoSwitchNightModel());
        tvNightModelTime.setText("开启夜间模式时间  " + Config.getNightModelTime());
        if (!Config.isCanAutoSwitchNightModel()) {
            tvNightModelTime.setClickable(false);
            tvNightModelTime.setTextColor(Color.GRAY);
            tvNightModelTime.setEnabled(false);
        }
        switchRecordFutureDiary.setChecked(Config.isCanRecordFutureDiary());
        switchRecordLocation.setChecked(Config.isCanRecordLocation());
        switchRecordWeather.setChecked(Config.isCanRecordWeather());
        switchAutoSave.setChecked(Config.isCanAutoSave());

        switchRemind.setOnCheckedChangeListener((view, isChecked) -> {
            Config.setCanRemind(isChecked);
            if (isChecked) {
                tvRemindTime.setTextColor(Color.BLACK);
                tvRemindTime.setClickable(true);
                tvRemindTime.setEnabled(true);
            } else {
                cancelRemind();
                tvRemindTime.setTextColor(GRAY);
                tvRemindTime.setClickable(false);
                tvRemindTime.setEnabled(false);
            }
        });

        switchAutoSwitchNightModel.setOnCheckedChangeListener((view, isChecked) -> {
            Config.setCanAutoSwitchNightModel(isChecked);
            if (isChecked) {
                tvNightModelTime.setTextColor(Color.BLACK);
                tvNightModelTime.setClickable(true);
                tvNightModelTime.setEnabled(true);
            } else {
                tvNightModelTime.setClickable(false);
                tvNightModelTime.setTextColor(Color.GRAY);
                tvNightModelTime.setEnabled(false);
            }
        });
        tvRemindTime.setOnClickListener(view -> showTimePickerDialog(view));
        tvNightModelTime.setOnClickListener(view -> showTimePickerDialog(view));
        switchRecordFutureDiary.setOnCheckedChangeListener((view,isChecked) -> Config.setCanRecordFutureDiary(isChecked));
        switchRecordLocation.setOnCheckedChangeListener((view, isChecked) -> Config.setCanRecordLocation(isChecked));
        switchRecordWeather.setOnCheckedChangeListener((view, isChecked) -> Config.setCanRecordWeather(isChecked));
        switchAutoSave.setOnCheckedChangeListener((view,isChecked) -> Config.setCanAutoSave(isChecked));
    }

    @Override
    public void initData() {

    }

    private void setRemind(int minute, int hour) {

        long firstTime = SystemClock.elapsedRealtime(); // 开机之后到现在的运行时间(包括睡眠时间)
        long systemTime = System.currentTimeMillis();   //系统现在的时间

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        // 这里时区需要设置一下，不然会有8个小时的时间差
        calendar.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        // 选择的定时时间
        long selectTime = calendar.getTimeInMillis();
        // 如果当前时间大于设置的时间，那么就从第二天的设定时间开始
        if(systemTime > selectTime) {
            Toast.makeText(this,"设置的时间小于当前时间", Toast.LENGTH_SHORT).show();
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            selectTime = calendar.getTimeInMillis();
        }
        // 计算现在时间到设定时间的时间差
        long time = selectTime - systemTime;
        firstTime += time;

        Intent intent = new Intent();
        intent.setAction(GlobalValues.REMIND_ACTION_REPEAT);
        intent.setComponent(new ComponentName("com.ffcc66.diary","com.ffcc66.diary.setting.RemindReceiver"));
        PendingIntent sender = PendingIntent.getBroadcast(this, 0, intent, 0);
        // 进行闹铃注册
        AlarmManager manager = (AlarmManager)getSystemService(ALARM_SERVICE);
        manager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime, AlarmManager.INTERVAL_DAY, sender);
        Toast.makeText(this,"设置提醒时间成功! ", Toast.LENGTH_LONG).show();
    }

    private void cancelRemind() {
        Intent intent = new Intent();
        intent.setAction(GlobalValues.REMIND_ACTION_REPEAT);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        manager.cancel(pendingIntent);
    }

    public void showTimePickerDialog(View view) {
        new TimePickerDialog(this,
                // 绑定监听器
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker v, int hour, int minute) {
                        String minuteStr = minute < 10 ? "0"+minute: minute+"";
                        if (view.getId() == R.id.remind_time) {
                            tvRemindTime.setText("每天提醒时间  " + hour + ":" + minuteStr);
                            setRemind(minute, hour);
                            Config.setRemindTime(hour+":"+minute);
                        } else {
                            tvNightModelTime.setText("开启夜间模式时间  "+hour+":"+minute);
                            Config.setNightModelTime(hour+":"+minuteStr);
                        }
                    }},20,0,true).show();
    }


}
