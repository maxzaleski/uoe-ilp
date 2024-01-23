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
    protected HashMap<String, Object> properties;
    @JsonProperty("geometry")
    protected Geometry<G> geometry;
    @JsonProperty("type")
    private String type = "Feature";

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

    public Feature()
    {
        this(null);
    }

    public HashMap<String, Object> getProperties()
    {
        return properties;
    }

    public Geometry<G> getGeometry()
    {
        return geometry;
    }

    public String getType()
    {
        return type;
    }

    public void setProperties(HashMap<String, Object> properties)
    {
        this.properties = properties;
    }

    /**
     * Represents a generic GeoJSON geometry.
     *
     * @param <T> the type of the geometry's coordinates.
     */
    public static class Geometry<T>
    {
        @JsonProperty("coordinates")
        protected T coordinates;
        @JsonProperty("type")
        private String type;

        public Geometry(String type, T coordinates)
        {
            this.type = type;
            this.coordinates = coordinates;
        }

        public Geometry()
        {
            this(null, null);
        }

        public T getCoordinates()
        {
            return coordinates;
        }

        public String getType()
        {
            return type;
        }

        public void setCoordinates(T coordinates)
        {
            this.coordinates = coordinates;
        }

        public void setType(String type)
        {
            this.type = type;
        }
    }
}
