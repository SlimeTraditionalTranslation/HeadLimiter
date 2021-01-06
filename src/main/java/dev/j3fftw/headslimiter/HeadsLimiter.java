package dev.j3fftw.headslimiter;

import io.github.thebusybiscuit.slimefun4.implementation.SlimefunItems;
import java.io.File;
import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.SlimefunItem;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class HeadsLimiter extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {

        if (!new File(getDataFolder(), "config.yml").exists())
            saveDefaultConfig();

        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static boolean isCargo(SlimefunItem sfItem) {
        return sfItem.isItem(SlimefunItems.CARGO_MANAGER)
            || sfItem.isItem(SlimefunItems.CARGO_CONNECTOR_NODE)
            || sfItem.isItem(SlimefunItems.CARGO_INPUT_NODE)
            || sfItem.isItem(SlimefunItems.CARGO_OUTPUT_NODE_2)
            || sfItem.isItem(SlimefunItems.CARGO_OUTPUT_NODE);

    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        final SlimefunItem sfItem = SlimefunItem.getByItem(e.getItemInHand());
        if (e.getBlock().getType() == Material.PLAYER_HEAD
            && sfItem != null && isCargo(sfItem)
        ) {
            int i = 0;
            final Block block = e.getBlock();
            for (BlockState bs : block.getChunk().getTileEntities()) {
                final SlimefunItem slimefunItem = BlockStorage.check(bs.getLocation());
                if (slimefunItem != null && (isCargo(slimefunItem)))
                    i++;
            }

            int threshold = this.getConfig().getInt("amount");
            if (i >= threshold) {
                Bukkit.getScheduler().runTask(this, () -> block.getWorld().dropItemNaturally(block.getLocation(),
                    sfItem.getItem())
                );
                BlockStorage.clearBlockInfo(block.getLocation());
                Bukkit.getScheduler().runTask(this, () -> block.setType(Material.AIR));
                e.getPlayer().sendMessage(ChatColor.RED + "You hit the limit of Cargo nodes");
            }
        }
    }
}
