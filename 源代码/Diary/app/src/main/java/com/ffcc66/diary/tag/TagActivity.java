package com.ffcc66.diary.tag;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.ffcc66.diary.R;
import com.ffcc66.diary.base.BaseActivity;
import com.ffcc66.diary.bean.Diary;
import com.ffcc66.diary.bean.Tag;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class TagActivity extends BaseActivity implements TagOperationListener {


    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.tag_recylerview)
    RecyclerView recyclerView;
    @BindView(R.id.add_tag)
    FloatingActionButton addTag;

    private ArrayList<Tag> tags = new ArrayList();
    private TagAdapter tagAdapter;

    PopupWindow operationPopupWindow;
    /**
     * 要操作的tag
     */
    Tag operationTag = null;

    private static final String TAG = "TagActivity";

    @Override
    public int initLayout() {
        return R.layout.activity_tag;
    }

    @Override
    public void initView() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        tagAdapter = new TagAdapter(tags, this,this);
        recyclerView.setAdapter(tagAdapter);
        toolbar.setNavigationOnClickListener(view -> finish());
        //初始化Tag操作菜单
        initTagOperationPopupWindow();

        tagAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                Tag tag = (Tag) adapter.getItem(position);
                TagDetailActivity.start(TagActivity.this, tag.getId());
            }
        });
    }

    @Override
    public void initData() {
        List<Tag> tagArrayList = LitePal.where("name != '默认'").order("id").find(Tag.class, true);
        if (tagArrayList != null && tagArrayList.size() >= 0) {
            tags.clear();
            tags.addAll(tagArrayList);
        }
        tagAdapter.notifyDataSetChanged();
    }

    @OnClick(R.id.add_tag)
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.add_tag:
                alert_add();
                break;
        }
    }

    /**
     * 添加Tag
     */
    public void alert_add(){
        new MaterialDialog.Builder(this)
                .title("新建标签")
                .positiveColorRes(R.color.black)
                .dividerColorRes(R.color.black)
                .inputRangeRes(1, 20, R.color.red)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input("请输入标签名称", null, false, (dialog, input) -> {
                    String tagName = input.toString();
                    List<Tag> tags = LitePal.where("name = ?",tagName).find(Tag.class);
                    if (tags.size() == 0) {
                        Tag tag = new Tag(tagName);
                        tag.save();
                        initData();
                    } else {
                        Toast.makeText(getApplicationContext(), "已存在"+tagName+"标签",
                                Toast.LENGTH_LONG).show();
                    }


                })
                .positiveText("确定")
                .show();
    }

    /**
     * 初始化Tag操作菜单
     */
    private void initTagOperationPopupWindow(){

        View contentView = LayoutInflater.from(this).inflate(R.layout.menu_tag_operation, null);
        operationPopupWindow = new PopupWindow(contentView, ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        operationPopupWindow.setTouchable(true);
        operationPopupWindow.setFocusable(true);
        operationPopupWindow.setOutsideTouchable(true);
        operationPopupWindow.setBackgroundDrawable(null);

        TextView rename = contentView.findViewById(R.id.rename_tag);
        rename.setOnClickListener(view -> {operationPopupWindow.dismiss(); renameTag(operationTag);});
        TextView delete = contentView.findViewById(R.id.delete_tag);
        delete.setOnClickListener(view -> {operationPopupWindow.dismiss(); deleteTag(operationTag); });

    }

    /**
     * 显示Tag操作弹窗
     * @param tagview
     * @param tag
     */
    private void showTagPopupWindow(View tagview, Tag tag){
        operationTag = tag;
        operationPopupWindow.showAsDropDown(tagview);
    }

    /**
     * 重命名Tag
     * @param tag
     */
    private void renameTag(Tag tag) {
        new MaterialDialog.Builder(this)
                .title("修改标签名称")
                .positiveColorRes(R.color.black)
                .dividerColorRes(R.color.black)
                .inputRangeRes(1, 20, R.color.red)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .positiveText("确定")
                .negativeText("取消")
                .input(null, tag.getName(), ((dialog, input) -> {
                    String tagName = input.toString();
                    //如果输入的名称与原来一致
                    if (tagName.equals(tag.getName())) {
                        dialog.dismiss();
                    } else {
                        //查找是否有新输入名称一致的标签
                        List<Tag> tags = LitePal.where("name = ?",tagName).find(Tag.class);
                        if (tags.size() == 0) {
                            tag.setName(tagName);
                            tag.update(tag.getId());
                            initData();
                        } else {
                            Toast.makeText(getApplicationContext(), "已存在"+tagName+"标签",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                }))
                .build().show();

    }

    /**
     * 删除Tag
     * @param tag
     */
    private void deleteTag(Tag tag) {
        new MaterialDialog.Builder(this)
                .title("删除标签")
                .content("删除该标签将会使该标签下的所有日记标签重置为默认")
                .positiveText("确认")
                .negativeText("取消")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                        if (tag.getName().equals("默认")) {
                            Toast.makeText(TagActivity.this, "默认标签不能删除",Toast.LENGTH_LONG).show();
                        } else {

                            ArrayList<Diary> diaryArrayList = tag.getDiaries();
                            Tag defaultTag = LitePal.find(Tag.class, 1);
                            //将该标签下的所有日记标签恢复默认
                            for (Diary diary : diaryArrayList) {
                                diary.setTag(defaultTag);
                                diary.save();
                            }
                            tag.delete();
                            initData();
                        }
                    }
                })
                .show();
    }

    /**
     * Tag菜单按钮的回调，在相应的Tag的item下显示操作弹窗
     * @param view
     * @param tag
     */
    @Override
    public void operation(View view, Tag tag) {
        if (operationPopupWindow!=null && operationPopupWindow.isShowing()) {
            operationPopupWindow.dismiss();
        } else {
            showTagPopupWindow(view,tag);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        initData();
    }
}
