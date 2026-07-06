package com.dalict.joinleavesound.listener;

import com.dalict.joinleavesound.JoinLeaveSound;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerJoinLeaveListener implements Listener {

    private final JoinLeaveSound plugin;
    private long lastJoinSound = 0;
    private long lastLeaveSound = 0;

    public PlayerJoinLeaveListener(JoinLeaveSound plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (plugin.isPlayerMuted(player)) return;

        FileConfiguration config = plugin.getConfig();
        int cooldown = config.getInt("cooldown.join", 10);
        long now = System.currentTimeMillis();

        if (cooldown > 0 && now - lastJoinSound < cooldown * 1000L) {
            lastJoinSound = now;
            return;
        }

        lastJoinSound = now;
        float volume = (float) config.getDouble("join-sound.volume", 1.0);
        float pitch  = (float) config.getDouble("join-sound.pitch", 1.0);
        playGlobalSound(plugin.getJoinSound(), volume, pitch);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (plugin.isPlayerMuted(player)) return;

        FileConfiguration config = plugin.getConfig();
        int cooldown = config.getInt("cooldown.leave", 10);
        long now = System.currentTimeMillis();

        if (cooldown > 0 && now - lastLeaveSound < cooldown * 1000L) {
            lastLeaveSound = now;
            return;
        }

        lastLeaveSound = now;
        float volume = (float) config.getDouble("leave-sound.volume", 1.0);
        float pitch  = (float) config.getDouble("leave-sound.pitch", 1.0);
        playGlobalSound(plugin.getLeaveSound(), volume, pitch);
    }

    private void playGlobalSound(Sound sound, float volume, float pitch) {
        if (sound == null) return;
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            player.playSound(player.getLocation(), sound, volume, pitch);
        }
    }
}
