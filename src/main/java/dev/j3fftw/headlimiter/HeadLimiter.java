package dev.j3fftw.headlimiter;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import dev.j3fftw.headlimiter.blocklimiter.Group;
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
import io.github.thebusybiscuit.slimefun4.libraries.dough.updater.GitHubBuildsUpdater;

import dev.j3fftw.headlimiter.blocklimiter.BlockLimiter;

public final class HeadLimiter extends JavaPlugin implements Listener {

    private static HeadLimiter instance;
    private BlockLimiter blockLimiter;
    private final HashSet<Group> groups = new HashSet<>();

    @Override
    public void onEnable() {
        instance = this;
        if (!new File(getDataFolder(), "config.yml").exists()) {
            saveDefaultConfig();
        }

        Utils.loadPermissions();

        getServer().getPluginManager().registerEvents(this, this);

        getCommand("headlimiter").setExecutor(new CountCommand());

        new MetricsService(this).start();

        if (getConfig().getBoolean("auto-update", true) && getDescription().getVersion().startsWith("DEV - ")) {
            new GitHubBuildsUpdater(this, getFile(), "J3fftw1/HeadLimiter/master").start();
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

    public Set<Group> getGroupSet() {
        return groups;
    }

    public int getLimitForItem(String slimefunItemId, Player player) {
        Set<Group> groupSet = HeadLimiter.getInstance().getGroupSet();
        for (Group group : groupSet) {
            if (group.getItems().contains(slimefunItemId)) {
                if (!group.getPermissionAmounts().isEmpty()) {
                    int highestPermissionAmount = -1;
                    for (Map.Entry<String, Integer> entry : group.getPermissionAmounts().entrySet()) {
                        if (player.hasPermission(entry.getKey()) || player.isOp()) {
                            highestPermissionAmount = Math.max(highestPermissionAmount, entry.getValue());
                        } else {
                            return group.getDefaultAmount();
                        }
                    }
                    return highestPermissionAmount;
                }
                return group.getDefaultAmount();
            }
        }
        return -1;
    }

    public void loadConfig() {
        ConfigurationSection configurationSection = instance.getConfig().getConfigurationSection("block-limits");
        for (String key : configurationSection.getKeys(false)) {
            groups.add(new Group(configurationSection.getConfigurationSection(key)));
        }
    }
}
