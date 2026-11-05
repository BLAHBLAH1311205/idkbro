package com.cmdgod;

import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;

/** Full-set bonuses for every god tier. Wearing all four pieces gives the tier's effects. */
public final class TierBonus {

	public static boolean fullSet(ServerPlayer player, String tierId) {
		Item[] a = TierContent.ARMOR.get(tierId);
		return a != null
				&& player.getItemBySlot(EquipmentSlot.HEAD).is(a[0])
				&& player.getItemBySlot(EquipmentSlot.CHEST).is(a[1])
				&& player.getItemBySlot(EquipmentSlot.LEGS).is(a[2])
				&& player.getItemBySlot(EquipmentSlot.FEET).is(a[3]);
	}

	/** Called every 5 ticks per player. */
	public static void apply(ServerPlayer player) {
		for (GodTiers.Tier tier : GodTiers.TIERS) {
			if (!fullSet(player, tier.id())) {
				continue;
			}
			for (String spec : tier.bonus()) {
				String[] parts = spec.split(":");
				Holder<MobEffect> effect = effectByName(parts[0]);
				if (effect != null) {
					player.addEffect(new MobEffectInstance(effect, 80, Integer.parseInt(parts[1]), true, false, true));
				}
			}
		}
	}

	private static Holder<MobEffect> effectByName(String name) {
		return switch (name) {
			case "night_vision" -> MobEffects.NIGHT_VISION;
			case "speed" -> MobEffects.SPEED;
			case "jump_boost" -> MobEffects.JUMP_BOOST;
			case "invisibility" -> MobEffects.INVISIBILITY;
			case "regeneration" -> MobEffects.REGENERATION;
			case "resistance" -> MobEffects.RESISTANCE;
			case "strength" -> MobEffects.STRENGTH;
			case "absorption" -> MobEffects.ABSORPTION;
			case "slow_falling" -> MobEffects.SLOW_FALLING;
			case "saturation" -> MobEffects.SATURATION;
			case "water_breathing" -> MobEffects.WATER_BREATHING;
			case "fire_resistance" -> MobEffects.FIRE_RESISTANCE;
			case "health_boost" -> MobEffects.HEALTH_BOOST;
			default -> null;
		};
	}

	private TierBonus() {
	}
}
