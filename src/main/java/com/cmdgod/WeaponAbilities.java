package com.cmdgod;

import java.util.HashMap;
import java.util.Map;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.Vec3;

/** On-hit abilities for the named god weapons. Runs on the server only. */
public final class WeaponAbilities {
	private static final Map<Item, String> ABILITY = new HashMap<>();

	public static void register() {
		for (GodTiers.Weapon w : GodTiers.WEAPONS) {
			Item item = TierContent.WEAPONS.get(w.id());
			if (item != null) {
				ABILITY.put(item, w.ability());
			}
		}

		ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamageTaken, damageTaken, blocked) -> {
			if (blocked || !(source.getEntity() instanceof ServerPlayer attacker)) {
				return;
			}
			if (!(entity.level() instanceof ServerLevel level)) {
				return;
			}
			String ability = ABILITY.get(attacker.getMainHandItem().getItem());
			if (ability == null) {
				return;
			}
			Vec3 away = entity.position().subtract(attacker.position()).normalize();
			switch (ability) {
				case "lightning" -> SetBonusHandler.spawnLightning(level, entity.position(), false);
				case "wither" -> entity.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 1));
				case "fire" -> entity.setRemainingFireTicks(160);
				case "heal" -> attacker.heal(4.0F);
				case "knockback" -> entity.push(away.x * 2.0, 0.6, away.z * 2.0);
				case "pull" -> entity.push(-away.x * 1.5, 0.3, -away.z * 1.5);
				case "weakness" -> entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 1));
				case "blind" -> entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0));
				case "slow" -> entity.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100, 3));
				case "levitate" -> entity.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 60, 1));
				default -> {
				}
			}
		});
	}

	private WeaponAbilities() {
	}
}
