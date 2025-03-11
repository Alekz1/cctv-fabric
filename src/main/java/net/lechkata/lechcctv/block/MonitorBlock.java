package net.lechkata.lechcctv.block;

import com.mojang.serialization.MapCodec;
import net.lechkata.lechcctv.blockentity.MonitorBlockEntity;
import net.lechkata.lechcctv.component.ModDataComponentTypes;
import net.lechkata.lechcctv.item.ModItems;
import net.lechkata.lechcctv.util.TickableBlockEntity;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import qouteall.imm_ptl.core.portal.Portal;
import qouteall.imm_ptl.core.portal.PortalManipulation;



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

                monitorEntity.setLinkedCamera(null, null); // No linked camera at start
            }
        }
    }

    @Override
    protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if(stack.getItem() == ModItems.LINK_TOOL){

            if(!world.isClient && stack.get(ModDataComponentTypes.COORDINATES) != null){
                camerapos = stack.get(ModDataComponentTypes.COORDINATES);
                // Determine orientation based on the clicked face
                cameraface = stack.get(ModDataComponentTypes.DIRECTION); // Get the camera face clicked
                BlockEntity entity = world.getBlockEntity(pos);
                if (entity instanceof MonitorBlockEntity monitorEntity) {
                    monitorEntity.setLinkedCamera(camerapos, cameraface); // No linked camera at start
                    monitorEntity.markDirty();
                    world.updateListeners(pos, state, state, Block.NOTIFY_ALL); // Ensures block updates
                }
                return ItemActionResult.SUCCESS;
            }
        } else if (stack.getItem() == Items.AIR) {
            BlockEntity entity = world.getBlockEntity(pos);
            if  (entity instanceof MonitorBlockEntity monitorEntity) {
                camerapos = monitorEntity.getLinkedCamera();
                cameraface = monitorEntity.getLinkedCameraDir();
            }

            if(camerapos!=null && cameraface!=null) {
                entity = world.getBlockEntity(pos);
                if (entity instanceof MonitorBlockEntity monitorEntity) {
                    boolean portalActive = monitorEntity.isPortalActive(pos);
                    if(portalActive &&player.isSneaking()){
                        monitorEntity.closePortal(pos);
                        return ItemActionResult.SUCCESS;
                    }
                    if(!monitorEntity.isPortalActive(pos) && !player.isSneaking()) {
                    Portal portal = new Portal(Portal.ENTITY_TYPE, world);
                    portal.setDestDim(world.getRegistryKey());
                    portal.setPortalSize(1, 1, 1);
                    portal.setOriginPos(Vec3d.ofCenter(pos));
                    portal.setDestination(Vec3d.ofCenter(camerapos));
                    portal.setInteractable(false);
                    portal.setTeleportable(false);
                    Direction face = hit.getSide();
                    switch (face) {
                        case NORTH ->
                                portal.setOrientation(new Vec3d(-1, 0, 0), new Vec3d(0, 1, 0)); // Facing north (Z-negative)
                        case SOUTH ->
                                portal.setOrientation(new Vec3d(1, 0, 0), new Vec3d(0, 1, 0)); // Facing south (Z-positive)
                        case EAST ->
                                portal.setOrientation(new Vec3d(0, 0, -1), new Vec3d(0, 1, 0)); // Facing east (X-positive)
                        case WEST ->
                                portal.setOrientation(new Vec3d(0, 0, 1), new Vec3d(0, 1, 0)); // Facing west (X-negative)
                        case UP ->
                                portal.setOrientation(new Vec3d(1, 0, 0), new Vec3d(0, 0, -1)); // Facing up (Y-positive)
                        case DOWN ->
                                portal.setOrientation(new Vec3d(1, 0, 0), new Vec3d(0, 0, 1)); // Facing down (Y-negative)
                    }
                    Vec3d offset = Vec3d.ofCenter(pos).add(Vec3d.of(face.getVector()).multiply(0.501)); // Offset from block center
                    portal.setOriginPos(offset);
                    switch (cameraface) {
                        case NORTH -> portal.setOtherSideOrientation(PortalManipulation.getPortalOrientationQuaternion(
                                new Vec3d(-1, 0, 0), new Vec3d(0, 1, 0))); // Camera facing north
                        case SOUTH -> portal.setOtherSideOrientation(PortalManipulation.getPortalOrientationQuaternion(
                                new Vec3d(1, 0, 0), new Vec3d(0, 1, 0))); // Camera facing south
                        case EAST -> portal.setOtherSideOrientation(PortalManipulation.getPortalOrientationQuaternion(
                                new Vec3d(0, 0, -1), new Vec3d(0, 1, 0))); // Camera facing east
                        case WEST -> portal.setOtherSideOrientation(PortalManipulation.getPortalOrientationQuaternion(
                                new Vec3d(0, 0, 1), new Vec3d(0, 1, 0))); // Camera facing west
                        case UP -> portal.setOtherSideOrientation(PortalManipulation.getPortalOrientationQuaternion(
                                new Vec3d(1, 0, 0), new Vec3d(0, 0, -1))); // Camera facing up
                        case DOWN -> portal.setOtherSideOrientation(PortalManipulation.getPortalOrientationQuaternion(
                                new Vec3d(1, 0, 0), new Vec3d(0, 0, 1))); // Camera facing down
                    }
                    if (portal.isPortalValid()) {
                        portal.getWorld().spawnEntity(portal);
                    }
                        return ItemActionResult.SUCCESS;
                    }
                }
            }
        }
        return ItemActionResult.FAIL;
    }


    @Override
    protected void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        BlockEntity entity = world.getBlockEntity(pos);
        if (entity instanceof MonitorBlockEntity monitorEntity) {
           monitorEntity.closePortal(pos);
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }
}
