package uk.ac.ed.inf.lib.pathFinder;

import uk.ac.ed.inf.ilp.data.LngLat;

import java.util.List;

/**
 * Represents a graph of the drone's base of operation and its destination.
 */
public interface IPathFinder
{
    /**
     * Performs a path finding operation.
     *
     * @param from the starting position.
     * @param to   the ending position.
     * @return the result of the search.
     */
    Result findRoute(LngLat from, LngLat to);

    /**
     * Represents the result of a path finding operation.
     *
     * @param route the route or visited nodes if no route was found.
     * @param ok    true if a route was found, false otherwise.
     * @param time  the time taken to find the route.
     */
    record Result(List<LngLat> route, boolean ok, double time)
    {
        /**
         * @return true if a route was found, false otherwise.
         */
        @Override
        public boolean ok()
        {
            return ok;
        }

        /**
         * @return the time taken to find the route.
         */
        @Override
        public double time()
        {
            return time;
        }

        /**
         * @return the route or visited nodes if no route was found.
         */
        @Override
        public List<LngLat> route()
        {
            return route;
        }
    }
}
