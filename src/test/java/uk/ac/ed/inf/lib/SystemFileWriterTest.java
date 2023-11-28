package uk.ac.ed.inf.lib;

import junit.framework.TestCase;
import uk.ac.ed.inf.ilp.data.LngLat;
import uk.ac.ed.inf.ilp.data.Order;
import uk.ac.ed.inf.lib.pathFinder.IPathFinder;
import uk.ac.ed.inf.lib.systemFileWriter.ISystemFileWriter;
import uk.ac.ed.inf.lib.systemFileWriter.SystemFileWriter;

import java.io.File;
import java.util.logging.Logger;

public class SystemFileWriterTest extends TestCase
{
    private static final String DATE = "0000-00-00";
    private final ISystemFileWriter writer = new SystemFileWriter(
            DATE,
            Logger.getLogger("SystemFileWriterTest"));

    public void testWriteOrders_IllegalArgumentException()
    {
        for (Order[] orders : new Order[][]{null, {}})
        {
            try
            {
                writer.writeOrders(orders);
                fail("expected 'IllegalArgumentException' to be thrown");
            } catch (IllegalArgumentException e)
            {
                // Caught as expected.
                assert e.getMessage().contains("orders");
            }
        }
    }

    public void testWriteOrders()
    {
        try
        {
            writer.writeOrders(new Order[]{new Order()});
            assertTrue(verify("/deliveries-" + DATE + ".json"));
        } catch (Exception e)
        {
            fail(e.getMessage());
        }
    }

    public void testWriteGeoJSON_IllegalArgumentException()
    {
        for (LngLat[] path : new LngLat[][]{null, {}})
        {
            try
            {
                writer.writeGeoJSON(path);
                fail("expected 'IllegalArgumentException' to be thrown");
            } catch (IllegalArgumentException e)
            {
                // Caught as expected.
                assertTrue(e.getMessage().contains("path"));
            }
        }
    }

    public void testWriteGeoJSON()
    {
        try
        {
            writer.writeGeoJSON(new LngLat[]{new LngLat(0, 0)});
            assertTrue(verify("/drone-" + DATE + ".geojson"));
        } catch (Exception e)
        {
            fail(e.getMessage());
        }
    }

    public void testWriteFlightPath_IllegalArgumentException()
    {
        for (IPathFinder.Result[] results : new IPathFinder.Result[][]{null, {}})
        {
            try
            {
                writer.writeFlightPath(results);
                fail("expected 'IllegalArgumentException' to be thrown");
            } catch (IllegalArgumentException e)
            {
                // Caught as expected.
                assertTrue(e.getMessage().contains("results"));
            }
        }
    }

    public void testWriteFlightPath()
    {
        try
        {
            writer.writeFlightPath(new IPathFinder.Result[]{new IPathFinder.Result()});
            assertTrue(verify("/flightpath-" + DATE + ".json"));
        } catch (Exception e)
        {
            fail(e.getMessage());
        }
    }

    private boolean verify(String path)
    {
        final File f = new File(ISystemFileWriter.LOCATION + path);
        assert f.exists() && !f.isDirectory();

        return f.delete();
    }
}