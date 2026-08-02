package com.example.cduprofiledisplay;

import net.minecraft.client.Minecraft;

/**
 * Any other part of your mod (a button, a leaderboard row, a keybind) can
 * open someone's profile the same way the /stats command and the clickable
 * username do — just call StatsLookup.lookupAndOpen(...).
 */
public final class PlayerStatsScreenExample {

    private PlayerStatsScreenExample() {
    }

    public static void openProfileFor(String username) {
        StatsLookup.lookupAndOpen(username, Minecraft.getInstance().screen);
    }
}
