package uk.ac.ed.inf.lib.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import uk.ac.ed.inf.ilp.data.NamedRegion;
import uk.ac.ed.inf.ilp.data.Order;
import uk.ac.ed.inf.ilp.data.Restaurant;
import uk.ac.ed.inf.lib.api.dtos.NamedRegionDto;
import uk.ac.ed.inf.lib.api.dtos.OrderDto;
import uk.ac.ed.inf.lib.api.dtos.RestaurantDto;

import java.net.URL;
import java.util.Arrays;

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

    public boolean isAlive() throws RuntimeException
    {
        try
        {
            return objectMapper.readValue(new URL(url + "/isAlive"), Boolean.class);
        } catch (Exception e)
        {
            throw new RuntimeException("failed to fetch heartbeat: " + e.getMessage(), e);
        }
    }

    public NamedRegion getCentralAreaCoordinates() throws RuntimeException
    {
        try
        {
            final NamedRegionDto namedRegionDto = objectMapper.readValue(
                    new URL(url + "/centralArea"),
                    NamedRegionDto.class);
            return dataObjectsFactory.createNamedRegion(namedRegionDto);
        } catch (Exception e)
        {
            throw new RuntimeException("failed to fetch central area coordinates: " + e.getMessage(), e);
        }
    }

    public NamedRegion[] getNoFlyZones() throws RuntimeException
    {
        try
        {
            final NamedRegionDto[] namedRegionDto = objectMapper.readValue(
                    new URL(url + "/noFlyZones"),
                    NamedRegionDto[].class);
            return Arrays.stream(namedRegionDto)
                    .map(dataObjectsFactory::createNamedRegion)
                    .toArray(NamedRegion[]::new);
        } catch (Exception e)
        {
            throw new RuntimeException("failed to fetch no fly zones: " + e.getMessage(), e);
        }
    }

    public Order[] getOrdersByISODate(String date) throws RuntimeException
    {
        // [note] we omit validating the date here as we expect this error to be caught much earlier in the program's
        //        lifecycle.
        try
        {
            final OrderDto[] ordersDto = objectMapper.readValue(new URL(url + "/orders/" + date), OrderDto[].class);
            return Arrays.stream(ordersDto)
                    .map(dataObjectsFactory::createOrder)
                    .toArray(Order[]::new);
        } catch (Exception e)
        {
            throw new RuntimeException("failed to fetch orders for date " + date + ": " + e.getMessage(), e);
        }
    }

    public Restaurant[] getRestaurants() throws RuntimeException
    {
        try
        {
            final RestaurantDto[] restaurantDto = objectMapper.readValue(
                    new URL(url + "/restaurants"),
                    RestaurantDto[].class);
            return Arrays.stream(restaurantDto)
                    .map(dataObjectsFactory::createRestaurant)
                    .toArray(Restaurant[]::new);
        } catch (Exception e)
        {
            throw new RuntimeException("failed to fetch restaurants: " + e.getMessage(), e);
        }
    }
}
