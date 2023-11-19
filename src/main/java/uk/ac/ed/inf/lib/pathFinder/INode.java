package uk.ac.ed.inf.lib.pathFinder;

import uk.ac.ed.inf.ilp.data.LngLat;

/**
 * Represents a node in the path.
 */
public interface INode extends Comparable<INode>
{
    /**
     * @return The previous node in the path.
     */
    INode getPrevious();

    /**
     * Sets the previous node in the path.
     */
    void setPrevious(INode previous);

    /**
     * @return The direction of the node.
     */
    Direction getDirection();

    /**
     * @return The score of the path to this node (gScore).
     */
    double getRouteScore();

    /**
     * Sets the score of the path to this node.
     */
    void setRouteScore(double routeScore);

    /**
     * @return The estimated score of the path to the restaurant.
     */
    double getEstimatedScore();

    /**
     * Sets the estimated score of the node to the destination (fScore).
     */
    void setEstimatedScore(double estimatedScore);

    /**
     * Returns the outcome of {@link Double#compare(double, double)}.
     */
    @Override
    int compareTo(INode o);

    /**
     * Represents a {@link INode} direction.
     * <p>
     * An angle of 999 is used to indicate that the bearing is unknown (the drone is hovering at the position).
     */
    record Direction(LngLat position, double angle, long ticksSinceStart)
    {
        /**
         * Constructs a direction with no angle.
         *
         * @param position the position of the direction.
         */
        Direction(LngLat position, long ticksSinceStart)
        {
            this(position, 999, ticksSinceStart);
        }
    }
}
