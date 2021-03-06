package com.ninjaguild.dragoneggdrop.events;

import com.ninjaguild.dragoneggdrop.DragonEggDrop;
import com.ninjaguild.dragoneggdrop.management.DEDManager;
import com.ninjaguild.dragoneggdrop.management.DEDManager.RespawnType;
import com.ninjaguild.dragoneggdrop.management.EndWorldWrapper;

import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.EnderDragon;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class RespawnListeners implements Listener {

	private final DragonEggDrop plugin;
	private final DEDManager manager;

	public RespawnListeners(DragonEggDrop plugin) {
		this.plugin = plugin;
		this.manager = plugin.getDEDManager();
	}

	@EventHandler
	public void onPlayerSwitchWorlds(PlayerChangedWorldEvent event) {
		World world = event.getPlayer().getWorld();
		if (world.getEnvironment() != Environment.THE_END) return;

		EndWorldWrapper worldWrapper = manager.getWorldWrapper(world);

		// Start the respawn countdown if joining an empty world
		if (plugin.getConfig().getBoolean("respawn-on-join", false)) {
			if (world.getPlayers().size() > 1 || worldWrapper.isRespawnInProgress() || world.getEntitiesByClass(EnderDragon.class).size() == 0) {
				return;
			}

			this.manager.getWorldWrapper(world).startRespawn(RespawnType.JOIN);
		}

		// Reset end crystal states just in case something went awry
		if (!worldWrapper.isRespawnInProgress()) {
			world.getEntitiesByClass(EnderCrystal.class).forEach(e -> {
				e.setInvulnerable(false);
				e.setBeamTarget(null);
			});
		}
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		World world = event.getPlayer().getWorld();
		if (world.getEnvironment() != Environment.THE_END) return;

		EndWorldWrapper worldWrapper = manager.getWorldWrapper(world);

		// Reset end crystal states just in case something went awry
		if (!worldWrapper.isRespawnInProgress()) {
			world.getEntitiesByClass(EnderCrystal.class).forEach(e -> {
				e.setInvulnerable(false);
				e.setBeamTarget(null);
			});
		}

		// Dragon respawn logic
		if (!plugin.getConfig().getBoolean("respawn-on-join", false)) return;
		if (!world.getPlayers().isEmpty() || manager.getWorldWrapper(world).isRespawnInProgress() || world.getEntitiesByClass(EnderDragon.class).size() == 0) {
			return;
		}

		this.manager.getWorldWrapper(world).startRespawn(RespawnType.JOIN);
	}

}