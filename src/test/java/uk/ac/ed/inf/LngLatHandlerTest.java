package uk.ac.ed.inf;

import junit.framework.TestCase;
import uk.ac.ed.inf.ilp.data.LngLat;
import uk.ac.ed.inf.ilp.data.NamedRegion;

public class LngLatHandlerTest extends TestCase
{
    final private LngLatHandler handler = new LngLatHandler();

    public void testDistanceTo()
    {
        final LngLat pointA = new LngLat(3, 2);
        final LngLat pointB = new LngLat(4, 1);

        // If two points are the same, the distance should be 0.
        assertEquals(0.0, handler.distanceTo(pointA, pointA));

        final double expectedDistance = Math.sqrt(2);

        // Check that the distance is the same regardless of the order of the points.
        assertEquals(expectedDistance, handler.distanceTo(pointA, pointB));
        assertEquals(expectedDistance, handler.distanceTo(pointB, pointA));
    }

    public void testDistanceTo_IllegalArgumentException()
    {
        final LngLat[][] cases = new LngLat[][]{
                {null, new LngLat(0, 0)},
                {new LngLat(0, 0), null},
        };
        for (LngLat[] positions : cases)
        {
            try
            {
                handler.distanceTo(positions[0], positions[1]);
                fail("expected 'IllegalArgumentException' to be thrown");
            } catch (IllegalArgumentException e)
            {
                // Caught as expected.
                assertTrue(e.getMessage().contains("positions"));
            }
        }
    }

    public void testIsCloseTo()
    {
        final LngLat startPosition = new LngLat(0.0, 0.0);

        // The two points are close to each other.
        assertTrue(handler.isCloseTo(startPosition, new LngLat(0.0001, 0.0001)));
        // The two points are not close to each other.
        assertFalse(handler.isCloseTo(startPosition, new LngLat(0.001, 0.001)));
    }

    public void testIsCloseTo_IllegalArgumentException()
    {
        final LngLat[][] cases = new LngLat[][]{
                {null, new LngLat(0, 0)},
                {new LngLat(0, 0), null},
        };
        for (LngLat[] positions : cases)
        {
            try
            {
                handler.isCloseTo(positions[0], positions[1]);
                fail("expected 'IllegalArgumentException' to be thrown");
            } catch (IllegalArgumentException e)
            {
                // Caught as expected.
                assertTrue(e.getMessage().contains("positions"));
            }
        }
    }

    public void testIsInCentralArea()
    {
        // This unit also indirectly tests `isInRegion` on a rectangle (polygon with 4 vertices).
        final NamedRegion centralArea = new NamedRegion("central", new LngLat[]{
                new LngLat(-3.192473, 55.946233),
                new LngLat(-3.192473, 55.942617),
                new LngLat(-3.184319, 55.942617),
                new LngLat(-3.184319, 55.946233),
        });

        // The point is within the central area.
        assertTrue(handler.isInCentralArea(new LngLat(-3.188396, 55.944), centralArea));
        // The point is not within the central area.
        assertFalse(handler.isInCentralArea(new LngLat(-3.2, 55.944), centralArea));
    }

    public void testIsInCentralArea_IllegalArgumentException()
    {
        final NamedRegion[] cases = new NamedRegion[]{
                null,
                new NamedRegion("", null)
        };
        for (NamedRegion region : cases)
        {
            try
            {
                // N.B. `isInCentralArea` utilises the `isInRegion` method, which already checks for a null point.
                // The null point will be caught later in the call stack.
                handler.isInCentralArea(null, region);
                fail("expected 'IllegalArgumentException' to be thrown");
            } catch (IllegalArgumentException e)
            {
                // Caught as expected.
                assertTrue(e.getMessage().contains("region"));
            }
        }
    }

