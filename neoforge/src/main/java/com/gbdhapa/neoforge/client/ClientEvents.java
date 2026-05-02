package com.gbdhapa.neoforge.client;

import com.gbdhapa.network.ConfigRequestPayload;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public class ClientEvents {
    public static final KeyMapping OPEN_CONFIG_KEY = new KeyMapping(
            "key.librarian-filter.open_trade_config",
            InputConstants.KEY_O,
            KeyMapping.Category.GAMEPLAY
    );

    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(OPEN_CONFIG_KEY);
    }

    public static void onClientTick(ClientTickEvent.Post event) {
        while (OPEN_CONFIG_KEY.consumeClick()) {
            ClientPacketDistributor.sendToServer(new ConfigRequestPayload());
        }
    }
}
