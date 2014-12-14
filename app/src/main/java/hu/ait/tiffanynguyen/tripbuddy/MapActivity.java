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
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import hu.ait.tiffanynguyen.tripbuddy.data.Route;
import hu.ait.tiffanynguyen.tripbuddy.map.GeoCodeRequest;
import hu.ait.tiffanynguyen.tripbuddy.map.HttpConnection;
import hu.ait.tiffanynguyen.tripbuddy.map.PathJSONParser;

/**
 * Directions code taken from http://javapapers.com/android/draw-path-on-google-maps-android-api/
 */

public class MapActivity extends Activity {

    private static final LatLng PIPA_UTCA = new LatLng(47.487512, 19.059062);
    private static final LatLng AIT = new LatLng(47.561333, 19.054627);

    private static final int REQUEST_ROUTE = 100;
    private static final int REQUEST_SAVED_ROUTE = 110;


    private GoogleMap map;
    private Route currDir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

//        map.setTrafficEnabled(true);

        // if you want to know where the marker was moved to, you need OnMarkerDragListener
//        map.setOnMarkerDragListener(this);
//        implements GoogleMap.OnMarkerDragListener

        String url = getMapsApiDirectionsUrl();
        ReadTask downloadTask = new ReadTask(this);
        downloadTask.execute(url);

        currDir = new Route(PIPA_UTCA, AIT);

        // You can change the CameraPosition to view the map differently
        // This will have a 3d movement Zooming into the position (animate Camera)
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(currDir.getMidpoint())
                .zoom(12)
//                .bearing(90) // sets how the map is viewed
                .tilt(30)
                .build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        addMarkers();
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
        return "https://maps.googleapis.com/maps/api/directions/"
                + output + "?" + params;
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

    public class ReadTask extends AsyncTask<String, Void, String> {

        public static final String FILTER_JSON = "FILTER_JSON";
        public static final String KEY_JSON = "KEY_JSON";
        private Context context;

        ReadTask(Context context) {
            this.context = context;
        }

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
            Log.i("LOG_JSON", result);
            currDir.setJson(result);
//            Log.i("LOG_SENDING", "Sending json..");
//            Intent i = new Intent(FILTER_JSON);
//            i.putExtra(KEY_JSON, result);
//            LocalBroadcastManager.getInstance(context).sendBroadcast(i);
            new ParserTask(context).execute(result);
        }
    }

    public class ParserTask extends
            AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        private Context context;

        public ParserTask(Context context) {
            this.context = context;
        }

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
            Log.i("LOG_ROUTESIZE", routes.size() + "");

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
        if (id == R.id.action_save) {
            Log.i("LOG_SAVE", "Saved!");
            try {
                currDir.save();
            } catch (Exception e) {
                Log.e("LOG_SAVE_FAILED", "Save failed");
                Toast.makeText(this, "Sorry, there was an issue saving. Please try again later",
                        Toast.LENGTH_SHORT).show();
//                e.printStackTrace();
            }
            return true;
        } else if (id == R.id.action_enter_address) {
            Intent i = new Intent();
            i.setClass(this, AddressActivity.class);
            // REQUEST... is a request code to get results from a certain activity
            startActivityForResult(i, REQUEST_ROUTE);
            return true;
        } else if (id == R.id.action_view_routes) {
            Intent i = new Intent();
            i.setClass(this, RouteListActivity.class);
            startActivityForResult(i, REQUEST_SAVED_ROUTE);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            Toast.makeText(this, "Directions sent", Toast.LENGTH_LONG).show();
            Bundle bundle = data.getBundleExtra(AddressActivity.LOCATION_BUNDLE);
            String start = bundle.getString(AddressActivity.START_LOCATION);
            String end = bundle.getString(AddressActivity.END_LOCATION);
            currDir.setStrStart(start);
            currDir.setStrEnd(end);

            GeoCodeRequest geoCodeRequest = new GeoCodeRequest(this);
            geoCodeRequest.execute(start.replaceAll("\\s+","+"), end.replaceAll("\\s+","+"));

        } else if (resultCode == RESULT_CANCELED) {
            Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
        }
    }

    private BroadcastReceiver locationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("LOG_RECEIVED", "got something");
            if (intent.hasExtra(GeoCodeRequest.KEY_ADDRESS)) {
                Log.i("LOG_RECEIVED", "got address");
                Bundle bundle = intent.getBundleExtra(GeoCodeRequest.KEY_ADDRESS);
                LatLng[] latLng = (LatLng[]) bundle.getParcelableArray(GeoCodeRequest.KEY_BUNDLE);

                updateMap(latLng);
                Log.i("LOG_CURRDIR", currDir.toString());
            }
        }
    };

    private void updateMap(LatLng[] latLng) {

        addNewMarkers(latLng);

        currDir.setStart(latLng[0]);
        currDir.setEnd(latLng[1]);

        ReadTask downloadTask = new ReadTask(this);
        String url = getMapsApiDirectionsUrl(latLng[0], latLng[1]);
        downloadTask.execute(url);

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(currDir.getMidpoint())
                .zoom(13)
//                .bearing(90) // sets how the map is viewed
                .tilt(30)
                .build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
     }
}
