package com.cmdgod;

import java.util.LinkedHashMap;
import java.util.Map;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

/** Registers the portal frame block, the portal block and the portal core item for every realm. */
public final class RealmContent {
	public static final Map<String, Block> FRAME = new LinkedHashMap<>();
	public static final Map<String, Block> PORTAL = new LinkedHashMap<>();
	public static final Map<String, Item> CORE = new LinkedHashMap<>();

	public static void initialize() {
		for (Realms.Realm r : Realms.REALMS) {
			FRAME.put(r.id(), ModBlocks.registerBlock(r.id() + "_frame", Block::new,
					BlockBehaviour.Properties.of()
							.strength(30.0F, 1200.0F)
							.requiresCorrectToolForDrops()
							.sound(SoundType.STONE)
							.lightLevel(state -> 6)));
			PORTAL.put(r.id(), ModBlocks.registerBlock(r.id() + "_portal", Block::new,
					BlockBehaviour.Properties.of()
							.noCollision()
							.strength(-1.0F, 3600000.0F)
							.sound(SoundType.GLASS)
							.lightLevel(state -> 13)));
			CORE.put(r.id(), ModItems.register(r.id() + "_portal_core", Item::new,
					new Item.Properties().rarity(Rarity.EPIC)));
		}

		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(tab -> {
			FRAME.values().forEach(tab::accept);
		});
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.INGREDIENTS).register(tab -> {
			CORE.values().forEach(tab::accept);
		});
	}

	private RealmContent() {
	}
}
