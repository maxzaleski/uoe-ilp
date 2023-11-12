package uk.ac.ed.inf.api.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

public class NamedRegionDto
{
    @JsonProperty("name")
    private String name;

    @JsonProperty("vertices")
    private LngLatDto[] vertices;

    public String getName()
    {
        return name;
    }

    public LngLatDto[] getVertices()
    {
        return vertices;
    }
}
