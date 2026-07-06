# JoinLeaveSound

轻量级 Bukkit/Spigot/Paper 插件，在玩家加入或离开服务器时向全服播放自定义音效。ouo

## 特性
- 自定义加入/离开音效（音量、音高可配）
- 独立的静音玩家管理（`/jls mute/unmute <玩家名>`）
- 配置热重载（`/jls reload`）
- 命令 Tab 补全
- 纯 Bukkit API，无第三方依赖

## 命令与权限
| 命令 | 权限 | 说明 |
|------|------|------|
| `/jls reload` | `joinleavesound.reload` | 重载配置 |
| `/jls mute <玩家名>` | `joinleavesound.mute` | 静音指定玩家 |
| `/jls unmute <玩家名>` | `joinleavesound.mute` | 取消静音 |

权限默认授予 OP。

## 构建
需要 Maven 与 JDK 8+。

```bash
mvn clean package
```

生成的 jar 位于 `target/` 目录。

## 安装
将 jar 放入服务器 `plugins/` 文件夹，重启或热加载即可。

## 配置
编辑 `plugins/JoinLeaveSound/config.yml` 和 `messages.yml`。

## 开源许可
MIT License
