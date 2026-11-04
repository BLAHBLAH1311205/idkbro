package com.cmdgod;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.block.Block;

public final class ModToolMaterials {
	public static final TagKey<Block> INCORRECT_FOR_COMMAND_TOOL = TagKey.create(
			BuiltInRegistries.BLOCK.key(),
			Identifier.fromNamespaceAndPath(CommandGodMod.MOD_ID, "incorrect_for_command_tool"));

	public static final ToolMaterial COMMAND = new ToolMaterial(
			INCORRECT_FOR_COMMAND_TOOL,
			3000,
			12.0F,
			5.0F,
			22,
			CommandArmorMaterial.REPAIRS);

	private ModToolMaterials() {
	}
}
