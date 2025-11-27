package com.hecookin.adastramekanized.worldgen.builder;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.*;

/**
 * Fluent builder for creating complex density function JSON structures.
 * Based on Tectonic's density function patterns.
 * Provides a type-safe way to construct worldgen JSON programmatically.
 */
public class DensityFunctionBuilder {
    private final String name;
    private String type = "minecraft:constant";
    private final Map<String, JsonElement> properties = new LinkedHashMap<>();
    private JsonElement cachedBuild = null;

    public DensityFunctionBuilder(String name) {
        this.name = name;
    }

    // Static factory methods for common patterns

    public static DensityFunctionBuilder constant(double value) {
        return new DensityFunctionBuilder("constant")
            .type("minecraft:constant")
            .property("argument", value);
    }

    public static DensityFunctionBuilder reference(String id) {
        // Reference to another density function
        return new DensityFunctionBuilder("reference")
            .rawValue(new JsonPrimitive(id));
    }

    public static DensityFunctionBuilder noise(String noiseId, double xzScale, double yScale) {
        return new DensityFunctionBuilder("noise")
            .type("minecraft:noise")
            .property("noise", noiseId)
            .property("xz_scale", xzScale)
            .property("y_scale", yScale);
    }

    // Type setters

    public DensityFunctionBuilder type(String type) {
        this.type = type;
        this.cachedBuild = null;
        return this;
    }

    // Property setters

    public DensityFunctionBuilder property(String key, JsonElement value) {
        this.properties.put(key, value);
        this.cachedBuild = null;
        return this;
    }

    public DensityFunctionBuilder property(String key, String value) {
        return property(key, new JsonPrimitive(value));
    }

    public DensityFunctionBuilder property(String key, Number value) {
        return property(key, new JsonPrimitive(value));
    }

    public DensityFunctionBuilder property(String key, boolean value) {
        return property(key, new JsonPrimitive(value));
    }

    public DensityFunctionBuilder property(String key, DensityFunctionBuilder builder) {
        return property(key, builder.build());
    }

    // Binary operations

    public DensityFunctionBuilder add(DensityFunctionBuilder other) {
        return new DensityFunctionBuilder(name + "_add")
            .type("minecraft:add")
            .property("argument1", this.build())
            .property("argument2", other.build());
    }

    public DensityFunctionBuilder mul(DensityFunctionBuilder other) {
        return new DensityFunctionBuilder(name + "_mul")
            .type("minecraft:mul")
            .property("argument1", this.build())
            .property("argument2", other.build());
    }

    public DensityFunctionBuilder mul(double scalar) {
        return new DensityFunctionBuilder(name + "_mul")
            .type("minecraft:mul")
            .property("argument1", scalar)
            .property("argument2", this.build());
    }

    public DensityFunctionBuilder min(DensityFunctionBuilder other) {
        return new DensityFunctionBuilder(name + "_min")
            .type("minecraft:min")
            .property("argument1", this.build())
            .property("argument2", other.build());
    }

    public DensityFunctionBuilder max(DensityFunctionBuilder other) {
        return new DensityFunctionBuilder(name + "_max")
            .type("minecraft:max")
            .property("argument1", this.build())
            .property("argument2", other.build());
    }

    // Unary operations

    public DensityFunctionBuilder abs() {
        return new DensityFunctionBuilder(name + "_abs")
            .type("minecraft:abs")
            .property("argument", this.build());
    }

    public DensityFunctionBuilder square() {
        return new DensityFunctionBuilder(name + "_square")
            .type("minecraft:square")
            .property("argument", this.build());
    }

    public DensityFunctionBuilder cube() {
        return new DensityFunctionBuilder(name + "_cube")
            .type("minecraft:cube")
            .property("argument", this.build());
    }

    public DensityFunctionBuilder squeeze() {
        return new DensityFunctionBuilder(name + "_squeeze")
            .type("minecraft:squeeze")
            .property("argument", this.build());
    }

    public DensityFunctionBuilder clamp(double min, double max) {
        return new DensityFunctionBuilder(name + "_clamp")
            .type("minecraft:clamp")
            .property("input", this.build())
            .property("min", min)
            .property("max", max);
    }

    // Caching operations (critical for performance)

    public DensityFunctionBuilder cache() {
        return new DensityFunctionBuilder(name + "_cached")
            .type("minecraft:cache_once")
            .property("argument", this.build());
    }

    public DensityFunctionBuilder flatCache() {
        return new DensityFunctionBuilder(name + "_flat_cached")
            .type("minecraft:flat_cache")
            .property("argument", this.build());
    }

