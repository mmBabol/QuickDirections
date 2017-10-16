package Directions;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mmbab on 11/21/2016.
 *
 * Route holds all the information relative to directions, including the distance,
 * duration, start and end locations, list of all the points, and the directions
 */

public class Route {
    public Distance distance;
    public Duration duration;
    public String endAddress;
    public LatLng endLocation;
    public String startAddress;
    public LatLng startLocation;

    public List<LatLng> points;
    public ArrayList<String> directions;
}