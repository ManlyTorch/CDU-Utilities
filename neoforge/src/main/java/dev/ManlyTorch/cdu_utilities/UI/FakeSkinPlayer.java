package dev.ManlyTorch.cdu_utilities.UI;

import dev.ManlyTorch.cdu_utilities.Lib.MojangService;

import com.mojang.authlib.GameProfile;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.PlayerModelPart;

public class FakeSkinPlayer extends RemotePlayer {
    public ResourceLocation skinTexture;
    private final GameProfile profile;

    public FakeSkinPlayer(ClientLevel level, GameProfile profile, ResourceLocation skinTexture) { super(level, profile); this.profile = profile; this.skinTexture = skinTexture; }
    @Override public boolean isModelPartShown(PlayerModelPart part) { return part != PlayerModelPart.CAPE; }
    @Override public PlayerSkin getSkin() {
        if (skinTexture == null) return super.getSkin();
        return new PlayerSkin(skinTexture, null, null, null, MojangService.isSlim(profile.getId().toString()) ? PlayerSkin.Model.SLIM : PlayerSkin.Model.WIDE, true);
    }
}