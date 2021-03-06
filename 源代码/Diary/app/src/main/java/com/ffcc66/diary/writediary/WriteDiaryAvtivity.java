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
     * ??????????????????
     */
    private String latLonPointString = "";

    /**
     * ???????????????????????????????????????
     */
    private String imgRealPath = "";

    /**
     * ????????????
     */
    private Date now = new Date();

    /**
     * AMapLocationClient?????????
     */
    public AMapLocationClient mLocationClient = null;

    /**
     * ???????????????????????????
     */
    public AMapLocationListener mLocationListener = new LocationListener();

    /**
     * ????????????????????????
     */
    private final int REQUEST_IMAGE_CODE = 100;

    /**
     * ???????????????
     */
    private TimePickerView timePickerView;

    /**
     * ????????????????????????
     */
    private MaterialDialog tagDialog;

    private MaterialDialog weatherDialog;

    //?????????????????????
    private boolean isEditModel = false;

    /**
     * ????????????
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
     * ????????????Diary??????
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

        //???????????????????????????????????????
        content.setOnFocusChangeListener(((view, hasFocus) -> {
            if (hasFocus) {
                tool.setVisibility(View.VISIBLE);
            } else {
                tool.setVisibility(View.GONE);
            }
        }));

        //?????????????????????????????????????????????
        initTimePicker();
        initTagDialog();
        initWeatherDiaolog();
        weather.setOnClickListener(v -> weatherDialog.show());
    }

    @Override
    public void initData() {

        //????????????????????????
        if (EasyPermissions.hasPermissions(this, permissions)) {
            getLocationData();   //????????????????????????????????????????????????
            getWeatherData();
        } else {
            //????????????
            EasyPermissions.requestPermissions(this,
                    "?????????????????????,????????????????????????????????????????????????",
                    0, permissions);
        }

    }


    /**
     * ????????????
     */
    private void saveDiary() {
        if (!imgRealPath.equals("")) {
            diary.setImg(new DateUtil(now.getTime()).getDateTimeStringOne() + ".jpg");
            FileUtils.copyFile(new File(imgRealPath), getExternalFilesDir("image").
                    getAbsolutePath() + File.separator, diary.getImg());
        }
        //????????????????????????Tag,??????????????????Tag
        if (tagBean == null) {
            tagBean = LitePal.where("name = ?","??????").findFirst(Tag.class);
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
     * ???edittext???????????????
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
     * ????????????
     *
     * @param view
     */
    @OnClick({R.id.tag, R.id.save, R.id.add_img, R.id.calendar,
            R.id.time, R.id.delete_img, R.id.tv_date, R.id.location})
    public void onClick(View view) {
        Log.e(TAG, "onClick: " + view.getId());
        switch (view.getId()) {
            //????????????
            case R.id.add_img:
                addDiaryImg();
                break;
            //????????????
            case R.id.delete_img:
                deleteDiaryImg();
                break;
            //??????Tag
            case R.id.tag:
                tagDialog.show();
                break;
            //????????????
            case R.id.save:
                if (content.getText().toString().trim().equals("")) {
                    Toast.makeText(this, "?????????????????????", Toast.LENGTH_SHORT).show();
                } else {
                    saveDiary();
                    finish();
                }
                break;
            //?????????????????????
            case R.id.calendar:
                addChar(content, "[" + new DateUtil(new Date().getTime()).getDateString() + "]");
                break;
            //?????????????????????
            case R.id.time:
                addChar(content, "[" + new DateUtil(new Date().getTime()).getTimeString() + "]");
                break;
            //??????????????????
            case R.id.tv_date:
                timePickerView.show();
                break;
            //??????????????????
            case R.id.location:
                editLocation();
                break;
            default:
                break;
        }
    }

    /**
     * ????????????
     */
    private void addDiaryImg() {
        BoxingMediaLoader.getInstance().init(new BoxingGlideLoader());
        BoxingConfig config = new BoxingConfig(BoxingConfig.Mode.SINGLE_IMG);
        config.needCamera(R.drawable.camera).withMaxCount(1).withAlbumPlaceHolderRes(R.drawable.loading);
        Boxing.of(config).withIntent(this, BoxingActivity.class).
                start(this, REQUEST_IMAGE_CODE);
    }

    /**
     * ????????????
     */
    private void deleteDiaryImg() {
        deleteImg.setVisibility(View.GONE);
        image.setImageDrawable(null);
        image.setBackgroundResource(R.color.gray_ec);
        addImgPanel.setVisibility(View.VISIBLE);

        //????????????????????????????????????????????????????????????????????????????????????????????????
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
     * ?????????????????????????????????????????????
     *
     * @param editable
     */
    @OnTextChanged(R.id.content)
    public void textChange(Editable editable) {
        int contentLength = editable.length();
        wordCount.setText("" + contentLength);
    }

    /**
     * ????????????????????????????????????
     */
    private void getLocationData() {

        if (isEditModel || !Config.isCanRecordLocation()) return;

        mLocationClient = new AMapLocationClient(getApplicationContext());
        mLocationClient.setLocationListener(mLocationListener);
        AMapLocationClientOption mLocationOption = new AMapLocationClientOption();
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.
                Hight_Accuracy);
        //???????????????????????????
        mLocationOption.setOnceLocation(true);
        //????????????3s???????????????????????????????????????
        mLocationOption.setOnceLocationLatest(true);
        mLocationOption.setInterval(1000 * 10);
        mLocationOption.setHttpTimeOut(10000);
        mLocationClient.setLocationOption(mLocationOption);
        mLocationClient.startLocation();
    }

    /**
     * ??????????????????
     */
    private void getWeatherData() {
        Log.e(TAG, "getWeatherData: "+Config.isCanRecordWeather());
        if (isEditModel || !Config.isCanRecordWeather()) return;
        WeatherListener weatherListener = new WeatherListener();
        HeConfig.init("HE1903211634591796",
                "34dfdf2d730f4b348bf0fec6afb7a100");

        HeConfig.switchToFreeServerNode();

        //???????????????????????????????????????????????????????????????????????????????????????????????????
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
     * ?????????????????????????????????????????????????????????
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
     * ????????????????????????
     */
    private void initTimePicker() {

        Calendar startDate = Calendar.getInstance();
        startDate.set(2000, 0, 1);
        Calendar endDate = Calendar.getInstance();
        if (Config.isCanRecordFutureDiary()) {
            endDate.set(2099,12,31);
        }

        timePickerView = new TimePickerBuilder(this, (selectedDate, view) -> {//??????????????????
            now = selectedDate;
            date.setText(new DateUtil(selectedDate.getTime()).getDateTimeStringTwo());
        })
                .setType(new boolean[]{true, true, true, true, true, false})// ??????????????????
                .setCancelText("??????")//??????????????????
                .setSubmitText("??????")//??????????????????
                .setTitleSize(20)//??????????????????
                .setTitleText("???????????????")//????????????
                .setTitleColor(Color.BLACK)//??????????????????
                .setSubmitColor(Color.BLACK)//????????????????????????
                .setCancelColor(Color.BLACK)//????????????????????????
                .setTitleBgColor(Color.WHITE)//?????????????????? Night mode
                .setBgColor(Color.WHITE)//?????????????????? Night mode
                .setLineSpacingMultiplier(1.5f)
                .setDate(Calendar.getInstance())
                .setRangDate(startDate, endDate)//???????????????????????????
                .setLabel("???", "???", "???", "???", "???", "")
                .isDialog(false)//??????????????????????????????
                .build();
    }

    /**
     * ?????????Tag????????????
     */
    private void initTagDialog() {
        List<Tag> tags = LitePal.findAll(Tag.class);
        ChooseTagAdapter chooseTagAdapter = new ChooseTagAdapter(tags, this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        tagDialog = new MaterialDialog.Builder(this)
                .adapter(chooseTagAdapter, linearLayoutManager)
                .title("???????????????")
                .build();
    }

    private void initWeatherDiaolog() {
        weatherDialog = new MaterialDialog.Builder(this)
                .title("??????")
                .positiveText("??????")
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
                .title("??????")
                .positiveColorRes(R.color.black)
                .dividerColorRes(R.color.black)
                .inputRangeRes(1, 40, R.color.red)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input("?????????????????????", null, false, (dialog, input) -> {
                    String location = input.toString();
                    diary.setLocation(location);
                    tvLocaltion.setText(location);
                })
                .positiveText("??????")
                .show();
    }


    /**
     * ????????????????????????
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
     * ??????????????????????????????
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
     * ??????????????????????????????
     *
     * @param requestCode
     * @param perms
     */
    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {

    }

    /**
     * ??????Tag?????????
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
     * ??????????????????????????????????????????????????????
     */
    private void back() {
        if (!content.getText().toString().trim().equals("")) {
            if (Config.isCanAutoSave()) {
                saveDiary();
            } else {
                new MaterialDialog.Builder(this)
                        .title("??????")
                        .positiveText("??????")
                        .negativeText("??????")
                        .content("???????????????????????????????????????")
                        .neutralText("??????")
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
     * ???????????????????????????????????????
     */
    class LocationListener implements AMapLocationListener {

        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {
            Log.e(TAG, "Location: " + aMapLocation.getAoiName());
            if (!"".equals(aMapLocation.getAoiName())) {
                tvLocaltion.setText(aMapLocation.getAoiName()); //?????????????????????
                diary.setLocation(aMapLocation.getAoiName());   //?????????????????????????????????
                //???????????????????????????gps??????
                LatLonPoint latLonPoint = LatLonUtil.toGPSPoint(aMapLocation.getLatitude(), aMapLocation.getLongitude());
                latLonPointString = latLonPoint.toString();
                getWeatherData();
            }
        }
    }

    /**
     * ????????????????????????
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
     * WriterDiaryActivity???????????????
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
