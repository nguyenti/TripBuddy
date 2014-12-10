package hu.ait.tiffanynguyen.tripbuddy;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by tiffanynguyen on 12/4/14.
 */
public class AddressActivity extends Activity implements AdapterView.OnItemClickListener {

    private AutoCompleteTextView actvFrom;
    private AutoCompleteTextView actvTo;

    public static final String START_LOCATION = "START_LOCATION";
    public static final String END_LOCATION = "END_LOCATION";
    public static final String LOCATION_BUNDLE = "LOCATION_BUNDLE";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address);


        actvFrom = (AutoCompleteTextView) findViewById(R.id.actvFrom);
        actvTo = (AutoCompleteTextView) findViewById(R.id.actvTo);

//        actvFrom.setText("Fisherman's Wharf, San Francisco, CA, United States");
//        actvTo.setText("The Moscone Center, Howard Street, San Francisco, CA, United States");

//        actvFrom.setAdapter(new PlacesAutoCompleteAdapter(this,
//                android.R.layout.simple_dropdown_item_1line));
//        actvTo.setAdapter(new PlacesAutoCompleteAdapter(this,
//                android.R.layout.simple_dropdown_item_1line));



        actvFrom.setAdapter(new PlacesAutoCompleteAdapter(this, R.layout.list_item));
        actvFrom.setOnItemClickListener(this);

        actvTo.setAdapter(new PlacesAutoCompleteAdapter(this, R.layout.list_item));
        actvTo.setOnItemClickListener(this);

        Log.i("LOG_FRAGMENT", "Success! Fragment launched!");

        Button btnSubmit = (Button) findViewById(R.id.load_directions);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentResult = new Intent();
                Bundle arguments = new Bundle();
                arguments.putString(START_LOCATION, actvFrom.getText().toString().trim().replaceAll("\\s+","+"));
                arguments.putString(END_LOCATION, actvTo.getText().toString().trim().replaceAll("\\s+","+"));
                intentResult.putExtra(LOCATION_BUNDLE, arguments);
                setResult(RESULT_OK, intentResult);
                finish();
            }
        });

//        // start async here
//        try {
//            new AutoComplete(getApplicationContext()).execute(/*Put the input here??*/);
//        } catch (Exception e) {
//            Toast.makeText(getApplicationContext(),
//                    getString(R.string.toast_city_not_entered), Toast.LENGTH_LONG).show();
//        }
        //broadcast -> set adapter
    }

    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        String str = (String) adapterView.getItemAtPosition(position);
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(
                locationReceiver, new IntentFilter(AutoComplete.FILTER_LOCATIONS)
        );
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(locationReceiver);
    }

    private BroadcastReceiver locationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ArrayList<String> rawJson = intent.getStringArrayListExtra(AutoComplete.KEY_LOCATION);

            try {
//                JSONObject root = new JSONObject(rawJson);


            } catch (Resources.NotFoundException e) {
                Toast.makeText(getApplicationContext(), "city not found", Toast.LENGTH_SHORT);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

}