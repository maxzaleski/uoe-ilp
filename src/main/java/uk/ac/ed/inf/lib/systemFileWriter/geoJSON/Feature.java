package uk.ac.ed.inf.lib.systemFileWriter.geoJSON;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;

/**
 * Represents an implementation of a GeoJSON feature.
 *
 * @param <T> the type of geometry contained in the feature.
 */
public class Feature<T extends IFeature.Geometry<?>> implements IFeature
{
    @JsonProperty("properties")
    protected final HashMap<String, Object> properties;
    @JsonProperty("type")
    private final String type = "Feature";
    @JsonProperty("geometry")
    private final T geometry;

    /**
     * Constructs a GeoJSON feature.
     *
     * @param geometry   the feature's geometry:
     *                   <ul>
     *                   <li>{@link LineString}</li>
     *                   <li>{@link Point}</li>
     *                   </ul>
     * @param properties the feature's properties.
     */
    public Feature(T geometry, HashMap<String, Object> properties)
    {
        this.geometry = geometry;
        this.properties = properties;
    }

    /**
     * Constructs a GeoJSON feature.
     *
     * @param geometry the feature's geometry:
     *                 <ul>
     *                 <li>{@link IFeature.LineString}</li>
     *                 <li>{@link IFeature.Point}</li>
     *                 </ul>
     */
    public Feature(T geometry)
    {
        this.geometry = geometry;
        this.properties = new HashMap<>();
    }
}
