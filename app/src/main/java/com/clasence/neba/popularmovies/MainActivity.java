package com.clasence.neba.popularmovies;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.GridView;
import android.widget.ProgressBar;

import com.clasence.neba.popularmovies.models.MovieModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MovieAdapter.CustomClickListener,LoaderManager.LoaderCallbacks<String>{
    //progress bar
    private ProgressBar progressBar;

    //list to hold details of movies
    private ArrayList<MovieModel> movielist;

    //custom arrayadapter
    private MovieAdapter movieAdapter;

    private Uri.Builder builder;
    public static final String MOVIE_DETAILS = "movie_details";

    //define asynctask object to enable cancel
    private AsyncTask<URL,Void,String> getMoviesAsync=null;

    //define gridview
    private  GridView gridView;
    private static final String GRID_POSITION = "gridposition";
    private static final String ALL_MOVIES = "allmovies";

    private static final int MY_LOADER_ID= 30;

    private String errorMessage="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        progressBar = (ProgressBar) findViewById(R.id.pbload);

        gridView = (GridView) findViewById(R.id.gridview);


        //build URL using URI builder
        builder = new Uri.Builder();
        builder.scheme(getString(R.string.scheme))
                .authority(getString(R.string.base_url))
                .appendPath("3")
                .appendPath("movie")
                .appendPath("popular")
                .appendQueryParameter("api_key", getString(R.string.api_key));


        //restore movie data if already loaded

        if(savedInstanceState!=null && savedInstanceState.containsKey(ALL_MOVIES)){

            movielist =  savedInstanceState.getParcelableArrayList(ALL_MOVIES);
            movieAdapter = new MovieAdapter(MainActivity.this,movielist,this,null);
            gridView.setAdapter(movieAdapter);

            //retain the current position of the grid
            int gridPosition = savedInstanceState.getInt(GRID_POSITION);

            //tried
            //gridView.setSelectionFromTop(gridPosition,0);
            //but api support is from 21, any other ideas will be welcomed
            gridView.setSelection(gridPosition);

        }
        else {
             //create new instance of movielist and update adapter
            movielist= new ArrayList<MovieModel>();
            movieAdapter = new MovieAdapter(MainActivity.this,movielist,this,null);
            gridView.setAdapter(movieAdapter);

            // if there is network connection on device, get data for popular movies
            if (CheckInternetConnection.isNetworkAvailable(getApplicationContext())) {

                if(getSupportLoaderManager().getLoader(MY_LOADER_ID)==null){
                    getSupportLoaderManager().initLoader(MY_LOADER_ID, null, this).forceLoad();
                }else{
                    getSupportLoaderManager().restartLoader(MY_LOADER_ID, null, this).forceLoad();
                }
            } else {
                showDialog(getResources().getString(R.string.message), getResources().getString(R.string.noint), 1);
            }
        }


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //save parcelable movie list and the position of the current list item
        if(movielist.size()>0) {
            outState.putParcelableArrayList(ALL_MOVIES, movielist);
            outState.putInt(GRID_POSITION, gridView.getFirstVisiblePosition());
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.popular_movies) {
            builder = new Uri.Builder();
            builder.scheme(getString(R.string.scheme))
                    .authority(getString(R.string.base_url))
                    .appendPath("3")
                    .appendPath("movie")
                    .appendPath("popular")
                    .appendQueryParameter("api_key", getString(R.string.api_key));

            if (CheckInternetConnection.isNetworkAvailable(getApplicationContext())) {
                if(getSupportLoaderManager().getLoader(MY_LOADER_ID)==null){
                    getSupportLoaderManager().initLoader(MY_LOADER_ID, null, this).forceLoad();
                }else{
                    getSupportLoaderManager().restartLoader(MY_LOADER_ID, null, this).forceLoad();
                }
            } else {
                showDialog(getResources().getString(R.string.message), getResources().getString(R.string.noint), 1);
            }

            return true;
        }
        else   if (id == R.id.top_movies) {

            builder = new Uri.Builder();
            builder.scheme(getString(R.string.scheme))
                    .authority(getString(R.string.base_url))
                    .appendPath("3")
                    .appendPath("movie")
                    .appendPath("top_rated")
                    .appendQueryParameter("api_key", getString(R.string.api_key));

            if (CheckInternetConnection.isNetworkAvailable(getApplicationContext())) {
                if(getSupportLoaderManager().getLoader(MY_LOADER_ID)==null){
                    getSupportLoaderManager().initLoader(MY_LOADER_ID, null, this).forceLoad();
                }else{
                    getSupportLoaderManager().restartLoader(MY_LOADER_ID, null, this).forceLoad();
                }

            } else {
                showDialog(getResources().getString(R.string.message), getResources().getString(R.string.noint), 1);
            }
            return true;

        }
        else if (id == R.id.favorite) {
            Intent intent = new Intent(this,FavoriteActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCustomClick(int position) {
        //start new activity on poster click and pass the movie details via intent
        final MovieModel movieModel = movielist.get(position);
        Intent intent = new Intent(this,DetailActivity.class);
        intent.putExtra(MOVIE_DETAILS,movieModel);
        startActivity(intent);
    }

    @Override
    public Loader<String> onCreateLoader(int id, Bundle args) {
        return new AsyncTaskLoader<String>(MainActivity.this) {
            HttpURLConnection urlConnection;
            @Override
            protected void onStartLoading() {
                super.onStartLoading();
                progressBar.setVisibility(View.VISIBLE);
                movielist.clear();
                movieAdapter.notifyDataSetChanged();
            }

            @Override
            public String loadInBackground() {
                try {
                    String string_url = builder.build().toString();
                    URL  url = new URL(string_url);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setDoInput(true);
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setUseCaches(false);
                    //set timeouts to 10s
                    urlConnection.setConnectTimeout(10000);
                    urlConnection.setReadTimeout(10000);


                    InputStream is =new BufferedInputStream(urlConnection.getInputStream());

                    //handle url redirects
                    if (!url.getHost().equals(urlConnection.getURL().getHost())) {
                        throw new IOException(getString(R.string.url_redirect_done));
                    }
                    BufferedReader br = new BufferedReader(new InputStreamReader(is));
                    StringBuilder response = new StringBuilder();
                    for (String line; (line = br.readLine()) != null; ) response.append(line + "\n");
                    Log.i("response",response.toString());
                    return response.toString();
                } catch (SocketTimeoutException e) {
                    errorMessage = getString(R.string.connectiontimeout);
                    e.printStackTrace();
                }
                catch (IOException ee){
                    errorMessage = getString(R.string.unabletoconnect);
                }catch(Exception eee){

                }

                return null;
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String data) {

        //dismiss progress bar
        progressBar.setVisibility(View.GONE);

        //if no error message, movie data was gotten
        if(errorMessage.equalsIgnoreCase("")) {
            try {
                JSONObject jsonObject = new JSONObject(data);
                JSONArray results = new JSONArray(jsonObject.getString("results"));
                if (results.length() > 0) {

                    //get movie details
                    for (int i = 0; i < results.length(); i++) {
                        JSONObject result = results.getJSONObject(i);
                        String movieId = result.getString("id");
                        String movieTitle = result.getString("original_title");
                        String voteAverage = result.getString("vote_average");
                        String overview = result.getString("overview");
                        String releaseDate = result.getString("release_date");
                        String posterPath = result.getString("poster_path");
                        String popularity = result.getString("popularity");
                        String video = result.getString("video");
                        String voteCount = result.getString("vote_count");
                        MovieModel movieModel = new MovieModel("-1",movieId, movieTitle, voteAverage, overview, releaseDate, posterPath, popularity, video, voteCount);
                        movielist.add(movieModel);
                    }
                    movieAdapter.notifyDataSetChanged();

                }

            } catch (JSONException e) {
                showDialog(getString(R.string.message),getString(R.string.unabletoconnect),1);
            }

        }else{
            showDialog(getString(R.string.message),errorMessage,1);
            errorMessage="";
        }


     getSupportLoaderManager().destroyLoader(MY_LOADER_ID);
    }

    @Override
    public void onLoaderReset(Loader<String> loader) {

    }


    /**
     * Function shows a dismissable dialog to the user with important information
     * @param title {@link String} title of dialog
     * @param body {@link String} body of dialog
     * @param type int indicates the action to perform on dialog click
     * */

    private void showDialog(String title, String body, int type) {
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(body);
        alertDialog.setCancelable(true);

        //make type accessible to inner class
        final int ttype = type;
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getResources().getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getResources().getString(R.string.reload),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if(ttype==1) {
                            if (CheckInternetConnection.isNetworkAvailable(getApplicationContext())) {

                                if(getSupportLoaderManager().getLoader(MY_LOADER_ID)==null){
                                    getSupportLoaderManager().initLoader(MY_LOADER_ID, null, MainActivity.this).forceLoad();
                                }else{
                                    getSupportLoaderManager().restartLoader(MY_LOADER_ID, null, MainActivity.this).forceLoad();
                                }
                            } else {
                                showDialog(getResources().getString(R.string.message), getResources().getString(R.string.noint), 1);
                            }
                        }

                    }
                });
        alertDialog.show();
    }

}

