package org.example.claudecraft.world;

import org.joml.Vector3f;

/**
 * Tracks the time of day and derives the sun's geometry from it. The sun
 * orbits east → overhead → west: day fraction 0 is sunrise on the eastern
 * horizon (+X), 0.25 noon, 0.5 sunset, 0.75 midnight.
 *
 * <p>Pure math; presentation (colors, light strengths) lives in the renderer.
 */
public final class DayNightCycle {

    /** Constant z-lean of the sun's orbital plane so noon light is not perfectly vertical. */
    private static final float SUN_TILT = 0.3f;
    /** Daylight ramps in/out while the sun crosses the horizon band. */
    private static final float TWILIGHT_START = -0.08f;
    private static final float TWILIGHT_END = 0.2f;

    private final float dayLengthSeconds;
    private float timeSeconds;

    public DayNightCycle(float dayLengthSeconds, float startFraction) {
        if (dayLengthSeconds <= 0.0f) {
            throw new IllegalArgumentException("dayLengthSeconds must be positive: " + dayLengthSeconds);
        }
        if (startFraction < 0.0f || startFraction >= 1.0f) {
            throw new IllegalArgumentException("startFraction must be in [0, 1): " + startFraction);
        }
        this.dayLengthSeconds = dayLengthSeconds;
        this.timeSeconds = startFraction * dayLengthSeconds;
    }

    /** Advances the clock, wrapping at the end of the day. */
    public void update(float dt) {
        timeSeconds = (timeSeconds + dt) % dayLengthSeconds;
    }

    /** Position within the day in {@code [0, 1)}; 0 sunrise, 0.25 noon. */
    public float dayFraction() {
        return timeSeconds / dayLengthSeconds;
    }

    /**
     * How much daylight there is, in {@code [0, 1]}: 1 through the day,
     * 0 at night, ramping smoothly through twilight.
     */
    public float daylight() {
        return smoothstep(TWILIGHT_START, TWILIGHT_END, sunHeight());
    }

    /**
     * Writes the normalized direction sunlight travels (from sun into the
     * world) into {@code dest} and returns it. Below the horizon it keeps
     * rotating (light would come from below); callers fade the sun's
     * contribution with {@link #daylight()} instead.
     */
    public Vector3f sunDirection(Vector3f dest) {
        float angle = (float) (dayFraction() * 2.0 * Math.PI);
        // Sun position on its unit orbit; light travels the opposite way.
        return dest.set(org.joml.Math.cos(angle), org.joml.Math.sin(angle), SUN_TILT)
                .normalize()
                .negate();
    }

    /** Sine of the sun's elevation: 1 at noon, negative below the horizon. */
    private float sunHeight() {
        return org.joml.Math.sin((float) (dayFraction() * 2.0 * Math.PI));
    }

    private static float smoothstep(float edge0, float edge1, float x) {
        float t = Math.clamp((x - edge0) / (edge1 - edge0), 0.0f, 1.0f);
        return t * t * (3.0f - 2.0f * t);
    }
}
