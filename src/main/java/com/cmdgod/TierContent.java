package com.cmdgod;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.GenerationStep;

/** Registers every god tier (shard, ore, armor, tools) and every named weapon from GodTiers. */
public final class TierContent {
	public static final Map<String, Item[]> ARMOR = new LinkedHashMap<>();
	public static final Map<String, Item> SHARD = new LinkedHashMap<>();
	public static final Map<String, Item> SWORD = new LinkedHashMap<>();
	public static final Map<String, Item> PICKAXE = new LinkedHashMap<>();
	public static final Map<String, Item> AXE = new LinkedHashMap<>();
	public static final Map<String, Block> ORE = new LinkedHashMap<>();
	public static final Map<String, Item> WEAPONS = new LinkedHashMap<>();
	public static final Map<String, ToolMaterial> TOOL_MATERIALS = new LinkedHashMap<>();

	private static Block registerOre(String name) {
		ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, CommandGodMod.id(name));
		Function<BlockBehaviour.Properties, Block> factory = Block::new;
		Block block = factory.apply(BlockBehaviour.Properties.of()
				.strength(7.0F, 9.0F)
				.requiresCorrectToolForDrops()
				.sound(SoundType.STONE)
				.lightLevel(state -> 4)
				.setId(blockKey));
		Registry.register(BuiltInRegistries.BLOCK, blockKey, block);

		ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, CommandGodMod.id(name));
		BlockItem blockItem = new BlockItem(block, new Item.Properties().setId(itemKey).useBlockDescriptionPrefix());
		Registry.register(BuiltInRegistries.ITEM, itemKey, blockItem);
		return block;
	}

	public static void initialize() {
		ArmorType[] types = {ArmorType.HELMET, ArmorType.CHESTPLATE, ArmorType.LEGGINGS, ArmorType.BOOTS};
		String[] names = {"helmet", "chestplate", "leggings", "boots"};

		for (GodTiers.Tier t : GodTiers.TIERS) {
			ResourceKey<EquipmentAsset> assetKey = ResourceKey.create(EquipmentAssets.ROOT_ID, CommandGodMod.id(t.id()));
			TagKey<Item> repairs = TagKey.create(BuiltInRegistries.ITEM.key(), CommandGodMod.id("repairs_" + t.id() + "_armor"));
			TagKey<Block> incorrect = TagKey.create(BuiltInRegistries.BLOCK.key(), CommandGodMod.id("incorrect_for_" + t.id() + "_tool"));

			ArmorMaterial armorMat = new ArmorMaterial(
					t.armorBase(),
					Map.of(ArmorType.HELMET, t.defHelmet(), ArmorType.CHESTPLATE, t.defChest(),
							ArmorType.LEGGINGS, t.defLegs(), ArmorType.BOOTS, t.defBoots()),
					t.ench(),
					SoundEvents.ARMOR_EQUIP_NETHERITE,
					t.toughness(),
					t.kb(),
					repairs,
					assetKey);
			ToolMaterial toolMat = new ToolMaterial(incorrect, t.toolDur(), t.toolSpeed(), t.toolAtk(), t.ench(), repairs);
			TOOL_MATERIALS.put(t.id(), toolMat);

			SHARD.put(t.id(), ModItems.register(t.id() + "_shard", Item::new, new Item.Properties().rarity(Rarity.EPIC)));

			Item[] pieces = new Item[4];
			for (int i = 0; i < 4; i++) {
				pieces[i] = ModItems.register(t.id() + "_" + names[i], Item::new,
						new Item.Properties().humanoidArmor(armorMat, types[i])
								.durability(types[i].getDurability(t.armorBase()))
								.rarity(t.rarity()));
			}
			ARMOR.put(t.id(), pieces);

			SWORD.put(t.id(), ModItems.register(t.id() + "_sword", Item::new,
					new Item.Properties().sword(toolMat, 3.0F, -2.4F).rarity(t.rarity())));
			PICKAXE.put(t.id(), ModItems.register(t.id() + "_pickaxe", Item::new,
					new Item.Properties().pickaxe(toolMat, 1.0F, -2.8F).rarity(t.rarity())));
			AXE.put(t.id(), ModItems.register(t.id() + "_axe",
					settings -> new AxeItem(toolMat, 5.0F, -3.0F, settings),
					new Item.Properties().rarity(t.rarity())));

			ORE.put(t.id(), registerOre(t.id() + "_ore"));
		}

		for (GodTiers.Weapon w : GodTiers.WEAPONS) {
			ToolMaterial mat = TOOL_MATERIALS.get(w.tier());
			WEAPONS.put(w.id(), ModItems.register(w.id(), Item::new,
					new Item.Properties().sword(mat, w.atk(), w.speed()).rarity(Rarity.EPIC)));
		}

		for (GodTiers.Tier t : GodTiers.TIERS) {
			BiomeModifications.addFeature(
					BiomeSelectors.foundInOverworld(),
					GenerationStep.Decoration.UNDERGROUND_ORES,
					ResourceKey.create(Registries.PLACED_FEATURE, CommandGodMod.id(t.id() + "_ore")));
		}

		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.COMBAT).register(tab -> {
			for (Item[] pieces : ARMOR.values()) {
				for (Item piece : pieces) {
					tab.accept(piece);
				}
			}
			SWORD.values().forEach(tab::accept);
			WEAPONS.values().forEach(tab::accept);
		});
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(tab -> {
			PICKAXE.values().forEach(tab::accept);
			AXE.values().forEach(tab::accept);
		});
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.INGREDIENTS).register(tab -> SHARD.values().forEach(tab::accept));
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.NATURAL_BLOCKS).register(tab -> ORE.values().forEach(tab::accept));
	}

	private TierContent() {
	}
}
