# ZeroEngine 成就系统（基于原版 Advancement）实现计划

## Context

用户希望为 ZeroEngine 添加自定义成就功能，选择使用 Minecraft 原版 Advancement 系统而非自定义 GUI。优势：无需数据库持久化（原版自动处理）、无需自定义 GUI（玩家按 F 键查看）、无需事件系统（原版已有 PlayerAdvancementDoneEvent）。

核心思路：ZeroEngine 根据附属插件注册的 SAchievement 定义，自动生成 datapack JSON 文件到世界目录，然后通过 Bukkit Advancement API 程序化授予/撤销成就。使用 `minecraft:impossible` 触发器确保只能通过 API 手动授予。

## 新增文件（5个，均在 feature/achievement/ 包下）

### 1. `SAchievement.java` — 抽象契约类
```java
public abstract class SAchievement {
    private static Plugin plugin;
    public static void init(Plugin p) { plugin = p; }

    public abstract String id();           // 成就ID，如 "first_uranium"
    public abstract String title();        // 标题（可带&颜色码）
    public abstract String description();  // 描述
    public abstract org.bukkit.Material icon(); // 图标

    public String namespace() { return "sf"; }     // 命名空间，附属覆盖为自己的id
    public String parent() { return null; }         // 父成就 fullId（用于成就树）
    public Frame frame() { return Frame.TASK; }     // TASK/GOAL/CHALLENGE
    public boolean hidden() { return false; }       // 是否隐藏直到解锁
    public boolean showToast() { return true; }     // 解锁时显示弹窗
    public boolean announceToChat() { return true; } // 解锁时全服公告
    public String criteriaName() { return "trigger"; } // criteria名称
    public void onGrant(Player p) {}                // 授予时奖励回调（发物品/金钱/命令）

    // 工具方法
    public void giveMoney(Player p, double amount) { SF.sf().giveMoney(p, amount); }
    public void giveItem(Player p, String itemId, int amount) { SF.sf().item().give(p, itemId, amount); }
    public void runCommand(String cmd) { SF.sf().console(cmd); }

    public enum Frame { TASK, GOAL, CHALLENGE }
}
```

### 2. `AchievementManager.java` — 注册+授予管理
```java
public class AchievementManager {
    private final Map<String, SAchievement> registry = new HashMap<>();

    // 注册（同 ItemManager 模式）
    public boolean registerIfAbsent(SAchievement a);
    public void unregisterAll();
    public SAchievement get(String fullId);  // "namespace:id"
    public Collection<SAchievement> all();

    // 授予/撤销（调用 Bukkit API）
    public boolean grant(Player p, String fullId);
    //   NamespacedKey key = new NamespacedKey(plugin, a.namespace() + "/" + a.id());
    //   Advancement adv = Bukkit.getAdvancement(key);
    //   if (adv == null) return false;
    //   AdvancementProgress progress = p.getAdvancement(adv);
    //   if (progress.isDone()) return false;
    //   progress.awardCriteria(a.criteriaName());

    public boolean revoke(Player p, String fullId);
    public boolean isGranted(Player p, String fullId);

    // 生成 datapack JSON
    public void generateDataPack();
    //   写入到 <world>/datapacks/sf_advancements/data/<namespace>/advancements/<id>.json
    //   JSON格式见下方

    public void shutdown();
}
```

### 3. `AchievementListener.java` — 奖励触发
```java
public class AchievementListener implements Listener {
    @EventHandler
    public void onAdvancementDone(PlayerAdvancementDoneEvent e) {
        // 从 e.getAdvancement().getKey() 提取 namespace:id
        // 查找对应的 SAchievement，调用 onGrant(e.getPlayer())
    }
}
```

### 4. `AchievementGenerator.java` — JSON 生成工具
```java
// 生成 advancement JSON 文件
// 路径: <world>/datapacks/sf_advancements/data/<namespace>/advancements/<id>.json
// 格式:
{
  "display": {
    "icon": { "id": "<material_lowercase>" },
    "title": "<title>",
    "description": "<description>",
    "frame": "task",
    "show_toast": true,
    "announce_to_chat": true,
    "hidden": false
  },
  "parent": "<namespace>:<parent_id>",   // 可选
  "criteria": {
    "trigger": {
      "trigger": "minecraft:impossible"
    }
  }
}
```
使用 `minecraft:impossible` 触发器——该触发器永远无法自动完成，只能通过 `awardCriteria()` 手动授予。同时生成 `pack.mcmeta` 文件标记 datapack。

