package uk.ac.ed.inf.lib.systemFileWriter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import uk.ac.ed.inf.factories.FeatureFactory;
import uk.ac.ed.inf.ilp.data.LngLat;
import uk.ac.ed.inf.ilp.data.Order;
import uk.ac.ed.inf.lib.geoJSON.GeoJSON;
import uk.ac.ed.inf.lib.geoJSON.IFeature;
import uk.ac.ed.inf.lib.geoJSON.IFeatureFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Logger;

public class SystemFileWriter implements ISystemFileWriter
{
    final private String date;
    final private ObjectMapper jsonObjectMapper;
    final private Logger logger;

    /**
     * Represents a writer for the system's output files.
     *
     * @param date   the date for which the output files are being written.
     * @param logger the logger to use for logging.
     * @throws RuntimeException if the output directory cannot be created.
     */
    public SystemFileWriter(String date, Logger logger) throws RuntimeException
    {
        this.date = date;
        this.jsonObjectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        this.logger = logger;

        // Create output directory if it doesn't exist.
        final java.io.File directory = new java.io.File(LOCATION);
        if (!directory.exists())
            if (!directory.mkdir())
                throw new RuntimeException(String.format("failed to create directory '%s'", LOCATION));
    }

    public void writeOrders(Order[] orders) throws IOException
    {
        if (orders == null || orders.length == 0)
            throw new IllegalArgumentException("nothing to write; orders is null or empty");

        final SerialisableOrder[] writableOrders = Arrays.stream(orders).
                map(SerialisableOrder::new).
                toArray(SerialisableOrder[]::new);

        write(LOCATION + "deliveries-" + date + ".json", writableOrders);
    }

    public void writeGeoJson(LngLat[] path) throws IOException
    {
        if (path == null || path.length == 0)
            throw new IllegalArgumentException("nothing to write; features is null or empty");

        final IFeatureFactory featureFactory = new FeatureFactory();
        final IFeature[] features = new IFeature[1];
        features[0] = featureFactory.createLineString(path);

        write(LOCATION + "drone-" + date + ".geojson", new GeoJSON(features));
    }

    /**
     * Writes the given data to the given destination.
     *
     * @param dest the destination to write to.
     * @param data the data to write.
     * @param <T>  the type of the data.
     * @throws IllegalArgumentException if the data is null or empty.
     * @throws IOException              if the data cannot be written to the destination.
     */
    private <T> void write(String dest, T data) throws IOException
    {
        try
        {
            final FileWriter file = new java.io.FileWriter(dest);
            file.write(jsonObjectMapper.writeValueAsString(data));
            file.close();
        } catch (IOException e)
        {
            throw new IOException(String.format("failed to write data to '%s': %s", dest, e.getMessage()));
        }

        logger.info(String.format("[system] successfully wrote to '%s'", dest));
    }
}
