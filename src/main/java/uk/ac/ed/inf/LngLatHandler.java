package uk.ac.ed.inf;

import uk.ac.ed.inf.ilp.constant.SystemConstants;
import uk.ac.ed.inf.ilp.data.LngLat;
import uk.ac.ed.inf.ilp.data.NamedRegion;
import uk.ac.ed.inf.ilp.interfaces.LngLatHandling;

/**
 * Represents a handler for longitude and latitude calculations.
 */
public class LngLatHandler implements LngLatHandling
{
    private static final double ANGLE_MULTIPLE = 360 / 16.0; // ← number of compose rose directions.

    /**
     * Calculates the distance between two points within a Euclidean context.
     *
     * @param startPosition The starting position.
     * @param endPosition   The ending position.
     * @return The distance between the two points.
     * @throws IllegalArgumentException If the starting and/or ending positions are null.
     */
    @Override
    public double distanceTo(LngLat startPosition, LngLat endPosition) throws IllegalArgumentException
    {
        if (startPosition == null || endPosition == null)
            throw new IllegalArgumentException("The starting and/or ending positions cannot be null.");

        double xDiff = startPosition.lng() - endPosition.lng();
        double yDiff = startPosition.lat() - endPosition.lat();

        // If the two points are the same, the distance is 0; exit early to avoid unnecessary calculations.
        if (xDiff == 0 && yDiff == 0)
        {
            return 0;
        }

        // Calculates the Euclidean distance between the two points:
        // ⇒ √((x₂ - x₁)² + (y₂ - y₁)²)
        return Math.sqrt(Math.pow(xDiff, 2) + Math.pow(yDiff, 2));
    }

    /**
     * Checks if the two points are close to each other based on the distance tolerance constant
     * ({@value SystemConstants#DRONE_IS_CLOSE_DISTANCE}).
     *
     * @param startPosition The starting position.
     * @param otherPosition The ending position.
     * @return True if the two points are close to each other, false otherwise.
     * @throws IllegalArgumentException If the starting and/or ending positions are null.
     */
    @Override
    public boolean isCloseTo(LngLat startPosition, LngLat otherPosition) throws IllegalArgumentException
    {
        if (startPosition == null || otherPosition == null)
            throw new IllegalArgumentException("The starting and/or ending positions cannot be null.");

        return distanceTo(startPosition, otherPosition) < SystemConstants.DRONE_IS_CLOSE_DISTANCE;
    }

    /**
     * Checks if the position is within the region.
     *
     * @param position The position to check.
     * @param region   The region as a closed polygon (min. 3 vertices).
     * @return True if the position is within the region, false otherwise.
     * @throws IllegalArgumentException If the position and/or region are null.
     */
    @Override
    public boolean isInRegion(LngLat position, NamedRegion region) throws IllegalArgumentException
    {
        if (position == null)
            throw new IllegalArgumentException("The position cannot be null.");
        if (region == null)
            throw new IllegalArgumentException("The region cannot be null.");

        final LngLat[] vertices = region.vertices();

        // 1. The region must be a closed polygon.
        if (vertices.length < 3)
            throw new IllegalArgumentException("The region must have at least 3 vertices to be a closed polygon.");

        final double xp = position.lng();
        final double yp = position.lat();

        // 2. Check if the position is within the region using the ray casting algorithm.
        //
        // → For the point to be inside the polygon, the ray must intersect the polygon's edges an odd number of times.
        // Otherwise, the point is outside.
        //
        // [Disclaimer] I'm not the author of this algorithm (or impl. thereof); I've adapted it from the video below:
        // → Video: 'Inside code' @ https://www.youtube.com/watch?v=RSXM9bgqxJM&ab_channel=Insidecode
        // → Source code: https://gist.github.com/inside-code-yt/7064d1d1553a2ee117e60217cfd1d099

        int intersectCount = 0;
        for (int i = 0; i < vertices.length; i++)
        {
            final LngLat vtx1 = vertices[i];
            final double x1 = vtx1.lng();
            final double y1 = vtx1.lat();

            final LngLat vtx2 = vertices[(i + 1) % vertices.length]; // last edge: {vertices[n-1], vertices[0]}
            final double x2 = vtx2.lng();
            final double y2 = vtx2.lat();

            // Check if the ray intersects the edge:
            //
            // [Remark] The condition fails if the point is exactly on the edge of two vertices. In practice,
            // I'm not expecting this to happen often, and as such choosing to ignore this case for now.
            if ((yp < y1) != (yp < y2) && xp < x1 + ((yp - y1) / (y2 - y1)) * (x2 - x1)) intersectCount++;
        }

        return intersectCount % 2 == 1; // odd → inside, even → outside
    }

    /**
     * Calculates the next position based on the starting position and the angle of movement.
     *
     * @param startPosition The starting position.
     * @param angle         The angle to be applied (must be one of the
     *                      <a href="https://commons.wikimedia.org/w/index.php?curid=2249878">16 compass directions
     *                      </a> 22.5° increments).
     * @return The new position.
     * @throws IllegalArgumentException If the starting position is null or the angle is not between [0, 360].
     */
    @Override
    public LngLat nextPosition(LngLat startPosition, double angle) throws IllegalArgumentException
    {
        if (startPosition == null)
            throw new IllegalArgumentException("The starting position cannot be null.");

        // [requirement] The drone can only move per one of the 16 major compass directions.
        if (angle % ANGLE_MULTIPLE != 0)
        {
            final String message = String.format("The angle must be a multiple of %s.", ANGLE_MULTIPLE);
            throw new IllegalArgumentException(message);
        }

        // Convert degrees into radians.
        final double angleInRadi = Math.toRadians(angle);
        final double R = SystemConstants.DRONE_MOVE_DISTANCE; // (angular distance)

        // Calculate the next position:
        return new LngLat(
                // x₂ = x₁ + R * cos(θ)
                startPosition.lng() + R * Math.cos(angleInRadi),
                // y₂ = y₁ + R * sin(θ)
                startPosition.lat() + R * Math.sin(angleInRadi));
    }
}
