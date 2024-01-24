package uk.ac.ed.inf;

import junit.framework.TestCase;
import uk.ac.ed.inf.ilp.constant.OrderStatus;
import uk.ac.ed.inf.ilp.constant.OrderValidationCode;
import uk.ac.ed.inf.ilp.constant.SystemConstants;
import uk.ac.ed.inf.ilp.data.*;
import uk.ac.ed.inf.lib.api.IAPIClient;
import uk.ac.ed.inf.lib.pathFinder.INode;
import uk.ac.ed.inf.lib.pathFinder.IPathFinder;
import uk.ac.ed.inf.lib.pathFinder.Node;
import uk.ac.ed.inf.lib.systemFileWriter.ISystemFileWriter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.logging.Logger;

import static java.time.DayOfWeek.*;

public class AppTest extends TestCase
{
    final String PROD_URL = System.getenv("PROD_URL");

    public void testApp_main()
    {
        if (PROD_URL != null && !PROD_URL.isEmpty())
        {
            final long start = System.nanoTime();
            App.main(new String[]{"2023-12-01", PROD_URL});
            // Calls `System.exit(1)` if an exception is caught.

            // Has to complete in less than 60 seconds.
            assert System.nanoTime() - start < 60 * 1e9;
        }
    }

    public void testApp_validateArgs_IllegalArgument_Date()
    {
        for (String date : new String[]{null, "", "0000-00-00", "2023-12-1", "2023-9-1", "01-12-2023"})
        {
            try
            {
                App.validateArgs(date, "");
                fail("expected 'IllegalArgumentException' to be thrown");
            } catch (Exception e)
            {
                assert e.getMessage().contains("date");
            }
        }
    }

    public void testApp_validateArgs_Date()
    {
        try
        {
            App.validateArgs("2023-12-01", "https://test.test");
        } catch (IllegalArgumentException e)
        {
            fail("expected 'IllegalArgumentException' to be thrown");
        }
    }

    public void testApp_validateArgs_IllegalArgument_Url()
    {
        for (String url : new String[]{null, "", "www.example.com/file[/].html", "127.0.0.1"})
        {
            try
            {
                App.validateArgs("2023-12-01", url);
                fail("expected 'IllegalArgumentException' to be thrown");
            } catch (Exception e)
            {
                assert e.getMessage().contains("url");
            }
        }
    }

    public void testApp_validateArgs_Url()
    {
        for (String url : new String[]{
                "http://ilp-rest.azurewebsites.net",
                "https://ilp-rest.azurewebsites.net",
                "http://127.0.0.0",
                "https://127.0.0.0",
        })
        {
            try
            {
                App.validateArgs("2023-12-01", url);
            } catch (Exception e)
            {
                fail("didn't expect 'IllegalArgumentException' to be thrown");
            }
        }
    }

    public void testApp_execute_NoDataAccessException()
    {
        this.testApp_execute_Exception(App.NoDataAccessException.class,
                new MockAPIClient(false, false, false, false),
                null);
    }

    public void testApp_execute_NoRestaurantException()
    {
        this.testApp_execute_Exception(App.NoRestaurantsException.class,
                new MockAPIClient(true, false, false, true),
                null);
    }

    public void testApp_execute_DataAccessException()
    {
        this.testApp_execute_Exception(App.DataAccessException.class,
                new MockAPIClient(true, true, true, false),
                null);
    }

    public void testApp_execute_FileWriterException()
    {
        this.testApp_execute_Exception(App.OutputFileException.class,
                new MockAPIClient(true, false, true, false),
                new MockSystemFileWriter(true));
    }

    public void testApp_execute()
    {
        final boolean[][] pathFinderFlags = new boolean[][]{
                {false, false},
                {false, true},
                {true, false},
        };
        for (boolean[] flags : pathFinderFlags)
        {
            try
            {
                App.execute(Logger.getGlobal(), "2023-12-01",
                        new MockAPIClient(true, false, false, false),
                        new MockSystemFileWriter(false),
                        new MockPathFinder(flags[0], flags[1])
                );
            } catch (Exception e)
            {
                fail(e.getMessage());
            }
        }
    }

    private <E extends Exception> void testApp_execute_Exception(
            Class<E> exception,
            IAPIClient apiClient,
            ISystemFileWriter fileWriter)
    {
        try
        {
            App.execute(Logger.getGlobal(), "2023-12-01",
                    apiClient,
                    fileWriter,
                    new MockPathFinder(false, false));

            fail("expected 'Exception' to be thrown");
        } catch (Exception e)
        {
            if (!exception.isInstance(e))
                fail(String.format("expected '%s' to be thrown, got '%s'", exception.getSimpleName(), e.getClass().getSimpleName()));
        }
    }

