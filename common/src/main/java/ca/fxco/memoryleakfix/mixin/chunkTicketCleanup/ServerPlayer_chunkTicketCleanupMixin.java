package ca.fxco.memoryleakfix.mixin.chunkTicketCleanup;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayer_chunkTicketCleanupMixin {

    @Unique
    private ChunkPos memoryLeakFix$oldChunkPos;

    @Unique
    private ServerLevel memoryLeakFix$oldLevel;

    @Inject(
            method = "teleportTo(Lnet/minecraft/server/level/ServerLevel;DDDFF)V",
            at = @At("HEAD")
    )
    private void memoryLeakFix$captureOldPosition(ServerLevel level, double x, double y, double z, float yRot, float xRot, CallbackInfo ci) {
        ServerPlayer self = (ServerPlayer) (Object) this;
        this.memoryLeakFix$oldChunkPos = self.chunkPosition();
        this.memoryLeakFix$oldLevel = self.serverLevel();
    }

    @Inject(
            method = "teleportTo(Lnet/minecraft/server/level/ServerLevel;DDDFF)V",
            at = @At("RETURN")
    )
    private void memoryLeakFix$cleanupOldChunkTicket(ServerLevel level, double x, double y, double z, float yRot, float xRot, CallbackInfo ci) {
        if (this.memoryLeakFix$oldChunkPos == null || this.memoryLeakFix$oldLevel == null) {
            return;
        }

        ServerPlayer self = (ServerPlayer) (Object) this;
        ServerLevel currentLevel = self.serverLevel();

        // Cross-dimension teleport: always remove old ticket since the player left that level entirely
        if (this.memoryLeakFix$oldLevel != currentLevel) {
            this.memoryLeakFix$oldLevel.getChunkSource().removeRegionTicket(
                    TicketType.PLAYER,
                    this.memoryLeakFix$oldChunkPos,
                    memoryLeakFix$PLAYER_TICKET_LEVEL,
                    this.memoryLeakFix$oldChunkPos
            );
        } else {
            // Same dimension: only remove if teleported beyond view distance
            ChunkPos newChunkPos = self.chunkPosition();
            int chunkDistance = Math.max(
                    Math.abs(newChunkPos.x - this.memoryLeakFix$oldChunkPos.x),
                    Math.abs(newChunkPos.z - this.memoryLeakFix$oldChunkPos.z)
            );
            int viewDistance = this.memoryLeakFix$oldLevel.getServer().getPlayerList().getViewDistance();

            if (chunkDistance > viewDistance) {
                this.memoryLeakFix$oldLevel.getChunkSource().removeRegionTicket(
                        TicketType.PLAYER,
                        this.memoryLeakFix$oldChunkPos,
                        memoryLeakFix$PLAYER_TICKET_LEVEL,
                        this.memoryLeakFix$oldChunkPos
                );
            }
        }

        this.memoryLeakFix$oldChunkPos = null;
        this.memoryLeakFix$oldLevel = null;
    }

    // PLAYER ticket level is always 33 in vanilla (ChunkLevel.byStatus(FullChunkStatus.ENTITY_TICKING))
    @Unique
    private static final int memoryLeakFix$PLAYER_TICKET_LEVEL = 33;
}
