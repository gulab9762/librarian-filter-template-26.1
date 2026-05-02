package com.gbdhapa.quilt.client;

import com.gbdhapa.config.TradeConfig;
import com.gbdhapa.network.ConfigRequestPayload;
import com.gbdhapa.network.OpenConfigScreenPayload;
import com.gbdhapa.network.TradeConfigSyncPayload;
import com.gbdhapa.quilt.client.gui.TradeConfigScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;

/**
 * Quilt client entrypoint. Uses Fabric ClientModInitializer which Quilt supports natively.
 * Registered as 'client_init' in quilt.mod.json.
 */
public class LibrarianFilterQuiltClient implements ClientModInitializer {
    private static KeyMapping OPEN_CONFIG_KEY;

    @Override
    public void onInitializeClient() {
        OPEN_CONFIG_KEY = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.librarian-filter.open_trade_config",
                InputConstants.KEY_O,
                KeyMapping.Category.GAMEPLAY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.level == null) return;
            while (OPEN_CONFIG_KEY.consumeClick()) {
                ClientPlayNetworking.send(new ConfigRequestPayload());
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(TradeConfigSyncPayload.ID, (payload, context) -> {
            TradeConfig.INSTANCE.enableReroll = payload.enableReroll();
            TradeConfig.INSTANCE.enableEachLevelReroll = payload.enableEachLevelReroll();
        });

        ClientPlayNetworking.registerGlobalReceiver(OpenConfigScreenPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                context.client().setScreen(new TradeConfigScreen(payload.enableReroll(), payload.enableEachLevelReroll()));
            });
        });
    }
}
