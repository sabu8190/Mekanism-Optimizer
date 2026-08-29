package com.mekanismoptimizer.core;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public final class PoissonSampler {
    private static final Map<Double, double[]> CACHE = new ConcurrentHashMap<>();

    private PoissonSampler() {
    }

    public static int sampleFast(double mean) {
        if (mean <= 0) {
            return 0;
        }

        MekanismOptimizerLogger.recordPoissonSample();

        if (mean < 30.0) {
            double[] cumulative = CACHE.computeIfAbsent(mean, PoissonSampler::computeCumulativeDistribution);
            double random = ThreadLocalRandom.current().nextDouble();
            int index = Arrays.binarySearch(cumulative, random);
            if (index < 0) {
                index = -index - 1;
            }
            return Math.min(index, cumulative.length - 1);
        } else {
            double stdDev = Math.sqrt(mean);
            double sample = mean + stdDev * ThreadLocalRandom.current().nextGaussian();
            return (int) Math.max(0, Math.round(sample));
        }
    }

    private static double[] computeCumulativeDistribution(double mean) {
        int maxK = Math.max(10, (int) Math.ceil(mean + 5.0 * Math.sqrt(mean)));
        double[] cumulative = new double[maxK + 1];

        double p = Math.exp(-mean);
        double sum = p;
        cumulative[0] = p;

        for (int k = 1; k <= maxK; k++) {
            p *= mean / k;
            sum += p;
            cumulative[k] = sum;
            if (sum >= 0.999999) {
                return Arrays.copyOf(cumulative, k + 1);
            }
        }
        return cumulative;
    }
}