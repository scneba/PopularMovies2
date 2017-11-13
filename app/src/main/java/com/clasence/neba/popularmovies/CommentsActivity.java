package com.clasence.neba.popularmovies;

import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.clasence.neba.popularmovies.models.ReviewHelper;
import com.clasence.neba.popularmovies.models.TrailerModel;

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

/**
 * Created by Neba.
 */

public class CommentsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String> {

    private final int LOADER_ID = 20;
    private RecyclerView recyclerView;
    private ProgressBar pbload;
    private String movieId;
    private ArrayList<ReviewHelper> reviewList;
    private ReviewAdapter reviewAdapter;
    private static final String REVIEW_lIST = "review_list";
    private static final String LIST_POSITION = "list_position";
    private static final String LAYOUT_MAN_STATE = "layout_man_state";

    //keeps error message from loader
    private String errorMessage="";

    private LinearLayoutManager layoutManager;

    //uri builder for asynctask loading
    private Uri.Builder builder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.comments_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = (RecyclerView) findViewById(R.id.commentsRecycler);
        recyclerView.setHasFixedSize(false);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());




        pbload = (ProgressBar) findViewById(R.id.pbload);

        movieId = getIntent().getExtras().getString(DetailActivity.MOVIE_ID);

        builder = new Uri.Builder();
        builder.scheme(getString(R.string.scheme))
                .authority(getString(R.string.base_url))
                .appendPath("3")
                .appendPath("movie")
                .appendPath(movieId)
                .appendPath("reviews")
                .appendQueryParameter("api_key", getString(R.string.api_key));


        if(savedInstanceState!=null && savedInstanceState.containsKey(REVIEW_lIST)) {

            reviewList=savedInstanceState.getParcelableArrayList(REVIEW_lIST);
            reviewAdapter = new ReviewAdapter(this,reviewList);
            recyclerView.setAdapter(reviewAdapter);
            layoutManager.onRestoreInstanceState(savedInstanceState.getParcelable(LAYOUT_MAN_STATE));
        }
        else{

            reviewList=new ArrayList<>();
            reviewAdapter = new ReviewAdapter(this,reviewList);
            recyclerView.setAdapter(reviewAdapter);
            if (CheckInternetConnection.isNetworkAvailable(getApplicationContext())) {

                if(getSupportLoaderManager().getLoader(LOADER_ID)==null){
                    getSupportLoaderManager().initLoader(LOADER_ID, null, this).forceLoad();
                }else{
                    getSupportLoaderManager().restartLoader(LOADER_ID, null, this).forceLoad();
                }
            }else{
                showDialog(getResources().getString(R.string.message), getResources().getString(R.string.noint), 1);
            }
        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //save parcelable movie list and the position of the current list item
        if(reviewList.size()>0) {
            outState.putParcelableArrayList(REVIEW_lIST, reviewList);
            outState.putParcelable(LAYOUT_MAN_STATE,layoutManager.onSaveInstanceState());
        }

    }


    @Override
    public void onBackPressed() {
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
    public Loader<String> onCreateLoader(int id, Bundle args) {

        return new AsyncTaskLoader<String>(CommentsActivity.this) {
             HttpURLConnection urlConnection;


            @Override
            protected void onStartLoading() {
                super.onStartLoading();
                pbload.setVisibility(View.VISIBLE);
                reviewList.clear();
                reviewAdapter.notifyDataSetChanged();
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
                    return response.toString();
                } catch (SocketTimeoutException e) {
                    errorMessage = getString(R.string.connectiontimeout);
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

        };
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String data) {
        pbload.setVisibility(View.GONE);

        try {
            JSONObject jsonObject = new JSONObject(data);


            JSONArray results = new JSONArray(jsonObject.getString("results"));
            Log.e("result length",results.length()+"");
            if (results.length() > 0) {
                for (int i = 0; i < results.length(); i++) {
                    JSONObject result = results.getJSONObject(i);
                    String id = result.getString("id");
                    String author = result.getString("author");
                    String comment = result.getString("content");
                    ReviewHelper reviewHelper = new ReviewHelper(id,author,comment);
                    reviewList.add(reviewHelper);

                }
                reviewAdapter.notifyDataSetChanged();
            }else{
                showDialog(getResources().getString(R.string.message), getResources().getString(R.string.noreviews), 1);

            }
        } catch (JSONException e) {
            if(!errorMessage.equalsIgnoreCase("")){
                showDialog(getString(R.string.message),errorMessage,1);
                errorMessage="";
            }
        }


        //destroy loader
        getSupportLoaderManager().destroyLoader(LOADER_ID);
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
        AlertDialog alertDialog = new AlertDialog.Builder(CommentsActivity.this).create();
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
                                if(getSupportLoaderManager().getLoader(LOADER_ID)!=null){
                                    getSupportLoaderManager().initLoader(LOADER_ID, null, CommentsActivity.this).forceLoad();
                                }else{
                                    getSupportLoaderManager().restartLoader(LOADER_ID, null, CommentsActivity.this).forceLoad();
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
