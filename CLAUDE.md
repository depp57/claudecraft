# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Goal

**Claudecraft** is a Minecraft clone written in **Java 26** using **OpenGL (core profile 3.3+) with programmable shaders** via **LWJGL 3**. The goal is a playable voxel sandbox: an infinite, chunk-based world the player can explore, with blocks that can be broken and placed.

### Target Features (in rough implementation order)

1. Window creation, game loop with fixed-timestep updates and interpolated rendering
2. Shader-based rendering pipeline (no fixed-function OpenGL — everything through GLSL vertex/fragment shaders)
3. Chunk-based voxel world (16×16×256 chunks), greedy or face-culled meshing
4. Texture atlas for block textures
5. First-person camera with mouse look and WASD movement
6. Procedural terrain generation (layered noise: heightmap, biomes, caves)
7. Block breaking/placing via raycasting
8. Frustum culling and asynchronous chunk loading/meshing
9. Basic lighting (ambient + directional sun, per-face shading; later: flood-fill block light)
10. Physics: gravity, AABB collision, jumping
11. Day/night cycle, fog, skybox
12. World persistence (region-file or per-chunk serialization)

## Build & Run

```bash
./gradlew build          # compile + test
./gradlew test           # run JUnit 6 tests
./gradlew run            # launch the game (application plugin)
./gradlew test --tests "org.example.claudecraft.world.ChunkTest"   # single test class
```

- Gradle with Kotlin DSL (`build.gradle.kts`). Use the Java toolchain set to **26**; do not rely on the system JDK.
- Dependencies: LWJGL 3 (glfw, opengl, stb, openal modules + natives via the LWJGL BOM), JOML for vector/matrix math, JUnit Jupiter for tests.
- The render thread owns the OpenGL context. **Never call GL functions from worker threads** — worker threads produce mesh data (float/int buffers); the render thread uploads it.

## Architecture

Package root: `org.example.claudecraft`. Keep a strict layering: `core` → `world`/`physics` (pure logic, no OpenGL imports) ← `render` (all GL code). Game logic must be testable without a GPU.

```
org.example.claudecraft
├── core/        Game loop, window (GLFW wrapper), input, time step
├── world/       Block, BlockType, Chunk, World, ChunkColumn — pure data + logic, NO OpenGL
│   └── gen/     TerrainGenerator interface + noise-based implementations
├── render/      Renderer, ShaderProgram, Mesh, TextureAtlas, Camera, Frustum
│   └── chunk/   ChunkMesher (world data → vertex buffers), ChunkRenderer
├── physics/     AABB, raycasting, collision resolution
├── player/      Player state, movement controller, block interaction
└── util/        Resource loading (shaders/textures from classpath), object pools
```

Shaders live in `src/main/resources/shaders/*.vert|*.frag`; textures in `src/main/resources/textures/`. Load resources from the classpath, never by filesystem path.

## Java Best Practices (mandatory)

Follow *Effective Java* idioms and modern Java 26 features:

- **Encapsulation first**: fields are `private final` wherever possible; expose behavior, not state. No public mutable fields, no getter/setter pairs that just leak representation.
- **Immutability by default**: use `record` for value types (`BlockPos`, `ChunkPos`, `AABB`, vertex data carriers). Mutable classes only where performance demands it (e.g., `Chunk` block storage, reused `Matrix4f` instances).
- **Sealed hierarchies + pattern matching**: model closed sets (e.g., block render layers, generator stages) as `sealed interface` with record implementations, dispatched via `switch` pattern matching — not `instanceof` chains.
- **Composition over inheritance**: prefer interfaces + delegation. Inheritance only for genuine is-a relationships with documented contracts; otherwise mark classes `final`.
- **Program to interfaces**: `World`, `TerrainGenerator`, `Mesher` are interfaces; concrete classes are implementation details. Constructor injection for dependencies — no singletons, no static mutable state.
- **Resource management**: every GL object (VAO, VBO, texture, shader program) is wrapped in a class implementing `AutoCloseable` with explicit lifecycle; use try-with-resources for short-lived natives (`MemoryStack`) and deterministic `close()` for long-lived ones. Never rely on finalizers/cleaners for GL handles.
- **Null discipline**: no `null` returns from public APIs — use `Optional` for "maybe" results, empty collections otherwise. Validate constructor/method arguments with `Objects.requireNonNull` and explicit range checks; fail fast.
- **Exceptions**: unchecked exceptions for programming errors, dedicated exception types (`ShaderCompileException`, `ResourceLoadException`) for recoverable resource failures. Never swallow exceptions.
- **Concurrency**: chunk generation/meshing on an `ExecutorService` (virtual threads are fine for IO, platform threads for CPU-bound meshing); hand results to the render thread via a thread-safe queue. Document thread ownership in Javadoc on every class touching concurrency.
- **Performance-aware, not premature**: per-frame hot paths (meshing, rendering) must avoid allocation — reuse buffers and JOML objects there. Everywhere else, prefer clarity; measure before optimizing.
- **Style**: standard Java conventions, one top-level class per file, Javadoc on all public types and non-obvious methods. Keep methods short and single-purpose.

## Testing

- JUnit Jupiter for all of `world`, `physics`, `gen`, and meshing logic — these must run headless (no GL context).
- Test meshing by asserting on produced vertex data, terrain generation by seeding the noise and asserting determinism, collision by table-driven AABB cases.
- Rendering code is verified manually via `./gradlew run`; keep it thin so the untestable surface stays small.
