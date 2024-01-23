package uk.ac.ed.inf.lib;

import junit.framework.TestCase;
import uk.ac.ed.inf.factories.DataObjectsFactory;
import uk.ac.ed.inf.lib.api.APIClient;
import uk.ac.ed.inf.lib.api.IAPIClient;

public class APIClientTest extends TestCase
{
    private final IAPIClient client = new APIClient("https://ilp-rest.azurewebsites.net", new DataObjectsFactory());

    public void testGetOrders()
    {
        isAlive();

        try
        {
            final var result = client.getOrdersByISODate("2023-12-01");
            assert result.length != 0;
        } catch (Exception e)
        {
            fail(e.getMessage());
        }
    }

    public void testGetRestaurants()
    {
        isAlive();

        try
        {
            final var result = client.getRestaurants();
            assert result != null;
            assert result.length != 0;
        } catch (Exception e)
        {
            fail(e.getMessage());
        }
    }

    public void testGetNoFlyZones()
    {
        isAlive();

        try
        {
            final var result = client.getNoFlyZones();
            assert result != null;
            assert result.length != 0;
        } catch (Exception e)
        {
            fail(e.getMessage());
        }
    }

    public void testGetCentralAreaCoordinates()
    {
        isAlive();

        try
        {
            final var coordinates = client.getCentralAreaCoordinates();
            assert coordinates != null;
        } catch (Exception e)
        {
            fail(e.getMessage());
        }
    }

    private void isAlive()
    {
        try
        {
            if (!client.isAlive())
                fail("failing test: API is not alive");
        } catch (Exception e)
        {
            fail("failing test: " + e.getMessage());
        }
    }
}
