package com.mekanismoptimizer.test;

import com.mekanismoptimizer.core.AdaptiveBackoffManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AdaptiveBackoffTest {

    private final Object testKey = new Object();

    @BeforeEach
    public void setup() {
        AdaptiveBackoffManager.clear();
    }

    @Test
    public void testInitialExecution() {
        assertFalse(AdaptiveBackoffManager.shouldBackoff(testKey), "Should not back off initially");
    }

    @Test
    public void testBackoffAndRecovery() {
        assertFalse(AdaptiveBackoffManager.shouldBackoff(testKey));

        // First failure: backs off for at least 2 ticks
        AdaptiveBackoffManager.recordFailure(testKey);
        assertTrue(AdaptiveBackoffManager.shouldBackoff(testKey), "Should back off immediately after failure");

        // Advance 1 tick -> still backed off
        AdaptiveBackoffManager.onServerTick();
        assertTrue(AdaptiveBackoffManager.shouldBackoff(testKey), "Should remain backed off on tick +1");

        // Advance 2 ticks -> backoff expires
        AdaptiveBackoffManager.onServerTick();
        AdaptiveBackoffManager.onServerTick();
        assertFalse(AdaptiveBackoffManager.shouldBackoff(testKey), "Should run after backoff duration expires");

        // Record success clears backoff
        AdaptiveBackoffManager.recordFailure(testKey);
        assertTrue(AdaptiveBackoffManager.shouldBackoff(testKey));
        AdaptiveBackoffManager.recordSuccess(testKey);
        assertFalse(AdaptiveBackoffManager.shouldBackoff(testKey), "Should run immediately after recordSuccess");
    }
}