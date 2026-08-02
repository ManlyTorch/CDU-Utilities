package com.example.cduprofiledisplay;

import com.google.gson.JsonObject;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public final class StatsLookup {
    private static final Executor IO_POOL = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "stats-lookup");
        t.setDaemon(true);
        return t;
    });

    private StatsLookup() {};

    public static void lookupAndOpen(String username, Screen returnTo) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.player != null) {
            Component message = Component.literal("Fetching stats for ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(username)
                    .withStyle(ChatFormatting.WHITE));
            mc.player.displayClientMessage(message, true);
        };

        CompletableFuture
            .supplyAsync(() -> resolveUuid(mc, username), IO_POOL)
            .thenApplyAsync(uuid -> {
                JsonObject stats;
                try {stats = PlayCduApi.fetchPlayerStats(mc, uuid, username);
                } catch (Exception e) {throw new RuntimeException("Failed to fetch stats: " + e.getMessage(), e);}
                return PlayerStatsData.fromJson(uuid, username, stats);
            }, IO_POOL)
            .whenCompleteAsync((data, error) -> {
                if (error != null) {
                    if (mc.player != null)
                        mc.player.displayClientMessage(Component.literal("Couldn't load stats for ")
                        .withStyle(ChatFormatting.RED)
                        .append(Component.literal(username)
                            .withStyle(ChatFormatting.WHITE)), false);
                    return;
                };
                mc.setScreen(new PlayerStatsScreen(returnTo, data));
            }, mc);
    };

    private static UUID resolveUuid(Minecraft mc, String username) {
        if (mc.getConnection() != null) {
            for (PlayerInfo info : mc.getConnection().getOnlinePlayers()) {
                if (info.getProfile().getName().equalsIgnoreCase(username)) {
                    return info.getProfile().getId();
                }
            }
        }
        try {
            return MojangApi.resolveUuid(username);
        } catch (Exception e) {
            throw new RuntimeException("Couldn't resolve a UUID for '" + username + "': " + e.getMessage(), e);
        }
    }
}
