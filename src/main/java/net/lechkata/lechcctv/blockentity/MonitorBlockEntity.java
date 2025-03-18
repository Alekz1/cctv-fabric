package net.lechkata.lechcctv.blockentity;

import net.lechkata.lechcctv.block.CCamera;
import net.lechkata.lechcctv.block.CameraBlock;
import net.lechkata.lechcctv.util.TickableBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import qouteall.imm_ptl.core.portal.Portal;
import java.util.List;
import static net.lechkata.lechcctv.block.Monitor.POWERED;

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

    public Portal findNearPortal(BlockPos monitorPos) {
        // Define the area to search
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
        return null;
    }
    public boolean isPortalActive(BlockPos monitorPos) {
        // Define the area to search
        double scanRadius = 1.1;
        Box scanArea = new Box(
                monitorPos.getX() - scanRadius, monitorPos.getY() - scanRadius, monitorPos.getZ() - scanRadius,
                monitorPos.getX() + scanRadius, monitorPos.getY() + scanRadius, monitorPos.getZ() + scanRadius
        );

        // Get a list of entities in the defined area
        List<Entity> nearbyEntities = world.getEntitiesByClass(Entity.class, scanArea, entity -> entity instanceof Portal);

        // Loop through the entities and find the portal
        for (Entity entity : nearbyEntities) {
            if (entity instanceof Portal portal) {
                BlockPos portalDestination = new BlockPos(
                        (int) Math.floor(portal.getDestination().x),
                        (int) Math.floor(portal.getDestination().y),
                        (int) Math.floor(portal.getDestination().z)
                );
                if (portalDestination.equals(linkedCamera)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void closePortal(BlockPos monitorPos) {
        // Define the area to search
        double scanRadius = 1.1;
        Box scanArea = new Box(
                monitorPos.getX() - scanRadius, monitorPos.getY() - scanRadius, monitorPos.getZ() - scanRadius,
                monitorPos.getX() + scanRadius, monitorPos.getY() + scanRadius, monitorPos.getZ() + scanRadius
        );

        // Get a list of entities in the defined area
        List<Entity> nearbyEntities = world.getEntitiesByClass(Entity.class, scanArea, entity -> entity instanceof Portal);

        // Loop through the entities and find the portal
        for (Entity entity : nearbyEntities) {
            if (entity instanceof Portal portal) {
                BlockPos portalDestination = new BlockPos(
                        (int) Math.floor(portal.getDestination().x),
                        (int) Math.floor(portal.getDestination().y),
                        (int) Math.floor(portal.getDestination().z)
                );

                if (portalDestination.equals(linkedCamera)) {
                    portal.remove(Entity.RemovalReason.KILLED);
                }
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
            cameraTag.putInt("CameraDirection", linkedCameraDirection.getId());
            tag.put("LinkedCamera", cameraTag); // Store inside "LinkedCamera"
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
            linkedCameraDirection = Direction.byId(cameraTag.getInt("CameraDirection"));
        } else {
            linkedCamera = null;
        }
    }

    public boolean isCameraStillValid() {
        if(world==null) {return false;}
        BlockState state = world.getBlockState(linkedCamera);
        if(state == null || state.isAir()){return false;}
        if(!(state.getBlock() instanceof CameraBlock) && !(state.getBlock() instanceof CCamera) ) {
            return false;
        }
        return true;
    }

    public String getCameratype() {
        if(world==null){return null;}
        BlockState state = world.getBlockState(linkedCamera);
        if(state.getBlock() instanceof CameraBlock){return "cblock";}
        if(state.getBlock() instanceof CameraBlock){return "camera";}
        return null;
    }


    @Override
    public void tick() {
        if (world != null && !world.isClient && linkedCamera != null) {
            if (!isCameraStillValid()) {
                closePortal(pos);
                this.linkedCamera = null;
                BlockState state = world.getBlockState(pos);
                world.setBlockState(pos, state.with(POWERED, false));
                markDirty();
            }
        }
    }


}

