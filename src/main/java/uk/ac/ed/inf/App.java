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
import uk.ac.ed.inf.lib.pathFinder.INode;
import uk.ac.ed.inf.lib.pathFinder.IPathFinder;
import uk.ac.ed.inf.lib.pathFinder.PathFinder;
import uk.ac.ed.inf.lib.systemFileWriter.ISystemFileWriter;
import uk.ac.ed.inf.lib.systemFileWriter.SystemFileWriter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

/**
 * Entry point for PizzaDronz, a drone delivery system for the University of Edinburgh.
 *
 * <p>
 * <br>
 * This program consumes a RESTful API to retrieve dynamic data, notably orders placed for a given date,
 * and calculates the shortest path between Appleton Tower and each restaurant from which an order has been placed.
 *
 * <p>
 * <br>
 * This program accepts the following arguments:
 * <ul>
 *   <li>[0]: the date for which to process orders (<i>yyyy-MM-dd</i>)</li>
 *   <li>[1]: the base URL of the RESTful API to consume</li>
 *   <li>[2]: a seed to be used for random number generation (ignored)</li>
 * </ul>
 *
 * <p>
 * This program outputs the following files under {@value SystemFileWriter#LOCATION}:
 * <ul>
 *  <li><i>deliveries-yyyy-MM-dd.json</i>: a JSON file containing the orders for the given date.</li>
 *  <li>
 *      <i>flightpath-yyyy-MM-dd.json</i> a JSON file containing the (flattened) moves constituting the drone's flight path
 *      for each order for the given date.
 *  </li>
 *  <li>
 *      <i>drone-yyyy-MM-dd.geojson</i>: a GeoJSON file containing a single (flattened) 'lineString' feature constituting
 *      the drone's flight path for each order for the given date.
 *      </li>
 *   </ul>
 * </p>
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

        if (args.length < 2)
        {
            logger.severe("[system] expected at least 2 arguments; received " + args.length);
            System.exit(1);
        }

        final String dateArg = args[0];
        final String apiBaseArg = args[1];
        final String seed = args[2];

        // [1] Program setup.
        try
        {
            validateArgs(dateArg, apiBaseArg);
            logger.info("[system] starting PizzaDronz... " + Map.of(
                    "date", dateArg,
                    "apiBase", apiBaseArg,
                    "seed(ignored)", seed
            ) + "\n");
        } catch (IllegalArgumentException e)
        {
            logger.severe("[system] failed to start PizzaDronz: " + e.getMessage());
            System.exit(1);
        }

        final APIClient apiClient = new APIClient(apiBaseArg, new DataObjectsFactory());
        final ISystemFileWriter fileWriter = new SystemFileWriter(dateArg, logger);

        Map<String, Restaurant> restaurantMap = new HashMap<>();
        Order[] orders = new Order[0];
        Order[] ordersToDeliver = orders;
        NamedRegion[] noFlyZones = new NamedRegion[0];

        // [2] Data fetching and validation.
        try
        {
            // [2.1] API health check.
            if (!apiClient.isAlive())
            {
                logger.severe("[system] API is unreachable, exiting...");
                System.exit(1);
            }

            logger.info("[system] begin data fetching...");

            // [2.2] Fetch restaurants.
            final Restaurant[] restaurants = apiClient.getRestaurants();
            //       Optimise later operations by mapping menu items to their respective restaurant instance;
            //       this is done to avoid having to perform a linear search for each order when calculating the flight
            //       path.
            Arrays.stream(restaurants).forEach(restaurant ->
                    Arrays.stream(restaurant.menu()).forEach(pizza -> restaurantMap.put(pizza.name(), restaurant)));

            // [2.3] Fetch orders for the specified date.
            orders = apiClient.getOrdersByISODate(dateArg);
            if (orders.length == 0)
            {
                logger.info("[system] no orders to process for " + dateArg + ", exiting with no errors...");
                System.exit(0);
            } else
            {
                logger.info("[system] received " + orders.length + " orders, validating...");

                // [2.3.1] Validate orders.
                final OrderValidation validator = new OrderValidator(dateArg);
                ordersToDeliver = Arrays.stream(orders)
                        .filter(order ->
                        {
                            validator.validateOrder(order, restaurants);
                            if (order.getOrderValidationCode() != OrderValidationCode.NO_ERROR)
                            {
                                final String msg = String.format("[order#%s] ignored ", order.getOrderNo());
                                logger.warning(msg + Map.of("code", order.getOrderValidationCode()));

                                return false;
                            }
                            return true;
                        })
                        .toArray(Order[]::new);
            }

            // [2.4] no-fly zones.
            noFlyZones = apiClient.getNoFlyZones();
        } catch (Exception e)
        {
            logger.severe("[system] failed to fetch or process necessary API data...");
            handleException(e);
        }

        logger.info("[system] finished data fetching and validation.\n");

        logger.info("[system] begin flight path calculations for " + ordersToDeliver.length + " orders...");

        final IPathFinder pathFinder = new PathFinder(noFlyZones);
        final List<IPathFinder.Result> pathResults = new ArrayList<>();

        // [3] Calculate the shortest path between Appleton Tower and each restaurant (and back).
        Arrays.stream(ordersToDeliver).forEach(order ->
        {
            final long calcStartTime = System.nanoTime();

            // (i) We have validated that each item in the order is from the same restaurant.
            //     In [2.2], we have mapped each menu item to its restaurant instance, as to retrieve its coordinates
            //     in O(1) rather than O(n) time.
            final Restaurant restaurant = restaurantMap.get(order.getPizzasInOrder()[0].name());

            final String restName = restaurant.name();
            final LngLat restPos = restaurant.location();

            try
            {
                // [3.1] Calculate the shortest path between Appleton Tower and the restaurant.
                IPathFinder.Result result = pathFinder.findRoute(AT_POSITION, restPos);
                result.setOrderNo(order.getOrderNo());

                if (result.getOk())
                    pathResults.add(result);
                else
                {
                    logger.warning(String.format("[order#%s] failed to find path to '%s' (%s)",
                            result.getOrderNo(),
                            restName,
                            restPos));
                    return;
                }

                // [3.2] Calculate the shortest path between the restaurant and Appleton Tower.
                result = pathFinder.findRoute(restPos, AT_POSITION);
                result.setOrderNo(order.getOrderNo());

                if (result.getOk())
                {
                    pathResults.add(result);
                    order.setOrderStatus(OrderStatus.DELIVERED);
                } else
                    logger.warning(String.format("[order#%s] failed to find return path to '%s' from '%s' (%s)",
                            result.getOrderNo(),
                            "Appleton Tower",
                            restName,
                            restPos));

                final String msg = String.format("[order#%s] flight path processed ", result.getOrderNo());
                logger.info(msg + Map.of(
                        "to", restName,
                        "took", String.format("%.2fms", (System.nanoTime() - calcStartTime) / 1e6))
                );
            } catch (Exception e)
            {
                logger.warning(String.format("[order#%s] %s", order.getOrderNo(), e.getMessage()));
            }
        });

        logger.info(String.format("[system] finished flight path calculations for %d orders.\n", ordersToDeliver.length));

        logger.info("[system] begin writing results...");

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
            handleException(e);
        }

        // [5] Program termination.
        logger.info(String.format("[system] finished processing all %s orders (completed in %.2fs).",
                orders.length,
                (System.nanoTime() - startTime) / 1e9));
    }

    /**
     * Validates the program arguments.
     *
     * @param date    the string to validate (expects yyyy-MM-dd).
     * @param apiBase the string to validate (expects valid URL or IPv4).
     * @throws IllegalArgumentException if any of the arguments are invalid.
     */
    public static void validateArgs(String date, String apiBase) throws IllegalArgumentException
    {
        // Validate received date.
        if (date == null || date.isEmpty())
            throw new IllegalArgumentException("argument[0] 'date' cannot be null|empty");
        else
        {
            final DateFormat sdf = new SimpleDateFormat(DATE_FMT);
            sdf.setLenient(false);

            try
            {
                sdf.parse(date);
            } catch (ParseException e)
            {
                throw new IllegalArgumentException(
                        String.format("argument[0] 'date' must be of format '%s'; received: '%s'", DATE_FMT, date));
            }
        }

        // TODO: revisit prior to submission.
        // Validate received API base.
        if (apiBase == null || apiBase.isEmpty())
            throw new IllegalArgumentException("argument[1] 'apiBase' cannot be null|empty");
        else
        {
            final boolean isURL = apiBase.matches("https?://.*");
            final boolean isIPv4 = apiBase.matches("^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)(\\.|$)){4}$");
            if (!isURL && !isIPv4)
                throw new IllegalArgumentException(
                        String.format("argument[1] 'apiBase' must be of standard URL/IPv4 format; received: '%s' ", apiBase));
        }
    }

    /**
     * Handles an exception by printing its stack trace and exiting the program with a non-zero exit code.
     *
     * @param e the exception to handle.
     */
    private static void handleException(Exception e)
    {
        //noinspection CallToPrintStackTrace
        e.printStackTrace(); // (already prints to System.err)
        System.exit(1);
    }
}
