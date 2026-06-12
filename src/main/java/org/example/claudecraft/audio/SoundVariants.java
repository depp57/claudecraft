package org.example.claudecraft.audio;

import java.util.List;
import java.util.Objects;
import java.util.random.RandomGenerator;

/**
 * A set of interchangeable variants of one sound (e.g. the three grass dig
 * sounds), picked at random but never the same variant twice in a row.
 * Does not own the buffers; the {@link SoundSystem} closes them.
 */
final class SoundVariants {

    private final List<SoundBuffer> variants;
    private int lastIndex = -1;

    SoundVariants(List<SoundBuffer> variants) {
        if (Objects.requireNonNull(variants, "variants").isEmpty()) {
            throw new IllegalArgumentException("variants must not be empty");
        }
        this.variants = List.copyOf(variants);
    }

    /** Picks a random variant, avoiding an immediate repeat when possible. */
    SoundBuffer pick(RandomGenerator random) {
        int index = random.nextInt(variants.size());
        if (index == lastIndex && variants.size() > 1) {
            index = (index + 1) % variants.size();
        }
        lastIndex = index;
        return variants.get(index);
    }
}
