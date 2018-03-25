package com.lzx.musiclibrary.aidl.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 其他信息
 * @author lzx
 * @date 2018/2/11
 */

public class TempInfo implements Parcelable {
    //提供9个临时字段：上面字段不够用的话，可以用临时字段
    private String temp_1 = ""; //临时字段
    private String temp_2 = ""; //临时字段
    private String temp_3 = ""; //临时字段
    private String temp_4 = ""; //临时字段
    private String temp_5 = ""; //临时字段
    private String temp_6 = ""; //临时字段
    private String temp_7 = ""; //临时字段
    private String temp_8 = ""; //临时字段
    private String temp_9 = ""; //临时字段

    public String getTemp_1() {
        return temp_1;
    }

    public void setTemp_1(String temp_1) {
        this.temp_1 = temp_1;
    }

    public String getTemp_2() {
        return temp_2;
    }

    public void setTemp_2(String temp_2) {
        this.temp_2 = temp_2;
    }

    public String getTemp_3() {
        return temp_3;
    }

    public void setTemp_3(String temp_3) {
        this.temp_3 = temp_3;
    }

    public String getTemp_4() {
        return temp_4;
    }

    public void setTemp_4(String temp_4) {
        this.temp_4 = temp_4;
    }

    public String getTemp_5() {
        return temp_5;
    }

    public void setTemp_5(String temp_5) {
        this.temp_5 = temp_5;
    }

    public String getTemp_6() {
        return temp_6;
    }

    public void setTemp_6(String temp_6) {
        this.temp_6 = temp_6;
    }

    public String getTemp_7() {
        return temp_7;
    }

    public void setTemp_7(String temp_7) {
        this.temp_7 = temp_7;
    }

    public String getTemp_8() {
        return temp_8;
    }

    public void setTemp_8(String temp_8) {
        this.temp_8 = temp_8;
    }

    public String getTemp_9() {
        return temp_9;
    }

    public void setTemp_9(String temp_9) {
        this.temp_9 = temp_9;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.temp_1);
        dest.writeString(this.temp_2);
        dest.writeString(this.temp_3);
        dest.writeString(this.temp_4);
        dest.writeString(this.temp_5);
        dest.writeString(this.temp_6);
        dest.writeString(this.temp_7);
        dest.writeString(this.temp_8);
        dest.writeString(this.temp_9);
    }

    public TempInfo() {
    }

    protected TempInfo(Parcel in) {
        this.temp_1 = in.readString();
        this.temp_2 = in.readString();
        this.temp_3 = in.readString();
        this.temp_4 = in.readString();
        this.temp_5 = in.readString();
        this.temp_6 = in.readString();
        this.temp_7 = in.readString();
        this.temp_8 = in.readString();
        this.temp_9 = in.readString();
    }

    public static final Parcelable.Creator<TempInfo> CREATOR = new Parcelable.Creator<TempInfo>() {
        @Override
        public TempInfo createFromParcel(Parcel source) {
            return new TempInfo(source);
        }

        @Override
        public TempInfo[] newArray(int size) {
            return new TempInfo[size];
        }
    };
}
