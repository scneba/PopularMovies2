package com.clasence.neba.popularmovies.models;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.net.URI;

/**
 * Created by Neba
 */

public class MovieProvider extends ContentProvider {
    //define static codes
    public static final int CODE_FAVOURITE= 10;
    public static final int CODE_GET_MOVIE_BY_MOVIE_ID= 11;
    public static final int CODE_DELETE_BY_ID= 12;

    //create instance of urimatcher to be used in code
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MovieDBHelper movieDBHelper;


    /**
     * Creates the UriMatcher that will match each URI to the CODE_WEATHER and
     * CODE_WEATHER_WITH_DATE constants defined above.
     * <p>
     * It's possible you might be thinking, "Why create a UriMatcher when you can use regular
     * expressions instead? After all, we really just need to match some patterns, and we can
     * use regular expressions to do that right?" Because you're not crazy, that's why.
     * <p>
     * UriMatcher does all the hard work for you. You just have to tell it which code to match
     * with which URI, and it does the rest automagically. Remember, the best programmers try
     * to never reinvent the wheel. If there is a solution for a problem that exists and has
     * been tested and proven, you should almost always use it unless there is a compelling
     * reason not to.
     *
     * @return A UriMatcher that correctly matches the constants for CODE_WEATHER and CODE_WEATHER_WITH_DATE
     */
    public static UriMatcher buildUriMatcher() {

        /*
         * All paths added to the UriMatcher have a corresponding code to return when a match is
         * found. The code passed into the constructor of UriMatcher here represents the code to
         * return for the root URI. It's common to use NO_MATCH as the code for this case.
         */
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MovieContract.CONTENT_AUTHORITY;

        /*
         * For each type of URI you want to add, create a corresponding code. Preferably, these are
         * constant fields in your class so that you can use them throughout the class and you no
         * they aren't going to change. In Sunshine, we use CODE_WEATHER or CODE_WEATHER_WITH_DATE.
         */

        /* This URI is content://com.example.android.sunshine/weather/ */
        matcher.addURI(authority, MovieContract.PATH_FAVORITE,CODE_FAVOURITE);

        /*
         * This URI would look something like content://com.example.android.sunshine/weather/1472214172
         * The "/#" signifies to the UriMatcher that if PATH_WEATHER is followed by ANY number,
         * that it should return the CODE_WEATHER_WITH_DATE code
         */
        matcher.addURI(authority, MovieContract.PATH_MOVIE_BY_ID +"/#", CODE_GET_MOVIE_BY_MOVIE_ID);

        matcher.addURI(authority, MovieContract.PATH_FAVORITE+"/#",CODE_DELETE_BY_ID);

        return matcher;
    }


    @Override
    public boolean onCreate() {
       movieDBHelper = new MovieDBHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        Cursor cursor;
        switch (sUriMatcher.match(uri)) {
            case CODE_FAVOURITE:{
                cursor = movieDBHelper.getReadableDatabase().query(
                        MovieContract.MovieEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);

                break;
            }
            case CODE_GET_MOVIE_BY_MOVIE_ID:{
                String string_id = uri.getLastPathSegment();
                String[] selectionArguments = new String[]{string_id};

                cursor = movieDBHelper.getReadableDatabase().query(
                        MovieContract.MovieEntry.TABLE_NAME,
                        projection,
                        MovieContract.MovieEntry.COLUMN_MOVIE_ID+ " = ? ",
                        selectionArguments,
                        null,
                        null,
                        sortOrder);
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }


    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {

        final SQLiteDatabase db = movieDBHelper.getWritableDatabase();

        Uri returnUri;
        switch (sUriMatcher.match(uri)) {
            case CODE_FAVOURITE:{
               long id= db.insert(MovieContract.MovieEntry.TABLE_NAME,null,values);
                if(id>0){
                    returnUri = ContentUris.withAppendedId(MovieContract.MovieEntry.CONTENT_URI,id);
                }else {
                    throw new android.database.SQLException("Error trying to insert data to database");
                }
                break;

            }
            default: {
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        if (selection==null) selection = "1";

        int numRowsDeleted=-1;
        switch (sUriMatcher.match(uri)) {

            case CODE_FAVOURITE: {
                numRowsDeleted = movieDBHelper.getWritableDatabase().delete(
                        MovieContract.MovieEntry.TABLE_NAME,
                        selection,
                        selectionArgs);

                break;
            }
            case  CODE_DELETE_BY_ID:{

                String string_id = uri.getLastPathSegment();
                String[] selectionArguments = new String[]{string_id};
                numRowsDeleted = movieDBHelper.getWritableDatabase().delete(
                        MovieContract.MovieEntry.TABLE_NAME,
                        MovieContract.MovieEntry._ID+ " = ? ",
                        selectionArguments);
               break;

            }


            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        /* If we actually deleted any rows, notify that a change has occurred to this URI */
        if (numRowsDeleted > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return numRowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        final SQLiteDatabase db = movieDBHelper.getWritableDatabase();

        switch (sUriMatcher.match(uri)) {

            case CODE_FAVOURITE:
                db.beginTransaction();
                int rowsInserted = 0;
                try {
                    for (ContentValues value : values) {

                        long _id = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            rowsInserted++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                if (rowsInserted > 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }

                return rowsInserted;

            default:
                return super.bulkInsert(uri, values);
        }
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Override
    @TargetApi(11)
    public void shutdown() {
        movieDBHelper.close();
        super.shutdown();
    }
}
