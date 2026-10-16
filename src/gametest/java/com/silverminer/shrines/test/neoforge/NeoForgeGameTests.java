package com.silverminer.shrines.test.neoforge;

//? if neoforge {
import com.silverminer.shrines.test.ClientSmoke;
import com.silverminer.shrines.test.ShrinesTests;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(NeoForgeGameTests.MOD_ID)
@PrefixGameTestTemplate(false)
@Mod(NeoForgeGameTests.MOD_ID)
public class NeoForgeGameTests {
	public static final String MOD_ID = "shrines_gametest";

	public NeoForgeGameTests(Dist dist) {
		if (dist.isClient() && ClientSmoke.start()) {
			NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post e) -> ClientSmoke.tick(net.minecraft.client.Minecraft.getInstance()));
		}
	}

	@GameTest(template = "empty", timeoutTicks = 1600)
	public static void allStructuresPlace(GameTestHelper helper) {
		ShrinesTests.allStructuresPlace(helper);
	}

	@GameTest(template = "empty")
	public static void templatesLoad(GameTestHelper helper) {
		ShrinesTests.templatesLoad(helper);
	}

	@GameTest(template = "empty")
	public static void lootTablesRoll(GameTestHelper helper) {
		ShrinesTests.lootTablesRoll(helper);
	}

	@GameTest(template = "empty")
	public static void biomeTagsResolve(GameTestHelper helper) {
		ShrinesTests.biomeTagsResolve(helper);
	}
}
//?}
