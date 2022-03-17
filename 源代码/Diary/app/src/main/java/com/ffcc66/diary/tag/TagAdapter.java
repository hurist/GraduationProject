package com.ffcc66.diary.tag;

import android.content.Context;
import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.ffcc66.diary.R;
import com.ffcc66.diary.bean.Tag;

import java.util.List;

public class TagAdapter extends BaseQuickAdapter<Tag, BaseViewHolder> {

    private Context context;
    private TagOperationListener tagOperationListener;

    public TagAdapter(@Nullable List<Tag> data, Context context, TagOperationListener tagOperationListener) {
        super(R.layout.item_tag_activity, data);
        this.context = context;
        this.tagOperationListener = tagOperationListener;
    }

    @Override
    protected void convert(BaseViewHolder helper, Tag item) {
        helper.setText(R.id.tag_name, item.getName());
        helper.setText(R.id.tag_num, item.getDiaries().size()+" 篇");
        helper.getView(R.id.menu).setOnClickListener(view -> {
            tagOperationListener.operation(view, item);
        });
    }
}
