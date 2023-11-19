package uk.ac.ed.inf.lib.pathFinder;

import uk.ac.ed.inf.ilp.data.LngLat;

/**
 * Represents a node in the route.
 */
public interface INode extends Comparable<INode>
{
    /**
     * @return The previous node in the route.
     */
    INode getPrevious();

    /**
     * @return The direction of the node.
     */
    Direction getDirection();

    /**
     * @return The score of the route to this node.
     */
    double getRouteScore();

    /**
     * @return The estimated score of the route to the restaurant.
     */
    double getEstimatedScore();

    /**
     * Sets the previous node in the route.
     */
    void setPrevious(INode previous);

    /**
     * Sets the score of the route to this node.
     */
    void setRouteScore(double routeScore);

    /**
     * Sets the estimated score of the route to the restaurant.
     */
    void setEstimatedScore(double estimatedScore);

    /**
     * Returns the outcome of {@link Double#compare(double, double)}.
     */
    @Override
    int compareTo(INode o);

    /**
     * Represents a {@link INode} direction.
     */
    record Direction(LngLat position, double angle)
    {
        /**
         * Constructs a direction with no angle.
         *
         * @param position the position of the direction.
         */
        Direction(LngLat position)
        {
            this(position, 999);
        }
    }
}
