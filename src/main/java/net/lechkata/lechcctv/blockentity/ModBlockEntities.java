package net.lechkata.lechcctv.blockentity;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.lechkata.lechcctv.CCTV;
import net.lechkata.lechcctv.block.ModBlocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModBlockEntities {
    public static final BlockEntityType<MonitorBlockEntity> MONITOR_BLOCK_ENTITY =
            Registry.register(
                    Registries.BLOCK_ENTITY_TYPE,
                    Identifier.of(CCTV.MOD_ID, "monitor_block_entity"),
                    FabricBlockEntityTypeBuilder.create(MonitorBlockEntity::new, ModBlocks.MONITOR_BLOCK).build()
            );

    public static void registerBlockEntities() {
        // This method ensures the block entities are loaded when the mod initializes.
    }
}
