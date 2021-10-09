package dev.j3fftw.headlimiter;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import org.bukkit.ChatColor;
import org.bukkit.block.BlockState;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.ParametersAreNonnullByDefault;

public class CountCommand implements CommandExecutor {

    private static final HeadLimiter INSTANCE = HeadLimiter.getInstance();

    private static final String[] IDS_TO_COUNT = {
            "CARGO_NODE_INPUT",
            "CARGO_NODE_OUTPUT",
            "CARGO_NODE_OUTPUT_ADVANCED",
            "CARGO_NODE",
            "CARGO_MANAGER"
    };

    @Override
    @ParametersAreNonnullByDefault
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (args.length == 1 && sender instanceof Player) {
            Player p = ((Player) sender);
            final BlockState[] te = p.getChunk().getTileEntities();
            INSTANCE.executorService.submit(() -> {
                int[] counts = new int[IDS_TO_COUNT.length];
                int total = 0;
                for (BlockState bs : te) {
                    final SlimefunItem slimefunItem = BlockStorage.check(bs.getLocation());
                    if (slimefunItem != null) {
                        for (int i = 0; i < IDS_TO_COUNT.length; i++) {
                            if (slimefunItem.getId().equals(IDS_TO_COUNT[i])) {
                                counts[i]++;
                                total++;
                                break;
                            }
                        }
                    }
                }
                StringBuilder message = new StringBuilder("Current count: ")
                        .append(total)
                        .append("/")
                        .append(INSTANCE.getConfig().getInt("amount"))
                        .append('\n');
                for (int i = 0; i < IDS_TO_COUNT.length; i++) {
                    if (counts[i] > 0) {
                        message.append(IDS_TO_COUNT[i])
                                .append(": ")
                                .append(counts[i])
                                .append('\n');
                    }
                }
                p.sendMessage(message.toString());
            });
        } else {
            sender.sendMessage(
                    ChatColor.GOLD + "/hl count" + ChatColor.GRAY + " Counts how many heads are in this chunk"
            );
        }

        return true;
    }

}
