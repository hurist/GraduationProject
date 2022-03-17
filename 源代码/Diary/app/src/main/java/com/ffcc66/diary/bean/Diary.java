package com.ffcc66.diary.bean;

import com.chad.library.adapter.base.entity.MultiItemEntity;

import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

import java.io.Serializable;

public class Diary extends LitePalSupport implements Serializable,MultiItemEntity {
    private int  id;
    private int weather = 100;
    private long data;
    private String img = "";
    private String content;
    private String location = "";
    private Tag tag;
    @Column(ignore = true)
    public int itemType = SMALL_IMG;

    public static final int BIG_IMG = 1;
    public static final int SMALL_IMG = 2;

    public int getWeather() {
        return weather;
    }

    public void setWeather(int weather) {
        this.weather = weather;
    }

    public long getData() {
        return data;
    }

    public void setData(long data) {
        this.data = data;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Tag getTag() {
        return tag;
    }

    public void setTag(Tag tag) {
        this.tag = tag;
    }

    public void setItemType(int itemType) {
        this.itemType = itemType;
    }

    @Override
    public int getItemType() {
        return itemType;
    }
}
