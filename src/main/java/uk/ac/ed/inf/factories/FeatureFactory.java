package uk.ac.ed.inf.factories;

import uk.ac.ed.inf.ilp.data.LngLat;
import uk.ac.ed.inf.lib.geoJSON.Feature;
import uk.ac.ed.inf.lib.geoJSON.IFeature;
import uk.ac.ed.inf.lib.geoJSON.IFeatureFactory;

public class FeatureFactory implements IFeatureFactory
{
    @Override
    public Feature<IFeature.LineString> createLineString(LngLat[] position)
    {
        return new Feature<>(new IFeature.LineString(position));
    }
}
