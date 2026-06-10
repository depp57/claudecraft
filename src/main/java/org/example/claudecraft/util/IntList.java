package org.example.claudecraft.util;

import java.util.Arrays;

/** Growable primitive int array; avoids boxing in the meshing hot path. */
public final class IntList {

    private int[] data;
    private int size;

    public IntList(int initialCapacity) {
        if (initialCapacity <= 0) {
            throw new IllegalArgumentException("initialCapacity must be positive: " + initialCapacity);
        }
        data = new int[initialCapacity];
    }

    public void add(int value) {
        if (size == data.length) {
            data = Arrays.copyOf(data, data.length * 2);
        }
        data[size++] = value;
    }

    public int size() {
        return size;
    }

    /** Returns a copy trimmed to the current size. */
    public int[] toArray() {
        return Arrays.copyOf(data, size);
    }
}
