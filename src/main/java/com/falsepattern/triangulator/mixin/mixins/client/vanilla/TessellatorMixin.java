package com.falsepattern.triangulator.mixin.mixins.client.vanilla;

import com.falsepattern.triangulator.Triangulator;
import com.falsepattern.triangulator.api.ToggleableTessellator;
import com.falsepattern.triangulator.mixin.helper.IQuadComparatorMixin;
import com.falsepattern.triangulator.mixin.helper.ITessellatorMixin;
import net.minecraft.client.renderer.Tessellator;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Comparator;

@Mixin(Tessellator.class)
public abstract class TessellatorMixin implements ITessellatorMixin, ToggleableTessellator {
    @Shadow
    private int drawMode;

    @Shadow private int[] rawBuffer;
    @Shadow private int rawBufferIndex;
    @Shadow private int vertexCount;
    private boolean hackedQuadRendering = false;
    private boolean drawingTris = false;
    private boolean alternativeTriangulation = false;
    private boolean quadTriangulationTemporarilySuspended = false;
    private boolean shaderOn = false;
    private int quadVerticesPutIntoBuffer = 0;

    @Inject(method = "reset",
            at = @At(value = "HEAD"),
            require = 1)
    private void resetState(CallbackInfo ci) {
        drawingTris = false;
        hackedQuadRendering = false;
        quadTriangulationTemporarilySuspended = false;
        alternativeTriangulation = false;
        quadVerticesPutIntoBuffer = 0;
    }

    @Redirect(method = "startDrawing",
              at = @At(value = "FIELD",
                       opcode = Opcodes.PUTFIELD,
                       target = "Lnet/minecraft/client/renderer/Tessellator;drawMode:I"),
              require = 1)
    private void forceDrawingTris(Tessellator instance, int value) {
        if (value == GL11.GL_QUADS) {
            hackedQuadRendering = true;
            value = GL11.GL_TRIANGLES;
        } else {
            hackedQuadRendering = false;
        }
        if (value == GL11.GL_TRIANGLES) {
            drawingTris = true;
        }
        drawMode = value;
    }

    @Override
    public boolean hackedQuadRendering() {
        return hackedQuadRendering;
    }

    @Override
    public boolean quadTriangulationActive() {
        return !quadTriangulationTemporarilySuspended;
    }

    @Override
    public void setAlternativeTriangulation() {
        alternativeTriangulation = true;
    }

    @Override
    public void suspendQuadTriangulation() {
        quadTriangulationTemporarilySuspended = true;
        if (quadVerticesPutIntoBuffer != 0) {
            Triangulator.triLog.error(new RuntimeException("Someone suspended triangulation while the tessellator had a partially rendered quad! Stacktrace: "));
            quadVerticesPutIntoBuffer = 0;
        }
    }

    @Override
    public void triangulate() {
        if (!hackedQuadRendering || quadTriangulationTemporarilySuspended) return;
        quadVerticesPutIntoBuffer++;
        if (quadVerticesPutIntoBuffer == 4) {
            int vertexSize = shaderOn() ? 18 : 8;
            quadVerticesPutIntoBuffer = 0;
            //Current vertex layout: ABCD
            if (alternativeTriangulation) {
                //Target vertex layout: ABD DBC
                System.arraycopy(rawBuffer, rawBufferIndex - (3 * vertexSize), rawBuffer, rawBufferIndex, 2 * vertexSize);
                System.arraycopy(rawBuffer, rawBufferIndex - vertexSize, rawBuffer, rawBufferIndex - (2 * vertexSize), vertexSize);
                alternativeTriangulation = false;
            } else {
                //Target vertex layout: ABC DAC
                System.arraycopy(rawBuffer, rawBufferIndex - (4 * vertexSize), rawBuffer, rawBufferIndex, vertexSize);
                System.arraycopy(rawBuffer, rawBufferIndex - (2 * vertexSize), rawBuffer, rawBufferIndex + vertexSize, vertexSize);
            }
            vertexCount += 2;
            rawBufferIndex += 2 * vertexSize;
        }
    }

    @Override
    public void resumeQuadTriangulation() {
        quadTriangulationTemporarilySuspended = false;
    }

    @Override
    public boolean shaderOn() {
        return shaderOn;
    }

    @Override
    public void shaderOn(boolean state) {
        shaderOn = state;
    }

    @Override
    public Comparator<?> hackQuadComparator(Comparator<?> comparator) {
        if (drawingTris) {
            IQuadComparatorMixin comp = (IQuadComparatorMixin) comparator;
            comp.enableTriMode();
            if (shaderOn)
                comp.enableShaderMode();
        }
        return comparator;
    }

    @Override
    public int hackQuadCounting(int constant) {
        return (constant / 4) * 3;
    }
}