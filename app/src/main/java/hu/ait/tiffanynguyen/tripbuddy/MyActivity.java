package hu.ait.tiffanynguyen.tripbuddy;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import hu.ait.tiffanynguyen.tripbuddy.map.HttpConnection;
import hu.ait.tiffanynguyen.tripbuddy.map.PathJSONParser;

/**
 * Directions code taken from http://javapapers.com/android/draw-path-on-google-maps-android-api/
 */

public class MyActivity extends Activity implements GoogleMap.OnMarkerDragListener {

    private GoogleMap map;

    private static final LatLng LOWER_MANHATTAN = new LatLng(40.722543,
            -73.998585);
    private static final LatLng BROOKLYN_BRIDGE = new LatLng(40.7057, -73.9964);
    private static final LatLng WALL_STREET = new LatLng(40.7064, -74.0094);
    private static final LatLng PIPA_UTCA = new LatLng(47.487512, 19.059062);
    private static final LatLng AIT = new LatLng(47.561333, 19.054627);

    private static int REQUEST_DIRECTIONS = 100;


    final String TAG = "PathGoogleMapActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

//        map.setTrafficEnabled(true);

        // if you want to know where the marker was moved to, you need OnMarkerDragListener
        map.setOnMarkerDragListener(this);

        String url = getMapsApiDirectionsUrl();
        ReadTask downloadTask = new ReadTask();
        downloadTask.execute(url);

        // You can change the CameraPosition to view the map differently
        // This will have a 3d movement Zooming into the position (animate Camera)
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(PIPA_UTCA)
                .zoom(13)
//                .bearing(90) // sets how the map is viewed
                .tilt(30)
                .build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        addMarkers();
    }

    private String getMapsApiDirectionsUrl() {
        String waypoints = "waypoints=optimize:true|"
                + PIPA_UTCA.latitude + "," + PIPA_UTCA.longitude
                + "||" + AIT.latitude + "," + AIT.longitude;
//                + LOWER_MANHATTAN.latitude + "," + LOWER_MANHATTAN.longitude
//                + "|" + "|" + BROOKLYN_BRIDGE.latitude + ","
//                + BROOKLYN_BRIDGE.longitude + "|" + WALL_STREET.latitude + ","
//                + WALL_STREET.longitude;

        String sensor = "sensor=false";
        String params = waypoints + "&" + sensor;
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/"
                + output + "?" + params;
        return url;
    }

    private void addMarkers() {
        if (map != null) {
//            map.addMarker(new MarkerOptions().position(BROOKLYN_BRIDGE)
//                    .title("First Point"));
//            map.addMarker(new MarkerOptions().position(LOWER_MANHATTAN)
//                    .title("Second Point"));
//            map.addMarker(new MarkerOptions().position(WALL_STREET)
//                    .title("Third Point"));
            map.addMarker(new MarkerOptions().position(PIPA_UTCA)
                    .title("Pipa Point"));
            map.addMarker(new MarkerOptions().position(AIT)
                    .title("AIT Point"));
        }
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {

    }

    private class ReadTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... url) {
            String data = "";
            try {
                HttpConnection http = new HttpConnection();
                data = http.readUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            new ParserTask().execute(result);
        }
    }

    private class ParserTask extends
            AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(
                String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                PathJSONParser parser = new PathJSONParser();
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> routes) {
            ArrayList<LatLng> points = null;
            PolylineOptions polyLineOptions = null;

            // traversing through routes
            for (int i = 0; i < routes.size(); i++) {
                points = new ArrayList<LatLng>();
                polyLineOptions = new PolylineOptions();
                List<HashMap<String, String>> path = routes.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                polyLineOptions.addAll(points);
                polyLineOptions.width(4);
                polyLineOptions.color(Color.CYAN);
            }

            map.addPolyline(polyLineOptions);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_add) {
            MarkerOptions marker = new MarkerOptions();

            // if you want to use persistence data, you can save the coordinates! LatLong
            marker.position(map.getCameraPosition().target);
            marker.title("My new marker");
            marker.snippet("My marker info text");

            marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_launcher));
            marker.draggable(true);


            map.addMarker(marker);

            return true;
        } else if (id == R.id.action_enter_address) {
            Log.i("LOG_FRAGMENT", "Fragment launcher");
//            addressFragment = new AddressFragment();
//            getFragmentManager().beginTransaction()
//                    .add(addressFragment, AddressFragment.TAG_ADDRESS_FRAGMENT)
//                    .commit();

//            FragmentManager fm = getFragmentManager();
//            FragmentTransaction ft = fm.beginTransaction();
//            ft.add(R.id.layoutContainer, new AddressActivity());
//            ft.addToBackStack(null);
//            ft.commit();
            Intent i = new Intent();
            i.setClass(this, AddressActivity.class);
            // REQUEST... is a request code to get results from a certain activity
            startActivityForResult(i, REQUEST_DIRECTIONS);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Toast.makeText(this, "Password changed", Toast.LENGTH_LONG).show();
        } else if (resultCode == RESULT_CANCELED) {
            Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
        }
    }
}
