package net.lechkata.lechcctv.block;

import com.mojang.serialization.MapCodec;
import net.lechkata.lechcctv.blockentity.ModBlockEntities;
import net.lechkata.lechcctv.blockentity.MonitorBlockEntity;
import net.lechkata.lechcctv.component.ModDataComponentTypes;
import net.lechkata.lechcctv.item.ModItems;
import net.lechkata.lechcctv.util.TickableBlockEntity;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import qouteall.imm_ptl.core.portal.Portal;

import java.util.List;


public class MonitorBlock extends BlockWithEntity implements BlockEntityProvider {

    public MonitorBlock(Settings settings) {
        super(settings);
    }
    public static final MapCodec<MonitorBlock> CODEC = MonitorBlock.createCodec(MonitorBlock::new);

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }


    public BlockPos camerapos;
    private Direction cameraface;
    private World pworld;


    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new MonitorBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return TickableBlockEntity.getTicker(world);
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);

        if (!world.isClient) {
            BlockEntity entity = world.getBlockEntity(pos);
            if (entity instanceof MonitorBlockEntity monitorEntity) {

                monitorEntity.setLinkedCamera(null); // No linked camera at start
            }
        }
    }

    @Override
    protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if(stack.getItem() == ModItems.LINK_TOOL){

            if(!world.isClient && stack.get(ModDataComponentTypes.COORDINATES) != null){
                camerapos = stack.get(ModDataComponentTypes.COORDINATES);
                BlockEntity entity = world.getBlockEntity(pos);
                if (entity instanceof MonitorBlockEntity monitorEntity) {
                    monitorEntity.setLinkedCamera(camerapos); // No linked camera at start
                    monitorEntity.markDirty();
                    world.updateListeners(pos, state, state, Block.NOTIFY_ALL); // Ensures block updates
                    player.sendMessage(Text.literal("Camerapos saved monitorEntity!"+camerapos));
                }
                player.sendMessage(Text.literal("Camerapos: "+ Vec3d.of(camerapos)));
                player.sendMessage(Text.literal("Monitorpos: "+ Vec3d.of(pos)));
                // Determine orientation based on the clicked face
                cameraface = stack.get(ModDataComponentTypes.DIRECTION); // Get the camera face clicked
                return ItemActionResult.SUCCESS;
            }
        } else if (stack.getItem() == Items.AIR) {
            BlockEntity entity = world.getBlockEntity(pos);
            if  (entity instanceof MonitorBlockEntity monitorEntity) {
                camerapos = monitorEntity.getLinkedCamera();
                player.sendMessage(Text.literal("Test!"));
                Text msg = null;
                if (camerapos != null) {
                    msg = Text.literal("CameraPos: X:" + camerapos.getX() + " Y:" + camerapos.getY() + "Z:" + camerapos.getZ());
                    player.sendMessage(msg); // No linked camera at start
                }

            }

            if(camerapos!=null){
                Portal portal = new Portal(Portal.ENTITY_TYPE, world);
                portal.setDestDim(world.getRegistryKey());
                portal.setPortalSize(1,1,1);
                portal.setOriginPos(Vec3d.ofCenter(pos));
                portal.setDestination(Vec3d.ofCenter(camerapos));
                portal.setInteractable(false);
                portal.setTeleportable(false);
                Direction face = hit.getSide();
                switch (face) {
                    case NORTH -> portal.setOrientation(new Vec3d(-1, 0, 0), new Vec3d(0, 1, 0)); // Facing north (Z-negative)
                    case SOUTH -> portal.setOrientation(new Vec3d(1, 0, 0), new Vec3d(0, 1, 0)); // Facing south (Z-positive)
                    case EAST -> portal.setOrientation(new Vec3d(0, 0, -1), new Vec3d(0, 1, 0)); // Facing east (X-positive)
                    case WEST -> portal.setOrientation(new Vec3d(0, 0, 1), new Vec3d(0, 1, 0)); // Facing west (X-negative)
                    case UP -> portal.setOrientation(new Vec3d(1, 0, 0), new Vec3d(0, 0, -1)); // Facing up (Y-positive)
                    case DOWN -> portal.setOrientation(new Vec3d(1, 0, 0), new Vec3d(0, 0, 1)); // Facing down (Y-negative)
                }
                Vec3d offset = Vec3d.ofCenter(pos).add(Vec3d.of(face.getVector()).multiply(0.501)); // Offset from block center
                portal.setOriginPos(offset);
                if(portal.isPortalValid()){
                    portal.getWorld().spawnEntity(portal);
                }else {
                    player.sendMessage(Text.literal("Invalid Portal!"));
                }
                return ItemActionResult.SUCCESS;
            }
        }
        return ItemActionResult.FAIL;
    }


    private Portal findPortalNearby(World world, BlockPos monitorPos) {
        // Define the area to search
        double scanRadius = 1.5;
        Box scanArea = new Box(
                monitorPos.getX() - scanRadius, monitorPos.getY() - scanRadius, monitorPos.getZ() - scanRadius,
                monitorPos.getX() + scanRadius, monitorPos.getY() + scanRadius, monitorPos.getZ() + scanRadius
        );

        // Get a list of entities in the defined area
        List<Entity> nearbyEntities = world.getEntitiesByClass(Entity.class, scanArea, entity -> entity instanceof Portal);

        // Loop through the entities and find the portal
        for (Entity entity : nearbyEntities) {
            if (entity instanceof Portal) {
                Portal portal = (Portal) entity;
                return portal;
            }
        }

        return null;  // No linked portal found in the area
    }


    @Override
    protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        Portal portal = findPortalNearby(world, pos);
        if(portal!=null){
            portal.remove(Entity.RemovalReason.KILLED);
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }
}
