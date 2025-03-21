package net.lechkata.lechcctv.blockentity;

import net.lechkata.lechcctv.block.CCamera;
import net.lechkata.lechcctv.block.CameraBlock;
import net.lechkata.lechcctv.block.Monitor;
import net.lechkata.lechcctv.block.MonitorBlock;
import net.lechkata.lechcctv.util.TickableBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import qouteall.imm_ptl.core.portal.Portal;

import java.util.*;

import static net.lechkata.lechcctv.block.Monitor.POWERED;
import static net.lechkata.lechcctv.block.MonitorBlock.getLeftDirection;
import static net.lechkata.lechcctv.block.MonitorBlock.getRightDirection;

public class MonitorBlockEntity extends BlockEntity implements TickableBlockEntity {
    private BlockPos linkedCamera;
    private Direction linkedCameraDirection;// Stores the linked camera position
    private int width = 1; // Default size
    private int height = 1;


    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void updateSize() {
        if (world == null) return;

        BlockPos startPos = getPos();
        Direction facing = getCachedState().get(MonitorBlock.FACING);

        width = countConnectedMonitors(startPos, getRightDirection(facing)) + countConnectedMonitors(startPos, getLeftDirection(facing)) + 1;
        height = countConnectedMonitors(startPos, Direction.UP) + countConnectedMonitors(startPos, Direction.DOWN) + 1;

        markDirty(); // Save changes
    }

    private int countConnectedMonitors(BlockPos startPos, Direction direction) {
        int count = 0;
        BlockPos currentPos = startPos.offset(direction);

        while (world.getBlockState(currentPos).getBlock() instanceof MonitorBlock &&
                world.getBlockState(currentPos).get(MonitorBlock.FACING) == getCachedState().get(MonitorBlock.FACING)) {
            count++;
            currentPos = currentPos.offset(direction);
        }
        return count;
    }

    public Vec3d getMonitorCenter() {
        if (world == null) return Vec3d.ofCenter(pos);

        BlockPos startPos = getPos();
        Direction facing = getCachedState().get(MonitorBlock.FACING);

        // Get the bounds of the connected monitor grid
        int minX = startPos.getX();
        int maxX = startPos.getX();
        int minY = startPos.getY();
        int maxY = startPos.getY();
        int minZ = startPos.getZ();
        int maxZ = startPos.getZ();

        for (BlockPos monitorPos : getConnectedMonitors()) {
            minX = Math.min(minX, monitorPos.getX());
            maxX = Math.max(maxX, monitorPos.getX());
            minY = Math.min(minY, monitorPos.getY());
            maxY = Math.max(maxY, monitorPos.getY());
            minZ = Math.min(minZ, monitorPos.getZ());
            maxZ = Math.max(maxZ, monitorPos.getZ());
        }

        // Compute the exact center
        double centerX = (minX + maxX) / 2.0 + 0.5;
        double centerY = (minY + maxY) / 2.0 + 0.5;
        double centerZ = (minZ + maxZ) / 2.0 + 0.5;

        System.out.println(new Vec3d(centerX,centerY,centerZ));
        return new Vec3d(centerX, centerY, centerZ);
    }

    private Set<BlockPos> getConnectedMonitors() {
        Set<BlockPos> monitors = new HashSet<>();
        Queue<BlockPos> toCheck = new LinkedList<>();
        toCheck.add(this.pos); // Start from this monitor

        Direction facing = getCachedState().get(MonitorBlock.FACING); // Get block's facing direction

        while (!toCheck.isEmpty()) {
            BlockPos current = toCheck.poll();
            if (!monitors.contains(current)) {
                monitors.add(current);

                // Check all four directions (left, right, up, down)
                for (Direction dir : new Direction[]{
                        Direction.UP, Direction.DOWN,
                        getLeftDirection(facing), getRightDirection(facing)}) {

                    BlockPos neighbor = current.offset(dir);
                    if (world.getBlockState(neighbor).getBlock() instanceof MonitorBlock &&
                            world.getBlockState(neighbor).get(MonitorBlock.FACING) == facing) {

                        toCheck.add(neighbor);
                    }
                }
            }
        }
        return monitors;
    }




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
        double scanRadius = 1.01;
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
        double scanRadius = 1.01;
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
                return true;
            }
        }
        return false;
    }

    public void closePortal(BlockPos monitorPos) {
        // Define the area to search
        double scanRadius = 1;
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
        if(linkedCamera!=null) {
            BlockState state = world.getBlockState(linkedCamera);
            if (state.getBlock() instanceof CameraBlock) {
                return "cblock";
            }
            if (state.getBlock() instanceof CameraBlock) {
                return "camera";
            }
        }
        return null;
    }


    @Override
    public void tick() {
        if (world != null && !world.isClient && linkedCamera != null) {
            if (!isCameraStillValid()) {
                closePortal(pos);
                this.linkedCamera = null;
                BlockState state = world.getBlockState(pos);
                if(state.getBlock() instanceof Monitor){world.setBlockState(pos, state.with(POWERED, false));}
                markDirty();
            }
        }
    }


}

