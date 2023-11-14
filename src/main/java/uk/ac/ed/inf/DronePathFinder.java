package uk.ac.ed.inf;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.ac.ed.inf.ilp.data.LngLat;
import uk.ac.ed.inf.ilp.data.NamedRegion;

/**
 * Encapsulates the logic for finding the shortest path between Appleton Tower and a given restaurant.
 */
public class DronePathFinder
{
    final private NamedRegion centralArea;
    final private NamedRegion[] noFlyZones;

    public DronePathFinder(NamedRegion centralArea, NamedRegion[] noFlyZones)
    {
        this.centralArea = centralArea;
        this.noFlyZones = noFlyZones;
    }

    /**
     * Finds the shortest path between Appleton Tower and the given restaurant.
     */
    public Move[] findShortestPath(LngLat restaurantCoords)
    {
        return null;
    }

    /**
     * Represents a single move from the flight path.
     */
    static public class Move
    {
        @JsonProperty("fromLongitude")
        final protected double fromLongitude;
        @JsonProperty("fromLatitude")
        final protected double fromLatitude;
        @JsonProperty("angle")
        final protected double angle;
        @JsonProperty("toLongitude")
        final protected double toLongitude;
        @JsonProperty("toLatitude")
        final protected double toLatitude;
        @JsonProperty("ticksSinceStartOfCalculation")
        final protected int ticksSinceStartOfCalculation;

        /**
         * @param from                         the coordinates of the drone at the start of this move.
         * @param to                           the coordinates of the drone at the end of this move.
         * @param angle                        the angle of travel of the drone in this move.
         * @param ticksSinceStartOfCalculation the elapsed ticks since the computation started for the day.
         */
        public Move(LngLat from, LngLat to, double angle, int ticksSinceStartOfCalculation)
        {
            this.fromLongitude = from.lng();
            this.fromLatitude = from.lat();
            this.angle = angle;
            this.toLongitude = to.lng();
            this.toLatitude = to.lat();
            this.ticksSinceStartOfCalculation = ticksSinceStartOfCalculation;
        }

        public Move(Move move)
        {
            this.fromLongitude = move.fromLongitude;
            this.fromLatitude = move.fromLatitude;
            this.angle = move.angle;
            this.toLongitude = move.toLongitude;
            this.toLatitude = move.toLatitude;
            this.ticksSinceStartOfCalculation = move.ticksSinceStartOfCalculation;
        }
    }
}
