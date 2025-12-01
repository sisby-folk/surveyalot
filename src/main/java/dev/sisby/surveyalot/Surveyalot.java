package dev.sisby.surveyalot;

import folk.sisby.surveyor.WorldSummary;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Surveyalot implements ModInitializer {
	public static final String ID = "surveyalot";
	public static final Logger LOGGER = LoggerFactory.getLogger(ID);

	@Override
	public void onInitialize() {
		LOGGER.info("[Surveyalot] Claim-ho!");
		WorldSummary.enableLandmarks();
		if (FabricLoader.getInstance().isModLoaded("openpartiesandclaims")) {
			OPACCompat.init();
		}
	}
}
