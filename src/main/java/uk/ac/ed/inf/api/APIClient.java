package uk.ac.ed.inf.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import uk.ac.ed.inf.api.dtos.NamedRegionDto;
import uk.ac.ed.inf.api.dtos.OrderDto;
import uk.ac.ed.inf.api.dtos.RestaurantDto;
import uk.ac.ed.inf.api.factories.DataObjectsFactory;
import uk.ac.ed.inf.ilp.data.NamedRegion;
import uk.ac.ed.inf.ilp.data.Order;
import uk.ac.ed.inf.ilp.data.Restaurant;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

/**
 * Represents the client for the application's REST API.
 */
public class APIClient implements IAPIClient
{
    final private String url;
    final private ObjectMapper objectMapper;
    final private DataObjectsFactory dataObjectsFactory;

    public APIClient(String url)
    {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());

        this.dataObjectsFactory = new DataObjectsFactory();

        this.url = url;
    }

    public boolean isAlive() throws IOException
    {
        return objectMapper.readValue(new URL(url + "/isAlive"), Boolean.class);
    }

    public NamedRegion getCentralAreaCoordinates() throws IOException
    {
        final NamedRegionDto namedRegionDto = objectMapper.readValue(new URL(url + "/centralArea"), NamedRegionDto.class);
        return dataObjectsFactory.createNamedRegion(namedRegionDto);
    }

    public Order[] getOrdersByISODate(String date) throws IllegalArgumentException, IOException
    {
        if (!date.matches("\\d{4}-\\d{2}-\\d{2}"))
            throw new IllegalArgumentException("Date must be of format YYYY-MM-DD");

        final OrderDto[] ordersDto = objectMapper.readValue(new URL(url + "/orders/" + date), OrderDto[].class);
        return Arrays.stream(ordersDto)
                .map(dataObjectsFactory::createOrder)
                .toArray(Order[]::new);
    }

    public Restaurant[] getRestaurants() throws IOException
    {
        final RestaurantDto[] restaurantDto = objectMapper.readValue(new URL(url + "/restaurants"), RestaurantDto[].class);
        return Arrays.stream(restaurantDto)
                .map(dataObjectsFactory::createRestaurant)
                .toArray(Restaurant[]::new);
    }
}
