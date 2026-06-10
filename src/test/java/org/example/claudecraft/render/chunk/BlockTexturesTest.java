package org.example.claudecraft.render.chunk;

import org.example.claudecraft.world.BlockType;
import org.example.claudecraft.world.Direction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BlockTexturesTest {

    @Test
    void grassUsesTopSideAndDirtTiles() {
        TextureTile top = BlockTextures.tileFor(BlockType.GRASS, Direction.UP);
        TextureTile side = BlockTextures.tileFor(BlockType.GRASS, Direction.NORTH);
        TextureTile bottom = BlockTextures.tileFor(BlockType.GRASS, Direction.DOWN);

        assertNotEquals(top, side);
        assertNotEquals(side, bottom);
        assertEquals(BlockTextures.tileFor(BlockType.DIRT, Direction.UP), bottom);

        for (Direction side2 : new Direction[] {Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST}) {
            assertEquals(side, BlockTextures.tileFor(BlockType.GRASS, side2));
        }
    }

    @Test
    void uniformBlocksUseSameTileOnAllFaces() {
        for (BlockType block : new BlockType[] {BlockType.DIRT, BlockType.STONE}) {
            TextureTile expected = BlockTextures.tileFor(block, Direction.UP);
            for (Direction face : Direction.values()) {
                assertEquals(expected, BlockTextures.tileFor(block, face), block + " face " + face);
            }
        }
    }

    @Test
    void airHasNoTiles() {
        assertThrows(IllegalArgumentException.class, () -> BlockTextures.tileFor(BlockType.AIR, Direction.UP));
    }
}
