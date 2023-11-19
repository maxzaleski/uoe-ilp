package uk.ac.ed.inf.lib.systemFileWriter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import uk.ac.ed.inf.factories.FeatureFactory;
import uk.ac.ed.inf.ilp.data.LngLat;
import uk.ac.ed.inf.ilp.data.Order;
import uk.ac.ed.inf.lib.pathFinder.INode.Direction;
import uk.ac.ed.inf.lib.pathFinder.IPathFinder;
import uk.ac.ed.inf.lib.systemFileWriter.geoJSON.GeoJSON;
import uk.ac.ed.inf.lib.systemFileWriter.geoJSON.IFeature;
import uk.ac.ed.inf.lib.systemFileWriter.geoJSON.IFeatureFactory;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    public void writeOrders(Order[] orders) throws RuntimeException
    {
        if (orders == null || orders.length == 0)
            throw new IllegalArgumentException("nothing to write; orders is null or empty");

        final SerialisableOrder[] writableOrders = Arrays.stream(orders).
                map(SerialisableOrder::new).
                toArray(SerialisableOrder[]::new);

        write(LOCATION + "deliveries-" + date + ".json", writableOrders);
    }

    public void writeGeoJSON(LngLat[] path) throws RuntimeException
    {
        if (path == null || path.length == 0)
            throw new IllegalArgumentException("nothing to write; path is null or empty");

        final IFeatureFactory featureFactory = new FeatureFactory();
        final IFeature[] features = new IFeature[1];
        features[0] = featureFactory.createLineString(path);

        write(LOCATION + "drone-" + date + ".geojson", new GeoJSON(features));
    }

    public void writeFlightPath(IPathFinder.Result[] results) throws RuntimeException
    {
        if (results == null || results.length == 0)
            throw new IllegalArgumentException("nothing to write; results is null or empty");

        final List<SerialisableDroneMove> moves = Arrays.stream(results)
                .flatMap(result ->
                {
                    final Direction[] sortedPath = result.getRoute().toArray(Direction[]::new);
                    return IntStream.range(1, sortedPath.length)
                            .mapToObj(i -> new SerialisableDroneMove(result.getOrderNo(), sortedPath[i - 1], sortedPath[i]));
                })
                .collect(Collectors.toCollection(ArrayList::new));

        write(LOCATION + "flightpath-" + date + ".json", moves);
    }

    /**
     * Writes the given data to the given destination.
     *
     * @param dest the destination to write to.
     * @param data the data to write.
     * @param <T>  the type of the data.
     * @throws RuntimeException if an error occurs while writing.
     */
    private <T> void write(String dest, T data) throws RuntimeException
    {
        try
        {
            final FileWriter file = new java.io.FileWriter(dest);
            file.write(jsonObjectMapper.writeValueAsString(data));
            file.close();
        } catch (Exception e)
        {
            throw new RuntimeException(String.format("failed to write data to '%s': %s", dest, e.getMessage()), e);
        }

        logger.info(String.format("[system] successfully wrote to '%s'", dest));
    }
}
