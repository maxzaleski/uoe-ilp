package uk.ac.ed.inf.api.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CreditCardInformationDto
{
    @JsonProperty("creditCardNumber")
    private String creditCardNumber;
    @JsonProperty("creditCardExpiry")
    private String creditCardExpiry;
    @JsonProperty("cvv")
    private String cvv;

    public String getCreditCardNumber()
    {
        return creditCardNumber;
    }

    public String getCreditCardExpiry()
    {
        return creditCardExpiry;
    }

    public String getCvv()
    {
        return cvv;
    }
}
