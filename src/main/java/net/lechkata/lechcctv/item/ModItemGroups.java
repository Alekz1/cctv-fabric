package net.lechkata.lechcctv.item;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.lechkata.lechcctv.CCTV;
import net.lechkata.lechcctv.block.ModBlocks;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;


public class ModItemGroups {

    public static final ItemGroup CCTV_ITEM_GROUP = Registry.register(Registries.ITEM_GROUP,
            Identifier.of(CCTV.MOD_ID, "cctv_items"),
            FabricItemGroup.builder().icon(() -> new ItemStack(ModBlocks.CAMERA_BLOCK))
                    .displayName(Text.translatable("itemgroup.lechcctv.cctv_items"))
                    .entries((displayContext, entries) -> {
                        entries.add(ModItems.LENS);
                        entries.add(ModItems.SCREEN);
                        entries.add(ModItems.LINK_TOOL);
                        entries.add(ModBlocks.MONITOR_BLOCK);
                        entries.add(ModBlocks.CAMERA_BLOCK);
                        entries.add(ModBlocks.MONITOR);
                        entries.add(ModBlocks.CAMERA);
                    })
                    .build()
            );

    public static void registerItemGroups(){
        CCTV.LOGGER.info("Registering Item groups for" + CCTV.MOD_ID);
    }
}
