package dev.j3fftw.headlimiter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import dev.j3fftw.headlimiter.blocklimiter.Group;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.java.JavaPlugin;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.implementation.SlimefunItems;
import io.github.bakedlibs.dough.updater.GitHubBuildsUpdaterTR;

import dev.j3fftw.headlimiter.blocklimiter.BlockLimiter;
import org.mini2Dx.gettext.GetText;
import org.mini2Dx.gettext.PoFile;

public final class HeadLimiter extends JavaPlugin implements Listener {

    private static HeadLimiter instance;
    private BlockLimiter blockLimiter;


    @Override
    public void onEnable() {
        instance = this;
        if (!new File(getDataFolder(), "config.yml").exists()) {
            saveDefaultConfig();
        }

        GetText.setLocale(Locale.TRADITIONAL_CHINESE);
        InputStream inputStream = getClass().getResourceAsStream("/translations/zh_tw.po");
        if (inputStream == null) {
            getLogger().severe("錯誤！無法找到翻譯檔案，請回報給翻譯者。");
            getServer().getPluginManager().disablePlugin(this);
            return;
        } else {
            getLogger().info("載入繁體翻譯檔案...");
            try {
                PoFile poFile = new PoFile(Locale.TRADITIONAL_CHINESE, inputStream);
                GetText.add(poFile);
            } catch (ParseCancellationException | IOException e) {
                getLogger().severe("錯誤！讀取翻譯時發生錯誤，請回報給翻譯者：" + e.getMessage());
                getServer().getPluginManager().disablePlugin(this);
                return;
            }
        }

        Utils.loadPermissions();

        getServer().getPluginManager().registerEvents(this, this);

        getCommand("headlimiter").setExecutor(new CountCommand());

        // new MetricsService(this).start();

        if (getConfig().getBoolean("auto-update", true) && getDescription().getVersion().startsWith("Build_STCT - ")) {
            new GitHubBuildsUpdaterTR(this, getFile(), "SlimeTraditionalTranslation/HeadLimiter/master").start();
        }

        this.blockLimiter = new BlockLimiter(this);
        loadConfig();
    }

    @Override
    public void onDisable() {
        instance = null;
    }

    public boolean isCargo(SlimefunItem sfItem) {
        return sfItem.isItem(SlimefunItems.CARGO_INPUT_NODE)
            || sfItem.isItem(SlimefunItems.CARGO_OUTPUT_NODE)
            || sfItem.isItem(SlimefunItems.CARGO_OUTPUT_NODE_2)
            || sfItem.isItem(SlimefunItems.CARGO_CONNECTOR_NODE)
            || sfItem.isItem(SlimefunItems.CARGO_MANAGER);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlace(BlockPlaceEvent e) {
        final Player player = e.getPlayer();
        final Block block = e.getBlock();

        if (!e.isCancelled()
            && (block.getType() == Material.PLAYER_HEAD || block.getType() == Material.PLAYER_WALL_HEAD)
            && !Utils.canBypass(player)
        ) {
            final SlimefunItem sfItem = SlimefunItem.getByItem(e.getItemInHand());
            if (sfItem != null
                && isCargo(sfItem)
            ) {
                final int maxAmount = Utils.getMaxHeads(player);
                Utils.count(
                    block.getChunk(),
                    result -> Utils.onCheck(player, block, maxAmount, result.getTotal(), sfItem)
                );
            }
        }
    }

    public BlockLimiter getBlockLimiter() {
        return blockLimiter;
    }

    public static HeadLimiter getInstance() {
        return instance;
    }

    public void loadConfig() {
        ConfigurationSection configurationSection = instance.getConfig().getConfigurationSection("block-limits");
        if (configurationSection == null) {
            throw new IllegalStateException("No configuration for groups is available.");
        }
        for (String key : configurationSection.getKeys(false)) {
            BlockLimiter.getInstance().getGroups().add(new Group(configurationSection.getConfigurationSection(key)));
        }
    }
}
