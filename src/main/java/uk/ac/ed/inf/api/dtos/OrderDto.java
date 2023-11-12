package uk.ac.ed.inf.api.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.ac.ed.inf.ilp.constant.OrderStatus;
import uk.ac.ed.inf.ilp.constant.OrderValidationCode;

import java.time.LocalDate;

public class OrderDto
{
    @JsonProperty("orderNo")
    private String orderNo;

    @JsonProperty("orderDate")
    private LocalDate orderDate;

    @JsonProperty("orderStatus")
    private OrderStatus orderStatus;

    @JsonProperty("orderValidationCode")
    private OrderValidationCode orderValidationCode;

    @JsonProperty("priceTotalInPence")
    private int priceTotalInPence;

    @JsonProperty("pizzasInOrder")
    private PizzaDto[] pizzasInOrder;

    @JsonProperty("creditCardInformation")
    private CreditCardInformationDto creditCardInformation;

    public String getOrderNo()
    {
        return orderNo;
    }

    public LocalDate getOrderDate()
    {
        return orderDate;
    }

    public OrderStatus getOrderStatus()
    {
        return orderStatus;
    }

    public OrderValidationCode getOrderValidationCode()
    {
        return orderValidationCode;
    }

    public int getPriceTotalInPence()
    {
        return priceTotalInPence;
    }

    public PizzaDto[] getPizzasInOrder()
    {
        return pizzasInOrder;
    }

    public CreditCardInformationDto getCreditCardInformation()
    {
        return creditCardInformation;
    }
}
