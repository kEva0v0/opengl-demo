package com.mashiro.uitest.bean;

import com.google.gson.annotations.SerializedName;

public class TestSerialize implements java.io.Serializable {
    private static final long serialVersionUID = 0L;

    public TestChildOne test;

    @SerializedName("test_two")
    public TestChildOne testTwo;
}
