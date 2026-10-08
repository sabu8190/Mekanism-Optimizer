package com.mekanismoptimizer.test;

import com.mekanismoptimizer.core.FastEnergyCubeRenderPool;
import com.mekanismoptimizer.core.FastTransmitterRenderCache;
import mekanism.api.tier.BaseTier;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FastTransmitterRenderCacheTest {

    @Test
    public void testNullTransmitterSafe() {
        List<String> sides = FastTransmitterRenderCache.getVisibleSides(null);
        assertNotNull(sides);
        assertTrue(sides.isEmpty());
    }

    @Test
    public void testEnergyCubeRenderPool() {
        Vec3 pos = new Vec3(10, 64, 10);
        var render1 = FastEnergyCubeRenderPool.getOrCreate(pos, null, 0.5f, 0, null);
        assertNotNull(render1);
        assertEquals(pos, render1.getCenterPos(0.0f));

        if (render1 instanceof FastEnergyCubeRenderPool.PooledLazyRender pooled) {
            FastEnergyCubeRenderPool.release(pooled);
            var render2 = FastEnergyCubeRenderPool.getOrCreate(pos, null, 0.8f, 0, null);
            assertSame(render1, render2, "PooledLazyRender should be recycled from pool");
        }
    }
}
