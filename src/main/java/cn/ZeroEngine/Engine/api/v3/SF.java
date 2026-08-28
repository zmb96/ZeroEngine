package cn.ZeroEngine.Engine.api.v3;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import cn.ZeroEngine.Engine.api.v3.database.Database;
import cn.ZeroEngine.Engine.api.v3.database.DatabaseManager;
import cn.ZeroEngine.Engine.api.v3.economy.SFEconomy;
import cn.ZeroEngine.Engine.api.v3.event.SFEvents;
import cn.ZeroEngine.Engine.api.v3.feature.engine.BlockControl;
import cn.ZeroEngine.Engine.api.v3.feature.engine.DamageSystem;
import cn.ZeroEngine.Engine.api.v3.feature.engine.MonsterAttribute;
import cn.ZeroEngine.Engine.api.v3.feature.engine.ResourcePackManager;
import cn.ZeroEngine.Engine.api.v3.feature.engine.SpawnControl;
import cn.ZeroEngine.Engine.api.v3.feature.gui.GUIManager;
import cn.ZeroEngine.Engine.api.v3.feature.gui.impl.GUIManagerImpl;
import cn.ZeroEngine.Engine.api.v3.feature.enchant.EnchantAttributeListener;
import cn.ZeroEngine.Engine.api.v3.feature.enchant.EnchantManager;
import cn.ZeroEngine.Engine.api.v3.feature.enchant.SEnchantment;
import cn.ZeroEngine.Engine.api.v3.feature.enchant.SFAttr;
import cn.ZeroEngine.Engine.api.v3.feature.gameplay.bedwars.Bedwars;
import cn.ZeroEngine.Engine.api.v3.feature.gameplay.bedwars.impl.BedwarsImpl;
import cn.ZeroEngine.Engine.api.v3.feature.gameplay.horde.Horde;
import cn.ZeroEngine.Engine.api.v3.feature.gameplay.horde.impl.HordeImpl;
import cn.ZeroEngine.Engine.api.v3.feature.gameplay.pvp.PvPArena;
import cn.ZeroEngine.Engine.api.v3.feature.gameplay.pvp.impl.PvPArenaImpl;
import cn.ZeroEngine.Engine.api.v3.feature.gameplay.village.VillageDefense;
import cn.ZeroEngine.Engine.api.v3.feature.gameplay.village.impl.VillageDefenseImpl;
import cn.ZeroEngine.Engine.api.v3.feature.item.ItemListener;
import cn.ZeroEngine.Engine.api.v3.feature.item.ItemManager;
import cn.ZeroEngine.Engine.api.v3.feature.item.SItem;
import cn.ZeroEngine.Engine.api.v3.feature.entity.EntityManager;
import cn.ZeroEngine.Engine.api.v3.feature.entity.EntityListener;
import cn.ZeroEngine.Engine.api.v3.feature.entity.SEntity;
import cn.ZeroEngine.Engine.api.v3.feature.recipe.RecipeManager;
import cn.ZeroEngine.Engine.api.v3.feature.recipe.SRecipe;
import cn.ZeroEngine.Engine.api.v3.feature.block.BlockManager;
import cn.ZeroEngine.Engine.api.v3.feature.block.BlockListener;
import cn.ZeroEngine.Engine.api.v3.feature.block.SFBlockCommand;
import cn.ZeroEngine.Engine.api.v3.feature.screen.ScreenManager;
import cn.ZeroEngine.Engine.api.v3.feature.screen.SScreen;
import cn.ZeroEngine.Engine.api.v3.feature.teleport.TeleportManager;
import cn.ZeroEngine.Engine.api.v3.feature.tick.TickManager;
import cn.ZeroEngine.Engine.api.v3.feature.chat.ChatManager;
import cn.ZeroEngine.Engine.api.v3.feature.world.WorldManager;
import cn.ZeroEngine.Engine.api.v3.feature.permission.PermissionManager;
import cn.ZeroEngine.Engine.api.v3.feature.main.ReachManager;
import cn.ZeroEngine.Engine.api.v3.feature.perf.PerformanceManager;
import cn.ZeroEngine.Engine.api.v3.main.SFCommandOps;
import cn.ZeroEngine.Engine.api.v3.main.SFLogger;
import cn.ZeroEngine.Engine.api.v3.main.SFPlayerOps;
import cn.ZeroEngine.Engine.api.v3.main.SFScheduler;
import cn.ZeroEngine.Engine.api.v3.main.SFServerOps;