### 5. `SFAchievementCommand.java` — `/sfadv` 命令
```
/sfadv              → 列出所有已注册成就
/sfadv grant <player> <fullId>   → 管理员授予（权限 sf.admin）
/sfadv revoke <player> <fullId>  → 管理员撤销
/sfadv list         → 列出所有成就及解锁状态
/sfadv gen          → 重新生成 datapack（需重启生效）
```

## 修改文件（3个）

### 1. `SFApi.java`（第81行 `VillageDefense villageDefense();` 后）
```java
import cn.ZeroEngine.Engine.api.v3.feature.achievement.AchievementManager;
// ...
AchievementManager achievements();
```

### 2. `SF.java`（三处修改）
- **字段**（第112行后）：`private AchievementManager achievementManager;`
- **lazy init**（约第485行后）：
```java
@Override
public AchievementManager achievements() {
    if (achievementManager == null) {
        SAchievement.init(plugin);
        achievementManager = new AchievementManager();
        regEvent(new AchievementListener(achievementManager), plugin);
        achievementManager.generateDataPack();  // 启动时生成JSON
        regCommand("sfadv", new SFAchievementCommand(achievementManager));
        sf.info("[Achievement] Advancement system initialized (/sfadv ready, " + achievementManager.all().size() + " achievements)");
    }
    return achievementManager;
}
```
- **shutdown()**（第167行后）：`if (instance.achievementManager != null) instance.achievementManager.shutdown();`

### 3. `SFAddonsCommand.java`
- `handleUnload()`（第80行后）：`am.unregisterAll();`
- `handleStatus()`（第109行后）：`成就: x`

## 关键设计点

| 设计点 | 选择 | 理由 |
|---|---|---|
| 展示方式 | 原版 advancement GUI（F键） | 无需自定义GUI，玩家熟悉 |
| 触发器 | `minecraft:impossible` | 只能通过API手动授予，不会被自动触发 |
| 持久化 | 原版自动处理（player data） | 无需数据库表，无需缓存 |
| 奖励触发 | `PlayerAdvancementDoneEvent` | 原版事件，已有v3绑定入口 |
| JSON生成 | onEnable时写入datapack | 首次安装需重启服务器才能加载datapack |
| 命名空间 | `namespace:id` 复合ID | 避免多附属冲突 |
| NamespacedKey | `new NamespacedKey(plugin, ns + "/" + id)` | Bukkit要求key格式 |

## 限制说明
- **首次安装/新增成就需重启服务器**：datapack 在服务器启动时加载，运行时新增的 JSON 需重启才生效
- **成就ID格式**：必须为 NamespacedKey 兼容格式（小写字母+数字+下划线）
- **不能热重载**：`/sfaddons reload` 后注册的新成就要等下次重启

## 附属插件使用示例
```java
// 注册
SF.sf().achievements().registerIfAbsent(new SAchievement() {
    @Override public String id() { return "first_uranium"; }
    @Override public String namespace() { return "zerotech"; }
    @Override public String title() { return "&6首块铀矿"; }
    @Override public String description() { return "&7获得第一块铀矿石"; }
    @Override public Material icon() { return Material.RAW_IRON; }
    @Override public Frame frame() { return Frame.GOAL; }
    @Override public void onGrant(Player p) {
        giveMoney(p, 500);
        runCommand("say " + p.getName() + " 达成了【首块铀矿】！");
    }
});

// 触发（业务逻辑中）
SF.sf().achievements().grant(player, "zerotech:first_uranium");
```

## 实现步骤

1. 创建 `feature/achievement/` 包
2. 编写 `SAchievement.java` 抽象类
3. 编写 `AchievementGenerator.java`（JSON 生成工具）
4. 编写 `AchievementManager.java`（注册 + grant/revoke + generateDataPack）
5. 编写 `AchievementListener.java`（监听 PlayerAdvancementDoneEvent 触发奖励）
6. 编写 `SFAchievementCommand.java`（/sfadv 命令）
7. 修改 `SFApi.java` 添加接口声明
8. 修改 `SF.java` 添加字段 + lazy init + shutdown
9. 修改 `SFAddonsCommand.java` 添加 unload + status
10. `mvn clean package` 编译验证

## 验证方式
- `mvn clean package` 编译通过
- 启动服务器后检查 `<world>/datapacks/sf_advancements/` 是否生成 JSON 文件
- `/sfadv list` 显示已注册成就
- `/sfadv grant <player> <id>` 授予后玩家收到弹窗
- 按 F 键在原版成就界面看到成就树
- 重启后已授予的成就仍然存在（原版持久化）
