package org.example.claudecraft.player;

import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlayerTest {

    private static final float EPSILON = 1e-6f;

    @Test
    void turnAccumulatesYaw() {
        Player player = new Player(new Vector3f(0, 0, 0));
        player.turn(0.5f, 0.0f);
        player.turn(0.25f, 0.0f);
        assertEquals(0.75f, player.yaw(), EPSILON);
    }

    @Test
    void pitchIsClampedShortOfVertical() {
        Player player = new Player(new Vector3f(0, 0, 0));
        float maxPitch = (float) Math.toRadians(89.0);

        player.turn(0.0f, 10.0f);
        assertEquals(maxPitch, player.pitch(), EPSILON);

        player.turn(0.0f, -20.0f);
        assertEquals(-maxPitch, player.pitch(), EPSILON);
    }

    @Test
    void translateMovesPosition() {
        Player player = new Player(new Vector3f(1.0f, 2.0f, 3.0f));
        player.translate(0.5f, -1.0f, 2.0f);

        assertEquals(1.5f, player.position().x(), EPSILON);
        assertEquals(1.0f, player.position().y(), EPSILON);
        assertEquals(5.0f, player.position().z(), EPSILON);
    }

    @Test
    void lookDirectionFollowsYawAndPitch() {
        Player player = new Player(new Vector3f(0, 0, 0));
        Vector3f direction = new Vector3f();

        player.lookDirection(direction);
        assertEquals(0.0f, direction.x, EPSILON);
        assertEquals(0.0f, direction.y, EPSILON);
        assertEquals(-1.0f, direction.z, EPSILON);

        player.turn((float) Math.toRadians(90.0), 0.0f);
        player.lookDirection(direction);
        assertEquals(1.0f, direction.x, 1e-5f);
        assertEquals(0.0f, direction.z, 1e-5f);

        player.turn(0.0f, (float) Math.toRadians(45.0));
        player.lookDirection(direction);
        assertEquals(-Math.sin(Math.toRadians(45.0)), direction.y, 1e-5f);
        assertEquals(1.0f, direction.length(), 1e-5f);
    }

    @Test
    void eyeSitsAboveFeetAndBoxMatchesDimensions() {
        Player player = new Player(new Vector3f(8.5f, 65.0f, 8.5f));

        assertEquals(65.0f + Player.EYE_HEIGHT, player.eyePosition().y(), EPSILON);
        assertEquals(8.5f, player.eyePosition().x(), EPSILON);

        var box = player.boundingBox();
        assertEquals(Player.WIDTH, box.maxX() - box.minX(), 1e-5f);
        assertEquals(Player.HEIGHT, box.maxY() - box.minY(), 1e-5f);
        assertEquals(65.0f, box.minY(), EPSILON);
    }

    @Test
    void spawnPositionIsCopiedNotAliased() {
        Vector3f spawn = new Vector3f(1.0f, 2.0f, 3.0f);
        Player player = new Player(spawn);
        spawn.set(9.0f, 9.0f, 9.0f);

        assertEquals(1.0f, player.position().x(), EPSILON);
    }
}
