package org.example.claudecraft.render;

import org.example.claudecraft.render.PlayerModelGeometry.PartGeometry;
import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3fc;

import static org.lwjgl.opengl.GL11C.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11C.glClear;

/**
 * Draws the player as the classic blocky humanoid (head, body, two arms, two
 * legs) wearing the bundled Steve skin, with a simple walk-cycle limb swing.
 *
 * <p>Owns its shader, skin texture and part meshes; release them with
 * {@link #close()}. Render thread only.
 */
public final class PlayerModelRenderer implements AutoCloseable {

    private static final int SKIN_TEXTURE_UNIT = 0;
    /** The 32-pixel-tall skin model scaled to exactly the 1.8-block hitbox. */
    private static final float BLOCKS_PER_PIXEL = 1.8f / 32.0f;
    private static final float LEG_SWING_RADIANS = 0.8f;
    private static final float ARM_SWING_RADIANS = 0.55f;
    /** How far the right arm raises at the peak of a click swing. */
    private static final float ATTACK_SWING_RADIANS = 1.2f;

    // First-person arm: rest pose in view space (camera at origin, -Z forward,
    // bottom-right of the screen) and how far the punch carries it.
    private static final float FP_ARM_X = 0.42f;
    private static final float FP_ARM_Y = -0.4f;
    private static final float FP_ARM_Z = -0.7f;
    private static final float FP_ARM_PITCH = 0.6f;
    private static final float FP_ARM_YAW = -0.2f;
    private static final float FP_ARM_ROLL = -0.1f;
    private static final float FP_PUNCH_PITCH = 0.9f;
    private static final float FP_PUNCH_YAW = -0.5f;
    private static final float FP_PUNCH_REACH_X = -0.25f;
    private static final float FP_PUNCH_REACH_Z = -0.18f;

    // Part pivots in skin pixels, relative to the feet center.
    private static final float HIP_Y = 12.0f;
    private static final float SHOULDER_Y = 22.0f;
    private static final float NECK_Y = 24.0f;
    private static final float LEG_X = 2.0f;
    private static final float ARM_X = 6.0f;

    private final ShaderProgram shader;
    private final Texture skin;
    private final Mesh head;
    private final Mesh body;
    private final Mesh rightArm;
    private final Mesh leftArm;
    private final Mesh rightLeg;
    private final Mesh leftLeg;
    /** Scratch matrices reused every frame; rendering must not allocate. */
    private final Matrix4f baseTransform = new Matrix4f();
    private final Matrix4f partTransform = new Matrix4f();

    public PlayerModelRenderer() {
        shader = ShaderProgram.load("/shaders/player.vert", "/shaders/player.frag");
        skin = Texture.loadFromClasspath("/textures/steve-skin.png");
        // Boxes are modeled around their pivot so a single rotation swings them.
        head = mesh(PlayerModelGeometry.box(0, 0, 8, 8, 8, -4, 0, -4, BLOCKS_PER_PIXEL));
        body = mesh(PlayerModelGeometry.box(16, 16, 8, 12, 4, -4, 0, -2, BLOCKS_PER_PIXEL));
        rightArm = mesh(PlayerModelGeometry.box(40, 16, 4, 12, 4, -2, -10, -2, BLOCKS_PER_PIXEL));
        leftArm = mesh(PlayerModelGeometry.box(40, 16, 4, 12, 4, -2, -10, -2, BLOCKS_PER_PIXEL));
        rightLeg = mesh(PlayerModelGeometry.box(0, 16, 4, 12, 4, -2, -12, -2, BLOCKS_PER_PIXEL));
        leftLeg = mesh(PlayerModelGeometry.box(0, 16, 4, 12, 4, -2, -12, -2, BLOCKS_PER_PIXEL));
    }

    private static Mesh mesh(PartGeometry geometry) {
        return new Mesh(geometry.vertices(), geometry.indices());
    }

