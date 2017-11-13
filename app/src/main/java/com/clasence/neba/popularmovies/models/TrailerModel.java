package com.clasence.neba.popularmovies.models;


import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Neba
 */

public class TrailerModel implements Parcelable {
    private String id,key, name,site, size;
    public TrailerModel(String id,String key,String name, String site,String size){
        this.id=id;
        this.key=key;
        this.name=name;
        this.site=site;
        this.size=size;
    }

    protected TrailerModel(Parcel in) {
        id = in.readString();
        key = in.readString();
        name = in.readString();
        site = in.readString();
        size = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(key);
        dest.writeString(name);
        dest.writeString(site);
        dest.writeString(size);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<TrailerModel> CREATOR = new Creator<TrailerModel>() {
        @Override
        public TrailerModel createFromParcel(Parcel in) {
            return new TrailerModel(in);
        }

        @Override
        public TrailerModel[] newArray(int size) {
            return new TrailerModel[size];
        }
    };

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSite() {
        return site;
    }

    public String getKey() {
        return key;
    }
}
