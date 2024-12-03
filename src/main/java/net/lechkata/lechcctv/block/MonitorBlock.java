package net.lechkata.lechcctv.block;

import net.lechkata.lechcctv.component.ModDataComponentTypes;
import net.lechkata.lechcctv.item.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import qouteall.imm_ptl.core.portal.Portal;

import java.util.List;

public class MonitorBlock extends Block {

    public MonitorBlock(Settings settings) {
        super(settings);
    }

    private BlockPos camerapos;
    private World pworld;



    @Override
    protected ItemActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if(stack.getItem() == ModItems.LINK_TOOL){
            if(!world.isClient && stack.get(ModDataComponentTypes.COORDINATES) != null){
                camerapos = stack.get(ModDataComponentTypes.COORDINATES);
                player.sendMessage(Text.literal("Camerapos: "+ Vec3d.of(camerapos)));
                player.sendMessage(Text.literal("Camerapos: "+ Vec3d.of(pos)));

            }if(camerapos!=null){
                Portal portal = new Portal(Portal.ENTITY_TYPE, world);
                portal.setDestDim(world.getRegistryKey());
                portal.setPortalSize(1,1,1);
                portal.setOriginPos(Vec3d.ofCenter(pos));
                portal.setDestination(Vec3d.ofCenter(camerapos));
                // Determine orientation based on the clicked face
                Direction face = hit.getSide(); // Get the face clicked
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
            }
        }


        return ItemActionResult.SUCCESS;
    }
    private Portal findPortalNearby(World world, BlockPos monitorPos) {
        // Define the area to search (e.g., 10 blocks in each direction)
        double scanRadius = 1.1;
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
