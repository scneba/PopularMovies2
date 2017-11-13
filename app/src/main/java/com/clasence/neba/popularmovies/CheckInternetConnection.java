package com.clasence.neba.popularmovies;

import android.content.Context;
import android.net.ConnectivityManager;

/**
 * Created by Neba.
 */

public class CheckInternetConnection {

        /**
         * Function checks if the phone is connected to a network
         * Ideas gotten from https://stackoverflow.com/questions/9570237/android-check-internet-connection
         * @param context {@link android.content.Context} context of application
         * @return boolean whether connected or not */
    public static boolean isNetworkAvailable(Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }
}
