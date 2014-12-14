package hu.ait.tiffanynguyen.tripbuddy;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import hu.ait.tiffanynguyen.tripbuddy.adapter.PlacesAutoCompleteAdapter;

/**
 * Created by tiffanynguyen on 12/4/14.
 */
public class AddressActivity extends Activity implements AdapterView.OnItemClickListener {

    private AutoCompleteTextView actvFrom;
    private AutoCompleteTextView actvTo;

    public static final String START_LOCATION = "START_LOCATION";
    public static final String END_LOCATION = "END_LOCATION";
    public static final String LOCATION_BUNDLE = "LOCATION_BUNDLE";
    public static final String SELECTED_RADIO = "SELECTED_RADIO";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address);


        actvFrom = (AutoCompleteTextView) findViewById(R.id.actvFrom);
        actvTo = (AutoCompleteTextView) findViewById(R.id.actvTo);

        actvFrom.setAdapter(new PlacesAutoCompleteAdapter(this, R.layout.list_item));
        actvFrom.setOnItemClickListener(this);

        actvTo.setAdapter(new PlacesAutoCompleteAdapter(this, R.layout.list_item));
        actvTo.setOnItemClickListener(this);

        final RadioGroup radioGroup = (RadioGroup) findViewById(R.id.rgTransport);

        Button btnSubmit = (Button) findViewById(R.id.load_directions);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentResult = new Intent();
                Bundle arguments = new Bundle();
                arguments.putString(START_LOCATION, actvFrom.getText().toString().trim());
                arguments.putString(END_LOCATION, actvTo.getText().toString().trim());
                arguments.putInt(SELECTED_RADIO, radioGroup.getCheckedRadioButtonId());
                intentResult.putExtra(LOCATION_BUNDLE, arguments);
                setResult(RESULT_OK, intentResult);
                finish();
            }
        });
    }

    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        String str = (String) adapterView.getItemAtPosition(position);
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
//        LocalBroadcastManager.getInstance(this).registerReceiver(
//                locationReceiver, new IntentFilter(PlacesAutoCompleteAdapter.AutoComplete.FILTER_LOCATIONS)
//        );
    }

    @Override
    public void onPause() {
        super.onPause();
//        LocalBroadcastManager.getInstance(this).unregisterReceiver(locationReceiver);
    }
}
