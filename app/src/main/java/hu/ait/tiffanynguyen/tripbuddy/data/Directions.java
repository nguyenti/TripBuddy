package hu.ait.tiffanynguyen.tripbuddy.data;

import com.google.android.gms.maps.model.LatLng;
import com.orm.SugarRecord;

/**
 * Created by tiffanynguyen on 12/10/14.
 */
public class Directions extends SugarRecord<Directions> {

    private LatLng start;
    private LatLng end;

    Directions() {}

    public Directions(LatLng start, LatLng end) {
        this.start = start;
        this.end = end;
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

    public LatLng getMidpoint() {
        return new LatLng(start.latitude/2 + end.latitude/2, start.longitude/2 + end.longitude/2);
    }
}
