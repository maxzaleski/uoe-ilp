package uk.ac.ed.inf.lib.systemFileWriter.geoJSON;

import uk.ac.ed.inf.ilp.data.LngLat;

/**
 * Represents a <a href="https://geojson.org/">GeoJSON</a> object.
 */
public interface IGeoJSON
{
    /**
     * Adds a line string feature to the GeoJSON object.
     *
     * @param positions the positions to be included.
     */
    void addLineString(LngLat[] positions);
}
