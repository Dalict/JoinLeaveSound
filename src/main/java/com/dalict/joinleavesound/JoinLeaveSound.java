package com.dalict.joinleavesound;

import com.dalict.joinleavesound.command.MuteCommand;
import com.dalict.joinleavesound.listener.PlayerJoinLeaveListener;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
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

    // 分组配置
    private File groupsFile;
    private FileConfiguration groupsConfig;
    private boolean enableDefault;
    private boolean enableGroups;
    private boolean newbieEnabled;
    private SoundConfig newbieJoinSound;
    private SoundConfig defaultJoinSound;
    private SoundConfig defaultLeaveSound;
    private List<Group> groups = new ArrayList<>();

    private final Set<String> mutedPlayers = ConcurrentHashMap.newKeySet();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadMessages();
        loadGroups();
        loadMutedPlayers();

        getCommand("joinleavesound").setExecutor(new MuteCommand(this));
        getCommand("joinleavesound").setTabCompleter(new MuteCommand(this));

        getServer().getPluginManager().registerEvents(new PlayerJoinLeaveListener(this), this);
        getLogger().info("JoinLeaveSound 1.1.0 已启用！");
    }

    @Override
    public void onDisable() {
        getLogger().info("JoinLeaveSound 已禁用！");
    }

    public void reloadPlugin() {
        saveDefaultConfig();
        reloadConfig();
        loadMessages();
        loadGroups();
        loadMutedPlayers();
        getLogger().info("配置文件与消息已重载。");
    }

    // ---------- 语言文件加载 ----------
    private void loadMessages() {
        String language = getConfig().getString("language", "zh_CN");
        File langDir = new File(getDataFolder(), "lang");
        if (!langDir.exists()) langDir.mkdirs();

        messagesFile = new File(langDir, language + ".yml");

        if (!messagesFile.exists()) {
            String resPath = "lang/" + language + ".yml";
            if (getResource(resPath) != null) {
                saveResource(resPath, false);
            }
        }

        if (!messagesFile.exists()) {
            getLogger().warning("语言文件 " + language + ".yml 不存在，回退到 zh_CN");
            messagesFile = new File(langDir, "zh_CN.yml");
            if (!messagesFile.exists()) {
                saveResource("lang/zh_CN.yml", false);
            }
        }

        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    // ---------- 分组加载 ----------
    private void loadGroups() {
        if (groupsFile == null) {
            groupsFile = new File(getDataFolder(), "groups.yml");
        }
        if (!groupsFile.exists()) {
            saveResource("groups.yml", false);
        }
        groupsConfig = YamlConfiguration.loadConfiguration(groupsFile);

        enableDefault = groupsConfig.getBoolean("enable-default", true);
        enableGroups = groupsConfig.getBoolean("enable-groups", true);

        ConfigurationSection newbieSection = groupsConfig.getConfigurationSection("newbie");
        if (newbieSection != null) {
            newbieEnabled = newbieSection.getBoolean("enabled", false);
            newbieJoinSound = loadSoundConfig(newbieSection, "join-sound");
        } else {
            newbieEnabled = false;
            newbieJoinSound = null;
        }

        ConfigurationSection defaultSection = groupsConfig.getConfigurationSection("default");
        if (defaultSection != null) {
            defaultJoinSound = loadSoundConfig(defaultSection, "join-sound");
            defaultLeaveSound = loadSoundConfig(defaultSection, "leave-sound");
        } else {
            defaultJoinSound = null;
            defaultLeaveSound = null;
        }

        groups.clear();
        ConfigurationSection groupsSection = groupsConfig.getConfigurationSection("groups");
        if (groupsSection != null) {
            for (String key : groupsSection.getKeys(false)) {
                ConfigurationSection groupSection = groupsSection.getConfigurationSection(key);
                if (groupSection == null) continue;

                String permission = groupSection.getString("permission");
                if (permission == null || permission.isEmpty()) continue;

                SoundConfig join = loadSoundConfig(groupSection, "join-sound");
                SoundConfig leave = loadSoundConfig(groupSection, "leave-sound");

                groups.add(new Group(permission, join, leave));
            }
        }
    }

    private SoundConfig loadSoundConfig(ConfigurationSection section, String path) {
        ConfigurationSection soundSection = section.getConfigurationSection(path);
        if (soundSection == null) return null;
        return loadSoundConfigFromSection(soundSection);
    }

    private SoundConfig loadSoundConfigFromSection(ConfigurationSection section) {
        String name = section.getString("sound");
        float volume = (float) section.getDouble("volume", 1.0);
        float pitch = (float) section.getDouble("pitch", 1.0);
        return new SoundConfig(name, volume, pitch);
    }

    // ---------- Getters ----------
    public boolean isNewbieEnabled() { return newbieEnabled; }
    public SoundConfig getNewbieJoinSound() { return newbieJoinSound; }

    public boolean isDefaultEnabled() { return enableDefault; }
    public SoundConfig getDefaultJoinSound() { return defaultJoinSound; }
    public SoundConfig getDefaultLeaveSound() { return defaultLeaveSound; }

    public boolean isGroupsEnabled() { return enableGroups; }

    public Group getGroupForPlayer(Player player) {
        if (!enableGroups) return null;
        for (Group group : groups) {
            if (player.hasPermission(group.permission)) {
                return group;
            }
        }
        return null;
    }

    // ---------- 静音管理 ----------
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

    // ---------- 消息获取 ----------
    public String getMessage(String key) {
        String raw = messagesConfig.getString(key);
        if (raw == null) {
            getLogger().warning("消息键缺失: " + key);
            return org.bukkit.ChatColor.translateAlternateColorCodes('&', "&c消息配置异常，请联系管理员");
        }
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', raw);
    }

    // ---------- 内部类 ----------
    public static class Group {
        public final String permission;
        public final SoundConfig joinSound;
        public final SoundConfig leaveSound;

        public Group(String permission, SoundConfig joinSound, SoundConfig leaveSound) {
            this.permission = permission;
            this.joinSound = joinSound;
            this.leaveSound = leaveSound;
        }
    }

    public static class SoundConfig {
        public final Sound sound;
        public final float volume;
        public final float pitch;

        public SoundConfig(String soundName, float volume, float pitch) {
            Sound s = null;
            if (soundName != null && !soundName.isEmpty()) {
                try {
                    s = Sound.valueOf(soundName.toUpperCase());
                } catch (IllegalArgumentException ignored) {}
            }
            this.sound = s;
            this.volume = volume;
            this.pitch = pitch;
        }
    }
}
