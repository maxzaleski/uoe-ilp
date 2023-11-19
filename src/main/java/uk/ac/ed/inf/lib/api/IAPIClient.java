package uk.ac.ed.inf.lib.api;

import uk.ac.ed.inf.ilp.data.NamedRegion;
import uk.ac.ed.inf.ilp.data.Order;
import uk.ac.ed.inf.ilp.data.Restaurant;

/**
 * Represents the client for the application's RESTful API.
 */
public interface IAPIClient
{
    /**
     * Performs a health check on the API.
     *
     * @return true if the API is alive, false otherwise
     * @throws RuntimeException if an unexpected error occurred
     */
    boolean isAlive() throws RuntimeException;

    /**
     * Retrieves the coordinates of the university's central area.
     *
     * @return the mapped {@link NamedRegion}
     * @throws RuntimeException if an unexpected error occurred
     */
    NamedRegion getCentralAreaCoordinates() throws RuntimeException;

    /**
     * Retrieves the coordinates of the no-fly zones.
     *
     * @return the mapped {@link NamedRegion} array
     * @throws RuntimeException if an unexpected error occurred
     */
    NamedRegion[] getNoFlyZones() throws RuntimeException;

    /**
     * Retrieves all orders for a given date.
     *
     * @param date the date in ISO format (yyyy-MM-dd)
     * @return the mapped {@link Order} array
     * @throws RuntimeException if an unexpected error occurred
     */
    Order[] getOrdersByISODate(String date) throws RuntimeException;

    /**
     * Retrieves all restaurants.
     *
     * @return the mapped {@link Restaurant} array
     * @throws RuntimeException if an unexpected error occurred
     */
    Restaurant[] getRestaurants() throws RuntimeException;
}
