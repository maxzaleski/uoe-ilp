package uk.ac.ed.inf.lib.systemFileWriter.geoJSON;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a <a href="https://geojson.org/">GeoJSON</a> object.
 */
public class GeoJSON
{
    @JsonProperty("type")
    private final String type = "FeatureCollection";
    @JsonProperty("features")
    private final IFeature[] features;

    /**
     * Constructs a GeoJSON object.
     *
     * @param features the features to be contained in the GeoJSON object.
     */
    public GeoJSON(IFeature[] features)
    {
        this.features = features;
    }
}
