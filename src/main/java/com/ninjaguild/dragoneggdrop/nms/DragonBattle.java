package com.ninjaguild.dragoneggdrop.nms;

import java.lang.reflect.Field;
import java.util.UUID;

import net.minecraft.server.v1_13_R2.BlockPosition;
import net.minecraft.server.v1_13_R2.BossBattle;
import net.minecraft.server.v1_13_R2.BossBattleServer;
import net.minecraft.server.v1_13_R2.ChatMessage;
import net.minecraft.server.v1_13_R2.EnderDragonBattle;
import net.minecraft.server.v1_13_R2.Entity;
import net.minecraft.server.v1_13_R2.PacketPlayOutBoss;
import net.minecraft.server.v1_13_R2.WorldServer;

import org.apache.commons.lang3.EnumUtils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.EnderDragon;

public class DragonBattle {

	private final EnderDragonBattle battle;

	protected DragonBattle(EnderDragonBattle battle) {
		this.battle = battle;
	}

	public void setBossBarTitle(String title) {
		if (title == null) return;

		BossBattleServer battleServer = battle.c; // Note: Field will be renamed in 1.14
		if (battleServer == null) return;

		battleServer.title = new ChatMessage(title);
		battleServer.sendUpdate(PacketPlayOutBoss.Action.UPDATE_NAME);
	}

	public boolean setBossBarStyle(BarStyle style, BarColor colour) {
		BossBattleServer battleServer = battle.c; // Note: Field will be renamed in 1.14
		if (battleServer == null) return false;

		if (style != null) {
			String nmsStyle = style.name().contains("SEGMENTED") ? style.name().replace("SEGMENTED", "NOTCHED") : "PROGRESS";
			if (EnumUtils.isValidEnum(BossBattle.BarStyle.class, nmsStyle)) {
				battleServer.style = BossBattle.BarStyle.valueOf(nmsStyle);
			}
		}
		if (colour != null) {
			battleServer.color = BossBattle.BarColor.valueOf(colour.name());
		}

		battleServer.sendUpdate(PacketPlayOutBoss.Action.UPDATE_STYLE);
		return true;
	}

	public EnderDragon getEnderDragon() {
		EnderDragon dragon = null;

		try {
			Field fieldWorldServer = EnderDragonBattle.class.getDeclaredField("d");
			Field fieldDragonUUID = EnderDragonBattle.class.getDeclaredField("m");
			fieldWorldServer.setAccessible(true);
			fieldDragonUUID.setAccessible(true);

			WorldServer world = (WorldServer) fieldWorldServer.get(battle);
			UUID dragonUUID = (UUID) fieldDragonUUID.get(battle);

			if (world == null || dragonUUID == null)
				return null;

			Entity dragonEntity = world.getEntity(dragonUUID);
			if (dragonEntity == null) return null;
			dragon = (EnderDragon) dragonEntity.getBukkitEntity();

			fieldWorldServer.setAccessible(false);
			fieldDragonUUID.setAccessible(false);
		} catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}

		return dragon;
	}

	public void respawnEnderDragon() {
		this.battle.e();
	}

	public Location getEndPortalLocation() {
		Location portalLocation = null;

		try {
			Field fieldExitPortalLocation = EnderDragonBattle.class.getDeclaredField("o");
			Field fieldWorldServer = EnderDragonBattle.class.getDeclaredField("d");
			fieldExitPortalLocation.setAccessible(true);
			fieldWorldServer.setAccessible(true);

			WorldServer worldServer = (WorldServer) fieldWorldServer.get(battle);
			BlockPosition position = (BlockPosition) fieldExitPortalLocation.get(battle);
			if (worldServer != null && position != null) {
				World world = worldServer.getWorld();
				portalLocation = new Location(world, Math.floor(position.getX()) + 0.5, position.getY() + 4, Math.floor(position.getZ()) + 0.5);
			}

			fieldWorldServer.setAccessible(false);
			fieldExitPortalLocation.setAccessible(false);
		} catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}

		return portalLocation;
	}

	public void resetBattleState() {
		try {
			Field fieldDragonBattleState = EnderDragonBattle.class.getDeclaredField("p");
			Field fieldDragonKilled = EnderDragonBattle.class.getDeclaredField("k");
			fieldDragonBattleState.setAccessible(true);
			fieldDragonKilled.setAccessible(true);

			fieldDragonBattleState.set(battle, null);
			fieldDragonKilled.set(battle, true);

			fieldDragonBattleState.setAccessible(false);
			fieldDragonKilled.setAccessible(false);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			e.printStackTrace();
		}

		this.battle.f();
	}

	/**
	 * Get the net.minecraft.server implementation of DragonBattle
	 *
	 * @return the wrapped battle
	 */
	public EnderDragonBattle getHandle() {
		return battle;
	}

}