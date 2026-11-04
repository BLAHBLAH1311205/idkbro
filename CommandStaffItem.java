package com.cmdgod;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/** Right-click: call lightning where you are looking. Costs mana and has a cooldown. */
public class CommandStaffItem extends Item {
	private static final int MANA_COST = 25;
	private static final int COOLDOWN_TICKS = 30;

	public CommandStaffItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult use(Level level, Player player, InteractionHand hand) {
		if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
			if (!ManaSystem.spend(serverPlayer, MANA_COST)) {
				serverPlayer.displayClientMessage(Component.literal("Not enough mana (" + ManaSystem.get(serverPlayer) + "/" + ManaSystem.MAX + ")"), true);
				return InteractionResult.FAIL;
			}
			Vec3 start = player.getEyePosition();
			Vec3 end = start.add(player.getLookAngle().scale(40.0));
			BlockHitResult hit = level.clip(new ClipContext(start, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
			SetBonusHandler.spawnLightning(serverLevel, hit.getLocation(), false);
			player.getCooldowns().addCooldown(player.getItemInHand(hand), COOLDOWN_TICKS);
			serverPlayer.displayClientMessage(Component.literal("Mana " + ManaSystem.get(serverPlayer) + "/" + ManaSystem.MAX), true);
		}
		return InteractionResult.SUCCESS;
	}
}
