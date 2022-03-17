package com.ffcc66.diary.base;

import android.content.Context;
import android.content.SharedPreferences;

public class Config {

    /**
     * 是否为第一次打开
     */
    private static final boolean IS_FIRST_OPEN = true;

    /**
     * 日记展示时的排序顺序
     * 0、倒序 1、顺序
     */
    private static final int ORDER = 0;
    /**
     * 首页展示模式
     * 1、大图模式 2、小图模式
     */
    private static final int VIEW_MODEL = 1;

    /**
     * 是否可以记录未来的日记
     */
    private static final boolean CAN_RECORD_FUTURE_DIARY = true;

    /**
     * 是否记录位置
     */
    private static final boolean CAN_RECORD_LOCATION = true;

    /**
     * 是否记录天气
     */
    private static final boolean CAN_RECORD_WEATHER = true;

    /**
     * 是否为夜间模式
     */
    private static final boolean IS_NIGHT_MODEL = false;

    /**
     * 自动打开夜间模式
     */
    private static final boolean CAN_AUTO_SWITCH_NIGHT_MODEL = false;

    private static final String NIGHT_MODEL_TIME = "20:00";

    /**
     * 返回自动保存
     */
    private static final boolean CAN_AUTO_SAVE = false;

    /**
     * 提醒时间
     */
    private static final String REMIND_TIME = "20:00";

    /**
     * 是否开启自动提醒
     */
    private static final boolean CAN_REMIND = false;


    private static SharedPreferences sp;

    public static void init(Context context) {
        sp = context.getSharedPreferences("setting", Context.MODE_PRIVATE);
    }

    public static boolean isFirstOpen() {
        return sp.getBoolean("isFirstOpen", IS_FIRST_OPEN);
    }

    public static void setIsFirstOpen(boolean isFirstOpen) {
        sp.edit().putBoolean("isFirstOpen", isFirstOpen).apply();
    }

    public static int getViewModel() {
        return sp.getInt("viewModel", VIEW_MODEL);
    }

    public static void setViewModel(int viewModel) {
        sp.edit().putInt("viewModel", viewModel).apply();
    }

    public static boolean isCanRecordFutureDiary() {
        return sp.getBoolean("canRecordFutureDiary", CAN_RECORD_FUTURE_DIARY);
    }

    public static void setCanRecordFutureDiary(boolean canRecordFutureDiary) {
        sp.edit().putBoolean("canRecordFutureDiary", canRecordFutureDiary).apply();
    }

    public static boolean isCanRecordLocation() {
        return sp.getBoolean("canRecordLocation", CAN_RECORD_LOCATION);
    }

    public static void setCanRecordLocation(boolean canRecordLocation) {
        sp.edit().putBoolean("canRecordLocation", canRecordLocation).apply();
    }

    public static boolean isCanRecordWeather() {
        return sp.getBoolean("canRecordWeather", CAN_RECORD_WEATHER);
    }

    public static void setCanRecordWeather(boolean canRecordWeather) {
        sp.edit().putBoolean("canRecordWeather", canRecordWeather).apply();
    }

    public static boolean isNightModel() {
        return sp.getBoolean("isNightModel", IS_NIGHT_MODEL);
    }

    public static void setIsNightModel(boolean isNightModel) {
        sp.edit().putBoolean("isNightModel", isNightModel).apply();
    }

    public static boolean isCanAutoSwitchNightModel() {
        return sp.getBoolean("canAutoSwitchNightModel", CAN_AUTO_SWITCH_NIGHT_MODEL);
    }

    public static void setCanAutoSwitchNightModel(boolean canAutoSwitchNightModel) {
        sp.edit().putBoolean("canAutoSwitchNightModel", canAutoSwitchNightModel).apply();
    }

    public static boolean isCanAutoSave() {
        return sp.getBoolean("canAutoSave", CAN_AUTO_SAVE);
    }

    public static void setCanAutoSave(boolean canAutoSave) {
        sp.edit().putBoolean("canAutoSave", canAutoSave).apply();
    }

    public static String getRemindTime() {
        return sp.getString("remindTime", REMIND_TIME);
    }

    public static void setRemindTime(String remindTime) {
        sp.edit().putString("remindTime", remindTime).apply();
    }

    public static boolean isCanRemind() {
        return sp.getBoolean("canRemind", CAN_REMIND);
    }

    public static void setCanRemind(boolean canRemind) {
        sp.edit().putBoolean("canRemind", canRemind).apply();
    }

    public static String getNightModelTime() {
        return sp.getString("nightModelTime", NIGHT_MODEL_TIME);
    }

    public static void setNightModelTime(String nightModelTime) {
        sp.edit().putString("nightModelTime", nightModelTime).apply();
    }

    public static void setOrder(int order) {
        sp.edit().putInt("order",order).apply();
    }

    public static int getOrder() {
        return sp.getInt("order",ORDER);
    }

    public static void setFirstUseTime(long date) {
        sp.edit().putLong("firstUseDate", date).apply();
    }

    public static long getFirstUseTime() {
        return sp.getLong("firstUseDate", 0);
    }
}
