package ca.fxco.memoryleakfix.mixin.navigatingMobsLeak;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * When entities are removed from a ServerLevel, their EntityCallbacks.onTrackingEnd
 * is called. However, if a Mob with an active PathNavigation is removed, it may
 * remain in ServerLevel's navigatingMobs set. This prevents the Mob (and everything
 * it references) from being garbage collected. By stopping navigation on tracking
 * end, the mob is properly removed from the navigatingMobs set.
 */
@Mixin(targets = "net.minecraft.server.level.ServerLevel$EntityCallbacks")
public abstract class EntityCallbacks_navigatingMobsMixin {

    @Inject(
            method = "onTrackingEnd(Lnet/minecraft/world/entity/Entity;)V",
            at = @At("RETURN")
    )
    private void memoryLeakFix$stopNavigationOnRemoval(Entity entity, CallbackInfo ci) {
        if (entity instanceof Mob mob) {
            if (mob.getNavigation().isInProgress()) {
                mob.getNavigation().stop();
            }
        }
    }
}
