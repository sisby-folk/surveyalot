package dev.sisby.surveyalot;

import com.google.common.collect.Table;
import com.google.common.collect.HashBasedTable;
import folk.sisby.surveyor.WorldSummary;
import folk.sisby.surveyor.landmark.Landmark;
import folk.sisby.surveyor.landmark.WorldLandmarks;
import folk.sisby.surveyor.landmark.component.LandmarkComponentTypes;
import folk.sisby.surveyor.util.RegionPos;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import xaero.pac.client.api.OpenPACClientAPI;
import xaero.pac.common.claims.player.api.IPlayerChunkClaimAPI;
import xaero.pac.common.claims.player.api.IPlayerClaimInfoAPI;
import xaero.pac.common.claims.player.api.IPlayerClaimPosListAPI;
import xaero.pac.common.claims.tracker.api.IClaimsManagerListenerAPI;
import xaero.pac.common.event.api.OPACServerAddonRegister;
import xaero.pac.common.server.api.OpenPACServerAPI;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

public class OPACCompat {
	public static void init() {
		OPACServerAddonRegister.EVENT.register((s, perms, parties, claims) -> claims.register(new SurveyalotListener(i -> s.getWorld(RegistryKey.of(RegistryKeys.WORLD, i)))));
	}

	public static void updateClaimLandmarksForDimension(World world) {
		WorldLandmarks landmarks = WorldLandmarks.of(world);
		if (landmarks == null) return;
		Table<UUID, Identifier, Landmark> changed = landmarks.removeAllForBatch(HashBasedTable.create(), l -> l.id().toString().startsWith("opac:claim"));
		for (IPlayerClaimInfoAPI player : world instanceof ServerWorld sw ? OpenPACServerAPI.get(sw.getServer()).getServerClaimsManager().getPlayerInfoStream().toList() : OpenPACClientAPI.get().getClaimsManager().getPlayerInfoStream().toList()) {
			for (IPlayerClaimPosListAPI claimPositions : Optional.ofNullable(player.getDimension(world.getRegistryKey().getValue())).map(d -> d.getStream().toList()).orElse(List.of())) {
				IPlayerChunkClaimAPI claim = claimPositions.getClaimState();
				String claimName = claim.getSubConfigIndex() != -1 ? Objects.requireNonNullElse(player.getClaimsName(claim.getSubConfigIndex()), "") : Objects.requireNonNullElse(player.getClaimsName(), "");
				landmarks.putForBatch(changed, Landmark.create(WorldLandmarks.GLOBAL, Identifier.of("opac", "claim/%s%s%s".formatted(claim.getPlayerId(), claim.getSubConfigIndex() == -1 ? "" : ("/" + claim.getSubConfigIndex()), claim.isForceloadable() ? "/forced" : "")), b -> b
					.add(LandmarkComponentTypes.NAME, Text.literal((claimName.isBlank() ? "" : claimName + " - ") + player.getPlayerUsername() + "'s Claim" + (claim.isForceloadable() ? " (forceloaded)" : "")))
					.add(LandmarkComponentTypes.COLOR, Optional.ofNullable(claim.getSubConfigIndex() == -1 ? Integer.valueOf(player.getClaimsColor()) : player.getClaimsColor(claim.getSubConfigIndex())).map(i -> 0x00_FFFFFF & i).orElse(null))
					.add(LandmarkComponentTypes.CHUNKS, RegionPos.chunksToRegions(claimPositions.getStream().toList()))
				));
			}
		}
		landmarks.handleChanged(changed, world.isClient(), null);
	}

	public record SurveyalotListener(Function<Identifier, World> worldGetter) implements IClaimsManagerListenerAPI {
		@Override
		public void onWholeRegionChange(@NotNull Identifier dimension, int regionX, int regionZ) {
			onDimensionChange(dimension);
		}

		@Override
		public void onChunkChange(@NotNull Identifier dimension, int chunkX, int chunkZ, IPlayerChunkClaimAPI api) {
			onDimensionChange(dimension);
		}

		@Override
		public void onDimensionChange(Identifier dimension) {
			World world = worldGetter.apply(dimension);
			if (world != null) updateClaimLandmarksForDimension(world);
		}
	}
}
