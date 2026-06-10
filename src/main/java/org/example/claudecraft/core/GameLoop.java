package org.example.claudecraft.core;

import java.util.Objects;

/**
 * Fixed-timestep game loop: simulation updates run at a constant rate while
 * rendering runs as fast as the swap interval allows, with an interpolation
 * factor passed to {@link Game#render(float)}.
 *
 * <p>Runs entirely on the calling (render) thread.
 */
public final class GameLoop {

    private static final double UPDATES_PER_SECOND = 60.0;
    private static final double STEP_SECONDS = 1.0 / UPDATES_PER_SECOND;
    /** Cap on per-frame simulation time so a long stall cannot spiral into endless catch-up updates. */
    private static final double MAX_FRAME_SECONDS = 0.25;

    private final Window window;
    private final Game game;

    public GameLoop(Window window, Game game) {
        this.window = Objects.requireNonNull(window, "window");
        this.game = Objects.requireNonNull(game, "game");
    }

    /** Runs until the window is flagged to close. */
    public void run() {
        long previousNanos = System.nanoTime();
        double accumulatorSeconds = 0.0;

        while (!window.shouldClose()) {
            long nowNanos = System.nanoTime();
            double frameSeconds = (nowNanos - previousNanos) / 1_000_000_000.0;
            previousNanos = nowNanos;
            accumulatorSeconds = Math.min(accumulatorSeconds + frameSeconds, MAX_FRAME_SECONDS);

            while (accumulatorSeconds >= STEP_SECONDS) {
                game.update((float) STEP_SECONDS);
                accumulatorSeconds -= STEP_SECONDS;
            }

            game.render((float) (accumulatorSeconds / STEP_SECONDS));
            window.update();
        }
    }
}
