package uk.ac.ed.inf.lib.systemFileWriter;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.ac.ed.inf.ilp.constant.OrderStatus;
import uk.ac.ed.inf.ilp.constant.OrderValidationCode;
import uk.ac.ed.inf.ilp.data.LngLat;
import uk.ac.ed.inf.ilp.data.Order;
import uk.ac.ed.inf.lib.pathFinder.INode.Direction;
import uk.ac.ed.inf.lib.pathFinder.IPathFinder;

import java.io.IOException;

/**
 * Represents a writer for the system's output files.
 */
public interface ISystemFileWriter
{
    String LOCATION = "resultfiles/";

    /**
     * Writes the orders to a JSON file (deliveries-YYYY-MM-DD.json).
     *
     * @param orders the orders to write to file.
     * @throws IOException if the orders cannot be written to file.
     */
    void writeOrders(Order[] orders) throws IOException;

    /**
     * Writes the drone's flight path to a GeoJSON file (drone-YYYY-MM-DD.geojson).
     *
     * @param path the coordinates constituting the drone's flight path for the day.
     * @throws IOException if the GeoJSON cannot be written to file.
     */
    void writeGeoJSON(LngLat[] path) throws IOException;

    /**
     * Writes the drone's flight path to a JSON file (flightpath-YYYY-MM-DD.json).
     *
     * @param results the results of the path finding operation.
     * @throws IOException if the JSON cannot be written to file.
     */
    void writeFlightPath(IPathFinder.Result[] results) throws IOException;

    /**
     * Represents a JSON-serialisable {@link Order}.
     */
    class SerialisableOrder
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
     * Represents a JSON-serialisable drone move between two {@link Direction}.
     */
    class SerialisableDroneMove
    {
        @JsonProperty("orderNo")
        final private String orderNo;
        @JsonProperty("fromLongitude")
        final double fromLongitude;
        @JsonProperty("fromLatitude")
        final double fromLatitude;
        @JsonProperty("toLongitude")
        final double toLongitude;
        @JsonProperty("toLatitude")
        final double toLatitude;
        @JsonProperty("angle")
        final double angle;
        @JsonProperty("ticksSinceStartOfCalculation")
        final long ticksSinceStartOfCalculation;

        public SerialisableDroneMove(String orderNo, Direction previous, Direction current)
        {
            this.orderNo = orderNo;

            final LngLat previousPosition = previous.position();
            this.fromLatitude = previousPosition.lng();
            this.fromLongitude = previousPosition.lat();

            final LngLat currentPosition = current.position();
            this.toLatitude = currentPosition.lng();
            this.toLongitude = currentPosition.lat();
            this.angle = current.angle();
            this.ticksSinceStartOfCalculation = current.ticksSinceStart();
        }
    }
}