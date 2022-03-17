package com.ffcc66.diary.writediary;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.bigkoo.pickerview.listener.OnTimeSelectListener;
import com.bigkoo.pickerview.view.TimePickerView;
import com.bilibili.boxing.Boxing;
import com.bilibili.boxing.BoxingMediaLoader;
import com.bilibili.boxing.model.config.BoxingConfig;
import com.bilibili.boxing.model.entity.BaseMedia;
import com.bilibili.boxing_impl.ui.BoxingActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.ffcc66.diary.R;
import com.ffcc66.diary.base.BaseActivity;
import com.ffcc66.diary.base.Config;
import com.ffcc66.diary.bean.Diary;
import com.ffcc66.diary.bean.Tag;
import com.ffcc66.diary.util.BoxingGlideLoader;
import com.ffcc66.diary.util.DateUtil;
import com.ffcc66.diary.util.LatLonPoint;
import com.ffcc66.diary.util.FileUtils;
import com.ffcc66.diary.util.LatLonUtil;

import org.litepal.LitePal;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import interfaces.heweather.com.interfacesmodule.bean.Lang;
import interfaces.heweather.com.interfacesmodule.bean.Unit;
import interfaces.heweather.com.interfacesmodule.bean.weather.now.Now;
import interfaces.heweather.com.interfacesmodule.view.HeConfig;
import interfaces.heweather.com.interfacesmodule.view.HeWeather;
import pub.devrel.easypermissions.EasyPermissions;

