package dev.ManlyTorch.cdu_utilities.UI;

import dev.ManlyTorch.cdu_utilities.Lib.MojangService;

import com.mojang.authlib.GameProfile;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.PlayerModelPart;

public class FakeSkinPlayer extends RemotePlayer {
    public ResourceLocation skinTexture;
    private final GameProfile profile;

    public FakeSkinPlayer(ClientLevel level, GameProfile profile, ResourceLocation skinTexture) { super(level, profile); this.profile = profile; this.skinTexture = skinTexture; }
    @Override public ResourceLocation getSkinTextureLocation() { return skinTexture != null ? skinTexture : super.getSkinTextureLocation(); }
    @Override public boolean isModelPartShown(PlayerModelPart part) { return part != PlayerModelPart.CAPE; }
    @Override public String getModelName() { return MojangService.isSlim(profile.getId().toString()) ? "slim" : "default"; }
}