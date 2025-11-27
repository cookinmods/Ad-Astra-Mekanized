package com.hecookin.adastramekanized.worldgen.builder;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.hecookin.adastramekanized.worldgen.builder.DensityFunctionBuilder.SplinePoint;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder for creating Minecraft spline definitions.
 * Splines are used for smooth terrain transitions based on Tectonic patterns.
 * Supports both simple and multi-spline configurations.
 */
public class SplineBuilder {
    private final JsonElement coordinate;
    private final List<SplinePoint> points = new ArrayList<>();
    private final List<MultiSplineEntry> multiSplineEntries = new ArrayList<>();
    private boolean isMultiSpline = false;

    // Simple spline with coordinate reference
    public SplineBuilder(String coordinate) {
        this.coordinate = new JsonPrimitive(coordinate);
    }

    // Spline with density function coordinate
    public SplineBuilder(DensityFunctionBuilder coordinate) {
        this.coordinate = coordinate.build();
    }

    // Add point to simple spline
    public SplineBuilder point(float location, float value, float derivative) {
        points.add(new SplinePoint(location, value, derivative));
        return this;
    }

    // Convenience method for flat point (0 derivative)
    public SplineBuilder flatPoint(float location, float value) {
        return point(location, value, 0.0f);
    }

    // Convenience method for sharp point (high derivative)
    public SplineBuilder sharpPoint(float location, float value) {
        return point(location, value, 5.0f);
    }

    // Add SplinePoint object
    public SplineBuilder addPoint(SplinePoint point) {
        points.add(point);
        return this;
    }

    // For multi-spline (nested splines based on another coordinate)
    public SplineBuilder multiSpline(float location, float derivative, SplineBuilder nestedSpline) {
        isMultiSpline = true;
        multiSplineEntries.add(new MultiSplineEntry(location, derivative, nestedSpline));
        return this;
    }

    // Tectonic preset: Continental transition (sharp coastlines)
    public static SplineBuilder continentalTransition(String noiseSource) {
        return new SplineBuilder(noiseSource)
            .point(-1.1f, 0.044f, 0.0f)
            .point(-1.02f, -0.2222f, 0.0f)
            .point(-0.51f, -0.2222f, 0.0f)
            .point(-0.44f, -0.12f, 0.0f)
            .point(-0.18f, -0.12f, 0.0f)
            .sharpPoint(-0.16f, 0.0f)  // Sharp transition at coast
            .point(-0.15f, 0.012f, 0.0f)
            .point(-0.10f, 0.012f, 0.0f)
            .point(0.25f, 0.035f, 0.0f)
            .point(1.0f, 0.060f, 0.0f);
    }

    // Tectonic preset: Erosion factor
    public static SplineBuilder erosionFactor(String erosionSource) {
        return new SplineBuilder(erosionSource)
            .point(-1.0f, -0.0775f, 0.0f)
            .point(-0.78f, -0.0775f, 0.0f)
            .sharpPoint(-0.575f, -0.0425f)
            .point(-0.375f, -0.04f, 0.0f)
            .flatPoint(0.0f, 0.05f)
            .point(0.375f, 0.05f, 0.0f)
            .point(0.55f, 0.1725f, 0.0f)
            .point(0.7625f, 0.21f, 0.0f)
            .point(1.0f, 0.21f, 0.0f);
    }

    // Tectonic preset: Ridge/mountain formation
    public static SplineBuilder ridgeFactor(String ridgeSource) {
        return new SplineBuilder(ridgeSource)
            .point(-1.0f, -0.01f, 0.0f)
            .point(-0.4f, 0.0f, 0.0f)
            .point(0.0f, 0.14f, 0.0f)
            .sharpPoint(0.4f, 0.35f)  // Sharp peak
            .point(0.45f, 0.5575f, 0.0f)
            .point(0.55f, 0.6175f, 0.0f)
            .point(0.58f, 0.615f, 0.0f)
            .point(0.7f, 0.5575f, 0.0f)
            .point(1.0f, 0.415f, 0.0f);
    }

    // Tectonic preset: Desert dune pattern (based on vegetation)
    public static SplineBuilder desertDunes(String vegetationSource) {
        return new SplineBuilder(vegetationSource)
            .point(-1.0f, 0.0f, 0.0f)
            .point(-0.5f, 0.0f, 0.0f)
            .sharpPoint(-0.3f, 0.05f)  // Dune crest
            .flatPoint(0.0f, 0.0f)
            .sharpPoint(0.3f, 0.05f)   // Another dune
            .flatPoint(0.5f, 0.0f)
            .point(1.0f, 0.0f, 0.0f);
    }