    public void testIsInRegion()
    {
        // Establishes a closed polygon with 11 vertices (long|lat ranges have been omitted for testing purposes).
        // → Visualisation: https://www.desmos.com/calculator/sucfr9257y
        final NamedRegion polygon = new NamedRegion("Polygon", new LngLat[]{
                new LngLat(0, 20),    // A
                new LngLat(-10, 20),  // B
                new LngLat(-10, 5),   // C
                new LngLat(-20, 10),  // D
                new LngLat(-30, 0),   // E
                new LngLat(-5, -5),   // F
                new LngLat(-20, -25), // G
                new LngLat(0, -20),   // H
                new LngLat(20, -20),  // I
                new LngLat(0, 0),     // J
                new LngLat(10, 10),   // K
        });

        // Tests points at various locations within the polygon:
        assertTrue(handler.isInRegion(new LngLat(0, 10), polygon));    // L
        assertTrue(handler.isInRegion(new LngLat(7, 12.99), polygon)); // M
        assertTrue(handler.isInRegion(new LngLat(-28, 1), polygon));   // N
        assertTrue(handler.isInRegion(new LngLat(-2, -2), polygon));   // O
        assertTrue(handler.isInRegion(new LngLat(10, -15), polygon));  // P

        // Tests points at various locations outside the polygon:
        assertFalse(handler.isInRegion(new LngLat(5, 0), polygon));    // R
        assertFalse(handler.isInRegion(new LngLat(-5, -23), polygon)); // S
        assertFalse(handler.isInRegion(new LngLat(-11, 6), polygon));  // T
        assertFalse(handler.isInRegion(new LngLat(-6, -5), polygon));  // U

        // [re: Remark:108] This is an example of case where the coordinates are exactly on the edge of two vertices (A & B).
        // This test should be amended to check for this case if the condition were to change in the future.
        assertFalse(handler.isInRegion(new LngLat(-5, 20), polygon));
        assertTrue(handler.isInRegion(new LngLat(-5, 19.9), polygon)); // Q
    }

    public void testIsInRegion_IllegalArgumentException_Position()
    {
        try
        {
            handler.isInRegion(null, null);
            fail("expected 'IllegalArgumentException' to be thrown");
        } catch (IllegalArgumentException e)
        {
            // Caught as expected.
            assertTrue(e.getMessage().contains("position"));
        }
    }

    public void testIsInRegion_IllegalArgumentException_Region()
    {
        final NamedRegion[] cases = new NamedRegion[]{
                null,
                new NamedRegion("",
                        // A region must be a closed polygon, thus must have at least 3 vertices.
                        new LngLat[]{
                                new LngLat(0, 0),
                                new LngLat(0, 0)
                        }
                )
        };
        for (NamedRegion region : cases)
        {
            try
            {
                handler.isInRegion(new LngLat(0, 0), region);
                fail("expected 'IllegalArgumentException' to be thrown");
            } catch (IllegalArgumentException e)
            {
                // Caught as expected.
                assertTrue(e.getMessage().contains("region"));
            }
        }
    }

    public void testNextPosition()
    {
        final double tolerance = 1E-17; // Necessary as decimal rounding will differ in runtime environment.

        double[][] cases = new double[][]{
                // [indexing]
                // 0: expected longitude
                // 1: expected latitude
                // 2: functional angle (in degrees)
                new double[]{1.0606601717798212E-4, 1.0606601717798212E-4, 45},
                new double[]{1.5000000000000001E-4, 0, 0}
        };
        for (double[] values : cases)
        {
            final LngLat nextPoint = handler.nextPosition(new LngLat(0, 0), values[2]);

            assertEquals(values[0], nextPoint.lng(), tolerance);
            assertEquals(values[1], nextPoint.lat(), tolerance);
        }
    }

    public void testNextPosition_IllegalArgumentException_Angle()
    {
        double[] cases = new double[]{
                -1,
                50.2,
                752,
                271,
                37.5,
                356.25,
        };
        for (double angle : cases)
        {
            try
            {
                handler.nextPosition(new LngLat(0, 0), angle);
                fail("expected 'IllegalArgumentException' to be thrown");
            } catch (IllegalArgumentException e)
            {
                // Caught as expected.
                assertTrue(e.getMessage().contains("angle"));
            }
        }
    }

    public void testNextPosition_IllegalArgumentException_StartPosition()
    {
        try
        {
            handler.nextPosition(null, 0);
            fail("expected 'IllegalArgumentException' to be thrown");
        } catch (IllegalArgumentException e)
        {
            // Caught as expected.
            assertTrue(e.getMessage().contains("position"));
        }
    }
}