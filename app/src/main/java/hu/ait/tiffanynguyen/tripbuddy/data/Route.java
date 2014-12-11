package hu.ait.tiffanynguyen.tripbuddy.data;

import com.google.android.gms.maps.model.LatLng;
import com.orm.SugarRecord;

import java.io.Serializable;

/**
 * Created by tiffanynguyen on 12/10/14.
 */
public class Route extends SugarRecord<Route> implements Serializable {

    LatLng start;
    String strStart;
    LatLng end;
    String strEnd;
    String json;

    Route() {}

    public Route(LatLng start, LatLng end) {
        this.start = start;
        this.end = end;
    }

    public String getStrStart() {
        return strStart;
    }

    public void setStrStart(String strStart) {
        this.strStart = strStart;
    }

    public String getStrEnd() {
        return strEnd;
    }

    public void setStrEnd(String strEnd) {
        this.strEnd = strEnd;
    }

    public LatLng getStart() {
        return start;
    }

    public void setStart(LatLng start) {
        this.start = start;
    }

    public LatLng getEnd() {
        return end;
    }

    public void setEnd(LatLng end) {
        this.end = end;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }

    public LatLng getMidpoint() {
        return new LatLng(start.latitude/2 + end.latitude/2, start.longitude/2 + end.longitude/2);
    }
}
