package uk.ac.ed.inf.lib.pathFinder;

import uk.ac.ed.inf.ilp.data.LngLat;

import java.util.ArrayList;
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
     */
    class Result
    {
        private final List<INode.Direction> route;
        private boolean ok;

        public Result()
        {
            this.route = new ArrayList<>();
            this.ok = false;
        }

        /**
         * @return true if a route was found, false otherwise.
         */
        public boolean getOk()
        {
            return ok;
        }

        /**
         * @return the directions constituting the route; empty if no route was found.
         */
        public List<INode.Direction> getRoute()
        {
            return route;
        }

        /**
         * Sets the outcome of the search.
         */
        public void setOK(boolean ok)
        {
            this.ok = ok;
        }

        /**
         * Reconstructs the path from the given node to the starting node.
         */
        public void setRoute(INode.Direction from, INode current)
        {
            route.add(from);
            while (current != null)
            {
                route.add(current.getDirection());
                current = current.getPrevious();
            }
        }
    }
}
