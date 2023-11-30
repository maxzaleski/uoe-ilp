package uk.ac.ed.inf.lib;

import junit.framework.TestCase;
import uk.ac.ed.inf.ilp.data.LngLat;
import uk.ac.ed.inf.lib.pathFinder.IPathFinder;
import uk.ac.ed.inf.lib.pathFinder.PathFinder;

public class PathFinderTest extends TestCase
{
    private static final IPathFinder pathFinder = new PathFinder(null);

    public void testFindPath()
    {
        final LngLat startPos = new LngLat(-3.1869, 55.9445);
        final LngLat endPos = new LngLat(-3.1912869215011597, 55.945535152517735);

        IPathFinder.Result result = pathFinder.findRoute(startPos, endPos);
        assert result.getOk();

        var route = result.getRoute();
        assert !route.isEmpty();

        for (int i = 0; i < route.size(); i++)
        {
            var direction = route.get(i);
            if (i == 0)
            {
                assert direction.position().equals(startPos);
                assert direction.angle() == 999.0;
            } else if (i == route.size() - 1)
            {
                assert direction.position().equals(endPos);
                assert direction.angle() == 999.0;
            } else
            {
                assert direction.angle() % LngLatHandler.ANGLE_MULTIPLE == 0;
                assert direction.angle() != 999.0;
            }
        }
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
            assertTrue(e.getMessage().contains("equal"));
        }
    }
}
