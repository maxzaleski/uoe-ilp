package uk.ac.ed.inf.lib.systemFileWriter;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.ac.ed.inf.ilp.constant.OrderStatus;
import uk.ac.ed.inf.ilp.constant.OrderValidationCode;
import uk.ac.ed.inf.ilp.data.LngLat;
import uk.ac.ed.inf.ilp.data.Order;
import uk.ac.ed.inf.lib.pathFinder.INode.Direction;
import uk.ac.ed.inf.lib.pathFinder.IPathFinder;

/**
 * Represents a writer for the system's output files.
 */
public interface ISystemFileWriter
{
    String LOCATION = "resultfiles";

    /**
     * Writes the orders to <i>{@value LOCATION}/deliveries-yyyy-MM-dd.json<i/>.
     *
     * @param orders the orders to write to file.
     * @throws IllegalArgumentException if the orders cannot be written to file.
     * @throws RuntimeException         if an unexpected error occurs during write.
     */
    void writeOrders(Order[] orders) throws RuntimeException;

    /**
     * Writes the drone's flight path as GeoJSON feature to <i>{@value LOCATION}/drone-yyyy-MM-dd.geojson</i>.
     *
     * @param path the coordinates constituting the drone's flight path for the day.
     * @throws IllegalArgumentException if the GeoJSON cannot be written to file.
     * @throws RuntimeException         if an unexpected error occurs during write.
     */
    void writeGeoJSON(LngLat[] path) throws RuntimeException;

    /**
     * Writes the drone's flight path to <i>{@value LOCATION}/flightpath-yyyy-MM-dd.json</>.
     *
     * @param results the results of the path finding operation.
     * @throws IllegalArgumentException if the JSON cannot be written to file.
     * @throws RuntimeException         if an unexpected error occurs during write.
     */
    void writeFlightPath(IPathFinder.Result[] results) throws RuntimeException;

    /**
     * Represents a JSON-serialisable {@link Order}.
     */
    class SerialisableOrder
    {
        @JsonProperty("orderNo")
        private String orderNo;
        @JsonProperty("orderStatus")
        private OrderStatus orderStatus;
        @JsonProperty("orderValidationCode")
        private OrderValidationCode orderValidationCode;
        @JsonProperty("costInPence")
        private int costInPence;

        public SerialisableOrder(Order order)
        {
            this.orderNo = order.getOrderNo();
            this.orderStatus = order.getOrderStatus();
            this.orderValidationCode = order.getOrderValidationCode();
            this.costInPence = order.getPriceTotalInPence();
        }

        public SerialisableOrder()
        {
        }

        public String getOrderNo()
        {
            return orderNo;
        }

        public OrderStatus getOrderStatus()
        {
            return orderStatus;
        }

        public OrderValidationCode getOrderValidationCode()
        {
            return orderValidationCode;
        }

        public int getCostInPence()
        {
            return costInPence;
        }

        public void setOrderNo(String orderNo)
        {
            this.orderNo = orderNo;
        }

        public void setOrderStatus(OrderStatus orderStatus)
        {
            this.orderStatus = orderStatus;
        }

        public void setOrderValidationCode(OrderValidationCode orderValidationCode)
        {
            this.orderValidationCode = orderValidationCode;
        }

        public void setCostInPence(int costInPence)
        {
            this.costInPence = costInPence;
        }
    }

    /**
     * Represents a JSON-serialisable drone move between two {@link Direction}.
     */
    class SerialisableDroneMove
    {
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
        @JsonProperty("orderNo")
        final private String orderNo;

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
        }
    }
}