public class WriteDiaryAvtivity extends BaseActivity implements EasyPermissions.PermissionCallbacks, ChooseTagListener {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.tv_date)
    TextView date;
    @BindView(R.id.weather)
    ImageView weather;
    @BindView(R.id.content)
    EditText content;
    @BindView(R.id.image)
    ImageView image;
    @BindView(R.id.add_img_panel)
    RelativeLayout addImgPanel;
    @BindView(R.id.location)
    LinearLayout llLocaltion;
    @BindView(R.id.tv_localtion)
    TextView tvLocaltion;
    @BindView(R.id.tool)
    LinearLayout tool;
    @BindView(R.id.calendar)
    AppCompatImageView calendar;
    @BindView(R.id.time)
    AppCompatImageView time;
    @BindView(R.id.wordcount)
    TextView wordCount;
    @BindView(R.id.delete_img)
    ImageView deleteImg;
    @BindView(R.id.tag)
    ImageView tag;

    private static final String TAG = "WriteDiaryAvtivity";

    private Tag tagBean = null;
    /**
     * 坐标点字符串
     */
    private String latLonPointString = "";

    /**
     * 选择或拍摄的图片的真实路径
     */
    private String imgRealPath = "";

    /**
     * 当前时间
     */
    private Date now = new Date();

    /**
     * AMapLocationClient类对象
     */
    public AMapLocationClient mLocationClient = null;

    /**
     * 声明定位回调监听器
     */
    public AMapLocationListener mLocationListener = new LocationListener();

    /**
     * 选择图片的请求码
     */
    private final int REQUEST_IMAGE_CODE = 100;

    /**
     * 时间选择器
     */
    private TimePickerView timePickerView;

    /**
     * 日记标签选择窗口
     */
    private MaterialDialog tagDialog;

    private MaterialDialog weatherDialog;

    //是否为编辑模式
    private boolean isEditModel = false;

    /**
     * 所需权限
     */
    private String permissions[] = new String[]{
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.INTERNET,
    };

    /**
     * 要存储的Diary对象
     */
    private Diary diary = new Diary();

    @Override
    public int initLayout() {
        return R.layout.activity_write_diary;
    }

    @Override
    public void initView() {
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(view -> back());

        Intent intent = getIntent();
        isEditModel = intent.getBooleanExtra("isEdit", false);
        if (isEditModel) {
            diary = LitePal.find(Diary.class, intent.getIntExtra("diaryId",0));
            now = new Date(diary.getData());
            tagBean = diary.getTag();
            initEditModelView();
        }
        date.setText(new DateUtil(now.getTime()).getDateTimeStringTwo());

        //设置下方工具栏的显示与关闭
        content.setOnFocusChangeListener(((view, hasFocus) -> {
            if (hasFocus) {
                tool.setVisibility(View.VISIBLE);
            } else {
                tool.setVisibility(View.GONE);
            }
        }));

        //初始化时间选择器与标签选择弹窗
        initTimePicker();
        initTagDialog();
        initWeatherDiaolog();
        weather.setOnClickListener(v -> weatherDialog.show());
    }

    @Override
    public void initData() {

        //判断是否拥有权限
        if (EasyPermissions.hasPermissions(this, permissions)) {
            getLocationData();   //如果不是编辑模式，则自动获取位置
            getWeatherData();
        } else {
            //请求权限
            EasyPermissions.requestPermissions(this,
                    "请求必要的权限,拒绝权限可能会无法定位和获取天气",
                    0, permissions);
        }

    }


    /**
     * 保存日记
     */
    private void saveDiary() {
        if (!imgRealPath.equals("")) {
            diary.setImg(new DateUtil(now.getTime()).getDateTimeStringOne() + ".jpg");
            FileUtils.copyFile(new File(imgRealPath), getExternalFilesDir("image").
                    getAbsolutePath() + File.separator, diary.getImg());
        }
        //如果用户没有选择Tag,那么使用默认Tag
        if (tagBean == null) {
            tagBean = LitePal.where("name = ?","默认").findFirst(Tag.class);
        }

        diary.setTag(tagBean);
        diary.setContent(content.getText().toString());
        diary.setData(now.getTime());
        if (diary.isSaved()) {
            diary.update(diary.getId());
            ContentValues contentValues = new ContentValues();
            contentValues.put("tag_id", tagBean.getId());
            LitePal.update(Diary.class, contentValues, diary.getId());
        } else {
            diary.save();
        }

        if (isEditModel) {
            Intent intent = new Intent();
            intent.putExtra("changedDiary", diary);
            setResult(RESULT_OK,intent);
        }
        finish();
    }

    /**
     * 向edittext中插入文字
     *
     * @param editText
     * @param data
     */
    private void addChar(EditText editText, String data) {
        int index = editText.getSelectionStart();
        Editable editable = editText.getText();
        editable.insert(index, data);
    }

    /**
     * 点击监听
     *
     * @param view
     */
    @OnClick({R.id.tag, R.id.save, R.id.add_img, R.id.calendar,
            R.id.time, R.id.delete_img, R.id.tv_date, R.id.location})
    public void onClick(View view) {
        Log.e(TAG, "onClick: " + view.getId());
        switch (view.getId()) {
            //添加图片
            case R.id.add_img:
                addDiaryImg();
                break;
            //删除图片
            case R.id.delete_img:
                deleteDiaryImg();
                break;
            //选择Tag
            case R.id.tag:
                tagDialog.show();
                break;
            //保存日记
            case R.id.save:
                if (content.getText().toString().trim().equals("")) {
                    Toast.makeText(this, "请先输入内容！", Toast.LENGTH_SHORT).show();
                } else {
                    saveDiary();
                    finish();
                }
                break;
            //添加日期字符串
            case R.id.calendar:
                addChar(content, "[" + new DateUtil(new Date().getTime()).getDateString() + "]");
                break;
            //添加时间字符串
            case R.id.time:
                addChar(content, "[" + new DateUtil(new Date().getTime()).getTimeString() + "]");
                break;
            //设置日记时间
            case R.id.tv_date:
                timePickerView.show();
                break;
            //手动选择地点
            case R.id.location:
                editLocation();
                break;
            default:
                break;
        }
    }

    /**
     * 添加图片
     */
    private void addDiaryImg() {
        BoxingMediaLoader.getInstance().init(new BoxingGlideLoader());
        BoxingConfig config = new BoxingConfig(BoxingConfig.Mode.SINGLE_IMG);
        config.needCamera(R.drawable.camera).withMaxCount(1).withAlbumPlaceHolderRes(R.drawable.loading);
        Boxing.of(config).withIntent(this, BoxingActivity.class).
                start(this, REQUEST_IMAGE_CODE);
    }

    /**
     * 删除图片
     */
    private void deleteDiaryImg() {
        deleteImg.setVisibility(View.GONE);
        image.setImageDrawable(null);
        image.setBackgroundResource(R.color.gray_ec);
        addImgPanel.setVisibility(View.VISIBLE);

        //如果当前日记实体里有图片名称，则说明当前要删除的是上次保存的图片
        if (!diary.getImg().equals("")) {
            File imageFile = new File(getExternalFilesDir("image").getAbsolutePath() + File.separator + diary.getImg());
            if (imageFile.exists()) {
                imageFile.delete();
            }
        }
        imgRealPath = "";
        diary.setToDefault("img");
    }

    /**
     * 输入文字监听，用于更新字数显示
     *
     * @param editable
     */
    @OnTextChanged(R.id.content)
    public void textChange(Editable editable) {
        int contentLength = editable.length();
        wordCount.setText("" + contentLength);
    }

    /**
     * 初始化定位配置并启动定位
     */
    private void getLocationData() {

        if (isEditModel || !Config.isCanRecordLocation()) return;

        mLocationClient = new AMapLocationClient(getApplicationContext());
        mLocationClient.setLocationListener(mLocationListener);
        AMapLocationClientOption mLocationOption = new AMapLocationClientOption();
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.
                Hight_Accuracy);
        //获取一次定位结果：
        mLocationOption.setOnceLocation(true);
        //获取最近3s内精度最高的一次定位结果：
        mLocationOption.setOnceLocationLatest(true);
        mLocationOption.setInterval(1000 * 10);
        mLocationOption.setHttpTimeOut(10000);
        mLocationClient.setLocationOption(mLocationOption);
        mLocationClient.startLocation();
    }

    /**
     * 获取天气信息
     */
    private void getWeatherData() {
        Log.e(TAG, "getWeatherData: "+Config.isCanRecordWeather());
        if (isEditModel || !Config.isCanRecordWeather()) return;
        WeatherListener weatherListener = new WeatherListener();
        HeConfig.init("HE1903211634591796",
                "34dfdf2d730f4b348bf0fec6afb7a100");

        HeConfig.switchToFreeServerNode();

        //如果已经定位到了用户的坐标就使用坐标获取天气，否则使用默认方法获取
        if (!latLonPointString.equals("")) {
            HeWeather.getWeatherNow(this,
                    latLonPointString,
                    Lang.CHINESE_SIMPLIFIED,
                    Unit.METRIC,
                    weatherListener);
        } else {
            HeWeather.getWeatherNow(this,
                    Lang.CHINESE_SIMPLIFIED,
                    Unit.METRIC,
                    weatherListener);
        }

    }

    /**
     * 当模式为编辑模式时，用此方法初始化界面
     */
    private void initEditModelView() {
        date.setClickable(false);
        content.setText(diary.getContent());
        tvLocaltion.setText(diary.getLocation());
        if (!diary.getImg().equals("")) {

            File imageFile = new File(getExternalFilesDir("image").getAbsolutePath() + File.separator + diary.getImg());
            addImgPanel.setVisibility(View.GONE);
            deleteImg.setVisibility(View.VISIBLE);

            Glide.with(this).load(imageFile.getAbsoluteFile())
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(image);

        }
        weather.setImageResource(FileUtils.getResource("weather_" + diary.getWeather(),
                "mipmap",
                this));
    }

    /**
     * 初始化时间选择器
     */
    private void initTimePicker() {

        Calendar startDate = Calendar.getInstance();
        startDate.set(2000, 0, 1);
        Calendar endDate = Calendar.getInstance();
        if (Config.isCanRecordFutureDiary()) {
            endDate.set(2099,12,31);
        }

        timePickerView = new TimePickerBuilder(this, (selectedDate, view) -> {//选中事件回调
            now = selectedDate;
            date.setText(new DateUtil(selectedDate.getTime()).getDateTimeStringTwo());
        })
                .setType(new boolean[]{true, true, true, true, true, false})// 默认全部显示
                .setCancelText("取消")//取消按钮文字
                .setSubmitText("确认")//确认按钮文字
                .setTitleSize(20)//标题文字大小
                .setTitleText("请选择时间")//标题文字
                .setTitleColor(Color.BLACK)//标题文字颜色
                .setSubmitColor(Color.BLACK)//确定按钮文字颜色
                .setCancelColor(Color.BLACK)//取消按钮文字颜色
                .setTitleBgColor(Color.WHITE)//标题背景颜色 Night mode
                .setBgColor(Color.WHITE)//滚轮背景颜色 Night mode
                .setLineSpacingMultiplier(1.5f)
                .setDate(Calendar.getInstance())
                .setRangDate(startDate, endDate)//起始终止年月日设定
                .setLabel("年", "月", "日", "时", "分", "")
                .isDialog(false)//是否显示为对话框样式
                .build();
    }

    /**
     * 初始化Tag选择窗口
     */
    private void initTagDialog() {
        List<Tag> tags = LitePal.findAll(Tag.class);
        ChooseTagAdapter chooseTagAdapter = new ChooseTagAdapter(tags, this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        tagDialog = new MaterialDialog.Builder(this)
                .adapter(chooseTagAdapter, linearLayoutManager)
                .title("请选择标签")
                .build();
    }

    private void initWeatherDiaolog() {
        weatherDialog = new MaterialDialog.Builder(this)
                .title("标题")
                .positiveText("确认")
                .items(R.array.weathers)
                .itemsIds(R.array.weatherIds)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        Log.e(TAG, "onSelection: "+view.getId());
                        weather.setImageResource(FileUtils.getResource("weather_"+view.getId(),"mipmap",WriteDiaryAvtivity.this));
                        diary.setWeather(view.getId());
                    }
                }).build();
    }

    public void editLocation(){
        new MaterialDialog.Builder(this)
                .title("地址")
                .positiveColorRes(R.color.black)
                .dividerColorRes(R.color.black)
                .inputRangeRes(1, 40, R.color.red)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input("请输入你的地址", null, false, (dialog, input) -> {
                    String location = input.toString();
                    diary.setLocation(location);
                    tvLocaltion.setText(location);
                })
                .positiveText("确定")
                .show();
    }


    /**
     * 权限请求结果回调
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    /**
     * 权限请求被允许的回调
     *
     * @param requestCode
     * @param perms
     */
    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        for (String perm: perms) {
            if (perm.equals( Manifest.permission.ACCESS_COARSE_LOCATION) ||
                    perm.equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                getLocationData();
                getWeatherData();
            }
        }
    }

    /**
     * 权限请求被拒绝的回调
     *
     * @param requestCode
     * @param perms
     */
    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {

    }

    /**
     * 选择Tag的回调
     *
     * @param tag
     */
    @Override
    public void onTagClick(Tag tag) {
        this.tagBean = tag;
        Log.e(TAG, "onTagClick: " + tag.getName());
        if (tagDialog.isShowing()) tagDialog.dismiss();
    }

    /**
     * 返回，包括按键返回和标题栏的返回按钮
     */
    private void back() {
        if (!content.getText().toString().trim().equals("")) {
            if (Config.isCanAutoSave()) {
                saveDiary();
            } else {
                new MaterialDialog.Builder(this)
                        .title("提示")
                        .positiveText("确认")
                        .negativeText("取消")
                        .content("尚未保存，是否放弃本次编辑")
                        .neutralText("保存")
                        .onPositive((dialog, whichAction) -> {
                            finish();
                        })
                        .onNegative((dialog, whichAction) -> {
                            dialog.dismiss();
                        })
                        .onNeutral((dialog, whichAction) -> {
                            dialog.dismiss();
                            saveDiary();
                            finish();
                        }).show();
            }
        } else {
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        back();
    }

    /**
     * 定位监听回调，获取位置信息
     */
    class LocationListener implements AMapLocationListener {

        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {
            Log.e(TAG, "Location: " + aMapLocation.getAoiName());
            if (!"".equals(aMapLocation.getAoiName())) {
                tvLocaltion.setText(aMapLocation.getAoiName()); //将位置显示出来
                diary.setLocation(aMapLocation.getAoiName());   //将位置记录到日志信息中
                //将高德的坐标转化为gps坐标
                LatLonPoint latLonPoint = LatLonUtil.toGPSPoint(aMapLocation.getLatitude(), aMapLocation.getLongitude());
                latLonPointString = latLonPoint.toString();
                getWeatherData();
            }
        }
    }

    /**
     * 天气信息接口回调
     */
    class WeatherListener implements HeWeather.OnResultWeatherNowBeanListener {
        @Override
        public void onError(Throwable throwable) {
            Log.e(TAG, "onError: ", throwable);
        }

        @Override
        public void onSuccess(List<Now> list) {
            String weatherCode = list.get(0).getNow().getCond_code();
            diary.setWeather(Integer.valueOf(weatherCode));
            weather.setImageResource(FileUtils.getResource("weather_" + weatherCode, "mipmap", getApplicationContext()));
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            final ArrayList<BaseMedia> medias = Boxing.getResult(data);
            if (requestCode == REQUEST_IMAGE_CODE && medias != null && medias.size() > 0) {
                addImgPanel.setVisibility(View.GONE);
                BaseMedia baseMedia = medias.get(0);
                imgRealPath = baseMedia.getPath();
                Log.e("onActivityResult", "onActivityResult: " + imgRealPath);
                Glide.with(this)
                        .load(imgRealPath)
                        .into(image);
                deleteImg.setVisibility(View.VISIBLE);
            }
        }
    }


    private void checkPermission() {

    }

    /**
     * WriterDiaryActivity的启动方法
     *
     * @param activity
     * @param isEdit
     * @param diaryId
     */
    public static void start(Activity activity, boolean isEdit, int diaryId) {
        Intent intent = new Intent(activity, WriteDiaryAvtivity.class);
        intent.putExtra("isEdit", isEdit);
        intent.putExtra("diaryId", diaryId);
        activity.startActivityForResult(intent, 0);
    }
}
