package com.ffcc66.diary.diarydetail;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.ToxicBakery.viewpager.transforms.StackTransformer;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.ffcc66.diary.R;
import com.ffcc66.diary.base.BaseActivity;
import com.ffcc66.diary.base.Config;
import com.ffcc66.diary.bean.Diary;
import com.ffcc66.diary.writediary.WriteDiaryAvtivity;

import org.litepal.LitePal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import butterknife.BindView;

public class DiaryDetailAcitvity extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.viewpager)
    ViewPager viewPager;

    private DiaryDetailViewPagerAdapter diaryDetailViewPagerAdapter;
    private ArrayList<Diary> diaries = new ArrayList<>();
    private int currentPosition = 0;
    private Diary operationDiary;

    private static final String TAG = "DiaryDetailAcitvity";

    @Override
    public int initLayout() {
        return R.layout.activity_diary_acitvity;
    }

    @Override
    public void initView() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        diaryDetailViewPagerAdapter = new DiaryDetailViewPagerAdapter(this,diaries);
        toolbar.setNavigationOnClickListener(view -> finish());
        viewPager.setAdapter(diaryDetailViewPagerAdapter);
        viewPager.setPageTransformer(true, new StackTransformer());
        //获取当前显示的日记
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) { }
            @Override
            public void onPageSelected(int position) {
                Log.e(TAG, "onPageSelected: "+position);
                currentPosition = position;
                operationDiary = diaries.get(position);
                Log.e(TAG, "onPageSelected: "+operationDiary.getContent());
            }
            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
    }

    @Override
    public void initData() {
        Intent intent = getIntent();
        if (intent != null) {
            currentPosition = intent.getIntExtra("current", 0);
            List<Diary> diaryList = (List<Diary>) intent.getSerializableExtra("diaryList");
            diaries.addAll(diaryList);
            operationDiary = diaries.get(currentPosition);
            diaryDetailViewPagerAdapter.notifyDataSetChanged();
            viewPager.setCurrentItem(currentPosition, false);
        }
//        String order = Config.getOrder() == 0 ? "desc":"asc";
//        List<Diary> diaryList = LitePal.order("data "+order).find(Diary.class);
//        diaries.clear();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_diary, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.delete:
                deletDiary();
                break;
            case R.id.edit:
                editDiary();
                break;
        }
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("currentPosition", currentPosition);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //initData();
    }

    private void deletDiary() {

        if (diaries.size() == 0) {
            Toast.makeText(this, "已经没有日记了！",Toast.LENGTH_LONG).show();
            return;
        }
        new MaterialDialog.Builder(this)
                .title("确认删除该日记？")
                .content("该操作无法撤回")
                .positiveText("确认")
                .negativeText("取消")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        LitePal.delete(Diary.class, operationDiary.getId());
                        diaries.remove(currentPosition);
                        //viewpager打开第一页不会触发onSelected的事件，所以在删除第一页的时候需要手动获取一下日记
                        if (currentPosition == 0 && diaries.size() > 0) {
                            operationDiary = diaries.get(0);
                        }
                        diaryDetailViewPagerAdapter.notifyDataSetChanged();
                    }
                }).build().show();
    }

    private void editDiary() {
        if (diaries.size() == 0) {
            Toast.makeText(this, "已经没有日记了！",Toast.LENGTH_LONG).show();
            return;
        }
        WriteDiaryAvtivity.start(this, true, operationDiary.getId());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK && requestCode == 0) {
            Diary changedDiary = (Diary) data.getSerializableExtra("changedDiary");
            for (Diary diary:diaries){
                if (diary.getData() == changedDiary.getData()) {
                    diary.setContent(changedDiary.getContent());
                    diary.setWeather(changedDiary.getWeather());
                    diary.setImg(changedDiary.getImg());
                    diary.setLocation(changedDiary.getLocation());
                    diaryDetailViewPagerAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    public static void start(Activity activity, int current, List<Diary> diaryList) {
        Intent intent = new Intent(activity, DiaryDetailAcitvity.class);
        intent.putExtra("current",current);
        intent.putExtra("diaryList", (Serializable) diaryList);
        activity.startActivity(intent);
    }



}
