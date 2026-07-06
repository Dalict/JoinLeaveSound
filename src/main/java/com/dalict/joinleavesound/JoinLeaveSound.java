package com.dalict.joinleavesound;

import com.dalict.joinleavesound.command.MuteCommand;
import com.dalict.joinleavesound.listener.PlayerJoinLeaveListener;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class JoinLeaveSound extends JavaPlugin {

    private FileConfiguration messagesConfig;
    private File messagesFile;
    private File mutedPlayersFile;
    private FileConfiguration mutedPlayersConfig;

    private Sound joinSound;
    private Sound leaveSound;

    private final Set<String> mutedPlayers = ConcurrentHashMap.newKeySet();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadMessages();
        loadMutedPlayers();
        validateAndCacheSounds();

        getCommand("joinleavesound").setExecutor(new MuteCommand(this));
        getCommand("joinleavesound").setTabCompleter(new MuteCommand(this));

        getServer().getPluginManager().registerEvents(new PlayerJoinLeaveListener(this), this);
        getLogger().info("JoinLeaveSound 1.0.8 已启用！");
    }

    @Override
    public void onDisable() {
        getLogger().info("JoinLeaveSound 已禁用！");
    }

    public void reloadPlugin() {
        reloadConfig();
        loadMessages();
        loadMutedPlayers();
        validateAndCacheSounds();
        getLogger().info("配置文件与消息已重载。");
    }

    private void loadMutedPlayers() {
        if (mutedPlayersFile == null) {
            mutedPlayersFile = new File(getDataFolder(), "muted-players.yml");
        }
        if (!mutedPlayersFile.exists()) {
            saveResource("muted-players.yml", false);
        }
        mutedPlayersConfig = YamlConfiguration.loadConfiguration(mutedPlayersFile);
        List<String> list = mutedPlayersConfig.getStringList("muted");
        mutedPlayers.clear();
        mutedPlayers.addAll(list.stream().map(String::toLowerCase).collect(Collectors.toList()));
    }

    public void saveMutedPlayers() {
        mutedPlayersConfig.set("muted", new ArrayList<>(mutedPlayers));
        try {
            mutedPlayersConfig.save(mutedPlayersFile);
        } catch (IOException e) {
            getLogger().severe("无法保存 muted-players.yml: " + e.getMessage());
        }
    }

    public boolean isPlayerMuted(Player player) {
        return player != null && mutedPlayers.contains(player.getName().toLowerCase());
    }

    public boolean addMutedPlayer(String name) {
        if (mutedPlayers.add(name.toLowerCase())) {
            saveMutedPlayers();
            return true;
        }
        return false;
    }

    public boolean removeMutedPlayer(String name) {
        if (mutedPlayers.remove(name.toLowerCase())) {
            saveMutedPlayers();
            return true;
        }
        return false;
    }

    private void validateAndCacheSounds() {
        FileConfiguration config = getConfig();
        joinSound = loadSoundFromConfig(config, "join-sound");
        leaveSound = loadSoundFromConfig(config, "leave-sound");
    }

    private Sound loadSoundFromConfig(FileConfiguration config, String path) {
        String soundName = config.getString(path + ".sound");
        if (soundName == null) {
            getLogger().warning("配置路径 " + path + ".sound 不存在，将禁用该音效。");
            return null;
        }
        try {
            return Sound.valueOf(soundName.toUpperCase());
        } catch (IllegalArgumentException e) {
            getLogger().warning("无效的音效名称: " + soundName + "（位于 " + path + "），该音效将被禁用。");
            return null;
        }
    }

    public Sound getJoinSound() { return joinSound; }
    public Sound getLeaveSound() { return leaveSound; }

    private void loadMessages() {
        if (messagesFile == null) {
            messagesFile = new File(getDataFolder(), "messages.yml");
        }
        if (!messagesFile.exists()) {
            saveResource("messages.yml", false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public String getMessage(String key) {
        String raw = messagesConfig.getString(key);
        if (raw == null) {
            getLogger().warning("消息键缺失: " + key + "，请检查 messages.yml。");
            return org.bukkit.ChatColor.translateAlternateColorCodes('&',
                    "&c消息配置异常，请联系管理员");
        }
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', raw);
    }
}
