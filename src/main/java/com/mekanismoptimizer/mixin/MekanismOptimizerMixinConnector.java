package com.mekanismoptimizer.mixin;

import org.spongepowered.asm.mixin.connect.IMixinConnector;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

/**
 * Early Mixin Connector executed before any IMixinConfigPlugin (such as FantasyEndingMixinPlugin).
 * Prevents self-attach IOException ("Can not attach to current VM") caused by mods like FantasyEnding
 * writing Boolean.TRUE via Unsafe.putObject into the 1-byte boolean field HotSpotVirtualMachine.ALLOW_ATTACH_SELF
 * under 32GB+ heap configurations (-Xmx32G).
 */
public class MekanismOptimizerMixinConnector implements IMixinConnector {

    @Override
    public void connect() {
        try {
            Field uf = Unsafe.class.getDeclaredField("theUnsafe");
            uf.setAccessible(true);
            Unsafe unsafe = (Unsafe) uf.get(null);

            Class<?> vmClass = Class.forName("sun.tools.attach.HotSpotVirtualMachine");
            Field pidField = vmClass.getDeclaredField("CURRENT_PID");
            Field allowField = vmClass.getDeclaredField("ALLOW_ATTACH_SELF");

            Object pidBase = unsafe.staticFieldBase(pidField);
            long pidOffset = unsafe.staticFieldOffset(pidField);
            unsafe.putLong(pidBase, pidOffset, -1L);

            Object allowBase = unsafe.staticFieldBase(allowField);
            long allowOffset = unsafe.staticFieldOffset(allowField);
            unsafe.putBoolean(allowBase, allowOffset, true);
        } catch (Throwable ignored) {
        }
    }
}
