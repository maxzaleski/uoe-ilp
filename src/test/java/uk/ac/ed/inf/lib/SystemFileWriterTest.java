package uk.ac.ed.inf.lib;

import com.fasterxml.jackson.databind.ObjectMapper;
import junit.framework.TestCase;
import uk.ac.ed.inf.ilp.constant.OrderStatus;
import uk.ac.ed.inf.ilp.constant.OrderValidationCode;
import uk.ac.ed.inf.ilp.data.LngLat;
import uk.ac.ed.inf.ilp.data.Order;
import uk.ac.ed.inf.lib.pathFinder.IPathFinder;
import uk.ac.ed.inf.lib.systemFileWriter.ISystemFileWriter;
import uk.ac.ed.inf.lib.systemFileWriter.SystemFileWriter;
import uk.ac.ed.inf.lib.systemFileWriter.geoJSON.GeoJSON;

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
        try
        {
            writer.writeOrders(null);
            fail("expected 'IllegalArgumentException' to be thrown");
        } catch (IllegalArgumentException e)
        {
            // Caught as expected.
            assert e.getMessage().contains("orders");
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

    public void testWriteOrders_Output()
    {
        try
        {
            final Order order = new Order();
            order.setOrderNo("foobar");
            order.setOrderStatus(OrderStatus.DELIVERED);
            order.setOrderValidationCode(OrderValidationCode.NO_ERROR);
            order.setPriceTotalInPence(100);

            writer.writeOrders(new Order[]{order});

            final String path = "/deliveries-" + DATE + ".json";

            final ISystemFileWriter.SerialisableOrder[] orders = read(path, ISystemFileWriter.SerialisableOrder[].class);
            assertEquals(1, orders.length);

            final ISystemFileWriter.SerialisableOrder serialisedOrder = orders[0];
            assertEquals(order.getOrderNo(), serialisedOrder.getOrderNo());
            assertEquals(order.getOrderStatus(), serialisedOrder.getOrderStatus());
            assertEquals(order.getOrderValidationCode(), serialisedOrder.getOrderValidationCode());
            assertEquals(order.getPriceTotalInPence(), serialisedOrder.getCostInPence());
        } catch (Exception e)
        {
            fail(e.getMessage());
        }
    }

    public void testWriteGeoJSON_IllegalArgumentException()
    {
        try
        {
            writer.writeGeoJSON(null);
            fail("expected 'IllegalArgumentException' to be thrown");
        } catch (IllegalArgumentException e)
        {
            // Caught as expected.
            assertTrue(e.getMessage().contains("path"));
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

    public void testWriteGeoJSONs_Output()
    {
        try
        {
            writer.writeGeoJSON(new LngLat[]{new LngLat(0, 0)});

            final String path = "/drone-" + DATE + ".geojson";

            final GeoJSON geoJSON = read(path, GeoJSON.class);
            assertEquals("FeatureCollection", geoJSON.getType());
            assertEquals(1, geoJSON.getFeatures().size());
        } catch (Exception e)
        {
            fail(e.getMessage());
        }
    }

    public void testWriteFlightPath_IllegalArgumentException()
    {
        try
        {
            writer.writeFlightPath(null);
            fail("expected 'IllegalArgumentException' to be thrown");
        } catch (IllegalArgumentException e)
        {
            // Caught as expected.
            assertTrue(e.getMessage().contains("results"));
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

    public void testWriteFlightPaths_Output()
    {
        // TODO: implement
    }

    private boolean verify(String path)
    {
        final File f = new File(ISystemFileWriter.LOCATION + path);
        assert f.exists() && !f.isDirectory();

        return f.delete();
    }

    private <T> T read(String path, Class<T> T)
    {
        try
        {
            final File f = new File(ISystemFileWriter.LOCATION + path);
            final T data = new ObjectMapper().readValue(f, T);
            f.delete();

            return data;
        } catch (Exception e)
        {
            throw new RuntimeException(String.format("failed to read data from '%s': %s", path, e.getMessage()), e);
        }
    }
}