package org.example.claudecraft.render;

/**
 * What the renderer draws of the player, depending on the active view:
 * first person shows the view-model arm, third person the whole body.
 * {@code attackSwing} runs 0…1 over one click/mining swing (0 = at rest).
 */
public sealed interface ViewPose {

    record FirstPerson(float attackSwing) implements ViewPose {
    }

    record ThirdPerson(PlayerModelState model) implements ViewPose {
    }
}
