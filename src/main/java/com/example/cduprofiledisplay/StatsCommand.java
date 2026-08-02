package com.example.cduprofiledisplay;

import net.minecraft.client.Minecraft;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.stream.Collectors;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

@Mod.EventBusSubscriber(modid = "cduprofiledisplay", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class StatsCommand {

    private StatsCommand() {
    }

    @SubscribeEvent
    public static void register(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(
            literal("stats").then(argument("username", word())
                .suggests((context, builder) -> {
                    Minecraft mc = Minecraft.getInstance();
                    if (mc.getConnection() == null) return builder.buildFuture();
                    return SharedSuggestionProvider.suggest(
                        mc.getConnection().getOnlinePlayers().stream()
                            .map(info -> info.getProfile().getName())
                            .collect(Collectors.toList()),
                        builder
                    );
                })
                .executes(ctx -> {
                    String username = getString(ctx, "username");
                    StatsLookup.lookupAndOpen(username, Minecraft.getInstance().screen);
                    return 1;
                })
            )
        );
    };
};
