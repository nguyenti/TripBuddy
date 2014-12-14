package hu.ait.tiffanynguyen.tripbuddy;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.orm.query.Condition;
import com.orm.query.Select;

import java.util.List;

import hu.ait.tiffanynguyen.tripbuddy.adapter.RouteAdapter;
import hu.ait.tiffanynguyen.tripbuddy.data.Route;


public class RouteListActivity extends ListActivity {

    public static final String SAVED_ROUTE = "SAVED_ROUTE";

    List<Route> routeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_route_list);
        boolean fail = false;
        try {
            refreshList();
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.issue_saved_routes),
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



        getListView().setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                new AlertDialog.Builder(RouteListActivity.this)
                        .setTitle(getString(R.string.delete))
                        .setMessage(getString(R.string.confirm_delete))
                        .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                Route.findById(Route.class,
                                        ((Route) getListAdapter().getItem(position)).getId()).delete();
                                refreshList();
                            }
                        })
                        .setNegativeButton(getString(R.string.cancel), null)
                        .show();
                return true;
            }
        });

        refreshList();
    }

    private void refreshList() {
        routeList = Route.listAll(Route.class);
        setListAdapter(new RouteAdapter(getApplicationContext(), routeList));
    }
}
