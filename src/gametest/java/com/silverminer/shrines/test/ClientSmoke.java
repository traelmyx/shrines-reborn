package com.silverminer.shrines.test;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldOptions;
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
					Screen screen = mc.screen;
					if (screen != null && mc.getOverlay() == null && ++ticks >= 20) {
						if (screen.getClass().getSimpleName().equals("LoadingErrorScreen")) fail("mod loading errors or warnings, see the log");
						ticks = 0;
						createWorld(mc);
						state = State.WAIT_WORLD;
					}
				}
				case WAIT_WORLD -> {
					if (mc.level != null && mc.player != null && mc.screen == null) state = State.SHOOT;
				}
				case SHOOT -> {
					ticks++;
					if (ticks == 40) place(mc, SHOTS[shot], shot);
					if (ticks == 240) {
						Screenshot.grab(mc.gameDirectory, "shrines-" + SHOTS[shot] + ".png", mc.getMainRenderTarget(), msg -> {});
						ticks = 0;
						if (++shot == SHOTS.length) {
							mc.options.hideGui = false;
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

	private static void createWorld(Minecraft mc) {
		LevelSettings settings = new LevelSettings("smoke", GameType.CREATIVE, false, Difficulty.PEACEFUL,
			true, new GameRules(), WorldDataConfiguration.DEFAULT);
		mc.createWorldOpenFlows().createFreshLevel("smoke", settings, new WorldOptions(1L, false, false),
			registries -> registries.registryOrThrow(Registries.WORLD_PRESET)
				.getHolderOrThrow(WorldPresets.NORMAL).value().createWorldDimensions(),
			mc.screen);
	}

	// place the structure 600 blocks apart each time, then look at it from the south-west, slightly above
	private static void place(Minecraft mc, String id, int index) {
		var server = mc.getSingleplayerServer();
		int x = 2000 + index * 600;
		int z = 2000;
		server.execute(() -> {
			var level = server.overworld();
			var source = server.createCommandSourceStack().withSuppressedOutput();
			var commands = server.getCommands();
			commands.performPrefixedCommand(source, "time set 1000");
			commands.performPrefixedCommand(source, "weather clear");
			commands.performPrefixedCommand(source, String.format("forceload add %d %d %d %d", x - 120, z - 120, x + 120, z + 120));
			int y = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
			try {
				int result = commands.getDispatcher().execute(String.format("place structure shrines:%s %d %d %d", id, x, y, z), source);
				if (result <= 0) fail("could not place " + id);
			} catch (Exception e) {
				fail("could not place " + id + ": " + e.getMessage());
			}
			var player = server.getPlayerList().getPlayers().get(0);
			player.setGameMode(net.minecraft.world.level.GameType.SPECTATOR);
			player.teleportTo(level, x - 24.5, y + 16, z - 24.5, -45, 25);
			System.out.println("[clientsmoke] placed " + id + " at " + x + " " + y + " " + z);
		});
		mc.options.hideGui = true;
	}

	public static void fail(String why) {
		System.err.println("[clientsmoke] FAILED: " + why);
		System.err.flush();
		Runtime.getRuntime().halt(1);
	}
}
