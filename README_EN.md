# JoinLeaveSound - Minecraft Join/Leave Sound Plugin

[中文](README.md) | English

## Introduction
A lightweight Bukkit/Spigot/Paper plugin that plays configurable sounds to all online players when someone joins or leaves the server.  
**Author: Dalict**  
**Repository: https://github.com/Dalict/JoinLeaveSound**

---

## Features
- 🎵 Customizable join/leave sounds (volume & pitch adjustable)
- 👥 Permission-based group sound system (different sounds for different groups, prioritized)
- 🆕 Newbie first-join exclusive sound (with independent cooldown)
- 🔇 Mute player management (`/jls mute/unmute <player>`, works offline)
- 🕒 Independent cooldowns (join/leave/newbie tracked separately to prevent noise from rapid joins/leaves)
- 🔄 Hot reload (`/jls reload`)
- 🌐 Multi-language support (`lang/zh_CN.yml`, `lang/en_US.yml`)
- 🛡️ OP bypass toggle (OPs can be exempted from sounds)
- 🐛 Debug mode (detailed matching logs in console)
- 📁 Isolated storage (`muted-players.yml`, `groups.yml`)
- 🚀 Pure Bukkit API, zero dependencies

---

## Installation
1. Place `JoinLeaveSound-1.1.0.jar` into the server's `plugins/` folder.
2. Restart the server or use a plugin manager to load it.
3. Customize settings by editing files under `plugins/JoinLeaveSound/`.

---

## Configuration File Structure

| File | Purpose |
|------|---------|
| `config.yml` | Global settings (cooldown, language, OP bypass, debug) |
| `groups.yml` | Permission group sounds, default sound, newbie sound |
| `lang/zh_CN.yml` | Simplified Chinese messages |
| `lang/en_US.yml` | English messages |
| `muted-players.yml` | Muted player list (maintained automatically) |

---

## Commands & Permissions

| Command | Alias | Permission | Description |
|------|------|------|------|
| `/joinleavesound reload` | `/jls reload` | `joinleavesound.reload` | Reload all configurations and messages |
| `/joinleavesound mute <player>` | `/jls mute <player>` | `joinleavesound.mute` | Mute a player (offline supported) |
| `/joinleavesound unmute <player>` | `/jls unmute <player>` | `joinleavesound.mute` | Unmute a player |

Permissions default to OP. Tab completion supports subcommands and online player names.

---

## Permission Group Sound System (`groups.yml`)

### Matching Priority (high to low)
1. **Newbie** (when `hasPlayedBefore() == false` and `newbie.enabled: true`)
2. **Custom groups** (from top to bottom in the `groups` list, first match wins)
3. **Default group** (used when `enable-default: true`)
4. **No sound** (none of the above matched)

### Configuration Example
```yaml
# Enable default group
enable-default: true
# Enable custom groups
enable-groups: true

# Newbie first-join sound
newbie:
  enabled: true
  join-sound:
    sound: BLOCK_NOTE_BLOCK_PLING
    volume: 1.0
    pitch: 2.0

# Default group sounds
default:
  join-sound:
    sound: BLOCK_NOTE_BLOCK_PLING
    volume: 1.0
    pitch: 1.0
  leave-sound:
    sound: BLOCK_NOTE_BLOCK_BASS
    volume: 1.0
    pitch: 1.0

# Custom groups (ordered by priority)
groups:
  admin:
    permission: "joinleavesound.group.admin"
    join-sound:
      sound: UI_TOAST_CHALLENGE_COMPLETE
      volume: 1.0
      pitch: 1.0
    leave-sound:
      sound: ENTITY_LIGHTNING_BOLT_THUNDER
      volume: 1.0
      pitch: 1.0

  vip:
    permission: "joinleavesound.group.vip"
    join-sound:
      sound: ENTITY_PLAYER_LEVELUP
      volume: 1.0
      pitch: 1.2
    leave-sound:
      sound: ENTITY_ENDERMAN_TELEPORT
      volume: 1.0
      pitch: 1.0
```

### Example Permission Assignment (LuckPerms)
```
/lp user Dalict permission set joinleavesound.group.admin true
/lp group vip permission set joinleavesound.group.vip true
```

### Common Scenarios
| Configuration | Player without permission | VIP | Admin |
|------|-----------|-----|-------|
| Default on + custom groups | Default sound | VIP sound | Admin sound |
| Default off + custom groups | No sound | VIP sound | Admin sound |
| Default on + empty groups | Default sound | Default sound | Default sound |

---

## Cooldown System

### Cooldown Types
| Type | Config path | Default | Independent timer |
|------|---------|--------|---------|
| Normal join | `cooldown.join` | 10 seconds | ✅ |
| Normal leave | `cooldown.leave` | 10 seconds | ✅ |
| Newbie join | `cooldown.newbie` | 0 seconds (disabled) | ✅ |

### Cooldown Behavior
- **During cooldown**: subsequent same events will NOT play the sound, but will **reset the cooldown timer**.
- **After cooldown**: the sound can play again only after a full cooldown period has elapsed since the last event.
- **Set to 0 or negative**: disables that cooldown type.

### Example (join cooldown 10s)
1. Player A joins → sound plays, cooldown starts.
2. Player B joins after 5s → no sound, cooldown resets (counts from second 5).
3. Player C joins after another 10s → sound plays.

---

## Global Configuration (`config.yml`)

```yaml
# Cooldown in seconds (0 or negative to disable)
cooldown:
  join: 10
  leave: 10
  newbie: 0

# Server language (zh_CN / en_US)
language: en_US

# OP bypass: true = OPs never trigger sounds
op-bypass: false

# Debug mode: logs detailed matching info to console
debug: false
```

---

## Multi-Language

The plugin ships with `zh_CN` (Simplified Chinese) and `en_US` (English) language files inside the `lang/` directory.

- Change the `language` value in `config.yml` and reload to switch.
- If a language file is missing, it automatically falls back to `zh_CN`.
- To add a new language, just place a properly named `.yml` file in the `lang/` folder.

---

## FAQ

**Q: Can I disable cooldown?**  
A: Yes. Set the corresponding cooldown value to `0`.

**Q: Will muted players be completely silent?**  
A: Yes, muted players will not trigger any join/leave sounds, and cooldown is also skipped for them.

**Q: What sound names can I use?**  
A: See the [Bukkit Sound enum](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Sound.html).

**Q: Does this plugin conflict with other plugins?**  
A: No. It only listens to standard join/leave events and doesn't modify any game mechanics. Lightweight and dependency-free.

**Q: Can I add unlimited custom groups?**  
A: Technically yes, hundreds of groups won't affect performance. Matching is just a simple list iteration with hash lookups.

**Q: If newbie sound is disabled, what will new players hear?**  
A: They will follow the normal group/default matching flow, just like regular players.

**Q: Can custom permission nodes be tab-completed in LuckPerms?**  
A: No, they must be typed manually the first time. After that, LP will remember them.

**Q: Will it be noisy when many players join/leave at once?**  
A: No, the cooldown system suppresses excessive sounds, keeping the server atmosphere elegant.

---

## License
MIT License

## Support & Feedback
- Issues: [GitHub Issues](https://github.com/Dalict/JoinLeaveSound/issues)
- Source: [https://github.com/Dalict/JoinLeaveSound](https://github.com/Dalict/JoinLeaveSound)