package com.clasence.neba.popularmovies;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ListViewCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.clasence.neba.popularmovies.models.MovieContract;
import com.clasence.neba.popularmovies.models.MovieModel;
import com.clasence.neba.popularmovies.models.TrailerModel;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by Neba.
 */

public class DetailActivity extends AppCompatActivity implements View.OnClickListener {

    //define imageview and textviews that will be used  to display movie details
    private ImageView imageView;
    private TextView tvMovieTitle, tvReleaseDate,tvRating,tvSynopsis;
    private boolean isInFav=false;

    private final String TAG = MainActivity.class.getSimpleName();
    private final int CURSOR_LOADER_ID=10;
    private final int ASYNC_LOADER_ID=11;

    private Button addToFavouriteButton, viewCommentsButton;

    private static final String TRAILER_LIST = "trailer_list";
    private static final String LIST_POSITION = "list_position";

    //instance of movie data
    private  MovieModel movieModel;
    private static int _ID;

    //holds the Uri to the current movie
    private Uri movieUri=null;

   //uri builder for asynctask loading
    private Uri.Builder builder;

    //list to hold movie trailers
    private ArrayList<TrailerModel> trailerList;

    private ProgressBar progressBar;

    private LinearLayout linearLayout;

    public static final String MOVIE_ID= "movie_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //get views
        imageView= (ImageView) findViewById(R.id.imagePoster);
        tvMovieTitle = (TextView) findViewById(R.id.movietitle);
        tvReleaseDate = (TextView) findViewById(R.id.release_date);
        tvRating = (TextView) findViewById(R.id.rating);
        tvSynopsis = (TextView) findViewById(R.id.synopsis);
        addToFavouriteButton = (Button) findViewById(R.id.btnAddToFavourite);
        viewCommentsButton = (Button) findViewById(R.id.btnViewComments);
        addToFavouriteButton.setOnClickListener(this);
        viewCommentsButton.setOnClickListener(this);

        //get movie details from  intent
        movieModel = getIntent().getExtras().getParcelable(MainActivity.MOVIE_DETAILS);

        //extract parameters and set to appropriate view element
        String posterImageName = movieModel.getPosterPath();
        String  posterPath = getString(R.string.image_base_url)+posterImageName;
        Picasso.with(DetailActivity.this).load(posterPath).fit().into(imageView);
        tvMovieTitle.setText(movieModel.getMovieTitle());
        tvReleaseDate.setText(movieModel.getReleaseDate());
        tvRating.setText(movieModel.getVoteAverage()+"/10");
        tvSynopsis.setText(movieModel.getOverview());

        progressBar = (ProgressBar) findViewById(R.id.trailerProgress);
        linearLayout= (LinearLayout) findViewById(R.id.trailerListView);





        //build uri for asynctask loading
        builder = new Uri.Builder();
        builder.scheme(getString(R.string.scheme))
                .authority(getString(R.string.base_url))
                .appendPath("3")
                .appendPath("movie")
                .appendPath(movieModel.getMovieId())
                .appendPath("videos")
                .appendQueryParameter("api_key", getString(R.string.api_key));

        //start or restart cursor loader
        if(getSupportLoaderManager().getLoader(CURSOR_LOADER_ID)==null) {
            getSupportLoaderManager().initLoader(CURSOR_LOADER_ID, null, new LoadMovieDetails()).forceLoad();
        }else{
            getSupportLoaderManager().restartLoader(CURSOR_LOADER_ID, null, new LoadMovieDetails()).forceLoad();
        }



