package hu.ait.tiffanynguyen.tripbuddy.map;

/**
 * Created by tiffanynguyen on 12/8/14.
 */

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.GeocodingResult;

/**
 * Created by tiffanynguyen on 11/27/14.
 */
public class ReverseGeoCodeRequest extends AsyncTask<LatLng, Void, String[]> {

    public static final String FILTER_STR_ADDRESS = "FILTER_STR_ADDRESS";
    public static final String KEY_STR_ADDRESS = "KEY_STR_ADDRESS";
    private Context context;

    // you need a public constructor
    public ReverseGeoCodeRequest(Context context) {
        this.context = context;
    }

    @Override
    protected String[] doInBackground(LatLng... params) {
        String start;
        String end;
        String[] result = new String[2];
        try {
            if (params.length != 2)
                throw new Exception();

            com.google.maps.model.LatLng llStart =
                    new com.google.maps.model.LatLng(params[0].latitude, params[0].longitude);
            com.google.maps.model.LatLng llEnd =
                    new com.google.maps.model.LatLng(params[1].latitude, params[1].longitude);

            GeoApiContext context = new GeoApiContext().setApiKey("AIzaSyCMhs1dkvIdMMt1R0kyp4ekYBgOzr4o1uc");
            GeocodingResult[] resultsStart =  GeocodingApi.reverseGeocode(context, llStart).await();
            start = resultsStart[0].formattedAddress;

            GeocodingResult[] resultsEnd =  GeocodingApi.reverseGeocode(context, llEnd).await();
            end = resultsEnd[0].formattedAddress;
            result[0] = start;
            result[1] = end;
        } catch (Exception e) {
            Log.i("LOG_REV_GEOCODING", "FAILED");
            e.printStackTrace();
        }
        return result;
    }

    @Override
    protected void onPostExecute(String[] addrs) {
        Intent i = new Intent(FILTER_STR_ADDRESS);
        i.putExtra(KEY_STR_ADDRESS, addrs);
//        Bundle bundle = new Bundle();
//        bundle.putStringArray("BUNDLE_KEY", addrs);
//        i.putExtra(KEY_STR_ADDRESS, bundle);
        Log.i("LOG_REV_GEOCODING", "SENDING");
        LocalBroadcastManager.getInstance(context).sendBroadcast(i);
    }
}
