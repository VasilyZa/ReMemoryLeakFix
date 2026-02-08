package ca.fxco.memoryleakfix.mixin.distanceManagerLeak;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.server.level.DistanceManager;
import net.minecraft.server.level.Ticket;
import net.minecraft.util.SortedArraySet;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * After purgeStaleTickets() removes expired tickets from the tickets map,
 * it can leave behind empty SortedArraySet entries keyed by chunk position.
 * Over time on busy servers, these empty sets accumulate and waste memory.
 * This mixin removes empty entries after purging to prevent unbounded growth.
 */
@Mixin(DistanceManager.class)
public abstract class DistanceManager_ticketCleanupMixin {

    @Shadow
    @Final
    private Long2ObjectOpenHashMap<SortedArraySet<Ticket<?>>> tickets;

    @Inject(
            method = "purgeStaleTickets",
            at = @At("RETURN")
    )
    private void memoryLeakFix$removeEmptyTicketSets(CallbackInfo ci) {
        this.tickets.long2ObjectEntrySet().removeIf(
                entry -> entry.getValue().isEmpty()
        );
    }
}
