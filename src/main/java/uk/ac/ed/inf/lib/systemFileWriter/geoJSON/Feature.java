package uk.ac.ed.inf.lib.systemFileWriter.geoJSON;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;

/**
 * Represents a generic GeoJSON feature.
 *
 * @param <G> the type of positions represented by the geometry.
 */
public class Feature<G>
{
    @JsonProperty("properties")
    protected final HashMap<String, Object> properties;
    @JsonProperty("geometry")
    protected final Geometry<G> geometry;
    @JsonProperty("type")
    private final String type = "Feature";

    /**
     * Constructs a GeoJSON feature.
     *
     * @param geometry the feature's geometry
     */
    public Feature(Geometry<G> geometry)
    {
        this.geometry = geometry;
        this.properties = new HashMap<>();
    }

    /**
     * Represents a generic GeoJSON geometry.
     *
     * @param <T> the type of the geometry's coordinates.
     */
    public static class Geometry<T>
    {
        @JsonProperty("coordinates")
        protected final T coordinates;
        @JsonProperty("type")
        private final String type;

        public Geometry(String type, T coordinates)
        {
            this.type = type;
            this.coordinates = coordinates;
        }
    }
}
