package hu.ait.tiffanynguyen.tripbuddy;

/**
 * Created by tiffanynguyen on 12/8/14.
 */

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.GeocodingResult;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by tiffanynguyen on 11/27/14.
 */
public class GeoCodeRequest extends AsyncTask<String, Void, LatLng[]> {

    public static final String FILTER_ADDRESS = "FILTER_ADDRESS";
    public static final String KEY_ADDRESS = "KEY_ADDRESS";
    public static final String KEY_BUNDLE = "KEY_BUNDLE";
    private Context context;

    // you need a public constructor
    public GeoCodeRequest(Context context) {
        this.context = context;
    }

    @Override
    protected LatLng[] doInBackground(String... params) {
        LatLng latLngStart = null;
        LatLng latLngEnd = null;
        LatLng[] result = new LatLng[2];
        try {
            if (params.length != 2)
                throw new Exception();

            GeoApiContext context = new GeoApiContext().setApiKey("AIzaSyCMhs1dkvIdMMt1R0kyp4ekYBgOzr4o1uc");
//            GeocodingResult[] results =  GeocodingApi.geocode(context,
//                    "1600 Amphitheatre Parkway Mountain View, CA 94043").await();
            GeocodingResult[] resultsStart =  GeocodingApi.geocode(context,
                    params[0]).await();
//            Log.i("LOG_GEOCODING", results[0].geometry.location.toString());
//            System.out.println(results[0].formattedAddress);
            com.google.maps.model.LatLng ll = resultsStart[0].geometry.location;
            latLngStart = new LatLng(ll.lat, ll.lng);

            GeocodingResult[] resultsEnd =  GeocodingApi.geocode(context,
                    params[1]).await();
            com.google.maps.model.LatLng llEnd = resultsEnd[0].geometry.location;
            latLngEnd = new LatLng(llEnd.lat, llEnd.lng);
            result[0] = latLngStart;
            result[1] = latLngEnd;
        } catch (Exception e) {
            Log.i("LOG_GEOCODING", "FAILED");
            e.getMessage();
        }

        return result;
    }

    @Override
    protected void onPostExecute(LatLng[] latLng) {
        Intent i = new Intent(FILTER_ADDRESS);
        Bundle bundle = new Bundle();
        bundle.putParcelableArray(KEY_BUNDLE, latLng);
        i.putExtra(KEY_ADDRESS, bundle);
        LocalBroadcastManager.getInstance(context).sendBroadcast(i);
    }
}
