package com.cmdgod;

import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class ModBlocks {
	public static <T extends Block> T registerBlock(String name, Function<BlockBehaviour.Properties, T> factory,
			BlockBehaviour.Properties props) {
		ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, CommandGodMod.id(name));
		T block = factory.apply(props.setId(blockKey));
		Registry.register(BuiltInRegistries.BLOCK, blockKey, block);

		ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, CommandGodMod.id(name));
		BlockItem blockItem = new BlockItem(block, new Item.Properties().setId(itemKey).useBlockDescriptionPrefix());
		Registry.register(BuiltInRegistries.ITEM, itemKey, blockItem);
		return block;
	}

	public static final Block COMMAND_ORE = registerBlock("command_ore", Block::new,
			BlockBehaviour.Properties.of()
					.strength(6.0F, 6.0F)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
					.lightLevel(state -> 3));

	public static void initialize() {
		// Touching this class runs the static registrations above.
	}

	private ModBlocks() {
	}
}
