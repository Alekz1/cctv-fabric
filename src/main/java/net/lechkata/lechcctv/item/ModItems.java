package net.lechkata.lechcctv.item;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.lechkata.lechcctv.CCTV;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {

    public static final Item SCREEN = registerItem("screen", new Item(new Item.Settings()));
    public static final Item LENS = registerItem("lens", new Item(new Item.Settings()));
    public static final Item LINK_TOOL = registerItem("link_tool", new LinkToolItem(new Item.Settings().maxCount(1)));

    private static Item registerItem(String name, Item item){
        return Registry.register(Registries.ITEM, Identifier.of(CCTV.MOD_ID, name), item);
    }
    public static void registerModItems() {
        CCTV.LOGGER.info("Registering Items for "+ CCTV.MOD_ID);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register(fabricItemGroupEntries -> {
            fabricItemGroupEntries.add(SCREEN);
            fabricItemGroupEntries.add(LENS);
        });
    }
}
