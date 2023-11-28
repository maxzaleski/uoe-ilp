package uk.ac.ed.inf.lib.systemFileWriter.geoJSON;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.ac.ed.inf.ilp.data.LngLat;

import java.util.ArrayList;
import java.util.List;

public class GeoJSON implements IGeoJSON
{

    @JsonProperty("type")
    private final String type = "FeatureCollection";
    @JsonProperty("features")
    private final List<IFeature> features;

    /**
     * Constructs a GeoJSON object.
     *
     * @param features the features to be contained in the GeoJSON object.
     */
    public GeoJSON(List<IFeature> features)
    {
        if (features == null) features = new ArrayList<>();
        this.features = features;
    }

    public void addLineString(LngLat[] positions)
    {
        this.features.add(new Feature<>(new IFeature.LineString(positions)));
    }
}
