package com.cmdgod;

import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

public final class CommandArmorMaterial {
	public static final int BASE_DURABILITY = 60;

	public static final ResourceKey<EquipmentAsset> KEY =
			ResourceKey.create(EquipmentAssets.ROOT_ID, CommandGodMod.id("command"));

	public static final TagKey<Item> REPAIRS =
			TagKey.create(BuiltInRegistries.ITEM.key(), CommandGodMod.id("repairs_command_armor"));

	public static final ArmorMaterial INSTANCE = new ArmorMaterial(
			BASE_DURABILITY,
			Map.of(ArmorType.HELMET, 3, ArmorType.CHESTPLATE, 8, ArmorType.LEGGINGS, 6, ArmorType.BOOTS, 3),
			20,
			SoundEvents.ARMOR_EQUIP_NETHERITE,
			3.0F,
			0.1F,
			REPAIRS,
			KEY);

	private CommandArmorMaterial() {
	}
}
