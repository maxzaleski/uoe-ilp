package uk.ac.ed.inf.api.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PizzaDto
{
    @JsonProperty("name")
    private String name;
    @JsonProperty("priceInPence")
    private Integer princeInPence;

    public String getName()
    {
        return name;
    }

    public Integer getPrinceInPence()
    {
        return princeInPence;
    }
}
