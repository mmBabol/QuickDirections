package Directions;

/**
 * Created by mmbab on 11/21/2016.
 */

import java.util.List;

public interface DirectionFinderListener {
    void onDirectionFinderStart();
    void onDirectionFinderSuccess(List<Route> route);
}
