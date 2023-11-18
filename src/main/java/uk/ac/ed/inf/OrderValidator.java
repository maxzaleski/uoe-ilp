package uk.ac.ed.inf;

import uk.ac.ed.inf.ilp.constant.OrderStatus;
import uk.ac.ed.inf.ilp.constant.OrderValidationCode;
import uk.ac.ed.inf.ilp.constant.SystemConstants;
import uk.ac.ed.inf.ilp.data.CreditCardInformation;
import uk.ac.ed.inf.ilp.data.Order;
import uk.ac.ed.inf.ilp.data.Pizza;
import uk.ac.ed.inf.ilp.data.Restaurant;
import uk.ac.ed.inf.ilp.interfaces.OrderValidation;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Optional;

/**
 * Represents the order validator.
 */
public class OrderValidator implements OrderValidation
{
    /**
     * Validate an order and deliver a validated version where the `OrderStatus` and `OrderValidationCode` are set
     * accordingly.
     *
     * @param orderToValidate    The order which needs validation.
     * @param definedRestaurants The vector of defined restaurants with their according menu structure.
     * @return the validated order.
     * @throws IllegalArgumentException If the order to validate is null and/or restaurants are null or empty.
     */
    @Override
    public Order validateOrder(Order orderToValidate, Restaurant[] definedRestaurants) throws IllegalArgumentException
    {
        if (orderToValidate == null)
            throw new IllegalArgumentException("The order to validate cannot be null.");
        if (definedRestaurants == null || definedRestaurants.length == 0)
            throw new IllegalArgumentException("The defined restaurants cannot be null or empty.");

        // [stage1: context] Check that the order context is valid.
        if (!this._validateContext(orderToValidate))
            return _invalidateOrder(orderToValidate, OrderValidationCode.UNDEFINED);

        // [stage 2: card] Check that the card information is valid.
        OrderValidationCode validationCode = _validateCard(orderToValidate.getCreditCardInformation());
        if (validationCode != OrderValidationCode.NO_ERROR)
            return _invalidateOrder(orderToValidate, validationCode);

        final Pizza[] items = orderToValidate.getPizzasInOrder();

        // [stage 3: restaurant] Check that any restaurant can fulfill the order.
        validationCode = _validateRestaurant(items, definedRestaurants);
        if (validationCode != OrderValidationCode.NO_ERROR)
            return _invalidateOrder(orderToValidate, validationCode);

        // [stage 4: price] Check that the total price is correct.
        validationCode = _validateTotal(items, orderToValidate.getPriceTotalInPence());
        if (validationCode != OrderValidationCode.NO_ERROR)
            return _invalidateOrder(orderToValidate, validationCode);

        // Order is valid and can be delivered.
        orderToValidate.setOrderStatus(OrderStatus.VALID_BUT_NOT_DELIVERED);
        orderToValidate.setOrderValidationCode(OrderValidationCode.NO_ERROR);
        return orderToValidate;
    }

    /**
     * Checks that the order context is valid:
     *
     * <p>
     * 1. The order has a unique order number
     * <p>
     * 2. The order was placed today
     * <p>
     * 3. The order has yet to be handled by the system
     * </p>
     *
     * @param order The order to validate.
     * @return True if the order context is valid, false otherwise.
     */
    private boolean _validateContext(Order order)
    {
        // [requirement] The order has been assigned a unique order number.
        if (order.getOrderNo().isEmpty())
            return false;

        // [requirement] The order has yet to be handled by the system.
        return order.getOrderStatus() == OrderStatus.UNDEFINED &&
                order.getOrderValidationCode() == OrderValidationCode.UNDEFINED;
    }

    /**
     * Checks that the card information is valid per the requirements defined in {@link CreditCardInformation}.
     *
     * @param cardInformation The card information to validate.
     * @return One of the following validation codes:
     * <p>
     * {@link OrderValidationCode#NO_ERROR} if the payment card is correct.
     * <p>
     * {@link OrderValidationCode#CVV_INVALID} if the card's CVV is incorrect.
     * <p>
     * {@link OrderValidationCode#CARD_NUMBER_INVALID} if the card's number is incorrect.
     * <p>
     * {@link OrderValidationCode#EXPIRY_DATE_INVALID} if the card's expiry date is incorrect.
     * </p>
     */
    private OrderValidationCode _validateCard(CreditCardInformation cardInformation)
    {
        // [requirement] Check that the CVV is 3 digits long and converts to a number.
        if (!cardInformation.getCvv().matches("\\d{3}"))
            return OrderValidationCode.CVV_INVALID;

        // [requirement] Check that the card number is 16 digits long and converts to a number.
        if (!cardInformation.getCreditCardNumber().matches("^\\d{16}$"))
            return OrderValidationCode.CARD_NUMBER_INVALID;

        // [requirement] Check that the expiry date is of valid format and in the future.
        try
        {
            final DateTimeFormatter f = DateTimeFormatter.ofPattern("MM/yy");
            final YearMonth parsed = YearMonth.parse(cardInformation.getCreditCardExpiry(), f);

            if (parsed.compareTo(YearMonth.now()) < 0)
                return OrderValidationCode.EXPIRY_DATE_INVALID; // Date is of valid format but in the past.
        } catch (DateTimeParseException e)
        {
            return OrderValidationCode.EXPIRY_DATE_INVALID; // Date is of invalid format.
        }

        return OrderValidationCode.NO_ERROR;
    }

