package com.project.carousalrecycler.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ItemModel {

    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("realname")
    @Expose
    private String realname;
    @SerializedName("team")
    @Expose
    private String team;
    @SerializedName("firstappearance")
    @Expose
    private String firstappearance;
    @SerializedName("createdby")
    @Expose
    private String createdby;
    @SerializedName("publisher")
    @Expose
    private String publisher;
    @SerializedName("imageurl")
    @Expose
    private String imageurl;
    @SerializedName("bio")
    @Expose
    private String bio;

    public ItemModel() {
    }

    public String getName() {
        return name;
    }
    public String getImageurl() {
        return imageurl;
    }


}
