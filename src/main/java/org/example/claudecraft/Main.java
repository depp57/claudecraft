package org.example.claudecraft;

import org.example.claudecraft.core.GameLoop;
import org.example.claudecraft.core.Window;

/** Application entry point. */
public final class Main {

    private static final String TITLE = "Claudecraft";
    private static final int WIDTH = 1280;
    private static final int HEIGHT = 720;

    private Main() {
    }

    public static void main(String[] args) {
        try (Window window = new Window(TITLE, WIDTH, HEIGHT);
             ClaudecraftGame game = new ClaudecraftGame(window)) {
            new GameLoop(window, game).run();
        }
    }
}
