package hu.ait.tiffanynguyen.tripbuddy.map;

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
import android.widget.Toast;

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

import hu.ait.tiffanynguyen.tripbuddy.R;

/**
 * Created by tiffanynguyen on 11/27/14.
 */
public class GeoCodeRequest extends AsyncTask<String, Void, LatLng[]> {

    public static final String FILTER_ADDRESS = "FILTER_ADDRESS";
    public static final String KEY_ADDRESS = "KEY_ADDRESS";
    public static final String KEY_BUNDLE = "KEY_BUNDLE";
    private Context context;
    private boolean failed;

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

            GeoApiContext context = new GeoApiContext().setApiKey(this.context.getString(R.string.geo_api_key));
            GeocodingResult[] resultsStart =  GeocodingApi.geocode(context,
                    params[0]).await();
            com.google.maps.model.LatLng ll = resultsStart[0].geometry.location;
            latLngStart = new LatLng(ll.lat, ll.lng);

            GeocodingResult[] resultsEnd =  GeocodingApi.geocode(context,
                    params[1]).await();
            com.google.maps.model.LatLng llEnd = resultsEnd[0].geometry.location;
            latLngEnd = new LatLng(llEnd.lat, llEnd.lng);
            result[0] = latLngStart;
            result[1] = latLngEnd;
            failed = false;
        } catch (Exception e) {
            e.getMessage();
            failed = true;
        }

        return result;
    }

    @Override
    protected void onPostExecute(LatLng[] latLng) {
        if (failed)
            Toast.makeText(context, context.getString(R.string.try_again_bad_input),
                    Toast.LENGTH_SHORT).show();
        else {
            Intent i = new Intent(FILTER_ADDRESS);
            Bundle bundle = new Bundle();
            bundle.putParcelableArray(KEY_BUNDLE, latLng);
            i.putExtra(KEY_ADDRESS, bundle);
            LocalBroadcastManager.getInstance(context).sendBroadcast(i);
        }
    }
}
