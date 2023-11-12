package uk.ac.ed.inf.api.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LngLatDto
{
    @JsonProperty("lng")
    private double lng;
    @JsonProperty("lat")
    private double lat;

    public double getLng()
    {
        return lng;
    }

    public double getLat()
    {
        return lat;
    }
}
