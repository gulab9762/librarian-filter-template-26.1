package com.gbdhapa.neoforge;

import com.gbdhapa.RerollHelper;
import com.gbdhapa.config.TradeConfig;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@Mod("librarian_filter")
public class LibrarianFilterNeoForge {
    public LibrarianFilterNeoForge(IEventBus modEventBus) {
        TradeConfig.setConfigFile(FMLPaths.CONFIGDIR.get().resolve("trade_config.json"));
        TradeConfig.load();

        modEventBus.addListener(this::setup);
        modEventBus.addListener(this::registerPayloads);
        
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            modEventBus.addListener(com.gbdhapa.neoforge.client.ClientEvents::registerKeys);
            NeoForge.EVENT_BUS.addListener(com.gbdhapa.neoforge.client.ClientEvents::onClientTick);
        }

        NeoForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("librarian_filter");
        
        registrar.playToClient(com.gbdhapa.network.TradeConfigSyncPayload.ID, com.gbdhapa.network.TradeConfigSyncPayload.CODEC, (payload, context) -> {
            TradeConfig.INSTANCE.enableReroll = payload.enableReroll();
            TradeConfig.INSTANCE.enableEachLevelReroll = payload.enableEachLevelReroll();
        });
        
        registrar.playToClient(com.gbdhapa.network.OpenConfigScreenPayload.ID, com.gbdhapa.network.OpenConfigScreenPayload.CODEC, (payload, context) -> {
            context.enqueueWork(() -> {
                net.minecraft.client.Minecraft.getInstance().setScreen(new com.gbdhapa.neoforge.client.TradeConfigScreenNeoForge(payload.enableReroll(), payload.enableEachLevelReroll()));
            });
        });
        
        registrar.playToServer(com.gbdhapa.network.TradeConfigUpdatePayload.ID, com.gbdhapa.network.TradeConfigUpdatePayload.CODEC, (payload, context) -> {
            Player player = context.player();
            if (player instanceof ServerPlayer serverPlayer) {
                // FIXME: Permission check for 26.1
                boolean isOp = true; 
                if (isOp) {
                    TradeConfig.INSTANCE.enableReroll = payload.enableReroll();
                    TradeConfig.INSTANCE.enableEachLevelReroll = payload.enableEachLevelReroll();
                    TradeConfig.save();
                    
                    var syncPayload = new com.gbdhapa.network.TradeConfigSyncPayload(payload.enableReroll(), payload.enableEachLevelReroll());
                    var server = serverPlayer.level().getServer();
                    if (server != null) {
                        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
                            PacketDistributor.sendToPlayer(p, syncPayload);
                        }
                    }
                }
            }
        });
        
        registrar.playToServer(com.gbdhapa.network.ConfigRequestPayload.ID, com.gbdhapa.network.ConfigRequestPayload.CODEC, (payload, context) -> {
            Player player = context.player();
            if (player instanceof ServerPlayer serverPlayer) {
                // FIXME: Permission check for 26.1
                boolean isOp = true;
                if (isOp) {
                    PacketDistributor.sendToPlayer(serverPlayer, new com.gbdhapa.network.OpenConfigScreenPayload(
                        TradeConfig.INSTANCE.enableReroll,
                        TradeConfig.INSTANCE.enableEachLevelReroll
                    ));
                }
            }
        });
    }

    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        Level world = event.getLevel();
        RerollHelper.performReroll(player, world, event.getPos());
    }
}