    public DensityFunctionBuilder cache2d() {
        return new DensityFunctionBuilder(name + "_2d_cached")
            .type("minecraft:cache_2d")
            .property("argument", this.build());
    }

    public DensityFunctionBuilder cacheAllInCell() {
        return new DensityFunctionBuilder(name + "_cell_cached")
            .type("minecraft:cache_all_in_cell")
            .property("argument", this.build());
    }

    // Interpolation

    public DensityFunctionBuilder interpolated() {
        return new DensityFunctionBuilder(name + "_interpolated")
            .type("minecraft:interpolated")
            .property("argument", this.build());
    }

    // Blend operations (for smooth transitions)

    public DensityFunctionBuilder blendAlpha() {
        return new DensityFunctionBuilder(name + "_blend_alpha")
            .type("minecraft:blend_alpha");
    }

    public DensityFunctionBuilder blendOffset() {
        return new DensityFunctionBuilder(name + "_blend_offset")
            .type("minecraft:blend_offset");
    }

    public DensityFunctionBuilder blendDensity(DensityFunctionBuilder input) {
        return new DensityFunctionBuilder(name + "_blend_density")
            .type("minecraft:blend_density")
            .property("argument", input.build());
    }

    // Spline operations

    public DensityFunctionBuilder spline(SplineBuilder splineBuilder) {
        return new DensityFunctionBuilder(name + "_spline")
            .type("minecraft:spline")
            .property("spline", splineBuilder.build());
    }

    public DensityFunctionBuilder spline(String coordinate, SplinePoint... points) {
        SplineBuilder splineBuilder = new SplineBuilder(coordinate);
        for (SplinePoint point : points) {
            splineBuilder.addPoint(point);
        }
        return spline(splineBuilder);
    }

    // Conditional operations

    public DensityFunctionBuilder conditional(DensityFunctionBuilder condition,
                                             DensityFunctionBuilder ifTrue,
                                             DensityFunctionBuilder ifFalse) {
        return new DensityFunctionBuilder(name + "_conditional")
            .type("minecraft:range_choice")
            .property("input", condition.build())
            .property("min_inclusive", -1.0)
            .property("max_exclusive", 1.0)
            .property("when_in_range", ifTrue.build())
            .property("when_out_of_range", ifFalse.build());
    }

    // Gradient operations

    public DensityFunctionBuilder yClampedGradient(int fromY, int toY, double fromValue, double toValue) {
        return new DensityFunctionBuilder(name + "_y_gradient")
            .type("minecraft:y_clamped_gradient")
            .property("from_y", fromY)
            .property("to_y", toY)
            .property("from_value", fromValue)
            .property("to_value", toValue);
    }

    // Shifted noise (for offset patterns)

    public DensityFunctionBuilder shiftedNoise(String noiseId,
                                               DensityFunctionBuilder shiftX,
                                               DensityFunctionBuilder shiftY,
                                               DensityFunctionBuilder shiftZ,
                                               double scale) {
        return new DensityFunctionBuilder(name + "_shifted_noise")
            .type("minecraft:shifted_noise")
            .property("noise", noiseId)
            .property("shift_x", shiftX.build())
            .property("shift_y", shiftY.build())
            .property("shift_z", shiftZ.build())
            .property("xz_scale", scale)
            .property("y_scale", 0.0);
    }

    // For raw JSON values (references)

    private DensityFunctionBuilder rawValue(JsonElement element) {
        this.cachedBuild = element;
        return this;
    }

    // Build the final JSON

    public JsonElement build() {
        if (cachedBuild != null) {
            return cachedBuild;
        }

        // Handle simple reference case
        if (type.equals("reference") && properties.isEmpty()) {
            return new JsonPrimitive(name);
        }

        JsonObject json = new JsonObject();
        json.addProperty("type", type);

        for (Map.Entry<String, JsonElement> entry : properties.entrySet()) {
            json.add(entry.getKey(), entry.getValue());
        }

        return json;
    }

    // Helper class for spline points

    public static class SplinePoint {
        public final float location;
        public final float value;
        public final float derivative;

        public SplinePoint(float location, float value, float derivative) {
            this.location = location;
            this.value = value;
            this.derivative = derivative;
        }

        public static SplinePoint point(float location, float value, float derivative) {
            return new SplinePoint(location, value, derivative);
        }

        public static SplinePoint flat(float location, float value) {
            return new SplinePoint(location, value, 0.0f);
        }

        public static SplinePoint sharp(float location, float value) {
            return new SplinePoint(location, value, 10.0f);
        }
    }
}