package com.cmdgod;

import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.equipment.ArmorType;

public final class ModItems {
	public static <T extends Item> T register(String name, Function<Item.Properties, T> factory, Item.Properties props) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, CommandGodMod.id(name));
		T item = factory.apply(props.setId(key));
		Registry.register(BuiltInRegistries.ITEM, key, item);
		return item;
	}

	// Materials
	public static final Item COMMAND_FRAGMENT = register("command_fragment", Item::new, new Item.Properties());
	public static final Item CHARGED_COMMAND_CRYSTAL = register("charged_command_crystal", Item::new,
			new Item.Properties().rarity(Rarity.RARE));
	public static final Item COMMAND_CORE = register("command_core", Item::new,
			new Item.Properties().rarity(Rarity.EPIC));

	// Armor
	public static final Item COMMAND_HELMET = register("command_helmet", Item::new,
			new Item.Properties().humanoidArmor(CommandArmorMaterial.INSTANCE, ArmorType.HELMET)
					.durability(ArmorType.HELMET.getDurability(CommandArmorMaterial.BASE_DURABILITY))
					.rarity(Rarity.EPIC));
	public static final Item COMMAND_CHESTPLATE = register("command_chestplate", Item::new,
			new Item.Properties().humanoidArmor(CommandArmorMaterial.INSTANCE, ArmorType.CHESTPLATE)
					.durability(ArmorType.CHESTPLATE.getDurability(CommandArmorMaterial.BASE_DURABILITY))
					.rarity(Rarity.EPIC));
	public static final Item COMMAND_LEGGINGS = register("command_leggings", Item::new,
			new Item.Properties().humanoidArmor(CommandArmorMaterial.INSTANCE, ArmorType.LEGGINGS)
					.durability(ArmorType.LEGGINGS.getDurability(CommandArmorMaterial.BASE_DURABILITY))
					.rarity(Rarity.EPIC));
	public static final Item COMMAND_BOOTS = register("command_boots", Item::new,
			new Item.Properties().humanoidArmor(CommandArmorMaterial.INSTANCE, ArmorType.BOOTS)
					.durability(ArmorType.BOOTS.getDurability(CommandArmorMaterial.BASE_DURABILITY))
					.rarity(Rarity.EPIC));

	// Tools and weapons
	public static final Item COMMAND_SWORD = register("command_sword", Item::new,
			new Item.Properties().sword(ModToolMaterials.COMMAND, 3.0F, -2.4F).rarity(Rarity.EPIC));
	public static final Item COMMAND_PICKAXE = register("command_pickaxe", Item::new,
			new Item.Properties().pickaxe(ModToolMaterials.COMMAND, 1.0F, -2.8F).rarity(Rarity.EPIC));
	public static final Item COMMAND_AXE = register("command_axe",
			settings -> new AxeItem(ModToolMaterials.COMMAND, 5.0F, -3.0F, settings),
			new Item.Properties().rarity(Rarity.EPIC));
	public static final Item COMMAND_STAFF = register("command_staff", CommandStaffItem::new,
			new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));

	public static void initialize() {
		// Touching this class runs the static registrations above.
	}

	private ModItems() {
	}
}
