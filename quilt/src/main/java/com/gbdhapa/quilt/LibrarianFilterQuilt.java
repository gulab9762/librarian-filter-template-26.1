package com.gbdhapa.quilt;

import com.gbdhapa.config.TradeConfig;
import com.gbdhapa.network.ConfigRequestPayload;
import com.gbdhapa.network.OpenConfigScreenPayload;
import com.gbdhapa.network.TradeConfigSyncPayload;
import com.gbdhapa.network.TradeConfigUpdatePayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.world.InteractionResult;

/**
 * Quilt entrypoint. Quilt supports Fabric's ModInitializer via the Fabric API compatibility layer.
 * This class is registered as 'init' in quilt.mod.json.
 */
public class LibrarianFilterQuilt implements ModInitializer {
    public static final String MOD_ID = "librarian-filter";

    @Override
    public void onInitialize() {
        // Quilt Loader exposes Fabric Loader API; FabricLoader.getInstance() works on Quilt
        TradeConfig.setConfigFile(FabricLoader.getInstance().getConfigDir().resolve("trade_config.json"));
        TradeConfig.load();

        PayloadTypeRegistry.clientboundPlay().register(TradeConfigSyncPayload.ID, TradeConfigSyncPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(OpenConfigScreenPayload.ID, OpenConfigScreenPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(TradeConfigUpdatePayload.ID, TradeConfigUpdatePayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ConfigRequestPayload.ID, ConfigRequestPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(TradeConfigUpdatePayload.ID, (payload, context) -> {
            if (context.player().level().getServer().getPlayerList().isOp(new NameAndId(context.player().getGameProfile()))) {
                TradeConfig.INSTANCE.enableReroll = payload.enableReroll();
                TradeConfig.INSTANCE.enableEachLevelReroll = payload.enableEachLevelReroll();
                TradeConfig.save();

                TradeConfigSyncPayload syncPayload = new TradeConfigSyncPayload(payload.enableReroll(), payload.enableEachLevelReroll());
                for (ServerPlayer player : context.player().level().getServer().getPlayerList().getPlayers()) {
                    ServerPlayNetworking.send(player, syncPayload);
                }
            }
        });

        ServerPlayNetworking.registerGlobalReceiver(ConfigRequestPayload.ID, (payload, context) -> {
            if (context.player().level().getServer().getPlayerList().isOp(new NameAndId(context.player().getGameProfile()))) {
                ServerPlayNetworking.send(context.player(), new OpenConfigScreenPayload(
                    TradeConfig.INSTANCE.enableReroll,
                    TradeConfig.INSTANCE.enableEachLevelReroll
                ));
            }
        });

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            com.gbdhapa.RerollHelper.performReroll(player, world, hitResult.getBlockPos());
            return InteractionResult.PASS;
        });
    }
}
