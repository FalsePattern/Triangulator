package com.falsepattern.triangulator.mixin.mixins.client;

import com.falsepattern.triangulator.mixin.helper.IQuadComparatorMixin;
import com.falsepattern.triangulator.mixin.helper.ITessellatorMixin;
import net.minecraft.client.renderer.Tessellator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

import java.util.Comparator;

@Mixin(Tessellator.class)
public abstract class TessellatorFoamFixMixin implements ITessellatorMixin {
    @ModifyArg(method = {"getVertexState_foamfix_old"},
               at = @At(value = "INVOKE",
                       target = "Ljava/util/PriorityQueue;<init>(ILjava/util/Comparator;)V",
                       remap = false),
               index = 1,
               remap = false,
               require = 1)
    private Comparator<?> hackQuadComparator(Comparator<?> comparator) {
        if (drawingTris()) {
            ((IQuadComparatorMixin)comparator).enableTriMode();
        }
        return comparator;
    }


    @ModifyConstant(method = "getVertexState_foamfix_old",
                    constant = @Constant(intValue = 32),
                    require = 1)
    private int hackQuadCounting(int constant) {
        return constant - 8;
    }
}