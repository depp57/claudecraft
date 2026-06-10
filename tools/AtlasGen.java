import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

/**
 * Generates src/main/resources/textures/atlas.png: a 4x4 grid of 16x16 tiles.
 * Row 0: grass top, grass side, dirt, stone. Unused tiles get the classic
 * magenta/black missing-texture checkerboard.
 *
 * <p>Deterministic (fixed seed); rerun after changing tile definitions:
 * {@code java tools/AtlasGen.java}
 */
public final class AtlasGen {

    private static final int TILE = 16;
    private static final int GRID = 4;

    private static final Random RNG = new Random(42);

    public static void main(String[] args) throws IOException {
        BufferedImage atlas = new BufferedImage(TILE * GRID, TILE * GRID, BufferedImage.TYPE_INT_RGB);

        speckle(atlas, 0, 0, 0x6AAA40, 0.08f);   // grass top
        grassSide(atlas, 1, 0);                  // grass side
        speckle(atlas, 2, 0, 0x866043, 0.10f);   // dirt
        speckle(atlas, 3, 0, 0x7D7D7D, 0.07f);   // stone

        for (int tileY = 0; tileY < GRID; tileY++) {
            for (int tileX = 0; tileX < GRID; tileX++) {
                if (tileY == 0) {
                    continue;
                }
                missing(atlas, tileX, tileY);
            }
        }

        File out = new File("src/main/resources/textures/atlas.png");
        out.getParentFile().mkdirs();
        ImageIO.write(atlas, "png", out);
        System.out.println("Wrote " + out + " (" + atlas.getWidth() + "x" + atlas.getHeight() + ")");
    }

    /** Fills a tile with the base color, each pixel's brightness jittered by ±variation. */
    private static void speckle(BufferedImage img, int tileX, int tileY, int rgb, float variation) {
        for (int y = 0; y < TILE; y++) {
            for (int x = 0; x < TILE; x++) {
                img.setRGB(tileX * TILE + x, tileY * TILE + y, jitter(rgb, variation));
            }
        }
    }

    /** Dirt tile with a grass band along the top edge. */
    private static void grassSide(BufferedImage img, int tileX, int tileY) {
        for (int y = 0; y < TILE; y++) {
            for (int x = 0; x < TILE; x++) {
                boolean grass = y < 3 || (y == 3 && RNG.nextBoolean());
                int base = grass ? 0x6AAA40 : 0x866043;
                img.setRGB(tileX * TILE + x, tileY * TILE + y, jitter(base, grass ? 0.08f : 0.10f));
            }
        }
    }

    /** Magenta/black checkerboard marking unassigned atlas slots. */
    private static void missing(BufferedImage img, int tileX, int tileY) {
        for (int y = 0; y < TILE; y++) {
            for (int x = 0; x < TILE; x++) {
                boolean magenta = ((x / 8) + (y / 8)) % 2 == 0;
                img.setRGB(tileX * TILE + x, tileY * TILE + y, magenta ? 0xF800F8 : 0x000000);
            }
        }
    }

    private static int jitter(int rgb, float variation) {
        float factor = 1.0f + (RNG.nextFloat() * 2.0f - 1.0f) * variation;
        int r = clamp((int) (((rgb >> 16) & 0xFF) * factor));
        int g = clamp((int) (((rgb >> 8) & 0xFF) * factor));
        int b = clamp((int) ((rgb & 0xFF) * factor));
        return (r << 16) | (g << 8) | b;
    }

    private static int clamp(int channel) {
        return Math.min(255, Math.max(0, channel));
    }
}