    // Tectonic preset: Jungle pillar pattern
    public static SplineBuilder junglePillars(String humiditySource, float pillarHeight) {
        return new SplineBuilder(humiditySource)
            .point(-1.0f, 0.0f, 0.0f)
            .point(0.0f, 0.0f, 0.0f)
            .point(0.2f, 0.0f, 0.0f)
            .sharpPoint(0.7f, pillarHeight)  // Pillar spike
            .flatPoint(0.75f, pillarHeight * 0.8f)
            .point(0.85f, 0.0f, -5.0f)  // Sharp drop
            .point(1.0f, 0.0f, 0.0f);
    }

    // Tectonic preset: Underground river carving
    public static SplineBuilder undergroundRiver(String depthSource) {
        return new SplineBuilder(depthSource)
            .point(-1.0f, 1.0f, 0.0f)   // Far from river = solid
            .point(-0.1f, 1.0f, 0.0f)
            .sharpPoint(0.0f, -0.5f)     // River channel
            .sharpPoint(0.1f, 1.0f)
            .point(1.0f, 1.0f, 0.0f);
    }

    // Build the JSON representation
    public JsonObject build() {
        JsonObject spline = new JsonObject();

        if (isMultiSpline) {
            // Multi-spline with nested splines
            spline.add("coordinate", coordinate);

            JsonArray pointsArray = new JsonArray();
            for (MultiSplineEntry entry : multiSplineEntries) {
                JsonObject point = new JsonObject();
                point.addProperty("location", entry.location);
                point.addProperty("derivative", entry.derivative);
                point.add("value", entry.nestedSpline.build());
                pointsArray.add(point);
            }
            spline.add("points", pointsArray);
        } else {
            // Simple spline with numeric values
            spline.add("coordinate", coordinate);

            JsonArray pointsArray = new JsonArray();
            for (SplinePoint p : points) {
                JsonObject point = new JsonObject();
                point.addProperty("location", p.location);
                point.addProperty("value", p.value);
                point.addProperty("derivative", p.derivative);
                pointsArray.add(point);
            }
            spline.add("points", pointsArray);
        }

        return spline;
    }

    // Helper class for multi-spline entries
    private static class MultiSplineEntry {
        final float location;
        final float derivative;
        final SplineBuilder nestedSpline;

        MultiSplineEntry(float location, float derivative, SplineBuilder nestedSpline) {
            this.location = location;
            this.derivative = derivative;
            this.nestedSpline = nestedSpline;
        }
    }

    // Factory method for creating complex multi-dimensional splines
    public static SplineBuilder multiDimensional(String primaryCoordinate) {
        return new SplineBuilder(primaryCoordinate);
    }

    // Tectonic preset: Full terrain shaper (combines continents, erosion, ridges)
    public static SplineBuilder terrainShaper(
        String continentCoordinate,
        String erosionCoordinate,
        String ridgeCoordinate
    ) {
        // This creates a 3D spline that varies based on all three inputs
        // Outer coordinate: continentalness
        SplineBuilder shaper = new SplineBuilder(continentCoordinate);

        // Ocean/deep ocean
        shaper.multiSpline(-1.0f, 0.0f,
            new SplineBuilder(erosionCoordinate)
                .point(-1.0f, -0.2f, 0.0f)
                .point(0.0f, -0.15f, 0.0f)
                .point(1.0f, -0.1f, 0.0f)
        );

        // Coast
        shaper.multiSpline(0.0f, 0.0f,
            new SplineBuilder(erosionCoordinate)
                .point(-1.0f, 0.0f, 0.0f)
                .point(0.0f, 0.0f, 0.0f)
                .point(1.0f, 0.05f, 0.0f)
        );

        // Inland
        shaper.multiSpline(0.5f, 0.0f,
            new SplineBuilder(erosionCoordinate)
                .multiSpline(-1.0f, 0.0f,
                    new SplineBuilder(ridgeCoordinate)
                        .point(-1.0f, 0.05f, 0.0f)
                        .point(0.0f, 0.1f, 0.0f)
                        .point(1.0f, 0.3f, 0.0f)  // Mountains
                )
                .multiSpline(0.0f, 0.0f,
                    new SplineBuilder(ridgeCoordinate)
                        .point(-1.0f, 0.05f, 0.0f)
                        .point(0.0f, 0.07f, 0.0f)
                        .point(1.0f, 0.15f, 0.0f)  // Hills
                )
        );

        return shaper;
    }
}