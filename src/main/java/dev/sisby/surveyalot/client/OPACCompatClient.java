package dev.sisby.surveyalot.client;

import dev.sisby.surveyalot.OPACCompat;
import folk.sisby.surveyor.client.SurveyorClientEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import xaero.pac.client.event.api.OPACClientAddonRegister;

public class OPACCompatClient {
	public static void init() {
		OPACClientAddonRegister.EVENT.register((c, r) -> c.register(new OPACCompat.SurveyalotListener(i -> MinecraftClient.getInstance().world != null && MinecraftClient.getInstance().world.getRegistryKey().getValue().equals(i) ? MinecraftClient.getInstance().world : null)));
		SurveyorClientEvents.Register.worldLoad(new Identifier("surveyalot", "opac"), (w, ws, p, t, s, l) -> OPACCompat.updateClaimLandmarksForDimension(w));
	}
}
