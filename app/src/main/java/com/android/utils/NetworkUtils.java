package com.android.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;

public final class NetworkUtils {


    private static int TYPE_NOT_CONNECTED = 0;


    private static int getConnectivityStatus(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        Network[] networks = cm.getAllNetworks();
        NetworkCapabilities activeNetwork = null;
        for (Network network : networks) {
            activeNetwork = cm.getNetworkCapabilities(network);
        }
        if (null != activeNetwork) {
            int TYPE_WIFI = 1;
            if (activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI))
                return TYPE_WIFI;

            int TYPE_MOBILE = 2;
            if (activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
                return TYPE_WIFI;

            NetworkInfo[] info = cm.getAllNetworkInfo();

            for (NetworkInfo anInfo : info) {
                if (anInfo.getDetailedState() == NetworkInfo.DetailedState.CONNECTING) {
                    return 1;
                }
            }
        }

        return TYPE_NOT_CONNECTED;
    }

    public static String getConnectivityStatusString(Context context) {
        int conn = NetworkUtils.getConnectivityStatus(context);
        String status = null;
        if (conn == NetworkUtils.TYPE_NOT_CONNECTED) {
            status = "Not connected to Internet";
        }
        return status;
    }

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = null;
        if (cm != null) {
            activeNetwork = cm.getActiveNetworkInfo();
        }
        return activeNetwork != null && activeNetwork.isConnected();
    }
}