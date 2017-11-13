package com.clasence.neba.popularmovies;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.Toast;

import com.clasence.neba.popularmovies.models.MovieContract;
import com.clasence.neba.popularmovies.models.MovieModel;

import java.util.ArrayList;

/**
 * Created by Neba.
 */

public class FavoriteActivity extends AppCompatActivity implements  LoaderManager.LoaderCallbacks<Cursor>,
        MovieAdapter.CustomClickListener,MovieAdapter.CustomLongClickListener {

    private final int LOADER_ID = 10;
    private ArrayList<MovieModel> movielist;

    //custom arrayadapter
    private MovieAdapter movieAdapter;



    //define gridview
    private GridView gridView;
    private static final String GRID_POSITION = "gridposition";
    private static final String FAV_MOVIES = "allmovies";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.favorite_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        gridView = (GridView) findViewById(R.id.gridview);



        if(savedInstanceState!=null && savedInstanceState.containsKey(FAV_MOVIES)){

            movielist =  savedInstanceState.getParcelableArrayList(FAV_MOVIES);
            movieAdapter = new MovieAdapter(FavoriteActivity.this,movielist,this,null);
            gridView.setAdapter(movieAdapter);

            //retain the current position of the grid
            int gridPosition = savedInstanceState.getInt(GRID_POSITION);

            //tried
            //gridView.setSelectionFromTop(gridPosition,0);
            //but api support is from 21, any other ideas will be welcomed
            gridView.setSelection(gridPosition);

        }else{
            movielist =  new ArrayList<>();
            movieAdapter = new MovieAdapter(FavoriteActivity.this,movielist,this,null);
            gridView.setAdapter(movieAdapter);

            if(getSupportLoaderManager().getLoader(LOADER_ID)==null){
                getSupportLoaderManager().initLoader(LOADER_ID, null, this).forceLoad();
            }else{
                getSupportLoaderManager().restartLoader(LOADER_ID, null, this).forceLoad();
            }
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            //make sure clicking on back button takes user back
            case android.R.id.home: {
                onBackPressed();
                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //save parcelable movie list and the position of the current list item
        if(movielist.size()>0) {
            outState.putParcelableArrayList(FAV_MOVIES, movielist);
            outState.putInt(GRID_POSITION, gridView.getFirstVisiblePosition());
        }

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = MovieContract.MovieEntry.CONTENT_URI;
        return new CursorLoader(this,
                uri,
                null,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        boolean cursorHasData = false;

        if (data != null) {
            while(data.moveToNext()){
                Log.e("data at zero", data.getString(data.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_TITLE)));
                String Id = Integer.toString(data.getInt(data.getColumnIndex(MovieContract.MovieEntry._ID)));
                String movieId = Integer.toString(data.getInt(data.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_ID)));
                String movieTitle = data.getString(data.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_TITLE));
                String voteAverage = data.getString(data.getColumnIndex(MovieContract.MovieEntry.COLUMN_VOTE_AVG));
                String overview = data.getString(data.getColumnIndex(MovieContract.MovieEntry.COLUMN_OVERVIEW));
                String releaseDate = data.getString(data.getColumnIndex(MovieContract.MovieEntry.COLUMN_RELEASE_DATE));
                String posterPath = data.getString(data.getColumnIndex(MovieContract.MovieEntry.COLUMN_POSTER_PATH));
                String popularity = data.getString(data.getColumnIndex(MovieContract.MovieEntry.COLUMN_POPULARITY));
                String video = data.getString(data.getColumnIndex(MovieContract.MovieEntry.COLUMN_VIDEO));
                String voteCount = data.getString(data.getColumnIndex(MovieContract.MovieEntry.COLUMN_VOTE_COUNT));
                MovieModel movieModel = new MovieModel(Id, movieId, movieTitle, voteAverage, overview, releaseDate, posterPath, popularity, video, voteCount);
                movielist.add(movieModel);

            }
            movieAdapter.notifyDataSetChanged();
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.nofavorite), Toast.LENGTH_LONG).show();
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onCustomClick(int position) {

        //start new activity on poster click and pass the movie details via intent
        final MovieModel movieModel = movielist.get(position);
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(MainActivity.MOVIE_DETAILS, movieModel);
        startActivity(intent);
    }


    @Override
    public void onCustomLongClick(int position) {
        showDeleteDialog(position);
    }



    /**
     * Show single choice selection dialog
     * @param position int position of gridview item
     **/
    public  void showDeleteDialog(final int position){
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(FavoriteActivity.this,R.style.MyAlertDialogStyle);
        builderSingle.setCancelable(true);

        builderSingle.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

         final ArrayAdapter arrayAdapter = new ArrayAdapter<String>(FavoriteActivity.this, android.R.layout.select_dialog_singlechoice);
        arrayAdapter.add(getResources().getString(R.string.remove_fav));
        builderSingle.setAdapter(
                arrayAdapter,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                      if(which==0){
                          Uri movieUri = MovieContract.MovieEntry.buildUriWithMSqlId(Integer.parseInt(movielist.get(position).getSqlId()));
                          getContentResolver().delete(movieUri,null,null);
                      }
                    }
                });
        builderSingle.show();
    }
}