import java.util.UUID;

public final class SF implements SFApi {

    private static SF instance;

    private final JavaPlugin plugin;
    private final SFEconomy economy;
    private final SFEvents events;
    private final SFLogger logger;
    private final SFScheduler scheduler;
    private final SFPlayerOps players;
    private final SFCommandOps commands;
    private final SFServerOps serverOps;
    private final TickManager tickManager;
    private TeleportManager teleportManager;
    private EnchantManager enchantManager;
    private EnchantAttributeListener enchantAttrListener;
    private ItemManager itemManager;
    private ItemListener itemListener;
    private EntityManager entityManager;
    private EntityListener entityListener;
    private RecipeManager recipeManager;
    private BlockManager blockManager;
    private BlockListener blockListener;
    private ScreenManager screenManager;
    private cn.ZeroEngine.Engine.api.v3.feature.biome.BiomeManager biomeManager;
    private cn.ZeroEngine.Engine.api.v3.feature.biome.BiomeListener biomeListener;
    private ChatManager chatManager;
    private WorldManager worldManager;
    private PermissionManager permissionManager;
    private ReachManager reachManager;
    private PerformanceManager perfManager;
    private MonsterAttribute monsterAttr;
    private DamageSystem damageSys;
    private BlockControl blockCtrl;
    private SpawnControl spawnCtrl;
    private ResourcePackManager resourcePackMgr;
    private GUIManager guiMgr;
    private Bedwars bedwarsMgr;
    private BedwarsImpl bedwarsImpl;
    private PvPArena pvpArena;
    private PvPArenaImpl pvpImpl;
    private Horde hordeMgr;
    private HordeImpl hordeImpl;
    private VillageDefense villageDef;
    private VillageDefenseImpl villageDefImpl;

    private SF(JavaPlugin plugin) {
        this.plugin = plugin;
        this.economy = new SFEconomy(plugin);
        this.events = new SFEvents(plugin);
        this.logger = new SFLogger(plugin);
        this.scheduler = new SFScheduler(plugin);
        this.players = new SFPlayerOps();
        this.commands = new SFCommandOps(plugin);
        this.serverOps = new SFServerOps();
        this.tickManager = new TickManager(plugin);
        this.tickManager.start();
        DatabaseManager.init(plugin);
    }

    public static void init(JavaPlugin plugin) {
        if (instance != null) {
            // 允许多个插件重复 SF.init(theirPlugin) — 但始终用第一次调用时的 plugin 作为上下文。
            // 原因：
            //   如果 zmb96 作为第三方插件先 onEnable 并 SF.init(zmb96)，
            //   而 ZeroEngine (v1 main) 稍后 onEnable，我们也不应该用 IllegalStateException 打断 ZeroEngine 的 onEnable。
            //   同时 SFCommandOps.regCommand 会自动 fallback 到 SimpleCommandMap 里找其他插件（ZeroEngine）
            //   声明的命令去覆盖执行器，所以"plugin=zmb96"也不会让命令注册失效。
            plugin.getLogger().info("[SF] already initialized by " + instance.plugin.getName()
                    + "; ignoring SF.init(" + plugin.getName() + ")");
            return;
        }
        instance = new SF(plugin);
        plugin.getServer().getServicesManager().register(SFApi.class, instance, plugin, org.bukkit.plugin.ServicePriority.Normal);
        plugin.getServer().getServicesManager().register(SF.class, instance, plugin, org.bukkit.plugin.ServicePriority.Normal);
    }

