package com.gbdhapa.fabric;

import com.gbdhapa.RerollHelper;
import com.gbdhapa.config.TradeConfig;
import com.gbdhapa.network.ConfigRequestPayload;
import com.gbdhapa.network.OpenConfigScreenPayload;
import com.gbdhapa.network.TradeConfigSyncPayload;
import com.gbdhapa.network.TradeConfigUpdatePayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LibrarianfilterFabric implements ModInitializer {
	public static final String MOD_ID = "librarian-filter";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		TradeConfig.setConfigFile(net.fabricmc.loader.api.FabricLoader.getInstance().getConfigDir().resolve("trade_config.json"));
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
				
				// Sync to all clients
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

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(Commands.literal("reroll")
				.requires(source -> {
					try {
						return source.getServer().getPlayerList().isOp(new NameAndId(source.getPlayerOrException().getGameProfile()));
					} catch (Exception e) {
						return false;
					}
				})
				.then(Commands.literal("config")
					.executes(context -> {
						ServerPlayer player = context.getSource().getPlayerOrException();
						ServerPlayNetworking.send(player, new OpenConfigScreenPayload(
								TradeConfig.INSTANCE.enableReroll,
								TradeConfig.INSTANCE.enableEachLevelReroll
						));
						return 1;
					})
				)
			);
		});

		new RerollHelperFabric();
	}
}
