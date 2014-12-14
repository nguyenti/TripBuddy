package hu.ait.tiffanynguyen.tripbuddy.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import hu.ait.tiffanynguyen.tripbuddy.R;
import hu.ait.tiffanynguyen.tripbuddy.data.Route;

/**
 * Created by tiffanynguyen on 12/10/14.
 */
public class RouteAdapter extends BaseAdapter {

    private Context context;
    private List<Route> routeList;

    public RouteAdapter(Context context, List<Route> routeList) {
        this.context = context;
        this.routeList = routeList;
    }

    @Override
    public int getCount() {
        return routeList.size();
    }

    @Override
    public Object getItem(int position) {
        return routeList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void addRoute(Route p) {
        routeList.add(p);
    }

    public void removeRoute(int position) {
        if (position < routeList.size())
            routeList.remove(position);
    }

    public static class ViewHolder {
        TextView tvFrom;
        TextView tvTo;
        ImageView ivType;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);

            v = inflater.inflate(R.layout.row_route, null);
            ViewHolder holder = new ViewHolder();
            holder.tvFrom = (TextView) v.findViewById(R.id.tvFrom);
            holder.tvTo = (TextView) v.findViewById(R.id.tvTo);
            holder.ivType = (ImageView) v.findViewById(R.id.ivDetailsTravelType);
            v.setTag(holder);
        }

        final Route c = routeList.get(position);

        if (c != null) {
            ViewHolder holder = (ViewHolder) v.getTag();
            holder.tvFrom.setText(context.getString(R.string.label_from) + c.getStrStart());
            holder.tvTo.setText(context.getString(R.string.label_to) + c.getStrEnd());
            switch (c.getTravelMode()) {
                case R.drawable.ic_cycling:
                    holder.ivType.setImageResource(R.drawable.ic_cycling);
                    break;
                case R.drawable.ic_walk:
                    holder.ivType.setImageResource(R.drawable.ic_walk);
                    break;
                default:
                    holder.ivType.setImageResource(R.drawable.ic_car);
            }
        }

        return v;
    }
}
