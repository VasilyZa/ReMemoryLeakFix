package ca.fxco.memoryleakfix.mixin.entityTrackerLeak;

import net.minecraft.server.network.ServerPlayerConnection;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

/**
 * When an entity is removed and broadcastRemoved() is called on its TrackedEntity,
 * the seenBy set retains references to ServerPlayerConnection objects. These stale
 * references prevent GC of the connection and its associated player data. This is
 * especially problematic during entity spawning bursts where many entities are
 * created and destroyed rapidly.
 */
@Mixin(targets = "net.minecraft.server.level.ChunkMap$TrackedEntity")
public abstract class TrackedEntity_seenByCleanupMixin {

    @Shadow
    @Final
    private Set<ServerPlayerConnection> seenBy;

    @Inject(
            method = "broadcastRemoved",
            at = @At("RETURN")
    )
    private void memoryLeakFix$clearSeenByOnRemoval(CallbackInfo ci) {
        this.seenBy.clear();
    }
}
