package net.lechkata.lechcctv.blockentity;

import net.lechkata.lechcctv.block.CameraBlock;
import net.lechkata.lechcctv.util.TickableBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import qouteall.imm_ptl.core.portal.Portal;

import java.util.List;

public class MonitorBlockEntity extends BlockEntity implements TickableBlockEntity {
    private BlockPos linkedCamera;
    private Direction linkedCameraDirection;// Stores the linked camera position

    public MonitorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MONITOR_BLOCK_ENTITY, pos, state);
    }

    public void setLinkedCamera(BlockPos cameraPos, Direction cameraDir) {
        this.linkedCamera = cameraPos;
        this.linkedCameraDirection = cameraDir;
        markDirty(); // Ensures the data is saved
    }

    public BlockPos getLinkedCamera() {
        return this.linkedCamera;
    }

    public Direction getLinkedCameraDir() {
        return this.linkedCameraDirection;
    }

    public void closePortal(BlockPos monitorPos) {
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
                portal.remove(Entity.RemovalReason.KILLED);
            }
        }
    }

    @Override
    protected void writeNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(tag, registryLookup);
        if (linkedCamera != null) {
            NbtCompound cameraTag = new NbtCompound();
            cameraTag.putInt("CameraX", linkedCamera.getX());
            cameraTag.putInt("CameraY", linkedCamera.getY());
            cameraTag.putInt("CameraZ", linkedCamera.getZ());
            tag.put("LinkedCamera", cameraTag); // Store inside "LinkedCamera"
            System.out.println("Saved camera position: "+ linkedCamera);
        }
    }

    @Override
    protected void readNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(tag, registryLookup);
        if (tag.contains("LinkedCamera")) {
            NbtCompound cameraTag = tag.getCompound("LinkedCamera");
            linkedCamera = new BlockPos(cameraTag.getInt("CameraX"),
                    cameraTag.getInt("CameraY"),
                    cameraTag.getInt("CameraZ"));
            System.out.println("Loaded camera position: " + linkedCamera);
        } else {
            linkedCamera = null;
            System.out.println("No saved camera found.");
        }
    }

    private boolean isCameraStillValid() {
        if(world==null) {return false;}
        BlockState state = world.getBlockState(linkedCamera);
        if(state == null || state.isAir()){System.out.println("No block found at "+ linkedCamera); return false;}
        if(!(state.getBlock() instanceof CameraBlock)) {
            System.out.println("Block found at "+linkedCamera+" but is not a camera");
            return false;
        }
        return true;

    }

    @Override
    public void tick() {
        if (world != null && !world.isClient && linkedCamera != null) {
            if (!isCameraStillValid()) {
                closePortal(pos);
                markDirty();
            }
        }
    }


}

