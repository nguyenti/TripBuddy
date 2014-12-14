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
    public static final String SAVED_ROUTE = "SAVED_ROUTE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_route_list);
        List<Route> routeList;
        boolean fail = false;
        try {
            routeList = Route.listAll(Route.class);
            setListAdapter(new RouteAdapter(getApplicationContext(), routeList));
        } catch (Exception e) {
            Toast.makeText(this, "Sorry, there was an issue getting your saved routes. Please try again later",
                    Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            fail = true;
        }

        if (!fail) {

        }

        ListView lv = getListView();
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // selected item
                Route item = (Route) getListAdapter().getItem(position);
                Intent intentResult = new Intent();
                intentResult.putExtra(SAVED_ROUTE, item.getId());
                setResult(RESULT_OK, intentResult);
                finish();
            }
        });
    }
}