    public static void shutdown() {
        if (instance != null) {
            if (instance.enchantAttrListener != null) instance.enchantAttrListener.shutdown();
            if (instance.enchantManager != null) instance.enchantManager.unregisterAll();
            if (instance.itemListener != null) instance.itemListener.shutdown();
            if (instance.itemManager != null) instance.itemManager.unregisterAll();
            if (instance.entityListener != null) instance.entityListener.shutdown();
            if (instance.entityManager != null) instance.entityManager.unregisterAll();
            if (instance.recipeManager != null) instance.recipeManager.unregisterAll();
            if (instance.blockListener != null) instance.blockListener.shutdown();
            if (instance.blockManager != null) instance.blockManager.shutdown();
            if (instance.screenManager != null) instance.screenManager.shutdown();
            if (instance.biomeListener != null) instance.biomeListener.shutdown();
            if (instance.biomeManager != null) instance.biomeManager.unregisterAll();
            if (instance.perfManager != null) instance.perfManager.shutdown();
            if (instance.bedwarsImpl != null) instance.bedwarsImpl.shutdown();
            if (instance.pvpImpl != null) instance.pvpImpl.shutdown();
            if (instance.hordeImpl != null) instance.hordeImpl.shutdown();
            if (instance.villageDefImpl != null) instance.villageDefImpl.shutdown();
            if (instance.guiMgr != null) ((GUIManagerImpl) instance.guiMgr).closeAll();
            instance.events.unregisterAll();
            instance.tickManager.shutdown();
            DatabaseManager.shutdown();
            instance.plugin.getServer().getServicesManager().unregister(instance);
        }
        instance = null;
    }

    public static SF sf() {
        if (instance == null) {
            throw new IllegalStateException("SF not initialized. Call SF.init(plugin) in onEnable().");
        }
        return instance;
    }

    public void setTeleportManager(TeleportManager tp) {
        this.teleportManager = tp;
    }

    public TeleportManager teleport() {
        return teleportManager;
    }

    public EnchantManager enchant() {
        if (enchantManager == null) {
            SEnchantment.init(plugin);
            enchantManager = new EnchantManager();
            enchantAttrListener = new EnchantAttributeListener(enchantManager);
            regEvent(new cn.ZeroEngine.Engine.api.v3.feature.enchant.EnchantAnvilListener(enchantManager), plugin);
            regEvent(enchantAttrListener, plugin);
            regEvent(new cn.ZeroEngine.Engine.api.v3.feature.enchant.EnchantChestListener(enchantManager), plugin);
            regEvent(new cn.ZeroEngine.Engine.api.v3.feature.enchant.EnchantTableListener(enchantManager), plugin);
            enchantAttrListener.startTick(this, 40L);
            enchantManager.registerIfAbsent(new cn.ZeroEngine.Engine.api.v3.feature.enchant.LifestealEnchant());
            enchantManager.registerIfAbsent(new cn.ZeroEngine.Engine.api.v3.feature.enchant.AncestralMightEnchant());
            // 绑定 /sfenchant 命令。
            // SFCommandOps.regCommand 已自动处理 fallback：
            //   - 如果 SF.init 先被 ZeroEngine 调 → plugin.getCommand("sfenchant") 直接命中 plugin.yml
            //   - 如果 SF.init 先被第三方插件调（zmb96... 等）→ 反射 SimpleCommandMap 找到 ZeroEngine 声明的
            //     sfenchant PluginCommand 并覆盖执行器为 v3 的命令
            regCommand("sfenchant", new cn.ZeroEngine.Engine.api.v3.feature.enchant.SFEnchantCommand(enchantManager));
            SF sf = SF.sf();
            sf.info("[Enchant] System initialized (v3 /sfenchant command ready; " + enchantManager.all().size() + " enchants loaded)");
        }
        return enchantManager;
    }

