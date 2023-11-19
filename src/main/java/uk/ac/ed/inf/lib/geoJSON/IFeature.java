package uk.ac.ed.inf.lib.geoJSON;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.ac.ed.inf.ilp.data.LngLat;

/**
 * Represents a GeoJSON feature.
 *
 * @link <a href="https://en.wikipedia.org/wiki/GeoJSON">Documentation</a>
 */
public interface IFeature
{
    /**
     * Represents a generic GeoJSON geometry.
     *
     * @param <T> the type of the geometry's coordinates.
     */
    class Geometry<T>
    {
        @JsonProperty("type")
        protected final String type;
        @JsonProperty("coordinates")
        protected final T coordinates;

        public Geometry(String type, T coordinates)
        {
            this.type = type;
            this.coordinates = coordinates;
        }
    }

    /**
     * Represents a GeoJSON LineString.
     */
    class LineString extends IFeature.Geometry<double[][]>
    {
        public LineString(LngLat[] position)
        {
            super("LineString", new double[position.length][]);
            for (int i = 0; i < position.length; i++)
            {
                this.coordinates[i] = new double[2];
                this.coordinates[i][0] = position[i].lng();
                this.coordinates[i][1] = position[i].lat();
            }
        }
    }
}
