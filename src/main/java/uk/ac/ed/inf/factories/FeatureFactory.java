package uk.ac.ed.inf.factories;

import uk.ac.ed.inf.ilp.data.LngLat;
import uk.ac.ed.inf.lib.systemFileWriter.geoJSON.Feature;
import uk.ac.ed.inf.lib.systemFileWriter.geoJSON.IFeature;
import uk.ac.ed.inf.lib.systemFileWriter.geoJSON.IFeatureFactory;

public class FeatureFactory implements IFeatureFactory
{
    @Override
    public Feature<IFeature.LineString> createLineString(LngLat[] position)
    {
        return new Feature<>(new IFeature.LineString(position));
    }
}
