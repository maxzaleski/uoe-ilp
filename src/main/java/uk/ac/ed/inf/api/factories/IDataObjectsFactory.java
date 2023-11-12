package uk.ac.ed.inf.api.factories;

import uk.ac.ed.inf.api.dtos.*;
import uk.ac.ed.inf.ilp.data.*;

public interface IDataObjectsFactory
{
    /**
     * Creates a new {@link CreditCardInformation} object from the given {@link CreditCardInformationDto}.
     */
    CreditCardInformation createCreditCardInformation(CreditCardInformationDto dto);

    /**
     * Creates a new {@link Pizza} object from the given {@link PizzaDto}.
     */
    Pizza createPizza(PizzaDto dto);

    /**
     * Creates a new {@link Restaurant} object from the given {@link RestaurantDto}.
     */
    Restaurant createRestaurant(RestaurantDto dto);

    /**
     * Creates a new {@link Order} object from the given {@link OrderDto}.
     */
    Order createOrder(OrderDto dto);

    /**
     * Creates a new {@link NamedRegion} object from the given {@link NamedRegionDto}.
     */
    NamedRegion createNamedRegion(NamedRegionDto dto);

    /**
     * Creates a new {@link LngLat} object from the given {@link LngLatDto}.
     */
    LngLat createLngLat(LngLatDto dto);
}
