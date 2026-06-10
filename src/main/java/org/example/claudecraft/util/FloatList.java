package org.example.claudecraft.util;

import java.util.Arrays;

/** Growable primitive float array; avoids boxing in the meshing hot path. */
public final class FloatList {

    private float[] data;
    private int size;

    public FloatList(int initialCapacity) {
        if (initialCapacity <= 0) {
            throw new IllegalArgumentException("initialCapacity must be positive: " + initialCapacity);
        }
        data = new float[initialCapacity];
    }

    public void add(float value) {
        if (size == data.length) {
            data = Arrays.copyOf(data, data.length * 2);
        }
        data[size++] = value;
    }

    public int size() {
        return size;
    }

    /** Returns a copy trimmed to the current size. */
    public float[] toArray() {
        return Arrays.copyOf(data, size);
    }
}
