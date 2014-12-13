package hu.ait.tiffanynguyen.tripbuddy;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

import hu.ait.tiffanynguyen.tripbuddy.adapter.RouteAdapter;
import hu.ait.tiffanynguyen.tripbuddy.data.Route;


public class RouteListActivity extends ListActivity {

    public static final String ARG_ITEM_ID = "item_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_route_list);

        try {
            List<Route> routeList = Route.listAll(Route.class);
            Log.i("LOG_ROUTELIST", routeList.toString());
            setListAdapter(new RouteAdapter(getApplicationContext(),routeList));
        } catch (Exception e) {
            Log.e("LOG_LISTALL", "Cannot find db");
            Toast.makeText(this, "Sorry, there was an issue getting your saved routes. Please try again later",
                    Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }


        ListView lv = getListView();
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
//                // selected item
//                Route item = (Route) getListAdapter().getItem(position);
//
//                // Launching new Activity on selecting single List Item
//                Intent i = new Intent(getBaseContext(), ItemDetailActivity.class);
//                // sending data to new activity
//                i.putExtra("item", item);
//                startActivity(i);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.route_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
