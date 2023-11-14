package uk.ac.ed.inf.api.factories;

import uk.ac.ed.inf.api.dtos.*;
import uk.ac.ed.inf.ilp.data.*;

import java.util.Arrays;

/**
 * A factory for creating data objects from received API DTOs (Data Transfer Object).
 */
public class DataObjectsFactory implements IDataObjectsFactory
{
    @Override
    public CreditCardInformation createCreditCardInformation(CreditCardInformationDto dto)
    {
        return new CreditCardInformation(dto.getCreditCardNumber(), dto.getCreditCardExpiry(), dto.getCvv());
    }

    @Override
    public Pizza createPizza(PizzaDto dto)
    {
        return new Pizza(dto.getName(), dto.getPrinceInPence());
    }

    @Override
    public Restaurant createRestaurant(RestaurantDto dto)
    {
        return new Restaurant(
                dto.getName(),
                createLngLat(dto.getLocation()),
                dto.getOpeningDays(),
                Arrays.stream(dto.getMenu())
                        .map(this::createPizza)
                        .toArray(Pizza[]::new));
    }

    @Override
    public Order createOrder(OrderDto dto)
    {
        return new Order(
                dto.getOrderNo(),
                dto.getOrderDate(),
                dto.getPriceTotalInPence(),
                Arrays.stream(dto.getPizzasInOrder())
                        .map(this::createPizza)
                        .toArray(Pizza[]::new),
                createCreditCardInformation(dto.getCreditCardInformation()));
    }

    @Override
    public NamedRegion createNamedRegion(NamedRegionDto dto)
    {
        return new NamedRegion(
                dto.getName(),
                Arrays.stream(dto.getVertices())
                        .map(this::createLngLat)
                        .toArray(LngLat[]::new));
    }

    @Override
    public LngLat createLngLat(LngLatDto dto)
    {
        return new LngLat(dto.getLng(), dto.getLat());
    }
}
