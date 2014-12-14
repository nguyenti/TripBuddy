package hu.ait.tiffanynguyen.tripbuddy;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.*;
import android.os.Process;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import hu.ait.tiffanynguyen.tripbuddy.data.Route;
import hu.ait.tiffanynguyen.tripbuddy.map.CustomMapTileProvider;
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
    private static final String PREF_NAME = "MyMaps";

    private GoogleMap map;
    private Route currDir;
    private boolean isConnected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();

        ConnectivityManager cm =
                (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        final SharedPreferences sp =
                getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        if (sp.getBoolean(getString(R.string.my_first_time), true)) {
            if (!isConnected) {
                new AlertDialog.Builder(MapActivity.this)
                        .setTitle(getString(R.string.network_connectivity))
                        .setMessage(getString(R.string.open_with_network))
                        .setNeutralButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
//                                MapActivity.this.finish();
                                  Process.killProcess(Process.myPid());
                                  System.exit(0);
                            }
                        })
                        .show();
            } else {
                sp.edit().putBoolean(getString(R.string.my_first_time), false).apply();
            }
        }

        map.addTileOverlay(new TileOverlayOptions().tileProvider(new CustomMapTileProvider(getResources().getAssets())));
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

    private String getMapsApiDirectionsUrl(LatLng start, LatLng end, int travelMode) {
        String waypoints = "waypoints=optimize:true|"
                + start.latitude + "," + start.longitude
                + "||" + end.latitude + "," + end.longitude;

        String mode;
        switch (travelMode) {
            case R.drawable.ic_walk:
                mode = "mode=walking";
                break;
            case R.drawable.ic_cycling:
                mode = "mode=bicycling";
                break;
            default:
                mode = "mode=driving";
        }

        String sensor = "sensor=false";
        String params = waypoints + "&" + sensor + "&" + mode;
        String output = "json";
        return "https://maps.googleapis.com/maps/api/directions/"
                + output + "?" + params;
    }

    private void addNewMarkers(LatLng[] latLng) {
        map.clear();
        for (int i = 0; i < latLng.length; i++) {
//            Log.i("LOG_MARKER", "Added marker at " + latLng[i].toString());
            map.addMarker(new MarkerOptions().position(latLng[i]).title("Item " + i));
        }
   }

    public class ReadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {
            String data = "";
            try {
                HttpConnection http = new HttpConnection();
                data = http.readUrl(url[0]);
            } catch (Exception e) {
//                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            currDir.setJson(result);
            new ParserTask().execute(result);
        }
    }

    public class ParserTask extends
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
            ArrayList<LatLng> points;
            PolylineOptions polyLineOptions = null;
            if (routes.size() > 0) {
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
            } else {
                Toast.makeText(getApplicationContext(),
                        getString(R.string.no_results_found), Toast.LENGTH_SHORT).show();
                currDir = null;
            }
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
            try {
                if (currDir == null) {
                    Toast.makeText(this, getString(R.string.look_up_route),
                            Toast.LENGTH_SHORT).show();
                } else if (Route.findById(Route.class, currDir.getId()) == null) {
                    currDir.save();
                    Toast.makeText(this, getString(R.string.saved),
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, getString(R.string.already_saved),
                            Toast.LENGTH_SHORT).show();
                }

            } catch (Exception e) {
                Toast.makeText(this, getString(R.string.issue_saving),
                        Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
            return true;
        } else if (id == R.id.action_enter_address) {
            ConnectivityManager cm =
                    (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            isConnected = activeNetwork != null &&
                    activeNetwork.isConnectedOrConnecting();
            if (isConnected) {
                Intent i = new Intent();
                i.setClass(this, AddressActivity.class);
                // REQUEST... is a request code to get results from a certain activity
                startActivityForResult(i, REQUEST_ROUTE);
            } else {
                Toast.makeText(this, getString(R.string.lookup_offline),
                        Toast.LENGTH_SHORT).show();
            }
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
            if (requestCode == REQUEST_ROUTE) {
                Bundle bundle = data.getBundleExtra(AddressActivity.LOCATION_BUNDLE);
                String start = bundle.getString(AddressActivity.START_LOCATION);
                String end = bundle.getString(AddressActivity.END_LOCATION);
                currDir = new Route(start, end);
                int iconId;
                switch (bundle.getInt(AddressActivity.SELECTED_RADIO)) {
                    case R.id.btnWalk:
                        iconId = R.drawable.ic_walk;
                        break;
                    case R.id.btnBike:
                        iconId = R.drawable.ic_cycling;
                        break;
                    default:
                        iconId = R.drawable.ic_car;
                }
                currDir.setTravelMode(iconId);

                GeoCodeRequest geoCodeRequest = new GeoCodeRequest(this);
                geoCodeRequest.execute(start.replaceAll("\\s+", "+"), end.replaceAll("\\s+", "+"));
            } else if (requestCode == REQUEST_SAVED_ROUTE) {
//                currDir = (Route) data.getSerializableExtra(RouteListActivity.SAVED_ROUTE);
                long currId = data.getLongExtra(RouteListActivity.SAVED_ROUTE, -1);
                if (currId != -1) {
                    currDir = Route.findById(Route.class, currId);
                    updateMapOffline(currDir.getStartEnd());
                } else {
                    Toast.makeText(this, getString(R.string.issue_saved_route),
                            Toast.LENGTH_SHORT).show();
                }
            }
        } else if (resultCode == RESULT_CANCELED) {
            Toast.makeText(this, getString(R.string.cancelled), Toast.LENGTH_LONG).show();
        }
    }

    private BroadcastReceiver locationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra(GeoCodeRequest.KEY_ADDRESS)) {
                Bundle bundle = intent.getBundleExtra(GeoCodeRequest.KEY_ADDRESS);
                LatLng[] latLng = (LatLng[]) bundle.getParcelableArray(GeoCodeRequest.KEY_BUNDLE);

                updateMap(latLng);
            }
        }
    };

    private void updateMap(LatLng[] latLng) {

        addNewMarkers(latLng);

        currDir.setStart(latLng[0]);
        currDir.setEnd(latLng[1]);

        ReadTask downloadTask = new ReadTask();
        String url = getMapsApiDirectionsUrl(latLng[0], latLng[1], currDir.getTravelMode());
        downloadTask.execute(url);

        moveCamera(latLng);
     }

    private void updateMapOffline(LatLng[] latLng) {

        addNewMarkers(latLng);

        ParserTask parserTask = new ParserTask();
        parserTask.execute(currDir.getJson());

        moveCamera(latLng);
    }

    private void moveCamera(LatLng[] latlng) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng item : latlng) {
            builder.include(item);
        }
        LatLngBounds bounds = builder.build();
        int padding = 100;
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        map.animateCamera(cu);
    }
}
