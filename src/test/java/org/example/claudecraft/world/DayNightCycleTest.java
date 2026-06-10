package org.example.claudecraft.world;

import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DayNightCycleTest {

    private static final float EPSILON = 1e-5f;

    @Test
    void noonIsFullDaylightWithSunOverhead() {
        DayNightCycle cycle = new DayNightCycle(100.0f, 0.25f);

        assertEquals(1.0f, cycle.daylight(), EPSILON);
        Vector3f sun = cycle.sunDirection(new Vector3f());
        assertTrue(sun.y < -0.9f, "noon light travels downward, got " + sun);
        assertEquals(1.0f, sun.length(), EPSILON);
    }

    @Test
    void midnightIsDark() {
        DayNightCycle cycle = new DayNightCycle(100.0f, 0.75f);
        assertEquals(0.0f, cycle.daylight(), EPSILON);
    }

    @Test
    void sunriseLightComesFromTheEast() {
        DayNightCycle cycle = new DayNightCycle(100.0f, 0.0f);
        Vector3f sun = cycle.sunDirection(new Vector3f());
        assertTrue(sun.x < -0.9f, "sun on the eastern horizon (+x) sends light toward -x, got " + sun);
    }

    @Test
    void timeWrapsAtTheEndOfTheDay() {
        DayNightCycle cycle = new DayNightCycle(10.0f, 0.95f);
        cycle.update(1.0f);
        assertEquals(0.05f, cycle.dayFraction(), EPSILON);
    }

    @Test
    void rejectsInvalidConfiguration() {
        assertThrows(IllegalArgumentException.class, () -> new DayNightCycle(0.0f, 0.0f));
        assertThrows(IllegalArgumentException.class, () -> new DayNightCycle(100.0f, 1.0f));
    }
}
