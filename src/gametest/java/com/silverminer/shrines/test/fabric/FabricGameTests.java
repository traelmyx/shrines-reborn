package com.silverminer.shrines.test.fabric;

//? if fabric {
/*import com.silverminer.shrines.test.ShrinesTests;
import net.minecraft.gametest.framework.GameTestHelper;
//? if >=1.21.5 {
import net.fabricmc.fabric.api.gametest.v1.GameTest;
//?} else {
/^import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
^///?}

//? if >=1.21.5 {
public class FabricGameTests {
	private static final String EMPTY = "shrines_gametest:empty";

	@GameTest(structure = EMPTY, maxTicks = 1600)
	public void allStructuresPlace(GameTestHelper helper) {
		ShrinesTests.allStructuresPlace(helper);
	}

	@GameTest(structure = EMPTY, maxTicks = 100)
	public void templatesLoad(GameTestHelper helper) {
		ShrinesTests.templatesLoad(helper);
	}

	@GameTest(structure = EMPTY, maxTicks = 100)
	public void lootTablesRoll(GameTestHelper helper) {
		ShrinesTests.lootTablesRoll(helper);
	}

	@GameTest(structure = EMPTY, maxTicks = 100)
	public void biomeTagsResolve(GameTestHelper helper) {
		ShrinesTests.biomeTagsResolve(helper);
	}
}
//?} else {
/^public class FabricGameTests implements FabricGameTest {
	private static final String EMPTY = "shrines_gametest:empty";

	@GameTest(template = EMPTY, timeoutTicks = 1600)
	public void allStructuresPlace(GameTestHelper helper) {
		ShrinesTests.allStructuresPlace(helper);
	}

	@GameTest(template = EMPTY, timeoutTicks = 100)
	public void templatesLoad(GameTestHelper helper) {
		ShrinesTests.templatesLoad(helper);
	}

	@GameTest(template = EMPTY, timeoutTicks = 100)
	public void lootTablesRoll(GameTestHelper helper) {
		ShrinesTests.lootTablesRoll(helper);
	}

	@GameTest(template = EMPTY, timeoutTicks = 100)
	public void biomeTagsResolve(GameTestHelper helper) {
		ShrinesTests.biomeTagsResolve(helper);
	}
}
^///?}
*///?}
