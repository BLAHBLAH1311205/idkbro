package com.cmdgod;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Portal activation and travel.
 * Build a 5x5 ring (16 blocks) of one realm's Portal Frame, stand it upright with an empty 3x3 middle,
 * then right-click a frame block holding 12 of that realm's Portal Cores. The middle fills with portal blocks.
 * Walking into a portal block sends you to the realm (or back to the Overworld if you are already in the realm).
 */
public final class RealmPortals {
	private static final int CORES_NEEDED = 12;
	private static final Set<UUID> INSIDE = new HashSet<>();
	private static final Map<UUID, Vec3> LAST_OVERWORLD = new HashMap<>();

	public static ResourceKey<Level> key(String realmId) {
		return ResourceKey.create(Registries.DIMENSION, CommandGodMod.id(realmId));
	}

	public static void register() {
		UseBlockCallback.EVENT.register(RealmPortals::onUse);
		ServerTickEvents.END_SERVER_TICK.register(RealmPortals::onTick);
	}

	// ---------------------------------------------------------------- activation

	private static InteractionResult onUse(Player player, Level world, InteractionHand hand, BlockHitResult hit) {
		if (!(world instanceof ServerLevel level) || !(player instanceof ServerPlayer serverPlayer)) {
			return InteractionResult.PASS;
		}
		BlockPos clicked = hit.getBlockPos();
		Block clickedBlock = level.getBlockState(clicked).getBlock();

		Realms.Realm realm = null;
		for (Realms.Realm r : Realms.REALMS) {
			if (RealmContent.FRAME.get(r.id()) == clickedBlock) {
				realm = r;
				break;
			}
		}
		if (realm == null) {
			return InteractionResult.PASS;
		}

		ItemStack held = player.getItemInHand(hand);
		if (!held.is(RealmContent.CORE.get(realm.id()))) {
			return InteractionResult.PASS;
		}
		if (held.getCount() < CORES_NEEDED) {
			serverPlayer.sendSystemMessage(Component.literal("You need " + CORES_NEEDED + " " + realm.display()
					+ " Portal Cores (you have " + held.getCount() + ")"), true);
			return InteractionResult.SUCCESS;
		}

		Block frame = RealmContent.FRAME.get(realm.id());
		Block portal = RealmContent.PORTAL.get(realm.id());
		if (!lightFrame(level, clicked, frame, portal)) {
			serverPlayer.sendSystemMessage(Component.literal(
					"Build an upright 5x5 ring of " + realm.display() + " Portal Frames with an empty 3x3 middle"), true);
			return InteractionResult.SUCCESS;
		}
		held.shrink(CORES_NEEDED);
		serverPlayer.sendSystemMessage(Component.literal(realm.display() + " portal activated"), true);
		return InteractionResult.SUCCESS;
	}

	/** Finds an upright 5x5 frame ring that contains the clicked block, fills its middle, returns true if found. */
	private static boolean lightFrame(ServerLevel level, BlockPos clicked, Block frame, Block portal) {
		int[][] axes = {{1, 0}, {0, 1}};
		for (int[] axis : axes) {
			for (int dx = 0; dx < 5; dx++) {
				for (int dy = 0; dy < 5; dy++) {
					boolean onEdge = dx == 0 || dx == 4 || dy == 0 || dy == 4;
					if (!onEdge) {
						continue;
					}
					BlockPos origin = clicked.offset(-axis[0] * dx, -dy, -axis[1] * dx);
					if (ringIsValid(level, origin, axis, frame)) {
						for (int i = 1; i <= 3; i++) {
							for (int j = 1; j <= 3; j++) {
								BlockPos p = origin.offset(axis[0] * i, j, axis[1] * i);
								level.setBlock(p, portal.defaultBlockState(), 3);
							}
						}
						return true;
					}
				}
			}
		}
		return false;
	}

	private static boolean ringIsValid(ServerLevel level, BlockPos origin, int[] axis, Block frame) {
		for (int i = 0; i < 5; i++) {
			for (int j = 0; j < 5; j++) {
				BlockState state = level.getBlockState(origin.offset(axis[0] * i, j, axis[1] * i));
				boolean edge = i == 0 || i == 4 || j == 0 || j == 4;
				if (edge ? !state.is(frame) : !state.isAir()) {
					return false;
				}
			}
		}
		return true;
	}

