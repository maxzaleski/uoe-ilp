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
     * @throws RuntimeException if an unexpected error occurs during execution.
     */
    Result findRoute(LngLat from, LngLat to) throws RuntimeException;

    /**
     * Represents the result of a path finding operation.
     */
    class Result
    {
        private List<INode.Direction> path;
        private String orderNo;
        private boolean ok;

        public Result()
        {
            this.ok = false;
            this.orderNo = "";
            this.path = new ArrayList<>();
        }

        /**
         * @return the order number associated with the path.
         */
        public String getOrderNo()
        {
            return orderNo;
        }

        /**
         * Sets the order number associated with the path.
         */
        public void setOrderNo(String orderNo)
        {
            this.orderNo = orderNo;
        }

        /**
         * @return true if a path was found, false otherwise.
         */
        public boolean getOk()
        {
            return ok;
        }

        /**
         * @return the directions constituting the path; empty if no path was found.
         */
        public List<INode.Direction> getRoute()
        {
            return path;
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
         *
         * @param end     the actual end final node (destination).
         * @param current the current node.
         */
        public void setRoute(INode.Direction end, INode current)
        {
            // [requirement] the drone must hover for one move at its destination (restaurant or when delivery to AT).
            path.add(end);
            while (current != null)
            {
                path.add(current.getDirection());
                current = current.getPrevious();
            }

            // Assign the sorted list of directions as the path.
            path = path.stream()
                    .sorted(Comparator.comparingLong(INode.Direction::ticksSinceStart))
                    .toList();
        }
    }
}
