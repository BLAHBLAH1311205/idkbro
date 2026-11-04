package com.cmdgod;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.Vec3;

/** Armor abilities, the full-set "Command God Mode", and mana regen. All server-side Java. */
public final class SetBonusHandler {
	private static final int GOD_DURATION_TICKS = 20 * 8;
	private static final int GOD_COOLDOWN_TICKS = 20 * 60;

	private static final Map<UUID, Long> GOD_UNTIL = new HashMap<>();
	private static final Map<UUID, Long> READY_AT = new HashMap<>();

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(SetBonusHandler::onTick);

		ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
			if (entity instanceof ServerPlayer player) {
				long now = player.level().getGameTime();
				Long until = GOD_UNTIL.get(player.getUUID());
				if (until != null && now < until) {
					return false;
				}
				if (source.is(DamageTypeTags.IS_EXPLOSION) && wears(player, EquipmentSlot.CHEST, ModItems.COMMAND_CHESTPLATE)) {
					return false;
				}
				if (source.is(DamageTypeTags.IS_FALL)
						&& (wears(player, EquipmentSlot.LEGS, ModItems.COMMAND_LEGGINGS)
								|| wears(player, EquipmentSlot.FEET, ModItems.COMMAND_BOOTS))) {
					return false;
				}
			}
			return true;
		});
	}

	private static boolean wears(ServerPlayer player, EquipmentSlot slot, Item item) {
		return player.getItemBySlot(slot).is(item);
	}

	private static MobEffectInstance effect(net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> type, int ticks, int amp) {
		return new MobEffectInstance(type, ticks, amp, true, false, true);
	}

	private static void onTick(MinecraftServer server) {
		if (server.getTickCount() % 5 != 0) {
			return;
		}
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			handle(player, server.getTickCount());
		}
	}

	private static void handle(ServerPlayer player, int serverTick) {
		ServerLevel level = (ServerLevel) player.level();
		boolean head = wears(player, EquipmentSlot.HEAD, ModItems.COMMAND_HELMET);
		boolean chest = wears(player, EquipmentSlot.CHEST, ModItems.COMMAND_CHESTPLATE);
		boolean legs = wears(player, EquipmentSlot.LEGS, ModItems.COMMAND_LEGGINGS);
		boolean feet = wears(player, EquipmentSlot.FEET, ModItems.COMMAND_BOOTS);

		if (serverTick % 10 == 0) {
			ManaSystem.regen(player, 2);
		}

		if (head) {
			player.addEffect(effect(MobEffects.NIGHT_VISION, 400, 0));
			// Mob detection: nearby hostile mobs glow through walls.
			List<Monster> monsters = level.getEntitiesOfClass(Monster.class, player.getBoundingBox().inflate(20.0));
			for (Monster monster : monsters) {
				monster.addEffect(effect(MobEffects.GLOWING, 40, 0));
			}
		}
		if (chest) {
			player.addEffect(effect(MobEffects.RESISTANCE, 40, 1));
			if (player.getHealth() <= 6.0F) {
				player.addEffect(effect(MobEffects.REGENERATION, 60, 2));
			}
		}
		if (legs) {
			player.addEffect(effect(MobEffects.SPEED, 40, 2));
			player.addEffect(effect(MobEffects.JUMP_BOOST, 40, 1));
		}
		if (feet) {
			player.addEffect(effect(MobEffects.JUMP_BOOST, 40, 3));
			player.addEffect(effect(MobEffects.SLOW_FALLING, 40, 0));
		}

		long now = level.getGameTime();
		Long godUntil = GOD_UNTIL.get(player.getUUID());
		boolean godActive = godUntil != null && now < godUntil;

		if (head && chest && legs && feet) {
			if (!godActive && player.isShiftKeyDown() && now >= READY_AT.getOrDefault(player.getUUID(), 0L)) {
				activateGodMode(player, level, now);
				godActive = true;
			}
			if (godActive) {
				level.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1.0, player.getZ(), 12, 0.6, 0.9, 0.6, 0.02);
				level.sendParticles(ParticleTypes.ELECTRIC_SPARK, player.getX(), player.getY() + 1.0, player.getZ(), 8, 0.7, 1.0, 0.7, 0.1);
			}
		}
	}

	private static void activateGodMode(ServerPlayer player, ServerLevel level, long now) {
		GOD_UNTIL.put(player.getUUID(), now + GOD_DURATION_TICKS);
		READY_AT.put(player.getUUID(), now + GOD_DURATION_TICKS + GOD_COOLDOWN_TICKS);

		player.addEffect(effect(MobEffects.STRENGTH, GOD_DURATION_TICKS, 2));
		player.addEffect(effect(MobEffects.REGENERATION, GOD_DURATION_TICKS, 3));
		player.sendSystemMessage(Component.literal("§6[COMMAND GOD MODE]§r activated"), true);

		spawnLightning(level, player.position(), true);
		level.playSound(null, player.blockPosition(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 1.0F, 1.0F);

		// Shockwave: damage and shove everything nearby.
		List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(8.0), e -> e != player);
		for (LivingEntity target : targets) {
			target.hurtServer(level, level.damageSources().magic(), 8.0F);
			Vec3 away = target.position().subtract(player.position()).normalize();
			target.push(away.x * 1.5, 0.5, away.z * 1.5);
		}
		level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, player.getX(), player.getY() + 0.5, player.getZ(), 1, 0, 0, 0, 0);
	}

	public static void spawnLightning(ServerLevel level, Vec3 pos, boolean visualOnly) {
		LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level, EntitySpawnReason.TRIGGERED);
		if (bolt != null) {
			bolt.setPos(pos.x, pos.y, pos.z);
			bolt.setVisualOnly(visualOnly);
			level.addFreshEntity(bolt);
		}
	}

	private SetBonusHandler() {
	}
}
