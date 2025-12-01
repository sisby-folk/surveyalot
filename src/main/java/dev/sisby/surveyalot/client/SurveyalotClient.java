package dev.sisby.surveyalot.client;

import dev.sisby.surveyalot.Surveyalot;
import folk.sisby.surveyor.WorldSummary;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class SurveyalotClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		Surveyalot.LOGGER.info("[Surveyalot Client] Client-ho!");
		WorldSummary.enableLandmarks();
		if (FabricLoader.getInstance().isModLoaded("openpartiesandclaims")) {
			OPACCompatClient.init();
		}
	}
}
