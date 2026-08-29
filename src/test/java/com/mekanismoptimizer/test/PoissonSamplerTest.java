package com.mekanismoptimizer.test;

import com.mekanismoptimizer.core.PoissonSampler;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PoissonSamplerTest {

    @Test
    public void testSamplingRange() {
        for (double mean = 0.5; mean <= 10.0; mean += 0.5) {
            for (int i = 0; i < 100; i++) {
                int sample = PoissonSampler.sampleFast(mean);
                assertTrue(sample >= 0, "Sample must be non-negative");
            }
        }
    }

    @Test
    public void testZeroMean() {
        assertEquals(0, PoissonSampler.sampleFast(0.0), "Mean 0 must return 0");
    }
}