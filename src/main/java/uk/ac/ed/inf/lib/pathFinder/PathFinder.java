package uk.ac.ed.inf.lib.pathFinder;

import uk.ac.ed.inf.ilp.data.LngLat;
import uk.ac.ed.inf.ilp.data.NamedRegion;
import uk.ac.ed.inf.lib.LngLatHandler;

import java.util.*;

public class PathFinder implements IPathFinder
{
    final private long startTime;

    /**
     * The implementation class is preferred over the interface as it provides additional context; this is due to the
     * artifact not containing any documentation for the interface.
     *
     * @see uk.ac.ed.inf.ilp.interfaces.LngLatHandling
     */
    final private LngLatHandler lngLatHandler;

    final private NamedRegion boundary;
    final private NamedRegion[] noFlyZones;

    public PathFinder(NamedRegion boundary, NamedRegion[] noFlyZones)
    {
        this.startTime = System.nanoTime();
        this.lngLatHandler = new LngLatHandler();

        this.boundary = boundary;
        this.noFlyZones = noFlyZones;
    }

    public Result findRoute(LngLat fromPos, LngLat toPos)
    {
        // [abstract]
        // This method finds the shortest path between the two positions using the A* algorithm.
        //
        // The following resources were used in the below adaptation:
        // [1] https://en.wikipedia.org/wiki/A*_search_algorithm
        //     → provides a high-level overview of the algorithm and its implementation.
        // [2] https://www.baeldung.com/java-a-star-pathfinding
        //     → provides a working example of the algorithm in Java using the London Underground network as the
        //       operating graph.
        //     → our implementation is simplified, and generates nodes on each iteration (`getNeighbours`) rather than
        //       using pre-defined ones.

        final IPathFinder.Result result = new Result();
        result.getRoute().add(new INode.Direction(fromPos, getTicksSinceStart()));

        // A priority queue which sorts nodes by their estimated score (fScore).
        final Queue<INode> openSet = new PriorityQueue<>();
        // A map of all nodes visited|generated so far.
        final Map<LngLat, INode> allNodes = new HashMap<>();

        // Begin the calculation by adding the starting position to the queue.
        INode current = new Node(null,
                new INode.Direction(fromPos, getTicksSinceStart()),
                0d,
                lngLatHandler.distanceTo(fromPos, toPos));
        openSet.add(current);
        allNodes.put(fromPos, current);

        try
        {
            // While there are nodes to consider:
            while (!openSet.isEmpty())
            {
                current = openSet.poll();
                final LngLat currentPos = current.getDirection().position();

                // Check if the current node is close to the destination; if so, we have found a route.
                // Note the use of `isCloseTo` rather than `equals` to account for possible inaccuracies in the position
                // calculation.
                if (lngLatHandler.isCloseTo(currentPos, toPos))
                {
                    result.setOK(true);
                    result.setRoute(new INode.Direction(toPos, getTicksSinceStart()), current);
                    break;
                }

                // Otherwise, generate 16 neighbours (one for each of the 16 possible bearings) and keep searching.
                for (INode.Direction nextDir : getNeighbours(currentPos))
                {
                    final LngLat nextPos = nextDir.position();
                    final INode next = allNodes.getOrDefault(nextPos, new Node(nextDir));
                    allNodes.put(nextPos, next);

                    final double newScore =
                            current.getRouteScore() + lngLatHandler.distanceTo(currentPos, next.getDirection().position());

                    // If the newly calculated score is better than the previously known one, the node's properties are
                    // updated accordingly, and added to the queue as to be considered in a later iteration.
                    if (newScore < next.getRouteScore())
                    {
                        next.setPrevious(current);
                        next.setRouteScore(newScore); // [gScore]
                        next.setEstimatedScore(newScore + lngLatHandler.distanceTo(nextPos, toPos)); // [fScore]
                        openSet.add(next);
                    }
                }
            }
        } catch (Exception e)
        {
            final String msg = String.format("unexpected error while calculating the shortest path from %s to %s: %s",
                    fromPos.toString(),
                    toPos.toString(),
                    e.getMessage() == null ? "no message given" : e.getMessage());
            throw new RuntimeException(msg, e);
        }

        return result;
    }

    /**
     * @param position the initial position to find the neighbours of.
     * @return the {@value LngLatHandler#BEARING_COUNT} neighbours of the given position.
     */
    private List<INode.Direction> getNeighbours(LngLat position)
    {
        final List<INode.Direction> neighbours = new ArrayList<>();
        for (int i = 0; i < LngLatHandler.BEARING_COUNT; i++)
        {
            final double angle = i * LngLatHandler.ANGLE_MULTIPLE;
            final LngLat nextPosition = lngLatHandler.nextPosition(position, angle);
            if (!isWithinBoundary(nextPosition))
                neighbours.add(new INode.Direction(nextPosition, angle, getTicksSinceStart()));
        }
        return neighbours;
    }

    /**
     * Checks if the given position is within any of the no-fly zones.
     *
     * @param position the position to check.
     * @return true if the position is not within the no-fly zones, false otherwise.
     */
    private boolean isWithinBoundary(LngLat position)
    {
        for (NamedRegion noFlyZone : noFlyZones)
        {
            if (lngLatHandler.isInRegion(position, noFlyZone))
                return true;
        }
        return false;
    }

    /**
     * @return the number of nanoseconds since the start of the program.
     */
    private long getTicksSinceStart()
    {
        return System.nanoTime() - startTime;
    }
}
