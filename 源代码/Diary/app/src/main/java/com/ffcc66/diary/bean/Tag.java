package com.ffcc66.diary.bean;

import org.litepal.crud.LitePalSupport;

import java.io.Serializable;
import java.util.ArrayList;

public class Tag extends LitePalSupport implements Serializable {
    private int id;
    private String name;

    ArrayList<Diary> diaries = new ArrayList<>();

    public Tag() {}

    public Tag(String tagName) {
        this.name = tagName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<Diary> getDiaries() {
        return diaries;
    }

    public void setDiaries(ArrayList<Diary> diaries) {
        this.diaries = diaries;
    }
}
