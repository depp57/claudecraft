package org.example.claudecraft.core;

/**
 * A game driven by {@link GameLoop}: fixed-timestep simulation updates and
 * interpolated rendering. Both methods are invoked on the render thread.
 */
public interface Game {

    /**
     * Advances the simulation by one fixed step.
     *
     * @param dt step duration in seconds, constant across calls
     */
    void update(float dt);

    /**
     * Renders one frame.
     *
     * @param alpha interpolation factor in {@code [0, 1)}: how far the current
     *              moment lies between the last update and the next one
     */
    void render(float alpha);
}
