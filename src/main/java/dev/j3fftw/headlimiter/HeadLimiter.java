package dev.j3fftw.headlimiter;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.implementation.SlimefunItems;
//import io.github.thebusybiscuit.slimefun4.libraries.dough.updater.GitHubBuildsUpdater;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
//import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public final class HeadLimiter extends JavaPlugin implements Listener {

    private static HeadLimiter instance;

    private final ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("HeadLimiter-pool-%d").build();

    public final ExecutorService executorService = Executors.newFixedThreadPool(
        this.getConfig().getInt("thread-pool-size", 4), threadFactory
    );

    @Override
    public void onEnable() {
        instance = this;
        if (!new File(getDataFolder(), "config.yml").exists())
            saveDefaultConfig();

        getServer().getPluginManager().registerEvents(this, this);

        getCommand("headlimiter").setExecutor(new CountCommand());

        //new Metrics(this, 9968);

        /*if (getConfig().getBoolean("auto-update", true) && getDescription().getVersion().startsWith("DEV - ")) {
            new GitHubBuildsUpdater(this, getFile(), "J3fftw1/HeadLimiter/master").start();
        }*/
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

    public void onCheck(int amount, Block block, BlockPlaceEvent e, int i, SlimefunItem sfItem) {
        if (i > amount) {
            Bukkit.getScheduler().runTask(this, () -> {
                if (block.getType() != Material.AIR) {
                    block.setType(Material.AIR);
                    if (e.getPlayer().getGameMode() != GameMode.CREATIVE) {
                        block.getWorld().dropItemNaturally(block.getLocation(), sfItem.getItem());
                    }
                }
            });
            BlockStorage.clearBlockInfo(block.getLocation());
            e.getPlayer().sendMessage(ChatColor.RED + "你已達到此區塊的最高物流放置數量");
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlace(BlockPlaceEvent e) {
        final SlimefunItem sfItem = SlimefunItem.getByItem(e.getItemInHand());
        if (!e.isCancelled()
            && (e.getBlock().getType() == Material.PLAYER_HEAD || e.getBlock().getType() == Material.PLAYER_WALL_HEAD)
            && sfItem != null && isCargo(sfItem)
        ) {
            final Block block = e.getBlock();
            final BlockState[] te = block.getChunk().getTileEntities();
            executorService.submit(() -> {
                int i = 0;
                for (BlockState bs : te) {
                    final SlimefunItem slimefunItem = BlockStorage.check(bs.getLocation());
                    if (slimefunItem != null && isCargo(slimefunItem))
                        i++;
                }
                if (this.getConfig().getBoolean("permissions", false)) {
                    for (String perm : this.getConfig().getConfigurationSection("permission").getKeys(false)) {
                        if (e.getPlayer().hasPermission("headlimiter.permission." + perm)) {
                            onCheck(this.getConfig().getInt("permission." + perm), block, e, i, sfItem);
                            break;
                        }
                    }
                } else {
                    onCheck(this.getConfig().getInt("amount"), block, e, i, sfItem);
                }
            });
        }
    }

    public static HeadLimiter getInstance() {
        return instance;
    }
}
