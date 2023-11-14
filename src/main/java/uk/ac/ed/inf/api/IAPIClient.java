package uk.ac.ed.inf.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import uk.ac.ed.inf.ilp.data.NamedRegion;
import uk.ac.ed.inf.ilp.data.Order;
import uk.ac.ed.inf.ilp.data.Restaurant;

import java.io.IOException;

public interface IAPIClient
{
    /**
     * Performs a health check on the API.
     *
     * @return true if the API is alive, false otherwise
     * @throws IOException if the response cannot be parsed
     */
    boolean isAlive() throws IOException;

    /**
     * Retrieves the coordinates of the university's central area.
     *
     * @return the mapped {@link NamedRegion}
     * @throws IOException if the response cannot be parsed
     */
    NamedRegion getCentralAreaCoordinates() throws IOException;

    /**
     * Retrieves the coordinates of the no-fly zones.
     *
     * @return the mapped {@link NamedRegion} array
     * @throws IOException if the response cannot be parsed
     */
    NamedRegion[] getNoFlyZones() throws IOException;

    /**
     * Retrieves all orders for a given date.
     *
     * @param date the date in ISO format (YYYY-MM-DD)
     * @return the mapped {@link Order} array
     * @throws JsonProcessingException if the response cannot be parsed
     */
    Order[] getOrdersByISODate(String date) throws IllegalArgumentException, IOException;

    /**
     * Retrieves all restaurants.
     *
     * @return the mapped {@link Restaurant} array
     * @throws JsonProcessingException if the response cannot be parsed
     */
    Restaurant[] getRestaurants() throws IOException;
}
