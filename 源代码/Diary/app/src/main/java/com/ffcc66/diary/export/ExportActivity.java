package com.ffcc66.diary.export;

import android.Manifest;
import android.graphics.Color;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bigkoo.pickerview.builder.TimePickerBuilder;
import com.bigkoo.pickerview.view.TimePickerView;
import com.ffcc66.diary.R;
import com.ffcc66.diary.base.BaseActivity;
import com.ffcc66.diary.base.BaseDisposableObserver;
import com.ffcc66.diary.base.Config;
import com.ffcc66.diary.bean.Diary;
import com.ffcc66.diary.util.DateUtil;
import com.ffcc66.diary.util.FileUtils;
import com.ffcc66.diary.util.PdfItextUtil;
import com.ffcc66.diary.util.WeatherUtil;
import com.itextpdf.text.DocumentException;

import org.litepal.LitePal;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.Action;
import butterknife.BindView;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import pub.devrel.easypermissions.EasyPermissions;

public class ExportActivity extends BaseActivity implements EasyPermissions.PermissionCallbacks{

    private final int SELECT_START_DATE = 0;
    private final int SELECT_END_DATE = 1;

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.et_start_date)
    EditText etStartDate;
    @BindView(R.id.et_end_date)
    EditText etEndDate;
    @BindView(R.id.btn_export_txt)
    AppCompatButton btnExportTXT;
    @BindView(R.id.btn_export_pdf)
    AppCompatButton btnExportPDF;

    private Date diaryStartDate;
    private Date diaryEndDate;
    private Date selectStartDate;
    private Date selectEndDate;

    private static final String TAG = "ExportActivity";
    private final int EXPORT_TXT = 1;
    private final int EXPORT_PDF = 2;
    final String DIARY_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Diary/";

    private final String[] perems = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private boolean hasPermission = false;
    private boolean hasDiary = true; //是否有日记可以导出

    private MaterialDialog waitDialog = null;


    @Override
    public int initLayout() {
        return R.layout.activity_export;
    }

    @Override
    public void initView() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(view -> finish());

        waitDialog = new MaterialDialog.Builder(this)
                .title("请稍等")
                .content("正在导出...")
                .progress(true, 0)
                .progressIndeterminateStyle(true).build();
    }

    @Override
    public void initData() {

        if (LitePal.findAll(Diary.class).size() == 0) {
            Toast.makeText(this, "没有日记可以导出",Toast.LENGTH_LONG).show();
            hasDiary = false;
        } else {

            diaryEndDate = new Date(LitePal.order("data desc").findFirst(Diary.class).getData());
            diaryStartDate = new Date(LitePal.order("data desc").findLast(Diary.class).getData());
        }

        etStartDate.setOnClickListener(view -> selectDate(SELECT_START_DATE));
        etEndDate.setOnClickListener(view -> selectDate(SELECT_END_DATE));
        btnExportTXT.setOnClickListener(view -> export(EXPORT_TXT));
        btnExportPDF.setOnClickListener(view -> export(EXPORT_PDF));
    }

    /**
     * 导出
     * @param exportType
     */
    private void export(int exportType) {

        if (!hasDiary) {
            Toast.makeText(this, "没有日记可以导出",Toast.LENGTH_LONG).show();
            return;
        }
        checkPermission();
        if (hasPermission) {
            waitDialog.show();
        }
        //创建observable 可观察对象
        //emitter 发射器
        Observable export = Observable.create(emitter -> {
            if (exportType == EXPORT_TXT) {
                if (!exportTXT(getDiaryList())) emitter.onNext(false);
                else emitter.onComplete();
            } else {
                if (!exportPDF(getDiaryList())) emitter.onNext(false);
                else emitter.onComplete();
            }

        });

        //创建一个观察者
        BaseDisposableObserver observer = new BaseDisposableObserver() {
            @Override
            public void onComplete() {
                waitDialog.dismiss();
                String msg = "文件已导出！保存在sdcard/Diary/ 目录下";
                Toast.makeText(ExportActivity.this, msg, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onNext(Object o) {
                Toast.makeText(ExportActivity.this, "缺少权限",Toast.LENGTH_LONG).show();
            }
        };

        export.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(observer);
        register(observer);

    }

    private List<Diary> getDiaryList() {
        String whereStr = "1 = 1";
        if (selectStartDate != null) {
            whereStr = whereStr + " and data > " + selectStartDate.getTime();
        }
        if (selectEndDate != null) {
            whereStr = whereStr + " and data < " + selectEndDate.getTime();
        }
        return LitePal.where(whereStr).find(Diary.class);
    }


    /**
     * 导出为PDF
     *
     * @param diaryList 数据
     */
    private boolean exportPDF(List<Diary> diaryList) {

        if (!hasPermission) {
            return false;
        }
        File diaryPDF = new File(DIARY_PATH + System.currentTimeMillis() + ".pdf");
        PdfItextUtil pdfItextUtil = null;

        try {

            diaryPDF.createNewFile();
            pdfItextUtil = new PdfItextUtil(diaryPDF.getAbsolutePath());

            for (Diary diary : diaryList) {

                DateUtil dateUtil = new DateUtil(diary.getData());
                String diaryTitle = dateUtil.getDateString() + "   " + dateUtil.getTimeString() + "   "
                        + WeatherUtil.getWeatherString(diary.getWeather(), this) + "   " + diary.getLocation();

                pdfItextUtil.addTitleToPdf(diaryTitle).addTextToPdf("\r\n" + diary.getContent() + "\r\n");

                if (!diary.getImg().equals("")) {
                    File file = new File(getExternalFilesDir("image").getAbsolutePath() + "/" + diary.getImg());
                    if (file.exists()) {
                        pdfItextUtil.addImageToPdfCenterH(file.getAbsolutePath());
                    }

                }
                pdfItextUtil.addTextToPdf("\r\n\r\n\r\n");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (pdfItextUtil != null)
                pdfItextUtil.close();
        }

        return true;

    }

    /**
     * 导出到TXT
     */
    private boolean exportTXT(List<Diary> diaryList) {
        if (!hasPermission) {
            return false;
        }
        File diaryTXT = new File(DIARY_PATH + System.currentTimeMillis() + ".txt");
        for (Diary diary : diaryList) {
            DateUtil dateUtil = new DateUtil(diary.getData());
            String diryTitle = dateUtil.getDateString() + " " + dateUtil.getTimeString() + " "
                    + WeatherUtil.getWeatherString(diary.getWeather(), this) + " " + diary.getLocation();
            Log.e(TAG, "exportTXT: "+diryTitle );
            FileUtils.writeDiaryToTXT(diaryTXT, diryTitle, this);
            FileUtils.writeDiaryToTXT(diaryTXT, diary.getContent() + "\r\n\r\n", this);
        }

        return true;

    }

    private void selectDate(int selectDateType) {
        if (!hasDiary) {
            Toast.makeText(this, "没有日记可以导出,无法选择",Toast.LENGTH_LONG).show();
            return;
        }

        //设置时间选择器的开始时间和结束时间
        Calendar startDate = Calendar.getInstance();
        Calendar endDate = Calendar.getInstance();

        if (selectDateType == SELECT_END_DATE) {
            if (selectStartDate != null) { //选择的结束时间不能比选择的开始时间早
                startDate.setTime(selectStartDate);
                endDate.setTime(diaryEndDate);
            } else {
                startDate.setTime(diaryStartDate);
                endDate.setTime(diaryEndDate);
            }
        } else {    //选择开始时间
            if (selectEndDate != null) { //选择的开始时间不能比选择的结束时间晚
                endDate.setTime(selectEndDate);
                startDate.setTime(diaryStartDate);
            } else {
                startDate.setTime(diaryStartDate);
                endDate.setTime(diaryEndDate);
            }
        }

        new TimePickerBuilder(this, (selectedDate, view) -> {//选中事件回调
            Calendar calendar = Calendar.getInstance();
            if (selectDateType == SELECT_END_DATE) {
                calendar.setTime(selectedDate);
                calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), 23, 59, 59);
                selectEndDate = calendar.getTime();
                etEndDate.setText(new DateUtil(selectEndDate.getTime()).getDateString());
            } else {
                calendar.setTime(selectedDate);
                calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
                selectStartDate = calendar.getTime();
                etStartDate.setText(new DateUtil(selectStartDate.getTime()).getDateString());
            }


        })
                .setType(new boolean[]{true, true, true, false, false, false})// 默认全部显示
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
                .setRangDate(startDate, endDate)
                .setLabel("年", "月", "日", "", "", "")
                .isDialog(false)//是否显示为对话框样式
                .build().show();
    }

    private void checkPermission() {

        if (!EasyPermissions.hasPermissions(this, perems)) {
            EasyPermissions.requestPermissions(this, "备份需要必要的权限，否则无法正常运行", 0, perems);
        } else {
            hasPermission = true;
        }

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

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        hasPermission = true;
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        hasPermission = false;
    }
}
