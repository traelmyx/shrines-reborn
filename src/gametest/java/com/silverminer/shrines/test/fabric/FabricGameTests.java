package com.silverminer.shrines.test.fabric;

//? if fabric {
/*import com.silverminer.shrines.test.ShrinesTests;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

public class FabricGameTests implements FabricGameTest {
	private static final String EMPTY = "shrines_gametest:empty";

	@GameTest(template = EMPTY, timeoutTicks = 2400)
	public void allStructuresPlace(GameTestHelper helper) {
		ShrinesTests.allStructuresPlace(helper);
	}

	@GameTest(template = EMPTY)
	public void templatesLoad(GameTestHelper helper) {
		ShrinesTests.templatesLoad(helper);
	}

	@GameTest(template = EMPTY)
	public void lootTablesRoll(GameTestHelper helper) {
		ShrinesTests.lootTablesRoll(helper);
	}

	@GameTest(template = EMPTY)
	public void biomeTagsResolve(GameTestHelper helper) {
		ShrinesTests.biomeTagsResolve(helper);
	}
}
*///?}