    /** Draws the model for the given pose under the current sky lighting. */
    public void draw(Matrix4fc viewProjection, Vector3fc sunDirection,
                     float ambient, float sunStrength, PlayerModelState state) {
        shader.bind();
        shader.setUniform("uViewProjection", viewProjection);
        shader.setUniform("uSunDirection", sunDirection);
        shader.setUniform("uAmbient", ambient);
        shader.setUniform("uSunStrength", sunStrength);
        shader.setUniform("uSkin", SKIN_TEXTURE_UNIT);
        skin.bind(SKIN_TEXTURE_UNIT);

        // Model space faces -Z; rotateY(-yaw) turns it to the player's heading.
        baseTransform.translation(state.x(), state.y(), state.z()).rotateY(-state.yaw());
        float legSwing = Math.sin(state.swingPhase()) * LEG_SWING_RADIANS * state.swingAmplitude();
        float armSwing = Math.sin(state.swingPhase()) * ARM_SWING_RADIANS * state.swingAmplitude();
        float punch = punchCurve(state.attackSwing());

        drawPart(body, 0.0f, HIP_Y, 0.0f);
        drawPart(head, 0.0f, NECK_Y, -state.pitch());
        drawPart(rightArm, ARM_X, SHOULDER_Y, -armSwing - punch * ATTACK_SWING_RADIANS);
        drawPart(leftArm, -ARM_X, SHOULDER_Y, armSwing);
        drawPart(rightLeg, LEG_X, HIP_Y, legSwing);
        drawPart(leftLeg, -LEG_X, HIP_Y, -legSwing);

        shader.unbind();
    }

    /**
     * Draws the right arm anchored to the camera at the bottom right of the
     * screen, punching toward the screen center as {@code attackSwing} runs
     * 0…1. Clears the depth buffer so the arm never clips into nearby blocks;
     * call after all world geometry.
     */
    public void drawFirstPersonArm(Matrix4fc projection, Vector3fc sunDirection,
                                   float ambient, float sunStrength, float attackSwing) {
        glClear(GL_DEPTH_BUFFER_BIT);
        shader.bind();
        shader.setUniform("uViewProjection", projection);
        shader.setUniform("uSunDirection", sunDirection);
        shader.setUniform("uAmbient", ambient);
        shader.setUniform("uSunStrength", sunStrength);
        shader.setUniform("uSkin", SKIN_TEXTURE_UNIT);
        skin.bind(SKIN_TEXTURE_UNIT);

        float punch = punchCurve(attackSwing);
        partTransform.translation(
                        FP_ARM_X + FP_PUNCH_REACH_X * punch,
                        FP_ARM_Y,
                        FP_ARM_Z + FP_PUNCH_REACH_Z * punch)
                .rotateX(FP_ARM_PITCH + FP_PUNCH_PITCH * punch)
                .rotateY(FP_ARM_YAW + FP_PUNCH_YAW * punch)
                .rotateZ(FP_ARM_ROLL);
        shader.setUniform("uModel", partTransform);
        rightArm.draw();

        shader.unbind();
    }

    /** Maps swing progress 0…1 to a raise-and-return curve peaking mid-swing. */
    private static float punchCurve(float attackSwing) {
        return Math.sin(attackSwing * (float) java.lang.Math.PI);
    }

    /** Draws one part rotated about the x-axis at its pivot (given in pixels). */
    private void drawPart(Mesh part, float pivotXPixels, float pivotYPixels, float rotationX) {
        partTransform.set(baseTransform)
                .translate(pivotXPixels * BLOCKS_PER_PIXEL, pivotYPixels * BLOCKS_PER_PIXEL, 0.0f)
                .rotateX(rotationX);
        shader.setUniform("uModel", partTransform);
        part.draw();
    }

    @Override
    public void close() {
        head.close();
        body.close();
        rightArm.close();
        leftArm.close();
        rightLeg.close();
        leftLeg.close();
        skin.close();
        shader.close();
    }
}
