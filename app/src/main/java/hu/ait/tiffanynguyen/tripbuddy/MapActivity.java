package hu.ait.tiffanynguyen.tripbuddy;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
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

public class MapActivity extends Activity implements GoogleMap.OnMarkerDragListener {

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
        setContentView(R.layout.activity_map);

        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

//        map.setTrafficEnabled(true);

        // if you want to know where the marker was moved to, you need OnMarkerDragListener
        map.setOnMarkerDragListener(this);

        String url = getMapsApiDirectionsUrl();
        ReadTask downloadTask = new ReadTask();
        downloadTask.execute(url);

//        GeoCodeRequest geoCodeRequest = new GeoCodeRequest();
//        geoCodeRequest.execute();

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

        String sensor = "sensor=false";
        String params = waypoints + "&" + sensor;
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/"
                + output + "?" + params;
        Log.i("LOG_URL", url);
        return url;
    }

    private String getMapsApiDirectionsUrl(LatLng start, LatLng end) {
        String waypoints = "waypoints=optimize:true|"
                + start.latitude + "," + start.longitude
                + "||" + end.latitude + "," + end.longitude;

        String sensor = "sensor=false";
        String params = waypoints + "&" + sensor;
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/"
                + output + "?" + params;
        return url;
    }

    private String getMapsApiDirectionsUrl(String start, String end) {
        String waypoints = "waypoints=optimize:true|"
                + start + "||" + end;

        String key = "key=AIzaSyAMPjOwwYqTflMR1HMmD7WwyJ8HWEj4G2Y";
        String sensor = "sensor=false";
        String params = waypoints + "&" + sensor;
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/"
                + output + "?" + params + "&" + key;
        return url;
    }

    private void addMarkers() {
        if (map != null) {
            map.addMarker(new MarkerOptions().position(PIPA_UTCA)
                    .title("Pipa Point"));
            map.addMarker(new MarkerOptions().position(AIT)
                    .title("AIT Point"));
        }
    }

    private void addNewMarkers(LatLng[] latLng) {
        map.clear();
        for (int i = 0; i < latLng.length; i++) {
            map.addMarker(new MarkerOptions().position(latLng[i]).title("Item " + i));
            Log.i("LOG_MARKER", "Added marker at " + latLng[i].toString());
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
            Log.i("LOG_ROUTESIZE", routes.size()+"");

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
            Intent i = new Intent();
            i.setClass(this, AddressActivity.class);
            // REQUEST... is a request code to get results from a certain activity
            startActivityForResult(i, REQUEST_DIRECTIONS);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private BroadcastReceiver locationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getBundleExtra(GeoCodeRequest.KEY_ADDRESS);
            LatLng[] latLng = (LatLng[]) bundle.getSerializable(GeoCodeRequest.KEY_BUNDLE);

            addNewMarkers(latLng);

            ReadTask downloadTask = new ReadTask();
            String url = getMapsApiDirectionsUrl(latLng[0], latLng[1]);
            downloadTask.execute(url);
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Toast.makeText(this, "Directions sent", Toast.LENGTH_LONG).show();
            Bundle bundle = data.getBundleExtra(AddressActivity.LOCATION_BUNDLE);
            String start = bundle.getString(AddressActivity.START_LOCATION);
            String end = bundle.getString(AddressActivity.END_LOCATION);

            GeoCodeRequest geoCodeRequest = new GeoCodeRequest(this);
            geoCodeRequest.execute(start, end);

        } else if (resultCode == RESULT_CANCELED) {
            Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(
                locationReceiver, new IntentFilter(GeoCodeRequest.FILTER_ADDRESS)
        );
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(locationReceiver);
    }
}