    static class MockAPIClient implements IAPIClient
    {
        private final boolean isAlive;
        private final boolean returnException;
        private final boolean noOrders;
        private final boolean noRestaurants;

        public MockAPIClient(boolean isAlive, boolean returnException, boolean noOrders, boolean noRestaurants)
        {
            this.isAlive = isAlive;
            this.returnException = returnException;
            this.noOrders = noOrders;
            this.noRestaurants = noRestaurants;
        }

        @Override
        public boolean isAlive() throws RuntimeException
        {
            return isAlive;
        }

        @Override
        public NamedRegion getCentralAreaCoordinates() throws RuntimeException
        {
            if (returnException)
                throw new RuntimeException("test exception");
            return new NamedRegion("test", new LngLat[]{});
        }

        @Override
        public NamedRegion[] getNoFlyZones() throws RuntimeException
        {
            if (returnException)
                throw new RuntimeException("test exception");
            return new NamedRegion[0];
        }

        @Override
        public Order[] getOrdersByISODate(String date) throws RuntimeException
        {
            if (returnException)
                throw new RuntimeException("test exception");
            else if (noOrders)
                return new Order[0];
            else
            {
                final Order order = new Order();
                order.setOrderNo("foobar");
                order.setOrderDate(LocalDate.parse("2023-12-01"));
                order.setOrderStatus(OrderStatus.UNDEFINED);
                order.setOrderValidationCode(OrderValidationCode.UNDEFINED);
                order.setPizzasInOrder(new Pizza[]{new Pizza("R1: Margarita", 1000)});
                order.setPriceTotalInPence(1000 + SystemConstants.ORDER_CHARGE_IN_PENCE);

                final CreditCardInformation cardInformation = new CreditCardInformation();
                cardInformation.setCvv("123");
                cardInformation.setCreditCardNumber("1234567890123456");
                cardInformation.setCreditCardExpiry("12/24");

                order.setCreditCardInformation(cardInformation);
                return new Order[]{order, new Order()};
            }
        }

        @Override
        public Restaurant[] getRestaurants() throws RuntimeException
        {
            if (returnException)
                throw new RuntimeException("test exception");
            else if (noRestaurants)
                return new Restaurant[0];
            else
            {
                return new Restaurant[]{
                        new Restaurant(
                                "Civerinos Slice",
                                new LngLat(-3.1912869215011597, 55.945535152517735),
                                new DayOfWeek[]{MONDAY, TUESDAY, FRIDAY, SATURDAY, SUNDAY},
                                new Pizza[]{
                                        new Pizza("R1: Margarita", 1000),
                                        new Pizza("R1: Calzone", 1400)
                                }
                        )};
            }
        }
    }

    static class MockSystemFileWriter implements ISystemFileWriter
    {
        private final boolean exception;

        public MockSystemFileWriter(boolean exception)
        {
            this.exception = exception;
        }

        @Override
        public void writeOrders(Order[] orders) throws RuntimeException
        {
            if (exception)
                throw new RuntimeException("test exception");
        }

        @Override
        public void writeGeoJSON(LngLat[] path) throws RuntimeException
        {
            if (exception)
                throw new RuntimeException("test exception");
        }

        @Override
        public void writeFlightPath(IPathFinder.Result[] results) throws RuntimeException
        {
            if (exception)
                throw new RuntimeException("test exception");
        }
    }

    static class MockPathFinder implements IPathFinder
    {
        private final boolean exception;
        private final boolean noResult;

        public MockPathFinder(boolean exception, boolean noResult)
        {
            this.exception = exception;
            this.noResult = noResult;
        }

        @Override
        public Result findRoute(LngLat from, LngLat to) throws RuntimeException
        {
            if (exception)
                throw new RuntimeException("test exception");

            var result = new Result();
            if (noResult)
                return result;
            else
            {
                result.setOK(true);
                result.setOrderNo("test");
                // (LngLat position, double angle, long ticksSinceStart
                result.setRoute(
                        new INode.Direction(from, 0, 0),
                        new Node(new INode.Direction(to, 0, 0)));
                return result;
            }
        }

        public void setNoFlyZones(NamedRegion[] noFlyZones)
        {
        }
    }
}
