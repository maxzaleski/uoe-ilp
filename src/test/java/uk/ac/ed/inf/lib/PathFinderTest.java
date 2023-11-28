package uk.ac.ed.inf.lib;

import junit.framework.TestCase;
import uk.ac.ed.inf.ilp.data.LngLat;
import uk.ac.ed.inf.lib.pathFinder.INode;
import uk.ac.ed.inf.lib.pathFinder.IPathFinder;
import uk.ac.ed.inf.lib.pathFinder.PathFinder;

import java.util.ArrayList;
import java.util.List;

public class PathFinderTest extends TestCase
{
    private static final IPathFinder pathFinder = new PathFinder(null);

    public void testFindPath()
    {
        // [abstract]
        // For each leg of the final path (start→end, end→start), we aim to verify that:
        //  1. The first direction in the route is the starting position and has an angle of 999.0.
        //  2. All directions in the route, except the first and last, have an angle not equal to 999.0.
        //  3. The last direction in the route is the ending position and has an angle of 999.0.

        final LngLat startPos = new LngLat(-3.1869, 55.9445);
        final LngLat endPos = new LngLat(-3.1912869215011597, 55.945535152517735);

        final List<INode.Direction> route = new ArrayList<>();

        IPathFinder.Result result = pathFinder.findRoute(startPos, endPos);
        assertTrue(result.getOk());

        route.addAll(result.getRoute());

        result = pathFinder.findRoute(endPos, startPos);
        assertTrue(result.getOk());

        route.addAll(result.getRoute());

        // [first leg: startPos -> endPos]

        // [1]
        INode.Direction dir = route.get(0);
        assertSame(dir.position(), startPos);
        assertEquals(dir.angle(), 999.0);

        // [2]
        for (int i = 1; i < 31; i++)
        {
            dir = route.get(i);
            assertTrue(dir.angle() != 999.0);
        }

        // [3]
        dir = route.get(31);
        assertSame(dir.position(), endPos);
        assertEquals(dir.angle(), 999.0);

        // [second leg: endPos -> startPos]

        // [1]
        dir = route.get(32);
        assertSame(dir.position(), endPos);
        assertEquals(dir.angle(), 999.0);

        // [2]
        for (int i = 33; i < route.size() - 1; i++)
        {
            dir = route.get(i);
            assertTrue(dir.angle() != 999.0);
        }

        // [3]
        dir = route.get(route.size() - 1);
        assertSame(dir.position(), startPos);
        assertEquals(dir.angle(), 999.0);
    }

    public void testFindPath_IllegalArgumentException_Null()
    {
        final LngLat[][] cases = new LngLat[][]{
                {null, new LngLat(0, 0)},
                {new LngLat(0, 0), null},
        };
        for (LngLat[] positions : cases)
        {
            try
            {
                pathFinder.findRoute(positions[0], positions[1]);
                fail("expected 'IllegalArgumentException' to be thrown");
            } catch (IllegalArgumentException e)
            {
                // Caught as expected.
                assertTrue(e.getMessage().contains("positions cannot be null"));
            }
        }
    }

    public void testFindPath_IllegalArgumentException_Equals()
    {
        try
        {
            pathFinder.findRoute(new LngLat(0, 0), new LngLat(0, 0));
            fail("expected 'IllegalArgumentException' to be thrown");
        } catch (IllegalArgumentException e)
        {
            // Caught as expected.
            assertTrue(e.getMessage().contains("positions are the same"));
        }
    }
}
