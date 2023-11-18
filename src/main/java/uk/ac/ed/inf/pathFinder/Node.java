package uk.ac.ed.inf.pathFinder;

import uk.ac.ed.inf.ilp.data.LngLat;

public class Node implements Comparable<Node>
{
    final private int id;
    final private LngLat position;
    private Node previous;
    private double routeScore;
    private double estimatedScore;

    public Node(int id, LngLat position)
    {
        this.id = id;
        this.previous = null;
        this.position = position;
        this.routeScore = Double.POSITIVE_INFINITY;
        this.estimatedScore = Double.POSITIVE_INFINITY;
    }

    public Node(int id, Node parent, LngLat position, double routeScore, double estimatedScore)
    {
        this.id = id;
        this.previous = parent;
        this.position = position;
        this.routeScore = routeScore;
        this.estimatedScore = estimatedScore;
    }

    public int getId()
    {
        return id;
    }

    public Node getPrevious()
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

    public void setPrevious(Node previous)
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
    public int compareTo(Node other)
    {
        if (this.estimatedScore > other.estimatedScore)
        {
            return 1;
        } else if (this.estimatedScore < other.estimatedScore)
        {
            return -1;
        } else
        {
            return 0;
        }
    }
}