    public ItemManager item() {
        if (itemManager == null) {
            SItem.init(plugin);
            itemManager = new ItemManager();
            itemListener = new ItemListener(itemManager);
            regEvent(itemListener, plugin);
            regEvent(new cn.ZeroEngine.Engine.api.v3.feature.item.ItemChestListener(itemManager), plugin);
            itemManager.registerIfAbsent(new cn.ZeroEngine.Engine.api.v3.feature.item.MagicScepterItem());
            regCommand("sfitem", new cn.ZeroEngine.Engine.api.v3.feature.item.SFItemCommand(itemManager));
            SF sf = SF.sf();
            sf.info("[Item] System initialized (v3 /sfitem command ready; " + itemManager.all().size() + " items loaded)");
        }
        return itemManager;
    }

    public EntityManager entities() {
        if (entityManager == null) {
            SEntity.init(plugin);
            entityManager = new EntityManager();
            entityListener = new EntityListener(entityManager);
            regEvent(entityListener, plugin);
            entityListener.startTick(plugin, this);
            regCommand("sfentity", new cn.ZeroEngine.Engine.api.v3.feature.entity.SFEntityCommand(entityManager));
            SF sf = SF.sf();
            sf.info("[Entity] Custom mob system initialized");
        }
        return entityManager;
    }

    public RecipeManager recipes() {
        if (recipeManager == null) {
            SRecipe.init(plugin);
            recipeManager = new RecipeManager();
            recipeManager.setItemManager(item());
            regEvent(new cn.ZeroEngine.Engine.api.v3.feature.recipe.AdvancedCraftTableListener(recipeManager), plugin);
            regCommand("sfrecipe", new cn.ZeroEngine.Engine.api.v3.feature.recipe.SFRecipeCommand(recipeManager));
            SF sf = SF.sf();
            sf.info("[Recipe] Custom recipe system initialized (Advanced Craft Table ready)");
        }
        return recipeManager;
    }

    public BlockManager blocks() {
        if (blockManager == null) {
            blockManager = new BlockManager(plugin, item());
            blockListener = new BlockListener(plugin, blockManager);
            regEvent(blockListener, plugin);
            regCommand("sfblock", new SFBlockCommand(blockManager));
            SF sf = SF.sf();
            sf.info("[Block] Custom block system initialized (v3 /sfblock command ready; " + blockManager.all().size() + " blocks loaded)");
        }
        return blockManager;
    }

    public ScreenManager screens() {
        if (screenManager == null) {
            screenManager = new ScreenManager(plugin);
            regEvent(screenManager, plugin);
            SF sf = SF.sf();
            sf.info("[Screen] Custom screen system initialized (v3 Dialog API, blocks join until accepted; " + screenManager.all().size() + " screens loaded)");
        }
        return screenManager;
    }

    public cn.ZeroEngine.Engine.api.v3.feature.biome.BiomeManager biomes() {
        if (biomeManager == null) {
            biomeManager = new cn.ZeroEngine.Engine.api.v3.feature.biome.BiomeManager(this);
            biomeListener = new cn.ZeroEngine.Engine.api.v3.feature.biome.BiomeListener(plugin, this, biomeManager);
            regEvent(biomeListener, plugin);
            biomeListener.startTick(plugin, this);
            SF sf = SF.sf();
            sf.info("[Biome] Custom biome system initialized (ChunkPopulateEvent hook ready)");
        }
        return biomeManager;
    }

    public ChatManager chat() {
        if (chatManager == null) {
            chatManager = new ChatManager(tickManager);
            regEvent(new cn.ZeroEngine.Engine.api.v3.feature.chat.ChatListener(chatManager), plugin);
            SF sf = SF.sf();
            sf.info("[Chat] System initialized");
        }
        return chatManager;
    }

    @Override
    public boolean isPluginListenerChat(Player player) {
        return chatManager != null && chatManager.isPluginListening(player);
    }

    @Override
    public boolean isPluginListenerChat(UUID playerId) {
        return chatManager != null && chatManager.isPluginListening(playerId);
    }

    public WorldManager world() {
        if (worldManager == null) {
            worldManager = new WorldManager();
            SF sf = SF.sf();
            sf.info("[World] System initialized");
        }
        return worldManager;
    }

