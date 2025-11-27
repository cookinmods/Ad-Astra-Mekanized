package com.hecookin.adastramekanized.worldgen.config;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration for surface rules on planets.
 * Based on WORLDGEN_SYSTEM_DESIGN.md specification.
 */
public class SurfaceRuleConfig {
    private List<SurfaceRuleEntry> rules;

    public SurfaceRuleConfig() {
        this.rules = new ArrayList<>();
    }

    public SurfaceRuleConfig addRule(String biomeName, String condition, String block) {
        rules.add(new SurfaceRuleEntry(biomeName, condition, block));
        return this;
    }

    // Getter
    public List<SurfaceRuleEntry> getRules() {
        return rules;
    }

    /**
     * Entry for a surface rule
     */
    public static class SurfaceRuleEntry {
        private String biomeName;
        private String condition;  // "floor", "ceiling", "underwater", etc.
        private String block;

        public SurfaceRuleEntry(String biomeName, String condition, String block) {
            this.biomeName = biomeName;
            this.condition = condition;
            this.block = block;
        }

        // Getters
        public String getBiomeName() { return biomeName; }
        public String getCondition() { return condition; }
        public String getBlock() { return block; }
    }
}
