# JoinLeaveSound - Minecraft 进出音效插件

[English](README_EN.md) | 中文

## 简介
轻量级 Bukkit/Spigot/Paper 插件，在玩家加入或离开服务器时向全服播放自定义音效。ouo
**作者：Dalict**  
**开源地址：https://github.com/Dalict/JoinLeaveSound**

---

## 特性
- 🎵 自定义加入/离开音效（音量、音高均可配）
- 👥 权限分组音效系统（不同权限组播放不同音效，优先级可排序）
- 🆕 新玩家首次加入专属音效（独立冷却控制）
- 🔇 静音玩家管理（`/jls mute/unmute <玩家名>`，支持离线添加）
- 🕒 独立冷却时间（加入/离开/新玩家分开计时，防止多人频繁进出造成噪音）
- 🔄 配置热重载（`/jls reload`）
- 🌐 多语言支持（`lang/zh_CN.yml`、`lang/en_US.yml`）
- 🛡️ OP 豁免开关（OP 玩家进出不触发音效）
- 🐛 调试模式（控制台输出详细匹配日志）
- 📁 独立存储（`muted-players.yml`、`groups.yml`）
- 🚀 纯 Bukkit API，零第三方依赖

---

## 安装
1. 将 `JoinLeaveSoundjar` 放入服务器 `plugins/` 文件夹。
2. 重启服务器或使用插件管理工具加载。
3. 编辑 `plugins/JoinLeaveSound/` 下的配置文件进行自定义。

---

## 配置文件结构

| 文件 | 用途 |
|------|------|
| `config.yml` | 全局设置（冷却、语言、OP豁免、调试） |
| `groups.yml` | 权限分组音效、默认音效、新玩家音效 |
| `lang/zh_CN.yml` | 简体中文消息 |
| `lang/en_US.yml` | English messages |
| `muted-players.yml` | 静音玩家列表（插件自动维护） |

---

## 命令与权限

| 命令 | 别名 | 权限 | 说明 |
|------|------|------|------|
| `/joinleavesound reload` | `/jls reload` | `joinleavesound.reload` | 重载所有配置与消息 |
| `/joinleavesound mute <玩家名>` | `/jls mute <玩家名>` | `joinleavesound.mute` | 静音指定玩家（不需在线） |
| `/joinleavesound unmute <玩家名>` | `/jls unmute <玩家名>` | `joinleavesound.mute` | 取消静音 |

权限默认授予 OP。Tab 补全支持子命令和在线玩家名。

---

## 权限分组音效系统（`groups.yml`）

### 匹配优先级（从高到低）
1. **新玩家**（`hasPlayedBefore() == false` 且 `newbie.enabled: true`）
2. **自定义权限组**（`groups` 列表，从上到下匹配，命中即停）
3. **默认组**（`enable-default: true` 时生效）
4. **无声**（以上均未匹配）

### 配置示例
```yaml
# 是否启用默认组
enable-default: true
# 是否启用自定义权限组
enable-groups: true

# 新玩家首次加入音效
newbie:
  enabled: true
  join-sound:
    sound: BLOCK_NOTE_BLOCK_PLING
    volume: 1.0
    pitch: 2.0

# 默认组音效
default:
  join-sound:
    sound: BLOCK_NOTE_BLOCK_PLING
    volume: 1.0
    pitch: 1.0
  leave-sound:
    sound: BLOCK_NOTE_BLOCK_BASS
    volume: 1.0
    pitch: 1.0

# 自定义权限组，优先级由上到下
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

### 权限分配示例（LuckPerms）
```
/lp user Dalict permission set joinleavesound.group.admin true
/lp group vip permission set joinleavesound.group.vip true
```

### 常见场景
| 配置 | 无权限玩家 | VIP | Admin |
|------|-----------|-----|-------|
| 默认开启 + 自定义组 | 默认音效 | VIP音效 | Admin音效 |
| 默认关闭 + 自定义组 | 无声 | VIP音效 | Admin音效 |
| 默认开启 + 自定义组为空 | 默认音效 | 默认音效 | 默认音效 |

---

## 冷却机制

### 冷却类型
| 类型 | 配置路径 | 默认值 | 独立计时 |
|------|---------|--------|---------|
| 普通加入 | `cooldown.join` | 10秒 | ✅ |
| 普通离开 | `cooldown.leave` | 10秒 | ✅ |
| 新玩家加入 | `cooldown.newbie` | 0秒（无冷却） | ✅ |

### 冷却行为
- **冷却期间**：同类事件发生时不播放音效，但**重置冷却计时**
- **冷却结束**：最后一次事件后等待完整冷却时间，才能再次触发
- **设为 0 或负数**：关闭该类型冷却

### 举例（加入冷却 10 秒）
1. 玩家 A 加入 → 播放音效，冷却开始
2. 5 秒后玩家 B 加入 → 不播放，冷却重置（从第 5 秒重新计时）
3. 再过 10 秒后玩家 C 加入 → 播放音效

---

## 全局配置（`config.yml`）

```yaml
# 冷却时间（秒），0 或负数表示无冷却
cooldown:
  join: 10
  leave: 10
  newbie: 0

# 服务器统一语言（zh_CN / en_US）
language: zh_CN

# OP 玩家进出是否触发音效（true = 不触发）
op-bypass: false

# 调试模式（控制台输出详细匹配日志）
debug: false
```

---

## 多语言

插件内置 `zh_CN`（简体中文）和 `en_US`（英文）两个语言文件，位于 `lang/` 目录。

- 修改 `config.yml` 中的 `language` 值后重载即可切换
- 语言文件缺失时自动回退到 `zh_CN`
- 如需其他语言，自行在 `lang/` 目录创建标准命名的 `.yml` 文件即可

---

## 常见问题

**Q：可以设置无冷却吗？**  
A：可以。将对应冷却值设为 `0` 即可。

**Q：静音玩家会完全没声音吗？**  
A：是的，静音玩家的加入、离开均不播放任何音效，冷却也会跳过。

**Q：音效名有哪些可以选择？**  
A：请参阅 [Bukkit Sound 枚举](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Sound.html)。

**Q：插件会和其他插件冲突吗？**  
A：不会。仅监听标准加入/离开事件，不修改游戏机制，轻量无依赖。

**Q：可以无限加自定义权限组吗？**  
A：技术上可以，几百个组也不影响性能。匹配逻辑是简单的列表遍历 + 哈希查找。

**Q：新玩家关闭后听到什么音效？**  
A：走正常的权限组/默认组匹配流程，和普通玩家一样。

**Q：自定义权限节点能在 LuckPerms 中补全吗？**  
A：不能自动补全，需手动输入完整节点名。输入过一次后 LP 会记住。

**Q：多人同时进出会很吵吗？**  
A：不会，冷却机制会抑制短时间内的大量音效，保持安静优雅。

---

## 开源协议
MIT License

## 支持与反馈
- 问题反馈：[GitHub Issues](https://github.com/Dalict/JoinLeaveSound/issues)
- 源码仓库：[https://github.com/Dalict/JoinLeaveSound](https://github.com/Dalict/JoinLeaveSound)