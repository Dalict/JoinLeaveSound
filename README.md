# JoinLeaveSound 插件文档

## 简介
轻量级 Bukkit/Spigot/Paper 插件，在玩家加入或离开服务器时向全服播放自定义音效。ouo

## 特性
- 自定义加入/离开音效（音量、音高均可配）
- 静音玩家管理（`/jls mute/unmute <玩家名>`，支持离线添加）
- 独立冷却时间（加入/离开分开计时，防止多人频繁进出造成的噪音）
- 配置热重载（`/jls reload`）
- 独立存储静音列表（`muted-players.yml`）

## 安装
1. 将 `JoinLeaveSound-1.0.9.jar` 放入服务器 `plugins/` 文件夹。
2. 重启服务器或使用插件管理工具加载。

## 命令与权限

| 命令 | 别名 | 权限 | 说明 |
|------|------|------|------|
| `/joinleavesound reload` | `/jls reload` | `joinleavesound.reload` | 重载所有配置与消息 |
| `/joinleavesound mute <玩家名>` | `/jls mute <玩家名>` | `joinleavesound.mute` | 静音指定玩家（不需在线） |
| `/joinleavesound unmute <玩家名>` | `/jls unmute <玩家名>` | `joinleavesound.mute` | 取消静音 |

权限默认授予 OP。

## 冷却机制
- 加入与离开各自拥有独立的冷却计时，互不干扰。
- 冷却时间可在 `config.yml` 中设置（单位：秒）。
- **冷却期间**：若有同类事件发生（如又有人加入），**不会播放音效，但冷却计时会重置**。
- **冷却结束**：当最后一波事件结束后，需等待完整的冷却时间，才能再次触发音效。

**举例**（加入冷却设为 10 秒）：
1. 玩家 A 加入 → 播放音效，冷却开始。
2. 5 秒后玩家 B 加入 → 不播放，冷却重置（从第 5 秒重新倒计时）。
3. 再过 10 秒后玩家 C 加入 → 播放音效。

此设计可有效防止玩家短时间集中进出导致的音效轰炸。

**Q：可以设置无冷却吗？**  
A：可以。将 `cooldown.join` 和 `cooldown.leave` 设为 `0` 即可。

**Q：静音玩家会完全没声音吗？**  
A：是的，静音玩家的加入、离开均不会播放任何音效（冷却也会跳过）。

**Q：音效名有哪些可以选择？**  
A：请参阅 [Bukkit Sound 枚举](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Sound.html)。

**Q：插件会和其他插件冲突吗？**  
A：不会。仅监听标准加入/离开事件，不修改游戏机制，轻量无依赖。