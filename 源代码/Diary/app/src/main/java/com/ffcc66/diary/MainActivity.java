package com.ffcc66.diary;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.ffcc66.diary.Index.IndexAdapter;
import com.ffcc66.diary.backup.BackupActivity;
import com.ffcc66.diary.base.BaseActivity;
import com.ffcc66.diary.base.Config;
import com.ffcc66.diary.bean.Diary;
import com.ffcc66.diary.diarydetail.DiaryDetailAcitvity;
import com.ffcc66.diary.export.ExportActivity;
import com.ffcc66.diary.setting.SettingActivity;
import com.ffcc66.diary.tag.TagActivity;
import com.ffcc66.diary.util.DateUtil;
import com.ffcc66.diary.writediary.WriteDiaryAvtivity;
import com.haibin.calendarview.Calendar;
import com.haibin.calendarview.CalendarLayout;
import com.haibin.calendarview.CalendarView;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener, CalendarView.OnYearChangeListener, CalendarView.OnCalendarSelectListener {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    @BindView(R.id.nav_view)
    NavigationView navigationView;
    @BindView(R.id.write_diary)
    FloatingActionButton writeDiary;
    @BindView(R.id.diary_recylerview)
    RecyclerView recyclerView;

    @BindView(R.id.tv_month_day)
    TextView mTextMonthDay;
    @BindView(R.id.tv_year)
    TextView mTextYear;
    @BindView(R.id.tv_lunar)
    TextView mTextLunar;
    @BindView(R.id.tv_current_day)
    TextView mTextCurrentDay;
    @BindView(R.id.calendarView)
    CalendarView mCalendarView;
    @BindView(R.id.rl_tool)
    RelativeLayout mRelativeTool;
    @BindView(R.id.calendarLayout)
    CalendarLayout mCalendarLayout;
    private int mYear;


    IndexAdapter indexAdapter;
    ArrayList<Diary> diaries = new ArrayList<>();

    private static final String TAG = "MainActivity";


    @Override
    public int initLayout() {
        return R.layout.activity_main;
    }

    @Override
    public void initView() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        //夜间模式开关
        Switch switchItem = navigationView.getMenu().findItem(R.id.nav_night_model).getActionView().findViewById(R.id.switch_night_model);
        switchItem.setChecked(Config.isNightModel());
        switchItem.setOnCheckedChangeListener((compoundButton, b) -> {
            drawerLayout.closeDrawers();
            swithNightModel(b);
            finish();
            startActivity(new Intent(this, this.getClass()));
            overridePendingTransition(0, 0);
        });

        //列表设置
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        indexAdapter = new IndexAdapter(diaries, this);
        recyclerView.setAdapter(indexAdapter);

        indexAdapter.setEmptyView(R.layout.empty, recyclerView);
        indexAdapter.setOnItemClickListener((adapter, view, position) ->
                DiaryDetailAcitvity.start(MainActivity.this, position, diaries));

        initCalendarView();

    }

    @Override
    public void initData() {
    }

    @Override
    protected void onResume() {
        super.onResume();
        getDiary("");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                getDiary(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                getDiary(newText);
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        String orderTipStr = Config.getOrder() == 0 ? "按时间顺序" : "按时间逆序";
        menu.findItem(R.id.order).setTitle(orderTipStr);
        String viewModelTipStr = Config.getViewModel() == 1 ? "小图模式" : "大图模式";
        menu.findItem(R.id.viewModel).setTitle(viewModelTipStr);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.order) {
            if (Config.getOrder() == 0) {
                Config.setOrder(1);
                item.setTitle("按时间逆序");
            } else {
                Config.setOrder(0);
                item.setTitle("按时间顺序");
            }
        } else if (id == R.id.viewModel) {
            if (Config.getViewModel() == 1) {
                Config.setViewModel(2);
                item.setTitle("小图模式");
            } else {
                Config.setViewModel(1);
                item.setTitle("大图模式");
            }

        }
        getDiary("");
        return true;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.nav_statistical) {
            startActivity(new Intent(this, StatisticalActivity.class));
        } else if (id == R.id.nav_classify) {
            startActivity(new Intent(this, TagActivity.class));
        } else if (id == R.id.nav_backup) {
            startActivity(new Intent(this, BackupActivity.class));
        } else if (id == R.id.nav_import_export) {
            startActivity(new Intent(this, ExportActivity.class));
        } else if (id == R.id.nav_setting) {
            startActivity(new Intent(this, SettingActivity.class));
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @OnClick(R.id.write_diary)
    public void writeDiary() {
        WriteDiaryAvtivity.start(this, false, 1);
    }


    /**
     * 获取日记的方法
     * @param keyWord 查询使用，一般情况则传入空字符串
     */
    public void getDiary(String keyWord) {
        String orderStr = Config.getOrder() == 0 ? "desc" : "asc";
        List<Diary> diaryList = null;
        if (keyWord.equals("")) {
            diaryList = LitePal.order("data " + orderStr).find(Diary.class);
        } else {
            diaryList = LitePal.where("content like ? or location like ?", "%" + keyWord + "%", "%" + keyWord + "%")
                    .order("data " + orderStr).find(Diary.class);
        }

        diaries.clear();
        diaries.addAll(diaryList);

        changeItemViewModel(Config.getViewModel());
        indexAdapter.notifyDataSetChanged();
        initCalendarSelected();
    }

    /**
     * 获取某一天的日记
     * @param calendar 要筛选的哪一天
     */
    private void getOneDayDiaries(Calendar calendar) {
        //这一天的0-24点之间的所有日记
        java.util.Calendar start = java.util.Calendar.getInstance();
        start.set(calendar.getYear(), calendar.getMonth() - 1, calendar.getDay(), 0, 0, 0);
        java.util.Calendar end = java.util.Calendar.getInstance();
        end.set(calendar.getYear(), calendar.getMonth() - 1, calendar.getDay(), 23, 59, 59);

        String whereStr = "data > " + start.getTime().getTime() + " and data < " + end.getTime().getTime();
        List<Diary> list = LitePal.where(whereStr).find(Diary.class);
        if (list.size() > 0) {
            diaries.clear();
            diaries.addAll(list);
            changeItemViewModel(Config.getViewModel());
            indexAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 改变日记的显示模式
     * @param viewModel
     */
    private void changeItemViewModel(int viewModel) {
        for (Diary diary : diaries) {
            diary.setItemType(viewModel);
        }
    }

    /**
     * 初始化日历的带标注项
     */
    private void initCalendarSelected() {

        Map<String, Calendar> map = new HashMap<>();
        for (Diary diary : diaries) {
            DateUtil dateUtil = new DateUtil(diary.getData());
            Log.e(TAG, "initData: " + getSchemeCalendar(dateUtil.getYear(), dateUtil.getMonth(), dateUtil.getDay(), 0xFFFFFFFF, "").toString());
            map.put(getSchemeCalendar(dateUtil.getYear(), dateUtil.getMonth(), dateUtil.getDay(), 0xFFFFFFFF, "").toString(),
                    getSchemeCalendar(dateUtil.getYear(), dateUtil.getMonth(), dateUtil.getDay(), 0xFFFFFFFF, ""));
        }
        mCalendarView.setSchemeDate(map);

    }

    /**
     * 初始化日历视图
     */
    private void initCalendarView() {
        mTextMonthDay.setOnClickListener(v -> {
            if (mCalendarView.getVisibility() == View.GONE) {
                TranslateAnimation mShowAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                        Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                        -1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
                mShowAction.setDuration(500);
                mCalendarView.startAnimation(mShowAction);
                mCalendarView.setVisibility(View.VISIBLE);
            } else {
                mCalendarView.setVisibility(View.GONE);
                mCalendarView.scrollToCurrent();
                mTextMonthDay.setText(mCalendarView.getCurMonth() + "月" + mCalendarView.getCurDay() + "日");
                getDiary("");
            }

        });
        findViewById(R.id.fl_current).setOnClickListener(v -> mCalendarView.scrollToCurrent());

        mCalendarView.setOnYearChangeListener(this);
        mCalendarView.setOnCalendarSelectListener(this);
        mTextYear.setText(String.valueOf(mCalendarView.getCurYear()));
        mYear = mCalendarView.getCurYear();
        mTextMonthDay.setText(mCalendarView.getCurMonth() + "月" + mCalendarView.getCurDay() + "日");
        mTextLunar.setText("今日");
        mTextCurrentDay.setText(String.valueOf(mCalendarView.getCurDay()));
    }

    /**
     * 日历年份变化监听
     * @param year
     */
    @Override
    public void onYearChange(int year) {
        mTextMonthDay.setText(String.valueOf(year));
    }

    /**
     * 日记超出范围监听
     * @param calendar
     */
    @Override
    public void onCalendarOutOfRange(Calendar calendar) {

    }

    /**
     * 日历选择监听
     * @param calendar
     * @param isClick
     */
    @Override
    public void onCalendarSelect(Calendar calendar, boolean isClick) {
        mTextLunar.setVisibility(View.VISIBLE);
        mTextYear.setVisibility(View.VISIBLE);
        mTextMonthDay.setText(calendar.getMonth() + "月" + calendar.getDay() + "日");
        mTextYear.setText(String.valueOf(calendar.getYear()));
        mTextLunar.setText(calendar.getLunar());
        mYear = calendar.getYear();
        if (isClick) {
            getOneDayDiaries(calendar);
        }

    }

    /**
     * 获取一个带标注的日历项
     * @param year
     * @param month
     * @param day
     * @param color
     * @param text
     * @return
     */
    @SuppressWarnings("all")
    private Calendar getSchemeCalendar(int year, int month, int day, int color, String text) {
        Calendar calendar = new Calendar();
        calendar.setYear(year);
        calendar.setMonth(month);
        calendar.setDay(day);
        calendar.setSchemeColor(color);//如果单独标记颜色、则会使用这个颜色
        calendar.setScheme(text);
        return calendar;
    }


}
