package dev.ManlyTorch.cdu_utilities;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.stream.Stream;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

@Mod.EventBusSubscriber(modid = "cduutils", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class StatsCommand {
    private StatsCommand() {}

    @SubscribeEvent
    public static void register(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(
            literal("stats").then(argument("username", word())
                .suggests((context, builder) -> {
                    Minecraft mc = Minecraft.getInstance();
                    ClientPacketListener con = mc.getConnection();
                    List<String> customNames = List.of("ManlyTorch", "Anti_Hydrogen", "ToxicogenicBees", "DerpDude", "Oliviajumba");
                    if (con == null) return SharedSuggestionProvider.suggest(customNames, builder);
                    Stream<String> onlineUsers = con.getOnlinePlayers().stream().map(info -> info.getProfile().getName());
                    return SharedSuggestionProvider.suggest(
                        Stream.concat(customNames.stream(), onlineUsers).distinct().toList(), builder
                    );
                })
                .executes(ctx -> {
                    String username = getString(ctx, "username");
                    PlayerStatsUI.loadPlayerStats(username, Minecraft.getInstance().screen);
                    return 1;
                })
            )
        );
    };
};
