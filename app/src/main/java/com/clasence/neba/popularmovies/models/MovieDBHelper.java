package com.clasence.neba.popularmovies.models;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.clasence.neba.popularmovies.models.MovieContract.MovieEntry;

/**
 * Created by Neba
 */

public class MovieDBHelper extends SQLiteOpenHelper {


    public static final String DATABASE_NAME = "movies.db";
    public static final int DATABASE_VERSION = 1;

    public MovieDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        final String SQL_CREATE_MOVIE_TABLE =

                "CREATE TABLE " + MovieEntry.TABLE_NAME + " (" +

                /*
                 * WeatherEntry did not explicitly declare a column called "_ID". However,
                 * WeatherEntry implements the interface, "BaseColumns", which does have a field
                 * named "_ID". We use that here to designate our table's primary key.
                 */
                        MovieEntry._ID               + " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                        MovieEntry.COLUMN_MOVIE_ID       + " INTEGER NOT NULL, "                 +

                        MovieEntry.COLUMN_MOVIE_TITLE + " TEXT NOT NULL,"                  +

                        MovieEntry.COLUMN_OVERVIEW   + " TEXT NOT NULL, "                    +

                        MovieEntry.COLUMN_RELEASE_DATE   + " TEXT NOT NULL, "                    +

                        MovieEntry.COLUMN_POPULARITY   + " TEXT NOT NULL, "                    +

                        MovieEntry.COLUMN_POSTER_PATH   + " TEXT NOT NULL, "                    +
                        MovieEntry.COLUMN_VIDEO   + " TEXT NOT NULL, "                    +

                        MovieEntry.COLUMN_VOTE_COUNT + " TEXT NOT NULL, "                    +
                        MovieEntry.COLUMN_VOTE_AVG    + " TEXT NOT NULL, "                    +

                        /**
                         * make movie ID unique*/
                        " UNIQUE (" + MovieEntry.COLUMN_MOVIE_ID + ") ON CONFLICT REPLACE);";

        db.execSQL(SQL_CREATE_MOVIE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
  //drop and recreate table for now
        db.execSQL("DROP TABLE IF EXISTS " + MovieEntry.TABLE_NAME);
        onCreate(db);
    }
}