	// ---------------------------------------------------------------- travel

	private static void onTick(MinecraftServer server) {
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			ServerLevel level = (ServerLevel) player.level();
			Block here = level.getBlockState(player.blockPosition()).getBlock();
			Realms.Realm inPortal = null;
			for (Realms.Realm r : Realms.REALMS) {
				if (RealmContent.PORTAL.get(r.id()) == here) {
					inPortal = r;
					break;
				}
			}
			if (inPortal == null) {
				INSIDE.remove(player.getUUID());
				continue;
			}
			// Only trigger when stepping in; you must step out before it can trigger again.
			if (INSIDE.add(player.getUUID())) {
				travel(server, player, level, inPortal);
			}
		}
	}

	private static void run(MinecraftServer server, String command) {
		var source = server.createCommandSourceStack();
		server.getCommands().performPrefixedCommand(source, command);
	}

	private static String xyz(double x, double y, double z) {
		return String.format(Locale.ROOT, "%.2f %.2f %.2f", x, y, z);
	}

	private static void travel(MinecraftServer server, ServerPlayer player, ServerLevel from, Realms.Realm realm) {
		ResourceKey<Level> realmKey = key(realm.id());
		UUID id = player.getUUID();

		if (from.dimension().equals(realmKey)) {
			ServerLevel overworld = server.getLevel(Level.OVERWORLD);
			if (overworld == null) {
				return;
			}
			Vec3 dest = LAST_OVERWORLD.get(id);
			if (dest == null) {
				overworld.getChunk(0, 0);
				int y = overworld.getHeight(Heightmap.Types.MOTION_BLOCKING, 0, 0) + 1;
				dest = new Vec3(0.5, y, 0.5);
			}
			run(server, "execute in minecraft:overworld run tp " + id + " " + xyz(dest.x, dest.y, dest.z));
			return;
		}

		ServerLevel target = server.getLevel(realmKey);
		if (target == null) {
			player.sendSystemMessage(Component.literal("The " + realm.display() + " is not loaded in this world"), true);
			return;
		}
		if (from.dimension().equals(Level.OVERWORLD)) {
			LAST_OVERWORLD.put(id, player.position());
		}
		buildArrival(target, realm);
		run(server, "execute in " + CommandGodMod.id(realm.id()) + " run tp " + id + " " + xyz(0.5, realm.spawnY() + 1, 0.5));
	}

	/** Builds a 9x9 obsidian platform with a lit return portal on first arrival. */
	private static void buildArrival(ServerLevel target, Realms.Realm realm) {
		int y = realm.spawnY();
		for (int cx = -1; cx <= 0; cx++) {
			for (int cz = -1; cz <= 0; cz++) {
				target.getChunk(cx, cz);
			}
		}
		if (target.getBlockState(new BlockPos(0, y, 0)).is(Blocks.OBSIDIAN)) {
			return;
		}
		BlockState platform = Blocks.OBSIDIAN.defaultBlockState();
		BlockState air = Blocks.AIR.defaultBlockState();
		for (int dx = -4; dx <= 4; dx++) {
			for (int dz = -4; dz <= 4; dz++) {
				target.setBlock(new BlockPos(dx, y, dz), platform, 2);
				for (int dy = 1; dy <= 7; dy++) {
					target.setBlock(new BlockPos(dx, y + dy, dz), air, 2);
				}
			}
		}
		BlockState frame = RealmContent.FRAME.get(realm.id()).defaultBlockState();
		BlockState portal = RealmContent.PORTAL.get(realm.id()).defaultBlockState();
		for (int i = 0; i < 5; i++) {
			for (int j = 0; j < 5; j++) {
				boolean edge = i == 0 || i == 4 || j == 0 || j == 4;
				target.setBlock(new BlockPos(-2 + i, y + 1 + j, 3), edge ? frame : portal, 2);
			}
		}
	}

	private RealmPortals() {
	}
}
