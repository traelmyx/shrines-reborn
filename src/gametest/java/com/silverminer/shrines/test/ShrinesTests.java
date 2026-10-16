package com.silverminer.shrines.test;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
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

	/** Every structure in the mod generates with /place structure, far away from the test area. */
	public static void allStructuresPlace(GameTestHelper helper) {
		MinecraftServer server = helper.getLevel().getServer();
		ServerLevel level = helper.getLevel();
		List<ResourceLocation> ids = level.registryAccess().registryOrThrow(Registries.STRUCTURE).keySet().stream()
			.filter(id -> id.getNamespace().equals(NAMESPACE)).sorted().toList();
		helper.assertTrue(ids.size() >= 30, "expected 30+ shrines structures, found " + ids.size());
		CommandSourceStack source = server.createCommandSourceStack().withLevel(level).withSuppressedOutput().withPermission(4);
		List<String> failed = new ArrayList<>();
		int i = 0;
		for (ResourceLocation id : ids) {
			int x = 20000 + (i++) * 400;
			try {
				int result = server.getCommands().getDispatcher().execute(String.format("place structure %s %d 0 20000", id, x), source);
				if (result <= 0) failed.add(id + " (result " + result + ")");
			} catch (Exception e) {
				failed.add(id + " (" + e.getMessage() + ")");
			}
		}
		helper.assertTrue(failed.isEmpty(), "structures failed to place: " + failed);
		helper.succeed();
	}

	/** Every structure piece referenced by the mod loads. */
	public static void templatesLoad(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		var manager = level.getStructureManager();
		List<ResourceLocation> ids = manager.listTemplates().filter(id -> id.getNamespace().equals(NAMESPACE)).toList();
		helper.assertTrue(ids.size() > 50, "too few shrines templates: " + ids.size());
		List<String> bad = new ArrayList<>();
		for (ResourceLocation id : ids) {
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
		List<ResourceKey<LootTable>> keys = server.reloadableRegistries().getKeys(Registries.LOOT_TABLE).stream()
			.filter(id -> id.getNamespace().equals(NAMESPACE))
			.map(id -> ResourceKey.create(Registries.LOOT_TABLE, id)).toList();
		helper.assertTrue(keys.size() >= 20, "expected 20+ shrines loot tables, found " + keys.size());
		List<String> empty = new ArrayList<>();
		BlockPos pos = helper.absolutePos(BlockPos.ZERO);
		for (ResourceKey<LootTable> key : keys) {
			LootTable table = server.reloadableRegistries().getLootTable(key);
			if (table == LootTable.EMPTY) {
				empty.add(key.location() + " (missing)");
				continue;
			}
			if (!key.location().getPath().startsWith("chests/")) continue;
			int items = 0;
			for (int roll = 0; roll < 5; roll++) {
				LootParams params = new LootParams.Builder(level).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos)).create(LootContextParamSets.CHEST);
				for (ItemStack stack : table.getRandomItems(params)) items += stack.isEmpty() ? 0 : 1;
			}
			if (items == 0) empty.add(key.location() + " (rolled nothing)");
		}
		helper.assertTrue(empty.isEmpty(), "loot tables: " + empty);
		helper.succeed();
	}

	/** Structures only spawn in the biomes they're tagged for. */
	public static void biomeTagsResolve(GameTestHelper helper) {
		var registry = helper.getLevel().registryAccess().registryOrThrow(Registries.STRUCTURE);
		List<String> none = new ArrayList<>();
		for (var entry : registry.entrySet()) {
			if (!entry.getKey().location().getNamespace().equals(NAMESPACE)) continue;
			Structure structure = entry.getValue();
			if (structure.biomes().size() == 0) none.add(entry.getKey().location().toString());
		}
		helper.assertTrue(none.isEmpty(), "structures with no biomes: " + none);
		helper.succeed();
	}
}
