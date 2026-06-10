package org.example.claudecraft.player;

import org.example.claudecraft.core.Input;
import org.example.claudecraft.physics.AABB;
import org.example.claudecraft.physics.RayHit;
import org.example.claudecraft.physics.VoxelRaycaster;
import org.example.claudecraft.world.BlockChangeListener;
import org.example.claudecraft.world.BlockType;
import org.example.claudecraft.world.World;
import org.joml.Vector3f;

import java.util.Objects;
import java.util.Optional;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT;

/**
 * Breaking and placing blocks: left click breaks the targeted block, right
 * click places one against the targeted face — unless it would overlap the
 * player. Changes are reported to the {@link BlockChangeListener} so the
 * renderer can remesh.
 */
public final class BlockInteraction {

    private static final float REACH_BLOCKS = 6.0f;
    /** Placeholder until a hotbar/inventory exists. */
    private static final BlockType PLACED_BLOCK = BlockType.STONE;

    private final World world;
    private final Player player;
    private final Input input;
    private final BlockChangeListener changeListener;
    /** Scratch vector reused every tick; update is a hot path and must not allocate. */
    private final Vector3f lookDirection = new Vector3f();

    public BlockInteraction(World world, Player player, Input input, BlockChangeListener changeListener) {
        this.world = Objects.requireNonNull(world, "world");
        this.player = Objects.requireNonNull(player, "player");
        this.input = Objects.requireNonNull(input, "input");
        this.changeListener = Objects.requireNonNull(changeListener, "changeListener");
    }

    /** Handles one simulation tick of click input. */
    public void update() {
        boolean wantBreak = input.isButtonJustPressed(GLFW_MOUSE_BUTTON_LEFT);
        boolean wantPlace = input.isButtonJustPressed(GLFW_MOUSE_BUTTON_RIGHT);
        if (!wantBreak && !wantPlace) {
            return;
        }

        Optional<RayHit> hit = VoxelRaycaster.raycast(
                world, player.eyePosition(), player.lookDirection(lookDirection), REACH_BLOCKS);
        if (hit.isEmpty()) {
            return;
        }
        if (wantBreak) {
            breakBlock(hit.get());
        } else {
            placeBlock(hit.get());
        }
    }

    private void breakBlock(RayHit hit) {
        if (world.setBlock(hit.x(), hit.y(), hit.z(), BlockType.AIR)) {
            changeListener.onBlockChanged(hit.x(), hit.y(), hit.z());
        }
    }

    private void placeBlock(RayHit hit) {
        int x = hit.x() + hit.face().dx();
        int y = hit.y() + hit.face().dy();
        int z = hit.z() + hit.face().dz();
        if (world.block(x, y, z) != BlockType.AIR) {
            return;
        }
        if (AABB.blockAt(x, y, z).intersects(player.boundingBox())) {
            return; // would trap the player inside the new block
        }
        if (world.setBlock(x, y, z, PLACED_BLOCK)) {
            changeListener.onBlockChanged(x, y, z);
        }
    }
}
