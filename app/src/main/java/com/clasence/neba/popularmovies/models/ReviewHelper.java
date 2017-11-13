package com.clasence.neba.popularmovies.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Neba
 */

public class ReviewHelper implements Parcelable{
    private String id,author,comment;
    public ReviewHelper(String id,String author,String comment){
        this.id=id;
        this.author=author;
        this.comment=comment;
    }

    protected ReviewHelper(Parcel in) {
        id = in.readString();
        author = in.readString();
        comment = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(author);
        dest.writeString(comment);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ReviewHelper> CREATOR = new Creator<ReviewHelper>() {
        @Override
        public ReviewHelper createFromParcel(Parcel in) {
            return new ReviewHelper(in);
        }

        @Override
        public ReviewHelper[] newArray(int size) {
            return new ReviewHelper[size];
        }
    };

    public String getAuthor() {
        return author;
    }

    public String getId() {
        return id;
    }

    public String getComment() {
        return comment;
    }
}
