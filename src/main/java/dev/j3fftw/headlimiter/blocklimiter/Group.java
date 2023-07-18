package dev.j3fftw.headlimiter.blocklimiter;

import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Group {

    private final String groupName;
    private final int defaultAmount;
    private final HashSet<String> items;
    private final HashMap<String, Integer> permissionAmounts;

    public Group(ConfigurationSection configurationSection) {
        this.groupName = configurationSection.getName();
        this.defaultAmount = configurationSection.getInt("items-amount", 0);
        this.items = new HashSet<>(configurationSection.getStringList("items"));
        this.permissionAmounts = new HashMap<>();

        ConfigurationSection permissionSection = configurationSection.getConfigurationSection("permission-amount");

        if (permissionSection != null) {
            for (String key : permissionSection.getKeys(false)) {
                permissionAmounts.put(key, permissionSection.getInt(key, 0));
            }
        }
    }

    public String getGroupName() {
        return groupName;
    }

    public int getDefaultAmount() {
        return defaultAmount;
    }

    public Set<String> getItems() {
        return items;
    }

    public Map<String, Integer> getPermissionAmounts() {
        return permissionAmounts;
    }
}
