package uk.ac.ed.inf;

import junit.framework.TestCase;
import uk.ac.ed.inf.ilp.constant.OrderStatus;
import uk.ac.ed.inf.ilp.constant.OrderValidationCode;
import uk.ac.ed.inf.ilp.constant.SystemConstants;
import uk.ac.ed.inf.ilp.data.CreditCardInformation;
import uk.ac.ed.inf.ilp.data.Order;
import uk.ac.ed.inf.ilp.data.Pizza;
import uk.ac.ed.inf.ilp.data.Restaurant;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class OrderValidatorTest extends TestCase
{
    final private OrderValidator validator = new OrderValidator(); // Stateless.

    /**
     * [requirement] The order has been assigned a unique order number.
     */
    public void testContext_OrderNumber()
    {
        final Order order = new Order();
        validator.validateOrder(order, new Restaurant[]{null});

        assertEquals(OrderValidationCode.UNDEFINED, order.getOrderValidationCode());
        assertEquals(OrderStatus.INVALID, order.getOrderStatus());
    }

    /**
     * [requirement] The order was placed today.
     */
    public void testContext_OrderDate()
    {
        final Order order = new Order();
        order.setOrderNo("foobar");
        order.setOrderDate(LocalDate.now().minusDays(1));

        validator.validateOrder(order, new Restaurant[]{null});

        assertEquals(OrderValidationCode.UNDEFINED, order.getOrderValidationCode());
        assertEquals(OrderStatus.INVALID, order.getOrderStatus());
    }

    /**
     * [requirement] The order has yet to be handled by the system.
     */
    public void testContext_OrderStatus()
    {
        final Order order = new Order();
        order.setOrderNo("foobar");
        order.setOrderDate(LocalDate.now());
        order.setOrderStatus(OrderStatus.VALID_BUT_NOT_DELIVERED);

        validator.validateOrder(order, new Restaurant[]{null});

        assertEquals(OrderValidationCode.UNDEFINED, order.getOrderValidationCode());
        assertEquals(OrderStatus.INVALID, order.getOrderStatus());
    }

    /**
     * [requirement] The order has yet to be handled by the system.
     */
    public void testContext_OrderValidation()
    {
        final Order order = new Order();
        order.setOrderNo("foobar");
        order.setOrderDate(LocalDate.now());
        order.setOrderStatus(OrderStatus.UNDEFINED);
        order.setOrderValidationCode(OrderValidationCode.NO_ERROR);

        validator.validateOrder(order, new Restaurant[]{null});

        assertEquals(OrderValidationCode.UNDEFINED, order.getOrderValidationCode());
        assertEquals(OrderStatus.INVALID, order.getOrderStatus());
    }

    /**
     * [requirement] Check that the CVV is 3 digits long and converts to a number.
     */
    public void testCard_CVV()
    {
        final String[] cases = new String[]{
                "12",
                "1234",
                "a12",
                "a123",
                "@12",
        };
        for (String cvv : cases)
        {
            final CreditCardInformation cardInformation = new CreditCardInformation();
            cardInformation.setCvv(cvv);

            final Order order = buildStage1Order();
            order.setCreditCardInformation(cardInformation);

            validator.validateOrder(order, new Restaurant[]{null});

            assertEquals(OrderValidationCode.CVV_INVALID, order.getOrderValidationCode());
            assertEquals(OrderStatus.INVALID, order.getOrderStatus());
        }
    }

    /**
     * [requirement] Check that the card number is 16 digits long and converts to a number.
     */
    public void testCard_Number()
    {
        final CreditCardInformation cardInformation = new CreditCardInformation();
        cardInformation.setCvv("123");

        final String[] cases = new String[]{
                "123456789012345",    // 15
                "a1234567890123456",  // 15 + char
                "@1234567890123456",  // 15 + special char
                "123456789012345678", // 17
                "abcde",
        };
        for (String card : cases)
        {
            cardInformation.setCreditCardNumber(card);

            final Order order = buildStage1Order();
            order.setCreditCardInformation(cardInformation);

            validator.validateOrder(order, new Restaurant[]{null});

            assertEquals(OrderValidationCode.CARD_NUMBER_INVALID, order.getOrderValidationCode());
            assertEquals(OrderStatus.INVALID, order.getOrderStatus());
        }
    }

    /**
     * [requirement] Check that the expiry date is of valid format and in the future.
     */
    public void testCard_ExpiryDate()
    {
        final CreditCardInformation cardInformation = new CreditCardInformation();
        cardInformation.setCvv("123");
        cardInformation.setCreditCardNumber("1234567890123456");

        final String[] cases = new String[]{
                "12/22",
                "13/23",
                "a1/23",
                "-1/23",
                "1/23",
                "01/a",
                "0a/0b",
        };
        for (String date : cases)
        {
            cardInformation.setCreditCardExpiry(date);

            final Order order = buildStage1Order();
            order.setCreditCardInformation(cardInformation);

            validator.validateOrder(order, new Restaurant[]{null});

            assertEquals(OrderValidationCode.EXPIRY_DATE_INVALID, order.getOrderValidationCode());
            assertEquals(OrderStatus.INVALID, order.getOrderStatus());
        }
    }

    /**
     * [requirement] The order contains at least 1 pizza but a maximum of 4.
     */
    public void testRestaurant_PizzaCount()
    {

        final Map<OrderValidationCode, Pizza[]> cases = new HashMap<>();
        cases.put(OrderValidationCode.PIZZA_NOT_DEFINED, new Pizza[]{});
        cases.put(OrderValidationCode.MAX_PIZZA_COUNT_EXCEEDED, new Pizza[]{
                new Pizza("", 1),
                new Pizza("", 1),
                new Pizza("", 1),
                new Pizza("", 1),
                new Pizza("", 1),
        });
        for (OrderValidationCode code : cases.keySet())
        {
            final Order order = buildStage2Order();
            order.setPizzasInOrder(cases.get(code));

            validator.validateOrder(order, new Restaurant[]{null});

            assertEquals(code, order.getOrderValidationCode());
            assertEquals(OrderStatus.INVALID, order.getOrderStatus());
        }
    }

    /**
     * [requirement] A restaurant has any of the pizzas on its menu.
     */
    public void testRestaurant_Menu()
    {
        final Order order = buildStage2Order();
        order.setPizzasInOrder(new Pizza[]{
                new Pizza("foobar", 1),
        });

        final Restaurant restaurant = new Restaurant(
                "Restaurant",
                null,
                null,
                new Pizza[]{new Pizza("barfoo", 1)}
        );

        validator.validateOrder(order, new Restaurant[]{restaurant});

        assertEquals(OrderValidationCode.PIZZA_NOT_DEFINED, order.getOrderValidationCode());
        assertEquals(OrderStatus.INVALID, order.getOrderStatus());
    }

    /**
     * [requirement] The restaurant is open.
     */
    public void testRestaurant_OpenDays()
    {
        final Pizza pizza = new Pizza("foobar", 1);
        final Order order = buildStage2Order();
        order.setPizzasInOrder(new Pizza[]{pizza});

        final Restaurant restaurant = new Restaurant(
                "Restaurant",
                null,
                new DayOfWeek[]{},
                new Pizza[]{pizza}
        );

        validator.validateOrder(order, new Restaurant[]{restaurant});

        assertEquals(OrderValidationCode.RESTAURANT_CLOSED, order.getOrderValidationCode());
        assertEquals(OrderStatus.INVALID, order.getOrderStatus());
    }

    /**
     * [requirement] All pizzas in the order are from the same restaurant.
     */
    public void testRestaurant_SingleRestaurant()
    {
        final Pizza pizza = new Pizza("foobar", 1);
        final Restaurant restaurant = buildRestaurant(new Pizza[]{pizza});

        final Order order = buildStage2Order();
        order.setPizzasInOrder(new Pizza[]{
                pizza,
                new Pizza("", 1), // From a secondary restaurant.
        });

        validator.validateOrder(order, new Restaurant[]{restaurant});

        assertEquals(OrderValidationCode.PIZZA_FROM_MULTIPLE_RESTAURANTS, order.getOrderValidationCode());
        assertEquals(OrderStatus.INVALID, order.getOrderStatus());
    }

    /**
     * [requirement] The total in pence is correct.
     */
    public void testTotal()
    {
        final Pizza pizza1 = new Pizza("foobar", 1);
        final Pizza pizza2 = new Pizza("barfoo", 1);
        final Restaurant restaurant = buildRestaurant(new Pizza[]{pizza1, pizza2});

        final Order order = buildStage2Order();
        order.setPizzasInOrder(new Pizza[]{pizza1, pizza2});
        order.setPriceTotalInPence(SystemConstants.ORDER_CHARGE_IN_PENCE);

        validator.validateOrder(order, new Restaurant[]{restaurant});

        assertEquals(OrderValidationCode.TOTAL_INCORRECT, order.getOrderValidationCode());
        assertEquals(OrderStatus.INVALID, order.getOrderStatus());
    }

    /**
     * [requirement] The total in pence includes the delivery charge.
     */
    public void testTotal_IncludeDeliveryCharge()
    {
        final Pizza pizza1 = new Pizza("foobar", 1);
        final Pizza pizza2 = new Pizza("barfoo", 1);
        final Restaurant restaurant = buildRestaurant(new Pizza[]{pizza1, pizza2});

        final Order order = buildStage2Order();
        order.setPizzasInOrder(new Pizza[]{pizza1, pizza2});
        order.setPriceTotalInPence(
                Arrays.stream(order.getPizzasInOrder()).
                        map(Pizza::priceInPence).
                        reduce(0, Integer::sum)
        );

        validator.validateOrder(order, new Restaurant[]{restaurant});

        assertEquals(OrderValidationCode.TOTAL_INCORRECT, order.getOrderValidationCode());
        assertEquals(OrderStatus.INVALID, order.getOrderStatus());
    }

    /**
     * [requirement] *includes all the above*.
     * [requirement] A valid order is deliverable.
     */
    public void testDeliverableOrder()
    {
        final Pizza pizza1 = new Pizza("foobar", 250);
        final Pizza pizza2 = new Pizza("barfoo", 500);
        final Restaurant restaurant = buildRestaurant(new Pizza[]{pizza1, pizza2});

        final Order order = buildStage2Order();
        order.setPizzasInOrder(new Pizza[]{pizza1, pizza2});
        order.setPriceTotalInPence(
                Arrays.stream(order.getPizzasInOrder()).
                        map(Pizza::priceInPence).
                        reduce(SystemConstants.ORDER_CHARGE_IN_PENCE, Integer::sum) // Base delivery charge of £1.
        );

        validator.validateOrder(order, new Restaurant[]{restaurant});

        assertEquals(OrderValidationCode.NO_ERROR, order.getOrderValidationCode());
        assertEquals(OrderStatus.VALID_BUT_NOT_DELIVERED, order.getOrderStatus());
    }

    public void testIllegalArgumentException_Order()
    {
        try
        {
            validator.validateOrder(null, null);
            fail("expected 'IllegalArgumentException' to be thrown");
        } catch (IllegalArgumentException e)
        {
            // Caught as expected.
            assertTrue(e.getMessage().contains("order"));
        }
    }

    public void testIllegalArgumentException_Restaurants()
    {
        final Restaurant[][] cases = new Restaurant[][]{
                null,
                new Restaurant[]{}
        };
        for (Restaurant[] restaurants : cases)
        {
            try
            {
                validator.validateOrder(buildStage2Order(), restaurants);
                fail("expected 'IllegalArgumentException' to be thrown");
            } catch (IllegalArgumentException e)
            {
                // Caught as expected.
                assertTrue(e.getMessage().contains("restaurants"));
            }
        }
    }


    // ------------------------ [builders] ------------------------

    private Order buildStage1Order()
    {
        final Order order = new Order();
        order.setOrderNo("foobar");
        order.setOrderDate(LocalDate.now());
        order.setOrderStatus(OrderStatus.UNDEFINED);
        order.setOrderValidationCode(OrderValidationCode.UNDEFINED);
        return order;
    }

    private Order buildStage2Order()
    {
        final CreditCardInformation cardInformation = new CreditCardInformation();
        cardInformation.setCvv("123");
        cardInformation.setCreditCardNumber("1234567890123456");
        cardInformation.setCreditCardExpiry("12/28");

        final Order order = buildStage1Order();
        order.setCreditCardInformation(cardInformation);

        return order;
    }

    private Restaurant buildRestaurant(Pizza[] items)
    {
        return new Restaurant(
                "Restaurant",
                null,
                new DayOfWeek[]{LocalDate.now().getDayOfWeek()},
                items
        );
    }
}
