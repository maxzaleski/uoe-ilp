package uk.ac.ed.inf.lib.systemFileWriter.geoJSON;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.ac.ed.inf.ilp.data.LngLat;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a <a href="https://geojson.org/">GeoJSON</a> object.
 */
public class GeoJSON
{
    @JsonProperty("type")
    private String type = "FeatureCollection";
    @JsonProperty("features")
    private List<Feature<?>> features;

    /**
     * Constructs a GeoJSON object.
     *
     * @param features the features to be contained in the GeoJSON object.
     */
    public GeoJSON(List<Feature<?>> features)
    {
        if (features == null) features = new ArrayList<>();
        this.features = features;
    }

    public GeoJSON()
    {
        this(null);
    }

    /**
     * Adds a line string feature to the GeoJSON object.
     *
     * @param positions the positions to be included.
     */
    public void addLineString(LngLat[] positions)
    {
        this.features.add(new LineString(positions));
    }

    public String getType()
    {
        return type;
    }

    public List<Feature<?>> getFeatures()
    {
        return features;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public void setFeatures(List<Feature<?>> features)
    {
        this.features = features;
    }
}
