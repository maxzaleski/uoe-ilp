package uk.ac.ed.inf.lib.pathFinder;

import uk.ac.ed.inf.ilp.data.LngLat;

import java.util.ArrayList;
import java.util.Comparator;
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
        private List<INode.Direction> route;
        private String orderNo;
        private boolean ok;

        public Result()
        {
            this.ok = false;
            this.orderNo = "";
            this.route = new ArrayList<>();
        }

        /**
         * @return the order number associated with the route.
         */
        public String getOrderNo()
        {
            return orderNo;
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
         * Sets the order number associated with the route.
         */
        public void setOrderNo(String orderNo)
        {
            this.orderNo = orderNo;
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

            // Assign the sorted list of directions as the route.
            route = route.stream()
                    .sorted(Comparator.comparingLong(INode.Direction::ticksSinceStart))
                    .toList();
        }
    }
}
