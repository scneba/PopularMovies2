package com.clasence.neba.popularmovies.models;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Neba
 * Class is the movie contract defining the names of table and table columns
 */

public class MovieContract {

    //authority
    public static final String CONTENT_AUTHORITY = "com.clasence.neba.popularmovies";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_FAVORITE = "favorite";
    public static final String PATH_MOVIE_BY_ID = "favorite_by_movie_id";

    public static final class MovieEntry implements BaseColumns {

        /* The base CONTENT_URI used to query the movie table from the content provider */
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_FAVORITE)
                .build();

        public static final Uri GET_BY_MOVIE_ID_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_MOVIE_BY_ID)
                .build();

        //table name
        public static final String TABLE_NAME = "fav_movies";

        //define table columns matching with MovieModel
        public static final String COLUMN_MOVIE_ID = "movie_id";
        public static final String COLUMN_MOVIE_TITLE= "movie_title";
        public static final String COLUMN_VOTE_AVG = "vote_avg";
        public static final String COLUMN_OVERVIEW= "overview";
        public static final String COLUMN_RELEASE_DATE = "release_date";
        public static final String COLUMN_POSTER_PATH= "poster_path";
        public static final String COLUMN_POPULARITY = "popularity";
        public static final String COLUMN_VIDEO= "video";
        public static final String COLUMN_VOTE_COUNT= "vote_count";


        public static Uri buildUriWithMovieId(Integer movie_id) {
            return GET_BY_MOVIE_ID_URI.buildUpon()
                    .appendPath(Integer.toString(movie_id))
                    .build();
        }

        public static Uri buildUriWithMSqlId(Integer movie_id) {
            return CONTENT_URI.buildUpon()
                    .appendPath(Integer.toString(movie_id))
                    .build();
        }

    }


}
