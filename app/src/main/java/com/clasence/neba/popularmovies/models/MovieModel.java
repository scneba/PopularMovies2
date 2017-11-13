package com.clasence.neba.popularmovies.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Neba
 * Class is a model for movie details. implements Parcelable so that it can be passed across activities
 */

public class MovieModel implements Parcelable {
    private String sqlId, movieId, movieTitle, voteAverage, overview, releaseDate, posterPath, popularity, video, voteCount;

    //constructor to get and set movie details
    public MovieModel(String sqlId,String movieId, String movieTitle, String voteAverage, String overview, String releaseDate, String posterPath, String popularity
            , String video, String voteCount) {
        this.sqlId=sqlId;
        this.movieId = movieId;
        this.movieTitle = movieTitle;
        this.voteAverage = voteAverage;
        this.overview = overview;
        this.releaseDate = releaseDate;
        this.posterPath = posterPath;
        this.popularity = popularity;
        this.video = video;
        this.voteCount = voteCount;
    }


    protected MovieModel(Parcel in) {
        sqlId = in.readString();
        movieId = in.readString();
        movieTitle = in.readString();
        voteAverage = in.readString();
        overview = in.readString();
        releaseDate = in.readString();
        posterPath = in.readString();
        popularity = in.readString();
        video = in.readString();
        voteCount = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(sqlId);
        dest.writeString(movieId);
        dest.writeString(movieTitle);
        dest.writeString(voteAverage);
        dest.writeString(overview);
        dest.writeString(releaseDate);
        dest.writeString(posterPath);
        dest.writeString(popularity);
        dest.writeString(video);
        dest.writeString(voteCount);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<MovieModel> CREATOR = new Creator<MovieModel>() {
        @Override
        public MovieModel createFromParcel(Parcel in) {
            return new MovieModel(in);
        }

        @Override
        public MovieModel[] newArray(int size) {
            return new MovieModel[size];
        }
    };

    //define getters for all moviemodel parameters
    public String getMovieId() {
        return movieId;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public String getOverview() {
        return overview;
    }

    public String getPopularity() {
        return popularity;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public String getVideo() {
        return video;
    }

    public String getVoteAverage() {
        return voteAverage;
    }

    public String getVoteCount() {
        return voteCount;
    }

    public String getSqlId() {
        return sqlId;
    }
}
