package uk.ac.ed.inf;

import uk.ac.ed.inf.factories.DataObjectsFactory;
import uk.ac.ed.inf.ilp.constant.OrderStatus;
import uk.ac.ed.inf.ilp.constant.OrderValidationCode;
import uk.ac.ed.inf.ilp.data.LngLat;
import uk.ac.ed.inf.ilp.data.NamedRegion;
import uk.ac.ed.inf.ilp.data.Order;
import uk.ac.ed.inf.ilp.data.Restaurant;
import uk.ac.ed.inf.ilp.interfaces.OrderValidation;
import uk.ac.ed.inf.lib.OrderValidator;
import uk.ac.ed.inf.lib.api.APIClient;
import uk.ac.ed.inf.lib.api.IAPIClient;
import uk.ac.ed.inf.lib.pathFinder.INode;
import uk.ac.ed.inf.lib.pathFinder.IPathFinder;
import uk.ac.ed.inf.lib.pathFinder.PathFinder;
import uk.ac.ed.inf.lib.systemFileWriter.ISystemFileWriter;
import uk.ac.ed.inf.lib.systemFileWriter.SystemFileWriter;

import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

/**
 * Entry point for PizzaDronz, a drone delivery system by the School of Informatics at the University of Edinburgh.
 *
 * @version 1.0.0
 */
public class App
{
    final private static String DATE_FMT = "yyyy-MM-dd";
    final private static LngLat AT_POSITION = new LngLat(-3.186874, 55.944494);

