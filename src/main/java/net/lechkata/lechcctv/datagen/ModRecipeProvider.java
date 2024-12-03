package net.lechkata.lechcctv.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.lechkata.lechcctv.block.ModBlocks;
import net.lechkata.lechcctv.item.ModItems;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends FabricRecipeProvider {
    public ModRecipeProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    public void generate(RecipeExporter recipeExporter) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModItems.LENS)
                .pattern(" G ")
                .pattern("G G")
                .pattern(" G ")
                .input('G', Items.GLASS)
                .criterion(hasItem(Items.GLASS), conditionsFromItem(Items.GLASS))
                .offerTo(recipeExporter);
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModBlocks.CAMERA_BLOCK)
                .pattern("III")
                .pattern("IRI")
                .pattern("ILI")
                .input('I', Items.IRON_INGOT)
                .input('R', Items.REDSTONE)
                .input('L', ModItems.LENS)
                .criterion(hasItem(ModItems.LENS), conditionsFromItem(ModItems.LENS))
                .offerTo(recipeExporter);
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModItems.SCREEN)
                .pattern("III")
                .pattern("IDI")
                .pattern("IGI")
                .input('I', Items.IRON_INGOT)
                .input('D', Items.GLOWSTONE_DUST)
                .input('G', Items.GLASS)
                .criterion(hasItem(Items.GLOWSTONE_DUST), conditionsFromItem(Items.GLOWSTONE_DUST))
                .offerTo(recipeExporter);
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModBlocks.MONITOR_BLOCK)
                .pattern("III")
                .pattern("IRI")
                .pattern("ISI")
                .input('I', Items.IRON_INGOT)
                .input('R', Items.REDSTONE)
                .input('S', ModItems.SCREEN)
                .criterion(hasItem(ModItems.SCREEN), conditionsFromItem(ModItems.SCREEN))
                .offerTo(recipeExporter);
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, ModItems.LINK_TOOL)
                .pattern("  R")
                .pattern(" S ")
                .pattern("I  ")
                .input('I', Items.IRON_INGOT)
                .input('R', Items.REDSTONE)
                .input('S', Items.STICK)
                .criterion(hasItem(Items.REDSTONE), conditionsFromItem(Items.REDSTONE))
                .offerTo(recipeExporter);

    }
}
