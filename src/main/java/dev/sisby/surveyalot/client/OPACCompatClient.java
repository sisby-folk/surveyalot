package dev.sisby.surveyalot.client;

import dev.sisby.surveyalot.OPACCompat;
import folk.sisby.surveyor.client.SurveyorClientEvents;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import xaero.pac.client.event.api.OPACClientAddonRegister;

public class OPACCompatClient {
	public static RegistryKey<World> prevDim = null;
	
	public static void init() {
		OPACClientAddonRegister.EVENT.register((c, r) -> c.register(new OPACCompat.SurveyalotListener(i -> MinecraftClient.getInstance().world != null && MinecraftClient.getInstance().world.getRegistryKey().getValue().equals(i) ? MinecraftClient.getInstance().world : null)));
		ClientTickEvents.END_WORLD_TICK.register((world -> { 
			if (WorldSummary.of(world) != null && !world.getRegistryKey().equals(prevDim)) {
				prevDim = world.getRegistryKey();
				OPACCompat.updateClaimLandmarksForDimension(world);
		    }
	 	}
	}
}
