package com.tunjos.searchbubble.models;

import java.util.Date;

import io.realm.RealmObject;

/**
 * Created by tunjos on 22/06/2015.
 */
public class Clip extends RealmObject{
    private int id;
    private String text;
    private int type;
    private Date creationDate;
    private boolean favourite;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public boolean isFavourite() {
        return favourite;
    }

    public void setFavourite(boolean favourite) {
        this.favourite = favourite;
    }
}