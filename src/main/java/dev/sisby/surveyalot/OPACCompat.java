package dev.sisby.surveyalot;

import folk.sisby.surveyor.WorldSummary;
import folk.sisby.surveyor.landmark.Landmark;
import folk.sisby.surveyor.landmark.WorldLandmarks;
import folk.sisby.surveyor.landmark.component.LandmarkComponentTypes;
import folk.sisby.surveyor.util.RegionPos;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import xaero.pac.common.claims.player.api.IPlayerChunkClaimAPI;
import xaero.pac.common.claims.player.api.IPlayerClaimPosListAPI;
import xaero.pac.common.claims.tracker.api.IClaimsManagerListenerAPI;
import xaero.pac.common.server.api.OpenPACServerAPI;
import xaero.pac.common.server.claims.player.api.IServerPlayerClaimInfoAPI;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class OPACCompat {
	public static void init() {
		ServerLifecycleEvents.SERVER_STARTED.register(s -> OpenPACServerAPI.get(s).getServerClaimsManager().getTracker().register(new SurveyalotListener(s)));
	}

	public static void updateClaimLandmarksForDimension(ServerWorld world) {
		WorldLandmarks landmarks = world == null ? null : WorldSummary.of(world).landmarks();
		if (landmarks == null) return;
		landmarks.removeAll(world, l -> l.id().toString().startsWith("opac:claim"));
		Map<UUID, Map<Identifier, Landmark>> changes = new HashMap<>();
		for (IServerPlayerClaimInfoAPI player : OpenPACServerAPI.get(world.getServer()).getServerClaimsManager().getPlayerInfoStream().toList()) {
			for (IPlayerClaimPosListAPI claimPositions : Optional.ofNullable(player.getDimension(world.getDimensionKey().getValue())).map(d -> d.getStream().toList()).orElse(List.of())) {
				IPlayerChunkClaimAPI claim = claimPositions.getClaimState();
				landmarks.putForBatch(changes, Landmark.create(WorldLandmarks.GLOBAL, Identifier.of("opac", "claim/%s%s".formatted(claim.getPlayerId(), claim.getSubConfigIndex() == -1 ? "" : ("/" + claim.getSubConfigIndex()))), b -> b
					.add(LandmarkComponentTypes.NAME, Text.literal((claim.getSubConfigIndex() == -1 ? "" : player.getClaimsName(claim.getSubConfigIndex()) + " - ") + player.getPlayerUsername() + "'s Claim"))
					.add(LandmarkComponentTypes.COLOR, claim.getSubConfigIndex() == -1 ? Integer.valueOf(player.getClaimsColor()) : player.getClaimsColor(claim.getSubConfigIndex()))
					.add(LandmarkComponentTypes.CHUNKS, RegionPos.chunksToRegions(claimPositions.getStream().toList()))
				));
			}
		}
		landmarks.handleChanged(world, changes, false, null);
	}

	public record SurveyalotListener(MinecraftServer server) implements IClaimsManagerListenerAPI {
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
			updateClaimLandmarksForDimension(server.getWorld(RegistryKey.of(RegistryKeys.WORLD, dimension)));
		}
	}
}
