package net.lechkata.lechcctv.block;

import com.mojang.serialization.MapCodec;
import net.fabricmc.loader.impl.lib.sat4j.core.VecInt;
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
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import qouteall.imm_ptl.core.portal.Portal;
import qouteall.imm_ptl.core.portal.PortalManipulation;

import java.util.Objects;


public class MonitorBlock extends HorizontalFacingBlock implements BlockEntityProvider {
    public MonitorBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState()
                .with(FACING, Direction.NORTH)  // Default facing
                .with(CONNECTED_LEFT, false)
                .with(CONNECTED_RIGHT, false)
                .with(CONNECTED_UP, false)
                .with(CONNECTED_DOWN, false)
        );
    }
    public static final MapCodec<MonitorBlock> CODEC = MonitorBlock.createCodec(MonitorBlock::new);

    public static final BooleanProperty CONNECTED_LEFT = BooleanProperty.of("connected_left");
    public static final BooleanProperty CONNECTED_RIGHT = BooleanProperty.of("connected_right");
    public static final BooleanProperty CONNECTED_UP = BooleanProperty.of("connected_up");
    public static final BooleanProperty CONNECTED_DOWN = BooleanProperty.of("connected_down");



    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        World world = ctx.getWorld();
        BlockPos pos = ctx.getBlockPos();
        Direction facing = ctx.getHorizontalPlayerFacing().getOpposite(); // Get the player's facing direction
        boolean isSneaking = ctx.getPlayer() != null && ctx.getPlayer().isSneaking(); // Check if the player is sneaking

        // Corrected left/right calculations
        boolean left = !isSneaking && isSameFacingMonitor(world, pos.offset(getLeftDirection(facing)), facing);
        boolean right = !isSneaking && isSameFacingMonitor(world, pos.offset(getRightDirection(facing)), facing);
        boolean up = !isSneaking && isSameFacingMonitor(world, pos.up(), facing);
        boolean down = !isSneaking && isSameFacingMonitor(world, pos.down(), facing);

        return this.getDefaultState()
                .with(FACING, facing)
                .with(CONNECTED_LEFT, left)
                .with(CONNECTED_RIGHT, right)
                .with(CONNECTED_UP, up)
                .with(CONNECTED_DOWN, down);
    }

    private boolean isSameFacingMonitor(World world, BlockPos pos, Direction facing) {
        BlockState state = world.getBlockState(pos);
        return state.getBlock() instanceof MonitorBlock && state.get(FACING) == facing;
    }

    // Helper methods to determine the left and right direction based on facing
    public static Direction getLeftDirection(Direction facing) {
        return facing.rotateYCounterclockwise();
    }

    public static Direction getRightDirection(Direction facing) {
        return facing.rotateYClockwise();
    }


    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
        Direction facing = state.get(FACING);

        boolean right = isSameFacingMonitor(world, pos.offset(getLeftDirection(facing)), facing);
        boolean left = isSameFacingMonitor(world, pos.offset(getRightDirection(facing)), facing);
        boolean up = isSameFacingMonitor(world, pos.up(), facing);
        boolean down = isSameFacingMonitor(world, pos.down(), facing);

        world.setBlockState(pos, state
                .with(CONNECTED_LEFT, left)
                .with(CONNECTED_RIGHT, right)
                .with(CONNECTED_UP, up)
                .with(CONNECTED_DOWN, down), 3);
    }



    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
        builder.add(CONNECTED_DOWN);
        builder.add(CONNECTED_UP);
        builder.add(CONNECTED_LEFT);
        builder.add(CONNECTED_RIGHT);
    }

    @Override
    protected MapCodec<? extends HorizontalFacingBlock> getCodec() {
        return CODEC;
    }


    public BlockPos camerapos;
    private Direction cameraface;
    public Vec3d center;
    private int mh;
    private int mw;
    private String cameratype;
    private Vec3d dest;


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
        if (world.getBlockEntity(pos) instanceof MonitorBlockEntity entity) {
            entity.updateSize();
        }

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
        } else if (stack.getItem() == Items.AIR && !world.isClient) {
            BlockEntity entity = world.getBlockEntity(pos);
            if  (entity instanceof MonitorBlockEntity monitorEntity) {
                camerapos = monitorEntity.getLinkedCamera();
                cameraface = monitorEntity.getLinkedCameraDir();
                center = monitorEntity.getMonitorCenter();
                mh = monitorEntity.getHeight() - 1;
                mw = monitorEntity.getWidth() - 1;
                cameratype = monitorEntity.getCameratype();
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
                    portal.setPortalSize(mw + 0.9, mh + 0.8, 1);
                    portal.setOriginPos(center);
                    dest = Vec3d.ofCenter(camerapos);
                    dest = dest.add(Vec3d.of(cameraface.getVector()).multiply(0.5));
                    portal.setDestination(dest);
                    portal.setInteractable(false);
                    portal.setTeleportable(false);
                    Direction face = state.get(FACING);
                    switch (face) {
                        case NORTH ->
                                portal.setOrientation(new Vec3d(-1, 0, 0), new Vec3d(0, 1, 0)); // Facing north (Z-negative)
                        case SOUTH ->
                                portal.setOrientation(new Vec3d(1, 0, 0), new Vec3d(0, 1, 0)); // Facing south (Z-positive)
                        case EAST ->
                                portal.setOrientation(new Vec3d(0, 0, -1), new Vec3d(0, 1, 0)); // Facing east (X-positive)
                        case WEST ->
                                portal.setOrientation(new Vec3d(0, 0, 1), new Vec3d(0, 1, 0)); // Facing west (X-negative)
                        case UP, DOWN -> {return ItemActionResult.FAIL;}
//                        case UP ->
//                                portal.setOrientation(new Vec3d(1, 0, 0), new Vec3d(0, 0, -1)); // Facing up (Y-positive)
//                        case DOWN ->
//                                portal.setOrientation(new Vec3d(1, 0, 0), new Vec3d(0, 0, 1)); // Facing down (Y-negative)
                    }
                    Vec3d offset = center.add(Vec3d.of(face.getVector()).multiply(0.501)); // Offset from block center
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
           monitorEntity.updateSize();
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }
}
