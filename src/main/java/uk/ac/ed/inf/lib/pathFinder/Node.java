package uk.ac.ed.inf.lib.pathFinder;

public class Node implements INode
{
    final private Direction direction;
    private INode previous;
    private double routeScore;
    private double estimatedScore;

    /**
     * Constructs an empty node.
     *
     * @param direction the position of the node.
     */
    public Node(Direction direction)
    {
        this.direction = direction;
        this.previous = null;
        this.routeScore = Double.POSITIVE_INFINITY;
        this.estimatedScore = Double.POSITIVE_INFINITY;
    }

    /**
     * Constructs a node.
     *
     * @param previous       the previous node in the path.
     *                       if this is null, then this node is the starting node.
     * @param direction      the direction of the node.
     * @param routeScore     the score of the path to this node.
     *                       if this is {@link Double#POSITIVE_INFINITY}, then this node is unreachable.
     * @param estimatedScore the estimated score of the path to the restaurant.
     *                       if this is {@link Double#POSITIVE_INFINITY}, then this node is unreachable.
     */
    public Node(INode previous, Direction direction, double routeScore, double estimatedScore)
    {
        this.previous = previous;
        this.direction = direction;
        this.routeScore = routeScore;
        this.estimatedScore = estimatedScore;
    }

    public INode getPrevious()
    {
        return previous;
    }

    public void setPrevious(INode previous)
    {
        this.previous = previous;
    }

    public Direction getDirection()
    {
        return direction;
    }

    public double getRouteScore()
    {
        return routeScore;
    }

    public void setRouteScore(double routeScore)
    {
        this.routeScore = routeScore;
    }

    public double getEstimatedScore()
    {
        return estimatedScore;
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