    public PermissionManager permission() {
        if (permissionManager == null) {
            permissionManager = new PermissionManager();
            permissionManager.initDefaults();
            regEvent(new cn.ZeroEngine.Engine.api.v3.feature.permission.PermissionListener(permissionManager), plugin);
            SF sf = SF.sf();
            sf.info("[Permission] System initialized");
        }
        return permissionManager;
    }

    public ReachManager reach() {
        if (reachManager == null) {
            reachManager = new ReachManager();
            SF sf = SF.sf();
            sf.info("[Reach] System initialized");
        }
        return reachManager;
    }

    public PerformanceManager perf() {
        if (perfManager == null) {
            perfManager = new PerformanceManager(plugin);
            perfManager.start();
            regEvent(new cn.ZeroEngine.Engine.api.v3.feature.perf.PerformanceListener(perfManager), plugin);
            SF sf = SF.sf();
            sf.info("[Perf] System initialized");
        }
        return perfManager;
    }

    @Override
    public SFAttr attr() {
        SFAttr.ensureLoaded();
        return SFAttr.INSTANCE;
    }

    @Override
    public Database database() {
        return DatabaseManager.db();
    }

    @Override
    public MonsterAttribute monster() {
        if (monsterAttr == null) {
            monsterAttr = new cn.ZeroEngine.Engine.api.v3.feature.engine.impl.MonsterAttributeImpl(plugin);
            SF sf = SF.sf();
            sf.info("[Engine] MonsterAttribute initialized");
        }
        return monsterAttr;
    }

    @Override
    public DamageSystem damage() {
        if (damageSys == null) {
            damageSys = new cn.ZeroEngine.Engine.api.v3.feature.engine.impl.DamageSystemImpl(plugin);
            SF sf = SF.sf();
            sf.info("[Engine] DamageSystem initialized");
        }
        return damageSys;
    }

    @Override
    public BlockControl block() {
        if (blockCtrl == null) {
            blockCtrl = new cn.ZeroEngine.Engine.api.v3.feature.engine.impl.BlockControlImpl(plugin);
            SF sf = SF.sf();
            sf.info("[Engine] BlockControl initialized");
        }
        return blockCtrl;
    }

    @Override
    public SpawnControl spawn() {
        if (spawnCtrl == null) {
            spawnCtrl = new cn.ZeroEngine.Engine.api.v3.feature.engine.impl.SpawnControlImpl(plugin);
            SF sf = SF.sf();
            sf.info("[Engine] SpawnControl initialized");
        }
        return spawnCtrl;
    }

    @Override
    public ResourcePackManager resourcePack() {
        if (resourcePackMgr == null) {
            resourcePackMgr = new cn.ZeroEngine.Engine.api.v3.feature.engine.impl.ResourcePackManagerImpl(plugin);
            SF sf = SF.sf();
            sf.info("[Engine] ResourcePackManager initialized");
        }
        return resourcePackMgr;
    }

    @Override
    public GUIManager gui() {
        if (guiMgr == null) {
            guiMgr = new GUIManagerImpl(plugin);
            SF sf = SF.sf();
            sf.info("[Engine] GUIManager initialized");
        }
        return guiMgr;
    }

    @Override
    public Bedwars bedwars() {
        if (bedwarsMgr == null) {
            bedwarsImpl = new BedwarsImpl(plugin);
            bedwarsMgr = bedwarsImpl;
            regEvent(bedwarsImpl, plugin);
            bedwarsImpl.start();
            SF sf = SF.sf();
            sf.info("[Gameplay] Bedwars initialized");
        }
        return bedwarsMgr;
    }

    @Override
    public PvPArena pvp() {
        if (pvpArena == null) {
            pvpImpl = new PvPArenaImpl(plugin);
            pvpArena = pvpImpl;
            regEvent(pvpImpl, plugin);
            pvpImpl.start();
            SF sf = SF.sf();
            sf.info("[Gameplay] PvPArena initialized");
        }
        return pvpArena;
    }

    @Override
    public Horde horde() {
        if (hordeMgr == null) {
            hordeImpl = new HordeImpl(plugin);
            hordeMgr = hordeImpl;
            regEvent(hordeImpl, plugin);
            hordeImpl.start();
            SF sf = SF.sf();
            sf.info("[Gameplay] Horde initialized");
        }
        return hordeMgr;
    }