    public static void main(String[] args)
    {
        final long startTime = System.nanoTime();
        final Logger logger = Logger.getLogger("main");

        String dateArg = "";
        String urlArg = "";

        // [1] Program setup.
        try
        {
            // [1.1] Check for sufficient arguments.
            if (args.length >= 2)
            {
                dateArg = args[0];
                urlArg = args[1];
            } else
            {
                logger.severe("[system] expected 2 arguments; received " + args.length);
                System.exit(1);
            }

            // [1.2] Validate received arguments.
            validateArgs(dateArg, urlArg);

            final Map<String, String> logFields = Map.of("date", dateArg, "url", urlArg);
            logger.info("[system] starting PizzaDronz... " + logFields + "\n");
        } catch (IllegalArgumentException e)
        {
            logger.severe("[system] failed to start PizzaDronz: " + e.getMessage());
            System.exit(1);
        }

        // [2] Main loop execution.
        try
        {
            final ISystemFileWriter fileWriter = new SystemFileWriter(dateArg, logger);
            final IAPIClient apiClient = new APIClient(urlArg, new DataObjectsFactory());
            final IPathFinder pathFinder = new PathFinder();

            final int processedOrdersCount = execute(logger, dateArg, apiClient, fileWriter, pathFinder);

            // [3] Program termination.
            logger.info(String.format("[system] finished processing %s orders (completed in %.2fs).",
                    processedOrdersCount,
                    (System.nanoTime() - startTime) / 1e9));
        } catch (NoDataAccessException | NoRestaurantsException e)
        {
            System.exit(2);
        } catch (Exception e)
        {
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Executes the program.
     *
     * @param logger     the logger to use.
     * @param date       the date to use.
     * @param apiClient  the API client to use.
     * @param fileWriter the file writer to use.
     * @return the number of orders processed.
     */
    public static int execute(Logger logger,
                              String date,
                              IAPIClient apiClient,
                              ISystemFileWriter fileWriter,
                              IPathFinder pathFinder)
            throws Exception
    {
        Restaurant[] restaurants;
        Map<String, Restaurant> restaurantMap = new HashMap<>();
        Order[] orders;
        NamedRegion[] noFlyZones;


        logger.info("[system] begin data fetching...");

        // [1] Check for server heartbeat.
        if (!apiClient.isAlive())
        {
            logger.severe("[system] server is not accepting requests, exiting...");
            throw new NoDataAccessException();
        }

        // [2] Data fetching and validation.
        try
        {
            // [2.2] Fetch restaurants.
            restaurants = apiClient.getRestaurants();
            if (restaurants.length == 0) throw new NoRestaurantsException();

            // Optimise later operations by mapping menu items to their respective restaurant instance;
            // this is done to avoid having to perform a linear search for each order when calculating the flight
            // path.
            Arrays.stream(restaurants).
                    forEach(restaurant ->
                            Arrays.stream(restaurant.menu()).
                                    forEach(pizza -> restaurantMap.put(pizza.name(), restaurant)));

            // [2.3] Fetch orders for the specified date.
            orders = apiClient.getOrdersByISODate(date);

            // [2.4] Fetch no-fly zones.
            noFlyZones = apiClient.getNoFlyZones();

            // [2.5] Fetch central area.
            //
            // (i) The path finding algorithm is not designed around circumventing the central area.
            //     I decided to omit this check entirely as to avoid having to include unnecessary overhead for every
            //     new position generated by the algorithm.

            final Map<String, Integer> logFields = Map.of(
                    "orders", orders.length,
                    "restaurants", restaurants.length,
                    "noFlyZones", noFlyZones.length);
            logger.info("[system] finished data fetching " + logFields + "\n");
        } catch (NoRestaurantsException e)
        {
            logger.severe("[system] no restaurants found, exiting...");
            throw e;
        } catch (Exception e)
        {
            logger.severe("[system] failed to fetch or process required server data:");
            throw new DataAccessException(e);
        }

        final List<IPathFinder.Result> pathResults = new ArrayList<>();
        pathFinder.setNoFlyZones(noFlyZones);

        // [3] Process orders if any were received.
        if (orders.length > 0)
        {
            logger.info("[system] begin flight path calculations...");

            final Restaurant[] finalRestaurants = restaurants; // (for use in lambda)
            final OrderValidation validator = new OrderValidator(date);

            Arrays.stream(orders)
                    // [3.1] Filter out invalid orders.
                    .filter(order ->
                    {
                        validator.validateOrder(order, finalRestaurants);
                        if (order.getOrderValidationCode() != OrderValidationCode.NO_ERROR)
                        {
                            final String msg = String.format("[order#%s] ignored ", order.getOrderNo());
                            logger.warning(msg + Map.of("code", order.getOrderValidationCode()));

                            return false;
                        }
                        return true;
                    })
                    // [3.2] Calculate the flight path for each remaining order.
                    .forEach(order ->
                    {
                        final long calcStartTime = System.nanoTime();

                        // (i) We have validated that each item in the order is from the same restaurant.
                        //     In [2.2], we have mapped each menu item to its restaurant instance, as to retrieve its
                        //     coordinates in O(1) time.
                        final Restaurant restaurant = restaurantMap.get(order.getPizzasInOrder()[0].name());
                        final String orderNo = order.getOrderNo();

                        final LngLat[] positions = new LngLat[]{AT_POSITION, restaurant.location()};

                        // [3.1] Calculate the shortest path between Appleton Tower <> restaurant.
                        for (int i = 0; i < positions.length; i++)
                        {
                            final LngLat from = positions[i];
                            final LngLat to = positions[i == 0 ? 1 : 0];

                            try
                            {
                                final IPathFinder.Result result = pathFinder.findRoute(from, to);
                                result.setOrderNo(orderNo);

                                // → Handle outcome.
                                if (result.getOk()) pathResults.add(result);
                                else
                                {
                                    final Map<String, LngLat> logFields = Map.of("from", from, "to", to);
                                    final String direction = i == 0 ? "outbound" : "inbound";
                                    logger.warning(
                                            String.format("[order#%s] failed to find %s path" + logFields, orderNo, direction));

                                    return;
                                }
                            } catch (Exception e)
                            {
                                logger.warning(String.format("[order#%s] %s", orderNo, e.getMessage()));
                            }

                            if (i == 1) order.setOrderStatus(OrderStatus.DELIVERED);
                        }

                        // [3.3] Log calculation metrics.
                        final Map<String, Object> logFields = Map.of(
                                "<>", restaurant.name(),
                                "took", String.format("%.2fms", (System.nanoTime() - calcStartTime) / 1e6));
                        logger.info(String.format("[order#%s] processed ", orderNo) + logFields);
                    });

            logger.info("[system] finished flight path calculations\n");
        } else
        {
            logger.info("[system] nothing to calculate\n");
        }

        // [4] Write items to their respective files.
        try
        {
            // [4.1] Write today's orders.
            fileWriter.writeOrders(orders);

            final IPathFinder.Result[] successfulResults = pathResults.stream()
                    .filter(IPathFinder.Result::getOk) // (only write successful results)
                    .toArray(IPathFinder.Result[]::new);

            // [4.2] Write the flight path for each order.
            fileWriter.writeFlightPath(successfulResults);

            final LngLat[] flattenedPositions = pathResults.stream()
                    .flatMap(result -> result.getRoute().stream())
                    .map(INode.Direction::position)
                    .toArray(LngLat[]::new);

            // [4.3] Write the drone's flight path as a flattened GeoJSON feature.
            fileWriter.writeGeoJSON(flattenedPositions);
        } catch (Exception e)
        {
            logger.severe("[system] failed to create result files...");
            throw new OutputFileException(e);
        }

        return orders.length;
    }

    /**
     * Validates the program arguments.
     *
     * @param date the string to validate (expects valid date of format yyyy-MM-dd).
     * @param url  the string to validate (expects valid URL).
     * @throws IllegalArgumentException if any of the arguments are invalid.
     */
    public static void validateArgs(String date, String url) throws IllegalArgumentException
    {
        // Validate received date.
        if (date == null || date.isEmpty())
            throw new IllegalArgumentException("args[0] 'date' cannot be null|empty");
        else
        {
            try
            {
                // SimpleDateFormat will accept yyyy-mm-d, which is unsatisfactory.
                if (date.length() != DATE_FMT.length()) throw new ParseException("invalid length", 0);

                final DateFormat sdf = new SimpleDateFormat(DATE_FMT);
                sdf.setLenient(false);
                sdf.parse(date);
            } catch (ParseException e)
            {
                final String fmt = "args[0] 'date' must be a valid date of format '%s'; received: '%s'";
                throw new IllegalArgumentException(String.format(fmt, DATE_FMT, date));
            }
        }

        // Validate received URL.
        if (url == null || url.isEmpty())
            throw new IllegalArgumentException("args[1] 'url' cannot be null|empty");
        else
        {
            try
            {
                new URL(url);
            } catch (Exception e)
            {
                throw new IllegalArgumentException(
                        String.format("args[1] 'url' must be a valid URL; received: '%s' ", url));
            }
        }
    }

    static class NoDataAccessException extends Exception
    {
        public NoDataAccessException()
        {
            super();
        }
    }

    static class NoRestaurantsException extends Exception
    {
        public NoRestaurantsException()
        {
            super();
        }
    }

    static class DataAccessException extends Exception
    {
        public DataAccessException(Exception e)
        {
            super(e);
        }
    }

    static class OutputFileException extends Exception
    {
        public OutputFileException(Exception e)
        {
            super(e);
        }
    }
}
