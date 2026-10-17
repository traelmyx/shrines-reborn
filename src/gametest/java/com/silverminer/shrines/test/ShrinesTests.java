package com.silverminer.shrines.test;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public final class ShrinesTests {
	public static final String NAMESPACE = "shrines";

	private ShrinesTests() {
	}

	private static String id(ResourceKey<?> key) {
		//? if >=1.21.11 {
		/*return key.identifier().toString();
		*///?} else {
		return key.location().toString();
		//?}
	}

	private static boolean ours(ResourceKey<?> key) {
		return id(key).startsWith(NAMESPACE + ":");
	}

	public static final int PLACE_SPACING_TICKS = 40;

	/** Every structure in the mod generates with /place structure, one every couple of seconds so unloaded chunks get freed. */
	public static void allStructuresPlace(GameTestHelper helper) {
		MinecraftServer server = helper.getLevel().getServer();
		ServerLevel level = helper.getLevel();
		//? if >=1.21.11 {
		/*var structures = level.registryAccess().lookupOrThrow(Registries.STRUCTURE);
		*///?} else {
		var structures = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
		//?}
		List<String> ids = structures.keySet().stream()
			.filter(id -> id.getNamespace().equals(NAMESPACE)).map(Object::toString).sorted().toList();
		helper.assertTrue(ids.size() >= 30, "expected 30+ shrines structures, found " + ids.size());
		CommandSourceStack source = server.createCommandSourceStack().withLevel(level).withSuppressedOutput();
		List<String> failed = new ArrayList<>();
		for (int i = 0; i < ids.size(); i++) {
			String id = ids.get(i);
			int x = 20000 + i * 400;
			helper.runAfterDelay(1 + (long) i * PLACE_SPACING_TICKS, () -> {
				String error = place(server, source, id, x, 64);
				if (error != null && error.contains("not loaded")) error = place(server, source, id, x, 112);
				if (error != null) failed.add(id + " (" + error + ")");
			});
		}
		helper.runAfterDelay(2 + (long) ids.size() * PLACE_SPACING_TICKS, () -> {
			helper.assertTrue(failed.isEmpty(), "structures failed to place: " + failed);
			helper.succeed();
		});
	}

	// /place needs every chunk the structure covers to be loaded
	private static String place(MinecraftServer server, CommandSourceStack source, String id, int x, int radius) {
		var dispatcher = server.getCommands().getDispatcher();
		try {
			dispatcher.execute(String.format("forceload add %d %d %d %d", x - radius, 20000 - radius, x + radius, 20000 + radius), source);
			int result = dispatcher.execute(String.format("place structure %s %d 0 20000", id, x), source);
			return result > 0 ? null : "result " + result;
		} catch (Exception e) {
			return e.getMessage();
		} finally {
			try {
				dispatcher.execute("forceload remove all", source);
			} catch (Exception ignored) {
			}
		}
	}

	/** Every structure piece referenced by the mod loads. */
	public static void templatesLoad(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		//? if >=26.3 {
		/*var manager = level.getStructureTemplateManager();
		*///?} else
		var manager = level.getStructureManager();
		var ids = manager.listTemplates().filter(id -> id.getNamespace().equals(NAMESPACE)).toList();
		helper.assertTrue(ids.size() > 50, "too few shrines templates: " + ids.size());
		List<String> bad = new ArrayList<>();
		for (var id : ids) {
			var template = manager.get(id);
			if (template.isEmpty() || template.get().getSize().equals(net.minecraft.core.Vec3i.ZERO)) bad.add(id.toString());
		}
		helper.assertTrue(bad.isEmpty(), "empty or broken templates: " + bad);
		helper.succeed();
	}

	/** Every loot table parses and the chest tables roll items. */
	public static void lootTablesRoll(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		MinecraftServer server = level.getServer();
		//? if >=1.21.11 {
		/*List<ResourceKey<LootTable>> keys = server.reloadableRegistries().lookup().lookupOrThrow(Registries.LOOT_TABLE).listElementIds()
			.filter(ShrinesTests::ours).toList();
		*///?} else {
		List<ResourceKey<LootTable>> keys = server.reloadableRegistries().getKeys(Registries.LOOT_TABLE).stream()
			.filter(id -> id.getNamespace().equals(NAMESPACE))
			.map(id -> ResourceKey.create(Registries.LOOT_TABLE, id)).toList();
		//?}
		helper.assertTrue(keys.size() >= 20, "expected 20+ shrines loot tables, found " + keys.size());
		List<String> empty = new ArrayList<>();
		BlockPos pos = helper.absolutePos(BlockPos.ZERO);
		for (ResourceKey<LootTable> key : keys) {
			LootTable table = server.reloadableRegistries().getLootTable(key);
			if (table == LootTable.EMPTY) {
				empty.add(id(key) + " (missing)");
				continue;
			}
			if (!id(key).startsWith(NAMESPACE + ":chests/")) continue;
			int items = 0;
			for (int roll = 0; roll < 5; roll++) {
				LootParams params = new LootParams.Builder(level).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos)).create(LootContextParamSets.CHEST);
				for (ItemStack stack : table.getRandomItems(params)) items += stack.isEmpty() ? 0 : 1;
			}
			if (items == 0) empty.add(id(key) + " (rolled nothing)");
		}
		helper.assertTrue(empty.isEmpty(), "loot tables: " + empty);
		helper.succeed();
	}

	/** Structures only spawn in the biomes they're tagged for. */
	public static void biomeTagsResolve(GameTestHelper helper) {
		//? if >=1.21.11 {
		/*var registry = helper.getLevel().registryAccess().lookupOrThrow(Registries.STRUCTURE);
		*///?} else {
		var registry = helper.getLevel().registryAccess().registryOrThrow(Registries.STRUCTURE);
		//?}
		List<String> none = new ArrayList<>();
		for (var entry : registry.entrySet()) {
			if (!ours(entry.getKey())) continue;
			Structure structure = entry.getValue();
			if (structure.biomes().size() == 0) none.add(id(entry.getKey()));
		}
		helper.assertTrue(none.isEmpty(), "structures with no biomes: " + none);
		helper.succeed();
	}
}