    @Override
    public VillageDefense villageDefense() {
        if (villageDef == null) {
            villageDefImpl = new VillageDefenseImpl(plugin);
            villageDef = villageDefImpl;
            regEvent(villageDefImpl, plugin);
            villageDefImpl.start();
            SF sf = SF.sf();
            sf.info("[Gameplay] VillageDefense initialized");
        }
        return villageDef;
    }

    @Override
    public SFLogger logger() {
        return logger;
    }

    @Override
    public SFEconomy economy() {
        return economy;
    }

    public SFEconomy eco() {
        return economy;
    }

    @Override
    public SFEvents events() {
        return events;
    }

    @Override
    public SFScheduler scheduler() {
        return scheduler;
    }

    @Override
    public SFPlayerOps players() {
        return players;
    }

    @Override
    public SFServerOps server() {
        return serverOps;
    }

    public SFCommandOps commands() {
        return commands;
    }

    @Override
    public TickManager tick() {
        return tickManager;
    }

    public JavaPlugin plugin() {
        return plugin;
    }

    public Server bukkit() {
        return plugin.getServer();
    }

    @Override
    public void info(String msg) {
        logger.info(msg);
    }

    @Override
    public void info(String fmt, Object... args) {
        logger.info(fmt, args);
    }

    @Override
    public void warn(String msg) {
        logger.warn(msg);
    }

    @Override
    public void warn(String fmt, Object... args) {
        logger.warn(fmt, args);
    }

    @Override
    public void error(String msg) {
        logger.error(msg);
    }

    @Override
    public void error(String msg, Throwable t) {
        logger.error(msg, t);
    }

    @Override
    public void error(String fmt, Object... args) {
        logger.error(fmt, args);
    }

    @Override
    public void broadcast(String msg) {
        serverOps.broadcast(msg);
    }

    @Override
    public void broadcast(String perm, String msg) {
        serverOps.broadcast(perm, msg);
    }

    @Override
    public void msg(CommandSender sender, String msg) {
        serverOps.msg(sender, msg);
    }

    @Override
    public Player player(String name) {
        return players.byName(name);
    }

    @Override
    public Player player(UUID id) {
        return players.byId(id);
    }

    @Override
    public boolean giveMoney(OfflinePlayer p, double amount) {
        return economy.give(p, amount);
    }

    @Override
    public boolean takeMoney(OfflinePlayer p, double amount) {
        return economy.take(p, amount);
    }

    @Override
    public boolean setMoney(OfflinePlayer p, double amount) {
        return economy.set(p, amount);
    }

    @Override
    public double balance(OfflinePlayer p) {
        return economy.balance(p);
    }

    @Override
    public boolean transferMoney(OfflinePlayer from, OfflinePlayer to, double amount) {
        return economy.transfer(from, to, amount);
    }

    @Override
    public String formatMoney(double amount) {
        return economy.format(amount);
    }

    @Override
    public boolean teleport(Player p, Location loc) {
        if (teleportManager != null) {
            return teleportManager.teleportNow(p, loc, "api");
        }
        return p.teleport(loc);
    }

    @Override
    public void run(Runnable r) {
        scheduler.run(r);
    }

    @Override
    public void runAsync(Runnable r) {
        scheduler.runAsync(r);
    }

    @Override
    public void runLater(Runnable r, long ticks) {
        scheduler.runLater(r, ticks);
    }

    @Override
    public void runTimer(Runnable r, long delay, long period) {
        scheduler.runTimer(r, delay, period);
    }

    @Override
    public void console(String cmd) {
        commands.console(cmd);
    }

    public SF regEvent(Listener listener, JavaPlugin p) {
        commands.regEvent(listener, p);
        return this;
    }

    public SF regEvent(Listener listener) {
        commands.regEvent(listener);
        return this;
    }

    public SF regCommand(String name, CommandExecutor executor) {
        commands.regCommand(name, executor);
        return this;
    }
}
