package hu.ait.tiffanynguyen.tripbuddy.data;

import com.google.android.gms.maps.model.LatLng;
import com.orm.SugarRecord;

import java.io.Serializable;

import hu.ait.tiffanynguyen.tripbuddy.R;

/**
 * Created by tiffanynguyen on 12/10/14.
 */
public class Route extends SugarRecord<Route> implements Serializable {

    double startLat;
    double startLng;
    String strStart;
    double endLat;
    double endLng;
    String strEnd;
    String json;
    int travelMode;

    public Route() {}

    public Route(LatLng start, LatLng end) {
        this.startLat = start.latitude;
        this.startLng = start.longitude;
        this.endLat = end.latitude;
        this.endLng = end.longitude;
    }

    public Route(String strStart, String strEnd) {
        this.strStart = strStart;
        this.strEnd = strEnd;
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

    public double getStartLat() {
        return startLat;
    }

    public void setStartLat(double startLat) {
        this.startLat = startLat;
    }

    public double getStartLng() {
        return startLng;
    }

    public void setStartLng(double startLng) {
        this.startLng = startLng;
    }

    public double getEndLat() {
        return endLat;
    }

    public void setEndLat(double endLat) {
        this.endLat = endLat;
    }

    public double getEndLng() {
        return endLng;
    }

    public void setStart(LatLng start) {
        this.startLat = start.latitude;
        this.startLng = start.longitude;
    }

    public LatLng getStart() {
        return new LatLng(startLat, startLng);
    }

    public void setEnd(LatLng end) {
        this.endLat = end.latitude;
        this.endLng = end.longitude;
    }

    public int getTravelMode() {
        return travelMode;
    }

    public void setTravelMode(int travelMode) {
        this.travelMode = travelMode;
    }

    public LatLng getEnd() {
        return new LatLng(endLat, endLng);
    }

    public void setEndLng(double endLng) {
        this.endLng = endLng;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }

    public LatLng[] getStartEnd() {
        LatLng ll[] = new LatLng[2];
        ll[0] = new LatLng(startLat, startLng);
        ll[1] = new LatLng(endLat, endLng);

        return ll;
    }

    public LatLng getMidpoint() {
        return new LatLng(startLat/2 + endLat/2, startLng/2 + endLng/2);
    }

    public String toString() {
        return "\n Start: " + startLat + "," + startLng + " - " + strStart + "\n"
                + "End: " + endLat + "," + endLng + " - " + strEnd + "\n";
    }
}
