package uk.ac.ed.inf.lib.systemFileWriter;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.ac.ed.inf.ilp.constant.OrderStatus;
import uk.ac.ed.inf.ilp.constant.OrderValidationCode;
import uk.ac.ed.inf.ilp.data.LngLat;
import uk.ac.ed.inf.ilp.data.Order;

import java.io.IOException;

/**
 * Represents a writer for the system's output files.
 */
public interface ISystemFileWriter
{
    String LOCATION = "output/";

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
     * @param features the features constituting the drone's flight path.
     * @throws IOException if the features cannot be written to file.
     */
    void writeGeoJson(LngLat[] path) throws IOException;

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
}
