package uk.ac.ed.inf.lib.systemFileWriter.geoJSON;

import uk.ac.ed.inf.ilp.data.LngLat;

/**
 * Represents a GeoJSON LineString.
 */
public class LineString extends Feature<double[][]>
{
    /**
     * Constructs a GeoJSON LineString.
     *
     * @param position the feature's positions.
     */
    public LineString(LngLat[] position)
    {
        super(new Geometry<>("LineString", new double[position.length][]));
        for (int i = 0; i < position.length; i++)
        {
            double[][] coordinates = geometry.coordinates;
            coordinates[i] = new double[2];
            coordinates[i][0] = position[i].lng();
            coordinates[i][1] = position[i].lat();
        }
    }
}
