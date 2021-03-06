package com.falsepattern.triangulator.mixin.mixins.client.vanilla;

import com.falsepattern.triangulator.mixin.helper.IQuadComparatorMixin;
import lombok.val;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.util.QuadComparator;

@Mixin(QuadComparator.class)
public abstract class QuadComparatorMixin implements IQuadComparatorMixin {
    @Shadow
    private float field_147630_a;

    @Shadow
    private float field_147628_b;

    @Shadow
    private float field_147629_c;

    @Shadow
    private int[] field_147627_d;

    private boolean triMode;

    private boolean shaderMode;

    private static float getCenter(float x, float y, float z, int[] vertexData, int i, int vertexSize) {
        val ax = Float.intBitsToFloat(vertexData[i]) - x;
        val ay = Float.intBitsToFloat(vertexData[i + 1]) - y;
        val az = Float.intBitsToFloat(vertexData[i + 2]) - z;
        val bx = Float.intBitsToFloat(vertexData[i + vertexSize]) - x;
        val by = Float.intBitsToFloat(vertexData[i + vertexSize + 1]) - y;
        val bz = Float.intBitsToFloat(vertexData[i + vertexSize + 2]) - z;
        val cx = Float.intBitsToFloat(vertexData[i + vertexSize * 2]) - x;
        val cy = Float.intBitsToFloat(vertexData[i + vertexSize * 2 + 1]) - y;
        val cz = Float.intBitsToFloat(vertexData[i + vertexSize * 2 + 2]) - z;

        val xAvg = (ax + bx + cx) / 3f;
        val yAvg = (ay + by + cy) / 3f;
        val zAvg = (az + bz + cz) / 3f;

        return xAvg * xAvg + yAvg * yAvg + zAvg * zAvg;
    }

    private static int compare(int a, int b, float x, float y, float z, int[] vertexData, int vertexSize) {
        return Float.compare(getCenter(x, y, z, vertexData, b, vertexSize),
                             getCenter(x, y, z, vertexData, a, vertexSize));
    }

    @Inject(method = "compare(Ljava/lang/Integer;Ljava/lang/Integer;)I",
            at = @At(value = "HEAD"),
            cancellable = true,
            require = 1)
    private void triCompare(Integer aObj, Integer bObj, CallbackInfoReturnable<Integer> cir) {
        if (!triMode) {
            return;
        }
        cir.setReturnValue(
                compare(aObj, bObj, this.field_147630_a, this.field_147628_b, this.field_147629_c, this.field_147627_d,
                        shaderMode ? 18 : 8));
    }

    @Override
    public void enableTriMode() {
        triMode = true;
    }

    @Override
    public void enableShaderMode() {
        shaderMode = true;
    }
}
