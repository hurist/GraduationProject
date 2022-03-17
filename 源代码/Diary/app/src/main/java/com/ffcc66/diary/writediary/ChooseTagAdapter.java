package com.ffcc66.diary.writediary;

import android.support.annotation.Nullable;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.ffcc66.diary.R;
import com.ffcc66.diary.bean.Tag;

import java.util.List;

public class ChooseTagAdapter extends BaseQuickAdapter<Tag, BaseViewHolder> {
    private ChooseTagListener chooseTagListener;

    public ChooseTagAdapter(@Nullable List<Tag> data, ChooseTagListener chooseTagListener) {
        super(R.layout.item_simple_tag, data);
        this.chooseTagListener = chooseTagListener;
    }

    @Override
    protected void convert(BaseViewHolder helper, Tag item) {
        helper.setText(R.id.tag_name, item.getName());
        helper.itemView.setOnClickListener(view -> chooseTagListener.onTagClick(item));
    }
}