    /**
     * Checks that the total price is correct and includes the default delivery charge of
     * {@value SystemConstants#ORDER_CHARGE_IN_PENCE}p (pence).
     *
     * @param items              The (pizza) items in the order.
     * @param actualTotalInPence The order's total price in pence.
     * @return One of the following validation codes:
     * <p>
     * {@link OrderValidationCode#NO_ERROR} if the total price is correct.
     * <p>
     * {@link OrderValidationCode#TOTAL_INCORRECT} if the total price is incorrect.
     * </p>
     */
    private OrderValidationCode _validateTotal(Pizza[] items, int actualTotalInPence)
    {
        // [requirement] Includes a delivery charge of £1.
        final int totalInPence = Arrays.stream(items).
                map(Pizza::priceInPence).
                reduce(SystemConstants.ORDER_CHARGE_IN_PENCE, Integer::sum);

        // [requirement] The total in pence is correct.
        return totalInPence != actualTotalInPence ?
                OrderValidationCode.TOTAL_INCORRECT :
                OrderValidationCode.NO_ERROR;
    }

    /**
     * Checks that the order can be fulfilled by any restaurant.
     *
     * @param items       The (pizza) items in the order.
     * @param restaurants The restaurants part of the scheme.
     * @return One of the following validation codes:
     * <p>
     * {@link OrderValidationCode#NO_ERROR} if the order can be fulfilled by any restaurant.
     * <p>
     * {@link OrderValidationCode#PIZZA_NOT_DEFINED} if the order contains less than one pizza or a pizza that is not
     * defined on any menu.
     * <p>
     * {@link OrderValidationCode#MAX_PIZZA_COUNT_EXCEEDED} if the order contains more than {@value SystemConstants#MAX_PIZZAS_PER_ORDER} pizzas.
     * <p>
     * {@link OrderValidationCode#RESTAURANT_CLOSED} if the restaurant is closed.
     * <p>
     * {@link OrderValidationCode#PIZZA_FROM_MULTIPLE_RESTAURANTS} if the order contains pizzas from multiple restaurants.
     * </p>
     */
    private OrderValidationCode _validateRestaurant(Pizza[] items, Restaurant[] restaurants)
    {
        // [requirement] The order contains at least 1 pizza but a maximum of 4.
        final int itemCount = items.length;
        if (itemCount < 1)
            return OrderValidationCode.PIZZA_NOT_DEFINED;
        else if (itemCount > SystemConstants.MAX_PIZZAS_PER_ORDER)
            return OrderValidationCode.MAX_PIZZA_COUNT_EXCEEDED;

        // [requirement] A restaurant has any of the pizzas on its menu.
        final Optional<Restaurant> maybeRestaurant = Arrays.stream(restaurants).
                filter(restaurant -> Arrays.stream(restaurant.menu()).
                        anyMatch(pizza -> pizza.name().equals(items[0].name()))
                ).findFirst();
        if (maybeRestaurant.isEmpty())
            return OrderValidationCode.PIZZA_NOT_DEFINED;

        // [requirement] The restaurant is open.
        final Restaurant restaurant = maybeRestaurant.get();
        if (Arrays.stream(restaurant.openingDays()).noneMatch(day -> day == LocalDate.now().getDayOfWeek()))
            return OrderValidationCode.RESTAURANT_CLOSED;

        // [requirement] All pizzas in the order are from the same restaurant.
        for (var i = 1; i < items.length; i++)
        {
            final String name = items[i].name();
            if (Arrays.stream(restaurant.menu()).noneMatch(pizza -> pizza.name().equals(name)))
                return OrderValidationCode.PIZZA_FROM_MULTIPLE_RESTAURANTS;
        }

        return OrderValidationCode.NO_ERROR;
    }

    /**
     * Invalidates an order by setting its status to {@link OrderStatus#INVALID} and validation code
     * {@link OrderValidationCode}.
     *
     * @param order          The order to invalidate.
     * @param validationCode The validation code.
     * @return The invalidated order.
     */
    private Order _invalidateOrder(Order order, OrderValidationCode validationCode)
    {
        order.setOrderStatus(OrderStatus.INVALID);
        order.setOrderValidationCode(validationCode);
        return order;
    }
}
