package uk.ac.ed.inf.lib.api.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.DayOfWeek;

public class RestaurantDto
{
    @JsonProperty("name")
    private String name;
    @JsonProperty("location")
    private LngLatDto location;
    @JsonProperty("openingDays")
    private DayOfWeek[] openingDays;
    @JsonProperty("menu")
    private PizzaDto[] menu;

    public String getName()
    {
        return name;
    }

    public LngLatDto getLocation()
    {
        return location;
    }

    public DayOfWeek[] getOpeningDays()
    {
        return openingDays;
    }

    public PizzaDto[] getMenu()
    {
        return menu;
    }
}
