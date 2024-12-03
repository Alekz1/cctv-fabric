package net.lechkata.lechcctv.item;

import net.lechkata.lechcctv.block.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class LinkToolItem extends Item {
    public LinkToolItem(Settings settings){
        super(settings);
    }
    private Text msg = Text.literal("Saved Camera Position!");
    public BlockPos camerapos;

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        Block block = world.getBlockState(context.getBlockPos()).getBlock();
        if (!world.isClient()) {
            if (block == ModBlocks.CAMERA_BLOCK) {
                camerapos = context.getBlockPos();
                context.getPlayer().sendMessage(msg);
            }
        }

        return ActionResult.SUCCESS;
    }
}
