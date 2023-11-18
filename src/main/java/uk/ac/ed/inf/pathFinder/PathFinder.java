package uk.ac.ed.inf.pathFinder;

import uk.ac.ed.inf.LngLatHandler;
import uk.ac.ed.inf.ilp.data.LngLat;
import uk.ac.ed.inf.ilp.data.NamedRegion;

import java.util.*;

public class PathFinder
{
    final private Graph graph;
    final private LngLatHandler lngLatHandler;
    final private Random nonce;

    public PathFinder(String nonceSeed, NamedRegion boundary, NamedRegion[] noFlyZones)
    {
        this.nonce = new Random(nonceSeed.hashCode());
        this.lngLatHandler = new LngLatHandler();
        this.graph = new Graph(lngLatHandler, boundary, noFlyZones);
    }

    public List<LngLat> findRoute(LngLat from, LngLat to)
    {
        Queue<Node> openSet = new PriorityQueue<>();
        Map<LngLat, Node> allNodes = new HashMap<>();

        final Node start = new Node(nonce.nextInt(), null, from, 0d, lngLatHandler.distanceTo(from, to));
        openSet.add(start);
        allNodes.put(from, start);

        List<LngLat> TEST_OUT = new ArrayList<>();
        while (!openSet.isEmpty())
        {
            Node next = openSet.poll();
            if (lngLatHandler.isCloseTo(next.getPosition(), to))
            {
                System.out.println("Found route!");
                TEST_OUT = new ArrayList<>();
                while (next != null)
                {
                    TEST_OUT.add(next.getPosition());
                    next = next.getPrevious();
                }
//                List<Node> route = new ArrayList<>();
//                return route;
                return TEST_OUT;
            }


            List<LngLat> neighbours;

            try
            {
                neighbours = graph.getNeighbours(next.getPosition());
                TEST_OUT.addAll(neighbours);
            } catch (Exception e)
            {
                return TEST_OUT;
            }

            for (LngLat connectionCoords : neighbours)
            {
                Node connectionNode = allNodes.getOrDefault(connectionCoords, new Node(nonce.nextInt(), connectionCoords));
                allNodes.put(connectionCoords, connectionNode);

                double newScore = next.getRouteScore() + lngLatHandler.distanceTo(next.getPosition(), connectionNode.getPosition());
                if (newScore < connectionNode.getRouteScore())
                {
                    connectionNode.setPrevious(next);
                    connectionNode.setRouteScore(newScore);
                    connectionNode.setEstimatedScore(newScore + lngLatHandler.distanceTo(connectionCoords, to));
                    openSet.add(connectionNode);
                }
            }
        }
        System.out.println("No route found");
        return TEST_OUT;
//        throw new IllegalStateException("No route found");
    }
}
