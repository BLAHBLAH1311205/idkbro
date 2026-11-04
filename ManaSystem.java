package com.cmdgod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;

/** Very small server-side mana pool. Resets when the server restarts. */
public final class ManaSystem {
	public static final int MAX = 100;
	private static final Map<UUID, Integer> MANA = new HashMap<>();

	public static int get(ServerPlayer player) {
		return MANA.getOrDefault(player.getUUID(), MAX);
	}

	public static boolean spend(ServerPlayer player, int cost) {
		int current = get(player);
		if (current < cost) {
			return false;
		}
		MANA.put(player.getUUID(), current - cost);
		return true;
	}

	public static void regen(ServerPlayer player, int amount) {
		MANA.put(player.getUUID(), Math.min(MAX, get(player) + amount));
	}

	private ManaSystem() {
	}
}
