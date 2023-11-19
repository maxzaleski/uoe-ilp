package uk.ac.ed.inf.lib.systemFileWriter.geoJSON;

import uk.ac.ed.inf.ilp.data.LngLat;

/**
 * Represents a factory for creating GeoJSON features.
 */
public interface IFeatureFactory
{
    /**
     * Creates a GeoJSON feature representing a line string.
     *
     * @param position the feature's position.
     * @return a GeoJSON feature representing a line string.
     */
    Feature<IFeature.LineString> createLineString(LngLat[] position);
}