        //restore state if there is a saved state or state loader acoordingly
        if(savedInstanceState!=null && savedInstanceState.containsKey(TRAILER_LIST)) {

            trailerList = savedInstanceState.getParcelableArrayList(TRAILER_LIST);
            for(TrailerModel trailerModel:trailerList){
                fillLayout(linearLayout,trailerModel);
            }

            viewCommentsButton.setVisibility(View.VISIBLE);

        }else{

            if (CheckInternetConnection.isNetworkAvailable(getApplicationContext())) {

                trailerList = new ArrayList<>();

                if(getSupportLoaderManager().getLoader(ASYNC_LOADER_ID)==null){
                    getSupportLoaderManager().initLoader(ASYNC_LOADER_ID, null, new LoadTrailers()).forceLoad();
                }else{
                    getSupportLoaderManager().restartLoader(ASYNC_LOADER_ID, null, new LoadTrailers()).forceLoad();
                }
            }else{
                //NO INTERNET
            }
        }

    }


    /**
     * Dynamically add items to a linearlayout
     * @param root {@link LinearLayout}
     * @param trailerModel {@link TrailerModel}
     */
    public void fillLayout(LinearLayout root, final TrailerModel trailerModel){
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(Toolbar.LayoutParams.MATCH_PARENT,Toolbar.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER_VERTICAL;
        params.setMargins(0,4,0,0);

        TextView textView = new TextView(this);
        textView.setLayoutParams(params);
        textView.setGravity(Gravity.LEFT);
        textView.setPadding(10,10,10,10);
        Drawable img =getResources().getDrawable(android.R.drawable.ic_menu_slideshow );
        textView.setCompoundDrawablesWithIntrinsicBounds( img, null, null, null);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                textView.setBackground( getResources().getDrawable(R.drawable.textview_selector));
            }else{
                textView.setBackgroundDrawable( getResources().getDrawable(R.drawable.textview_selector) );

            }

        textView.setText(trailerModel.getName());
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(trailerModel.getSite().trim().equalsIgnoreCase("youtube")){
                    String url = getString(R.string.youtube_base_url)+trailerModel.getKey();
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
                }
            }
        });
        root.addView(textView);
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //save parcelable movie list and the position of the current list item
        if(trailerList.size()>0) {
            outState.putParcelableArrayList(TRAILER_LIST, trailerList);
        }

    }
    @Override
    public void onBackPressed() {
        //let android handle back press
        super.onBackPressed();
        //destroy activity
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            //make sure clicking on back button takes user back
            case android.R.id.home: {
                onBackPressed();
                return true;
            }
            default:{
                return super.onOptionsItemSelected(item);
            }
        }
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnAddToFavourite:{
                if(!isInFav) {
                    ContentValues values = new ContentValues();
                    values.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, Integer.parseInt(movieModel.getMovieId()));
                    values.put(MovieContract.MovieEntry.COLUMN_MOVIE_TITLE,movieModel.getMovieTitle());
                    values.put(MovieContract.MovieEntry.COLUMN_OVERVIEW,movieModel.getOverview());
                    values.put(MovieContract.MovieEntry.COLUMN_POPULARITY,movieModel.getPopularity());
                    values.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE,movieModel.getReleaseDate());
                    values.put(MovieContract.MovieEntry.COLUMN_POSTER_PATH,movieModel.getPosterPath());
                    values.put(MovieContract.MovieEntry.COLUMN_VIDEO,movieModel.getVideo());
                    values.put(MovieContract.MovieEntry.COLUMN_VOTE_COUNT,movieModel.getVoteCount());
                    values.put(MovieContract.MovieEntry.COLUMN_VOTE_AVG,movieModel.getVoteAverage());

                    try {
                        movieUri = getApplicationContext().getContentResolver().insert(MovieContract.MovieEntry.CONTENT_URI, values);

                        if(movieUri!=null) {
                            Toast.makeText(DetailActivity.this, getString(R.string.successfav), Toast.LENGTH_SHORT).show();
                            addToFavouriteButton.setText(R.string.remove_fav);
                            isInFav = true;
                        }

                    }catch (SQLException exception){
                        Toast.makeText(DetailActivity.this,getString(R.string.unableToInsert),Toast.LENGTH_SHORT);
                    }
                }else{
                    try{
                        int deletedNumber = getApplicationContext().getContentResolver().delete(movieUri,null,null);
                        if(deletedNumber==1){
                            addToFavouriteButton.setText(R.string.add_to_favourite);
                            Toast.makeText(DetailActivity.this, getString(R.string.removedfav), Toast.LENGTH_SHORT).show();
                            isInFav = false;
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                }

                break;
            }
            case R.id.btnViewComments:{
                final String movieId = movieModel.getMovieId();
                Intent intent = new Intent(this,CommentsActivity.class);
                intent.putExtra(MOVIE_ID,movieId);
                startActivity(intent);
            }
        }
    }

    /**
     * private class to perform loading of movie details
    */
    private class LoadMovieDetails  implements  LoaderManager.LoaderCallbacks<Cursor>{

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            switch (id){
                case CURSOR_LOADER_ID: {
                    Uri uri = MovieContract.MovieEntry.buildUriWithMovieId(Integer.parseInt(movieModel.getMovieId()));
                    return new CursorLoader(DetailActivity.this,
                            uri,
                            null,
                            null,
                            null,
                            null);
                }

                default:
                    throw new RuntimeException("Loader Not Implemented: " + id);
            }
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

            boolean cursorHasData = false;
            if (data != null && data.moveToFirst()) {
                isInFav=true;
                addToFavouriteButton.setText(getString(R.string.remove_fav));

                //build movie uri from cursor
                movieUri = MovieContract.MovieEntry.buildUriWithMSqlId(new Integer(data.getInt(data.getColumnIndex(MovieContract.MovieEntry._ID))));
            }else{
                isInFav=false;
                addToFavouriteButton.setText(getString(R.string.add_to_favourite));
            }

        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    }

    /**
     * Class to load trailers from server*/
    private class LoadTrailers implements LoaderManager.LoaderCallbacks<String>{

        private HttpURLConnection urlConnection;
        private String errorMessage="";
        private int responseCode=-1;

        @Override
        public Loader<String> onCreateLoader(int id, Bundle args) {
            return new AsyncTaskLoader<String>(DetailActivity.this) {
                @Override
                protected void onStartLoading() {
                    super.onStartLoading();
                    trailerList.clear();
                    progressBar.setVisibility(View.VISIBLE);
                    viewCommentsButton.setVisibility(View.GONE);
                }

                @Override
                public String loadInBackground() {


                    try {
                        String string_url = builder.build().toString();
                        URL url =new URL(string_url);
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
                        responseCode=urlConnection.getResponseCode();
                        return response.toString();
                    } catch (SocketTimeoutException e) {
                        errorMessage = getString(R.string.unable_trailers);
                        e.printStackTrace();
                    }
                    catch (IOException ee){
                        errorMessage = getString(R.string.unabletoconnect);
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }

                    return null;
                }

                @Override
                protected void onStopLoading() {
                    super.onStopLoading();
                }
            };
        }

        @Override
        public void onLoadFinished(Loader<String> loader, String data) {
            progressBar.setVisibility(View.GONE);
            if(responseCode==200) {
                if (data != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(data);
                        JSONArray results = new JSONArray(jsonObject.getString("results"));
                        Log.e("result length", results.length() + "");
                        if (results.length() > 0) {
                            for (int i = 0; i < results.length(); i++) {
                                JSONObject result = results.getJSONObject(i);
                                String trailerId = result.getString("id");
                                String key = result.getString("key");
                                String name = result.getString("name");
                                String site = result.getString("site");
                                String size = result.getString("size");
                                TrailerModel trailerModel = new TrailerModel(trailerId, key, name, site, size);
                                trailerList.add(trailerModel);
                            }

                            for (TrailerModel trailerModel : trailerList) {
                                fillLayout(linearLayout, trailerModel);
                            }


                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }else{
                showDialog(getString(R.string.message),errorMessage,1);
            }


            viewCommentsButton.setVisibility(View.VISIBLE);

            getSupportLoaderManager().destroyLoader(ASYNC_LOADER_ID);

        }

        @Override
        public void onLoaderReset(Loader<String> loader) {

        }
    }



    /**
     * Function shows a dismissable dialog to the user with important information
     * @param title {@link String} title of dialog
     * @param body {@link String} body of dialog
     * @param type int indicates the action to perform on dialog click
     * */

    private void showDialog(String title, String body, int type) {
        AlertDialog alertDialog = new AlertDialog.Builder(DetailActivity.this).create();
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
                                if(getSupportLoaderManager().getLoader(ASYNC_LOADER_ID)==null){
                                    getSupportLoaderManager().initLoader(ASYNC_LOADER_ID, null, new LoadTrailers()).forceLoad();
                                }else{
                                    getSupportLoaderManager().restartLoader(ASYNC_LOADER_ID, null, new LoadTrailers()).forceLoad();
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
