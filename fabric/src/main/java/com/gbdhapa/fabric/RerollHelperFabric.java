package com.gbdhapa.fabric;

import com.gbdhapa.RerollHelper;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.world.InteractionResult;

public class RerollHelperFabric {
    public RerollHelperFabric() {
        registerEvent();
    }

    private void registerEvent() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            RerollHelper.performReroll(player, world, hitResult.getBlockPos());
            return InteractionResult.PASS;
        });
    }
}
