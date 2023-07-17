package dev.j3fftw.headlimiter.blocklimiter;

import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Group {

    private final String group;
    private final int itemAmount;
    private final HashSet<String> items;
    private final HashMap<String, Integer> permissionAmount;


    public Group(ConfigurationSection configurationSection) {
        this.group = configurationSection.getName();
        this.itemAmount = configurationSection.getInt("items-amount");
        this.items = new HashSet<>(configurationSection.getStringList("items"));
        this.permissionAmount = new HashMap<>();

        ConfigurationSection permissionAmountSection =
                configurationSection.getConfigurationSection("permission-amount");

        for (String key : permissionAmountSection.getKeys(false)) {
            permissionAmount.put(key, permissionAmountSection.getInt(key));
        }
    }

    public String getGroup() {
        return group;
    }

    public int getItemAmount() {
        return itemAmount;
    }

    public Set<String> getItems() {
        return items;
    }

    public Map<String, Integer> getPermissionAmount() {
        return permissionAmount;
    }
}
