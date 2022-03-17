package com.ffcc66.diary.tag;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.ffcc66.diary.Index.IndexAdapter;
import com.ffcc66.diary.R;
import com.ffcc66.diary.base.BaseActivity;
import com.ffcc66.diary.bean.Diary;
import com.ffcc66.diary.bean.Tag;
import com.ffcc66.diary.diarydetail.DiaryDetailAcitvity;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

public class TagDetailActivity extends BaseActivity {

    @BindView(R.id.diary_recylerview)
    RecyclerView diaryRecylerview;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private IndexAdapter adapter;
    private List<Diary> diaries = new ArrayList<>();
    private int tagId;

    @Override
    public int initLayout() {
        return R.layout.activity_tag_detail;
    }

    @Override
    public void initView() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setNavigationOnClickListener(view -> finish());
        adapter = new IndexAdapter(diaries, this);
        diaryRecylerview.setLayoutManager(new LinearLayoutManager(this));
        diaryRecylerview.setAdapter(adapter);
        adapter.setEmptyView(R.layout.empty, diaryRecylerview);
        adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                DiaryDetailAcitvity.start(TagDetailActivity.this, position, diaries);
            }
        });
    }

    @Override
    public void initData() {
        Intent intent = getIntent();
        tagId = intent.getIntExtra("tagId",-1);
        if (tagId != -1) {
            getDiary();
        }

    }

    private void getDiary() {
        Tag tag = LitePal.where("id = ? ",tagId+"").findFirst(Tag.class);
        toolbar.setTitle(tag.getName());
        if (tag != null) {
            List<Diary> list = LitePal.where("tag_id = ?", tag.getId()+"").find(Diary.class);
            if (list.size() >= 0) {
                diaries.clear();
                diaries.addAll(list);
                adapter.notifyDataSetChanged();
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        getDiary();
    }

    public static void start(Context context, int tagId) {
        Intent intent = new Intent(context, TagDetailActivity.class);
        intent.putExtra("tagId", tagId);
        context.startActivity(intent);
    }
}
