package com.silverminer.shrines.test;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.presets.WorldPresets;

import java.nio.file.Files;

// Run with -Dsmoketest=true: creates a world, places a few structures, screenshots each and quits. Exit code 1 on failure.
public final class ClientSmoke {
	public static final boolean ENABLED = Boolean.getBoolean("smoketest");
	private static final long TIMEOUT_MS = 8 * 60 * 1000;
	private static final String[] SHOTS = {"small_temple", "jungle_tower", "watch_tower", "luxury_villa", "balloon"};

	private enum State { WAIT_TITLE, WAIT_WORLD, SHOOT, DONE }

	private static State state = State.WAIT_TITLE;
	private static int ticks;
	private static int shot;
	private static final java.util.concurrent.atomic.AtomicBoolean PLACED = new java.util.concurrent.atomic.AtomicBoolean();
	private static net.minecraft.core.BlockPos land;

	private ClientSmoke() {
	}

	public static boolean start() {
		if (!ENABLED) return false;
		Thread watchdog = new Thread(() -> {
			try {
				Thread.sleep(TIMEOUT_MS);
			} catch (InterruptedException e) {
				return;
			}
			fail("timed out in state " + state);
		}, "clientsmoke-watchdog");
		watchdog.setDaemon(true);
		watchdog.start();
		return true;
	}

	public static void tick(Minecraft mc) {
		try {
			switch (state) {
				case WAIT_TITLE -> {
					Screen screen = SmokeCompat.screen(mc);
					if (screen != null && SmokeCompat.overlayHidden(mc) && ++ticks >= 20) {
						if (screen.getClass().getSimpleName().equals("LoadingErrorScreen")) fail("mod loading errors or warnings, see the log");
						ticks = 0;
						SmokeCompat.createWorld(mc, GameType.CREATIVE, WorldPresets.NORMAL, 1L, false);
						state = State.WAIT_WORLD;
					}
				}
				case WAIT_WORLD -> {
					if (mc.level != null && mc.player != null && SmokeCompat.screen(mc) == null) state = State.SHOOT;
				}
				case SHOOT -> {
					ticks++;
					if (ticks == 40) {
						PLACED.set(false);
						place(mc, SHOTS[shot], shot);
					}
					// the server places synchronously; give the client 8 s after that to receive and draw the chunks
					if (ticks > 40 && !PLACED.get()) ticks = 41;
					if (ticks == 200) {
						SmokeCompat.screenshot(mc, "shrines-" + SHOTS[shot] + ".png");
						ticks = 0;
						if (++shot == SHOTS.length) {
							SmokeCompat.hideGui(mc, false);
							Files.writeString(mc.gameDirectory.toPath().resolve("smoke-ok"), "ok\n");
							System.out.println("[clientsmoke] PASSED");
							state = State.DONE;
							mc.stop();
						}
					}
				}
				case DONE -> {}
			}
		} catch (Throwable t) {
			t.printStackTrace();
			fail(t.toString());
		}
	}


	// place the structure 600 blocks apart each time, then look at it from the south-west, slightly above
	private static void place(Minecraft mc, String id, int index) {
		var server = mc.getSingleplayerServer();
		server.execute(() -> {
			var level = server.overworld();
			if (land == null) {
				// line the structures up in the nearest plains so they stand on land
				var found = level.findClosestBiome3d(b -> b.is(net.minecraft.world.level.biome.Biomes.PLAINS), net.minecraft.core.BlockPos.ZERO, 6400, 32, 64);
				land = found == null ? new net.minecraft.core.BlockPos(2000, 64, 2000) : found.getFirst();
			}
			int x = land.getX() + index * 160;
			int z = land.getZ();
			var source = server.createCommandSourceStack().withSuppressedOutput();
			var commands = server.getCommands();
			commands.performPrefixedCommand(source, "time set 1000");
			commands.performPrefixedCommand(source, "weather clear");
			commands.performPrefixedCommand(source, String.format("forceload add %d %d %d %d", x - 64, z - 64, x + 64, z + 64));
			int y = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
			try {
				int result = commands.getDispatcher().execute(String.format("place structure shrines:%s %d %d %d", id, x, y, z), source);
				if (result <= 0) fail("could not place " + id);
			} catch (Exception e) {
				fail("could not place " + id + ": " + e.getMessage());
			}
			var player = server.getPlayerList().getPlayers().get(0);
			player.setGameMode(net.minecraft.world.level.GameType.SPECTATOR);
			//? if >=1.21.11 {
			/*player.teleportTo(level, x - 24.5, y + 16, z - 24.5, java.util.Set.of(), -45, 25, true);
			*///?} else {
			player.teleportTo(level, x - 24.5, y + 16, z - 24.5, -45, 25);
			//?}
			System.out.println("[clientsmoke] placed " + id + " at " + x + " " + y + " " + z);
			PLACED.set(true);
		});
		SmokeCompat.hideGui(mc, true);
	}

	public static void fail(String why) {
		System.err.println("[clientsmoke] FAILED: " + why);
		System.err.flush();
		Runtime.getRuntime().halt(1);
	}
}
