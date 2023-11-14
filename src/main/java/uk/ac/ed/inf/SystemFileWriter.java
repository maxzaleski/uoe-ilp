package uk.ac.ed.inf;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import uk.ac.ed.inf.ilp.constant.OrderStatus;
import uk.ac.ed.inf.ilp.constant.OrderValidationCode;
import uk.ac.ed.inf.ilp.data.Order;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Represents a writer for the system's output files.
 */
public class SystemFileWriter
{
    final static String LOCATION = "output/";

    final private String date;
    final private ObjectMapper jsonObjectMapper;
    final private Logger logger;

    /**
     * Represents a writer for the system's output files.
     *
     * @param date   the date for which the output files are being written.
     * @param logger the logger to use for logging.
     * @throws RuntimeException if the output directory cannot be created.
     */
    public SystemFileWriter(String date, Logger logger) throws RuntimeException
    {
        this.date = date;
        this.jsonObjectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        this.logger = logger;

        // Create output directory if it doesn't exist.
        final java.io.File directory = new java.io.File(LOCATION);
        if (!directory.exists())
            if (!directory.mkdir())
                throw new RuntimeException(String.format("failed to create directory '%s'", LOCATION));
    }

    /**
     * Writes the orders to a JSON file (deliveries-YYYY-MM-DD.json).
     *
     * @param orders the orders to write to file.
     * @throws IOException if the orders cannot be written to file.
     */
    public void writeOrders(Order[] orders) throws IOException
    {
        if (orders == null || orders.length == 0)
            throw new IllegalArgumentException("nothing to write; orders is null or empty");

        final SerialisableOrder[] writableOrders = Arrays.stream(orders).
                map(SerialisableOrder::new).
                toArray(SerialisableOrder[]::new);

        write(LOCATION + "deliveries-" + date + ".json", writableOrders);
    }

    /**
     * Writes the moves constituting the drone's flight path to a JSON file (flightpath-YYYY-MM-DD.json)
     *
     * @param paths the moves constituting the drone's flight path for each order.
     * @throws IOException if the moves cannot be written to file.
     */
    public void writeFlightPaths(Map<String, DronePathFinder.Move[]> paths) throws IOException
    {
        if (paths == null || paths.isEmpty())
            throw new IllegalArgumentException("nothing to write; paths is null or empty");

        final List<SerialisableMove> writableMoves = new ArrayList<>();
        paths.forEach((key, values) ->
                Arrays.stream(values)
                        .map(move -> new SerialisableMove(key, move))
                        .forEach(writableMoves::add));

        write(LOCATION + "flightpath-" + date + ".json", writableMoves.toArray());
    }

    /**
     * Writes the given data to the given destination.
     *
     * @param dest the destination to write to.
     * @param data the data to write.
     * @param <T>  the type of the data.
     * @throws IllegalArgumentException if the data is null or empty.
     * @throws IOException              if the data cannot be written to the destination.
     */
    private <T> void write(String dest, T[] data) throws IOException
    {
        try
        {
            final FileWriter file = new FileWriter(dest);
            file.write(jsonObjectMapper.writeValueAsString(data));
            file.close();
        } catch (IOException e)
        {
            throw new IOException(String.format("failed to write data to '%s': %s", dest, e.getMessage()));
        }

        logger.info(String.format("[system] successfully wrote %d items to '%s'", data.length, dest));
    }

    // --- [classes]

    /**
     * Represents a JSON-serialisable {@link Order}.
     */
    private static class SerialisableOrder
    {
        @JsonProperty("orderNo")
        final private String orderNo;
        @JsonProperty("orderStatus")
        final private OrderStatus orderStatus;
        @JsonProperty("orderValidationCode")
        final private OrderValidationCode orderValidationCode;
        @JsonProperty("costInPence")
        final private int costInPence;

        public SerialisableOrder(Order order)
        {
            this.orderNo = order.getOrderNo();
            this.orderStatus = order.getOrderStatus();
            this.orderValidationCode = order.getOrderValidationCode();
            this.costInPence = order.getPriceTotalInPence();
        }
    }

    /**
     * Represents a JSON-serialisable {@link DronePathFinder.Move}.
     */
    private static class SerialisableMove extends DronePathFinder.Move
    {
        @JsonProperty()
        private String orderNo;

        public SerialisableMove(String orderNo, DronePathFinder.Move move)
        {
            super(move);
            this.orderNo = orderNo;
        }
    }

    // --- [/classes]
}
