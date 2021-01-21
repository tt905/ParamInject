package com.mo.annotation.app;

import android.os.Parcel;
import android.os.Parcelable;


public class UserModel implements Parcelable {

    public String name = "username";

    public UserModel(){}

    public UserModel(String name){
        this.name = name;
    }

    protected UserModel(Parcel in) {
        name = in.readString();
    }

    public static final Creator<UserModel> CREATOR = new Creator<UserModel>() {
        @Override
        public UserModel createFromParcel(Parcel in) {
            return new UserModel(in);
        }

        @Override
        public UserModel[] newArray(int size) {
            return new UserModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
    }
}
