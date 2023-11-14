package uk.ac.ed.inf;

import uk.ac.ed.inf.api.APIClient;
import uk.ac.ed.inf.ilp.constant.OrderValidationCode;
import uk.ac.ed.inf.ilp.data.LngLat;
import uk.ac.ed.inf.ilp.data.NamedRegion;
import uk.ac.ed.inf.ilp.data.Order;
import uk.ac.ed.inf.ilp.data.Restaurant;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
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
 *   <li>[0]: the date for which to process orders (<i>YYYY-MM-DD</i>)</li>
 *   <li>[1]: the base URL of the RESTful API to consume</li>
 * </ul>
 *
 * <p>
 * This program outputs the following files under {@value SystemFileWriter#LOCATION}:
 * <ul>
 *  <li><i>deliveries-YYYY-MM-DD.json</i>: a JSON file containing the orders to be delivered for the given date.</li>
 *  <li><i>flightpath-YYYY-MM-DD.json</i> a JSON file containing the (flatten) moves constituting the drone's flight path
 *     for each order for the given date.</li>
 *  <li><i>geojson-YYYY-MM-DD.json</i>: a GeoJSON file containing the moves constituting the drone's flight path for each
 *     order for the given date.</li>
 *   </ul>
 * </p>
 *
 * @version 1.0.0
 */
public class App
{
    final private static Logger logger = Logger.getLogger("PizzaDronz");

    public static void main(String[] args)
    {
        final String dateArg = args[0];
        final String apiURLArg = args[1];

        // [setup]
        try
        {
            validateArgs(dateArg, apiURLArg);
            logger.info("[system] starting PizzaDronz " + Map.of(
                    "date", dateArg,
                    "apiURL", apiURLArg
            ));
        } catch (IllegalArgumentException e)
        {
            logger.severe("[system] failed to start PizzaDronz: " + e.getMessage());
            System.exit(1);
        }

        final APIClient apiClient = new APIClient(apiURLArg);
        final SystemFileWriter fileWriter = new SystemFileWriter(dateArg, logger);

        // [execute]
        try
        {

            // [1] API health check.
            if (!apiClient.isAlive())
            {
                logger.severe("[system] API failed health check, exiting...");
                System.exit(1);
            }

            // [2] Fetch restaurants.
            final Restaurant[] restaurants = apiClient.getRestaurants();
            //     Optimise later operations by mapping menu items to their respective restaurant instance;
            //     this is done to avoid having to perform a linear search for each order when calculating the flight
            //     path.
            final Map<String, Restaurant> restaurantMap = new HashMap<>();
            Arrays.stream(restaurants).forEach(restaurant ->
                    Arrays.stream(restaurant.menu()).forEach(pizza -> restaurantMap.put(pizza.name(), restaurant)));

            // [3] Fetch orders for the specified date and validate them.
            Order[] orders = apiClient.getOrdersByISODate(dateArg);
            if (orders.length == 0)
            {
                logger.info("[system] no orders to process for " + dateArg + ", exiting...");
                System.exit(0);
            }
            logger.info("[system] begin processing " + orders.length + " orders...");

            orders = filterInvalidOrders(orders, restaurants);

            // [4] Fetch central area and no-fly zones.
            final NamedRegion campus = apiClient.getCentralAreaCoordinates();
            final NamedRegion[] noFlyZones = apiClient.getNoFlyZones();

            final PathFinder pathFinder = new PathFinder(campus, noFlyZones);
            final Map<String, PathFinder.Move[]> flightPathMap = new HashMap<>();

            // [5] Calculate the flight path for each order.
            Arrays.stream(orders).forEach(order ->
            {
                // (i) We have validated that each item in the order is from the same restaurant.
                //     In [2], we have mapped each menu item to its restaurant instance, as to retrieve its coordinates
                //     in O(1) rather than O(n) time.
                final LngLat restaurantCoords = restaurantMap
                        .get(order.getPizzasInOrder()[0].name())
                        .location();
                flightPathMap.put(order.getOrderNo(), pathFinder.findShortestPath(restaurantCoords));
            });

            // [6] Write items to their respective files.
            fileWriter.writeOrders(orders);
            fileWriter.writeFlightPaths(flightPathMap);
            // TODO: geojson
        } catch (Exception e)
        {
            logger.severe("[system] a fatal error as occurred:");
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Filters out invalid orders by checking their validation code.
     *
     * @param orders      the orders to filter.
     * @param restaurants the restaurants to validate the orders against.
     * @return the filtered orders with no validation errors.
     */
    private static Order[] filterInvalidOrders(Order[] orders, Restaurant[] restaurants)
    {
        final OrderValidator orderValidator = new OrderValidator();
        return Arrays.stream(orders).
                filter(order ->
                {
                    final OrderValidationCode code = orderValidator
                            .validateOrder(order, restaurants)
                            .getOrderValidationCode();
                    if (code == OrderValidationCode.NO_ERROR) return true;

                    logger.warning(String.format("[order#%s] ignored: code '%s'", order.getOrderNo(), code));
                    return false;
                }).
                toArray(Order[]::new);
    }

    /**
     * Validates the program arguments.
     *
     * @param date the date to validate.
     * @param url  the URL to validate.
     * @throws IllegalArgumentException if any of the arguments are invalid.
     */
    private static void validateArgs(String date, String url) throws IllegalArgumentException
    {
        if (date == null || date.isEmpty() || !date.matches("\\d{4}-\\d{2}-\\d{2}"))
            throw new IllegalArgumentException(
                    String.format("argument 'date' cannot be null|empty, and must be of format YYYY-MM-DD; received: '%s'", date));
        if (url == null || url.isEmpty() || !url.matches("https?://.*"))
            throw new IllegalArgumentException(
                    String.format("argument 'url' cannot be null|empty, and must be of standard URL format; received: '%s' ", url));
    }
}
