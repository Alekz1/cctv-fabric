package net.lechkata.lechcctv.block;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.lechkata.lechcctv.CCTV;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

public class ModBlocks {

    public static final Block CAMERA_BLOCK = registerBlock("camera_block",
            new CameraBlock(AbstractBlock.Settings.create().strength(2f).sounds(BlockSoundGroup.METAL)));
    public static final Block MONITOR_BLOCK = registerBlock("monitor_block",
            new MonitorBlock(AbstractBlock.Settings.create().strength(2f).sounds(BlockSoundGroup.METAL)));
    public static final Block MONITOR = registerBlock("monitor",
            new Monitor(AbstractBlock.Settings.create().strength(2f).sounds(BlockSoundGroup.METAL).nonOpaque().luminance(state -> state.get(Monitor.POWERED) ? 7 : 0)));
    public static final Block CAMERA = registerBlock("camera",
            new Camera(AbstractBlock.Settings.create().strength(2f).sounds(BlockSoundGroup.METAL).nonOpaque()));
    private static Block registerBlock(String name, Block block){
        registerBlockItem(name, block);
        return  Registry.register(Registries.BLOCK, Identifier.of(CCTV.MOD_ID, name), block);
    }

    private static void registerBlockItem(String name, Block block) {
        Registry.register(Registries.ITEM, Identifier.of(CCTV.MOD_ID, name),
                new BlockItem(block, new Item.Settings()));
    }

    public static void registerModBlocks(){
        CCTV.LOGGER.info("Registering Blocks for"+ CCTV.MOD_ID);
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register(fabricItemGroupEntries -> {
                fabricItemGroupEntries.add(CAMERA_BLOCK);
                fabricItemGroupEntries.add(MONITOR_BLOCK);
        });
    }
}
