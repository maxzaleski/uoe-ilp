package uk.ac.ed.inf.lib.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import uk.ac.ed.inf.ilp.data.NamedRegion;
import uk.ac.ed.inf.ilp.data.Order;
import uk.ac.ed.inf.ilp.data.Restaurant;
import uk.ac.ed.inf.lib.api.dtos.NamedRegionDto;
import uk.ac.ed.inf.lib.api.dtos.OrderDto;
import uk.ac.ed.inf.lib.api.dtos.RestaurantDto;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

/**
 * Represents the client for the application's RESTful API.
 */
public class APIClient implements IAPIClient
{
    final private String url;
    final private ObjectMapper objectMapper;
    final private IDataObjectsFactory dataObjectsFactory;

    public APIClient(String url, IDataObjectsFactory dataObjectsFactory)
    {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());

        this.url = url;
        this.dataObjectsFactory = dataObjectsFactory;
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

    public NamedRegion[] getNoFlyZones() throws IOException
    {
        final NamedRegionDto[] namedRegionDto = objectMapper.readValue(new URL(url + "/noFlyZones"), NamedRegionDto[].class);
        return Arrays.stream(namedRegionDto)
                .map(dataObjectsFactory::createNamedRegion)
                .toArray(NamedRegion[]::new);
    }

    public Order[] getOrdersByISODate(String date) throws IOException
    {
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
