package com.mekanismoptimizer.test;

import com.mekanismoptimizer.core.AdaptiveBackoffManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AdaptiveBackoffTest {

    private final Object testKey = new Object();

    @BeforeEach
    public void setup() {
        AdaptiveBackoffManager.recordSuccess(testKey);
    }

    @Test
    public void testInitialExecution() {
        assertTrue(AdaptiveBackoffManager.shouldRun(testKey, 100L), "Should run initially on tick 100");
    }

    @Test
    public void testBackoffAndWakeUp() {
        long currentTick = 100L;
        assertTrue(AdaptiveBackoffManager.shouldRun(testKey, currentTick));

        // First failure: backoff = 2 ticks -> next tick = 102
        AdaptiveBackoffManager.recordFailure(testKey, currentTick);
        assertFalse(AdaptiveBackoffManager.shouldRun(testKey, 101L), "Should not run on tick 101");
        assertTrue(AdaptiveBackoffManager.shouldRun(testKey, 102L), "Should run on tick 102");

        // Second failure at 102: backoff = 4 ticks -> next tick = 106
        AdaptiveBackoffManager.recordFailure(testKey, 102L);
        assertFalse(AdaptiveBackoffManager.shouldRun(testKey, 103L));
        assertFalse(AdaptiveBackoffManager.shouldRun(testKey, 105L));
        assertTrue(AdaptiveBackoffManager.shouldRun(testKey, 106L));

        // WakeUp resets backoff immediately
        AdaptiveBackoffManager.recordFailure(testKey, 106L);
        assertFalse(AdaptiveBackoffManager.shouldRun(testKey, 107L));
        AdaptiveBackoffManager.wakeUp(testKey);
        assertTrue(AdaptiveBackoffManager.shouldRun(testKey, 107L), "Should run immediately after wake up");
    }
}