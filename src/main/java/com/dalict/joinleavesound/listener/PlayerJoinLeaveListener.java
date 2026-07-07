package com.dalict.joinleavesound.listener;

import com.dalict.joinleavesound.JoinLeaveSound;
import com.dalict.joinleavesound.JoinLeaveSound.Group;
import com.dalict.joinleavesound.JoinLeaveSound.SoundConfig;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerJoinLeaveListener implements Listener {

    private final JoinLeaveSound plugin;
    private long lastJoinSound = 0;
    private long lastLeaveSound = 0;
    private long lastNewbieSound = 0;

    public PlayerJoinLeaveListener(JoinLeaveSound plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (plugin.getConfig().getBoolean("op-bypass", false) && player.isOp()) {
            debug("OP 豁免: " + player.getName() + " 加入");
            return;
        }

        if (plugin.isPlayerMuted(player)) {
            debug("静音跳过: " + player.getName() + " 加入");
            return;
        }

        // 确定音效配置
        SoundConfig config = null;
        String groupName = "无";
        boolean isNewbie = false;

        if (!player.hasPlayedBefore() && plugin.isNewbieEnabled()) {
            config = plugin.getNewbieJoinSound();
            groupName = "新玩家";
            isNewbie = true;
        } else {
            if (plugin.isGroupsEnabled()) {
                Group group = plugin.getGroupForPlayer(player);
                if (group != null) {
                    config = group.joinSound;
                    groupName = group.permission;
                }
            }
            if (config == null && plugin.isDefaultEnabled()) {
                config = plugin.getDefaultJoinSound();
                groupName = "默认组";
            }
        }

        if (config == null) {
            debug("未匹配任何组: " + player.getName());
            return;
        }

        // 冷却检查
        long now = System.currentTimeMillis();
        if (isNewbie) {
            int newbieCooldown = plugin.getConfig().getInt("cooldown.newbie", 0);
            if (newbieCooldown > 0 && now - lastNewbieSound < newbieCooldown * 1000L) {
                lastNewbieSound = now;
                debug("新玩家冷却中: " + player.getName() + " (重置计时)");
                return;
            }
            lastNewbieSound = now;
        } else {
            int joinCooldown = plugin.getConfig().getInt("cooldown.join", 10);
            if (joinCooldown > 0 && now - lastJoinSound < joinCooldown * 1000L) {
                lastJoinSound = now;
                debug("冷却中: " + player.getName() + " 加入 (重置计时)");
                return;
            }
            lastJoinSound = now;
        }

        debug("播放: " + player.getName() + " | 组: " + groupName +
              " | 音效: " + (config.sound != null ? config.sound.name() : "null") +
              " | 音量: " + config.volume + " | 音高: " + config.pitch);

        playGlobalSound(config);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (plugin.getConfig().getBoolean("op-bypass", false) && player.isOp()) {
            debug("OP 豁免: " + player.getName() + " 离开");
            return;
        }

        if (plugin.isPlayerMuted(player)) {
            debug("静音跳过: " + player.getName() + " 离开");
            return;
        }

        int cooldown = plugin.getConfig().getInt("cooldown.leave", 10);
        long now = System.currentTimeMillis();
        if (cooldown > 0 && now - lastLeaveSound < cooldown * 1000L) {
            lastLeaveSound = now;
            debug("冷却中: " + player.getName() + " 离开 (重置计时)");
            return;
        }
        lastLeaveSound = now;

        SoundConfig config = null;
        String groupName = "无";

        if (plugin.isGroupsEnabled()) {
            Group group = plugin.getGroupForPlayer(player);
            if (group != null) {
                config = group.leaveSound;
                groupName = group.permission;
            }
        }

        if (config == null && plugin.isDefaultEnabled()) {
            config = plugin.getDefaultLeaveSound();
            groupName = "默认组";
        }

        if (config == null) {
            debug("未匹配任何组: " + player.getName());
            return;
        }

        debug("播放: " + player.getName() + " | 组: " + groupName +
              " | 音效: " + (config.sound != null ? config.sound.name() : "null") +
              " | 音量: " + config.volume + " | 音高: " + config.pitch);

        playGlobalSound(config);
    }

    private void playGlobalSound(SoundConfig config) {
        if (config == null || config.sound == null) return;
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            player.playSound(player.getLocation(), config.sound, config.volume, config.pitch);
        }
    }

    private void debug(String msg) {
        if (plugin.getConfig().getBoolean("debug", false)) {
            plugin.getLogger().info("[Debug] " + msg);
        }
    }
}
