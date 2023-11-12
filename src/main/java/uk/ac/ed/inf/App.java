package uk.ac.ed.inf;

import uk.ac.ed.inf.api.APIClient;
import uk.ac.ed.inf.ilp.constant.OrderValidationCode;
import uk.ac.ed.inf.ilp.data.Order;
import uk.ac.ed.inf.ilp.data.Restaurant;

import java.util.Arrays;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Hello world!
 */
public class App
{
    final private static Logger logger = Logger.getLogger("PizzaDronz");

    public static void main(String[] args)
    {
        // [1] setup

        // [1.1] parse program arguments.
        final String date = args[0];
        final String apiURL = args[1];
        final String seed = args[2];
        logger.info("Starting PizzaDronz " + Map.of(
                "date", date,
                "apiURL", apiURL,
                "seed", seed
        ));

        final APIClient apiClient = new APIClient(apiURL);

        try
        {
            // [1.2] API health check.
            if (!apiClient.isAlive())
            {
                logger.severe("[api] failed health check, exiting...");
                System.exit(1);
            }

            // [1.3] Fetch orders for the specified date and validates them.
            Order[] orders = apiClient.getOrdersByISODate(date);
            if (orders.length == 0)
            {
                logger.info("[orders] none for " + date + ", exiting...");
                System.exit(0);
            }
            orders = validateOrders(orders, apiClient.getRestaurants());

            // [1.4] Calculate the flight path for each order.
        } catch (Exception e)
        {
            logger.severe(e.getMessage());
            System.exit(1);
        }
    }

    private static Order[] validateOrders(Order[] orders, Restaurant[] restaurants)
    {
        logger.info("[orders] validating " + orders.length + " orders...");

        final OrderValidator orderValidator = new OrderValidator();
        return Arrays.stream(orders).
                filter(order ->
                {
                    final OrderValidationCode code = orderValidator
                            .validateOrder(order, restaurants)
                            .getOrderValidationCode();
                    if (code == OrderValidationCode.NO_ERROR) return true;

                    logger.info(String.format("[order#%s] invalid: %s", order.getOrderNo(), code));
                    return false;
                }).
                toArray(Order[]::new);
    }

    private static void calculateFlightPath
}
