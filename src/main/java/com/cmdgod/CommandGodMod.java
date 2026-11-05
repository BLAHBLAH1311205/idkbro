package com.cmdgod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.levelgen.GenerationStep;

public class CommandGodMod implements ModInitializer {
	public static final String MOD_ID = "cmdgod";

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}

	@Override
	public void onInitialize() {
		ModBlocks.initialize();
		ModItems.initialize();
		TierContent.initialize();
		SetBonusHandler.register();
		WeaponAbilities.register();

		BiomeModifications.addFeature(
				BiomeSelectors.foundInOverworld(),
				GenerationStep.Decoration.UNDERGROUND_ORES,
				ResourceKey.create(Registries.PLACED_FEATURE, id("command_ore")));

		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.COMBAT).register(tab -> {
			tab.accept(ModItems.COMMAND_HELMET);
			tab.accept(ModItems.COMMAND_CHESTPLATE);
			tab.accept(ModItems.COMMAND_LEGGINGS);
			tab.accept(ModItems.COMMAND_BOOTS);
			tab.accept(ModItems.COMMAND_SWORD);
			tab.accept(ModItems.COMMAND_STAFF);
		});
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(tab -> {
			tab.accept(ModItems.COMMAND_PICKAXE);
			tab.accept(ModItems.COMMAND_AXE);
		});
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.INGREDIENTS).register(tab -> {
			tab.accept(ModItems.COMMAND_FRAGMENT);
			tab.accept(ModItems.CHARGED_COMMAND_CRYSTAL);
			tab.accept(ModItems.COMMAND_CORE);
		});
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.NATURAL_BLOCKS).register(tab -> tab.accept(ModBlocks.COMMAND_ORE));
	}
}
