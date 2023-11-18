package uk.ac.ed.inf.pathFinder;

import uk.ac.ed.inf.LngLatHandler;
import uk.ac.ed.inf.ilp.data.LngLat;
import uk.ac.ed.inf.ilp.data.NamedRegion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Graph
{
    final Map<LngLat, Node> nodes = new HashMap<>();
    final private LngLatHandler lngLatHandler;
    final private NamedRegion boundary;
    final private NamedRegion[] noFlyZones;

    private int iterations = 20;

    public Graph(LngLatHandler handler, NamedRegion boundary, NamedRegion[] NoFlyZone)
    {
        this.lngLatHandler = handler;
        this.boundary = boundary;
        this.noFlyZones = NoFlyZone;
    }

    public List<LngLat> getNeighbours(LngLat position)
    {
//        if (iterations == 0) throw new RuntimeException("Too many iterations");
//        iterations--;
        List<LngLat> neighbours = new ArrayList<>();
        for (int i = 0; i < 16; i++)
        {
            LngLat nextPosition = lngLatHandler.nextPosition(position, i * 22.5);
            if (nodes.containsKey(nextPosition)) continue;
//            if (lngLatHandler.isInRegion(nextPosition, boundary) && !noFlyZonesContain(nextPosition))
            if (!noFlyZonesContain(nextPosition))
            {
                neighbours.add(nextPosition);
            }
        }
        return neighbours;
    }

    private boolean noFlyZonesContain(LngLat position)
    {
        for (NamedRegion noFlyZone : noFlyZones)
        {
            if (lngLatHandler.isInRegion(position, noFlyZone))
                return true;
        }
        return false;
    }
}
