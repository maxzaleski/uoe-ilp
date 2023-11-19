package uk.ac.ed.inf.lib.pathFinder;

import uk.ac.ed.inf.ilp.data.LngLat;
import uk.ac.ed.inf.ilp.data.NamedRegion;
import uk.ac.ed.inf.ilp.interfaces.LngLatHandling;
import uk.ac.ed.inf.lib.LngLatHandler;

import java.util.*;

public class PathFinder implements IPathFinder
{
    final private LngLatHandling lngLatHandler;
    final private NamedRegion boundary;
    final private NamedRegion[] noFlyZones;

    public PathFinder(NamedRegion boundary, NamedRegion[] noFlyZones)
    {
        this.lngLatHandler = new LngLatHandler();
        this.boundary = boundary;
        this.noFlyZones = noFlyZones;
    }

    public Result findRoute(LngLat fromPos, LngLat toPos)
    {
        final Queue<INode> openSet = new PriorityQueue<>();
        final Map<LngLat, INode> allNodes = new HashMap<>();

        INode current = new Node(null, new INode.Direction(fromPos), 0d, lngLatHandler.distanceTo(fromPos, toPos));
        openSet.add(current);
        allNodes.put(fromPos, current);

        while (!openSet.isEmpty())
        {
            current = openSet.poll();
            final LngLat currentPos = current.getDirection().position();

            // Check if the current node is close to the destination; if so, we have found a route.
            if (lngLatHandler.isCloseTo(currentPos, toPos))
            {
                // TODO: reconstruct path.
                final IPathFinder.Result result = new Result(new ArrayList<>(), true, 0d);
                result.route().add(toPos);
                while (current != null)
                {
                    result.route().add(currentPos);
                    current = current.getPrevious();
                }
                return result;
            }

            // Otherwise, continue searching.
            for (INode.Direction nextDir : getNeighbours(currentPos))
            {
                final LngLat nextPos = nextDir.position();
                final INode next = allNodes.getOrDefault(nextPos, new Node(nextDir));
                allNodes.put(nextPos, next);

                final double newScore =
                        current.getRouteScore() + lngLatHandler.distanceTo(currentPos, next.getDirection().position());
                if (newScore < next.getRouteScore())
                {
                    next.setPrevious(current);
                    next.setRouteScore(newScore);
                    next.setEstimatedScore(newScore + lngLatHandler.distanceTo(nextPos, toPos));
                    openSet.add(next);
                }
            }
        }

        return new Result(allNodes.keySet().stream().toList(), false, 0d);
    }

    /**
     * @param position the position to find the neighbours of.
     * @return the neighbours of the given position.
     */
    private List<INode.Direction> getNeighbours(LngLat position)
    {
        final List<INode.Direction> neighbours = new ArrayList<>();
        for (int i = 0; i < 16; i++)
        {
            final double angle = i * 22.5;
            final LngLat nextPosition = lngLatHandler.nextPosition(position, angle);
            if (!isWithinBoundary(nextPosition))
                neighbours.add(new INode.Direction(nextPosition, angle));
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

    private void reconstructPath(INode current, List<LngLat> path)
    {
        if (current == null)
            return;
        reconstructPath(current.getPrevious(), path);
        path.add(current.getDirection().position());
    }
}
