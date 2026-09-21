package dev.ManlyTorch.cdu_utilities;

import dev.ManlyTorch.cdu_utilities.Lib.DataStore;
import dev.ManlyTorch.cdu_utilities.Lib.DataStore.AHEntry;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import org.lwjgl.glfw.GLFW;

import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;

public final class AutoSell {
    private static final String CATEGORY = "key.categories.cduutils";
    public static final KeyMapping SELL_KEY = new KeyMapping("key.cduutils.sell", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_T, CATEGORY);
    public static final KeyMapping BULK_SELL_KEY = new KeyMapping("key.cduutils.bulkSell", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_Y, CATEGORY);
    private AutoSell() {};

    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.screen != null) return;
        while (SELL_KEY.consumeClick()) handleKeyPress(mc.player, "normal");
        while (BULK_SELL_KEY.consumeClick()) handleKeyPress(mc.player, "bulk");
    };

    private static String heldId(LocalPlayer player) {
        ItemStack held = player.getMainHandItem();
        if (held.isEmpty()) held = player.getOffhandItem();
        if (held.isEmpty()) return null;
        return BuiltInRegistries.ITEM.getKey(held.getItem()).toString();
    };

    private static void msg(String text) {
        LocalPlayer plyr = Minecraft.getInstance().player;
        if (plyr != null) plyr.displayClientMessage(Component.literal(text), false);
    };

    private static void handleKeyPress(LocalPlayer player, String type) {
        String id = heldId(player);
        if (id == null) return;
        AHEntry entry = DataStore.get(type, id);
        if (entry == null) return;
        player.connection.sendCommand("ah sell " + entry.price + " " + entry.amount + " " + entry.listingTime);
    };

    private static int addItem(String type, int price, int amount, int listingTime) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return 0;
        String id = heldId(player);
        if (id == null) { msg("Not holding an item, are you ok?"); return 0; }
        DataStore.put(type, id, new AHEntry(price, amount, listingTime));
        msg("Saved: " + id + " | $" + price + " x" + amount + " " + listingTime + "d");
        return 1;
    };

    private static int listItems(String type) {
        msg("Saved items:");
        DataStore.getAll(type).forEach((id, e) -> msg("  " + id + " $" + e.price + " x" + e.amount + " " + e.listingTime + "d"));
        return 1;
    };

    private static int removeItem(String type) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return 0;
        String id = heldId(player);
        if (id == null) { msg("Not holding an item, are you ok?"); return 0; }
        if (!DataStore.has(type, id)) { msg("Item's not in the list."); return 0; }
        DataStore.getAll(type).remove(id);
        DataStore.save();
        msg("Removed: " + id);
        return 1;
    };

    public static <S> LiteralArgumentBuilder<S> command(String name, String type) {
        return LiteralArgumentBuilder.<S>literal(name)
            .then(RequiredArgumentBuilder.<S, Integer>argument("price", integer(0))
                .executes(ctx -> addItem(type, getInteger(ctx, "price"), 1, 1))
                .then(RequiredArgumentBuilder.<S, Integer>argument("amount", integer(1))
                    .executes(ctx -> addItem(type, getInteger(ctx, "price"), getInteger(ctx, "amount"), 1))
                    .then(RequiredArgumentBuilder.<S, Integer>argument("listingtime", integer(1))
                        .executes(ctx -> addItem(type, getInteger(ctx, "price"), getInteger(ctx, "amount"), getInteger(ctx, "listingtime")))
                    )
                )
            )
            .then(LiteralArgumentBuilder.<S>literal("list").executes(ctx -> listItems(type)))
            .then(LiteralArgumentBuilder.<S>literal("remove").executes(ctx -> removeItem(type)));
    };
};