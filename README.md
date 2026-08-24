# ZeroEngine

> 一款现代化、模块化的 Minecraft 服务器引擎，提供完整的服务器管理工具、原版操控能力和简洁的 API 供其他插件调用。

![Java](https://img.shields.io/badge/Java-21-orange)
![Bukkit](https://img.shields.io/badge/Bukkit-1.21.5-green)
![Version](https://img.shields.io/badge/Version-3.2.4--LTS-blue)
![License](https://img.shields.io/badge/License-GPLv3-blue)

## 目录

- [✨ 特性](#-特性)
- [📦 安装](#-安装)
- [🚀 快速开始](#-快速开始)
- [🎮 命令参考](#-命令参考)
    - [系统命令](#系统命令)
    - [世界管理命令](#世界管理命令)
    - [聊天系统命令](#聊天系统命令)
    - [权限系统命令](#权限系统命令)
    - [附魔系统命令](#附魔系统命令)
    - [物品系统命令](#物品系统命令)
- [⚡ SF Tick 系统](#-sf-tick-系统)
- [🔮 自定义附魔系统](#-自定义附魔系统)
- [🎯 SFAttr 属性常量库](#-sfattr-属性常量库)
- [🎒 自定义物品系统](#-自定义物品系统)
- [🧟 自定义生物系统](#-自定义生物系统)
- [📝 SFText 文本组件 API](#-sftext-文本组件-api)
- [💬 聊天事件优先级 API](#-聊天事件优先级-api)
- [🚀 性能优化系统](#-性能优化系统)
- [🗄️ SQLite / MySQL 数据库 API](#-sqlite--mysql-数据库-api)
- [⚙️ ZeroEngine 原版操控引擎](#-zeroengine-原版操控引擎)
    - [怪物属性操控](#怪物属性操控)
    - [伤害系统操控](#伤害系统操控)
    - [方块/挖掘操控](#方块挖掘操控)
    - [实体生成操控](#实体生成操控)
    - [资源包管理](#资源包管理)
- [🔐 权限列表](#-权限列表)
- [⚙️ 配置文件](#️-配置文件)
- [💻 开发者 API](#-开发者-api)
    - [API 接口文档](#api-接口文档)
    - [📦 箱子 GUI 系统（ChestGUI）](#-箱子-gui-系统chestgui)
    - [玩法功能 API（v3 新增）](#玩法功能-apiv3-新增)
        - [🛏️ 起床战争（Bedwars）](#-起床战争bedwars)
        - [⚔️ PVP 竞技（PvPArena）](#-pvp-竞技pvparena)
        - [🧟 惊变尸潮（Horde）](#-惊变尸潮horde)
        - [🏰 保卫村庄（VillageDefense）](#-保卫村庄villagedefense)
    - [API 接入示例](#api-接入示例)
- [❓ 常见问题](#-常见问题)
- [📝 变更日志](#-变更日志)
- [🤝 贡献指南](#-贡献指南)
- [📄 License](#-license)

---

## ✨ 特性

- 🚀 **极简 API**：一行代码完成日志、经济、传送、调度等操作
- 🎯 **20+ 内置命令**：世界管理、聊天、权限、附魔、物品一应俱全
- 💰 **双后端经济**：自动检测 EssentialsX / Vault，无需手动配置
- 🗄️ **持久化存储**：内置 SQLite / MySQL 切换，零配置开箱即用
- 🔔 **完整事件系统**：120+ Bukkit 事件分类封装，链式调用
- ⚡ **SF Tick 系统**：独立线程 100tick/秒，不干扰原版 20tick/秒
- 🌍 **世界管理**：时间/天气/难度/PVP/世界边界/生物生成/火焰蔓延/预设
- 💬 **聊天系统**：多频道、禁言、脏话过滤、聊天格式化
- 🔑 **权限系统**：权限组、继承、前缀后缀、个人权限
- 🔮 **附魔注册系统**：继承 `SEnchantment` 自定义附魔，铁砧附魔支持
- 🎯 **SFAttr 属性常量库**：全部 Bukkit Attribute 枚举封装、中文名、快捷构造，自动兼容多版本
- 🎒 **物品注册系统**：继承 `SItem` 自定义物品，属性加成、交互事件
- 🧟 **生物注册系统**：继承 `SEntity` 自定义生物，血量/攻击/阵营/生成条件/装备掉落/SFTick 钩子
- 📝 **SFText 文本组件**：物品精灵图、玩家头颅、富文本交互（URL/命令/复制/hover）
- 💬 **聊天优先级 API**：`ChatHandler` 按优先级消费聊天消息，插件可拦截玩家输入
- 🚀 **性能优化系统**：内存监控、区块卸载、实体清理、TPS 自适应视距
- ⚙️ **ZeroEngine 原版操控引擎**：怪物属性、伤害系统、方块挖掘、实体生成、资源包管理 5 大引擎模块
- 🔌 **第三方接入**：通过 Bukkit ServicesManager 暴露 `SFApi` 接口
- ⚡ **异步安全**：经济操作自动回滚

---

## 📦 安装

### 环境要求

| 组件 | 版本 |
|------|------|
| Minecraft Server | 1.21.5+ |
| Java | 21+ |
| Bukkit/Paper API | 1.21.5 |

**支持的服务端**：Paper、Purpur、Folia（实验性）、Spigot、CraftBukkit

### 可选依赖

以下插件非必需，但推荐安装以解锁更多功能：

| 插件 | 用途 | 下载 |
|------|------|------|
| EssentialsX | 经济系统后端（优先） | [essentialsx.net](https://essentialsx.net/) |
| Vault | 经济系统后端（回退） | [spigotmc.org](https://www.spigotmc.org/resources/vault.34315/) |

> 💡 如果两个都安装，会优先使用 EssentialsX；若都未安装，经济相关功能会自动禁用，但不影响其他功能。

### 安装步骤

**1. 下载插件**

从 [Releases 页面](https://github.com/zmb96/ZeroCkate_ServerManagementPlugin/releases) 下载最新版本的 `.jar` 文件。

**2. 放入插件目录**

将 jar 文件放入服务器的 `plugins/` 目录：

```
你的服务器/
├── plugins/
│   └── ZeroCkate_ServerManagementPlugin-x.x.x.jar   ← 放这里
├── server.jar
└── ...
```

**3. 启动服务器**

首次启动会自动生成以下文件：

```
plugins/ZeroCkate_SFServerPlugin/
├── config.yml          ← 主配置文件
├── data.db             ← SQLite 数据库（默认）
└── help.txt            ← /sh 命令的帮助文本
```

**4. 验证安装**

在控制台或游戏内执行 `/servermanagement`，看到帮助信息即表示安装成功。

查看经济系统状态（控制台日志）：

```
[INFO] Database ready: true
[INFO] Economy ready: true (Essentials=true, Vault=false)
[INFO] 插件已加载
```

### 升级

1. **停止服务器**
2. 备份 `plugins/ZeroCkate_SFServerPlugin/` 目录（特别是 `data.db`）
3. 替换为新版本的 jar 文件
4. 启动服务器

> ⚠️ 升级前务必备份！数据库结构可能随版本变化。

### 卸载

1. 停止服务器
2. 执行 `/servermanagement` 确认插件状态
3. 删除 `plugins/ZeroCkate_SFServerPlugin.jar`
4. （可选）删除 `plugins/ZeroCkate_SFServerPlugin/` 目录以清除所有数据

### 切换到 MySQL

默认使用 SQLite，若要切换到 MySQL：

1. 编辑 `config.yml`：

```yaml
database:
  mysql:
    enabled: true
    host: localhost
    port: 3306
    database: minecraft
    user: root
    password: "你的密码"
    prefix: "sf_"
```

2. 在 MySQL 中创建数据库：

```sql
CREATE DATABASE minecraft CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

3. 重启服务器，表会自动创建。

---

## 🚀 快速开始

本指南将带你在 5 分钟内完成 SF 插件的基础配置。

### 1. 安装插件

将 `ZeroCkate_ServerApiPlugin.jar` 放入服务器的 `plugins/` 目录，重启服务器。

### 2. 验证加载

控制台应显示：
```
[SF] 插件已加载
[SF] Economy ready: true (Essentials=..., Vault=...)
```

### 3. 基本命令

```
/servermanagement reload    # 重载配置
/sfenchant list             # 查看所有附魔
/sfitem list                # 查看所有物品
/sfworld day                # 设为白天
/sfchat help                # 聊天系统帮助
/sfperm list                # 查看权限组
/sfreach info               # 查看交互距离
```

### 4. 权限设置

使用 LuckPerms 或类似插件分配权限：

```
# 给管理员所有权限
lp group admin permission set sf.admin.* true
```

---

## 🎮 命令参考

### 系统命令

| 命令 | 用法 | 说明 | 权限 |
|------|------|------|------|
| `/servermanagement` | `/servermanagement [reload]` | 插件管理 | `servermanagement.use` |
| `/ty` | `/ty <意见内容>` | 提交意见反馈 | - |
| `/ru` | `/ru` | 显示服务器规则 | - |
| `/sh` | `/sh` | 显示帮助信息 | - |
| `/giveit` | `/giveit` | 给予预设物品 | - |

`/servermanagement` **别名**：`sm`, `svm`

### 世界管理命令

**命令**：`/sfworld`（别名 `/sfw`） ｜ **权限**：`sf.admin.world`

#### 时间控制

| 用法 | 说明 |
|------|------|
| `/sfworld time <数值> [世界]` | 设置时间为指定值 |
| `/sfworld day [世界]` | 设为白天（1000） |
| `/sfworld night [世界]` | 设为夜晚（13000） |
| `/sfworld noon [世界]` | 设为中午（6000） |
| `/sfworld midnight [世界]` | 设为午夜（18000） |
| `/sfworld locktime [世界]` | 锁定当前时间 |
| `/sfworld unlocktime [世界]` | 解锁时间流动 |

#### 天气控制

| 用法 | 说明 |
|------|------|
| `/sfworld weather sun [世界]` | 设为晴天 |
| `/sfworld weather rain [世界]` | 设为雨天 |
| `/sfworld weather storm [世界]` | 设为雷暴 |

#### 难度与 PVP

| 用法 | 说明 |
|------|------|
| `/sfworld difficulty <peaceful\|easy\|normal\|hard> [世界]` | 设置难度 |
| `/sfworld pvp <true\|false> [世界]` | 开关 PVP |

#### 世界边界

| 用法 | 说明 |
|------|------|
| `/sfworld border size <数值> [秒数] [世界]` | 设置边界大小（可选过渡秒数） |
| `/sfworld border center <x> <z> [世界]` | 设置边界中心 |
| `/sfworld border reset [世界]` | 重置世界边界 |

#### 世界规则

| 用法 | 说明 |
|------|------|
| `/sfworld mob <true\|false> [世界]` | 开关生物生成 |
| `/sfworld fire <true\|false> [世界]` | 开关火焰蔓延 |

#### 预设管理

| 用法 | 说明 |
|------|------|
| `/sfworld preset save <名称> [世界]` | 保存当前世界状态为预设 |
| `/sfworld preset apply <名称> [世界]` | 应用预设到世界 |
| `/sfworld preset list` | 列出所有预设 |

#### 信息查询

| 用法 | 说明 |
|------|------|
| `/sfworld info [世界]` | 查看世界详细信息 |
| `/sfworld list` | 列出所有已加载世界 |

### 聊天系统命令

**命令**：`/sfchat`（别名 `/sfc`）

#### 频道管理

| 用法 | 说明 |
|------|------|
| `/sfchat channel [名称]` | 查看/切换频道 |
| `/sfchat create <名称> [范围] [前缀]` | 创建新频道（范围=0 全局，>0 附近格数） |
| `/sfchat delete <名称>` | 删除自定义频道 |

内置频道：
- `global` — 全局频道（默认）
- `local` — 附近频道（100 格内可见）
- `staff` — 管理频道

**示例**：
```
/sfchat create trade 0 §7[§6交易§7]
/sfchat create city 200 §7[§a同城§7]
/sfchat channel trade
```

#### 禁言管理

| 用法 | 说明 |
|------|------|
| `/sfchat mute <玩家> [秒数] [原因]` | 禁言玩家（秒数=0 永久） |
| `/sfchat unmute <玩家>` | 解除禁言 |
| `/sfchat muteinfo <玩家>` | 查看禁言信息 |

**权限**：`sf.admin.chat`

#### 屏蔽词

| 用法 | 说明 |
|------|------|
| `/sfchat block <词语>` | 添加屏蔽词 |
| `/sfchat unblock <词语>` | 移除屏蔽词 |
| `/sfchat blocklist` | 查看屏蔽词列表 |

#### 其他

| 用法 | 说明 |
|------|------|
| `/sfchat clear` | 清空聊天屏幕 |

### 权限系统命令

**命令**：`/sfperm`（别名 `/sfp`） ｜ **权限**：`sf.admin.permission`

#### 权限组管理

| 用法 | 说明 |
|------|------|
| `/sfperm group create <组名> [前缀] [后缀] [权重]` | 创建权限组 |
| `/sfperm group setprefix <组名> <前缀>` | 设置组前缀 |
| `/sfperm group setsuffix <组名> <后缀>` | 设置组后缀 |
| `/sfperm group addperm <组名> <权限>` | 添加组权限 |
| `/sfperm group rmperm <组名> <权限>` | 移除组权限 |
| `/sfperm group inherit <组名> <父组名>` | 设置组继承 |

#### 玩家权限

| 用法 | 说明 |
|------|------|
| `/sfperm set <玩家> <组名>` | 设置玩家所属组 |
| `/sfperm addperm <玩家> <权限>` | 给玩家添加个人权限 |
| `/sfperm rmperm <玩家> <权限>` | 移除玩家个人权限 |

#### 信息查询

| 用法 | 说明 |
|------|------|
| `/sfperm info <玩家>` | 查看玩家权限信息 |
| `/sfperm list` | 列出所有权限组 |

内置权限组：
- `default` — 默认组（权重 0）
- `vip` — VIP 组（权重 10，跳过传送冷却）
- `mod` — 管理组（权重 50，继承 vip，聊天管理权限）
- `admin` — 管理员组（权重 100，继承 mod，全部管理权限）
- `owner` — 服主组（权重 200，继承 admin，所有权限）

### 附魔系统命令

**命令**：`/sfenchant`（别名 `/sfe`） ｜ **权限**：`sf.admin.enchant`

| 用法 | 说明 |
|------|------|
| `/sfenchant list` | 列出所有已注册附魔 |
| `/sfenchant book <id> [等级] [数量]` | 获取附魔书 |
| `/sfenchant apply <id> [等级]` | 将附魔应用到手持物品 |
| `/sfenchant remove <id>` | 移除手持物品上的指定附魔 |
| `/sfenchant info <id>` | 查看附魔详情 |
| `/sfenchant hand` | 查看手持物品的所有附魔 |
| `/sfenchant reload` | 重置附魔系统 |

### 物品系统命令

**命令**：`/sfitem`（别名 `/sfi`） ｜ **权限**：`sf.admin.item`

| 用法 | 说明 |
|------|------|
| `/sfitem list` | 列出所有已注册物品 |
| `/sfitem give <id> [数量] [玩家]` | 给予物品 |
| `/sfitem info <id>` | 查看物品详情 |
| `/sfitem hand` | 查看手中物品信息 |
| `/sfitem reload` | 重置物品系统 |

---

## ⚡ SF Tick 系统

SF 插件内置独立的 Tick 调度系统，**1 秒 = 100 tick**，在独立线程运行，完全不干扰原版 20tick/秒的游戏循环。

### 核心概念

| 概念 | 说明 |
|------|------|
| SF Tick | SF 自定义的时间单位，1 秒 = 100 SF tick |
| Bukkit Tick | 原版游戏 tick，1 秒 = 20 tick |
| 独立线程 | SF Tick 在 `ScheduledExecutorService` 上运行，不阻塞主线程 |

### 换算关系

| SF Tick | 秒 | Bukkit Tick |
|---------|-----|-------------|
| 1 | 0.01s | 0.2 |
| 10 | 0.1s | 2 |
| 50 | 0.5s | 10 |
| 100 | 1s | 20 |
| 600 | 6s | 120 |
| 1000 | 10s | 200 |
| 6000 | 60s | 1200 |

### API 用法

```java
TickManager tick = SF.sf().tick();

// 延迟执行（100 tick = 1 秒后）
tick.runLater(sfTick -> {
    SF.sf().info("1秒后执行");
}, 100);

// 定时循环（每 100 tick = 每秒）
tick.runTimer(sfTick -> {
    SF.sf().info("每秒执行一次，当前 tick: " + sfTick);
}, 100);

// 带延迟的定时循环
tick.runTimer(sfTick -> {
    SF.sf().broadcast("每分钟公告");
}, 6000, 6000);  // 延迟 60 秒，每 60 秒

// 取消任务
long taskId = tick.runTimer(t -> { ... }, 100);
tick.cancel(taskId);

// 获取当前 tick
long now = tick.now();

// 时间换算
long seconds = tick.toSeconds(500);         // 5
long sfTicks = tick.fromSeconds(30);        // 3000
long bukkitTicks = tick.toBukkitTicks(100);  // 20

// 需要回到主线程操作 Bukkit API 时
tick.runSync(() -> {
    player.sendMessage("在主线程执行");
});

// 延迟回到主线程
tick.runSyncLater(() -> {
    player.sendMessage("1秒后在主线程执行");
}, 100);
```

### 常量

```java
TickManager.TICKS_PER_SECOND  // 100
TickManager.TICK_INTERVAL_MS  // 10
```

### 注意事项

- SF Tick 系统在**独立线程**运行，不要在 tick 回调中直接调用 Bukkit API
- 需要操作 Bukkit API 时使用 `runSync()` / `runSyncLater()` 切回主线程
- 所有新 API 的定时功能（如禁言倒计时）都基于此系统
- 插件卸载时自动关闭 tick 线程

---

## 🔮 自定义附魔系统

SF 提供全新的附魔注册系统，通过继承 `SEnchantment` 类即可创建自定义附魔，玩家可通过**铁砧**附魔到工具上。

### 创建自定义附魔

```java
import server.sf.model.api.v2.feature.enchant.SEnchantment;
import org.bukkit.attribute.Attribute;
import java.util.*;

public class MyEnchant extends SEnchantment {

    @Override
    public String id() { return "my_enchant"; }

    @Override
    public String displayName() { return "§a我的附魔"; }

    @Override
    public int maxLevel() { return 3; }

    @Override
    public Set<String> applicableItems() {
        return new HashSet<>(Arrays.asList("SWORD", "AXE"));
    }

    @Override
    public List<AttributeBonus> attributes() {
        return Arrays.asList(
            AttributeBonus.add("dmg", "GENERIC_ATTACK_DAMAGE", 2.0, 1.0)
        );
    }

    @Override
    public void onAttack(EnchantContext ctx) {
        if (ctx.level() >= 2) {
            ctx.target().setFireTicks(40);
        }
    }

    @Override
    public void onDamaged(EnchantContext ctx) {
        if (Math.random() < 0.1 * ctx.level()) {
            ctx.player().setHealth(ctx.player().getHealth() + 2);
        }
    }
}
```

### 注册附魔

```java
SF.sf().enchant().register(new MyEnchant());
```

### SEnchantment 可重写方法

| 方法 | 说明 |
|------|------|
| `id()` | 附魔唯一标识（必填） |
| `displayName()` | 游戏内显示名称（必填） |
| `maxLevel()` | 最大等级（必填） |
| `applicableItems()` | 可附魔物品类型（必填） |
| `attributes()` | 属性加成列表 |
| `conflictGroups()` | 冲突组（同组互斥） |
| `anvilCost()` | 铁砧消耗经验等级 |
| `onAttack(ctx)` | 攻击时触发 |
| `onDamaged(ctx)` | 被攻击时触发 |
| `onEquip(ctx)` | 装备时触发 |
| `onUnequip(ctx)` | 卸下时触发 |
| `onTick(ctx)` | 每刻触发（性能敏感） |

### AttributeBonus

```java
// AttributeBonus.add(名称, 属性名, 基础值, 每级增量)
AttributeBonus.add("health", "GENERIC_MAX_HEALTH", 4.0, 2.0)
// 等级1: +4.0, 等级2: +6.0, 等级3: +8.0

// AttributeBonus.multiply(名称, 属性名, 基础值, 每级增量)
AttributeBonus.multiply("speed", "GENERIC_MOVEMENT_SPEED", 0.05, 0.02)
// 等级1: +5%, 等级2: +7%, 等级3: +9%
```

### 可用属性列表

所有属性基于 Bukkit `Attribute` 枚举，系统通过反射自动查找，依次尝试 `GENERIC_` / `PLAYER_` / 无前缀三种写法，兼容不同 Paper 版本：

| 属性名 | 说明 | 操作类型建议 |
|--------|------|-------------|
| `GENERIC_MAX_HEALTH` | 最大生命值 | ADD (1.0 = 半颗心) |
| `GENERIC_ATTACK_DAMAGE` | 攻击伤害 | ADD (1.0 = 半颗心) |
| `GENERIC_ATTACK_SPEED` | 攻击速度 | ADD (4.0 = 每秒多一次) |
| `GENERIC_ARMOR` | 护甲值 | ADD |
| `GENERIC_ARMOR_TOUGHNESS` | 护甲韧性 | ADD |
| `GENERIC_KNOCKBACK_RESISTANCE` | 击退抗性 | ADD (1.0 = 完全免疫) |
| `GENERIC_MOVEMENT_SPEED` | 移动速度 | MULTIPLY (0.05 = +5%) |
| `GENERIC_FLYING_SPEED` | 飞行速度 | MULTIPLY |
| `GENERIC_LUCK` | 幸运值 | ADD |
| `GENERIC_BLOCK_INTERACTION_RANGE` | 方块交互距离 | ADD (1.0 = +1格) |
| `GENERIC_ENTITY_INTERACTION_RANGE` | 实体交互距离 | ADD (1.0 = +1格) |
| `GENERIC_GRAVITY` | 重力 | MULTIPLY (1.0 = 默认) |
| `GENERIC_JUMP_STRENGTH` | 跳跃高度 | MULTIPLY |
| `GENERIC_SCALE` | 体型大小 | MULTIPLY (1.0 = 默认) |
| `GENERIC_STEP_HEIGHT` | 自动跨越高度 | ADD (0.5 = 半格) |
| `GENERIC_FALL_DAMAGE_MULTIPLIER` | 摔落伤害倍率 | MULTIPLY |
| `GENERIC_SAFE_FALL_DISTANCE` | 安全摔落距离 | ADD |

> 如果当前服务端版本不支持某个属性，系统会静默跳过，不会崩溃。

### 附魔书获取方式

#### 方式一：管理员命令

```
/sfenchant book <id> [等级]
```

| 参数 | 说明 |
|------|------|
| `id` | 附魔 ID，如 `ancestral_might` |
| `等级` | 可选，指定等级（默认满级） |

**示例：**
```
/sfenchant book ancestral_might       # 获取满级祖宗之力附魔书
/sfenchant book ancestral_might 2     # 获取2级附魔书
```

#### 方式二：代码 API

```java
EnchantManager enchant = SF.sf().enchant();

// 创建附魔书（返回 ItemStack）
ItemStack book = enchant.createBook("ancestral_might");
ItemStack bookLv2 = enchant.createBook("ancestral_might", 2);

// 直接给予玩家
enchant.giveBook(player, "ancestral_might");
enchant.giveBook(player, "ancestral_might", 2);
```

#### 方式三：普通玩家被动获取（箱子战利品）

玩家打开**箱子**时，系统有概率自动生成附魔书到箱子中：

- 默认概率：**5%** 每本附魔书
- 每个箱子最多：**2** 本
- 可通过 API 调整概率和数量

```java
// 获取 EnchantChestListener 实例调整配置
// 注意：监听器在 enchant() 初始化时创建
EnchantChestListener chestListener = ...; // 需自行保存引用

chestListener.setDefaultChance(0.10);     // 设置默认概率 10%
chestListener.setMaxLootPerChest(3);      // 每箱最多 3 本
chestListener.setLootChance("my_enchant", 0.20); // 单独设置某附魔概率
chestListener.addBlacklistWorld("world_nether"); // 黑名单世界
```

> 提示：同一个箱子只会生成一次，第二次打开不会再生成。

#### 方式四：附魔台获取

玩家使用**附魔台**附魔物品时，有概率获得自定义附魔：

- 默认概率：**15%** + 书架等级加成
- 每级书架额外 +5% 概率
- 玩家会收到提示：`✨ 附魔台为你附上了 XXX!`
- 只有物品类型匹配的附魔才会出现

```java
// 获取 EnchantTableListener 实例调整配置
EnchantTableListener tableListener = ...; // 需自行保存引用

tableListener.setBaseChance(0.20);        // 基础概率 20%
tableListener.setPerLevelBonus(0.08);     // 每级书架加成 8%
tableListener.setEnchantChance("my_enchant", 0.30); // 单独设置某附魔概率
tableListener.addBlacklistWorld("world_nether");    // 黑名单世界
```

> 提示：宝藏附魔（`isTreasure() == true`）不会通过附魔台获取。

### 铁砧附魔

1. 将附魔书放在铁砧左侧
2. 将工具放在铁砧右侧
3. 消耗经验即可附魔

**附魔规则：**
- 附魔书等级 < 物品现有等级：升级一级
- 附魔书等级 ≥ 物品现有等级：取最高等级
- 与现有附魔冲突时无法附魔
- 消耗经验 = `anvilCost() × 最终等级`

---

## 🎯 SFAttr 属性常量库

`SFAttr` 封装了 **全部 Bukkit `Attribute` 枚举**，提供静态常量、中文名映射、快捷构造方法，通过 `sf().attr()` 访问，自动兼容不同 Paper 版本（`GENERIC_` 前缀 / 无前缀）。

### 获取方式

```java
SFAttr attr = sf().attr();  // 获取实例（推荐）
// 或静态工具方法：
Attribute a = SFAttr.get(SFAttr.MAX_HEALTH);
```

### 静态属性常量（30+）

不区分大小写，可写 `MAX_HEALTH` / `GENERIC_MAX_HEALTH` / `max_health`，系统自动查找：

| 常量 | 中文名 | 默认值参考 |
|------|--------|-----------|
| `MAX_HEALTH` | 最大生命 | 20.0 |
| `MOVEMENT_SPEED` | 移动速度 | 0.1 |
| `FLYING_SPEED` | 飞行速度 | 0.4 |
| `ATTACK_DAMAGE` | 攻击伤害 | 1.0 |
| `ATTACK_SPEED` | 攻击速度 | 4.0 |
| `ATTACK_KNOCKBACK` | 攻击击退 | 0.0 |
| `KNOCKBACK_RESISTANCE` | 击退抗性 | 0.0 |
| `ARMOR` | 护甲 | 0.0 |
| `ARMOR_TOUGHNESS` | 护甲韧性 | 0.0 |
| `FALL_DAMAGE_MULTIPLIER` | 坠落伤害倍率 | 1.0 |
| `LUCK` | 幸运 | 0.0 |
| `MAX_ABSORPTION` | 最大吸收值 | 0.0 |
| `BLOCK_INTERACTION_RANGE` | 方块交互距离 | 4.5 |
| `ENTITY_INTERACTION_RANGE` | 实体交互距离 | 3.0 |
| `GRAVITY` | 重力 | 0.08 |
| `SAFE_FALL_DISTANCE` | 安全坠落距离 | 3.0 |
| `BURNING_TIME` | 燃烧时间 | - |
| `MOVEMENT_EFFICIENCY` | 移动效率 | - |
| `OXYGEN_BONUS` | 氧气加成 | - |
| `WATER_MOVEMENT_EFFICIENCY` | 水中移动效率 | - |
| `ATTACK_TIME` | 攻击冷却 | - |
| `MINING_EFFICIENCY` | 挖掘效率 | - |
| `SNEAKING_SPEED` | 潜行速度 | - |
| `SUBMERGED_MINING_SPEED` | 水下挖掘速度 | - |
| `SWEEPING_DAMAGE_RATIO` | 横扫伤害比率 | - |
| `TEMPT_RANGE` | 吸引范围 | - |
| `SCALE` | 实体缩放 | 1.0 |
| `STEP_HEIGHT` | 台阶高度 | 0.6 |
| `EXPLOSION_KNOCKBACK_REDUCTION` | 爆炸击退减免（兼容旧名，建议用 `EXPLOSION_KNOCKBACK_RESISTANCE`） | - |
| `SPAWN_REINFORCEMENTS` | 僵尸增援率 | 0.0 |
| `BLOCK_BREAK_SPEED` | 方块破坏速度 | 1.0 |
| `JUMP_STRENGTH` | 跳跃强度 | 0.42 |
| `EXPLOSION_KNOCKBACK_RESISTANCE` | 爆炸击退抗性（Bukkit 1.21+ 正确名） | 0.0 |

### 工具方法

```java
// 静态查询方法
Attribute a = SFAttr.get("MAX_HEALTH");       // 按名查找
boolean exists = SFAttr.exists("MAX_HEALTH");  // 是否存在
int count = SFAttr.count();                    // 当前版本加载的属性总数
Set<String> names = SFAttr.allNames();         // 所有属性名
Collection<Attribute> all = SFAttr.all();      // 所有 Attribute 对象
String zh = SFAttr.display("MAX_HEALTH");      // 中文名："最大生命"
```

### 附魔 AttributeBonus 快捷构造

通过 `sf().attr().xxx(base, perLevel)` 一行构造 `SEnchantment.AttributeBonus`：

```java
@Override
public List<AttributeBonus> attributes() {
    SFAttr attr = sf().attr();
    return Arrays.asList(
        attr.maxHealth(4.0, 2.0),               // +最大生命：4 + 2×(等级-1)
        attr.attackDamage(3.0, 1.5),             // +攻击伤害：3 + 1.5×(等级-1)
        attr.movementSpeed(0.05, 0.02),          // +移动速度：5% + 2%/级
        attr.armor(2.0, 1.0),                    // +护甲
        attr.attackKnockback(0.5, 0.2),           // +攻击击退
        attr.luck(1.0, 0.5),                      // +幸运
        attr.fallDamageMul(0.9, -0.05),           // 坠落伤害倍率×(0.9 - 0.05/级)
        attr.scale(0.02, 0.01),                   // +体型缩放
        attr.miningEfficiency(0.1, 0.05),         // +挖掘效率
        attr.sweepingDamage(0.1, 0.05)            // +横扫伤害比率
    );
}
```

### 所有快捷方法一览

| 方法 | 说明 | 默认操作 |
|------|------|---------|
| `maxHealth(base, perLevel)` | 最大生命 | ADD |
| `attackDamage(base, perLevel)` | 攻击伤害 | ADD |
| `attackSpeed(base, perLevel)` | 攻击速度 | ADD |
| `attackKnockback(base, perLevel)` | 攻击击退 | ADD |
| `movementSpeed(base, perLevel)` | 移动速度 | ADD |
| `flyingSpeed(base, perLevel)` | 飞行速度 | ADD |
| `knockbackResistance(base, perLevel)` | 击退抗性 | ADD |
| `armor(base, perLevel)` | 护甲 | ADD |
| `armorToughness(base, perLevel)` | 护甲韧性 | ADD |
| `luck(base, perLevel)` | 幸运 | ADD |
| `maxAbsorption(base, perLevel)` | 最大吸收 | ADD |
| `blockRange(base, perLevel)` | 方块交互距离 | ADD |
| `entityRange(base, perLevel)` | 实体交互距离 | ADD |
| `followRange(base, perLevel)` | 追踪范围 | ADD |
| `fallDamageMul(base, perLevel)` | 坠落伤害倍率 | MULTIPLY |
| `gravity(base, perLevel)` | 重力 | ADD |
| `safeFallDistance(base, perLevel)` | 安全坠落距离 | ADD |
| `scale(base, perLevel)` | 实体缩放 | ADD |
| `stepHeight(base, perLevel)` | 台阶高度 | ADD |
| `miningEfficiency(base, perLevel)` | 挖掘效率 | ADD |
| `sweepingDamage(base, perLevel)` | 横扫伤害比率 | ADD |
| `sneakSpeed(base, perLevel)` | 潜行速度 | ADD |
| `submergedMining(base, perLevel)` | 水下挖掘速度 | ADD |
| `waterMoveEff(base, perLevel)` | 水中移动效率 | ADD |
| `oxygenBonus(base, perLevel)` | 氧气加成 | ADD |
| `moveEfficiency(base, perLevel)` | 移动效率 | ADD |
| `burningTime(base, perLevel)` | 燃烧时间 | ADD |
| `attackTime(base, perLevel)` | 攻击冷却 | ADD |
| `temptRange(base, perLevel)` | 吸引范围 | ADD |
| `explosionKnockbackReduction(base, perLevel)` | 爆炸击退减免 | ADD |
| `add(name, attr, base, perLevel)` | 自定义 ADD 加成 | ADD |
| `multiply(name, attr, base, perLevel)` | 自定义 MULTIPLY 加成 | MULTIPLY |
| `add(name, attr, base, perLevel, op, slot)` | 完全自定义构造 | 自由指定 |

### 多版本兼容原理

Paper 1.21+ 的 `Attribute` 枚举去除了 `GENERIC_` 前缀（旧版为 `GENERIC_MAX_HEALTH`，新版为 `MAX_HEALTH`）。`SFAttr` + `SEnchantment.findAttribute()` 的查找策略为：

1. 先从 `Attribute.values()` 预加载 **当前服务端** 的所有属性名
2. 查找时依次尝试：`无前缀` → `GENERIC_前缀` → `PLAYER_前缀` → `ZOMBIE_前缀`
3. 仍找不到则通过反射直接扫描 `Attribute.class` 的字段
4. 结果写入缓存，后续零成本命中

因此无论使用哪种写法（`MAX_HEALTH` 或 `GENERIC_MAX_HEALTH`），在任意 Paper 版本上都能正确解析。

---

## 🎒 自定义物品系统

SF 提供物品注册系统，通过继承 `SItem` 类即可创建自定义物品，支持属性加成、右键/左键交互、装备音效等。

### 创建自定义物品

```java
import server.sf.model.api.v2.feature.item.SItem;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerInteractEvent;
import java.util.*;

public class MyItem extends SItem {

    @Override
    public String id() { return "my_item"; }

    @Override
    public String displayName() { return "§a我的神器"; }

    @Override
    public Material material() { return Material.DIAMOND_SWORD; }

    @Override
    public List<String> lore() {
        return Arrays.asList("§7一把传说中的武器", "§7右键触发特殊效果");
    }

    @Override
    public List<ItemAttributeBonus> attributes() {
        return Arrays.asList(
            new ItemAttributeBonus("dmg", "GENERIC_ATTACK_DAMAGE", 5.0, 0,
                AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND)
        );
    }

    @Override
    public boolean onRightClick(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        p.setVelocity(p.getLocation().getDirection().multiply(2));
        p.getWorld().spawnParticle(Particle.FLAME, p.getLocation(), 30);
        return true;  // 取消原版右键行为
    }

    @Override
    public boolean onLeftClick(PlayerInteractEvent e) {
        e.getPlayer().sendMessage("§c左键触发！");
        return true;
    }
}
```

### 注册物品

```java
SF.sf().item().register(new MyItem());
```

### SItem 可重写方法

| 方法 | 说明 |
|------|------|
| `id()` | 物品唯一标识（必填） |
| `displayName()` | 显示名称（必填） |
| `material()` | 原版物品材质（必填） |
| `lore()` | 物品描述 |
| `attributes()` | 属性加成列表 |
| `onRightClick(e)` | 右键交互 |
| `onLeftClick(e)` | 左键交互 |
| `onEquip(e)` | 装备时触发 |
| `onUnequip(e)` | 卸下时触发 |
| `customModelData()` | 自定义模型数据 |

### 物品获取方式

#### 方式一：管理员命令

```
/sfitem give <id> [数量] [玩家]
```

| 参数 | 说明 |
|------|------|
| `id` | 物品 ID，如 `magic_scepter` |
| `数量` | 可选，物品数量（默认 1） |
| `玩家` | 可选，目标玩家（默认自己） |

**示例：**
```
/sfitem give magic_scepter                  # 给自己1个
/sfitem give magic_scepter 5                # 给自己5个
/sfitem give magic_scepter 1 Notch          # 给Notch1个
```

#### 方式二：代码 API

```java
ItemManager item = SF.sf().item();

// 给予玩家物品
item.give(player, "magic_scepter");
item.give(player, "magic_scepter", 5);

// 创建物品（用于 GUI 或箱子）
ItemStack scepter = item.create("magic_scepter");
ItemStack scepter5 = item.create("magic_scepter", 5);

// 检查/消耗物品
boolean has = item.has(player, "magic_scepter");
int count = item.count(player, "magic_scepter");
item.consume(player, "magic_scepter");
item.consume(player, "magic_scepter", 3);
```

#### 方式三：普通玩家被动获取（箱子战利品）

玩家打开**箱子**时，系统有概率自动生成自定义物品到箱子中：

- 默认概率：**3%** 每个物品
- 每个箱子最多：**1** 件
- 可通过 API 调整概率和数量

```java
// 获取 ItemChestListener 实例调整配置
ItemChestListener chestListener = ...; // 需自行保存引用

chestListener.setDefaultChance(0.05);     // 设置默认概率 5%
chestListener.setMaxLootPerChest(2);       // 每箱最多 2 件
chestListener.setItemChance("my_item", 0.10); // 单独设置某物品概率
chestListener.addBlacklistWorld("world_nether"); // 黑名单世界
```

> 提示：同一个箱子只会生成一次，第二次打开不会再生成。

### ItemAttributeBonus

```java
new ItemAttributeBonus(
    "唯一名称",           // 属性标识
    "GENERIC_ATTACK_DAMAGE", // Bukkit 属性名
    5.0,                    // 基础值
    0,                      // 额外值
    AttributeModifier.Operation.ADD_NUMBER, // 操作类型
    EquipmentSlot.HAND      // 装备槽
)
```

### 可用属性

物品系统同样使用 Bukkit `Attribute` 枚举，与附魔系统共用同一套反射查找逻辑。完整属性列表见[附魔系统 - 可用属性列表](#可用属性列表)。

装备槽（`EquipmentSlot`）可选值：

| 装备槽 | 说明 |
|--------|------|
| `HAND` | 主手 |
| `OFF_HAND` | 副手 |
| `HEAD` | 头部 |
| `CHEST` | 胸部 |
| `LEGS` | 腿部 |
| `FEET` | 脚部 |

### 内置示例

- **魔法权杖**（`magic_scepter`）：右键瞬移、左键粒子效果、速度加成

---

## 🧟 自定义生物系统

继承 `SEntity` 抽象基类即可定义自定义生物，自动覆盖**血量/攻击/速度/护甲/阵营/生成条件/装备掉落/SFTick 钩子/攻击玩家监听**等全部行为。装配入口 `sf.entities()`（懒加载，自动注册监听器、调度 SFTick、绑定 `/sfentity` 命令）。

### 注册与使用

```java
@Override
public void onEnable() {
    SF sf = SF.sf();
    sf.entities().register(new ShadowStalkerEntity());   // 注册自定义生物
}
```

三种生成方式：

```java
sf.entities().spawn("shadow_stalker", player.getLocation());   // 强生成（无视 SpawnCondition）
sf.entities().trySpawn("shadow_stalker", loc);                  // 按条件尝试自然生成
// 或在 SEntity 里设 spawnCondition().replaceVanillaSpawns = true
// → 原版同 EntityType 生物出现时自动转换
```

### SEntity 抽象基类

| 必须实现 | 说明 |
|---------|------|
| `id()` | 唯一 ID（PDC key = `sf_entity_id`）|
| `displayName()` | 显示名（实体的 customName）|
| `entityType()` | 基础材质：Bukkit `EntityType`（如 `ZOMBIE` / `HUSK` / `WITHER_SKELETON`）|

**属性方法（带默认值，可重写）**：

| 方法 | 默认值 | 说明 |
|------|-------|------|
| `maxHealth()` | 20.0 | 最大生命 |
| `attackDamage()` | 2.0 | 攻击伤害 |
| `attackSpeed()` | 4.0 | 攻击速度 |
| `movementSpeed()` | 0.3 | 移动速度 |
| `knockbackResistance()` | 0.0 | 击退抗性 |
| `armor()` | 0.0 | 护甲 |
| `armorToughness()` | 0.0 | 护甲韧性 |
| `followRange()` | 16.0 | 追踪范围 |
| `flyingSpeed()` | 0.4 | 飞行速度 |

属性在 `applyAttributes(entity)` 里通过 `SFAttr` 写入 Bukkit `AttributeInstance`，自动兼容 `GENERIC_` / 无前缀命名。

### 阵营（Hostility）

```java
public enum Hostility {
    HOSTILE,    // 主动攻击玩家
    NEUTRAL,    // 被攻击后才反击
    PASSIVE     // 永不攻击玩家
}
```

`EntityListener.onTarget` 会自动按阵营取消目标事件：
- `PASSIVE`：永远取消追踪玩家
- `NEUTRAL`：取消自然生成导致的追踪（CLOSEST_PLAYER / RANDOM_TARGET），仅在被攻击后才追
- `HOSTILE`：放行原版逻辑

### 生成条件（SpawnCondition）

链式构造：

```java
@Override
public SpawnCondition spawnCondition() {
    return new SpawnCondition()
            .chance(0.2)         // 20% 几率
            .nightOnly()         // 仅夜晚（世界时间 >= 13000）
            .burnInDay()         // 白天太阳下燃烧（怕光照）
            .light(0, 7)         // 仅在光照 0~7 的位置生成
            .world("world")      // 限定世界（可多次调用）
            .biome(Biome.PLAINS); // 限定群系（可多次调用）
}
```

| 字段 | 默认值 | 说明 |
|------|-------|------|
| `chance` | 1.0 | 生成几率 0.0~1.0 |
| `worlds` | 空=所有 | 允许生成的世界名集合 |
| `biomes` | 空=所有 | 允许的生物群系集合 |
| `minY` / `maxY` | -64 / 320 | Y 坐标范围 |
| `minLight` / `maxLight` | 0 / 15 | 光照范围（实际光照必须落在区间内才生成）|
| `burnInDaylight` | false | 怕光照，白天太阳下燃烧 |
| `onlyAtNight` | false | 只在夜晚生成 |
| `replaceVanillaSpawns` | false | 是否替换原版同类型生物（true 时原版生物出现自动转换）|
| `spawnLimitPerChunk` | 4 | 每区块最大数量 |

### 装备与掉落

**生成时穿装备**（按 `chance` 概率穿戴）：

```java
@Override
public List<EquipmentEntry> equipment() {
    return Arrays.asList(
        new EquipmentEntry(new ItemStack(Material.IRON_SWORD), 0.5, EquipmentSlot.HAND, true, 0.05),
        new EquipmentEntry(new ItemStack(Material.IRON_HELMET), 0.3, EquipmentSlot.HEAD, true, 0.10)
    );
    //              物品                  穿戴几率  装备槽      死亡掉  死亡掉率
}
```

**死亡额外掉落**：

```java
@Override
public List<ItemStack> deathDrops() {
    return Collections.singletonList(new ItemStack(Material.WITHER_ROSE, 1));
}
```

### 事件钩子

| 钩子 | 触发时机 |
|------|---------|
| `onSpawn(entity, loc, reason)` | 生物生成后（PDC 标签 + 属性 + 装备应用完）|
| `onDeath(entity, event)` | 死亡时（追加掉落已经加进 event.getDrops()）|
| `onAttack(attacker, target, damage, event)` | 攻击玩家时（仅当 target 是 Player 才触发）|
| `onDamaged(entity, event)` | 任何受伤时 |
| `onTarget(event)` | EntityTargetEvent，可用于自定义 AI |
| `onTick(entity, sfTick)` | 每 5 SFTick（= 1 Bukkit tick）调用一次 |
| `onPerSecond(entity, sfTick)` | 每 100 SFTick（= 1 秒）调用一次 |

> Bukkit 实体操作必须主线程，所以 `onTick` 通过 `runTaskTimer` 同步调度，每 5 SFTick 合批到 1 Bukkit tick 执行。

### 内置示例

**暗影猎手**（`shadow_stalker`）—— 完整演示所有特性：

```java
public class ShadowStalkerEntity extends SEntity {
    @Override public String id() { return "shadow_stalker"; }
    @Override public String displayName() { return "§5暗影猎手"; }
    @Override public EntityType entityType() { return EntityType.HUSK; }

    @Override public double maxHealth() { return 40.0; }
    @Override public double attackDamage() { return 6.0; }
    @Override public double armor() { return 4.0; }
    @Override public double knockbackResistance() { return 0.5; }
    @Override public Hostility hostility() { return Hostility.HOSTILE; }

    @Override public SpawnCondition spawnCondition() {
        return new SpawnCondition().chance(0.2).nightOnly().burnInDay().light(0, 7);
    }

    @Override public List<EquipmentEntry> equipment() {
        return Arrays.asList(
            new EquipmentEntry(new ItemStack(Material.IRON_SWORD), 0.5, EquipmentSlot.HAND, true, 0.05),
            new EquipmentEntry(new ItemStack(Material.IRON_HELMET), 0.3, EquipmentSlot.HEAD, true, 0.10)
        );
    }

    @Override public List<ItemStack> deathDrops() {
        return Collections.singletonList(new ItemStack(Material.WITHER_ROSE, 1));
    }

    @Override public void onAttack(LivingEntity attacker, LivingEntity target, double damage, EntityDamageByEntityEvent e) {
        if (target instanceof Player p) {
            var pe = Registry.EFFECT.get(NamespacedKey.minecraft("poison"));
            if (pe != null) p.addPotionEffect(new PotionEffect(pe, 80, 2, false, true, true));
        }
    }

    @Override public void onTick(LivingEntity entity, long sfTick) {
        if (sfTick % 20 != 0) return;  // 每 20 SFTick 拖一次粒子
        entity.getWorld().spawnParticle(Particle.DUST,
                entity.getLocation().add(0, 1.2, 0), 5, 0.3, 0.5, 0.3, 0.01,
                new Particle.DustOptions(Color.fromRGB(80, 0, 100), 1.2f));
    }

    @Override public void onPerSecond(LivingEntity entity, long sfTick) {
        if (Math.random() > 0.01) return;  // 1% 几率回血
        AttributeInstance inst = entity.getAttribute(SFAttr.get(SFAttr.MAX_HEALTH));
        if (inst != null && entity.getHealth() < inst.getValue()) {
            entity.setHealth(Math.min(inst.getValue(), entity.getHealth() + 1.0));
        }
    }
}
```

### `/sfentity` 命令（别名 `/sfe`）

| 子命令 | 作用 |
|--------|------|
| `/sfentity list` | 列出所有已注册生物（id/名称/类型/阵营/HP/攻击/活动数）|
| `/sfentity spawn <id> [数量]` | 在玩家脚下生成（最多 50）|
| `/sfentity info <id>` | 查看属性 / 装备 / 生成条件 / 当前活动数 |
| `/sfentity count [id]` | 查看活动实例数 |
| `/sfentity cleanup` | 清理无效引用 |
| `/sfentity reload` | 清空注册表（需代码重新注册）|
| `/sfentity help` | 帮助 |

权限：`sf.admin.entity`（默认 op）

---

## 📝 SFText 文本组件 API

`SFText` 是基于 Adventure Component 的富文本工具类，支持物品精灵图、玩家头颅、交互组件等，全部静态方法调用。

### 物品精灵图

在聊天消息中显示物品图标，鼠标悬停查看物品详情：

```java
import server.sf.model.api.v2.feature.text.SFText;

// 显示玩家手持物品（hover 弹出物品 NBT 信息）
Component c = SFText.item(player.getInventory().getItemInMainHand());

// 自定义显示名
Component c = SFText.item(itemStack, "附魔剑");
```

### 玩家头颅

```java
// 按 OfflinePlayer 显示
Component c = SFText.skull(player);

// 按 UUID + 名称
Component c = SFText.skull(uuid, "Steve");

// 按 base64 材质
Component c = SFText.skullByTexture(base64Texture, "自定义头");
```

### 交互组件

```java
SFText.url("点击打开", "https://github.com")       // 点击打开链接
SFText.command("执行", "/spawn")                    // 点击执行命令
SFText.suggest("填入", "/msg ")                     // 点击填入聊天框
SFText.copy("复制IP", "mc.example.com")             // 点击复制到剪贴板
SFText.tooltip("悬停查看", "这是提示文字")            // hover 提示
```

### Builder 链式拼接

```java
Component msg = SFText.builder()
    .append("获得: ", NamedTextColor.GOLD)
    .appendItem(itemStack)
    .append("  ")
    .appendSkull(player)
    .append("  ")
    .appendUrl("查看详情", "https://...")
    .build();
player.sendMessage(msg);
```

### 辅助方法

| 方法 | 说明 |
|------|------|
| `text(String)` | 纯文本组件 |
| `text(String, NamedTextColor)` | 带颜色文本 |
| `text(String, String hexColor)` | HEX 颜色文本 |
| `newline()` | 换行 |
| `separator()` | 分隔线 |
| `plain(Component)` | Component 转纯文本 |

---

## 💬 聊天事件优先级 API

通过 `ChatHandler` 接口注册聊天处理器，按优先级依次执行。插件可以拦截玩家聊天作为输入，或修改消息内容。

### 核心概念

| 方法 | 作用 |
|------|------|
| `ctx.consume()` | 消息是插件输入，不广播到聊天 |
| `ctx.cancel()` | 中断后续 handler 执行 |
| `ctx.formattedMessage(Component)` | 修改消息内容 |
| `ctx.channel(ChatChannel)` | 切换消息频道 |

### 注册 Handler

```java
SF.sf().chat().registerHandler(new ChatHandler() {
    @Override public int priority() { return 10; }  // 数值越小越先执行

    @Override public void handle(ChatContext ctx) {
        // 修改消息内容
        ctx.formattedMessage(
            SFText.builder()
                .append("[自定义] ")
                .append(ctx.formattedMessage())
                .build()
        );
    }
});
```

### 插件输入拦截示例

```java
// 插件等待玩家输入确认
SF.sf().chat().registerHandler(new ChatHandler() {
    @Override public int priority() { return 5; }

    @Override public void handle(ChatContext ctx) {
        if (waitingInput.containsKey(ctx.player().getUniqueId())
                && ctx.rawMessage().equals("确认")) {
            ctx.consume();  // 吃掉消息，不广播
            handleConfirm(ctx.player());
        }
    }
});
```

### 一次性输入标记

如果插件只需要玩家下一条消息作为输入，可以使用 `markListening` 机制：

```java
// 标记玩家，下一条聊天消息会被吞掉（不广播）
SF.sf().chat().markListening(player);

// 判断当前是否在监听
if (SF.sf().isPluginListenerChat(player)) {
    // true → 消息不会发出
}

// 中途取消
SF.sf().chat().unmarkListening(player);
```

> `markListening` 是一次性消费：标记后玩家下一条消息被拦截，标记自动清除。

### 处理流程

```
玩家发消息
  ↓
禁言检查 → 过滤词 → 频道格式化
  ↓
dispatch（按 priority 顺序）
  ├─ Handler A (priority=5): 检查是不是插件输入 → consume()
  ├─ Handler B (priority=10): 修改消息内容
  └─ Handler C (priority=20): cancel() → C 之后的 handler 不再执行
  ↓
consumed == true → 不广播
consumed == false → 正常广播到频道
```

---

## 🚀 性能优化系统

通过 `/sfperf` 命令管理，4 大优化模块全部基于 SF Tick 系统异步运行。

### 命令

```
/sfperf              # 完整状态报告
/sfperf tps          # TPS + MSPT
/sfperf mem          # 内存使用
/sfperf gc           # 手动 GC
/sfperf chunks       # 手动清理区块
/sfperf entities     # 手动清理实体
/sfperf toggle <feature>  # 开关某个模块
/sfperf help         # 帮助
```

**权限**：`sf.admin.perf`

### 优化模块

**1. 内存监控**（每 2 秒）
- 读取 JMX Heap 使用率
- 85% 告警，90% 自动触发 `System.gc()`

**2. 区块管理**（每 6 秒）
- 自动卸载无玩家、无实体的空闲区块
- 每周期最多卸载 50 个，避免卡顿

**3. 实体清理**（每 6 秒）
- 掉落物超过 60 秒自动清除
- 无主弹射物超过 10 秒清除
- 单区块实体超过 50 个时清理多余掉落物/弹射物

**4. TPS 自适应**（每 2 秒）
- TPS < 15：视距/模拟距离降到最小值
- TPS < 18：视距 -2，模拟距离 -1
- TPS 正常：恢复最大值

### API

```java
PerformanceManager perf = SF.sf().perf();

perf.getTps();           // 获取当前 TPS（double[3]）
perf.getUsedMemory();    // 已用内存（MB）
perf.getMaxMemory();     // 最大内存（MB）
perf.toggle("memory");   // 开关内存监控
perf.toggle("chunks");   // 开关区块管理
perf.toggle("entities"); // 开关实体清理
perf.toggle("throttle"); // 开关 TPS 自适应
```

---

## 🗄️ SQLite / MySQL 数据库 API

SF 插件内置数据库系统，默认使用 SQLite（零配置），也可切换 MySQL。通过 `sf().database()` 获取 `Database` 接口，直接执行 SQL。

### 获取 Database 实例

```java
Database db = sf().database();
```

### Database 接口

```java
public interface Database {
    boolean connect();
    void disconnect();
    boolean isConnected();
    Connection connection();
    int executeUpdate(String sql, Object... params);
    <T> T executeQuery(String sql, Function<ResultSet, T> mapper, Object... params);
}
```

### 建表

```java
Database db = sf().database();

db.executeUpdate("CREATE TABLE IF NOT EXISTS player_tags (" +
    "uuid VARCHAR(36) NOT NULL," +
    "tag_id VARCHAR(64) NOT NULL," +
    "purchased_at BIGINT NOT NULL," +
    "PRIMARY KEY (uuid, tag_id)" +
    ")");
```

### 插入 / 更新 / 删除

```java
// 插入
db.executeUpdate(
    "INSERT OR IGNORE INTO player_tags (uuid, tag_id, purchased_at) VALUES (?, ?, ?)",
    player.getUniqueId().toString(), "tag1", System.currentTimeMillis()
);

// 更新
db.executeUpdate(
    "UPDATE player_tags SET tag_id = ? WHERE uuid = ?",
    "tag2", player.getUniqueId().toString()
);

// 删除
db.executeUpdate(
    "DELETE FROM player_tags WHERE uuid = ? AND tag_id = ?",
    player.getUniqueId().toString(), "tag1"
);
```

### 查询

```java
// 查询单值
boolean has = db.executeQuery(
    "SELECT COUNT(*) AS c FROM player_tags WHERE uuid = ? AND tag_id = ?",
    rs -> {
        try {
            return rs.next() ? rs.getInt("c") > 0 : false;
        } catch (Exception e) {
            return false;
        }
    },
    player.getUniqueId().toString(), "tag1"
);

// 查询列表
List<String> owned = db.executeQuery(
    "SELECT tag_id FROM player_tags WHERE uuid = ?",
    rs -> {
        List<String> list = new ArrayList<>();
        try {
            while (rs.next()) list.add(rs.getString("tag_id"));
        } catch (Exception ignored) {
        }
        return list;
    },
    player.getUniqueId().toString()
);
```

### 内置数据表

SF 启动时自动创建以下表：

| 表名 | 用途 | 主键 |
|------|------|------|
| `homes` | 玩家家园传送点 | (uuid, name) |
| `warps` | 公共传送点 | name |
| `last_locations` | 玩家最后位置 | uuid |

第三方插件可通过 `sf().database()` 创建自己的表，与 SF 共享同一个数据库连接。

### 配置切换

默认 SQLite，切换 MySQL 编辑 `config.yml`：

```yaml
database:
  mysql:
    enabled: true        # true=MySQL, false=SQLite
    host: localhost
    port: 3306
    database: minecraft
    user: root
    password: "你的密码"
    prefix: "sf_"
  sqlite:
    file: data.db
```

### 完整示例：前缀购买系统

```java
public class TagManager {
    private final Database db = SF.sf().database();

    public void init() {
        db.executeUpdate("CREATE TABLE IF NOT EXISTS player_tags (" +
            "uuid VARCHAR(36) NOT NULL," +
            "tag_id VARCHAR(64) NOT NULL," +
            "purchased_at BIGINT NOT NULL," +
            "PRIMARY KEY (uuid, tag_id)" +
            ")");
    }

    public boolean has(Player player, String tagId) {
        Integer count = db.executeQuery(
            "SELECT COUNT(*) AS c FROM player_tags WHERE uuid = ? AND tag_id = ?",
            rs -> { try { return rs.next() ? rs.getInt("c") : 0; } catch (Exception e) { return 0; } },
            player.getUniqueId().toString(), tagId
        );
        return count != null && count > 0;
    }

    public boolean buy(Player player, String tagId, double price) {
        SF sf = SF.sf();
        if (sf.balance(player) < price) return false;
        if (!sf.takeMoney(player, price)) return false;
        int rows = db.executeUpdate(
            "INSERT OR IGNORE INTO player_tags (uuid, tag_id, purchased_at) VALUES (?, ?, ?)",
            player.getUniqueId().toString(), tagId, System.currentTimeMillis()
        );
        return rows > 0;
    }
}
```

---

## ⚙️ ZeroEngine 原版操控引擎

ZeroEngine 4.0 核心功能，提供 5 大原版操控模块，全部作为 API 供外部插件调用。引擎本身不参与具体业务逻辑，仅提供操控能力。

### 获取方式

```java
SF sf = SF.sf();

MonsterAttribute monster = sf.monster();
DamageSystem damage = sf.damage();
BlockControl block = sf.block();
SpawnControl spawn = sf.spawn();
ResourcePackManager resourcePack = sf.resourcePack();
```

所有引擎模块均采用懒加载，首次调用时自动初始化并注册事件监听器。

---

### 怪物属性操控

`MonsterAttribute` — 对原版生物的属性进行精确控制。

| 方法 | 说明 |
|------|------|
| `setBaseDamage(entity, damage)` | 设置基础攻击伤害 |
| `setBaseHealth(entity, health)` | 设置基础最大生命值（同时修正当前血量） |
| `setBaseSpeed(entity, speed)` | 设置基础移动速度 |
| `setBaseKnockbackResistance(entity, resistance)` | 设置击退抗性 |
| `setBaseArmor(entity, armor)` | 设置护甲值 |
| `setBaseArmorToughness(entity, toughness)` | 设置护甲韧性 |
| `scale(entity, healthMul, damageMul, speedMul)` | 按倍率缩放属性 |
| `reset(entity)` | 重置所有属性到默认值 |
| `get(attribute, entity)` | 获取属性当前值 |
| `set(attribute, entity, value)` | 设置任意属性基础值 |
| `addModifier(attribute, entity, name, amount, operation)` | 添加属性修饰器 |
| `removeModifier(attribute, entity, name)` | 移除属性修饰器 |
| `applyPersistent(entityId, modifiers)` | 持久化属性修饰（跨tick保持） |
| `getPersistent(entityId)` | 获取持久化属性 |
| `clearPersistent(entityId)` | 清除持久化属性 |

**使用示例：**

```java
SF sf = SF.sf();
MonsterAttribute ma = sf.monster();

// 设置僵尸属性
Zombie zombie = world.spawn(loc, Zombie.class);
ma.setBaseHealth(zombie, 100);
ma.setBaseDamage(zombie, 20);
ma.setBaseSpeed(zombie, 0.35);

// 按倍率缩放（困难模式）
ma.scale(zombie, 2.0, 1.5, 1.2);

// 添加自定义修饰器
ma.addModifier(
    Attribute.GENERIC_ATTACK_SPEED,
    zombie,
    "fast_attack",
    2.0,
    AttributeModifier.Operation.ADD_NUMBER
);

// 重置
ma.reset(zombie);
```

---

### 伤害系统操控

`DamageSystem` — 自定义伤害计算公式、PvP 控制、护甲穿透。

| 方法 | 说明 |
|------|------|
| `registerDamageModifier(name, priority, fn)` | 注册伤害修改器（按优先级执行） |
| `unregisterDamageModifier(name)` | 移除伤害修改器 |
| `calculateDamage(attacker, victim, rawDamage, cause)` | 手动计算伤害 |
| `setPvpEnabled(enabled)` | 全局 PvP 开关 |
| `isPvpEnabled()` | 查询全局 PvP 状态 |
| `setPvpEnabled(worldId, enabled)` | 按世界设置 PvP |
| `isPvpEnabled(worldId)` | 查询世界 PvP 状态 |
| `setDamageMultiplier(cause, multiplier)` | 设置伤害类型倍率 |
| `getDamageMultiplier(cause)` | 获取伤害类型倍率 |
| `resetDamageMultiplier(cause)` | 重置伤害类型倍率 |
| `setArmorPenetration(percent)` | 设置护甲穿透百分比（0~1） |
| `getArmorPenetration()` | 获取护甲穿透 |
| `setCustomDamage(attacker, victim, damage)` | 对特定目标设置固定伤害 |
| `clearCustomDamage(attacker)` | 清除自定义伤害 |

**DamageContext 接口：**

```java
public interface DamageContext {
    LivingEntity attacker();    // 攻击者
    LivingEntity victim();      // 受害者
    double rawDamage();         // 原始伤害
    DamageCause cause();        // 伤害原因
    boolean isCritical();       // 是否暴击
    void setDamage(double d);   // 修改最终伤害
    void setCancelled(boolean c); // 取消伤害
    boolean isCancelled();      // 是否已取消
}
```

**使用示例：**

```java
SF sf = SF.sf();
DamageSystem ds = sf.damage();

// 关闭 PvP
ds.setPvpEnabled(false);

// 按世界关闭 PvP
ds.setPvpEnabled(world.getUID(), false);

// 摔伤减半
ds.setDamageMultiplier(DamageCause.FALL, 0.5);

// 30% 护甲穿透
ds.setArmorPenetration(0.3);

// 注册自定义伤害修改器（高优先级）
ds.registerDamageModifier("boss_resist", 100, (ctx, dmg) -> {
    if (ctx.victim() instanceof Boss) {
        return dmg * 0.7; // Boss 受到 30% 减伤
    }
    return dmg;
});

// 对特定玩家固定伤害
ds.setCustomDamage(attacker, victim, 50.0);
```

---

### 方块/挖掘操控

`BlockControl` — 修改原版方块的挖掘速度、爆炸抗性、掉落物等。

| 方法 | 说明 |
|------|------|
| `setBreakSpeed(material, speed)` | 设置挖掘速度倍率 |
| `getBreakSpeed(material)` | 获取挖掘速度 |
| `resetBreakSpeed(material)` | 重置挖掘速度 |
| `setBlastResistance(material, resistance)` | 设置爆炸抗性 |
| `getBlastResistance(material)` | 获取爆炸抗性 |
| `resetBlastResistance(material)` | 重置爆炸抗性 |
| `setDrop(material, drop, chance)` | 设置自定义掉落物和概率 |
| `getDrop(material)` | 获取自定义掉落物 |
| `resetDrop(material)` | 重置掉落物 |
| `setExpDrop(material, minExp, maxExp)` | 设置经验掉落范围 |
| `resetExpDrop(material)` | 重置经验掉落 |
| `registerBreakHandler(material, handler)` | 注册方块破坏处理器 |
| `unregisterBreakHandler(material)` | 移除方块破坏处理器 |
| `setRequireTool(material, requireTool)` | 设置是否需要工具才能挖掘 |
| `isRequireTool(material)` | 查询是否需要工具 |
| `setReplaceOnBreak(material, replaceWith)` | 破坏后替换为其他方块 |
| `cancelBlockUpdate(location, radius)` | 取消区域内的方块更新 |
| `getModifiedBreakSpeeds()` | 获取所有已修改的挖掘速度 |
| `getModifiedBlastResistances()` | 获取所有已修改的爆炸抗性 |

**使用示例：**

```java
SF sf = SF.sf();
BlockControl bc = sf.block();

// 钻石矿挖掘速度减半
bc.setBreakSpeed(Material.DIAMOND_ORE, 0.5f);

// 圆石掉落钻石（10% 概率）
bc.setDrop(Material.STONE, new ItemStack(Material.DIAMOND), 0.1f);

// 煤矿掉落 1~3 经验
bc.setExpDrop(Material.COAL_ORE, 1, 3);

// 黑曜石必须用镐子挖
bc.setRequireTool(Material.OBSIDIAN, true);

// 破坏草方块后替换为泥土
bc.setReplaceOnBreak(Material.GRASS_BLOCK, Material.DIRT);

// 注册自定义破坏处理器
bc.registerBreakHandler(Material.SPAWNER, (player, block) -> {
    if (!player.hasPermission("server.mine.spawner")) {
        player.sendMessage("§c你没有权限破坏刷怪笼！");
        return false; // 取消破坏
    }
    return true; // 允许破坏
});
```

---

### 实体生成操控

`SpawnControl` — 控制原版怪物生成规则、概率、上限、黑名单。

| 方法 | 说明 |
|------|------|
| `createRule(name, type, chance, maxPerChunk, worlds)` | 创建生成规则 |
| `registerRule(rule)` | 注册生成规则 |
| `unregisterRule(name)` | 移除生成规则 |
| `getRule(name)` | 获取生成规则 |
| `allRules()` | 获取全部生成规则 |
| `blacklistEntity(type, worlds)` | 将实体加入世界黑名单 |
| `unblacklistEntity(type, worlds)` | 从黑名单移除 |
| `isBlacklisted(type, worldName)` | 查询是否在黑名单中 |
| `setSpawnCap(worldId, type, cap)` | 设置世界内实体生成上限 |
| `getSpawnCap(worldId, type)` | 获取生成上限 |
| `registerSpawnFilter(filter)` | 注册实体类型过滤器 |
| `unregisterSpawnFilter(filter)` | 移除实体类型过滤器 |
| `registerLocationFilter(filter)` | 注册生成位置过滤器 |
| `unregisterLocationFilter(filter)` | 移除生成位置过滤器 |
| `forceSpawn(type, location, count)` | 强制在指定位置生成实体 |
| `clearEntities(worldId, type)` | 清除世界内指定类型实体 |
| `getEntityCounts(worldId)` | 获取世界内各实体数量统计 |

**SpawnRule 接口：**

```java
public interface SpawnRule {
    String name();              // 规则名称
    EntityType type();          // 实体类型
    double chance();            // 生成概率 (0~1)
    int maxPerChunk();          // 每区块最大数量
    List<String> worlds();      // 生效世界列表
    boolean enabled();          // 是否启用
    void setChance(double c);   // 修改概率
    void setMaxPerChunk(int max); // 修改上限
    void setEnabled(boolean e); // 启用/禁用
}
```

**使用示例：**

```java
SF sf = SF.sf();
SpawnControl sc = sf.spawn();

// 创建僵尸生成规则：50% 概率，每区块最多 10 只，仅在 world 生效
SpawnRule rule = sc.createRule("zombie_rule", EntityType.ZOMBIE, 0.5, 10, List.of("world"));
sc.registerRule(rule);

// 在主世界禁止苦力怕生成
sc.blacklistEntity(EntityType.CREEPER, List.of("world"));

// 设置世界内最多 20 只骷髅
sc.setSpawnCap(world.getUID(), EntityType.SKELETON, 20);

// 注册类型过滤器：禁止所有 boss 类型生成
sc.registerSpawnFilter(type -> {
    return type != EntityType.WITHER && type != EntityType.ENDER_DRAGON;
});

// 注册位置过滤器：出生点 100 格内不生成怪物
sc.registerLocationFilter((type, loc) -> {
    return loc.distance(loc.getWorld().getSpawnLocation()) > 100;
});

// 强制生成 5 只僵尸
sc.forceSpawn(EntityType.ZOMBIE, location, 5);

// 查看世界实体统计
Map<EntityType, Integer> counts = sc.getEntityCounts(world.getUID());
counts.forEach((type, count) -> System.out.println(type + ": " + count));
```

---

### 资源包管理

`ResourcePackManager` — 管理服务器资源包、自定义模型数据、音乐播放。

| 方法 | 说明 |
|------|------|
| `create(name, url, hash, forced, promptMessage)` | 创建资源包定义 |
| `register(pack)` | 注册资源包 |
| `unregister(name)` | 移除资源包 |
| `get(name)` | 获取资源包 |
| `all()` | 获取全部资源包 |
| `send(player, name)` | 向玩家发送指定资源包 |
| `send(player, pack)` | 向玩家发送资源包对象 |
| `sendAll(player)` | 向玩家发送所有资源包 |
| `sendAll(player, onComplete)` | 发送所有资源包，完成后回调 |
| `setCustomModelData(itemId, modelData, texturePath)` | 设置物品自定义模型数据 |
| `getCustomModelData(itemId)` | 获取自定义模型数据 |
| `registerMusic(id, soundName, durationTicks)` | 注册自定义音乐 |
| `playMusic(player, id)` | 为玩家播放指定音乐 |
| `stopMusic(player)` | 停止玩家所有音乐 |
| `playMusicAll(id)` | 全服播放音乐 |
| `stopMusicAll()` | 全服停止音乐 |
| `setDefaultPack(pack)` | 设置默认资源包 |
| `getDefaultPack()` | 获取默认资源包 |

**ResourcePack 接口：**

```java
public interface ResourcePack {
    String name();           // 资源包名称
    String url();            // 下载地址
    byte[] hash();           // SHA1 哈希
    boolean forced();        // 是否强制
    String promptMessage();  // 提示消息
}
```

**使用示例：**

```java
SF sf = SF.sf();
ResourcePackManager rpm = sf.resourcePack();

// 注册资源包
ResourcePack pack = rpm.create(
    "sf_textures",
    "https://example.com/pack.zip",
    hashBytes,
    true,
    "§a请安装资源包以获得最佳体验"
);
rpm.register(pack);
rpm.setDefaultPack(pack);

// 玩家进服自动发送
@EventHandler
public void onJoin(PlayerJoinEvent e) {
    rpm.send(e.getPlayer(), "sf_textures");
}

// 注册自定义音乐
rpm.registerMusic("boss_fight", "music.boss_fight", 1200);

// 播放音乐
rpm.playMusic(e.getPlayer(), "boss_fight");

// 全服停止音乐
rpm.stopMusicAll();

// 设置自定义模型数据
rpm.setCustomModelData(Material.DIAMOND_SWORD.ordinal(), 100001, "items/sf_sword");
```

---

## 🔐 权限列表

### 默认权限

以下权限默认所有玩家都拥有（无需手动赋予）：

| 权限 | 说明 |
|------|------|
| `servermanagement.use` | 使用 `/servermanagement` 命令 |

### 管理员权限

| 权限 | 说明 | 默认 |
|------|------|------|
| `sf.admin.world` | 世界管理 `/sfworld` | OP |
| `sf.admin.chat` | 聊天管理 `/sfchat` | OP |
| `sf.admin.permission` | 权限管理 `/sfperm` | OP |
| `sf.admin.enchant` | 附魔管理 `/sfenchant` | OP |
| `sf.admin.item` | 物品管理 `/sfitem` | OP |
| `sf.admin.perf` | 性能管理 `/sfperf` | OP |

### 系统权限

| 权限 | 说明 | 默认 |
|------|------|------|
| `servermanagement.reload` | 重载配置文件 | OP |

### 推荐权限分配（LuckPerms）

**默认组**（所有玩家）：
```bash
lp group default permission set servermanagement.use true
```

**管理员组**：
```bash
# 方法 1: 逐个赋予
lp group admin permission set sf.admin.world true
lp group admin permission set sf.admin.chat true
lp group admin permission set sf.admin.permission true
lp group admin permission set sf.admin.enchant true
lp group admin permission set sf.admin.item true

# 方法 2: 使用通配符（如果你的权限插件支持）
lp group admin permission set sf.admin.* true
lp group admin permission set sf.* true
```

### 通配符权限

SF 支持以下通配符（需权限插件支持，如 LuckPerms）：

| 通配符 | 包含 |
|--------|------|
| `sf.admin.*` | 所有 `sf.admin.xxx` 权限 |
| `sf.*` | 所有 `sf.xxx` 权限 |

---

## ⚙️ 配置文件

### 完整配置示例

```yaml
# ====== 数据库配置 ======
database:
  # 是否启用 MySQL（false 则使用 SQLite）
  mysql:
    enabled: false
    host: localhost
    port: 3306
    database: minecraft
    user: root
    password: ""
    prefix: "sf_"
  # SQLite 配置（mysql.enabled=false 时使用）
  sqlite:
    file: data.db

# ====== 传送系统配置 ======
teleport:
  # 冷却时间（秒），0 表示无冷却
  cooldown:
    spawn: 5
    home: 5
    warp: 5
    back: 10
    tpa: 10
    tpahere: 10
    tp: 0        # 管理员传送无冷却
    tphere: 0
  # 延迟传送（秒），0 表示立即传送
  delay:
    spawn: 3
    home: 3
    warp: 3
    back: 3
    tpa: 3
    tpahere: 3
  # TPA 请求超时（秒）
  tpa:
    timeout: 60
```

### 配置项详解

#### `database.mysql.enabled`

是否启用 MySQL。设为 `false` 则使用 SQLite。

#### `database.mysql.host` / `port` / `database` / `user` / `password`

MySQL 连接信息。`database` 是数据库名（需要预先创建）。

#### `database.mysql.prefix`

表名前缀。多服务器共用同一个数据库时有用：

```yaml
database:
  mysql:
    prefix: "sf_survival_"   # 表名会变成 sf_survival_homes 等
```

#### `database.sqlite.file`

SQLite 数据库文件名。文件位于 `plugins/ZeroCkate_SFServerPlugin/` 目录下。

#### `teleport.cooldown.*`

传送命令的冷却时间（秒）。同一玩家在冷却时间内无法再次使用该命令。

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `spawn` | 5 | `/spawn` 冷却 |
| `home` | 5 | `/home` 冷却 |
| `warp` | 5 | `/warp` 冷却 |
| `back` | 10 | `/back` 冷却 |
| `tpa` | 10 | `/tpa` 冷却 |
| `tpahere` | 10 | `/tpahere` 冷却 |
| `tp` | 0 | `/tp` 冷却（管理员） |
| `tphere` | 0 | `/tphere` 冷却（管理员） |

> 💡 拥有 `sf.teleport.bypass` 权限的玩家可以跳过冷却。

#### `teleport.delay.*`

传送延迟（秒）。玩家执行命令后不会立即传送，而是等待指定秒数。期间移动会取消传送。

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `spawn` | 3 | `/spawn` 延迟 |
| `home` | 3 | `/home` 延迟 |
| `warp` | 3 | `/warp` 延迟 |
| `back` | 3 | `/back` 延迟 |
| `tpa` | 3 | `/tpa` 接受后延迟 |
| `tpahere` | 3 | `/tpahere` 接受后延迟 |

> 💡 设为 `0` 表示立即传送（无延迟）。

#### `teleport.tpa.timeout`

TPA 请求超时时间（秒）。请求发出后，对方在指定时间内未响应则自动失效。

### 修改配置后

修改 `config.yml` 后，执行以下命令热重载（无需重启服务器）：

```
/servermanagement reload
# 或
/sm reload
```

### 常见配置场景

**场景 1：关闭所有冷却（休闲服）**

```yaml
teleport:
  cooldown:
    spawn: 0
    home: 0
    warp: 0
    back: 0
    tpa: 0
    tpahere: 0
```

**场景 2：长冷却防滥用（生存服）**

```yaml
teleport:
  cooldown:
    spawn: 30
    home: 30
    warp: 30
    back: 60
    tpa: 60
    tpahere: 60
  delay:
    spawn: 5
    home: 5
    warp: 5
    back: 5
    tpa: 5
    tpahere: 5
```

**场景 3：多服务器共享数据库**

```yaml
# 服务器 A（生存服）
database:
  mysql:
    enabled: true
    host: db.example.com
    port: 3306
    database: mc_network
    user: mc_user
    password: "xxx"
    prefix: "sf_survival_"

# 服务器 B（小游戏服）—— 仅 prefix 不同
database:
  mysql:
    enabled: true
    host: db.example.com
    port: 3306
    database: mc_network
    user: mc_user
    password: "xxx"
    prefix: "sf_minigame_"
```

**场景 4：纯立即传送（无延迟）**

```yaml
teleport:
  delay:
    spawn: 0
    home: 0
    warp: 0
    back: 0
    tpa: 0
    tpahere: 0
```

---

## 💻 开发者 API

SF 插件通过 Bukkit `ServicesManager` 对外暴露 `SFApi` 接口，其他插件可以通过它调用 SF 的所有功能。

### Maven 依赖

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.zmb96</groupId>
        <artifactId>ZeroCkate_ServerManagementPlugin</artifactId>
        <version>main-SNAPSHOT</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

### Gradle (Kotlin DSL)

```kotlin
repositories {
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("com.github.zmb96:ZeroCkate_ServerManagementPlugin:main-SNAPSHOT")
}
```

> ⚠️ 使用 `provided` / `compileOnly` 作用域，不要把 SF 打包进你的 jar。

### 在 plugin.yml 中声明依赖

```yaml
name: MyPlugin
version: 1.0.0
main: com.example.myplugin.MyPlugin
api-version: '1.21.5'

# 声明依赖（让 SF 先加载）
depend: [ZeroCkate_SFServerPlugin]
# 或者软依赖（SF 不存在也能加载）
softdepend: [ZeroCkate_SFServerPlugin]
```

- 用 `depend`：你的插件**强依赖** SF，SF 必须存在才能启用
- 用 `softdepend`：你的插件**软依赖** SF，SF 不存在时降级运行

### API 接口文档

#### 接口概览

```java
package server.sf.model.api.v2;

public interface SFApi {
    // 子模块访问器
    SFLogger logger();
    SFEconomy economy();
    SFEvents events();
    SFScheduler scheduler();
    SFPlayerOps players();
    SFServerOps server();
    TickManager tick();
    ChatManager chat();
    WorldManager world();
    PermissionManager permission();
    Database database();

    // 日志快捷方法
    void info(String msg);
    void info(String fmt, Object... args);
    void warn(String msg);
    void warn(String fmt, Object... args);
    void error(String msg);
    void error(String msg, Throwable t);
    void error(String fmt, Object... args);

    // 聊天/广播
    void broadcast(String msg);
    void broadcast(String perm, String msg);
    void msg(CommandSender sender, String msg);

    // 玩家查找
    Player player(String name);
    Player player(UUID id);

    // 经济系统（便捷方法）
    boolean giveMoney(OfflinePlayer p, double amount);
    boolean takeMoney(OfflinePlayer p, double amount);
    boolean setMoney(OfflinePlayer p, double amount);
    double balance(OfflinePlayer p);
    boolean transferMoney(OfflinePlayer from, OfflinePlayer to, double amount);
    String formatMoney(double amount);

    // 传送
    boolean teleport(Player p, Location loc);

    // 调度
    void run(Runnable r);                  // 主线程同步
    void runAsync(Runnable r);             // 异步
    void runLater(Runnable r, long ticks); // 延迟
    void runTimer(Runnable r, long delay, long period);  // 定时

    // 控制台
    void console(String cmd);

    // 获取 API 实例
    static SFApi get();
    static boolean isAvailable();
}
```

#### 获取 API 实例

**方法 1：静态方法（推荐）**

```java
if (SFApi.isAvailable()) {
    SFApi api = SFApi.get();
    api.info("成功接入 SF API！");
}
```

**方法 2：通过 ServicesManager**

```java
RegisteredServiceProvider<SFApi> rsp = getServer().getServicesManager().getRegistration(SFApi.class);
if (rsp != null) {
    SFApi api = rsp.getProvider();
}
```

#### 子模块详解

**SFLogger - 日志**

```java
SFLogger logger = api.logger();

logger.info("普通信息");
logger.info("格式化信息: %s 已上线", playerName);  // 支持 String.format
logger.warn("警告信息");
logger.error("错误信息");
logger.error("错误带异常", exception);
```

**SFEconomy - 经济系统**

```java
SFEconomy eco = api.economy();

// 状态查询
eco.ready();              // 经济系统是否就绪
eco.hasEssentials();      // 是否使用 EssentialsX 后端
eco.hasVault();           // 是否使用 Vault 后端

// 账户操作（OfflinePlayer 也支持）
eco.hasAccount(player);
eco.balance(player);
eco.give(player, 100);
eco.take(player, 50);
eco.set(player, 1000);
eco.transfer(playerA, playerB, 100);
eco.format(100.5);        // 格式化为字符串

// 直接访问后端
eco.essentials();         // EssentialsBackend 实例
eco.vault();              // VaultBackend 实例
eco.ops();                // EconomyOps 高级操作
```

**SFEvents - 事件系统**

```java
SFEvents events = api.events();

// 通用方法（任意 Bukkit 事件）
events.on(PlayerJoinEvent.class, e -> {
    api.broadcast("欢迎 " + e.getPlayer().getName());
});

// 分类快捷方法
events.player().join(e -> { ... });
events.player().quit(e -> { ... });
events.player().chat(e -> { ... });
events.player().death(e -> { ... });
events.player().move(e -> { ... });

events.block().break_(e -> { ... });
events.block().place(e -> { ... });

events.entity().damage(e -> { ... });
events.entity().death(e -> { ... });

events.inventory().click(e -> { ... });

events.server().command(e -> { ... });
events.world().load(e -> { ... });

// 支持指定优先级
events.on(PlayerChatEvent.class, EventPriority.HIGH, true, e -> {
    // HIGH 优先级，忽略已取消的事件
});

// 卸载所有监听器
events.unregisterAll();
```

**SFScheduler - 调度**

```java
SFScheduler scheduler = api.scheduler();

// 主线程同步执行
scheduler.run(() -> {
    player.sendMessage("在主线程执行");
});

// 异步执行（不要在异步中调用 Bukkit API！）
scheduler.runAsync(() -> {
    // 数据库查询、HTTP 请求等
});

// 延迟执行（20 ticks = 1 秒）
scheduler.runLater(() -> {
    player.sendMessage("1 秒后执行");
}, 20L);

// 定时执行
scheduler.runTimer(() -> {
    api.broadcast("每 5 秒广播一次");
}, 0L, 100L);  // delay=0, period=100 ticks
```

**SFPlayerOps - 玩家查找**

```java
SFPlayerOps players = api.players();

Player p1 = players.byName("Notch");
Player p2 = players.byId(uuid);
```

**SFServerOps - 服务器操作**

```java
SFServerOps server = api.server();

server.server();                  // 获取 Bukkit Server
server.broadcast("全服广播");
server.broadcast("permission.node", "只有特定权限的玩家能看到");
server.msg(sender, "发送消息给 sender");
```

**TickManager - SF Tick 系统**

```java
TickManager tick = api.tick();

tick.runLater(sfTick -> { ... }, 100);        // 1秒后执行
tick.runTimer(sfTick -> { ... }, 100);         // 每秒执行
tick.runTimer(sfTick -> { ... }, 200, 100);    // 延迟2秒后每秒执行
tick.cancel(taskId);                           // 取消任务
tick.now();                                    // 当前 tick
tick.toSeconds(500);                           // 5
tick.fromSeconds(30);                          // 3000
tick.runSync(() -> { ... });                   // 切回主线程
tick.runSyncLater(() -> { ... }, 100);         // 1秒后切回主线程
```

**ChatManager - 聊天系统**

`ChatManager` 提供多频道聊天、禁言、屏蔽词过滤、消息格式化等功能。通过 `SF.sf().chat()` 获取实例。

#### 频道管理 API

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `registerChannel(ChatChannel)` | 频道对象 | `void` | 注册新频道 |
| `unregisterChannel(String)` | 频道名 | `void` | 删除频道 |
| `getChannel(String)` | 频道名 | `ChatChannel` | 按名称获取频道 |
| `getChannel(Player)` | 玩家 | `ChatChannel` | 获取玩家当前所在频道 |
| `setChannel(Player, String)` | 玩家, 频道名 | `void` | 切换玩家频道 |
| `allChannels()` | — | `Collection<ChatChannel>` | 获取所有已注册频道 |

#### ChatChannel 构造

```java
new ChatManager.ChatChannel(name, prefix, range, cooldownTicks)
```

| 参数 | 类型 | 说明 |
|------|------|------|
| `name` | `String` | 频道唯一标识（不区分大小写） |
| `prefix` | `String` | 频道前缀，支持颜色代码，如 `"§7[§6交易§7] "` |
| `range` | `Double` | `null` = 全局频道；`100.0` = 同世界 100 格内可见 |
| `cooldownTicks` | `long` | 发言冷却（SF Tick，100 = 1 秒，0 = 无冷却） |

#### 禁言 API

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `mute(Player, long, String)` | 玩家, 秒数, 原因 | `void` | 禁言，秒数 ≤ 0 为永久 |
| `unmute(Player)` | 玩家 | `void` | 解除禁言 |
| `isMuted(Player)` | 玩家 | `boolean` | 是否被禁言（过期自动清除） |
| `muteReason(Player)` | 玩家 | `String` | 获取禁言原因（未禁言返回 `null`） |
| `muteRemaining(Player)` | 玩家 | `long` | 剩余禁言秒数（永久禁言返回 `Long.MAX_VALUE`） |

> 禁言基于 SF Tick 系统，服务器重启后禁言状态会丢失（内存存储）。

#### 屏蔽词 API

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `addBlockedWord(String)` | 词语 | `void` | 添加屏蔽词（不区分大小写） |
| `removeBlockedWord(String)` | 词语 | `void` | 移除屏蔽词 |
| `blockedWords()` | — | `Set<String>` | 获取所有屏蔽词 |
| `filterMessage(String)` | 原始消息 | `String` | 过滤消息中的屏蔽词（替换为 `*`） |

#### 消息格式化 API

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `format(Player, String)` | 玩家, 消息 | `String` | 格式化消息，自动读取权限系统的前缀后缀 |

默认格式模板：`<{prefix}{name}{suffix}> {message}`

可通过反射修改 `defaultFormat.template` 来自定义：

```java
ChatManager chat = SF.sf().chat();
chat.getDefaultFormat().template = "{prefix}{name}§7: {message}";
```

#### 收件人获取 API

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `getRecipients(Player, ChatChannel)` | 发送者, 频道 | `Collection<Player>` | 根据频道范围获取可见玩家列表 |

全局频道返回所有在线玩家；范围频道返回同世界指定距离内的玩家。

#### 内置频道

| 频道名 | 前缀 | 范围 | 说明 |
|--------|------|------|------|
| `global` | `§7[§a全§7] ` | 全局 | 默认频道，所有人可见 |
| `local` | `§7[§e附近§7] ` | 100 格 | 同世界 100 格内可见 |
| `staff` | `§7[§c管理§7] ` | 全局 | 管理员频道（需自行控制权限） |

#### 完整使用示例

```java
ChatManager chat = SF.sf().chat();

// ===== 频道管理 =====
// 创建交易频道（全局可见，3秒冷却）
chat.registerChannel(new ChatManager.ChatChannel(
    "trade", "§7[§6交易§7] ", null, 300));

// 创建同城频道（500格内可见）
chat.registerChannel(new ChatManager.ChatChannel(
    "city", "§7[§a同城§7] ", 500.0, 0));

// 切换玩家频道
chat.setChannel(player, "trade");

// 获取玩家当前频道
ChatManager.ChatChannel ch = chat.getChannel(player);
SF.sf().info("玩家所在频道: " + ch.name + ", 前缀: " + ch.prefix);

// 列出所有频道
for (ChatManager.ChatChannel c : chat.allChannels()) {
    SF.sf().info("频道: " + c.name + " 范围: " + c.range);
}

// 删除频道
chat.unregisterChannel("city");

// ===== 禁言 =====
// 临时禁言 60 秒
chat.mute(player, 60, "刷屏广告");

// 永久禁言
chat.mute(player, 0, "严重违规");

// 检查禁言状态
if (chat.isMuted(player)) {
    SF.sf().info("已禁言，原因: " + chat.muteReason(player)
        + "，剩余: " + chat.muteRemaining(player) + "秒");
}

// 解除禁言
chat.unmute(player);

// ===== 屏蔽词 =====
// 添加屏蔽词
chat.addBlockedWord("垃圾");
chat.addBlockedWord("外挂");

// 过滤消息
String filtered = chat.filterMessage("你真垃圾，用外挂");  // 你真**，用**
SF.sf().info(filtered);

// 移除屏蔽词
chat.removeBlockedWord("垃圾");

// 获取所有屏蔽词
Set<String> words = chat.blockedWords();
SF.sf().info("当前屏蔽词: " + words);

// ===== 消息格式化 =====
// 自动读取权限系统前缀后缀
String formatted = chat.format(player, "大家好");
// 输出: <[VIP]玩家名> 大家好

// ===== 获取收件人 =====
ChatManager.ChatChannel channel = chat.getChannel(player);
Collection<Player> recipients = chat.getRecipients(player, channel);
for (Player r : recipients) {
    r.sendMessage("收到消息");
}
```

#### 第三方插件接入示例

```java
public class TradeChannelPlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        SF sf = getServer().getServicesManager().load(SF.class);
        if (sf == null) return;

        ChatManager chat = sf.chat();

        // 注册交易频道
        chat.registerChannel(new ChatManager.ChatChannel(
            "trade", "§7[§6交易§7] ", null, 200));

        // 注册切换频道命令
        getCommand("trade").setExecutor((sender, cmd, label, args) -> {
            if (!(sender instanceof Player p)) return true;
            chat.setChannel(p, "trade");
            p.sendMessage("§a已切换到交易频道");
            return true;
        });
    }
}
```

**WorldManager - 世界管理**

```java
WorldManager world = api.world();

world.setDay(world);                    // 白天
world.setNight(world);                  // 夜晚
world.setTime(world, 6000);             // 自定义时间
world.lockTime(world, 6000);            // 锁定时间
world.unlockTime(world);                // 解锁

world.setStorm(world, false);           // 晴天
world.setThunder(world, true);          // 雷暴
world.setDifficulty(world, Difficulty.HARD);
world.setPvp(world, false);
world.setBorder(world, 5000);           // 边界 5000 格
world.setBorderCenter(world, 0, 0);
world.setMobSpawning(world, false);
world.setFireSpread(world, false);

// 预设
world.savePreset("survival", world);
world.applyPreset("survival", world);
```

**PermissionManager - 权限系统**

```java
PermissionManager perm = api.permission();

// 组管理
perm.registerGroup(new PermissionManager.Group("vip", "§a[VIP] ", "", 10));
perm.getGroup("vip");
perm.allGroups();

// 玩家权限
perm.setGroup(player, "vip");
perm.getGroup(player);                  // 获取玩家所在组
perm.getPrefix(player);                 // 获取前缀
perm.getSuffix(player);                 // 获取后缀
perm.addPermission(player, "my.perm");
perm.removePermission(player, "my.perm");
perm.has(player, "my.perm");            // 检查权限
perm.getEffectivePermissions(player);   // 获取所有有效权限
perm.applyPermissions(player);          // 重新应用权限
```

**EnchantManager - 附魔系统**

```java
EnchantManager enchant = ((SF) api).enchant();

enchant.register(new MyEnchant());
enchant.get("my_enchant");
enchant.all();
enchant.apply(item, "my_enchant", 2);   // 给物品附魔
enchant.remove(item, "my_enchant");
enchant.getEnchants(item);              // 获取物品上的所有附魔

// 附魔书操作
enchant.createBook("my_enchant");              // 创建满级附魔书
enchant.createBook("my_enchant", 2);           // 创建指定等级附魔书
enchant.createBook(enchantObj, 3);             // 传 SEnchantment 对象
enchant.giveBook(player, "my_enchant");        // 给予满级附魔书
enchant.giveBook(player, "my_enchant", 2);     // 给予指定等级附魔书
```

**ItemManager - 物品系统**

```java
ItemManager item = ((SF) api).item();

item.register(new MyItem());
item.get("my_item");
item.all();

// 给予/创建
item.give(player, "my_item");
item.give(player, "my_item", 5);
item.create("my_item");
item.create("my_item", 3);

// 检查/消耗
item.has(player, "my_item");
item.count(player, "my_item");
item.consume(player, "my_item");
item.consume(player, "my_item", 3);
item.find(player, "my_item");           // 查找玩家背包中的物品数量
```

#### 异常处理

所有 API 方法都会捕获内部异常并通过 logger 输出，不会抛出异常中断调用方代码。

但 `SFApi.get()` 在 API 未注册时会抛出 `IllegalStateException`，建议先检查：

```java
if (!SFApi.isAvailable()) {
    getLogger().warning("SF API 不可用，相关功能已禁用");
    return;
}
SFApi api = SFApi.get();
```

#### 线程安全

| 方法 | 线程安全 | 说明 |
|------|----------|------|
| `logger.*` | ✅ | 完全线程安全 |
| `economy.balance/give/take/set/transfer` | ⚠️ | 读取可异步，写入建议主线程 |
| `economy.format` | ✅ | 纯计算 |
| `events.on/register` | ⚠️ | 必须主线程调用 |
| `scheduler.runAsync` | ✅ | 任何线程可调用 |
| `scheduler.run/runLater/runTimer` | ⚠️ | 必须主线程调用 |
| `tick.runLater/runTimer` | ✅ | 独立线程，线程安全 |
| `tick.runSync/runSyncLater` | ⚠️ | 从 tick 线程切回主线程 |
| `teleport` | ⚠️ | 必须主线程调用 |
| `broadcast/msg` | ⚠️ | 必须主线程调用 |
| `chat.mute/unmute/isMuted` | ✅ | 线程安全 |
| `permission.setGroup/addPermission` | ⚠️ | 必须主线程调用 |
| `world.*` | ⚠️ | 必须主线程调用 |

> 💡 不确定时，用 `api.run(() -> { ... })` 包裹代码确保主线程执行。

#### 版本兼容

API 遵循语义化版本：

- **Major**（如 v2 → v3）：破坏性变更
- **Minor**（如 v2.1 → v2.2）：新增功能，向后兼容
- **Patch**（如 v2.1.1 → v2.1.2）：Bug 修复

当前 API 版本：**v3**

包名 `server.sf.model.api.v2` 中的 `v2` 即为 Major 版本号。未来如有破坏性变更会新增 `v3` 包并保留 `v2`。

### API 接入示例

#### 最简单的用法

```java
package com.example.myplugin;

import org.bukkit.plugin.java.JavaPlugin;
import server.sf.model.api.v2.SFApi;

public class MyPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        if (!SFApi.isAvailable()) {
            getLogger().warning("SF API 不可用，相关功能已禁用");
            return;
        }

        SFApi api = SFApi.get();
        api.info("MyPlugin 已接入 SF API！");
    }
}
```

#### 缓存 API 实例

```java
public class MyPlugin extends JavaPlugin {

    private SFApi sf;

    @Override
    public void onEnable() {
        if (SFApi.isAvailable()) {
            sf = SFApi.get();
            sf.info("MyPlugin 已接入 SF API");
        } else {
            getLogger().warning("SF API 不可用");
        }
    }

    public SFApi sf() {
        return sf;
    }
}
```

#### 示例 1：登录奖励

玩家登录时给予 100 金币并广播欢迎消息。

```java
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import server.sf.model.api.v2.SFApi;

public class LoginBonusListener implements Listener {

    private final SFApi sf;

    public LoginBonusListener(SFApi sf) {
        this.sf = sf;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        sf.run(() -> {
            boolean ok = sf.giveMoney(e.getPlayer(), 100);
            if (ok) {
                sf.msg(e.getPlayer(), "§a登录奖励：100 金币");
            }
        });

        sf.broadcast("§e" + e.getPlayer().getName() + " §a加入了服务器！");
    }
}
```

注册监听器：

```java
@Override
public void onEnable() {
    SFApi sf = SFApi.get();
    getServer().getPluginManager().registerEvents(new LoginBonusListener(sf), this);
}
```

#### 示例 2：自定义商店

玩家右键牌子时扣钱给物品。

```java
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import server.sf.model.api.v2.SFApi;

public class SignShopListener implements Listener {

    private final SFApi sf;

    public SignShopListener(SFApi sf) {
        this.sf = sf;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (e.getClickedBlock() == null) return;

        var sign = (org.bukkit.block.Sign) e.getClickedBlock().getState();
        if (!sign.getLine(0).equals("[Shop]")) return;

        double price = Double.parseDouble(sign.getLine(1));
        var player = e.getPlayer();

        if (sf.balance(player) < price) {
            sf.msg(player, "§c金币不足，需要 " + sf.formatMoney(price));
            return;
        }

        if (sf.takeMoney(player, price)) {
            player.getInventory().addItem(new ItemStack(org.bukkit.Material.DIAMOND, 1));
            sf.msg(player, "§a购买成功！剩余余额：" + sf.formatMoney(sf.balance(player)));
        }
    }
}
```

#### 示例 3：定时全服公告

每 10 分钟广播一次。

```java
@Override
public void onEnable() {
    SFApi sf = SFApi.get();

    sf.runTimer(() -> {
        sf.broadcast("§6===== 服务器公告 =====");
        sf.broadcast("§a欢迎来到我们的服务器！");
        sf.broadcast("§a输入 /sh 查看帮助");
    }, 0L, 12000L);  // 12000 ticks = 10 分钟
}
```

#### 示例 4：玩家死亡惩罚

死亡时扣除 10% 金币。

```java
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import server.sf.model.api.v2.SFApi;

public class DeathPenaltyListener implements Listener {

    private final SFApi sf;

    public DeathPenaltyListener(SFApi sf) {
        this.sf = sf;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        var player = e.getEntity();
        double balance = sf.balance(player);
        if (balance <= 0) return;

        double penalty = balance * 0.10;
        sf.takeMoney(player, penalty);
        sf.msg(player, "§c死亡惩罚：扣除 " + sf.formatMoney(penalty) + " 金币");
    }
}
```

#### 示例 5：使用 SF 的事件系统

不用自己实现 Listener，直接用 SF 的链式 API。

```java
@Override
public void onEnable() {
    SFApi sf = SFApi.get();

    sf.events()
        .on(org.bukkit.event.player.PlayerJoinEvent.class, e -> {
            sf.info("玩家加入：" + e.getPlayer().getName());
        })
        .on(org.bukkit.event.player.PlayerQuitEvent.class, e -> {
            sf.info("玩家退出：" + e.getPlayer().getName());
        })
        .on(org.bukkit.event.entity.EntityDeathEvent.class, e -> {
            if (e.getEntity() instanceof org.bukkit.entity.Player p) {
                sf.broadcast("§c" + p.getName() + " 死了！");
            }
        });

    sf.events().player().join(e -> {
        sf.giveMoney(e.getPlayer(), 50);
    });
}
```

> 💡 注意：通过 `sf.events().on()` 注册的监听器由 SF 管理，**不需要** 再调用 `getServer().getPluginManager().registerEvents()`。

#### 示例 6：异步数据库查询 + 主线程更新

```java
public void showStats(Player player) {
    SFApi sf = SFApi.get();

    sf.runAsync(() -> {
        String stats = queryFromDatabase(player.getUniqueId());

        sf.run(() -> {
            sf.msg(player, "§6===== 你的统计数据 =====");
            sf.msg(player, stats);
        });
    });
}
```

#### 示例 7：跨插件传送

```java
public void teleportToLobby(Player player) {
    SFApi sf = SFApi.get();

    // 方法 1：直接传送（绕过冷却/延迟）
    var lobbyLoc = new Location(Bukkit.getWorld("world"), 0, 64, 0);
    sf.teleport(player, lobbyLoc);

    // 方法 2：通过 TeleportManager 享受完整特性（冷却、延迟、防移动）
    // 注意：这需要 SF 实现，且 teleportManager 已注册
    // sf.teleport().teleportDelayed(player, lobbyLoc, "custom", 60);  // 3 秒延迟
}
```

#### 示例 8：完整的工资系统

```java
package com.example.myplugin;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import server.sf.model.api.v2.SFApi;

public class SalaryPlugin extends JavaPlugin {

    private SFApi sf;

    @Override
    public void onEnable() {
        if (!SFApi.isAvailable()) {
            getLogger().severe("需要 SF 插件！");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        sf = SFApi.get();
        sf.info("工资系统已启动");

        sf.runTimer(this::paySalary, 0L, 28800L);  // 每 24 分钟
    }

    private void paySalary() {
        for (var player : Bukkit.getOnlinePlayers()) {
            double salary = 100;
            if (player.isOp()) {
                salary *= 1.5;
            }

            if (sf.giveMoney(player, salary)) {
                sf.msg(player, "§a=========================");
                sf.msg(player, "§a  日工资到账：" + sf.formatMoney(salary));
                sf.msg(player, "§a  当前余额：" + sf.formatMoney(sf.balance(player)));
                sf.msg(player, "§a=========================");
            }
        }
        sf.info("日工资发放完成");
    }
}
```

#### 调试技巧

**1. 检查 API 是否就绪**

```java
sf.info("Economy ready: " + sf.economy().ready());
sf.info("Essentials: " + sf.economy().hasEssentials());
sf.info("Vault: " + sf.economy().hasVault());
sf.info("Database: " + server.sf.model.api.v2.database.DatabaseManager.ready());
```

**2. 安全调用**

```java
public void safeGiveMoney(Player p, double amount) {
    sf.run(() -> {
        try {
            if (sf.economy().ready()) {
                sf.giveMoney(p, amount);
            } else {
                sf.warn("经济系统未就绪，无法给 " + p.getName() + " 发钱");
            }
        } catch (Throwable t) {
            sf.error("给钱失败", t);
        }
    });
}
```

**3. 监听 SF 的状态**

```java
sf.events().server().pluginEnable(e -> {
    if (e.getPlugin().getName().equals("Essentials")) {
        sf.info("Essentials 已加载，经济系统可能可用");
    }
});
```

#### 扩展 SF 的传送系统

通过 `api.teleport()` 访问 `TeleportManager`：

```java
SFApi api = SFApi.get();
TeleportManager tp = ((SF) api).teleport();

tp.teleportNow(player, location, "myplugin");
tp.teleportDelayed(player, location, "myplugin", 60);  // 3 秒延迟
tp.back(player);
```

> ⚠️ 注意：`api.teleport()` 是 `SF` 实现类的方法，不在 `SFApi` 接口中。需要强转或直接使用 `SF.sf()`。

---

### 📦 箱子 GUI 系统（ChestGUI）

ZeroEngine 3.2.1+ 提供纯 Java 的箱子 GUI 系统，无需 YAML 配置，链式调用构建交互式菜单。

#### 核心 API

```java
public interface GUIManager {
    ChestGUI create();
    ChestGUI create(String title, int rows);
    ChestGUI create(String title, int rows, boolean readonly);
    void closeAll();
}
```

通过 `SFApi.gui()` 获取：

```java
GUIManager guiMgr = SF.sf().gui();
ChestGUI myGui = guiMgr.create("我的菜单", 3);
```

#### ChestGUI 接口能力

| 方法 | 说明 |
|------|------|
| `title(String)` | 设置标题 |
| `rows(int)` / `size(int)` | 设置行数（1-6）或格数 |
| `item(slot, ItemStack)` | 在指定格子放物品 |
| `item(slot, ItemStack, onClick)` | 放物品并绑定点击回调 |
| `item(row, col, ...)` | 按行列坐标放置 |
| `fill(ItemStack)` | 填充所有空位 |
| `border(ItemStack)` | 填充边框 |
| `fillRange(start, end, ItemStack)` | 填充指定范围 |
| `clear(slot)` / `clear()` | 清除指定格 / 全部 |
| `onOpen(Consumer<Player>)` | 打开回调 |
| `onClose(Consumer<Player>)` | 关闭回调 |
| `onAnyClick(Consumer<ClickContext>)` | 任意点击回调 |
| `readonly(boolean)` | 是否禁止拿取物品（默认 true） |
| `pagination(items, perPage)` | 分页显示物品列表 |
| `page(int)` / `nextPage()` / `prevPage()` | 翻页 |
| `refresh()` / `refresh(Player)` | 刷新界面 |
| `open(Player)` / `close(Player)` / `closeAll()` | 打开/关闭 |

#### ClickContext

```java
interface ClickContext {
    Player player();          // 点击的玩家
    int slot();               // 原始槽位
    int row();                // 行（0-5）
    int col();                // 列（0-8）
    ItemStack cursor();       // 鼠标上的物品
    ItemStack current();      // 被点击的物品
    boolean isShiftClick();   // 是否 Shift 点击
    boolean isRightClick();   // 是否右键
    ClickType type();         // 点击类型枚举
    ChestGUI gui();           // 所属 GUI
}
```

#### 完整示例：服务器选择菜单

```java
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import server.sf.model.api.v3.SF;
import server.sf.model.api.v3.feature.gui.ChestGUI;
import server.sf.model.api.v3.feature.gui.GUIManager;

public class ServerMenu {

    public static void open(Player player) {
        GUIManager mgr = SF.sf().gui();

        ChestGUI gui = mgr.create("§8» §b选择服务器 §8«", 3);

        // 边框
        gui.border(Material.GRAY_STAINED_GLASS_PANE, " ");

        // 生存服
        gui.item(1, 2, Material.GRASS_BLOCK, "§a生存服", ctx -> {
            player.sendMessage("§a正在传送到生存服...");
            player.performCommand("server survival");
            ctx.gui().close(player);
        }, "§7点击进入", "§7在线: 42人");

        // 小游戏服
        gui.item(1, 4, Material.DIAMOND_SWORD, "§b小游戏服", ctx -> {
            player.sendMessage("§b正在传送到小游戏服...");
            player.performCommand("server games");
            ctx.gui().close(player);
        }, "§7点击进入", "§7在线: 18人");

        // 创造服
        gui.item(1, 6, Material.BRICKS, "§e创造服", ctx -> {
            player.sendMessage("§e正在传送到创造服...");
            player.performCommand("server creative");
            ctx.gui().close(player);
        }, "§7点击进入", "§7在线: 7人");

        // 关闭按钮
        gui.item(2, 4, Material.BARRIER, "§c关闭菜单", ctx -> {
            ctx.gui().close(player);
        });

        gui.onClose(p -> p.sendMessage("§7菜单已关闭"));

        gui.open(player);
    }
}
```

#### 分页示例：物品浏览器

```java
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import server.sf.model.api.v3.SF;
import server.sf.model.api.v3.feature.gui.ChestGUI;
import server.sf.model.api.v3.feature.gui.GUIManager;

import java.util.ArrayList;
import java.util.List;

public class ItemBrowser {

    public static void open(Player player, List<ItemStack> allItems) {
        GUIManager mgr = SF.sf().gui();

        ChestGUI gui = mgr.create("§8物品浏览器 §7(第1页)", 6);
        gui.readonly(true);

        // 分页：每页 45 格（前5行），最后一行放导航
        gui.pagination(allItems, 45);

        // 底部导航栏
        gui.item(5, 0, Material.ARROW, "§a上一页", ctx -> {
            if (gui.currentPage() > 0) {
                gui.page(gui.currentPage() - 1);
                gui.refresh(player);
            }
        });
        gui.item(5, 8, Material.ARROW, "§a下一页", ctx -> {
            if (gui.currentPage() < gui.totalPages() - 1) {
                gui.page(gui.currentPage() + 1);
                gui.refresh(player);
            }
        });
        gui.item(5, 4, Material.BARRIER, "§c关闭", ctx -> {
            ctx.gui().close(player);
        });

        gui.open(player);
    }
}
```

#### 动态刷新示例

```java
// 创建一个可动态刷新的商店界面
ChestGUI shop = SF.sf().gui().create("§6商店", 3);

// 放置商品（价格随时可变）
shop.item(1, 1, Material.IRON_INGOT, "§f铁锭 §7(10金币)", ctx -> {
    if (SF.sf().takeMoney(ctx.player(), 10)) {
        ctx.player().getInventory().addItem(new ItemStack(Material.IRON_INGOT, 1));
        ctx.player().sendMessage("§a购买成功！");
        shop.refresh(ctx.player());  // 刷新界面
    } else {
        ctx.player().sendMessage("§c金币不足！");
    }
});

// 定时刷新价格
SF.sf().runTimer(() -> {
    shop.item(1, 1, Material.IRON_INGOT, "§f铁锭 §7(" + getRandomPrice() + "金币)");
    shop.refresh();
}, 0L, 1200L);  // 每60秒刷新一次
```

#### 要点

- **纯 Java**：无 YAML，所有配置通过链式 API 完成
- **readonly 模式**：默认开启，玩家无法拿走 GUI 中的物品
- **自动事件监听**：`GUIManager.create()` 内部自动注册 Listener
- **自动清理**：玩家退出/关服时自动注销监听器、关闭界面
- **线程安全**：支持多玩家同时打开同一个 GUI 实例
- **分页内置**：`pagination()` 自动切页，配合 `prevPage()/nextPage()` 导航

---

### 玩法功能 API（v3 新增）

v3 新增 4 大玩法模块，均通过 `SFApi` 暴露，采用接口 + 实现分离模式，懒加载（首次调用自动注册事件并启动 tick 线程）。

| 玩法 | 入口方法 | 包路径 |
|------|---------|--------|
| 起床战争 | `api.bedwars()` | `server.sf.model.api.v3.feature.gameplay.bedwars` |
| PVP 竞技 | `api.pvp()` | `server.sf.model.api.v3.feature.gameplay.pvp` |
| 惊变尸潮 | `api.horde()` | `server.sf.model.api.v3.feature.gameplay.horde` |
| 保卫村庄 | `api.villageDefense()` | `server.sf.model.api.v3.feature.gameplay.village` |

> 💡 以下示例均假设已获取 API 实例：`SFApi api = SFApi.get();`

#### 🛏️ 起床战争（Bedwars）

**核心概念**：玩家分为多支队伍，每队拥有一张床。床被破坏后该队玩家死亡即淘汰，最后存活的队伍获胜。地图中设有资源生成器（铁锭/金锭/钻石/绿宝石），玩家用资源在商店购买装备。

**枚举**

```java
Bedwars.GameState  // WAITING, COUNTDOWN, PLAYING, ENDING
Bedwars.TeamColor  // RED, BLUE, GREEN, YELLOW, AQUA, WHITE, PINK, GRAY
```

**注册竞技场**

```java
Bedwars bw = api.bedwars();

Map<Bedwars.TeamColor, Location> spawns = new HashMap<>();
spawns.put(Bedwars.TeamColor.RED,   new Location(world, 100, 64, 0));
spawns.put(Bedwars.TeamColor.BLUE,  new Location(world, -100, 64, 0));

Map<Bedwars.TeamColor, Location> beds = new HashMap<>();
beds.put(Bedwars.TeamColor.RED,  new Location(world, 105, 64, 5));
beds.put(Bedwars.TeamColor.BLUE, new Location(world, -105, 64, 5));

List<Map<String, Object>> generators = new ArrayList<>();
Map<String, Object> ironGen = new HashMap<>();
ironGen.put("location", new Location(world, 0, 65, 0));
ironGen.put("material", Material.IRON_INGOT);
ironGen.put("interval", 20); // 每 20 tick 掉落一次
generators.add(ironGen);

bw.registerArena(
    "bw1",              // arenaId
    "起床战争-1",        // 显示名
    "world",            // 世界名
    2,                  // 最少玩家
    4,                  // 每队最大人数
    spawns,             // 各队出生点
    beds,               // 各队床位置
    lobbyLoc,           // 等待大厅
    spectatorLoc,       // 观战出生点
    generators,         // 资源生成器配置
    new ArrayList<>()   // 商店配置（可空）
);
```

**玩家加入 / 离开**

```java
bw.join(player, "bw1", Bedwars.TeamColor.RED);  // 加入红队
bw.leave(player);                                 // 离开游戏
```

**开始 / 结束游戏**

```java
bw.startCountdown("bw1", 10);        // 10 秒倒计时
bw.forceStart("bw1");                 // 强制开始
bw.forceEnd("bw1", TeamColor.RED);    // 强制结束，指定红队获胜（null = 平局）
```

**床破坏 & 方块保护**

```java
bw.breakBed(player, Bedwars.TeamColor.BLUE);  // 玩家破坏蓝队床
bw.isProtected("bw1", x, y, z);               // 检查坐标是否受保护
bw.placeBlock(player, x, y, z, Material.WOOL); // 放置方块（需在竞技场内）
bw.breakBlock(player, x, y, z);                 // 破坏方块
```

**资源生成器控制**

```java
bw.dropGenerator("bw1", Bedwars.TeamColor.RED, Material.IRON_INGOT, 1, 40);
// 在红队出生点附近每 40 tick 掉落 1 个铁锭

bw.setResourceDrop(new Location(world, 0, 65, 0), Material.DIAMOND, 1);
// 在指定位置设置资源掉落
```

**注册商店物品**

```java
bw.registerShopItem(
    Material.IRON_SWORD,        // 图标
    "铁剑",                      // 名称
    10,                          // 价格
    Material.IRON_INGOT,         // 货币类型
    List.of(new ItemStack(Material.IRON_SWORD)),  // 奖励物品
    p -> p.sendMessage("你购买了铁剑！")  // 购买回调
);
```

**事件监听**

```java
bw.onEvent(e -> {
    switch (e.type()) {
        case GAME_START    -> api.broadcast("起床战争开始！");
        case BED_BROKEN    -> {
            Bedwars.TeamColor broken = (Bedwars.TeamColor) e.data();
            api.broadcast(e.player().getName() + " 破坏了 " + broken.name + " 队的床！");
        }
        case PLAYER_DEATH  -> e.player().sendMessage("你死亡了");
        case TEAM_ELIMINATED -> {
            Bedwars.Team t = (Bedwars.Team) e.data();
            api.broadcast(t.color().name + " 队被淘汰！");
        }
        case GAME_END      -> {
            Bedwars.Team winner = (Bedwars.Team) e.data();
            if (winner != null) api.broadcast(winner.color().name + " 队获胜！");
        }
    }
});
```

**玩家统计**

```java
int kills     = bw.getKills(player, "bw1");
int beds      = bw.getBedsBroken(player, "bw1");
int deaths    = bw.getDeaths(player, "bw1");
int wins      = bw.getWins(player, "bw1");
bw.resetStats("bw1", player);  // 重置统计
```

---

#### ⚔️ PVP 竞技（PvPArena）

**核心概念**：支持多种对战模式（1v1、团队战、FFA、排位赛等），含 Kit 系统、ELO 等级、自动匹配队列。

**枚举**

```java
PvPArena.GameState    // WAITING, COUNTDOWN, FIGHTING, ENDING
PvPArena.Mode         // DUEL_1V1, TEAM_2V2, TEAM_3V3, TEAM_5V5, FFA, BATTLE_ROYALE, RANKED, PARTY
PvPArena.MatchResult  // WIN_A, WIN_B, DRAW, CANCELLED
```

**注册竞技场**

```java
PvPArena pvp = api.pvp();

pvp.registerArena(
    "arena1",
    "竞技场-1",
    List.of(new Location(world, 50, 64, 0)),   // spawnsA 队A出生点
    List.of(new Location(world, -50, 64, 0)),   // spawnsB 队B出生点
    lobbyLoc,     // 等待大厅
    specLoc,      // 观战点
    10,           // 最大人数
    List.of("DUEL_1V1", "TEAM_2V2", "FFA"),  // 允许的模式
    false,        // 是否允许建造
    false,        // 是否允许交互
    30            // 比赛结束后重置秒数
);
```

**注册 Kit（装备包）**

```java
pvp.registerKit(new PvPArena.Kit() {
    @Override public String id() { return "warrior"; }
    @Override public String name() { return "战士"; }
    @Override public String permission() { return "sf.kit.warrior"; }
    @Override public int price() { return 0; }
    @Override public List<ItemStack> armor() {
        return List.of(
            new ItemStack(Material.IRON_HELMET),
            new ItemStack(Material.IRON_CHESTPLATE),
            new ItemStack(Material.IRON_LEGGINGS),
            new ItemStack(Material.IRON_BOOTS)
        );
    }
    @Override public List<ItemStack> inventory() {
        return List.of(new ItemStack(Material.IRON_SWORD), new ItemStack(Material.GOLDEN_APPLE, 3));
    }
    @Override public List<String> effects() { return List.of("SPEED:1:600"); }
    @Override public double healthScale() { return 20.0; }
    @Override public double walkSpeed() { return 0.2; }
});
```

**玩家加入 / 匹配**

```java
// 手动加入队伍
pvp.joinTeamA(player, "arena1");
pvp.joinTeamB(player, "arena1");
pvp.joinFFA(player, "arena1");  // FFA 模式

// 自动匹配队列
pvp.queuePlayer(player, PvPArena.Mode.DUEL_1V1);
pvp.dequeuePlayer(player);

// 启动自动匹配器（定时检查队列并创建比赛）
pvp.startAutoMatchmaking();
pvp.setMatchmakerInterval(100); // 每 100 tick 检查一次

// 设置 Kit
pvp.setKit(player, "warrior");
pvp.giveKit(player, "warrior");  // 直接发放 Kit 物品
```

**开始 / 结束比赛**

```java
pvp.startCountdown("arena1", 5);     // 5 秒倒计时
pvp.forceStart("arena1");             // 强制开始
pvp.forceEnd("arena1", PvPArena.MatchResult.WIN_A);  // 强制结束，A 队获胜
```

**击杀 / 死亡记录**

```java
PvPArena.Match m = pvp.matchOf(player);
if (m != null) {
    pvp.addKill(m, killer, victim);
    pvp.addDeath(m, victim);
    boolean pvpAllowed = pvp.isPvPAllowedInMatch(m);
    pvp.damageMatchPlayer(m, attacker, victim, 10.0, EntityType.PLAYER);
}
```

**ELO 等级系统**

```java
int elo = pvp.getElo(player);                    // 获取总 ELO
int rankedElo = pvp.getElo(player, PvPArena.Mode.RANKED);  // 获取排位 ELO
pvp.addElo(player, PvPArena.Mode.RANKED, 25);    // 增加 ELO
pvp.setElo(player, PvPArena.Mode.RANKED, 1500);  // 设置 ELO

PvPArena.Rank rank = pvp.getRank(elo);           // 获取段位
// rank.tier(), rank.name(), rank.prefix(), rank.requiredElo()
```

**观战系统**

```java
pvp.registerSpectator(spectator, match);
pvp.removeSpectator(spectator, match);
List<Player> specs = pvp.spectators(match);
```

**事件监听**

```java
pvp.onEvent(e -> {
    switch (e.type()) {
        case START      -> api.broadcast("比赛开始！");
        case KILL       -> {
            Player killer = e.player();
            Player victim = (Player) e.data();
            api.broadcast(killer.getName() + " 击杀了 " + victim.getName());
        }
        case MATCH_END  -> {
            PvPArena.MatchResult r = (PvPArena.MatchResult) e.data();
            api.broadcast("比赛结束: " + r.name());
        }
        case ELO_CHANGE -> {
            int newElo = (int) e.data();
            e.player().sendMessage("你的 ELO 变更为: " + newElo);
        }
    }
});
```

**玩家统计**

```java
int wins      = pvp.getWins(player, PvPArena.Mode.RANKED);
int losses    = pvp.getLosses(player, PvPArena.Mode.RANKED);
int streak    = pvp.getWinStreak(player, PvPArena.Mode.RANKED);
int bestStreak = pvp.getBestWinStreak(player, PvPArena.Mode.RANKED);
int kills     = pvp.getKills(player, PvPArena.Mode.RANKED);
int deaths    = pvp.getDeaths(player, PvPArena.Mode.RANKED);
pvp.resetStats(player);
```

---

#### 🧟 惊变尸潮（Horde）

**核心概念**：类似"尸潮"玩法，玩家作为幸存者在波次中对抗大量怪物。支持难度系统、精英怪/Boss、倒地/复活机制、血月事件。

**枚举**

```java
Horde.GameState   // WAITING, COUNTDOWN, PREPARING, WAVE_ACTIVE, WAVE_INTERVAL, BOSS_WAVE, ENDING
Horde.Difficulty  // EASY, NORMAL, HARD, NIGHTMARE, APOCALYPSE
Horde.SpawnType   // NORMAL, ELITE, BOSS, SWARM, SPECIAL
```

**注册竞技场**

```java
Horde horde = api.horde();

horde.registerArena(
    "horde1",
    "尸潮生存-1",
    "world",
    1,                   // 最少玩家
    8,                   // 最大玩家
    List.of(spawn1, spawn2),        // 玩家出生点
    lobbyLoc,            // 大厅
    specLoc,             // 观战点
    List.of(mobSpawn1, mobSpawn2),  // 怪物出生点
    100,                 // 边界半径
    centerLoc,           // 边界中心
    Horde.Difficulty.NORMAL,        // 默认难度
    20,                  // 最大波次
    30,                  // 准备时间（秒）
    15,                  // 波次间隔（秒）
    false                // 是否允许建造
);
```

**玩家加入 / 开始**

```java
horde.join(player, "horde1");
horde.startCountdown("horde1", 10);
horde.forceStart("horde1");
horde.forceEnd("horde1");
```

**创建指定难度的游戏**

```java
Horde.Game g = horde.createGame("horde1", Horde.Difficulty.NIGHTMARE);
```

**波次怪物规则**

```java
horde.addWaveMobRule("horde1", 1, new Horde.MobRule() {
    @Override public EntityType type() { return EntityType.ZOMBIE; }
    @Override public SpawnType spawnType() { return SpawnType.NORMAL; }
    @Override public int weight() { return 100; }
    @Override public int minCount() { return 5; }
    @Override public int maxCount() { return 10; }
    @Override public double healthMul() { return 1.0; }
    @Override public double damageMul() { return 1.0; }
    @Override public double speedMul() { return 1.0; }
    @Override public List<String> effects() { return List.of(); }
    @Override public Map<EntityType, Double> equipmentChance() { return Map.of(); }
});

horde.removeWaveMobRule("horde1", 1, EntityType.ZOMBIE);  // 移除规则
```

**难度倍率配置**

```java
horde.setDifficultyMultiplier(Horde.Difficulty.HARD, "health", 1.5);
horde.setDifficultyMultiplier(Horde.Difficulty.HARD, "damage", 1.3);
horde.setDifficultyMultiplier(Horde.Difficulty.HARD, "spawnCount", 2.0);
double hpMul = horde.getDifficultyMultiplier(Horde.Difficulty.HARD, "health");
```

**注册精英怪 / Boss**

```java
horde.registerElite(
    EntityType.ZOMBIE,       // 基础实体
    "elite_zombie",          // 精英 ID
    "精英僵尸",               // 显示名
    3.0,                     // 血量倍率
    2.0,                     // 伤害倍率
    1.2,                     // 速度倍率
    List.of("SPEED:2:99999", "STRENGTH:1:99999"),  // 永久药水效果
    Map.of("skill_explosion", true)  // 技能配置
);

horde.registerBoss(
    EntityType.WITHER,       // 基础实体
    "boss_necromancer",      // Boss ID
    "死灵法师",               // 显示名
    5.0,                     // 血量倍率
    3.0,                     // 伤害倍率
    0.8,                     // 速度倍率
    List.of("REGENERATION:2:99999"),
    Map.of("skill_summon", true, "skill_teleport", true),
    List.of(new ItemStack(Material.NETHER_STAR)),  // 掉落物
    500                      // 击杀得分
);
```

**倒地 / 复活机制**

```java
horde.downPlayer(player, 30);        // 玩家倒地，30 秒倒计时
boolean downed = horde.isPlayerDowned(player);
horde.revivePlayer(deadPlayer, reviver);  // 另一玩家复活倒地玩家
```

**波次奖励 / 击杀奖励**

```java
horde.registerWaveReward(
    5,                                    // 第 5 波
    List.of(new ItemStack(Material.DIAMOND, 2)),  // 物品奖励
    100.0,                                // 每人金钱
    200                                   // 每人得分
);

horde.registerKillReward(
    EntityType.ZOMBIE,
    10,                                   // 得分
    5.0,                                  // 金钱
    List.of(new ItemStack(Material.ROTTEN_FLESH))  // 掉落
);
```

**血月事件**

```java
horde.setBloodMoonChance("horde1", 0.1);  // 10% 概率触发血月
boolean bloodMoon = horde.isBloodMoon(game);  // 当前是否血月
```

**手动生成波次怪物**

```java
Horde.Game g = horde.getGame("horde1");
Horde.Wave w = g.currentWave();
int spawned = horde.spawnWaveMobs(g, w);
horde.clearArenaMobs("horde1");  // 清空竞技场怪物
```

**事件监听**

```java
horde.onEvent(e -> {
    switch (e.type()) {
        case WAVE_START       -> api.broadcast("第 " + e.game().currentWave().number() + " 波开始！");
        case BOSS_WAVE_START  -> api.broadcast("Boss 波次来袭！");
        case PLAYER_DOWN      -> {
            int timer = (int) e.data();
            e.player().sendMessage("你倒地了！" + timer + " 秒内需要队友复活");
        }
        case PLAYER_REVIVE    -> api.broadcast(e.player().getName() + " 复活了一名队友");
        case MOB_KILL         -> {
            LivingEntity mob = (LivingEntity) e.data();
            e.player().sendMessage("击杀 " + mob.getType().name());
        }
        case SURVIVOR_WIN     -> api.broadcast("幸存者胜利！");
        case GAME_END         -> {
            int wave = e.game().currentWave().number();
            api.broadcast("游戏结束，存活到第 " + wave + " 波");
        }
    }
});
```

**玩家统计**

```java
int waves    = horde.getWavesSurvived(player, "horde1");
int kills    = horde.getTotalKills(player, "horde1");
int deaths   = horde.getTotalDeaths(player, "horde1");
int bestWave = horde.getBestWave(player, "horde1");
int bestScore = horde.getBestScore(player, "horde1");
int played   = horde.getGamesPlayed(player, "horde1");
int won      = horde.getGamesWon(player, "horde1");
horde.resetStats("horde1", player);
```

---

#### 🏰 保卫村庄（VillageDefense）

**核心概念**：玩家保卫村庄核心建筑（CORE），通过建造防御塔/城墙/资源建筑、招募单位来抵御一波又一波的敌人进攻。核心血量归零即失败，存活到最后一波即胜利。

**枚举**

```java
VillageDefense.GameState    // WAITING, COUNTDOWN, BUILD_PHASE, WAVE_ACTIVE, WAVE_INTERVAL, ENDING
VillageDefense.BuildingType // CORE, TOWER_ARROW, TOWER_MAGIC, TOWER_CANNON, WALL, GATE,
                            // GOLD_MINE, LUMBER_CAMP, BARRACKS, BLACKSMITH, WELL, FARM, VILLAGER_HOUSE
VillageDefense.UnitType     // VILLAGER, GUARD, ARCHER, KNIGHT, MAGE, HEALER, WORKER
VillageDefense.EnemyType    // RAIDER, ARCHER, GRUNT, BRUTE, SHAMAN, BOSS_WARCHIEF, BOSS_BEHEMOTH
```

**注册竞技场**

```java
VillageDefense vd = api.villageDefense();

vd.registerArena(
    "vd1",
    "村庄保卫-1",
    "world",
    1,                   // 最少玩家
    8,                   // 最大玩家
    lobbyLoc,            // 大厅
    specLoc,             // 观战点
    coreLoc,             // 核心建筑位置
    List.of(spawn1, spawn2),            // 玩家出生点
    List.of(enemySpawn1, enemySpawn2),  // 敌人生成点
    80,                  // 地图半径
    30,                  // 最大波次
    180,                 // 建造阶段时间（秒）
    30,                  // 波次间隔（秒）
    400.0,               // 核心最大血量
    true                 // 是否允许建造
);
```

**玩家加入 / 开始**

```java
vd.join(player, "vd1");
vd.startCountdown("vd1", 10);
vd.forceStart("vd1");       // 跳过倒计时直接进入建造阶段
vd.forceEnd("vd1", true);   // true=胜利, false=失败
```

**建造建筑**

```java
VillageDefense.Building core = vd.build(player, VillageDefense.BuildingType.CORE, coreLoc);
VillageDefense.Building tower = vd.build(player, VillageDefense.BuildingType.TOWER_ARROW, towerLoc);
VillageDefense.Building wall  = vd.build(player, VillageDefense.BuildingType.WALL, wallLoc);
VillageDefense.Building mine  = vd.build(player, VillageDefense.BuildingType.GOLD_MINE, mineLoc);

// 升级建筑
vd.upgradeBuilding(player, tower);

// 修复建筑
vd.repairBuilding(player, tower, 50.0);

// 拆除建筑（核心不可拆除）
vd.demolish(player, tower);
```

**建筑属性查询**

```java
Building b = vd.build(player, VillageDefense.BuildingType.TOWER_CANNON, loc);
b.type();           // TOWER_CANNON
b.level();          // 当前等级
b.maxLevel();       // 最大等级
b.health();         // 当前血量
b.maxHealth();      // 最大血量
b.attackRange();    // 攻击范围
b.attackDamage();   // 攻击伤害
b.attackSpeedTicks(); // 攻击间隔（tick）
b.resourcePerTick();  // 每 tick 产出资源（金矿/伐木场等）
b.upgrade();          // 升级
b.repair(100);        // 修复
b.setEnabled(false);  // 禁用
```

**生成单位**

```java
VillageDefense.Unit guard = vd.spawnUnit(game, VillageDefense.UnitType.GUARD, spawnLoc, barracksBuilding);
VillageDefense.Unit archer = vd.spawnUnit(game, VillageDefense.UnitType.ARCHER, spawnLoc, barracksBuilding);

// 单位操作
guard.attack(targetEntity);
guard.moveTo(newLoc);
guard.heal(20.0);
guard.isAlive();
vd.removeUnit(guard);
```

**资源系统**

```java
vd.grantResource(player, "gold", 100);    // 给予资源
boolean ok = vd.spendResource(player, "gold", 50);  // 消耗资源
int gold = vd.getResource(player, "gold"); // 查询资源
// 资源类型: gold, wood, stone, iron
```

**建筑 / 单位 / 敌人属性配置**

```java
// 设置建筑花费（1 级）
vd.setBuildingCost(VillageDefense.BuildingType.TOWER_ARROW, Map.of("gold", 100, "wood", 50));

// 获取指定等级花费（自动按 1.5^level 递增）
Map<String, Integer> cost = vd.getBuildingCost(VillageDefense.BuildingType.TOWER_ARROW, 3);

// 设置建筑属性
vd.setBuildingStats(VillageDefense.BuildingType.TOWER_ARROW, 2, Map.of("health", 150.0, "damage", 8));

// 设置单位花费 / 属性
vd.setUnitCost(VillageDefense.UnitType.KNIGHT, Map.of("gold", 100, "iron", 20));
vd.setUnitStats(VillageDefense.UnitType.KNIGHT, Map.of("health", 60.0, "damage", 10, "armor", 5, "speed", 1.0));

// 设置敌人属性
vd.setEnemyStats(VillageDefense.EnemyType.BRUTE, Map.of("health", 100.0, "damage", 10.0, "speed", 0.8));
```

**自定义波次敌人**

```java
vd.addWaveSpawn("vd1", 5, new VillageDefense.EnemySpawn() {
    @Override public EnemyType type() { return EnemyType.BRUTE; }
    @Override public int count() { return 5; }
    @Override public int intervalTicks() { return 60; }
    @Override public double healthMul() { return 1.5; }
    @Override public double damageMul() { return 1.2; }
    @Override public double speedMul() { return 0.9; }
});

vd.removeWaveSpawns("vd1", 5);  // 移除第 5 波所有自定义生成
```

**波次奖励 / 击杀奖励**

```java
vd.registerWaveReward(
    10,                                    // 第 10 波
    Map.of("gold", 100, "wood", 50),       // 每人资源
    500                                    // 每人得分
);

vd.registerKillReward(
    VillageDefense.EnemyType.BOSS_WARCHIEF,
    200,                                   // 得分
    50,                                    // 金币
    List.of(new ItemStack(Material.DIAMOND))  // 掉落
);
```

**核心操作**

```java
vd.damageCore("vd1", 50.0, enemyEntity);  // 对核心造成伤害
vd.healAllBuildings("vd1", 0.5);           // 修复所有建筑 50% 血量

// 查询附近实体
List<LivingEntity> enemies = vd.findEnemiesNear(centerLoc, 20.0);
List<Building> towers = vd.findBuildingsNear(centerLoc, 30.0, VillageDefense.BuildingType.TOWER_ARROW);
```

**建造限制**

```java
vd.setBuildLimitPerPlayer("vd1", VillageDefense.BuildingType.TOWER_ARROW, 5);
int limit = vd.getBuildLimitPerPlayer("vd1", VillageDefense.BuildingType.TOWER_ARROW);
```

**事件监听**

```java
vd.onEvent(e -> {
    switch (e.type()) {
        case BUILD_START       -> api.broadcast("建造阶段开始！抓紧时间建设防御");
        case WAVE_START        -> {
            VillageDefense.Wave w = (VillageDefense.Wave) e.data();
            api.broadcast("第 " + w.number() + " 波敌人来袭！");
        }
        case BUILDING_BUILT    -> {
            VillageDefense.Building b = (VillageDefense.Building) e.data();
            e.player().sendMessage("建造了 " + b.type().name());
        }
        case BUILDING_DESTROYED -> {
            VillageDefense.Building b = (VillageDefense.Building) e.data();
            api.broadcast("建筑被摧毁: " + b.type().name());
        }
        case CORE_DAMAGED      -> {
            double hp = e.game().coreHealth();
            api.broadcast("核心受到攻击！剩余血量: " + (int) hp);
        }
        case CORE_DESTROYED    -> api.broadcast("核心被摧毁！村庄陷落！");
        case ENEMY_KILL        -> {
            int score = (int) e.data();
            e.player().sendMessage("击杀敌人 + " + score + " 分");
        }
        case VICTORY           -> api.broadcast("村庄保卫成功！全员胜利！");
        case DEFEAT            -> api.broadcast("村庄陷落...游戏失败");
    }
});
```

**玩家统计**

```java
int waves     = vd.getWavesSurvived(player, "vd1");
int bestWave  = vd.getBestWave(player, "vd1");
int bestScore = vd.getBestScore(player, "vd1");
int buildings = vd.getBuildingsBuilt(player, "vd1");
int units     = vd.getUnitsSpawned(player, "vd1");
int played    = vd.getGamesPlayed(player, "vd1");
int won       = vd.getGamesWon(player, "vd1");
int kills     = vd.getTotalKills(player, "vd1");
vd.resetStats("vd1", player);
```

---

#### 玩法 API 通用说明

**生命周期**

所有玩法模块均为懒加载，首次调用 `api.bedwars()` / `api.pvp()` / `api.horde()` / `api.villageDefense()` 时自动：
1. 实例化实现类
2. 注册 Bukkit 事件监听器
3. 启动 tick 定时任务（1 tick/tick 频率）

插件卸载时 `SF.shutdown()` 会自动调用各模块的 `shutdown()` / `stop()` 方法清理资源。

**线程安全**

| 操作 | 线程安全 | 说明 |
|------|----------|------|
| 注册竞技场 / Kit / 规则 | ⚠️ | 建议在 `onEnable` 主线程调用 |
| 玩家加入 / 离开 | ⚠️ | 必须主线程 |
| 游戏状态查询 | ✅ | `ConcurrentHashMap` / `CopyOnWriteArrayList` 保证安全 |
| 事件监听注册 | ⚠️ | 建议主线程 |
| 统计数据查询 | ✅ | 线程安全 |
| `forceStart` / `forceEnd` | ⚠️ | 建议主线程 |

> 💡 不确定时，用 `api.run(() -> { ... })` 包裹代码确保主线程执行。

**数据持久化**

玩法模块的统计数据（击杀/死亡/胜率/ELO/最高波次等）存储在内存中的 `ConcurrentHashMap`，插件重启后重置。如需持久化，可通过 `SFApi.database()` 将数据写入 SQLite/MySQL。

**自定义扩展**

每个玩法模块均支持通过事件回调（`onEvent`）进行二次开发，无需继承实现类。如需更深度的定制（如自定义怪物 AI、特殊技能），可直接实例化对应 Impl 类并覆写方法。

```java
// 直接获取实现类进行高级操作
SF sf = (SF) SFApi.get();
BedwarsImpl bwImpl = (BedwarsImpl) sf.bedwars();
// 可访问实现类内部方法
```

---

## ❓ 常见问题

### 安装相关

**Q: 启动后报错 `java.lang.NoClassDefFoundError: server/sf/model/api/v2/SFApi`**

A：你的服务器没有正确加载 SF 插件。检查：

1. jar 文件是否在 `plugins/` 目录下
2. 启动顺序：SF 应该在其他依赖它的插件之前加载（在 `plugin.yml` 中声明 `depend: [ZeroCkate_SFServerPlugin]`）
3. 控制台日志中是否有 SF 启动失败的错误

**Q: 经济系统显示 `Economy ready: false`**

A：SF 没有检测到任何经济后端。检查：

1. 是否安装了 [EssentialsX](https://essentialsx.net/) 或 [Vault](https://www.spigotmc.org/resources/vault.34315/)
2. `plugin.yml` 中 `softdepend` 是否包含 `Essentials, Vault`（默认已配置）
3. 重启服务器，看启动日志中是否显示 `Essentials=true` 或 `Vault=true`

**Q: 数据库报错 `SQLException: database is locked`**

A：SQLite 在并发写入时会锁定。解决方案：

1. 切换到 MySQL（在 `config.yml` 中 `database.mysql.enabled: true`）
2. 或减少异步数据库操作

**Q: 切换到 MySQL 后报错 `Communications link failure`**

A：检查 MySQL 连接：

1. MySQL 服务是否在运行
2. 主机/端口是否正确
3. 用户名/密码是否正确
4. 数据库是否存在（需要手动创建）
5. 防火墙是否放行 3306 端口

```sql
CREATE DATABASE minecraft CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 命令相关

**Q: `/home` 提示家不存在**

A：检查家的名称。如果不指定名称，默认使用 `default`：

```
/home           # 访问名为 "default" 的家
/home myhome    # 访问名为 "myhome" 的家
/sethome myhome # 创建名为 "myhome" 的家
```

用 `/homes` 查看所有家。

**Q: `/tpa` 请求没反应**

A：检查：

1. 对方是否在线（`/tpa` 只能发给在线玩家）
2. 对方是否已经有待处理请求（一次只能有一个）
3. 是否被对方用 `/tpdeny` 拒绝
4. 请求是否已超时（默认 60 秒）

**Q: 传送提示 "你移动了，传送已取消"**

A：这是**延迟传送**机制。在传送延迟期间移动会取消传送。

- 修改 `config.yml` 中的 `teleport.delay.*` 为 `0` 可以禁用延迟
- 拥有 `sf.teleport.bypass` 权限可以跳过延迟

**Q: `/vanish` 后 OP 也看不到我**

A：需要给 OP 玩家单独赋予权限：

```bash
lp user 你的名字 permission set sf.admin.seevanished true
```

**Q: `/gm` 命令的参数是什么**

A：支持以下所有写法：

| 数字 | 缩写 | 全名 | 模式 |
|------|------|------|------|
| 0 | s | survival | 生存 |
| 1 | c | creative | 创造 |
| 2 | a | adventure | 冒险 |
| 3 | sp | spectator | 旁观 |

例如 `/gm 1` 和 `/gm creative` 等价。

### API 相关

**Q: `SFApi.get()` 抛出 `IllegalStateException`**

A：SF API 没有被注册。可能原因：

1. SF 插件未启用（检查 `/plugins` 命令）
2. 你的插件先于 SF 加载（在 `plugin.yml` 中添加 `depend: [ZeroCkate_SFServerPlugin]`）
3. SF 启动失败（检查控制台日志）

正确做法：

```java
if (!SFApi.isAvailable()) {
    getLogger().warning("SF API 不可用");
    return;
}
SFApi api = SFApi.get();
```

**Q: 调用 `giveMoney` 返回 `false`**

A：可能原因：

1. 经济系统未就绪（先检查 `api.economy().ready()`）
2. 玩家没有经济账户（先检查 `api.economy().hasAccount(player)`）
3. 金额为负数（SF 会拒绝负数操作）
4. 操作在异步线程执行但 Essentials 不支持（改用 `api.run(() -> api.giveMoney(p, 100))`）

**Q: 异步线程中调用 API 报错**

A：Bukkit 的大部分 API 都**不是线程安全**的。在异步线程中：

- ✅ 可以调用：`logger.*`, `economy.balance/format`, `scheduler.runAsync`
- ❌ 不可调用：`teleport`, `broadcast`, `msg`, `events.on`, `economy.give/take/set`

正确做法：异步中查询数据，主线程中修改游戏状态：

```java
api.runAsync(() -> {
    double balance = api.balance(player);
    api.run(() -> {
        api.giveMoney(player, 100);
        api.msg(player, "钱到账了");
    });
});
```

**Q: 通过 `sf.events().on()` 注册的监听器不生效**

A：检查：

1. 是否在 `onEnable()` 中注册（不要在 `onLoad()` 中）
2. 事件类是否正确导入（例如 `AsyncPlayerChatEvent` vs `PlayerChatEvent`）
3. 是否被其他插件取消（设置更高优先级 `EventPriority.HIGH`）
4. 控制台是否有异常日志

**Q: 编译报错找不到 `SFApi` 类**

A：Maven/Gradle 依赖配置问题。检查：

1. 是否添加了 JitPack 仓库
2. 依赖 scope 是否正确（`provided` 或 `compileOnly`）
3. 是否执行了 `mvn clean install` 刷新依赖

### 性能相关

**Q: 服务器 TPS 下降**

A：排查步骤：

1. 使用 `/tps` 查看当前 TPS
2. 检查是否有大量异步数据库操作（改为批量操作）
3. 检查 `sf.events().on()` 注册的监听器是否过多或过重
4. 切换到 MySQL 避免 SQLite 锁争用

**Q: 数据库查询慢**

A：

1. SQLite：启用 WAL 模式（默认已启用）
2. MySQL：确保 `homes(uuid, name)` 和 `warps(name)` 有索引（建表时已添加 PRIMARY KEY）
3. 避免在循环中频繁查询，用 `getHomes(uuid)` 一次获取所有

### 其他

**Q: 如何卸载插件而不丢失数据**

A：

1. 停止服务器
2. 备份 `plugins/ZeroCkate_SFServerPlugin/data.db`（SQLite）或导出 MySQL 数据库
3. 删除 jar 文件
4. 数据保留在备份中，下次安装时恢复即可

**Q: 多个服务器能共享家数据吗**

A：可以。所有服务器连同一个 MySQL 数据库，并使用相同的 `prefix`：

```yaml
database:
  mysql:
    enabled: true
    host: shared.db.example.com
    database: mc_network
    prefix: "sf_shared_"
```

如果想让数据相互独立，使用不同的 `prefix`。

**Q: 如何向作者反馈 bug**

A：在 [GitHub Issues](https://github.com/zmb96/ZeroEngine/issues) 提交 issue，附上：

- SF 插件版本
- 服务器类型（Paper/Spigot）和版本
- 完整的错误日志（堆栈跟踪）
- 复现步骤

**Q: 可以商用吗**

A：SF 使用 GPLv3 协议，允许商用、修改、分发，但衍生作品必须同样以 GPLv3 开源。详见 [LICENSE](LICENSE)。

---

## 📝 变更日志

本项目版本变更记录遵循 [语义化版本](https://semver.org/lang/zh-CN/)。

### [3.2.4-LTS] - 2026-08-24

#### ✨ 新增

**自定义生物系统（v3/feature/entity/）**

全新模块，继承 `SEntity` 抽象基类即可定义自定义生物，覆盖完整生命周期：

- `SEntity` 抽象基类：`id()` / `displayName()` / `entityType()` 必须实现；属性方法（HP / 攻击 / 速度 / 护甲 / 韧性 / 击退抗性 / 追踪范围 / 飞行速度）带默认值可重写
- `Hostility` 枚举：`HOSTILE`（主动攻击）/ `NEUTRAL`（被攻击反击）/ `PASSIVE`（永不攻击），`EntityListener.onTarget` 自动按阵营取消目标事件
- `SpawnCondition`：链式构造生成条件 —— 几率 / 世界 / 群系 / Y 范围 / 光照范围 / 怕光照白天燃烧 / 仅夜晚 / 替换原版生物 / 每区块上限
- `EquipmentEntry`：生成时按几率穿戴物品 / 盔甲，可配置死亡掉率
- 7 个事件钩子：`onSpawn` / `onDeath` / `onAttack`（攻击玩家）/ `onDamaged` / `onTarget` / `onTick`（每 5 SFTick）/ `onPerSecond`（每秒）
- `EntityManager`：注册 / 查询 / 强生成 `spawn(id, loc)` / 按条件生成 `trySpawn(id, loc)` / PDC 反查 `find(living)` / 活动实例追踪
- `EntityListener`：监听 `CreatureSpawnEvent` / `EntityDamageByEntityEvent` / `EntityDamageEvent` / `EntityDeathEvent` / `EntityTargetEvent` / `EntityCombustEvent`，自动调度 SFTick 任务
- `SFEntityCommand`（`/sfentity` 别名 `/sfe`，权限 `sf.admin.entity`）：`list` / `spawn` / `info` / `count` / `cleanup` / `reload` / `help`
- `SF.java` 新增 `entities()` 懒加载装配方法，自动注册监听 + 启动 tick 调度 + 绑定命令
- 内置示例 `ShadowStalkerEntity`（`shadow_stalker`）：HUSK 材质，40 HP / 6 攻击，夜晚生成 / 怕光照 / 光照 0~7，50% 铁剑 + 30% 铁头盔，攻击附毒 III，每 20 SFTick 拖紫色粒子，1% 几率每秒回 1 血

**SFAttr 属性常量库扩展**

- 新增 `BLOCK_BREAK_SPEED`（方块破坏速度，Bukkit 1.21+）
- 新增 `JUMP_STRENGTH`（跳跃强度，Bukkit 1.21+）
- 新增 `EXPLOSION_KNOCKBACK_RESISTANCE`（爆炸击退抗性，Bukkit 1.21+ 正确名）
- 保留 `EXPLOSION_KNOCKBACK_REDUCTION` 作为兼容旧名（运行时仍可命中）
- 新增同名 `GENERIC_BLOCK_BREAK_SPEED` / `GENERIC_JUMP_STRENGTH` / `GENERIC_EXPLOSION_KNOCKBACK_RESISTANCE` 兼容前缀
- DISPLAY 中文名表补全 3 项：方块破坏速度 / 跳跃强度 / 爆炸击退抗性
- 新增快捷构造方法：`blockBreakSpeed(base, perLevel)` / `jumpStrength(base, perLevel)` / `explosionKnockbackResistance(base, perLevel)` / `spawnReinforcements(base, perLevel)`（修复 `SPAWN_REINFORCEMENTS` 唯一缺快捷构造的缺口）

#### 🔄 变更

- `pom.xml` 版本：`3.2.3-LTS` → `3.2.4-LTS`
- `plugin.yml` 注册 `sfentity` 命令 + `sf.admin.entity` 权限
- `SF.java` 添加 `entity()` 懒加载方法 + shutdown 清理逻辑
- README 新增「🧟 自定义生物系统」章节、SFAttr 表格补充 3 个新属性、特性列表加入生物注册系统

### [3.2.1] - 2026-08-16

#### ✨ 新增

**箱子 GUI 系统（ChestGUI）**
- 新增 `feature/gui/` 模块：`ChestGUI` 接口 + `GUIManager` 管理器
- `SFApi` 新增 `gui()` 方法，返回 `GUIManager`
- 支持链式调用：title / rows / item / fill / border / fillRange / clear
- 点击回调系统：`Consumer<ClickContext>`，支持 8 种 ClickType
- 分页支持：`pagination(items, perPage)` + `page() / nextPage() / prevPage()`
- 生命周期回调：`onOpen / onClose / onAnyClick`
- readonly 模式：默认禁止拿取物品
- 动态刷新：`refresh() / refresh(Player)`
- 自动事件监听 + 玩家退出/关服自动清理

### [3.2.0] - 2026-08-15

#### 🔄 重大变更

**项目更名为 ZeroEngine**
- artifactId: `ZeroCkate_ServerApiPlugin` → `ZeroEngine`
- 插件从「服务器 API 插件」升级为「服务器引擎」
- GitHub 仓库迁移至 `zmb96/ZeroEngine`
- 保留全部旧功能，无破坏性变更

#### ✨ 新增

**ZeroEngine 原版操控引擎（v2/feature/engine/）**

5 大引擎模块，全部作为 API 供外部插件调用，引擎本身不参与业务逻辑：

1. **MonsterAttribute（怪物属性操控）**
   - 设置/获取/重置生物的攻击伤害、生命值、移动速度、护甲、击退抗性、护甲韧性
   - 按倍率缩放属性（`scale`）
   - 添加/移除属性修饰器（`addModifier` / `removeModifier`）
   - 持久化属性修饰（跨 tick 保持，`applyPersistent` / `getPersistent` / `clearPersistent`）

2. **DamageSystem（伤害系统操控）**
   - 自定义伤害计算公式（`registerDamageModifier`，按优先级链式执行）
   - 全局/按世界 PvP 开关（`setPvpEnabled`）
   - 伤害类型倍率（`setDamageMultiplier`，如摔伤减半、火焰免疫）
   - 护甲穿透（`setArmorPenetration`，0~1 百分比）
   - 对特定目标设置固定伤害（`setCustomDamage`）
   - `DamageContext` 接口提供攻击者/受害者/原始伤害/伤害原因/暴击/取消

3. **BlockControl（方块/挖掘操控）**
   - 修改挖掘速度（`setBreakSpeed`）
   - 修改爆炸抗性（`setBlastResistance`）
   - 自定义掉落物和概率（`setDrop`）
   - 自定义经验掉落范围（`setExpDrop`）
   - 注册方块破坏处理器（`registerBreakHandler`，可取消破坏）
   - 工具要求（`setRequireTool`）
   - 破坏后替换方块（`setReplaceOnBreak`）
   - 取消区域内方块更新（`cancelBlockUpdate`）

4. **SpawnControl（实体生成操控）**
   - 创建/注册/移除生成规则（`SpawnRule`：概率、每区块上限、生效世界）
   - 实体黑名单（`blacklistEntity`，按世界）
   - 生成上限（`setSpawnCap`，按世界按类型）
   - 实体类型过滤器（`registerSpawnFilter`）
   - 生成位置过滤器（`registerLocationFilter`，如出生点保护）
   - 强制生成（`forceSpawn`）
   - 清除世界内指定类型实体（`clearEntities`）
   - 世界实体数量统计（`getEntityCounts`）

5. **ResourcePackManager（资源包管理）**
   - 创建/注册/发送资源包（`ResourcePack`：URL、SHA1、强制、提示消息）
   - 批量发送所有资源包（`sendAll`，支持完成回调）
   - 自定义模型数据注册（`setCustomModelData`）
   - 自定义音乐注册和播放（`registerMusic` / `playMusic` / `stopMusic`）
   - 全服音乐控制（`playMusicAll` / `stopMusicAll`）
   - 默认资源包设置（`setDefaultPack` / `getDefaultPack`）

**API 入口**
- `SFApi` 新增 5 个方法：`monster()` / `damage()` / `block()` / `spawn()` / `resourcePack()`
- `SF` 实现懒加载，首次调用时自动初始化并注册事件监听器
- 全部模块均在 `feature/engine/` 和 `feature/engine/impl/` 目录下

#### 🔧 优化

**代码风格统一**
- 全项目 `SF.sf().method()` 链式调用改为 `SF sf = SF.sf(); sf.method()` 局部变量写法
- 涉及 30+ 文件、300+ 处调用点
- 减少重复方法调用开销，提升可读性

### [3.0.0] - 2026-08-07

#### ✨ 新增

**SFText 文本组件 API（v2/feature/text/）**
- `SFText`：基于 Adventure Component 的富文本工具类
- 物品精灵图：`item(ItemStack)` / `item(ItemStack, String)` — hover 显示物品详情
- 玩家头颅：`skull(OfflinePlayer)` / `skull(UUID, String)` / `skullByTexture(String, String)`
- 交互组件：`url()` / `command()` / `suggest()` / `copy()` / `tooltip()`
- `Builder` 链式拼接，支持 appendItem / appendSkull / appendUrl 等
- 辅助方法：`text()` / `newline()` / `separator()` / `plain()`

**聊天事件优先级 API（v2/feature/chat/）**
- `ChatHandler` 接口：按 priority 依次执行，数值越小越先执行
- `ChatContext`：`consume()` 不广播、`cancel()` 中断链、`formattedMessage()` 修改内容、`channel()` 切换频道
- `markListening` / `unmarkListening` / `isPluginListening` — 一次性输入拦截
- `SF.isPluginListenerChat(Player)` — 判断玩家是否被标记
- `ChatManager.registerHandler()` / `unregisterHandler()` — 线程安全注册

**性能优化系统（v2/feature/perf/）**
- `PerformanceManager`：4 大优化模块，基于 SF Tick 异步运行
- 内存监控：JMX Heap 读取，85% 告警，90% 自动 GC
- 区块管理：自动卸载空闲区块，每周期最多 50 个
- 实体清理：掉落物 60 秒、弹射物 10 秒、单区块 50 实体阈值
- TPS 自适应：TPS < 15 降最小视距，TPS < 18 降 2，正常恢复
- `PerformanceCommand`：`/sfperf`（`/sfp`）命令
- `PerformanceListener`：事件追踪

#### 🛠️ 变更
- `SF.java` 新增 `perf()` 入口方法和 `isPluginListenerChat()` 方法
- `ChatManager` 新增 `pluginListening` Set 和 handler 注册/分发机制
- `ChatListener` 重构：先检查 `markListening`，再走禁言/过滤/handler 链
- `ChatHandler.ChatContext` 拆分 `consume`（不广播）和 `cancel`（中断链）两个概念
- `pom.xml` 版本号升级到 3.0.0
- README.md 新增 SFText / 聊天优先级 / 性能优化三个章节

### [2.0.0] - 2026-08-05

#### ✨ 新增

**附魔书被动获取**
- `EnchantChestListener`：打开箱子有概率生成附魔书（默认 5%，每箱最多 2 本）
- `EnchantTableListener`：附魔台附魔时有概率获得自定义附魔（默认 15% + 书架加成）
- `EnchantManager.createBook()` / `giveBook()`：API 创建和给予附魔书

**物品被动获取**
- `ItemChestListener`：打开箱子有概率生成自定义物品（默认 3%，每箱最多 1 件）

**交互距离系统**
- `ReachManager`：动态调整玩家方块/实体交互距离
- `ReachCommand`：`/sfreach`（`/sfre`）命令
- 属性查找改用反射兼容不同 Paper 版本

**属性查找日志**
- 附魔和物品系统 `findAttribute()` 添加详细日志输出
- 匹配成功：INFO 级别显示匹配的属性名
- 匹配失败：WARN 级别显示尝试的候选列表

**SF 注册修复**
- SF 类同时注册 `SF.class` 和 `SFApi.class` 到 ServicesManager
- 两种 `load()` 写法都能正常获取实例

### [1.1.0] - 2026-08-04

#### ✨ 新增

**SF Tick 系统（v2/feature/tick/）**
- `TickManager`：独立线程运行，1 秒 = 100 tick，不干扰原版 20tick/秒
- `TickTask`：函数式接口，支持 lambda
- `runLater` / `runTimer` / `cancel` 任务调度
- `runSync` / `runSyncLater` 主线程切换
- 时间换算：`toSeconds` / `fromSeconds` / `toBukkitTicks` / `fromBukkitTicks`
- 所有新 API 的定时功能基于此系统

**世界管理（v2/feature/world/）**
- `WorldManager`：时间/天气/难度/PVP/边界/生物/火焰/预设
- `WorldCommand`：`/sfworld`（`/sfw`）命令
- 支持 15+ 子命令，含 Tab 补全
- 世界预设保存/加载

**聊天系统（v2/feature/chat/）**
- `ChatManager`：多频道、禁言、屏蔽词、格式化
- `ChatListener`：拦截聊天，频道分发
- `ChatCommand`：`/sfchat`（`/sfc`）命令
- 内置频道：global（全局）、local（附近 100 格）、staff（管理）
- 支持动态创建/删除频道
- 禁言基于 SF Tick 系统，支持临时/永久
- 脏话过滤，自动替换屏蔽词
- 与权限系统集成，自动读取前缀后缀

**权限系统（v2/feature/permission/）**
- `PermissionManager`：权限组、继承、前缀后缀、个人权限
- `PermissionListener`：登录应用权限，退出清理
- `PermissionCommand`：`/sfperm`（`/sfp`）命令
- 内置组：default / vip / mod / admin / owner
- 组继承链，权限自动传递
- 通过 `PermissionAttachment` 注入 Bukkit 权限
- 支持 `-` 前缀权限（否定权限）

**附魔系统（v2/feature/enchant/）**
- `SEnchantment` 抽象基类，继承即可注册自定义附魔
- `EnchantManager` 管理注册/附魔/移除/创建附魔书
- `EnchantAnvilListener` 铁砧附魔监听
- `EnchantAttributeListener` 属性加成自动应用
- `EnchantChestListener` 箱子战利品生成附魔书
- `EnchantTableListener` 附魔台获取自定义附魔
- `AncestralMightEnchant` 示例："祖宗之力"
- `SFEnchantCommand`：`/sfenchant`（`/sfe`）命令
- 使用 `PersistentDataContainer` 存储，不依赖原版附魔注册表

**物品系统（v2/feature/item/）**
- `SItem` 抽象基类，继承即可注册自定义物品
- `ItemManager` 管理注册/给予/消耗/查询
- `ItemListener` 交互监听（右键/左键/装备）
- `ItemChestListener` 箱子战利品生成自定义物品
- `MagicScepterItem` 示例："魔法权杖"
- `SFItemCommand`：`/sfitem`（`/sfi`）命令

#### 🛠️ 变更
- `SF.java` 新增 `tick()` / `chat()` / `world()` / `permission()` / `enchant()` / `item()` 入口方法
- `SFApi` 接口新增 `tick()` / `chat()` / `world()` / `permission()` 方法
- `plugin.yml` 新增 6 个命令和 6 个权限节点
- README.md 全面更新，新增第二阶段文档

---

### [1.0.0] - 2026-08-02

首个正式版本发布！

#### ✨ 新增

**核心架构**
- 建立 `server.sf.model.api.v2` 包结构，分离 v1 主入口与 v2 API
- 实现门面模式 `SF` 类，统一对外暴露所有功能
- 通过 Bukkit `ServicesManager` 注册 `SFApi` 接口供第三方插件接入

**子模块（v2/main/）**
- `SFLogger`：分级日志（info/warn/error），支持格式化参数
- `SFScheduler`：同步/异步/延迟/定时任务调度
- `SFPlayerOps`：玩家查找（按名/按 UUID）
- `SFCommandOps`：命令与事件注册，支持链式调用
- `SFServerOps`：广播、消息发送

**经济系统（v2/economy/）**
- 双后端支持：EssentialsX 优先，Vault 回退
- `EconomyBackend` 接口抽象，`EssentialsBackend` / `VaultBackend` 独立实现
- `EconomyOps` 高级操作：余额检查、负数校验、转账自动回滚
- 修复 BigDecimal 精度问题
- 支持 `hasAccount`、`give`、`take`、`set`、`transfer`、`format` 完整 API

**事件系统（v2/event/）**
- 12 个分类文件，覆盖 120+ Bukkit 事件
- 通用 `on()` 方法支持任意自定义事件
- 支持指定优先级和忽略已取消事件
- 单监听器异常隔离，不影响其他监听器

**数据库基础（v2/database/）**
- `Database` 接口 + `SQLiteDatabase` / `MySQLDatabase` 双实现
- SQLite 启用 WAL 模式提升并发性能
- `DatabaseManager` 全局管理，自动建表
- 支持表名前缀，便于多服务器共享数据库

**传送系统（v2/feature/teleport/）**
- `TeleportManager` 核心：冷却 / 延迟 / 防移动取消 / 跨世界
- `/spawn` `/setspawn`：出生点管理
- `/home` `/sethome` `/delhome` `/homes`：个人家（数据库持久化）
- `/warp` `/setwarp` `/delwarp` `/warps`：公共传送点
- `/back`：返回上次位置
- `/tp` `/tphere`：管理员传送

**TPA 系统（v2/feature/tpa/）**
- `/tpa` `/tpahere` `/tpaccept` `/tpdeny` `/tpcancel`
- 请求超时自动清理（默认 60 秒，可配置）
- 互斥机制：同时只能有一个请求

**管理员工具（v2/feature/admin/）**
- `/gm` `/fly` `/heal` `/feed` `/god` `/vanish`
- `/ec` `/wb` `/clear` `/speed` `/suicide`
- `AdminStateManager` 管理状态持久化
- god/vanish 状态在重登后保持

#### 🛠️ 配置
- `plugin.yml`：28+ 命令注册，完整权限节点，别名支持
- `config.yml`：数据库配置 / 传送冷却 / 传送延迟 / TPA 超时
- 支持 SQLite / MySQL 一键切换
- 配置热重载（`/servermanagement reload`）

#### ⚙️ 技术规格
- **Java 版本**：21+
- **API 版本**：Bukkit 1.21.5
- **构建工具**：Maven 3.9+
- **依赖**：Paper API, Vault（可选）, EssentialsX（可选）

---

## 🤝 贡献指南

感谢你对 ZeroCkate ServerManagementPlugin 项目的兴趣！

### 环境要求

- JDK 21+
- Maven 3.9+
- Git
- IDE（推荐 IntelliJ IDEA）

### 本地开发

```bash
# 1. Fork 仓库并克隆
git clone https://github.com/你的用户名/ZeroCkate_ServerManagementPlugin.git
cd ZeroCkate_ServerManagementPlugin

# 2. 添加上游远程
git remote add upstream https://github.com/zmb96/ZeroCkate_ServerManagementPlugin.git

# 3. 构建项目
mvn clean package

# 4. 将 target/ 下的 jar 文件放入测试服务器 plugins/ 目录测试
```

### 报告 Bug

1. 在 [Issues](https://github.com/zmb96/ZeroCkate_ServerManagementPlugin/issues) 搜索是否已有相同问题
2. 如果没有，创建新 Issue，包含以下信息：
   - **环境**：服务器类型（Paper/Spigot）、版本、Java 版本
   - **插件版本**：可在 `/plugins` 中查看
   - **复现步骤**：详细步骤
   - **预期行为**：你期望发生什么
   - **实际行为**：实际发生了什么
   - **完整日志**：相关堆栈跟踪

### 提交代码

1. **Fork** 本仓库
2. 基于最新 `main` 分支创建特性分支：
   ```bash
   git checkout main
   git pull upstream main
   git checkout -b feature/你的特性名
   ```
3. 编写代码，遵循以下规范：
   - 不写注释（项目作者偏好）
   - 一个文件只负责一个小块功能
   - 使用包结构组织代码
   - 命令类实现 `CommandExecutor` 和 `TabCompleter`
   - 监听器类实现 `Listener`
4. 本地测试通过：`mvn clean package`
5. 在测试服务器中验证功能正常
6. 提交修改并推送
7. 在 GitHub 上创建 **Pull Request** 到 `main` 分支

### 代码规范

**包结构**

```
server.sf.model.api.v2/
├── SF.java              # 门面类
├── SFApi.java           # API 接口
├── database/            # 数据库相关
├── economy/             # 经济系统
├── event/               # 事件系统
├── main/                # 核心工具
└── feature/             # 功能模块
    ├── teleport/        # 传送系统
    ├── tpa/             # TPA 系统
    ├── admin/           # 管理员工具
    ├── tick/            # SF Tick 系统 (100tick/秒)
    ├── world/           # 世界管理
    ├── chat/            # 聊天系统
    ├── permission/      # 权限系统
    ├── enchant/         # 附魔注册系统
    ├── item/            # 物品注册系统
    ├── text/            # SFText 文本组件 API
    └── perf/            # 性能优化系统
```

**命名约定**

| 类型 | 命名规则 | 示例 |
|------|----------|------|
| 类 | PascalCase | `TeleportManager` |
| 方法 | camelCase | `teleportNow()` |
| 常量 | UPPER_SNAKE | `DEFAULT_TIMEOUT` |
| 包 | 全小写 | `server.sf.model.api.v2.event` |
| 命令类 | XxxCommand | `HomeCommand` |
| 监听器 | XxxListener | `AdminListener` |
| 管理器 | XxxManager | `TpaManager` |

**Commit 规范**

| 前缀 | 用途 |
|------|------|
| `feat:` | 新功能 |
| `fix:` | Bug 修复 |
| `docs:` | 文档变更 |
| `refactor:` | 重构（不影响功能） |
| `perf:` | 性能优化 |
| `chore:` | 构建/工具变更 |

### 手动测试清单

提交 PR 前，请确保以下功能正常：

- [ ] `mvn clean package` 构建成功
- [ ] 插件能在 Paper 1.21.5 启动
- [ ] 新功能在游戏内测试通过
- [ ] 没有引入新的异常日志
- [ ] `/servermanagement reload` 仍然可用
- [ ] 卸载插件不报错

### 维护者

- **zmb96** - 项目创建者与主要维护者

---

## 📄 License

GNU General Public License v3.0 - 详见 [LICENSE](LICENSE)
