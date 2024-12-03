package net.lechkata.lechcctv;

import net.fabricmc.api.ModInitializer;

import net.lechkata.lechcctv.block.ModBlocks;
import net.lechkata.lechcctv.component.ModDataComponentTypes;
import net.lechkata.lechcctv.item.ModItemGroups;
import net.lechkata.lechcctv.item.ModItems;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CCTV implements ModInitializer {
	public static final String MOD_ID = "lechcctv";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Hello Fabric world!");
		ModItems.registerModItems();
		ModBlocks.registerModBlocks();
		ModItemGroups.registerItemGroups();
		ModDataComponentTypes.registerDataComponentTypes();
	}
}