package net.lechkata.lechcctv.item;

import net.lechkata.lechcctv.block.ModBlocks;
import net.lechkata.lechcctv.component.ModDataComponentTypes;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class LinkToolItem extends Item {
    public LinkToolItem(Settings settings){
        super(settings);
    }
    private Text msg = Text.literal("Saved Camera Position!");

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        Block block = world.getBlockState(context.getBlockPos()).getBlock();
        if (!world.isClient()) {
            if (block == ModBlocks.CAMERA_BLOCK) {
                context.getStack().set(ModDataComponentTypes.COORDINATES, context.getBlockPos());
                context.getStack().set(ModDataComponentTypes.DIRECTION, context.getSide());
                context.getPlayer().sendMessage(msg);
            }
        }

        return ActionResult.SUCCESS;
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        if (stack.get(ModDataComponentTypes.COORDINATES) != null) {
            tooltip.add(Text.literal("Camera position saved: "+ stack.get(ModDataComponentTypes.COORDINATES)));
        }

        super.appendTooltip(stack, context, tooltip, type);
    }
}
