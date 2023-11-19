package uk.ac.ed.inf.lib.pathFinder;

import uk.ac.ed.inf.ilp.data.LngLat;

public class Node implements INode
{
    final private LngLat position;
    private INode previous;
    private double routeScore;
    private double estimatedScore;

    /**
     * Constructs an empty node.
     *
     * @param position the position of the node.
     */
    public Node(LngLat position)
    {
        this.position = position;
        this.previous = null;
        this.routeScore = Double.POSITIVE_INFINITY;
        this.estimatedScore = Double.POSITIVE_INFINITY;
    }

    /**
     * Constructs a node.
     *
     * @param previous       the previous node in the route.
     *                       if this is null, then this node is the starting node.
     * @param position       the position of the node.
     * @param routeScore     the score of the route to this node.
     *                       if this is {@link Double#POSITIVE_INFINITY}, then this node is unreachable.
     * @param estimatedScore the estimated score of the route to the restaurant.
     *                       if this is {@link Double#POSITIVE_INFINITY}, then this node is unreachable.
     */
    public Node(INode previous, LngLat position, double routeScore, double estimatedScore)
    {
        this.previous = previous;
        this.position = position;
        this.routeScore = routeScore;
        this.estimatedScore = estimatedScore;
    }

    public INode getPrevious()
    {
        return previous;
    }

    public LngLat getPosition()
    {
        return position;
    }

    public double getRouteScore()
    {
        return routeScore;
    }

    public double getEstimatedScore()
    {
        return estimatedScore;
    }

    public void setPrevious(INode previous)
    {
        this.previous = previous;
    }

    public void setRouteScore(double routeScore)
    {
        this.routeScore = routeScore;
    }

    public void setEstimatedScore(double estimatedScore)
    {
        this.estimatedScore = estimatedScore;
    }

    @Override
    public int compareTo(INode o)
    {
        return Double.compare(this.estimatedScore, o.getEstimatedScore());
    }
}
